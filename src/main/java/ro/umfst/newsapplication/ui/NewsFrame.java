package ro.umfst.newsapplication.ui;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.service.RssReader;

import ro.umfst.newsapplication.dao.ArticleDao;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.sql.Timestamp;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.time.LocalDateTime;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;
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

        model = new DefaultTableModel(
                    new String[]{"ID", "Source", "Author", "Title", "URL", "Publish Date", "Created at", "lastModifiedAt"},
                    0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        
        for(Article article : articles){
            model.addRow(new Object[]{
                article.getId(),
                article.getSource(),
                article.getAuthor(),
                article.getTitle(),
                article.getUrl(),
                article.getPublishDate(),
                article.getCreatedAt(),
                article.getLastModifiedAt()
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
        
        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if(rows.length < 1){
                JOptionPane.showMessageDialog(
                this,
                "Please select at least one article");
                return;
            }
            else{
            int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this article?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION){
                List<Integer> selectedRowIDs = new ArrayList<>();
                for(int row : rows){
                    int id = (Integer) model.getValueAt(row, 0);
                    selectedRowIDs.add(id);
                }
                ArticleDao daoForDelete = new ArticleDao();
                for(int id : selectedRowIDs){
                    daoForDelete.deleteArticle(id);
                }
                loadArticles();
            }
            }
            
        });
        // Edit button
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> {
            String[] options = {"Title", "Author"};

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "What do you want to edit?",
                    "Edit Article",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            // Ha title modosit 
            if (choice == 0) { 

                String newTitle = JOptionPane.showInputDialog(
                        this,
                        "Enter new title:"
                );

                if (newTitle != null && !newTitle.isBlank()) {
                    System.out.println("New title: " + newTitle);
                    // uj article letrehozas az update() miatt
                    Article article = new Article();
                    
                    if (table.getSelectedRowCount() != 1) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please select exactly one article.");
                        return;
                    }
                    int row = table.getSelectedRow();
                    
                    Timestamp currentDateModified = Timestamp.valueOf(LocalDateTime.now());
                    
                    article.setId((Integer) model.getValueAt(row, 0));
                    article.setSource((String) model.getValueAt(row, 1));
                    article.setAuthor((String) model.getValueAt(row, 2));
                    article.setTitle((String) newTitle);
                    article.setUrl((String) model.getValueAt(row, 4));
                    article.setPublishDate((Timestamp) model.getValueAt(row, 5));
                    article.setCreatedAt((Timestamp) model.getValueAt(row, 6));
                    article.setLastModifiedAt(currentDateModified);
                    
                    dao.updateArticle(article);
                    loadArticles();
                }
            // ha Author modosit
            } else if (choice == 1) {

                String newAuthor = JOptionPane.showInputDialog(
                        this,
                        "Enter new author:"
                );

                if (newAuthor != null && !newAuthor.isBlank()) {
                    System.out.println("New author: " + newAuthor);
                    Article article = new Article();
                    
                    if (table.getSelectedRowCount() != 1) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please select exactly one article.");
                        return;
                    }
                    int row = table.getSelectedRow();
                    
                    Timestamp currentDateModified = Timestamp.valueOf(LocalDateTime.now());
                    
                    article.setId((Integer) model.getValueAt(row, 0));
                    article.setSource((String) model.getValueAt(row, 1));
                    article.setAuthor((String) newAuthor);
                    article.setTitle((String) model.getValueAt(row,3 ));
                    article.setUrl((String) model.getValueAt(row, 4));
                    article.setPublishDate((Timestamp) model.getValueAt(row, 5));
                    article.setCreatedAt((Timestamp) model.getValueAt(row, 6));
                    article.setLastModifiedAt(currentDateModified);
                    
                    dao.updateArticle(article);
                    loadArticles();
                }

            } else {

                System.out.println("User cancelled");
            }
        });
        
                
        // Search function
        JPanel topPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        topPanel.add(searchField);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(searchButton);
        topPanel.add(button);
        add(topPanel, BorderLayout.NORTH);
        
        // Search button action listener
        searchButton.addActionListener(ev -> {
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
                article.getPublishDate(),
                article.getCreatedAt(),
                article.getLastModifiedAt()
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
                article.getPublishDate(),
                article.getCreatedAt(),
                article.getLastModifiedAt()
            });
        }
    }
}