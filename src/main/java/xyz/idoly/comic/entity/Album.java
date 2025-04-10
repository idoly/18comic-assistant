package xyz.idoly.comic.entity;

import jakarta.persistence.Column;
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
    private int id;

    // 第一张图片, 标记初始编号: 0 or 1
    private int start;

    // 已下载图片数量， end == total 时，设置 status = true
    @Column(name = "`end`")
    private int end;

    //  初始状态                   首次下载             第二次下载             第三次下载
    // start = 0，end = 0 -> start = 0，end = 1 -> start = 0，end = 2 -> start = 0，end = 2
    // start = 0，end = 0 -> start = 1，end = 1 -> start = 1，end = 2 -> start = 1，end = 3
    private int total;

    private boolean status;

    @ManyToOne
    @JoinColumn(name = "comic_id")
    private Comic comic;
}