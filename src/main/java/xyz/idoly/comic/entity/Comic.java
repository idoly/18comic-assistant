package xyz.idoly.comic.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class Comic {

    @Id
    private String id;

    private String title;

    private String cover;

    @OneToMany(mappedBy = "comic", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("index ASC")
    private List<Album> albums = new ArrayList<>();

    public Comic() {}

    public Comic(String id) {
        this.id = id;
    }

    public Comic(String id, String title, String cover) {
        this.id = id;
        this.title = title;
        this.cover = cover;
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

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    @Override
    public String toString() {
        return "Comic [id=" + id + ", title=" + title + ", cover=" + cover + ", albums=" + albums + "]";
    }

    public record WithoutAlbum(String id, String title, String cover) {}
}