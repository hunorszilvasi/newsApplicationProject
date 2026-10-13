package ro.umfst.newsapplication.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:news.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // creating the database
    public static void initializeDatabase() {

        // SQLite prompt
        String sql = """
                CREATE TABLE IF NOT EXISTS articles (
                    title TEXT NOT NULL,
                    description TEXT,
                    author TEXT,
                    publisher TEXT,
                    source TEXT NOT NULL,
                    url TEXT NOT NULL,
                    publish_date TIMESTAMP,
                    created_at TIMESTAMP,
                    last_modified_at TIMESTAMP,
                    language TEXT,
                    cover_image TEXT,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (source, url)
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Database initialized successfully.");

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
}