package ro.umfst.newsapplication.ui;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.service.RssReader;

import ro.umfst.newsapplication.dao.ArticleDao;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.JTextField;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class NewsFrame extends JFrame {
    DefaultTableModel model;
    public NewsFrame() {
        setLayout(new BorderLayout());
        setTitle("News Reader");
        setSize(1500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();

        String[] columns = {"ID", "Source", "Author", "Title", "URL", "Publish Date"};
        model = new DefaultTableModel(
                new String[]{"ID", "Source", "Author", "Title", "URL", "Publish Date"}, 0);
        
        for(Article article : articles){
            model.addRow(new Object[]{
                article.getId(),
                article.getSource(),
                article.getAuthor(),
                article.getTitle(),
                article.getUrl(),
                article.getPublishDate()
            });
        }
        JTable table = new JTable(model);
        // Gomb
        JButton button = new JButton("Refresh");
        button.addActionListener(e -> {
            RssReader reader = new RssReader();
            reader.testFeed(); // uj adatok lekerdezese 
            loadArticles();
            System.out.println("Feed refreshed");
        });
        // Search function
        JPanel topPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(button);
        add(topPanel, BorderLayout.NORTH);
        
        // Search button action listener
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText();
            List<Article> searchResults = dao.searchByTitle(keyword);
            model.setRowCount(0);
            for(Article article : searchResults){
            model.addRow(new Object[]{
                article.getId(),
                article.getSource(),
                article.getAuthor(),
                article.getTitle(),
                article.getUrl(),
                article.getPublishDate()
            });
        }
            
        });
        
        
        add(new JScrollPane(table));
        setVisible(true);
    }
    
    private void loadArticles(){
        model.setRowCount(0);
        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();
        for(Article article : articles){
            model.addRow(new Object[]{
                article.getId(),
                article.getSource(),
                article.getAuthor(),
                article.getTitle(),
                article.getUrl(),
                article.getPublishDate()
            });
        }
    }
}