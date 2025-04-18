package xyz.idoly.comic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Album;
import xyz.idoly.comic.entity.Comic;

@Repository
public interface AlbumRepository extends JpaRepository<Album, String> {

    List<Album.WithoutPhotos> findAllByComicOrderByIndexAsc(Comic comic);

}