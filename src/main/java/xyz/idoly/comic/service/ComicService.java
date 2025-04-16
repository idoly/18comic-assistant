package xyz.idoly.comic.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import xyz.idoly.comic.entity.Album;
import xyz.idoly.comic.entity.Comic;
import xyz.idoly.comic.entity.Photo;
import xyz.idoly.comic.repository.AlbumRepository;
import xyz.idoly.comic.repository.ComicRepository;
import xyz.idoly.comic.repository.PhotoRepository;

@Service
public class ComicService {

    private static final Log log = LogFactory.getLog(ComicService.class);

    private static final String JMCOMIC = "https://jmcmomic.github.io";

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    private static final int[] pieces = new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0";

    private static final Function<Element, Integer> data_index = e -> Integer.valueOf(e.attr("data-index"));
    private static final Function<Element, String> data_album = e -> e.attr("data-album");
    private static final Function<Element, Integer> data_page = e -> Integer.valueOf(e.attr("data-page"));
    private static final Function<Element, String> data_original = e -> e.attr("data-original").split("\\?")[0];

    @Resource
    private ThreadPoolTaskExecutor executor;

    @Resource
    private Configuration configuration;

    @Resource
    private ComicRepository comicRepository;

    @Resource 
    private AlbumRepository albumRepository;

    @Resource
    private PhotoRepository photoRepository;

    @Resource
    private RestClient restClient;

    private List<String> urls;

    private int retries;

