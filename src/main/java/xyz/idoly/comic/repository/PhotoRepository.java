package xyz.idoly.comic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.idoly.comic.entity.Photo;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {}
