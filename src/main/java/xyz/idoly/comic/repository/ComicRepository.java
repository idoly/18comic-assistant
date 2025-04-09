package xyz.idoly.comic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Comic;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Integer> {

}