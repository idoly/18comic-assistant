package xyz.idoly.comic.tool;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;

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
