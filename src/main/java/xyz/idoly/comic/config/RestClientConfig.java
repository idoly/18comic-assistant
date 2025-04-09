package xyz.idoly.comic.config;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
@EnableConfigurationProperties({ProxyProperties.class})
public class RestClientConfig {

    @Bean
    public ProxySelector proxySelector(ProxyProperties proxyProperties) {
        ProxyProperties.Mode mode = proxyProperties.getMode();
        return switch (mode) {
            case AUTO -> {
                System.setProperty("java.net.useSystemProxies", "true");
                yield ProxySelector.getDefault();
            }
            case MANUAL -> ProxySelector.of(new InetSocketAddress(
                proxyProperties.getHost(), proxyProperties.getPort()
            ));
            case NONE -> ProxySelector.of(null);
        };
    }

    @Bean
    public CookieManager cookieManager() {
        return new CookieManager(null, CookiePolicy.ACCEPT_NONE);
    }

    @Bean
    public HttpClient httpClient(ProxySelector proxySelector, CookieManager cookieManager) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
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

}

