package xyz.idoly.comic.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Album {

    @ManyToOne
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Id
    private String id;

    private Integer index;

    private Integer total = 0;

    @OneToMany(mappedBy = "album", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("index ASC")
    private List<Photo> photos = new ArrayList<>();

    public Album() {}

    public Album(String id, Integer index) {
        this.id = id;
        this.index = index;
    }

    public Album(Comic comic, String id, Integer index) {
        this.comic = comic;
        this.id = id;
        this.index = index;
    }

    public Album(Comic comic, String id, Integer index, Integer total) {
        this.comic = comic;
        this.id = id;
        this.index = index;
        this.total = total;
    }

    public Album comic(Comic comic) {
        setComic(comic);
        return this;
    }

    public Comic getComic() {
        return comic;
    }

    public void setComic(Comic comic) {
        this.comic = comic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "Album [id=" + id + ", index=" + index + ", photos=" + photos + "]";
    }

    public static class WithoutPhotos {

        private String id;
        private Integer index;
        private Integer total;

        public WithoutPhotos(String id, Integer index, Integer total) {
            this.id = id;
            this.index = index;
            this.total = total;
        }

        public String getId() {
            return id;
        }

        public Integer getIndex() {
            return index;
        }

        public Integer getTotal() {
            return total;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }
    }
}