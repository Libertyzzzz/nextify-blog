package com.nextify.blog.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import com.nextify.blog.service.PosterGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PosterGeneratorServiceImpl implements PosterGeneratorService {

    @Override
    public byte[] generatePostImage(String shareId) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setDeviceScaleFactor(2.0) // 2倍采样率，确保 Retina 屏幕清晰度
                    .setViewportSize(750, 1334)); // 标准手机竖屏比例

            Page page = context.newPage();
            // 访问前端专门准备的海报模板页
            page.navigate("https://nextify.cn/assessment/poster/" + shareId);

            // 等待 ECharts 动画执行完成
            page.waitForTimeout(1000);

            // 截图
            return page.screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.JPEG)
                    .setQuality(90));
        } catch (Exception e) {
            log.error("生成海报失败", e);
            return null;
        }
    }
}
