package xyz.idoly.comic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Album;
import xyz.idoly.comic.entity.Comic;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {

    // 根据 Comic 查所有 Album（自动按 comic_id 外键查）
    List<Album> findByComic(Comic comic);

    // 也可以根据 comic id 字符串查
    List<Album> findByComicId(Integer comicId);

    // 根据 comic id 和 status 查
    List<Album> findByComicIdAndStatus(Integer comicId, Boolean status);
}