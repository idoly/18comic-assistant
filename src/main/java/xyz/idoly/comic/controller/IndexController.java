package xyz.idoly.comic.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String demo() {
        return "ai";
    }

    @GetMapping("/index.html")
    public ResponseEntity<Resource> index() throws IOException {
        File file = new File("index.html");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }
}