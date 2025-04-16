package xyz.idoly.comic.config;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "spring.proxy")
public class AppConfig {

    @Bean
    public ThreadPoolTaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.afterPropertiesSet();
        return executor;
    }

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
