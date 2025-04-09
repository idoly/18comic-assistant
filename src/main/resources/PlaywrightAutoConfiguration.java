// package xyz.idoly.comic.config;

// import static java.net.Proxy.Type.valueOf;

// import java.nio.file.Paths;
// import java.util.List;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import com.microsoft.playwright.Browser;
// import com.microsoft.playwright.BrowserContext;
// import com.microsoft.playwright.BrowserType;
// import com.microsoft.playwright.Playwright;
// import com.microsoft.playwright.options.Cookie;

// import xyz.idoly.demo.utils.BrowserClient;

// @Configuration
// @ConditionalOnClass(Playwright.class)
// @EnableConfigurationProperties({ProxyProperties.class, ComicProperties.class})
// public class PlaywrightAutoConfiguration {

//     @Bean
//     public BrowserType.LaunchOptions launchOptions() {
//         return new BrowserType.LaunchOptions()
//             .setHeadless(false)
// 			.setArgs(List.of("--disable-blink-features=AutomationControlled"));
//     }

//     @Bean
//     public Browser browser(BrowserType.LaunchOptions options) {
//         return Playwright.create().chromium().launch(options);
//     }

//     @Bean
//     public Browser.NewContextOptions newContextOptions(ProxyProperties proxyProperties, ComicProperties comicProperties) {
//         return new Browser.NewContextOptions()
//             .setLocale("zh-CN")
//             .setBypassCSP(true)
//             .setJavaScriptEnabled(true)
//             .setProxy(proxyProperties.getProxy())
//             .setBaseURL(comicProperties.getHost())
//             .setUserAgent(comicProperties.getUserAgent());
//     }

//     @Bean
//     public BrowserContext browserContext(Browser browser, Browser.NewContextOptions options) {

//         // Cookie rememberId = new Cookie("remember_id", "a%3A3%3A%7Bs%3A8%3A%22username%22%3Bs%3A10%3A%22idolysfsdf%22%3Bs%3A8%3A%22password%22%3Bs%3A13%3A%22zxcvbnm159456%22%3Bs%3A5%3A%22check%22%3Bs%3A40%3A%228ad5d5996ce8aab7ebd7ecfd40346b1b91879bac%22%3B%7D"); 
//         // rememberId.setDomain("18comic.vip");
//         // rememberId.setUrl("/");

//         // Cookie remember = new Cookie("remember", "a%3A3%3A%7Bs%3A8%3A%22username%22%3Bs%3A10%3A%22idolysfsdf%22%3Bs%3A8%3A%22password%22%3Bs%3A32%3A%226fbad2a8e2478fe2a8bc9506914d2371%22%3Bs%3A5%3A%22check%22%3Bs%3A40%3A%228ad5d5996ce8aab7ebd7ecfd40346b1b91879bac%22%3B%7D");
//         // remember.setDomain("18comic.vip");
//         // remember.setUrl("/");


//         // Cookie cf = new Cookie("cf_clearance", "HuUMpc5D5WvNQKf_AFujocLUaYowiwYUhnJSDru1PNA-1744117852-1.2.1.1-BUgmjHg_o5r6.FGGWcTb.jb7k2bwpd0RYdQQKCoOYZsNpXaJ11bfEi10bmhzySK9CIT4Ty9FPrdvzeLzclvFpEODOPxuCT9O1hHJ9dcgl_osw.sICPzrQq63nge0bqSLT.OQ_4cN2q8XkBGDz0oRiI6NFeBf9RiNMW6vwpz08cmi4CBgYYmsmNTm1qaPNkLy9YLzyJi13AUiAabWOAAuyOM6XjeL39kLUGDDfSnFEz1fvMlhJoZroS8FoiEz3WQliPrTo1YhP4C9nq8FrvD4.QIeb1BAARJuVpc9rHG_6nIGF_W_xoeabpKmZDc.RZNnj6pdOusLp8xe45mCzo0cJIwnmG89gh.IZGyCyC1WRMk");
//         // cf.setDomain(".18comic.vip");
//         // cf.setUrl("/");


//         BrowserContext context = browser.newContext(options);
//         // context.addCookies(List.of(rememberId, remember));

//         return browser.newContext(options);
//     }

//     @Bean
//     public BrowserClient browserClient(BrowserContext browserContext) {
//         return new BrowserClient(browserContext);
//     }

// }
