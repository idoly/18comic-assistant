package xyz.idoly.comic.entity;

import java.util.List;

public interface Task {

     Integer getId();

    // comic, album
    // 更新 下载 删除 查询, 追更
    // private String type;

    // private 

    // private int start;

    // private int end;

    Boolean getStatus();


    public static class AbstractTask implements Task {

        @Override
        public Integer getId() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getId'");
        }

        @Override
        public Boolean getStatus() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
        }

    }


    public static class AlbumTask extends AbstractTask {

        private Album album;

        private List<String> rerocds;
    }


    public static class ComicTask extends AbstractTask {

        private Comic comic;

        private int start;

        private int end;

        private List<AlbumTask> albumTasks;
    }
    
}
