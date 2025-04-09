package xyz.idoly.comic.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Album {

    @Id
    private Integer id;

    private String title;

    private Integer photos;

    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "comic_id")
    private Comic comic;
}