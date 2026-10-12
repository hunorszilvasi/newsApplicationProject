package ro.umfst.newsapplication.ui;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.service.RssReader;
import ro.umfst.newsapplication.dao.*;
import java.util.List;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.model.ArticleTitleComparator;

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
    private DefaultTableModel model;
    // attributumok
    private JLabel titleLabel;
    private JLabel sourceLabel;
    private JLabel authorLabel;
    private JLabel publisherLabel;
    private JLabel dateLabel;
    private JLabel coverImageLabel;
    private JLabel idLabel;
    private JLabel urlLabel;
    private JLabel createdAtLabel;
    private JLabel lastModifiedAtLabel;
    private JLabel languageLabel;

    private JTextArea descriptionArea;
    private JButton openArticleButton;

    private String selectedArticleUrl;
    
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
                "Title",
                "Source",
                "Author",
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
        // ne lehessen beleirni a cellakba
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
        
        for (Article article : articles) {
            model.addRow(new Object[]{
                article.getId(),
                article.getTitle(),
                article.getSource(),
                article.getAuthor(),
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        // Rejtett oszlopok
        String[] hiddenColumns = {
            "ID",
            "Author",
            "Description",
            "Publisher",
            "URL",
            "Created At",
            "Last Modified",
            "Language",
            "Cover Image"
        };

        for (String columnName : hiddenColumns) {
            table.removeColumn(table.getColumn(columnName));
        }

        table.setAutoCreateRowSorter(true);
        
        // kep
        coverImageLabel = new JLabel("Select an article to view its image");
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        coverImageLabel.setPreferredSize(new Dimension(300, 200));
        
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
                clearArticleDetails();
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
                    clearArticleDetails();
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

            // Check if exactly one article is selected
            if (table.getSelectedRowCount() != 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select exactly one article."
                );
                return;
            }

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

            // Cancel vagy close
            if (choice == JOptionPane.CLOSED_OPTION) {
                return;
            }

            // sor alakitas
            int viewRow = table.getSelectedRow();
            int row = table.convertRowIndexToModel(viewRow);

            // Read article data using the current model column order
            Article article = new Article();

            article.setId((Integer) model.getValueAt(row, 0));
            article.setTitle(String.valueOf(model.getValueAt(row, 1)));
            article.setSource(String.valueOf(model.getValueAt(row, 2)));
            article.setAuthor(String.valueOf(model.getValueAt(row, 3)));
            article.setDescription(String.valueOf(model.getValueAt(row, 4)));
            article.setPublisher(String.valueOf(model.getValueAt(row, 5)));
            article.setUrl(String.valueOf(model.getValueAt(row, 6)));

            // Convert date strings to Timestamp safely
            Object publishDateValue = model.getValueAt(row, 7);
            if (publishDateValue != null
                    && !publishDateValue.toString().isBlank()
                    && !publishDateValue.toString().equals("null")) {
                article.setPublishDate(
                        Timestamp.valueOf(publishDateValue.toString())
                );
            }

            Object createdAtValue = model.getValueAt(row, 8);
            if (createdAtValue != null
                    && !createdAtValue.toString().isBlank()
                    && !createdAtValue.toString().equals("null")) {
                article.setCreatedAt(
                        Timestamp.valueOf(createdAtValue.toString())
                );
            }

            article.setLanguage(
                    String.valueOf(model.getValueAt(row, 10))
            );

            article.setCoverImage(
                    String.valueOf(model.getValueAt(row, 11))
            );

            // Update the selected field
            if (choice == 0) { // ha title

                String newTitle = JOptionPane.showInputDialog(
                        this,
                        "Enter new title:",
                        article.getTitle()
                );

                if (newTitle == null || newTitle.isBlank()) {
                    return;
                }

                article.setTitle(newTitle.trim());

            } else if (choice == 1) { // ha author

                String newAuthor = JOptionPane.showInputDialog(
                        this,
                        "Enter new author:",
                        article.getAuthor()
                );

                if (newAuthor == null || newAuthor.isBlank()) {
                    return;
                }

                article.setAuthor(newAuthor.trim());
            }

            // Update modification timestamp
            article.setLastModifiedAt(
                    Timestamp.valueOf(LocalDateTime.now())
            );

            try {
                dao.updateArticle(article);

                // ujratolteni 
                loadArticles();

                // Find the updated article in the table and select it
                // updating the right panel after edit
                for (int i = 0; i < model.getRowCount(); i++) {
                    int currentId = (Integer) model.getValueAt(i, 0);

                    if (currentId == article.getId()) {
                        int viewRow1 = table.convertRowIndexToView(i);

                        if (viewRow1 != -1) {
                            table.setRowSelectionInterval(viewRow1, viewRow1);
                        }

                        // Refresh the right-side details panel
                        showArticleDetails(i);
                        break;
                    }
                }

                JOptionPane.showMessageDialog(
                    this,
                    "Article updated successfully!"
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error updating article: " + ex.getMessage()
                );
            }
        });
        
        

        // Search label
        JPanel topPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        topPanel.add(clearDbButton);
        topPanel.add(loadingLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);
        
        // search button actionListener
        searchButton.addActionListener(ev -> {
            String keyword = searchField.getText();
            List<Article> searchResults = dao.searchByTitle(keyword);
            model.setRowCount(0);
            for(Article article : searchResults){
            model.addRow(new Object[]{
                article.getId(),
                article.getTitle(),
                article.getSource(),
                article.getAuthor(),
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
        });
        
        // Ketto paneles view (gmail szeru)
        JScrollPane tableScrollPane = new JScrollPane(table);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel detailsPanel = createDetailsPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                detailsPanel
        );

        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.35);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = table.getSelectedRow();

                if (viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    showArticleDetails(modelRow);
                }
            }
        });

        setVisible(true);
        
        }    
    // articlek betoltese a guiba
    private void loadArticles() {
        model.setRowCount(0);

        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();
        articles.sort(new ArticleTitleComparator()); // ABC sorrendbe comparatorral

        for (Article article : articles) {
            model.addRow(new Object[]{
                article.getId(),
                article.getTitle(),
                article.getSource(),
                article.getAuthor(),
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
    // cover image betoltes
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
    // jobb oldali panel szerkezete
    private JPanel createDetailsPanel() {

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));

        detailsPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // Article information
        JPanel informationPanel = new JPanel();
        informationPanel.setLayout(
                new BoxLayout(informationPanel, BoxLayout.Y_AXIS)
        );

        titleLabel = new JLabel("Select an article");
        titleLabel.setFont(titleLabel.getFont().deriveFont(22f));

        sourceLabel = new JLabel("Source: -");
        authorLabel = new JLabel("Author: -");
        publisherLabel = new JLabel("Publisher: -");
        dateLabel = new JLabel("Published: -");
        idLabel = new JLabel("ID: -");
        urlLabel = new JLabel("URL: -");
        createdAtLabel = new JLabel("Created At: -");
        lastModifiedAtLabel = new JLabel("Last Modified At: -");
        languageLabel = new JLabel("Language: -");

        informationPanel.add(titleLabel);
        informationPanel.add(Box.createVerticalStrut(10));
        informationPanel.add(sourceLabel);
        informationPanel.add(authorLabel);
        informationPanel.add(publisherLabel);
        informationPanel.add(dateLabel);
        informationPanel.add(idLabel);
        informationPanel.add(urlLabel);
        informationPanel.add(createdAtLabel);
        informationPanel.add(lastModifiedAtLabel);
        informationPanel.add(languageLabel);

        detailsPanel.add(informationPanel, BorderLayout.NORTH);

        // Cover image
        coverImageLabel = new JLabel("No article selected");
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setPreferredSize(new Dimension(400, 220));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(
                BorderFactory.createTitledBorder("Cover Image")
        );
        imagePanel.add(coverImageLabel, BorderLayout.CENTER);

        // Description
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(14f));
        descriptionArea.setText("Select an article to read its description.");

        JScrollPane descriptionScrollPane =
                new JScrollPane(descriptionArea);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(imagePanel, BorderLayout.NORTH);
        contentPanel.add(descriptionScrollPane, BorderLayout.CENTER);

        detailsPanel.add(contentPanel, BorderLayout.CENTER);

        // Open article button
        openArticleButton = new JButton("Open full article");
        openArticleButton.setEnabled(false);
        openArticleButton.addActionListener(e -> {
            if (selectedArticleUrl == null
                    || selectedArticleUrl.isBlank()
                    || selectedArticleUrl.equals("null")) {

                JOptionPane.showMessageDialog(
                        this,
                        "No article URL available.",
                        "Cannot Open Article",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                if (!java.awt.Desktop.isDesktopSupported()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Opening the browser is not supported on this system."
                    );
                    return;
                }
                // megnyitani a browsert
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI(selectedArticleUrl)
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not open the article: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        detailsPanel.add(openArticleButton, BorderLayout.SOUTH);

        return detailsPanel;
    }
    // jobb oldali panel feltoltese
    private void showArticleDetails(int row) {
        if (row < 0) {
            return;
        }

        titleLabel.setText(
            String.valueOf(model.getValueAt(row, 1))
        );

        sourceLabel.setText(
            "Source: " + String.valueOf(model.getValueAt(row, 2))
        );

        authorLabel.setText(
            "Author: " + String.valueOf(model.getValueAt(row, 3))
        );

        publisherLabel.setText(
            "Publisher: " + String.valueOf(model.getValueAt(row, 5))
        );

        dateLabel.setText(
            "Published: " + String.valueOf(model.getValueAt(row, 7))
        );
        idLabel.setText(
            "ID: " + String.valueOf(model.getValueAt(row, 0))
        );

        urlLabel.setText(
            "URL: " + String.valueOf(model.getValueAt(row, 6))
        );

        createdAtLabel.setText(
            "Created At: " + String.valueOf(model.getValueAt(row, 8))
        );

        lastModifiedAtLabel.setText(
            "Last Modified At: " + String.valueOf(model.getValueAt(row, 9))
        );

        languageLabel.setText(
            "Language: " + String.valueOf(model.getValueAt(row, 10))
        );

        String description = String.valueOf(model.getValueAt(row, 4));
        descriptionArea.setText(description);
        descriptionArea.setCaretPosition(0);

        selectedArticleUrl = String.valueOf(model.getValueAt(row, 6));

        openArticleButton.setEnabled(
            selectedArticleUrl != null
            && !selectedArticleUrl.isBlank()
            && !selectedArticleUrl.equals("null")
        );

        String imagePath = String.valueOf(model.getValueAt(row, 11));
        displayCoverImage(imagePath);
    }
    // a jobb oldali panel torlese (pl delete eseten)
    private void clearArticleDetails() {
        // article details
        titleLabel.setText("");
        sourceLabel.setText("Source: ");
        authorLabel.setText("Author: ");
        publisherLabel.setText("Publisher: ");
        dateLabel.setText("Published: ");
        idLabel.setText("ID: ");
        urlLabel.setText("URL: ");
        createdAtLabel.setText("Created At: ");
        lastModifiedAtLabel.setText("Last Modified At: ");
        languageLabel.setText("Language: ");

        // description
        descriptionArea.setText("");

        // cover image
        displayCoverImage(null);

        // selected article URL
        selectedArticleUrl = null;
        openArticleButton.setEnabled(false);
    }
}