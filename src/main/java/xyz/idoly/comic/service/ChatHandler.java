package xyz.idoly.comic.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import xyz.idoly.comic.entity.Album;
import xyz.idoly.comic.entity.Comic;
import xyz.idoly.comic.entity.Result;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private String DEFAULT_PROMPT = """
        请输入您要执行的操作，并提供必要的参数。例如：
            下载漫画 [漫画ID]
            下载漫画 [漫画ID] 的 [起始章节] 到 [结束章节]
            下载漫画 [漫画ID] 的以下章节：[章节列表]
            下载以下章节：[章节列表]

            打包漫画 [漫画ID]
            打包漫画 [漫画ID] 的 [起始章节] 到 [结束章节]
            打包漫画 [漫画ID] 的以下章节：[章节列表]
            打包以下章节：[章节列表]

            搜索漫画 [漫画ID]
            推荐漫画

            使用账号 [用户名] 和密码 [密码] 登录
            查看登录状态
            下载收藏漫画
            注销登陆
            退出登录
        """;

    @Resource
    private ComicService comicService;

	@Resource
	private OpenAiChatModel chatModel;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        sendMessage(session, chatClient.prompt(message.getPayload() + ",中文回答, json").system(DEFAULT_PROMPT).tools(this).call().content());
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Tool(description = "下载指定漫画", returnDirect = true)
    public Result<Comic> downloadComic(String id) {
        return comicService.downloadComic(id);
    }

    @Tool(description = "下载指定漫画章节范围")
    public Result<Comic> downloadComicRange(String id, Integer start, Integer end) {
        return comicService.downloadComic(id, start, end);
    }

    @Tool(description = "下载指定漫画的多个章节")
    public Result<Comic> downloadComicAlbums(String id, SortedSet<Integer> selectIndexs) {
        return comicService.downloadComic(id, selectIndexs);
    }

    @Tool(description = "下载多个指定章节")
    public Result<List<Result<Album>>> downloadAlbums(SortedSet<String> ids) {
        return comicService.downloadAlbum(ids);
    }

    @Tool(description = "打包指定漫画")
    public Result<File> zipComic(String id) {
        return comicService.zipComic(id);
    }

    @Tool(description = "打包指定漫画章节范围")
    public Result<File>  zipComicRange(String id, Integer start, Integer end) {
        return comicService.zipComic(id, start, end);
    }

    @Tool(description = "打包指定漫画的多个章节")
    public Result<File> zipComicAlbums(String id, SortedSet<Integer> selectIndexs) {
        return comicService.zipComic(id, selectIndexs);
    }

    @Tool(description = "打包多个指定章节")
    public Result<File> zipAlbums(SortedSet<String> ids) {
        return comicService.zipAlbum(ids);
    }

    @Tool(description = "推荐漫画")
    public Result<Comic> recommendComics() {
        return comicService.recommendComics();
    }

    @Tool(description = "查询漫画")
    public Result<Comic> queryComic(String id) {
        return comicService.queryComic(id);
    }

    @Tool(description = "用户登陆")
    public Result<Void> login(String username, String password) {
        return comicService.login(username, password);
    }

    @Tool(description = "查看状态")
    public Result<String> status() {
        return comicService.status();
    }

    @Tool(description = "注销登陆")
    public Result<Void> logout() {
        return comicService.logout();
    }

    @Tool(description = "下载收藏")
    public Result<List<Result<Comic>>> downloadFavorites() {
        return comicService.downloadFavorites();
    }
}