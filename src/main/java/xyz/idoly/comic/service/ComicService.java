package xyz.idoly.comic.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

import com.microsoft.playwright.BrowserContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import xyz.idoly.comic.utils.BrowserClient;

/**
 * https://jmcmomic.github.io/
 * 
 * 轮询/权重域名 + 失败重试
 */
@Service
@ConfigurationProperties(prefix = "spring.comic")
public class ComicService {

    private static final Comparator<Element> comparator = (e1,e2) -> Integer.parseInt(e1.attr("data-page")) - Integer.parseInt(e2.attr("data-page"));
    private static final Function<Element, String> original = e -> e.attr("data-original").split("\\?")[0];
    private static final int[] PIECES = new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

    // @Resource
    // private ComicProperties properties;

    private List<String> hosts;

    private String username;
    
    private String password;

    private String userAgent;

    private int retries = 3;

    @Resource
    private RestClient restClient;

    // @Resource
    // private BrowserClient browserClient;

    // https://18comic.vip/album/140709
    // public List<String> albums(String id) {
    //     String html = browserClient.navigate("/album/" + id);
    //     Document doc = Jsoup.parse(html);
    //     Elements elements =  doc.selectXpath("//div[@id='episode-block']/div/div/ul/a");
    //     if (elements.isEmpty()) {
    //         elements = doc.selectXpath("//div[contains(@class, 'read-block')]//a[contains(@class, 'reading')]");
    //     }

    //     return elements.stream().map(element -> element.attr("href")).toList();
    // }

    // // https://18comic.vip/photo/146420
    // public List<String> photos(String uri) {
    //     String html = browserClient.navigate(uri);
    //     System.out.println(html);
    //     Document doc = Jsoup.parse(html);
    //     Elements elements = doc.selectXpath("//img[starts-with(@id, 'album_photo')]");    
    //     return elements.stream().sorted(comparator).map(original).toList();
    // }

    // https://18comic.vip + /album/ + 140709
    // public List<String> albums(String id) {
    //     final String uri = properties.getHost() + "/album/" + id;
    //     final String html = restClient.get().uri(uri).retrieve().body(String.class);
    //     System.out.println(html);

    //     Document doc = Jsoup.parse(html);
    //     Elements elements =  doc.selectXpath("//div[@id='episode-block']/div/div/ul/a");
    //     if (elements.isEmpty()) {
    //         elements = doc.selectXpath("//div[contains(@class, 'read-block')]//a[contains(@class, 'reading')]");
    //     }

    //     return elements.stream().map(element -> element.attr("href")).toList();
    // }

    // // https://18comic.vip + /photo/146420
    // public List<String> photos(String path) {
    //     final String uri = properties.getHost() +  path;
    //     final String html = restClient.get().uri(uri).retrieve().body(String.class);

    //     System.out.println(html);
    //     Document doc = Jsoup.parse(html);
    //     Elements elements = doc.selectXpath("//img[starts-with(@id, 'album_photo')]");    
    //     return elements.stream().sorted(comparator).map(original).toList();
    // }

    // https://cdn-msp3.18comic.vip/media/photos/550437/00001.webp
    public void download(String uri) {
        Matcher matcher = Pattern.compile("/(\\d+)/(\\d+)\\.").matcher(uri);
        if (matcher.find()) {
            int albumId = Integer.parseInt(matcher.group(1));
            String photoIndex = matcher.group(2);
            try {
                ResponseEntity<byte[]> response = restClient.get().uri(URI.create(uri)).retrieve().toEntity(byte[].class);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
                File file = new File("/vertx-comic/" + albumId +  "-" + photoIndex + ".png");
                ImageIO.write(reverse(image, pieces(albumId, photoIndex)),"png", file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int pieces(int albumId, String photoIndex) {
        String md5 = DigestUtils.md5DigestAsHex((albumId + photoIndex).getBytes());
        int c = md5.charAt(md5.length() - 1);
        if (albumId < 220980) {
            return 1;
        } else if (albumId >= 268850) {
            return PIECES[c % (albumId <= 421925 ? 10 : 8)];
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
            subImageHeight = i == piece - 1 ? height - i * subImageHeight : subImageHeight;
            BufferedImage subImage = image.getSubimage(0, subImageY, width, subImageHeight);
            graphics.drawImage(subImage, 0, newIamgeY, null);
        }
    
        graphics.dispose();
        return newImage;
    }
    
}
