package com.nextify.blog.controller;

import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.service.BlogArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SitemapController {

    @Autowired
    private BlogArticleService articleService;

    @Value("${nextify.site-url:https://nextify.cn}")
    private String siteUrl;

    private static final  DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PublicApi
    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=utf-8")
    public String getSitemap() {
        String baseUrl = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;
        // Only fetch published articles (status = 1)
        List<BlogArticle> articles = articleService.lambdaQuery().eq(BlogArticle::getStatus, 1).list();
        StringBuilder xml = new StringBuilder();


        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首页与博客列表页
        xml.append("  <url>\n");
        xml.append("    <loc>").append(baseUrl).append("/</loc>\n");
        xml.append("    <priority>1.0</priority>\n");
        xml.append("    <changefreq>daily</changefreq>\n");
        xml.append("  </url>\n");
        xml.append("  <url>\n");
        xml.append("    <loc>").append(baseUrl).append("/blog</loc>\n");
        xml.append("    <priority>0.9</priority>\n");
        xml.append("    <changefreq>daily</changefreq>\n");
        xml.append("  </url>\n");

        // 文章详情页（对齐前端路由 /post/:articleSlug）
        for (BlogArticle article : articles) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(baseUrl).append("/post/").append(article.getId()).append("</loc>\n");
            if (article.getUpdateTime() != null) {
                xml.append("    <lastmod>").append(article.getUpdateTime().format(DATE_TIME_FORMATTER)).append("</lastmod>\n");
            } else if (article.getCreateTime() != null) {
                xml.append("    <lastmod>").append(article.getCreateTime().format(DATE_TIME_FORMATTER)).append("</lastmod>\n");
            }
            xml.append("    <priority>0.8</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }
}