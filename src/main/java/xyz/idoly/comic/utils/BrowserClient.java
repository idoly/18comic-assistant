package xyz.idoly.comic.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

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