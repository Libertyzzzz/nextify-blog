package com.nextify.blog.controller;

import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.service.BlogArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
public class SitemapController {

    @Autowired
    private BlogArticleService articleService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    public String getSitemap() {
        List<BlogArticle> articles = articleService.list(); // 获取所有已发布文章
        StringBuilder xml = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首页
        xml.append("  <url>\n");
        xml.append("    <loc>https://yourblog.com/</loc>\n");
        xml.append("    <priority>1.0</priority>\n");
        xml.append("  </url>\n");

        // 文章详情页
        for (BlogArticle article : articles) {
            xml.append("  <url>\n");
            xml.append("    <loc>https://yourblog.com/article/").append(article.getId()).append("</loc>\n");
            xml.append("    <lastmod>").append(sdf.format(article.getUpdateTime())).append("</lastmod>\n");
            xml.append("    <priority>0.8</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }
}