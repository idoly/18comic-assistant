package xyz.idoly.comic.service;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import xyz.idoly.comic.config.AppConfig;
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

    private static final int[] PIECES = new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

    private static final AtomicInteger urlIndex = new AtomicInteger(0);

    @Resource
    private AppConfig config;

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

    private int retries = 3;

    private Path ROOT;

    private String url;

    @PostConstruct
    public void init() {
        try {
            // 1.
            ROOT = Path.of(config.getPath());

            // 2.
            createTempDir();

            // 3. 
            if (config.getUrls().isEmpty()) {
                List<String> urls = fetchUrls();
                config.setUrls(urls);
            }

            log.info("comic urls: " + config.getUrls());

            // 4.
            if (Strings.isNotBlank(config.getUsername()) && Strings.isNotBlank(config.getPassword())) {
                login(config.getUsername(), config.getPassword());
            } 
        } catch (Exception e) {
            log.error("comic-service init failed: {}", e);
        } finally {
            retries = config.getUrls().size();
        }
    }

    private void setUrl(String url) {
        this.url = url;
    }

    private String getUrl() {
        if (url == null) {
            List<String> urls = config.getUrls();
            if (urls == null || urls.isEmpty()) {
                throw new IllegalStateException("URL list is empty, did you forget to fetch URLs?");
            }
            return urls.get(urlIndex.getAndIncrement() % urls.size());
        } 

        return url;
    }

    private void createTempDir() throws IOException {
        Path temp = ROOT.resolve("comic", "temp");
        Files.createDirectories(temp);
        ImageIO.setUseCache(true);
        ImageIO.setCacheDirectory(temp.toFile());
    }

    private List<String> fetchUrls() throws IOException {
        String html = request(JMCOMIC, String.class);
        if (html == null) return new ArrayList<>();

        List<String> urls = new ArrayList<>();
        Elements urlElements = Jsoup.parse(html).selectXpath("//button/a");
        urlElements.stream().forEach(urlElement -> {
            String urlHtml = request(JMCOMIC + urlElement.attr("href"), String.class);
            Pattern pattern = Pattern.compile("document\\.location\\s*=\\s*\"(https?://[^\"]+)\"");
            Matcher matcher = pattern.matcher(urlHtml);
            if (matcher.find()) {
                urls.add(matcher.group(1));
            }
        });

        return urls;
    }

    private void login(String username, String password) {
        String url = getUrl();
        try {
            ResponseEntity<String> response = restClient.post()
                .uri(url + "/login")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .body("username=" + username + "&password=" + password + "&id_remember=on&login_remember=on&submit_login=1")
                .retrieve()
                .toEntity(String.class);

            if (!response.getStatusCode().is2xxSuccessful()) throw new Exception(response.getBody());
            
            setUrl(url);
            downloadFavorites(username);

            log.info("Login succeeded for user[" + username + "] to " + url);
        } catch (Exception e) {
            log.error("Login failed for user[" + username + "] to " + url + ": " + e.getMessage());
        }
    }

    private void downloadFavorites(String username) {
        String html = requestFavorites(username);
        if (html == null) return;

        Document doc = Jsoup.parse(html);
        Elements comicElements = getComicElements(doc);
        comicElements.stream().map(comicElement -> Id.apply(comicElement)).forEach(this::downloadComic);
    }

    private String requestFavorites(String username) {
        return request(getUrl() + "/user/" + username + "/favorite/albums", String.class);
    }

    private String requestComic(String id) {
        return request(getUrl() + "/album/" + id, String.class);
    }

    private String requestAlbum(String id) {
        return request(getUrl() + "/photo/" + id, String.class);
    }
    
    private <T> T request(String uri, Class<T> responseType) {
        for (int retry = 0; retry < retries; retry++) {
            try {
                ResponseEntity<T> response = restClient.get().uri(uri).retrieve().toEntity(responseType);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new Exception(response.getStatusCode().toString());
                }

                return response.getBody();
            } catch (Exception e) {
                log.warn("Request failed for uri: " + uri + ", retrying " + (retry + 1) + "/" + retries + ", e: " + e.getMessage());
                try {
                    Thread.sleep(1000 );
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); 
                    log.warn("Retry sleep interrupted" + ie.getMessage());
                    break; 
                }
            }
        }
        log.error("Request failed after " + retries + " attempts: " + uri);
        return null;
    }

    private Comic getComicById(String id, int start, int end) {
        String html = requestComic(id);
        if (html == null) return null;
    
        Document doc = Jsoup.parse(html);
        String title = getTitle(doc);
        List<String> covers = getCovers(doc);
        Elements albumElements = getAlbumElements(doc);
    
        Comic comic = comicRepository.findById(id).orElseGet(() -> comicRepository.saveAndFlush(new Comic(id, title, covers)));
        Map<String, Album> idToAlbum = comic.getAlbums().stream().collect(Collectors.toMap(Album::getId, Function.identity()));
    
        List<Album> startToEnd = new ArrayList<>();
    
        if (albumElements.isEmpty()) {
            startToEnd.add(idToAlbum.containsKey(id) ? idToAlbum.get(id) : getAlbumById(comic, id, 0));
        } else {
            albumElements.stream()
                .filter(albumElement -> {
                    int index = dataIndex.apply(albumElement);
                    return index >= Math.max(start, 0) && index < Math.min(end, albumElements.size());
                })
                .map(albumElement -> {
                    // don't getOrDefault
                    String albumId = dataAlbum.apply(albumElement);
                    return idToAlbum.containsKey(albumId) ? idToAlbum.get(albumId) : getAlbumById(comic, dataAlbum.apply(albumElement), dataIndex.apply(albumElement));
                })
                .sorted(Comparator.comparing(Album::getIndex))
                .forEachOrdered(startToEnd::add); 
        }
    
        comic.setAlbums(startToEnd);

        return comic;
    }

    private Album getAlbumById(Comic comic, String id, int index) {
        String html = requestAlbum(id);
        if (html == null) return new Album(comic, id, index);

        Document doc = Jsoup.parse(html);
        Elements photoElements = getPhotoElements(doc);

        Album album = new Album(comic, id, index, photoElements.size());

        photoElements.stream()
        .map(photo -> new Photo(album, dataPage.apply(photo), dataOriginal.apply(photo)))
        .sorted(Comparator.comparing(Photo::getIndex))
        .forEachOrdered(album.getPhotos()::add);

        return albumRepository.saveAndFlush(album);
    }
    
    public Comic downloadComic(String id) {
        return downloadComic(id, 0, Integer.MAX_VALUE);
    }
    
    public Comic downloadComic(String id, Integer start, Integer end) {
        Comic comic = getComicById(id, start, end);
        if (comic == null) {
            log.warn("Comic [" + id + "] not found");
        } else {

            for (int i = 0; i < comic.getCovers().size(); i++) download(comic.getId(), i, comic.getCovers().get(i));
            templateIndex();

            List<Album.WithoutPhotos> albums = albumRepository.findAllByComicOrderByIndexAsc(comic);
            templateComic(comic, albums);

            comic.getAlbums().stream().forEach(album -> {
                album.getPhotos().parallelStream().forEach(photo -> {
                    try {
                        download(photo);
                    } catch (Exception e) {
                        // Exception already logged inside download(photo)
                    }
                });

                templateAlbum(comic, albums, album);
                log.info("Album [" + album.getId() + "] has been downloaded successfully.");
            });

            log.info("Comic [" + id + "] (start=" + start + ", end=" + (start + comic.getAlbums().size()) + ") has been downloaded successfully.");
        }
    
        return comic;
    }
    
    public void zipComic(String id) {
        zipComic(id, 0, Integer.MAX_VALUE);
    }

    public void zipComic(String id, int start, int end) {
        if (start >= end) return;

        Comic comic = downloadComic(id, start, end);
        if (comic == null) return;

        start = Math.max(start, 0);
        end = start + comic.getAlbums().size();
        String sanitizedTitle = comic.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
        File zipFile = ROOT.resolve(sanitizedTitle + "[" + start + "-" + end + "].zip").toFile();
        if (zipFile.exists()) return;

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
            comic.getAlbums().stream().forEach(album -> {
                album.getPhotos().parallelStream().forEach(photo -> {
                    try {
                        File file = download(photo);
                        if (file == null || !file.exists()) throw new Exception("file not found");
                
                        try (InputStream in = new FileInputStream(file)) {
                            zip.putNextEntry(new ZipEntry(album.getId() + "/" + photo.getIndex() + ".png"));
                            in.transferTo(zip);
                            zip.closeEntry();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to package photo [album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "] " + e.getMessage());
                    }
                });

                log.info("Packaged album [" + album.getId() +"] successfully.");
            });

        } catch (Exception e) {
            log.info("Failed to package comic [" + id + "], range: [" + start + "-" + end + "]: " + e.getMessage());
        }
    }

    private Album getAlbumById(String id)  {
        return albumRepository.findById(id).orElseGet(() -> {
            String html = requestAlbum(id);
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            String comicId = getComicId(doc).orElse(id);
            Elements photoElements = getPhotoElements(doc);

            html = requestComic(id);
            if (html == null) return null;

            doc = Jsoup.parse(html);
            String title = getTitle(doc);
            List<String> covers = getCovers(doc);
            Elements albumElements = getAlbumElements(doc);
        
            Comic comic = comicRepository.findById(comicId).orElseGet(() -> comicRepository.saveAndFlush(new Comic(comicId, title, covers)));

            Album album = albumElements.stream()
                .filter(albumElement -> dataAlbum.apply(albumElement).equals(id))
                .findFirst()
                .map(albumElement -> new Album(comic, id, dataIndex.apply(albumElement), photoElements.size()))
                .orElseGet(() -> new Album(comic, id, 0, photoElements.size()));

            photoElements.stream()
                .map(photo -> new Photo(album, dataPage.apply(photo), dataOriginal.apply(photo)))
                .sorted(Comparator.comparing(Photo::getIndex))
                .forEachOrdered(album.getPhotos()::add);

            return albumRepository.saveAndFlush(album);
        });
    }
    
    public Album downloadAlbum(String id) {
        List<Album> albums = downloadAlbum(new String[]{id});
        return albums.isEmpty() ? null : albums.get(0);
    }

    public List<Album> downloadAlbum(String... ids) {
        if (ids == null || ids.length == 0) return List.of();
        
        List<Album> albums = new ArrayList<>();
        for (String id : ids) {
            Album album = getAlbumById(id);
            if (album == null) {
                log.error("No album found for ID: " + id);
            } else {
                albums.add(album);
                album.getPhotos().parallelStream().forEach(this::download);
                Comic comic = album.getComic();
                templateIndex();
                templateComic(comic, null);
                templateAlbum(comic, null, album);
                log.info("Comic: [" + comic.getId() + "] , Album: [" + id + "] has been downloaded successfully.");
            }
        }

        return albums;
    }

    public void zipAlbum(String id) {
        zipAlbum(new String[]{id});
    }

    public void zipAlbum(String... ids) {
        if (ids == null || ids.length == 0) return;

        File zipFile = ROOT.resolve("[" + String.join(",", ids) +"]" + ".zip").toFile();
        if (zipFile.exists()) return;

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String id : ids) {
                Album album = downloadAlbum(id);
                if (album == null) continue;

                album.getPhotos().parallelStream().forEach(photo -> {
                    try {
                        File file = download(photo);
                        if (file == null || !file.exists()) {
                            log.warn("Photo file not found for [album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "]");
                            return;
                        }
                
                        try (InputStream in = new FileInputStream(file)) {
                            zip.putNextEntry(new ZipEntry(album.getId() + "/" + photo.getIndex() + ".png"));
                            in.transferTo(zip);
                            zip.closeEntry();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to package photo [album=" + album.getId() + ", photoIndex=" + photo.getIndex() + "]: " + e.getMessage());
                    }
                });

                log.info("Album [" + album.getId() + "] packaged successfully.");
            }

            log.info("Zip file " + zipFile.getName() + " created successfully.");
        } catch (Exception e) {
            log.error("Failed to create zip file [" + zipFile.getName() + "]: " + e.getMessage());
        } 
    }

    private File download(Photo photo) {
        String comicId = photo.getAlbum().getComic().getId();
        String albumId = photo.getAlbum().getId();
        Integer photoIndex = photo.getIndex();
        String photoOrigin = photo.getOrigin();

        File file = file(comicId, albumId, photoIndex + ".png");
        try {
            BufferedImage image = null;
            if (file.exists()) {
                image = ImageIO.read(new FileInputStream(file));
            } else {
                byte[] body = request(photoOrigin, byte[].class);
                if (body == null || body.length == 0) throw new Exception("response body is empty");
                
                image = ImageIO.read(new ByteArrayInputStream(body));
                if (image == null) throw new Exception("image decode failed");

                int pieces = pieces(Integer.parseInt(albumId), photoIndex);
                BufferedImage processed = reverse(image, pieces);

                boolean success = ImageIO.write(processed, "png", file);
                if (!success) throw new Exception("failed to write image to file");
            }
            
            photo.setStatus(true);
            photo.setWidth(image.getWidth());
            photo.setHeight(image.getHeight());
            photoRepository.saveAndFlush(photo);
        } catch (Exception e) {
            file = null;
            log.warn("[album=" + albumId + ", photoIndex=" + photoIndex + ", origin=" + photoOrigin + "] " + e.getMessage());
        } 

        return file;
    }
    
    private void download(String comic, int index, String url) {
        try {
            BufferedImage image = null;

            File file = file(comic, index + ".png");
            if (file.exists()) return;

            byte[] body = request(url, byte[].class);;
            if (body == null || body.length == 0) throw new Exception("response body is empty");
            
            image = ImageIO.read(new ByteArrayInputStream(body));
            if (image == null) throw new Exception("image decode failed");

            boolean success = ImageIO.write(image, "png", file);
            if (!success) throw new Exception("failed to write image to file");
        } catch (Exception e) {
            log.warn("[comic=" + comic + ", index=" + index + ", origin=" + url + "] " + e.getMessage());
        } 
    }

    private int pieces(int albumId, int photoIndex) {
        if (albumId < 220980) {
            return 1;
        } 
        
        if (albumId >= 268850) {
            String key = albumId + String.format("%05d", photoIndex + 1);
            String md5 = DigestUtils.md5DigestAsHex(key.getBytes());

            int c = md5.charAt(md5.length() - 1);
            int mod = (albumId <= 421925) ? 10 : 8; 
            return PIECES[c % mod];
        } 

        return 10;
    }

    private BufferedImage reverse(BufferedImage image, int piece) {
        if (piece <= 1 || image.getHeight() < 2) return image;
    
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int baseHeight = height / piece;
        final int remainder = height % piece;
    
        int[] subHeights = new int[piece];
        int[] srcYs = new int[piece];
        for (int i = 0, y = 0; i < piece; y += subHeights[i], i++) {
            subHeights[i] = baseHeight + (i == piece - 1 ? remainder : 0);
            srcYs[i] = y;
        }
    
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        WritableRaster source = image.getRaster();
        WritableRaster target = newImage.getRaster();
    
        IntStream stream = IntStream.range(0, piece);
        if (piece > Runtime.getRuntime().availableProcessors()) {
            stream = stream.parallel();
        }
        stream.forEach(i -> {
            int sh = subHeights[i];
            int destY = height - (srcYs[i] + sh);
            int[] buffer = new int[width * sh * source.getNumBands()];
            source.getPixels(0, srcYs[i], width, sh, buffer);
            target.setPixels(0, destY, width, sh, buffer);
        });
    
        return newImage;
    }
    
    private void templateIndex() {
        template("index.ftl", Map.of("comics", comicRepository.findAllByOrderByIdAsc()));
    }

    private void templateComic(Comic comic, Object albums) {
        template("comic.ftl",
            Map.of(
                "comic", comic, 
                "albums", Optional.ofNullable(albums).orElse(comic.getAlbums())
            ), comic.getId()
        );
    }

    private void templateAlbum(Comic comic, Object albums, Album album) {
        template("album.ftl", 
            Map.of(
                "comic", comic, 
                "albums", Optional.ofNullable(albums).orElse(comic.getAlbums()),
                "album", album
            ), comic.getId(), album.getId()
        );
    }

    private void template(String name, Map<String, Object> params, String... paths) {
        File file = file(paths);
        try {
            Template template = configuration.getTemplate(name);
            try (FileWriter writer = new FileWriter(file)) {
                template.process(params, writer);
            }
        } catch (IOException | TemplateException e) {
            log.error("Failed to render template [" + name + "] to ["+ file.getName() +"]", e);
        }
    }

    private File file(String... paths) {
        Path path = ROOT;

        // 0 index.html
        if (paths == null || paths.length == 0) {
            path =  path.resolve("index.html");
        };

        // 1 comic/ path[0] / index.html
        if (paths.length == 1) {
            path =  path.resolve("comic").resolve(paths[0]).resolve("index.html");
        }

        // 2 comic/ path[0] / path[1]
        // 2 comic/ path[0] / path[1] / index.html
        if (paths.length == 2) {
            path = path.resolve("comic").resolve(paths[0]);
            if (paths[1].contains(".") && !paths[1].endsWith(".")) {
                path = path.resolve(paths[1]);
            } else {
                path = path.resolve("album").resolve(paths[1]).resolve("index.html");
            }
        }

        // 3 comic/ path[0] / album / path[1] / path[2]
        if (paths.length == 3) {
            path =  path.resolve("comic").resolve(paths[0])
                    .resolve("album").resolve(paths[1])
                    .resolve(paths[2]);
        }

        try {
            File parent = path.getParent().toFile();
            if (!parent.exists()) Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return path.toFile();
    }

    private String getTitle(Document doc) {
        return doc.select("#book-name").text();
    }

    private List<String> getCovers(Document doc) {
        List<String> covers = new ArrayList<>();
        
        Elements coverElements = doc.select("#album_photo_cover > div.thumb-overlay > a:nth-child(2) > div > img");
        coverElements.stream().map(coverElement -> src.apply(coverElement)).forEach(covers::add);
        if (!covers.isEmpty()) {
            String cover = covers.get(0);
            int index = cover.lastIndexOf(".");
            covers.addFirst(cover.substring(0, index) + "_3x4" + cover.substring(index));
        }

        return covers;
    }

    private Optional<String> getComicId(Document doc) {
        return Optional.ofNullable(doc.select("#series_id").attr("value")).filter(s -> !s.isEmpty());
    }

    private Elements getComicElements(Document doc) {
        return doc.select("[id^=favorites_album_]");
    }

    private Elements getAlbumElements(Document doc) {
        return doc.select("#episode-block > div > div > ul > a");
    }

    private Elements getPhotoElements(Document doc) {
        return doc.select("img[id^=album_photo_]");
    }

    private Function<Element, String> Id = e -> e.id().substring("favorites_album_".length());
    private Function<Element, String> src = e -> e.attr("src").split("\\?")[0];
    private Function<Element, Integer> dataIndex = e -> Integer.valueOf(e.attr("data-index"));
    private Function<Element, String> dataAlbum = e -> e.attr("data-album");
    private Function<Element, Integer> dataPage = e -> Integer.valueOf(e.attr("data-page"));
    private Function<Element, String> dataOriginal = e -> e.attr("data-original").split("\\?")[0];

    /*************************************************************************************************************************** */

    @Tool(description = "根据 comic ID 获取 Comic 对象")
    public Comic search(String id) {
        String html = requestComic(id);
        if (html == null) return null;

        Document doc = Jsoup.parse(html);
        String title = getTitle(doc);
        List<String> covers = getCovers(doc);
        Elements albumElements = getAlbumElements(doc);

        Comic comic = new Comic(id,title, covers);
        if (albumElements.isEmpty()) {
            comic.getAlbums().add(new Album(id, 0));
        } else {
            albumElements.stream()
                .map(albumElement -> new Album(dataAlbum.apply(albumElement), dataIndex.apply(albumElement)))
                .sorted(Comparator.comparing(Album::getIndex))
                .forEachOrdered(comic.getAlbums()::add);
        }
        
        return comic;
    }

    @Tool(description = "根据 comic ID 更新 Comic 对象")
    public void getComicById(String id) {
        getComicById(id, 0, Integer.MAX_VALUE);
    }
}
