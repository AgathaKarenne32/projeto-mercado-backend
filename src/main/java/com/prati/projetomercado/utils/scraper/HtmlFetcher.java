package com.prati.projetomercado.utils.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import io.github.kihdev.playwright.stealth4j.Stealth4j;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class HtmlFetcher {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext stealthContext;

    public HtmlFetcher() {
        this.playwright = Playwright.create();
        this.browser = playwright.firefox().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
        this.stealthContext = Stealth4j.newStealthContext(browser);
    }

    public String getHtml(String url) {
        Page page = stealthContext.newPage();
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        String html = page.content();
        page.close();
        return html;
    }

    @PreDestroy
    public void close() {
        try {
            stealthContext.close();
            browser.close();
            playwright.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
