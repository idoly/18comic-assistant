package xyz.idoly.comic.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comic {

    @Id
    private String id;

    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> covers = new ArrayList<>();

    @OneToMany(mappedBy = "comic", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("index ASC")
    private List<Album> albums = new ArrayList<>();

    public Comic() {}

    public Comic(String id, String title, List<String> covers) {
        this.id = id;
        this.title = title;
        this.covers = covers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getCovers() {
        return covers;
    }

    public void setCovers(List<String> covers) {
        this.covers = covers;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    @Override
    public String toString() {
        return "Comic [id=" + id + ", title=" + title + ", cover=" + covers + ", albums=" + albums + "]";
    }

    public record WithoutAlbums(String id, String title, List<String> covers) {}


}