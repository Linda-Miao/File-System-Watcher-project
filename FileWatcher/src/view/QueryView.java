package view;

/* TCSS 360 File watcher project */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import model.QueryManager;
import model.FileEvent;
import model.EventType;
import java.util.List;

/**
 * Enhanced QueryView with comprehensive search capabilities.
 * Supports extension, event type, directory path, filename, and date range queries.
 *
 * @author Mutahar Wafayee
 * @author Linda Miao
 * @version June 13, 2025
 */
public class QueryView {

    // ------ GUI components -------
    // Main dialog and panels
    private JDialog queryDialog;
    private JPanel queryFormPanel;
    private DefaultTableModel tableModel;

    // Basic search components
    private final JTextField extensionTextField;
    private final JButton submitButton;

    // Enhanced search components
    private JComboBox<String> eventTypeComboBox;
    private JTextField fileNameTextField;
    private JButton advancedSearchButton;
    private JButton clearButton;
    private JButton clearFieldsButton;

    private JButton clearDbButton;


    // ------- Data ------
    // Date formatting
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Event types for dropdown (matching backend EventType enum)
    private final String[] eventTypes = {
            "ALL", "ENTRY_CREATE", "ENTRY_MODIFY", "ENTRY_DELETE"
    };

    // Reference to main view and QueryManager
    private final FileWatcherView watcherView;
    private final QueryManager queryManager;

    //------- Constructor -------
    public QueryView(final FileWatcherView watcherView) {
        this.watcherView = watcherView;
        this.queryManager = new QueryManager();

        // Initialize basic components
        submitButton = new JButton("Search Extension");
        extensionTextField = new JTextField(4);
        extensionTextField.setText(watcherView.getSelectedExtension()); // Pre-fill with selected extension

        // Initialize enhanced components
        initializeEnhancedComponents();

        // Create and setup dialog
        createQueryDialog();

        // Setup UI layout
        setupUI();

        // Setup event listeners
        setupEventListeners();

        // Show dialog
        finalizeDialog();
    }

    // ------ Initialization -----
    /** Initializes all the new enhanced UI components.*/
    private void initializeEnhancedComponents() {

        // Enhanced search components
        eventTypeComboBox = new JComboBox<>(eventTypes);
        fileNameTextField = new JTextField(15);
        advancedSearchButton = new JButton("Advanced Search");
        clearButton = new JButton("Clear Results");
        clearFieldsButton = new JButton("Clear All Fields");
        clearDbButton = new JButton("Clear Database");

        // Set placeholder text and tooltips
        setPlaceholderText(extensionTextField, "txt, pdf, java, html...");
        setPlaceholderText(fileNameTextField, "report, *.txt, Main.java...");

        fileNameTextField.setToolTipText("Enter filename or pattern (e.g., *.txt, report)");
        eventTypeComboBox.setToolTipText("Select event type to filter");
    }

    /** Creates the main query dialog. */
    private void createQueryDialog() {
        queryDialog = new JDialog(watcherView.mainFrame, "Enhanced Query Interface", true);
        queryFormPanel = new JPanel(new BorderLayout());
        queryDialog.setLayout(new GridBagLayout());

        // Initialize table model
        tableModel = new DefaultTableModel(
                new Object[]{"Row", "Extension", "Filename", "Path", "Event", "Date/Time"}, 0);
    }

    // ----- GUI setup -----
    /** Sets up the complete UI layout.*/
    private void setupUI() {
        createEnhancedFormPanel();
        createTablePanel();
        layoutMainDialog();
    }

    /** Creates the enhanced form panel with all search options. */
    private void createEnhancedFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title and instructions
        addInstructionSection(formPanel, gbc);

        // Basic search section
        addBasicSearchSection(formPanel, gbc);

        // Advanced search section
        addAdvancedSearchSection(formPanel, gbc);

        // Action buttons section
        addActionButtonsSection(formPanel, gbc);