    @PostConstruct
    public void init() {
        try {
            // 1. 
            urls = new ArrayList<>();
            String html = restClient.get().uri(JMCOMIC).retrieve().body(String.class);
            Elements as = Jsoup.parse(html).selectXpath("//button/a");
            for (Element a : as) {
                html = restClient.get().uri(JMCOMIC + a.attr("href")).retrieve().body(String.class);
                Pattern pattern = Pattern.compile("document\\.location\\s*=\\s*\"(https?://[^\"]+)\"");
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    urls.add(matcher.group(1));
                }
            }

            // 2. 
            Path temp = ROOT.resolve("comic", "temp");

            Files.createDirectories(temp);
            ImageIO.setUseCache(true);
            ImageIO.setCacheDirectory(temp.toFile());
        } catch (Exception e) {
            log.error("comic-service init failed: {}", e);
        } finally {
            retries = urls.size();
        }
    }

    // 实现轮询，权重，随机
    public String getUrl() {
        return "https://jmcomic-zzz.one";
        // return this.urls.getFirst();
    }

    public String request(String path, String id) {
        String uri = getUrl()  + "/" + path + "/" + id;

        int retry = 0;
        while (retry < retries) {
            try {
                return restClient.get()
                    .uri(uri)
                    .header("User-Agent", USER_AGENT)
                    .header("Origin", getUrl())
                    .header("Referer", getUrl())
                    .retrieve()
                    .body(String.class);
            } catch (Exception e) {
                retry++;
            }
            
        }

        return null;
    }

    @Transactional
    public Comic getComicById(String id)  {
        String html = request("album", id);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        String title = doc.selectXpath("//*[@id='book-name']").text();
        String cover = doc.selectXpath("//*[@id='album_photo_cover']/div/div[1]/div[1]/img[1]").attr("src").split("\\?")[0];
        Elements albums =  doc.selectXpath("//div[@id='episode-block']/div/div/ul/a");

        Comic comic = comicRepository.findById(id).orElse(new Comic(id, title, cover));

        if (albums.isEmpty()) {
            // single: data-page
            albums = doc.selectXpath("//div[contains(@class, 'read-block')]//a[contains(@class, 'reading')]");
            if (comic.getAlbums().size() == 0) {
                comic.getAlbums().add(getAlbumById(comic, id, 0));
            }
        } else {
            // multi album: data-album, data-index
            Map<String, Album> idToAlbum = new HashMap<>();
            comic.getAlbums().stream().forEach(album -> idToAlbum.put(album.getId(), album));
            comic.getAlbums().clear();

            albums.parallelStream()
                .map(album -> idToAlbum.getOrDefault(data_album.apply(album), getAlbumById(comic, data_album.apply(album), data_index.apply(album))))
                .sorted(Comparator.comparing(Album::getIndex))
                .forEachOrdered(comic.getAlbums()::add);
        }

        return comicRepository.saveAndFlush(comic);
    }

    private Album getAlbumById(Comic comic, String id, int index) {
        String html = request("photo", id);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        Elements photos = doc.selectXpath("//img[starts-with(@id, 'album_photo')]");

        Album album = new Album(comic, id, index, photos.size());

        photos.stream()
            .map(photo -> new Photo(album, data_page.apply(photo), data_original.apply(photo)))
            .sorted(Comparator.comparing(Photo::getIndex))
            .forEachOrdered(photo -> album.getPhotos().add(photo));

        return album;
    }

    public Comic downloadComic(String id) {
        Comic comic = getComicById(id);
        if (comic == null) {
            log.error("No comic found for ID: " + id);
        } else{
            template("comic.ftl", Map.of("comics", comicRepository.findAllByOrderByIdAsc()));
            template("album.ftl", Map.of("albums", comic.getAlbums()), id);
            
            for (Album album : comic.getAlbums()) {
                template("photo.ftl", Map.of("photos", album.getPhotos()), id, album.getId());
                album.getPhotos().stream().forEach(this::download);
    
                log.info("Album: [" + album.getId() + "] has been downloaded successfully.");
            }
    
            log.info("Comic: [" + id + "] has been downloaded successfully.");
        }

        return comic;
    }

    public Comic downloadComic(String id, int start, int end) {
        Comic comic = getComicById(id);
        if (comic == null) {
            log.error("No comic found for ID: " + id);
        } else{
            template("comic.ftl", Map.of("comics", comicRepository.findAllByOrderByIdAsc()));
            template("album.ftl", Map.of("albums", comic.getAlbums()), id);

            for (int i = Math.max(start, 0); i < Math.min(end, comic.getAlbums().size()); i++) {
                Album album = comic.getAlbums().get(i);
                template("photo.ftl", Map.of("photos", album.getPhotos()), id, album.getId());
                album.getPhotos().stream().forEach(photo -> {try { download(photo).get(); } catch (Exception e) {}});
                
                log.info("Album: [" + album.getId() + "] has been downloaded successfully.");
            }

            log.info("Comic: [" + id + ", start=" + start  + ", end= " + end +"] has been downloaded successfully.");
        }

        return comic;
    }

    public void zipComic(String id) {
        Comic comic = downloadComic(id);
        if (comic != null) {
            File zipFile = ROOT.resolve(comic.getTitle() + ".zip").toFile();
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (Album album : comic.getAlbums()) {
                    album.getPhotos().stream().forEach(photo -> {
                        try (InputStream in = new FileInputStream(download(photo).get())) {
                            zip.putNextEntry(new ZipEntry(album.getId() + "/" + photo.getIndex() + ".png"));
                            in.transferTo(zip);
                            zip.closeEntry();
                        } catch (Exception e) {
                            log.error("[album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "] packaged fail: ", e);
                        }
                    });
                }
    
                log.info("Comic: [" + id + "] has been packaged successfully.");
            } catch (Exception e) {
                log.info("Comic: [" + id + "] packaged fail: ", e);
            } 
        }
    }

    public void zipComic(String id,  int start, int end) {
        Comic comic = downloadComic(id);
        if (comic != null) {
            File zipFile = ROOT.resolve(comic.getTitle() + ".zip").toFile();
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (int i = Math.max(start, 0); i < Math.min(end, comic.getAlbums().size()); i++) {
                    Album album = comic.getAlbums().get(i);
                    album.getPhotos().stream().forEach(photo -> {
                        try (InputStream in = new FileInputStream(this.download(photo).get())) {
                            zip.putNextEntry(new ZipEntry(album.getId() + "/" + photo.getIndex() + ".png"));
                            in.transferTo(zip);
                            zip.closeEntry();
                        } catch (Exception e) {
                            log.error("[album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "] packaged fail: ", e);
                        }
                    });

                    log.info("Album: [" + album.getId() + "] has been downloaded successfully.");
                }

                log.info("Comic: [" + id + ", start=" + start  + ", end= " + end + "] has been packaged successfully.");
            } catch (Exception e) {
                log.info("Comic: [" + id + ", start=" + start  + ", end= " + end + "] packaged fail: ", e);
            } 
        }
    }

    @Transactional
    private Album getAlbumById(String id)  {
        return albumRepository.findById(id).orElseGet(() -> {
            // 查询 comicId
            String html = request("photo", id);
            if (html == null) return null;
            Document doc = Jsoup.parse(html);
            String comicId = doc.selectXpath("//*[@id='series_id']").attr("value");
            Elements photos = doc.selectXpath("//img[starts-with(@id, 'album_photo')]");

            // 查询 album_index
            html = request("album", comicId);
            if (html == null) return null;
            doc = Jsoup.parse(html);
            String title = doc.selectXpath("//*[@id='book-name']").text();
            String cover = doc.selectXpath("//*[@id='album_photo_cover']/div/div[1]/div[1]/img[1]").attr("src").split("\\?")[0];
            Elements elements =  doc.selectXpath("//div[@id='episode-block']/div/div/ul/a");
            if (elements.isEmpty()) {
                elements = doc.selectXpath("//div[contains(@class, 'read-block')]//a[contains(@class, 'reading')]");
            }

            Comic comic = comicRepository.findById(comicId).orElse(new Comic(comicId, title, cover));

            Element element = elements.stream().filter(album -> !data_album.apply(album).equals(id)).findFirst().get();
            Album album = new Album(comic, id, data_index.apply(element), photos.size());
            photos.stream()
                .map(photo -> new Photo(album, data_page.apply(photo), data_original.apply(photo)))
                .sorted(Comparator.comparing(Photo::getIndex))
                .forEachOrdered(album.getPhotos()::add);

            comic.getAlbums().add(album);
            comic.getAlbums().sort(Comparator.comparing(Album::getIndex));
            comicRepository.saveAndFlush(comic);

            return album;
        });
    }

    public Album downloadAlbum(String id) {
        Album album = getAlbumById(id);

        if (album == null) {
            log.error("No album found for ID: " + id);
        } else {
            Comic comic = album.getComic();
            template("comic.ftl", Map.of("comics", comicRepository.findAllByOrderByIdAsc()));
            template("album.ftl", Map.of("albums", comic.getAlbums()), comic.getId());
            template("photo.ftl", Map.of("photos", album.getPhotos()), comic.getId(), id);
    
            album.getPhotos().stream().forEach(this::download);
        
            log.info("Comic: [" + comic.getId() + "] , Album: [" + id + "] has been downloaded successfully.");
        }

        return album;
    }

    public void zipAlbum(String id) {
        Album album = downloadAlbum(id);
        if (album != null) {
            File zipFile = ROOT.resolve(album.getComic().getTitle() + "- EP" + album.getIndex() + ".zip").toFile();
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
                album.getPhotos().stream().forEach(photo -> {
                    try (InputStream in = new FileInputStream(this.download(photo).get())) {
                        zip.putNextEntry(new ZipEntry(album.getId() + "/" + photo.getIndex() + ".png"));
                        in.transferTo(zip);
                        zip.closeEntry();
                    } catch (Exception e) {
                        log.error("zip [album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "] fail: ", e);
                    }
                });
    
                log.info("zip album(" + id + ") has been downloaded successfully.");
            } catch (Exception e) {
                log.error("zip album(" + id + ") fail: ", e);
            } 
        }
    }


    @Transactional
    private Future<File> download(Photo photo) {
        return executor.submit((Callable<File>) () -> {
            String comicId = photo.getAlbum().getComic().getId();
            String albumId = photo.getAlbum().getId();
            Integer photoIndex = photo.getIndex();
            String photoOrigin = photo.getOrigin();

            File file = path(comicId, albumId, photoIndex + ".png").toFile();
            try {

                if (file.exists()) {
                    photo.setStatus(true);
                    photoRepository.saveAndFlush(photo);
                } else {
                    ResponseEntity<byte[]> response = restClient.get().uri(URI.create(photoOrigin)).retrieve().toEntity(byte[].class);
                    if (response.getBody() == null) {
                        log.error("[album=" + albumId + ", photoIndex=" + photoIndex + ", origin=" + photoOrigin +"]: reponse.getBody() == null");
                        return null;
                    }
        
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
                    if(ImageIO.write(reverse(image, pieces(Integer.valueOf(albumId), photoIndex)), "png", file)) {
                        photo.setStatus(true);
                        photoRepository.saveAndFlush(photo);
                    }
                }
            } catch (Exception e) {
                log.error("[album=" + albumId + ", photoIndex=" + photoIndex + ", origin=" + photoOrigin +"]: download failed", e);
            } 

            return file;
        });
    }

    private int pieces(int albumId, int photoIndex) {
        String md5 = DigestUtils.md5DigestAsHex((albumId + String.format("%05d", photoIndex + 1)).getBytes());
        int c = md5.charAt(md5.length() - 1);
        if (albumId < 220980) {
            return 1;
        } else if (albumId >= 268850) {
            return pieces[c % (albumId <= 421925 ? 10 : 8)];
        } 

        return 10;
    }

    private BufferedImage reverse(BufferedImage image, int piece) {
        if (piece == 1) return image;

        int height = image.getHeight(), width = image.getWidth();
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        Graphics2D graphics = newImage.createGraphics();

        for (int i = 0, subImageHeight = height / piece; i < piece; i++) {
            int subImageY = i * subImageHeight;
            int newIamgeY = (piece - 1 - i) * subImageHeight;

            subImageHeight = (i == piece - 1) ? height - subImageY : subImageHeight;
            if(subImageHeight <= 0) {
                continue;
            }

            BufferedImage subImage = image.getSubimage(0, subImageY, width, subImageHeight);
            graphics.drawImage(subImage, 0, newIamgeY, null);
        }
    
        graphics.dispose();
        return newImage;
    }

    private void template(String name, Map<String, Object> params, String... paths) {
        Path path = path(paths);
        try {
            Files.createDirectories(path.getParent());
            Template template = configuration.getTemplate(name);
            try (FileWriter writer = new FileWriter(path.toFile())) {
                template.process(params, writer);
            }
        } catch (IOException | TemplateException e) {
            log.error("Failed to render template [" + name + "] to ["+ path +"]", e);
        }
    }

    private Path path(String... paths) {

        Path path = ROOT;
    
        if (paths == null || paths.length == 0) {
            return path.resolve("index.html");
        }

        if (paths.length == 1) {
            return path.resolve("comic").resolve(paths[0]).resolve("index.html");
        }
    
        if (paths.length == 2) {
            return path.resolve("comic").resolve(paths[0]).resolve("album").resolve(paths[1]).resolve("index.html");
        }
    
        if (paths.length == 3) {
            return path.resolve("comic").resolve(paths[0]).resolve("album").resolve(paths[1]).resolve(paths[2]);
        }
    
        for (String part : paths) {
            path = path.resolve(part);
        }

        return path;
    }
}
