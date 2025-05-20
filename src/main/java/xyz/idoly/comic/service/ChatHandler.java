package xyz.idoly.comic.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import xyz.idoly.comic.entity.Comic;
import xyz.idoly.comic.entity.Result;

@Component
public class ChatHandler extends TextWebSocketHandler {

    @Resource
    private ComicService comicService;

	@Resource
	private DeepSeekChatModel chatModel;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        sendMessage(session, chatClient.prompt(message.getPayload() + ",中文回答, json").system("""
            你是漫画助手, 你可以提供这些功能：
                1. 下载/同步/删除/查询/推荐漫画
                2. 用户登陆, 查看状态, 注销登陆, 下载收藏, 查看收藏，查看书架
        """).tools(this).call().content());
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Tool(description = "查看书架", returnDirect = true)
    public Result<String> index() {
        return Result.success("/index.html").type("url");
    }

    @Tool(description = "推荐漫画", returnDirect = true)
    public Result<Comic> recommendComic() {
        return comicService.recommendComic().type("comic");
    }

    @Tool(description = "查询漫画", returnDirect = true)
    public Result<Comic> queryComic(String id) {
        return comicService.queryComic(id).type("comic");
    }

    @Tool(description = "下载漫画", returnDirect = true)
    public Result<String> zipComic(String id) {
        Result<File> zipResult = comicService.zipComic(id);
        if (zipResult.getCode() != Result.SUCCESS_CODE) {
            return Result.error(zipResult.getMsg());
        }

        return Result.success(zipResult.getData().getName()).type("file");
    }

    @Tool(description = "下载漫画范围", returnDirect = true)
    public Result<String>  zipComicRange(String id, Integer start, Integer end) {
        Result<File> zipResult = comicService.zipComic(id, start, end);
        if (zipResult.getCode() != Result.SUCCESS_CODE) {
            return Result.error(zipResult.getMsg());
        }

        return Result.success(zipResult.getData().getName()).type("file");
    }

    @Tool(description = "下载漫画章节", returnDirect = true)
    public Result<String> zipComicAlbums(String id, Set<Integer> selectIndexs) {
        Result<File> zipResult = comicService.zipComic(id, selectIndexs);
        if (zipResult.getCode() != Result.SUCCESS_CODE) {
            return Result.error(zipResult.getMsg());
        }

        return Result.success(zipResult.getData().getName()).type("file");
    }

    @Tool(description = "同步漫画", returnDirect = false)
    public Result<Void> downloadComic(String id) {
        Result<Comic> comicResult = comicService.downloadComic(id);
        return new Result<>(comicResult.getCode(), comicResult.getMsg());
    }

    @Tool(description = "删除漫画", returnDirect = false)
    public Result<Void> deleteComic(String id) {
        return comicService.deleteComic(id);
    }

    @Tool(description = "用户登陆", returnDirect = false)
    public Result<Void> login(String username, String password) {
        return comicService.login(username, password);
    }

    @Tool(description = "登陆状态", returnDirect = false)
    public Result<Boolean> status() {
        return comicService.status();
    }

    @Tool(description = "注销登陆", returnDirect = false)
    public Result<Void> logout() {
        return comicService.logout();
    }

    @Tool(description = "查看收藏", returnDirect = false)
    public Result<List<String>> queryFavorites() {
        return comicService.queryFavorites();
    }

    @Tool(description = "下载收藏", returnDirect = false)
    public Result<List<Result<Comic>>> downloadFavorites() {
        return comicService.downloadFavorites();
    }
}