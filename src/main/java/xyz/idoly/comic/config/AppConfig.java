package xyz.idoly.comic.config;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
@ConfigurationProperties(prefix = "spring.comic")
public class AppConfig {

    @Bean
    public ProxySelector proxySelector() {
        return proxy != null ? ProxySelector.of(new InetSocketAddress(proxy.getHost(), proxy.getPort())) : ProxySelector.of(null);
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
        return builder.requestFactory(requestFactory).requestInterceptor(logAndInjectHeaders()).build();
    }

    private ClientHttpRequestInterceptor logAndInjectHeaders() {
        return (request, body, execution) -> {  
            HttpHeaders headers = request.getHeaders();
            headers.set("Referer", request.getURI().toString());
            headers.set("User-Agent", userAgent);
    
            // System.out.println("=== [RestClient Request] ===");
            // System.out.println("URI     : " + request.getURI());
            // System.out.println("Method  : " + request.getMethod());
            // headers.forEach((key, values) -> values.forEach(value -> System.out.println("  " + key + ": " + value)));
            // System.out.println("============================");
    
            ClientHttpResponse response = execution.execute(request, body);
    
            // System.out.println("=== [RestClient Response] ===");
            // System.out.println("Status code: " + response.getStatusCode() + " " + response.getStatusText());
            // response.getHeaders().forEach((key, values) -> values.forEach(value -> System.out.println("  " + key + ": " + value)));
            // System.out.println("==============================");
    
            return response;
        };
    }

    private String path = System.getProperty("user.dir");

    private List<String> urls = new ArrayList<>();

    private String username;

    private String password;

    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0";

    private Proxy proxy;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public static class  Proxy {

        private String host;
        private int port;
        
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
}
