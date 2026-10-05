/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.newsapplication;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author hunorszilvasi
 */
public class NewsApplication {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        DatabaseManager.initializeDatabase();
        Article article = new Article();

        article.setTitle("Test News");
        article.setAuthor("Hunor");
        article.setSource("BBC");
        article.setUrl("https://example.com/news2");
        article.setPublishDate("2026-10-05");
        article.setCreatedAt("2026-10-05");
        article.setLastModifiedAt("2026-10-05");

        ArticleDao dao = new ArticleDao();
        dao.save(article);
        List<Article> articles = dao.findAll();

        for (Article article1 : articles) {
            System.out.println(article);
        }
    }
}
