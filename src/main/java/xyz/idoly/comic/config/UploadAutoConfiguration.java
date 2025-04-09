package xyz.idoly.comic.config;
// package xyz.idoly.demo.config;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// @EnableConfigurationProperties(UploadProperties.class)
// public class UploadAutoConfiguration {

//     private static final String DEFAULT_UPLOAD_STRATEGY = "uploadStrategy";

//     @Bean(name = DEFAULT_UPLOAD_STRATEGY)
//     @ConditionalOnProperty(name = "upload.strategy", havingValue = "GITHUB")
//     public UploadStrategy githubUploadStrategy(UploadProperties uploadProperties) {
//         System.out.println("[UploadStrategy] Using GitHub upload strategy");
//         return new GitHubUploadStrategy(uploadProperties);
//     }
    
//     @Bean(name = DEFAULT_UPLOAD_STRATEGY)
//     @ConditionalOnProperty(name = "upload.strategy", havingValue = "DATABASE")
//     public UploadStrategy databaseUploadStrategy(UploadProperties uploadProperties) {
//         System.out.println("[UploadStrategy] Using Database upload strategy");
//         return new DatabaseUploadStrategy(uploadProperties);
//     }
    
//     @Bean(name = DEFAULT_UPLOAD_STRATEGY)
//     @ConditionalOnMissingBean // 默认使用 LOCAL 策略
//     public UploadStrategy localFileUploadStrategy(UploadProperties uploadProperties) {
//         System.out.println("[UploadStrategy] Using Local File upload strategy");
//         return new LocalFileUploadStrategy(uploadProperties);
//     }
    

// }
