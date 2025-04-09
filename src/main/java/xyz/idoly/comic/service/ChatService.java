package xyz.idoly.comic.service;

import static java.net.Proxy.Type.HTTP;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jsoup.nodes.Element;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ChatService {

	@Resource
	private OpenAiChatModel chatModel;

	public OpenAiChatModel getChatModel() {
		return chatModel;
	}

	// private final ChatClient chatClient;

	// public ChatService(ChatClient.Builder modelBuilder) {

	// 	// @formatter:off
	// 	this.chatClient = modelBuilder
    //             // .defaultTools("getCurrentDateTime")

	// 			.build();
	// 	// @formatter:on
	// }

	// public Flux<String> chat(String chatId, String userMessageContent) {

	// 	return ChatClient.create(chatModel)
    //     .prompt("What day is tomorrow?")
    //     .tools(new DateTimeTools())
    //     .call()
    //     .content();
	// }


	// // Tool
	// @Tool(description = "Get the current date and time in the user's timezone")
    // String getCurrentDateTime() {
    //     return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    // }


	// 每周推荐
	
	// 查询分类

	// 查询分类下 动漫，按什么排序，每页显示多少。

	// 根据 id 获取 漫画名称， 简介信息

	// 根据 id 获取 章节列表，可点击阅读，按什么排序，每页显示多少。

	// 根据漫画名称，模糊搜索。

	// 下载打包某一章节或正本漫画， 也可提取其中一张图片。
}
