package xyz.idoly.comic;

import java.net.CookieManager;
import java.net.HttpCookie;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import jakarta.annotation.Resource;
import xyz.idoly.comic.service.ComicService;

@SpringBootApplication
public class Application {

	@Resource
	private CookieManager cookieManager;


	// @Resource
	// private ComicProperties comicProperties;

	@Resource
	private RestClient restClient;

	@Resource
	private ComicService comicService;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	// /photo/195818
	// https://cdn-msp3.18comic.org/media/photos/195818/00000.webp
	@Bean
	public CommandLineRunner runner() {
		return (args) -> {

			String rootPath = System.getProperty("user.dir");
			System.out.println("当前项目根路径: " + rootPath);
			// try {
			// 	String response = restClient.post().uri("/login")
			// 	.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
			// 	.header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
			// 	.header(HttpHeaders.ORIGIN, comicProperties.getHost())
			// 	.header(HttpHeaders.REFERER, comicProperties.getHost() + "/")
			// 	.body("username=" + comicProperties.getUsername() + "&password=" + comicProperties.getPassword() + "&id_remember=on&login_remember=on&submit_login=1")
			// 	.retrieve().body(String.class);

			// 	System.out.println(response);
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// } 

			// for (String photo : comicService.photos("/photo/1121834")) {
				// https://cdn-msp.18comic.vip/media/photos/1121834/00001.webp
				// comicService.download("https://cdn-msp.18comic.vip/media/photos/1121834/00007.webp");
				// comicService.download("https://cdn-msp.18comic.vip/media/photos/1121834/00008.webp");
				// comicService.download("https://cdn-msp.18comic.vip/media/photos/1121834/00009.webp");


			// }
		};

	}

}