package xyz.idoly.comic.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import xyz.idoly.comic.service.ChatService;
// import xyz.idoly.comic.tool.DateTimeTools;

@RestController
public class IndexController {

    @Resource
    private ChatService chatService;

    // @GetMapping("/ai/date")
    // public String generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    //     return ChatClient.create(chatService.getChatModel())
    //         .prompt("展示一张图片")
    //         .tools(new DateTimeTools())
    //         .call()
    //         .content();
    // }

}