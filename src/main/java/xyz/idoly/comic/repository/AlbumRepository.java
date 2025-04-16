package xyz.idoly.comic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, String> {}