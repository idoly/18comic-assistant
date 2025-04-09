package xyz.idoly.comic.config;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

@Configuration
@ConfigurationProperties(prefix = "spring.proxy")
public class AppConfig {

    // RestClient 

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

    // Playwright

    // @Bean
    // public BrowserType.LaunchOptions launchOptions() {
    //     return new BrowserType.LaunchOptions()
    //         .setHeadless(false)
	// 		.setArgs(List.of("--disable-blink-features=AutomationControlled"));
    // }

    // @Bean
    // public Browser browser(BrowserType.LaunchOptions options) {
    //     return Playwright.create().chromium().launch(options);
    // }

    // @Bean
    // public Browser.NewContextOptions newContextOptions(ProxyProperties proxyProperties, ComicProperties comicProperties) {
    //     return new Browser.NewContextOptions()
    //         .setLocale("zh-CN")
    //         .setBypassCSP(true)
    //         .setJavaScriptEnabled(true)
    //         .setProxy(proxyProperties.getProxy())
    //         .setBaseURL(comicProperties.getHost())
    //         .setUserAgent(comicProperties.getUserAgent());
    // }

    // @Bean
    // public BrowserContext browserContext(Browser browser, Browser.NewContextOptions options) {

    //     // Cookie rememberId = new Cookie("remember_id", "a%3A3%3A%7Bs%3A8%3A%22username%22%3Bs%3A10%3A%22idolysfsdf%22%3Bs%3A8%3A%22password%22%3Bs%3A13%3A%22zxcvbnm159456%22%3Bs%3A5%3A%22check%22%3Bs%3A40%3A%228ad5d5996ce8aab7ebd7ecfd40346b1b91879bac%22%3B%7D"); 
    //     // rememberId.setDomain("18comic.vip");
    //     // rememberId.setUrl("/");

    //     // Cookie remember = new Cookie("remember", "a%3A3%3A%7Bs%3A8%3A%22username%22%3Bs%3A10%3A%22idolysfsdf%22%3Bs%3A8%3A%22password%22%3Bs%3A32%3A%226fbad2a8e2478fe2a8bc9506914d2371%22%3Bs%3A5%3A%22check%22%3Bs%3A40%3A%228ad5d5996ce8aab7ebd7ecfd40346b1b91879bac%22%3B%7D");
    //     // remember.setDomain("18comic.vip");
    //     // remember.setUrl("/");


    //     // Cookie cf = new Cookie("cf_clearance", "HuUMpc5D5WvNQKf_AFujocLUaYowiwYUhnJSDru1PNA-1744117852-1.2.1.1-BUgmjHg_o5r6.FGGWcTb.jb7k2bwpd0RYdQQKCoOYZsNpXaJ11bfEi10bmhzySK9CIT4Ty9FPrdvzeLzclvFpEODOPxuCT9O1hHJ9dcgl_osw.sICPzrQq63nge0bqSLT.OQ_4cN2q8XkBGDz0oRiI6NFeBf9RiNMW6vwpz08cmi4CBgYYmsmNTm1qaPNkLy9YLzyJi13AUiAabWOAAuyOM6XjeL39kLUGDDfSnFEz1fvMlhJoZroS8FoiEz3WQliPrTo1YhP4C9nq8FrvD4.QIeb1BAARJuVpc9rHG_6nIGF_W_xoeabpKmZDc.RZNnj6pdOusLp8xe45mCzo0cJIwnmG89gh.IZGyCyC1WRMk");
    //     // cf.setDomain(".18comic.vip");
    //     // cf.setUrl("/");


    //     BrowserContext context = browser.newContext(options);
    //     // context.addCookies(List.of(rememberId, remember));

    //     return browser.newContext(options);
    // }

    // @Bean
    // public BrowserClient browserClient(BrowserContext browserContext) {
    //     return new BrowserClient(browserContext);
    // }



    public class BrowserClient {

    private static final String selector = "#TAYH8 input[type='hidden']";

    private BrowserContext context;

    public BrowserClient(BrowserContext context) {
        this.context = context;
    }

    public BrowserContext getContext() {
        return context;
    }

    public void setContext(BrowserContext context) {
        this.context = context;
    }

    public String navigate(String uri) {
        try (Page page = getContext().newPage()) {

            page.navigate(uri);
            // page.waitForSelector("iframe[src*='turnstile']", new Page.WaitForSelectorOptions().setTimeout(10000));

            // // Step 2: 提取 sitekey（从 iframe 中）
            // String sitekey = page.evalOnSelector("iframe[src*='turnstile']",
            //     "el => new URL(el.src).searchParams.get('k')").toString();

            // System.out.println("Extracted sitekey: " + sitekey);

            while (!page.title().contains("Comics")) {


                waitAndCheck(page, 3000 * 1000);
            }

            page.waitForSelector(".container", new Page.WaitForSelectorOptions().setTimeout(300 * 1000));		

            return page.content();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    public void waitAndCheck(Page page, int timeoutMs) {
        FrameLocator iframe = page.frameLocator("iframe[id^='cf-chl-widget-']");
        Locator checkbox = iframe.locator("input[type='checkbox']");
        checkbox.waitFor(new Locator.WaitForOptions()
            .setTimeout(timeoutMs)
            .setState(WaitForSelectorState.VISIBLE)
        );
    
        if (!checkbox.isChecked()) {
            checkbox.check(new Locator.CheckOptions().setTimeout(timeoutMs));
        }
    }
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
