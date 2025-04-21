package xyz.idoly.comic.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import xyz.idoly.comic.entity.Comic;
import xyz.idoly.comic.service.ComicService;

@RestController
public class IndexController {

    @Resource
    private ComicService comicService;

	@Resource
	private OpenAiChatModel chatModel;

    // @GetMapping("/query")
    // public String queryComicById(@RequestParam String id) {
    //     return ChatClient.create(chatModel)
    //         .prompt("帮我查个漫画, 它的id是 " + id)
    //         .tools(comicService)
    //         .call()
    //         .content();
    // }

    // @GetMapping("/query")
    // public Comic queryComicById(@RequestParam String id) {
    //     return comicService.search(id);
    // }

}