package xyz.idoly.comic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resource location does not end with slash
        // registry.addResourceHandler("/index.html").addResourceLocations(new FileSystemResource("index.html"));
        registry.addResourceHandler("/comic/**").addResourceLocations("file:./comic/");
    }
}