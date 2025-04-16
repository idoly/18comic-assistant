package xyz.idoly.comic;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import xyz.idoly.comic.service.ComicService;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner runner(ComicService comicService) {
		return args -> {
			comicService.downloadComic("195818",0,3);
			comicService.downloadComic("584487",0,3);
			comicService.downloadComic("1128578",0,3);

			comicService.downloadComic("53",0,3);
			comicService.downloadComic("58",0,3);

			comicService.downloadAlbum("1100555");
		};
	}

}