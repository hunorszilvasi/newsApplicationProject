/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ro.umfst.newsapplication.model;
import java.sql.Timestamp;

/**
 *
 * @author hunorszilvasi
 */
public class Article {
    private int id;
    private String title;
    private String author;
    private String source;
    private String url;
    private Timestamp publishDate;
    private Timestamp createdAt;
    private Timestamp lastModifiedAt;
    
    public Article(){
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }

    public Timestamp getPublishDate() {
        return publishDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPublishDate(Timestamp publishDate) {
        this.publishDate = publishDate;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastModifiedAt(Timestamp lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    @Override
    public String toString() {
        return "Article{" + "id=" + id + ", title=" + title + ", author=" + author + ", source=" + source + ", url=" + url + ", publishDate=" + publishDate + ", createdAt=" + createdAt + ", lastModifiedAt=" + lastModifiedAt + '}';
    }
    
    
}
