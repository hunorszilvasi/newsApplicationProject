package ro.umfst.newsapplication.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:news.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    // creating the database
    public static void initializeDatabase() {
        
        String sql = """
                CREATE TABLE IF NOT EXISTS articles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    author TEXT,
                    publisher TEXT,
                    source TEXT,
                    url TEXT UNIQUE,
                    publish_date TIMESTAMP,
                    created_at TIMESTAMP,
                    last_modified_at TIMESTAMP,
                    language TEXT,
                    cover_image TEXT
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            addReadColumnIfMissing(conn);
            System.out.println("Database initialized successfully.");
        // sql exception
        } catch (SQLException e) {
            System.err.println(
                    "Database initialization failed: " + e.getMessage()
            );
        }
    }
    // delete database
    public static boolean clearDatabase() {
        String sql = "DELETE FROM articles";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("Database cleared successfully.");
            return true;

        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
            return false;
        }
    }
    
    // isRead function
    private static void addReadColumnIfMissing(Connection conn)
            throws SQLException {

        boolean columnExists = false;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(articles)")) {

            while (rs.next()) {
                if ("is_read".equals(rs.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }

        if (!columnExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    ALTER TABLE articles
                    ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0
                """);

                System.out.println("Added is_read column.");
            }
        }
    }
}