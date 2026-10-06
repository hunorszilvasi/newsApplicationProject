package ro.umfst.newsapplication.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import ro.umfst.newsapplication.model.Article;
public class ArticleDao {

    
    // egy article lementese
    public int save(Article article) {

        String sql = """
                INSERT OR IGNORE INTO articles
                (title, author, source, url, publish_date, created_at, last_modified_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getAuthor());
            pstmt.setString(3, article.getSource());
            pstmt.setString(4, article.getUrl());
            pstmt.setTimestamp(5, article.getPublishDate());
            pstmt.setTimestamp(6, article.getCreatedAt());
            pstmt.setTimestamp(7, article.getLastModifiedAt());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    // Article listazasa
    public List<Article> findAll(){
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM articles";
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()){
            while(rs.next()){
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setTitle(rs.getString("title"));
                article.setAuthor(rs.getString("author"));
                article.setSource(rs.getString("source"));
                article.setUrl(rs.getString("url"));
                article.setPublishDate(rs.getTimestamp("publish_date"));
                article.setCreatedAt(rs.getTimestamp("created_at"));
                article.setLastModifiedAt(rs.getTimestamp("last_modified_at"));
                articles.add(article);
            }
            
        } catch(SQLException e){
            e.printStackTrace();
        }
        return articles;
        
    }
    // Article torles ID szerint
    public void deleteArticle(int id){
        String sql = "DELETE FROM articles WHERE id = ?";
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){    
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Article deleted successfully.");
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    // Article modositas
    public void updateArticle(Article article){
        String sql = """
            UPDATE articles
            SET title = ?,
                author = ?,
                source = ?,
                url = ?,
                publish_date = ?,
                created_at = ?,
                last_modified_at = ?
            WHERE id = ?
            """;
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)){
            
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getAuthor());
            pstmt.setString(3, article.getSource());
            pstmt.setString(4, article.getUrl());
            pstmt.setTimestamp(5, article.getPublishDate());
            pstmt.setTimestamp(6, article.getCreatedAt());
            pstmt.setTimestamp(7, article.getLastModifiedAt());
            pstmt.setInt(8, article.getId());

            pstmt.executeUpdate();

            System.out.println("Article updated successfully.");
            
        }catch(SQLException e){
            e.printStackTrace();
        }
            
    }
}
