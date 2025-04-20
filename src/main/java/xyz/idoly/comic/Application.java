package xyz.idoly.comic;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import xyz.idoly.comic.entity.Album;
import xyz.idoly.comic.entity.Comic;
import xyz.idoly.comic.repository.AlbumRepository;
import xyz.idoly.comic.repository.ComicRepository;
import xyz.idoly.comic.service.ComicService;

@SpringBootApplication
public class Application {

	@Resource
	private ComicRepository comicRepository;

	@Resource
	private AlbumRepository albumRepository;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner runner(ComicService comicService) {
		return args -> {

			// // 漫画：多章节
			// comicService.downloadComic("144410");
			// comicService.zipComic("144410");
			
			// // 漫画：单章节
			// comicService.downloadComic("1128801");
			// comicService.zipComic("1128801");

			// // 漫画：区间章节
			// comicService.downloadComic("1085608", 6, 8);
			// comicService.zipComic("195818",0,2);

			// // 随机：多章节-漫画
			// comicService.downloadAlbum("585239");
			// comicService.downloadAlbum("1114300");
			// comicService.zipAlbum("585237");

			// // 随机：单章节-漫画
			// comicService.downloadAlbum("58");
			// comicService.zipAlbum("58");

			// // 随机多章节
			// comicService.downloadAlbum("1128061", "585238");
			// comicService.zipAlbum("1128061", "585238");
		};
	}

}