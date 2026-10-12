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
                (title, description, author, publisher, source, url,
                 publish_date, created_at, last_modified_at,
                 language, cover_image)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getDescription());
            pstmt.setString(3, article.getAuthor());
            pstmt.setString(4, article.getPublisher());
            pstmt.setString(5, article.getSource());
            pstmt.setString(6, article.getUrl());
            pstmt.setTimestamp(7, article.getPublishDate());
            pstmt.setTimestamp(8, article.getCreatedAt());
            pstmt.setTimestamp(9, article.getLastModifiedAt());
            pstmt.setString(10, article.getLanguage());
            pstmt.setString(11, article.getCoverImage());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save article: " + e.getMessage());
            return -1;
        }
    }
    // Article listazasa
    public List<Article> findAll() {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM articles";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Article article = new Article();

                article.setId(rs.getInt("id"));
                article.setTitle(rs.getString("title"));
                article.setDescription(rs.getString("description"));
                article.setAuthor(rs.getString("author"));
                article.setPublisher(rs.getString("publisher"));
                article.setSource(rs.getString("source"));
                article.setUrl(rs.getString("url"));
                article.setPublishDate(rs.getTimestamp("publish_date"));
                article.setCreatedAt(rs.getTimestamp("created_at"));
                article.setLastModifiedAt(rs.getTimestamp("last_modified_at"));
                article.setLanguage(rs.getString("language"));
                article.setCoverImage(rs.getString("cover_image"));

                articles.add(article);
            }

        } catch (SQLException e) {
            System.err.println("Failed to load articles: " + e.getMessage());
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
            System.out.println("Article deleted successfully, id: " + id);
            
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    // Article modositas
    public void updateArticle(Article article) {

        String sql = """
                UPDATE articles
                SET title = ?,
                    description = ?,
                    author = ?,
                    publisher = ?,
                    source = ?,
                    url = ?,
                    publish_date = ?,
                    created_at = ?,
                    last_modified_at = ?,
                    language = ?,
                    cover_image = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getDescription());
            pstmt.setString(3, article.getAuthor());
            pstmt.setString(4, article.getPublisher());
            pstmt.setString(5, article.getSource());
            pstmt.setString(6, article.getUrl());
            pstmt.setTimestamp(7, article.getPublishDate());
            pstmt.setTimestamp(8, article.getCreatedAt());
            pstmt.setTimestamp(9, article.getLastModifiedAt());
            pstmt.setString(10, article.getLanguage());
            pstmt.setString(11, article.getCoverImage());
            pstmt.setInt(12, article.getId());

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Article updated successfully.");
            } else {
                System.out.println("No article found with the specified ID.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to update article: " + e.getMessage());
        }
    }
    // kereso bar
    public List<Article> searchByTitle(String keyword) {

        List<Article> articles = new ArrayList<>();

        String sql = """
                SELECT * FROM articles
                WHERE title LIKE ? OR description LIKE ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Article article = new Article();

                    article.setId(rs.getInt("id"));
                    article.setTitle(rs.getString("title"));
                    article.setDescription(rs.getString("description"));
                    article.setAuthor(rs.getString("author"));
                    article.setPublisher(rs.getString("publisher"));
                    article.setSource(rs.getString("source"));
                    article.setUrl(rs.getString("url"));
                    article.setPublishDate(rs.getTimestamp("publish_date"));
                    article.setCreatedAt(rs.getTimestamp("created_at"));
                    article.setLastModifiedAt(rs.getTimestamp("last_modified_at"));
                    article.setLanguage(rs.getString("language"));
                    article.setCoverImage(rs.getString("cover_image"));

                    articles.add(article);
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to search articles: " + e.getMessage());
        }

        return articles;
    }
}
