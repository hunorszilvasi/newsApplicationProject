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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class NewsFrame extends JFrame {
    private DefaultTableModel model;
    // attributes
    private JLabel titleLabel;
    private JLabel sourceLabel;
    private JLabel authorLabel;
    private JLabel publisherLabel;
    private JLabel dateLabel;
    private JLabel coverImageLabel;
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
                "Cover Image",
                "Is Read"
            },
            0
    ) {
        // Locking the cells
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
                article.getCoverImage(),
                article.isRead()
            });
        }
        JTable table = new JTable(model);
        
        // Text highlighter after marked as read
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {

            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                java.awt.Component component =
                        super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                int modelRow = table.convertRowIndexToModel(row);
                boolean isRead = Boolean.TRUE.equals(model.getValueAt(modelRow, 12));

                // Keep the normal selection colors on the selected row
                if (isRead && !isSelected) {
                    // Set color of the text
                    component.setForeground(new java.awt.Color(190, 190, 190));
                } else if (!isSelected) {
                    component.setForeground(table.getForeground());
                }

                return component;
            }
        });
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        // Hidden fields
        String[] hiddenColumns = {
            "ID",
            "Author",
            "Description",
            "Publisher",
            "URL",
            "Created At",
            "Last Modified",
            "Language",
            "Cover Image",
            "Is Read"
        };

        for (String columnName : hiddenColumns) {
            table.removeColumn(table.getColumn(columnName));
        }

        table.setAutoCreateRowSorter(true);
        
        // Image
        coverImageLabel = new JLabel("Select an article to view its image");
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        coverImageLabel.setPreferredSize(new Dimension(300, 200));
        
        // ADD button
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addArticle());
        
        // Refresh button
        // Loading animation
        ImageIcon originalIcon =
        new ImageIcon(getClass().getResource("/loading.gif"));
        
        Image scaledImage = // Scale the loading GIF
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
                // Collect the keys first, because deleting changes the table rows
                List<String[]> selectedKeys = new ArrayList<>();
                for (int viewRow : rows) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    selectedKeys.add(new String[]{
                        (String) model.getValueAt(modelRow, 2), // source
                        (String) model.getValueAt(modelRow, 6)  // url
                    });
                }
                ArticleDao daoForDelete = new ArticleDao();
                for (String[] key : selectedKeys) {
                    daoForDelete.deleteArticle(key[0], key[1]);
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
        
        // Edit button: opens a form with all article fields
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
 
            // The table can be sorted, so convert the view row to the model row
            int row = table.convertRowIndexToModel(table.getSelectedRow());

            // The key BEFORE editing: source and url are the primary key and can be changed
            String oldSource = (String) model.getValueAt(row, 2);
            String oldUrl = (String) model.getValueAt(row, 6);
 
            // Turns a table cell into text; a NULL cell becomes an empty string, not "null"
            java.util.function.Function<Object, String> text =
                    value -> value == null ? "" : value.toString();
 
            // Form fields, pre-filled with the current values of the selected article
            JTextField titleField = new JTextField(text.apply(model.getValueAt(row, 1)), 30);
            JTextField sourceField = new JTextField(text.apply(model.getValueAt(row, 2)), 30);
            JTextField urlField = new JTextField(text.apply(model.getValueAt(row, 6)), 30);
            JTextField authorField = new JTextField(text.apply(model.getValueAt(row, 3)), 30);
            JTextField publisherField = new JTextField(text.apply(model.getValueAt(row, 5)), 30);
            JTextField coverImageField = new JTextField(text.apply(model.getValueAt(row, 11)), 30);
 
            Object publishValue = model.getValueAt(row, 7);
            String publishText = publishValue instanceof Timestamp ts
                    ? ts.toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "";
            JTextField publishDateField = new JTextField(publishText, 30);
 
            // Language is RO or HU; any other stored value is kept as an extra choice
            JComboBox<String> languageBox = new JComboBox<>(new String[]{"RO", "HU"});
            String language = text.apply(model.getValueAt(row, 10));
            if (language.equalsIgnoreCase("RO") || language.equalsIgnoreCase("HU")) {
                languageBox.setSelectedItem(language.toUpperCase());
            } else if (!language.isBlank()) {
                languageBox.addItem(language);
                languageBox.setSelectedItem(language);
            }
 
            JTextArea descriptionArea = new JTextArea(text.apply(model.getValueAt(row, 4)), 8, 30);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
 
            // Build the form: short fields on top, description fills the rest
            JPanel fields = new JPanel(new GridLayout(0, 2, 5, 5));
            fields.add(new JLabel("Title *:"));
            fields.add(titleField);
            fields.add(new JLabel("Source *:"));
            fields.add(sourceField);
            fields.add(new JLabel("URL *:"));
            fields.add(urlField);
            fields.add(new JLabel("Author:"));
            fields.add(authorField);
            fields.add(new JLabel("Publisher:"));
            fields.add(publisherField);
            fields.add(new JLabel("Publish date (yyyy-MM-dd HH:mm:ss):"));
            fields.add(publishDateField);
            fields.add(new JLabel("Language:"));
            fields.add(languageBox);
            fields.add(new JLabel("Cover image (URL or resource path):"));
            fields.add(coverImageField);
 
            JPanel descriptionPanel = new JPanel(new BorderLayout(5, 5));
            descriptionPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
            descriptionPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
 
            JPanel form = new JPanel(new BorderLayout(5, 10));
            form.add(fields, BorderLayout.NORTH);
            form.add(descriptionPanel, BorderLayout.CENTER);
 
            // Show the form again with the typed values until the input is valid or cancelled
            Timestamp publishDate;
            String title;
            String source;
            String url;
 
            while (true) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        form,
                        "Edit Article",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );
 
                // Cancel or close
                if (result != JOptionPane.OK_OPTION) {
                    return;
                }
 
                title = titleField.getText().trim();
                source = sourceField.getText().trim();
                url = urlField.getText().trim();
                String dateText = publishDateField.getText().trim();
                publishDate = null;
                String error = null;
 
                if (title.isEmpty() || source.isEmpty() || url.isEmpty()) {
                    error = "Title, Source and URL are required.";
                } else {
                    try {
                        java.net.URI uri = new java.net.URI(url);
                        if (uri.getHost() == null
                                || !("http".equalsIgnoreCase(uri.getScheme())
                                || "https".equalsIgnoreCase(uri.getScheme()))) {
                            error = "Please enter a valid HTTP or HTTPS URL.";
                        }
                    } catch (java.net.URISyntaxException ex) {
                        error = "The URL format is invalid.";
                    }
                }
 
                if (error == null && !dateText.isEmpty()) {
                    try {
                        publishDate = Timestamp.valueOf(dateText);
                    } catch (IllegalArgumentException ex) {
                        error = "Invalid date format.\n"
                                + "Use: yyyy-MM-dd HH:mm:ss\n"
                                + "Example: 2026-10-12 14:30:00";
                    }
                }
 
                if (error == null) {
                    break;
                }
 
                JOptionPane.showMessageDialog(
                        this,
                        error,
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
            }
 
            // Build the updated article; the creation date stays unchanged
            Article article = new Article();
 
            article.setTitle(title);
            article.setSource(source);
            article.setUrl(url);
            article.setAuthor(authorField.getText().isBlank() ? null : authorField.getText().trim());
            article.setPublisher(publisherField.getText().isBlank() ? null : publisherField.getText().trim());
            article.setPublishDate(publishDate);
            article.setLanguage((String) languageBox.getSelectedItem());
            article.setCoverImage(coverImageField.getText().isBlank() ? null : coverImageField.getText().trim());
            article.setDescription(descriptionArea.getText().trim());
 
            if (model.getValueAt(row, 8) instanceof Timestamp createdAt) {
                article.setCreatedAt(createdAt);
            }
 
            // Update modification timestamp
            article.setLastModifiedAt(
                    Timestamp.valueOf(LocalDateTime.now())
            );
 
            // Save; fails if the new (source, url) already belongs to another article
            if (!dao.updateArticle(article, oldSource, oldUrl)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not save the changes.\n"
                        + "Another article may already use this source and URL.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Reloading the gui
            loadArticles();

            // Find the updated article by its new key and select it
            for (int i = 0; i < model.getRowCount(); i++) {
                boolean sameKey = source.equals(model.getValueAt(i, 2))
                        && url.equals(model.getValueAt(i, 6));

                if (sameKey) {
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
        });
        
        

        // Search label
        JPanel topPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        topPanel.add(addButton);
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
                article.getCoverImage(),
                article.isRead()
            });
        }
        });
        
        // 2 sided panel view (Gmail look)
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

                    // Show article details
                    showArticleDetails(modelRow);

                    // Check if the article is already read
                    boolean isRead = Boolean.TRUE.equals(
                            model.getValueAt(modelRow, 12)
                    );

                    if (!isRead) {

                        // Get the article's source and URL
                        String source = String.valueOf(
                                model.getValueAt(modelRow, 2)
                        );

                        String url = String.valueOf(
                                model.getValueAt(modelRow, 6)
                        );

                        // Save the read status in the database
                        ArticleDao articleDao = new ArticleDao();

                        boolean success = articleDao.markAsRead(source, url);

                        if (success) {

                            // Update the table model
                            model.setValueAt(true, modelRow, 12);

                            // Refresh the table appearance
                            table.repaint();

                            System.out.println(
                                    "Article marked as read: " + source
                            );
                        }
                    }
                }
            }
        });

        setVisible(true);
        
        }    
    
    // ADD function + panel
    private void addArticle() {
        // Create input fields
        JTextField titleField = new JTextField("Title", 25);
        JTextField sourceField = new JTextField("Source", 25);
        JTextField urlField = new JTextField("https://www.example.com", 25);
        JTextField authorField = new JTextField("Author", 25);
        JTextField publisherField = new JTextField("Publisher", 25);
        JTextField publishDateField = new JTextField("2026-10-10 10:00:00", 25);
        JTextField languageField = new JTextField("HU", 25);
        JTextField coverImageField = new JTextField("images/default.png", 25);

        JTextArea descriptionField = new JTextArea("Description", 4, 25);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);

        // Build the form
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        panel.add(new JLabel("Title *:"));
        panel.add(titleField);

        panel.add(new JLabel("Source *:"));
        panel.add(sourceField);

        panel.add(new JLabel("URL *:"));
        panel.add(urlField);

        panel.add(new JLabel("Author:"));
        panel.add(authorField);

        panel.add(new JLabel("Publisher:"));
        panel.add(publisherField);

        panel.add(new JLabel("Publish Date:"));
        panel.add(publishDateField);

        panel.add(new JLabel("Language:"));
        panel.add(languageField);

        panel.add(new JLabel("Cover Image URL:"));
        panel.add(coverImageField);

        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionField));

        // Display the form
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Article",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        // Stop if the user cancels
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Extracting the user inputs
        String title = titleField.getText().trim();
        String source = sourceField.getText().trim();
        String url = urlField.getText().trim();

        if (title.isEmpty() || source.isEmpty() || url.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Title, Source and URL are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validate URL
        try {
            java.net.URI uri = new java.net.URI(url);

            if (uri.getHost() == null
                    || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                // Show a warning dialog if the URL is invalid
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid HTTP or HTTPS URL.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

        } catch (java.net.URISyntaxException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "The URL format is invalid.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Parse the publication date if provided
        Timestamp publishDate = null;
        String publishDateText = publishDateField.getText().trim();

        if (!publishDateText.isEmpty()) {
            try {
                publishDate = Timestamp.valueOf(publishDateText);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid date format.\n"
                        + "Use: yyyy-MM-dd HH:mm:ss\n"
                        + "Example: 2026-10-12 14:30:00",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }

        // Set timestamps
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Creating the article with the input data
        Article article = new Article();

        article.setTitle(title);
        article.setSource(source);
        article.setUrl(url);
        article.setAuthor(authorField.getText().trim());
        article.setPublisher(publisherField.getText().trim());
        article.setDescription(descriptionField.getText().trim());
        article.setPublishDate(publishDate);
        article.setCreatedAt(now);
        article.setLastModifiedAt(now);

        String language = languageField.getText().trim();
        article.setLanguage(language.isEmpty() ? "HU" : language);

        String coverImage = coverImageField.getText().trim();
        article.setCoverImage(coverImage.isEmpty() ? null : coverImage);

        // Saving the new article to the DB
        ArticleDao articleDao = new ArticleDao();
        int saved = articleDao.save(article);

        if (saved == 1) {
            // Reloading the GUI if succeed
            loadArticles();

            JOptionPane.showMessageDialog(
                    this,
                    "Article added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else if (saved == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "An article with this source and URL already exists.",
                    "Duplicate Article",
                    JOptionPane.WARNING_MESSAGE
            );

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save the article. Check the console for details.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    
    // Loading in the articles in the GUI
    private void loadArticles() {
        model.setRowCount(0);

        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();
        articles.sort(new ArticleTitleComparator()); // ABC order comparator

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
                article.getCoverImage(),
                article.isRead()
            });
        }
    }
    // cover image load
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
    // Right sided panel 
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
                // opening the browser
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
    // Loading in the right sided panel 
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
    // Deleting the right sided panel (only in GUI)
    private void clearArticleDetails() {
        // article details
        titleLabel.setText("");
        sourceLabel.setText("Source: ");
        authorLabel.setText("Author: ");
        publisherLabel.setText("Publisher: ");
        dateLabel.setText("Published: ");
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