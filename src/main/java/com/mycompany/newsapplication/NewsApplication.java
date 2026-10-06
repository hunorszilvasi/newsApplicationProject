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
        RssReader reader = new RssReader();
        reader.testFeed();
        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();

        for (Article article1 : articles) {
            System.out.println(article1);
        }
    }
}