        queryFormPanel.add(formPanel, BorderLayout.CENTER);
    }

    /** Adds instruction section.*/
    private void addInstructionSection(final JPanel formPanel, final GridBagConstraints gbc) {
        JLabel titleLabel = new JLabel("Advanced File Event Search");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);

        // Reset for next sections
        gbc.gridwidth = 1;
    }

    /** Adds basic search section (extension).*/
    private void addBasicSearchSection(final JPanel formPanel, final GridBagConstraints gbc) {
        gbc.gridy = 1; gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Extension:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(extensionTextField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(submitButton, gbc);

        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    }

    /** Adds advanced search section (event type, filename).*/
    private void addAdvancedSearchSection(final JPanel formPanel, final GridBagConstraints gbc) {
        gbc.gridy = 2; gbc.gridx = 0;
        formPanel.add(new JLabel("Event Type:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(eventTypeComboBox, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Filename:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(fileNameTextField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(advancedSearchButton, gbc);

        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    }


    /** Adds action buttons section.*/
    private void addActionButtonsSection(final JPanel formPanel, final GridBagConstraints gbc) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        clearButton.setBackground(new Color(255, 140, 140)); // Light red
        clearFieldsButton.setBackground(new Color(140, 140, 255)); // Light blue
        clearDbButton.setBackground(Color.CYAN);

        actionPanel.add(clearFieldsButton);
        actionPanel.add(clearButton);
        actionPanel.add(clearDbButton);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 5;
        formPanel.add(actionPanel, gbc);
    }


    /* returns clear db button. */
    public JButton getClearDbButton() {
        return clearDbButton;
    }



    /** Creates the table panel for displaying results.*/
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());

        JLabel resultsLabel = new JLabel("Query Results:");
        resultsLabel.setFont(resultsLabel.getFont().deriveFont(Font.BOLD, 12f));
        tablePanel.add(resultsLabel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // Row
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // Extension
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Filename
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // PATH
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Event
        table.getColumnModel().getColumn(5).setPreferredWidth(130);  // Date/Time

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        queryDialog.add(tablePanel, createTableConstraints());
    }

    /** Layouts the main dialog components.*/
    private void layoutMainDialog() {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        queryDialog.add(queryFormPanel, gbc);
    }

    /** Creates constraints for table panel. */
    private GridBagConstraints createTableConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    // ------ Event listeners ------

    /**
     * This method sets up event listeners
     */
    private void setupEventListeners() {
        submitButton.addActionListener(e -> handleExtensionQuery());
        advancedSearchButton.addActionListener(e -> handleAdvancedQuery());
        clearButton.addActionListener(e -> clearResults());
        clearFieldsButton.addActionListener(e -> clearAllFields());
        clearDbButton.addActionListener(e -> handleClearDatabase());
    }


    /**
     * This method clears the database.
     */
    private void handleClearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
                queryFormPanel,
                "Are you sure you want to clear the entire database?",
                "Confirm Database Clear",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                queryManager.clearDatabase();
                JOptionPane.showMessageDialog(
                        queryFormPanel,
                        "Cleared the entire database successfully",
                        "Database Cleared",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        queryFormPanel,
                        "Failed to clear the database: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }

    // ------ Query handlers -------//

    /**
     * This methods handles extension query.
     */
    private void handleExtensionQuery() {
        String extension = getActualText(extensionTextField, "txt, pdf, java, html...");

        tableModel.setRowCount(0);
        try {
            if (!extension.isEmpty() && !extension.startsWith(".")) {
                extension = "." + extension;
            }
            List<FileEvent> events = queryManager.queryByExtension(extension);
            populateTable(events);
            showMessage("Queried by extension: " + (extension.isEmpty() ? "ALL files" : extension),
                    "Extension Search", JOptionPane.INFORMATION_MESSAGE);
            checkNoResults("extension: " + (extension.isEmpty() ? "ALL files" : extension));
        } catch (Exception ex) {
            showError("Extension query failed: " + ex.getMessage());
        }
    }


    /**
     * This method handles advanced searches.
     */
    private void handleAdvancedQuery() {
        String extension = getActualText(extensionTextField, "txt, pdf, java, html...");
        String eventTypeStr = (String) eventTypeComboBox.getSelectedItem();
        String filename = getActualText(fileNameTextField, "report, *.txt, Main.java...");

        tableModel.setRowCount(0);
        try {
            List<FileEvent> events = queryManager.queryByExtension(extension.isEmpty() ? null : "." + extension);
            if (!eventTypeStr.equals("ALL")) {
                EventType eventType = EventType.valueOf(eventTypeStr);
                events = events.stream()
                        .filter(e -> e.getMyEventType() == eventType)
                        .toList();
            }

            if (!filename.isEmpty()) {
                String finalFilename = filename.toLowerCase();
                events = events.stream()
                        .filter(e -> e.getMyFileName().toLowerCase().contains(finalFilename))
                        .toList();
            }
            populateTable(events);
            StringBuilder summary = new StringBuilder("Advanced Search Applied:");
            if (!extension.isEmpty()) summary.append("\n• Extension: ").append("." + extension);
            if (!eventTypeStr.equals("ALL")) summary.append("\n• Event Type: ").append(eventTypeStr);
            if (!filename.isEmpty()) summary.append("\n• Filename: ").append(filename);
            showMessage(summary.toString(), "Advanced Search", JOptionPane.INFORMATION_MESSAGE);
            checkNoResults("advanced search criteria");
        } catch (Exception ex) {
            showError("Advanced query failed: " + ex.getMessage());
        }
    }
    

    /**
     * This method clears the results.
     */
    private void clearResults() {
        tableModel.setRowCount(0);
        showMessage("Results cleared", "Clear Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method clears all fields from the view table.
     */
    private void clearAllFields() {
        setPlaceholderText(extensionTextField, "txt, pdf, java, html...");
        setPlaceholderText(fileNameTextField, "report, *.txt, Main.java...");
        eventTypeComboBox.setSelectedIndex(0);
        showMessage("All fields cleared", "Clear Fields", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------ Helper Methods -------

    /**
     * This method populates the table.
     *
     * @param events
     */
    private void populateTable(final List<FileEvent> events) {
        for (int i = 0; i < events.size(); i++) {
            FileEvent event = events.get(i);
            tableModel.addRow(new Object[]{
                    i + 1,
                    event.getMyFileExtension(),
                    event.getMyFileName(),
                    event.getMyPath(),
                    event.getMyEventType().toString(),
                    event.getMyTimeStamp().format(dateTimeFormatter)
            });
        }
    }

    /**
     * This method sets up the textfields.
     *
     * @param textField
     * @param placeholder
     */
    private void setPlaceholderText(final JTextField textField, final String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * This method returns the text from the textfield.
     *
     * @param textField
     * @param placeholder
     * @return
     */
    private String getActualText(final JTextField textField, final String placeholder) {
        String text = textField.getText().trim();
        if (text.equals(placeholder) || textField.getForeground().equals(Color.GRAY)) {
            return "";
        }
        return text;
    }

    /**
     * Checks if no results found.
     *
     * @param criteria
     */
    private void checkNoResults(final String criteria) {
        if (tableModel.getRowCount() == 0) {
            showMessage("No files found matching " + criteria, "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows message to the user.
     *
     * @param message
     * @param title
     * @param messageType
     */
    private void showMessage(final String message, final String title, final int messageType) {
        JOptionPane.showMessageDialog(queryDialog, message, title, messageType);
    }

    /**
     * Shoes any error message.
     *
     * @param message
     */
    private void showError(final String message) {
        JOptionPane.showMessageDialog(queryDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /**
     * This method finalizes the dialog window.
     */
    private void finalizeDialog() {
        queryDialog.pack();
        queryDialog.setSize(680, 600);
        queryDialog.setLocationRelativeTo(watcherView.mainFrame);
        queryDialog.setVisible(true);
    }
}