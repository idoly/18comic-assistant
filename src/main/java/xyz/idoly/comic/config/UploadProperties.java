package xyz.idoly.comic.config;

import java.io.IOException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.upload")
public class UploadProperties {

    private Strategy strategy;  // 上传策略：GitHub, Local

    private LocalStrategy local;    // 本地配置
    private GitHubStrategy github;  // GitHub配置

    public static class GitHubStrategy implements UploadStrategy {
        private String token;
        private String repository;
        private String branch;
    }

    public static class LocalStrategy implements UploadStrategy {
        
        private static final String root = System.getProperty("user.dir") + "/comic";


        // getters and setters
    }

    public enum Strategy {
        LOCAL, GITHUB
    }

    // getters and setters for all fields
    public interface UploadStrategy {


    }
}
