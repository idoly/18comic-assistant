package xyz.idoly.comic.service;

import static java.net.Proxy.Type.HTTP;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jsoup.nodes.Element;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
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


	public class DateTimeTools {

		@Tool(description = "Get the current date and time in the user's timezone")
		String getCurrentDateTime() {
			return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
		}
	
		@Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
		void setAlarm(String time) {
			LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
			System.out.println("Alarm set for " + alarmTime);
		}
	
		@Tool(description = "得到骂人的几个单词")
		List<String> getwords() {
			return List.of("傻逼","色狼","弱智");
		}
	
	
		@Tool(description = "返回漫画列表，包含名称和链接")
		Map<String, String> getComics() {
			return Map.of("社团学姊", "https://18comic.vip/album/1123318", "洞洞雜貨店", "https://18comic.vip/album/603766");
		}
	
		@Tool(description = "得到一张图片")
		byte[] getImage() {
			try {
				return new ClassPathResource("94.png").getContentAsByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			return null;
		}
	
	}
}
