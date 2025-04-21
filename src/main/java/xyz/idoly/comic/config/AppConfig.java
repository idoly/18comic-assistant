package xyz.idoly.comic.config;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "spring.proxy")
public class AppConfig {

    private static final String JMCOMIC = "https://jmcmomic.github.io";

    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Bean
    public ProxySelector proxySelector() {
        return switch (getMode()) {
            case AUTO -> {
                System.setProperty("java.net.useSystemProxies", "true");
                yield ProxySelector.getDefault();
            }
            case MANUAL -> ProxySelector.of(new InetSocketAddress(getHost(), getPort()));
            case NONE -> ProxySelector.of(null);
        };
    }

    @Bean
    public CookieManager cookieManager() {
        return new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    }

    @Bean
    public HttpClient httpClient(ProxySelector proxySelector, CookieManager cookieManager) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(Redirect.NEVER)
            .cookieHandler(cookieManager)
            .proxy(proxySelector)
            .build();
    }

    @Bean
    public ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
        return new JdkClientHttpRequestFactory(httpClient);
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientHttpRequestFactory requestFactory) {
        return builder.requestFactory(requestFactory).build();
    }

    private ClientHttpRequestInterceptor logAndInjectHeaders() {
        return (request, body, execution) -> {
            
            HttpHeaders headers = request.getHeaders();
    
            // 注入常用请求头
            // headers.set("Referer", request.getURI().toString());
            // headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0");
            // headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            // headers.set("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
    
            // 打印请求日志
            System.out.println("=== [RestClient Request] ===");
            System.out.println("URI     : " + request.getURI());
            // System.out.println("Method  : " + request.getMethod());
            // headers.forEach((key, values) -> values.forEach(value -> System.out.println("  " + key + ": " + value)));
            // System.out.println("============================");
    
            ClientHttpResponse response = execution.execute(request, body);
    
            // 打印响应头
            // System.out.println("=== [RestClient Response] ===");
            // System.out.println("Status code: " + response.getStatusCode() + " " + response.getStatusText());
            // response.getHeaders().forEach((key, values) -> values.forEach(value -> System.out.println("  " + key + ": " + value)));
            // System.out.println("==============================");
    
            return response;
        };
    }

    public enum Mode {AUTO, MANUAL, NONE}

    private Mode mode = Mode.NONE;

    private String host;
    
    private int port;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
