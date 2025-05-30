package xyz.idoly.comic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Comic;

@Repository
public interface ComicRepository extends JpaRepository<Comic, String> {

    List<Comic.WithoutAlbums> findAllByOrderByIdAsc();

    @Query("SELECT c.id FROM Comic c ORDER BY c.id ASC")
    List<String> findAllIdsOrderByIdAsc();


}