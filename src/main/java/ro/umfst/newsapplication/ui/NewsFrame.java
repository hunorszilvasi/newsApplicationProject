package ro.umfst.newsapplication.ui;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.service.RssReader;
import ro.umfst.newsapplication.dao.*;

import ro.umfst.newsapplication.dao.ArticleDao;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.sql.Timestamp;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.*;
import java.time.LocalDateTime;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.JLabel;
import java.util.List;
import java.util.ArrayList;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class NewsFrame extends JFrame {
    DefaultTableModel model;
    private JLabel coverImageLabel;
    public NewsFrame() {
        setLayout(new BorderLayout());
        setTitle("News Reader");
        setSize(1500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();

        model = new DefaultTableModel(
            new String[]{
                "ID",
                "Source",
                "Author",
                "Title",
                "Description",
                "Publisher",
                "URL",
                "Publish Date",
                "Created At",
                "Last Modified",
                "Language",
                "Cover Image"
            },
            0
    ) {
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
                article.getDescription(),
                article.getPublisher(),
                article.getUrl(),
                article.getPublishDate(),
                article.getCreatedAt(),
                article.getLastModifiedAt(),
                article.getLanguage(),
                article.getCoverImage()
            });
        }
        JTable table = new JTable(model);
        
        // Cover image preview
        coverImageLabel = new JLabel("Select an article to view its image");
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        coverImageLabel.setPreferredSize(new Dimension(300, 200));

        // Update image when an article is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();

                if (row >= 0) {
                    String imagePath = (String) model.getValueAt(row, 11);
                    displayCoverImage(imagePath);
                }
            }
        });
        
        // Refresh Gomb
        // Loading animation
        ImageIcon originalIcon =
        new ImageIcon(getClass().getResource("/loading.gif"));
        
        Image scaledImage = // loading gif meretezese
        originalIcon.getImage().getScaledInstance(
                32, 32, Image.SCALE_DEFAULT);

        ImageIcon loadingIcon = new ImageIcon(scaledImage);

        JLabel loadingLabel = new JLabel(loadingIcon);
        
        loadingLabel.setVisible(false);
        //refresh
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {

            loadingLabel.setVisible(true);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                @Override
                protected Void doInBackground() throws Exception {

                    RssReader reader = new RssReader();
                    reader.testFeed();

                    return null;
                }

                @Override
                protected void done() {

                    loadArticles();
                    loadingLabel.setVisible(false);

                    JOptionPane.showMessageDialog(
                            NewsFrame.this,
                            "Refresh completed!"
                    );
                }
            };

            worker.execute();
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
        // Clear DB
        JButton clearDbButton = new JButton("Clear data");
        clearDbButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete all articles?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {

                boolean success = DatabaseManager.clearDatabase();

                if (success) {
                    loadArticles();

                    JOptionPane.showMessageDialog(
                        this,
                        "Database cleared successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to clear the database. Please try again.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );
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
        
        topPanel.add(clearDbButton);
        topPanel.add(loadingLabel);
        topPanel.add(searchField);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(searchButton);
        topPanel.add(refreshButton);
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
        
        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(
                BorderFactory.createTitledBorder("Article Image")
        );
        imagePanel.add(coverImageLabel, BorderLayout.CENTER);

        centerPanel.add(imagePanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

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
                article.getDescription(),
                article.getPublisher(),
                article.getUrl(),
                article.getPublishDate(),
                article.getCreatedAt(),
                article.getLastModifiedAt(),
                article.getLanguage(),
                article.getCoverImage()
            });
        }
    }
    private void displayCoverImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            coverImageLabel.setIcon(null);
            coverImageLabel.setText("No image available");
            return;
        }

        try {
            ImageIcon imageIcon;

            if (imagePath.startsWith("http://")
                    || imagePath.startsWith("https://")) {

                imageIcon = new ImageIcon(new java.net.URL(imagePath));

            } else {
                java.net.URL resource =
                        getClass().getResource("/" + imagePath);

                if (resource == null) {
                    coverImageLabel.setIcon(null);
                    coverImageLabel.setText("Image not found");
                    return;
                }

                imageIcon = new ImageIcon(resource);
            }

            if (imageIcon.getIconWidth() <= 0
                    || imageIcon.getIconHeight() <= 0) {
                coverImageLabel.setIcon(null);
                coverImageLabel.setText("Invalid image");
                System.out.println("Could not decode image: " + imagePath);
                return;
            }

            Image scaledImage = imageIcon.getImage().getScaledInstance(
                    280,
                    180,
                    Image.SCALE_SMOOTH
            );

            coverImageLabel.setText("");
            coverImageLabel.setIcon(new ImageIcon(scaledImage));

        } catch (Exception ex) {
            coverImageLabel.setIcon(null);
            coverImageLabel.setText("Could not load image");
            ex.printStackTrace();
        }
    }
}