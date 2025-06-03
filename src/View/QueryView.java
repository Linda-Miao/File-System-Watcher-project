package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Enhanced QueryView with comprehensive search capabilities.
 * Supports extension, event type, directory path, filename, and date range queries.
 */
public class QueryView {

    // ========================= UI COMPONENTS =========================

    // Main dialog and panels
    private JDialog queryDialog;
    private JPanel queryFormPanel;
    private DefaultTableModel tableModel;

    // Basic search components
    private final JTextField extensionTextField;
    private final JButton submitButton;

    // Date range components
    private JTextField startDateTextField;
    private JTextField endDateTextField;
    private JButton submitDateButton;

    // NEW: Enhanced search components
    private JComboBox<String> eventTypeComboBox;
    private JTextField directoryPathTextField;
    private JTextField fileNameTextField;
    private JButton advancedSearchButton;
    private JButton clearButton;
    private JButton clearFieldsButton;

    // NEW: Quick time filter buttons
    private JButton last24HoursButton;
    private JButton lastWeekButton;
    private JButton lastMonthButton;

    // ========================= DATA =========================

    // Date formatting
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Event types for dropdown (matching backend EventType enum)
    private final String[] eventTypes = {
            "ALL", "CREATED", "MODIFIED", "DELETED", "ACCESSED", "ARCHIVED"
    };

    // Reference to main view
    private final FileWatcherView watcherView;

    // ========================= CONSTRUCTOR =========================

    public QueryView(FileWatcherView watcherView) {
        this.watcherView = watcherView;

        // Initialize basic components
        submitButton = new JButton("Search Extension");
        extensionTextField = new JTextField(10);

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

    // ========================= INITIALIZATION =========================

    /**
     * Initializes all the new enhanced UI components.
     */
    private void initializeEnhancedComponents() {
        // Date components
        startDateTextField = new JTextField(12);
        endDateTextField = new JTextField(12);
        submitDateButton = new JButton("Search Dates");

        // Enhanced search components
        eventTypeComboBox = new JComboBox<>(eventTypes);
        directoryPathTextField = new JTextField(15);
        fileNameTextField = new JTextField(15);
        advancedSearchButton = new JButton("Advanced Search");
        clearButton = new JButton("Clear Results");
        clearFieldsButton = new JButton("Clear All Fields");

        // Quick time buttons
        last24HoursButton = new JButton("Last 24h");
        lastWeekButton = new JButton("Last Week");
        lastMonthButton = new JButton("Last Month");

        // Set placeholder text and tooltips
        setPlaceholderText(extensionTextField, "txt, pdf, java, html...");
        setPlaceholderText(directoryPathTextField, "C:\\Documents, /home/user...");
        setPlaceholderText(fileNameTextField, "report, *.txt, Main.java...");
        setPlaceholderText(startDateTextField, "yyyy-mm-dd");
        setPlaceholderText(endDateTextField, "yyyy-mm-dd");

        startDateTextField.setToolTipText("Format: yyyy-MM-dd (e.g., 2023-10-15)");
        endDateTextField.setToolTipText("Format: yyyy-MM-dd (e.g., 2023-10-15)");
        directoryPathTextField.setToolTipText("Enter directory path (e.g., C:\\Documents)");
        fileNameTextField.setToolTipText("Enter filename or pattern (e.g., *.txt, report)");
        eventTypeComboBox.setToolTipText("Select event type to filter");
    }

    /**
     * Creates the main query dialog.
     */
    private void createQueryDialog() {
        queryDialog = new JDialog(watcherView.mainFrame, "Enhanced Query Interface", true);
        queryFormPanel = new JPanel(new BorderLayout());
        queryDialog.setLayout(new GridBagLayout());

        // Initialize table model
        tableModel = new DefaultTableModel(
                new Object[]{"Row", "Extension", "Filename", "PATH", "Event", "Date/Time"}, 0);
        loadSampleData();
    }

    // ========================= UI SETUP =========================

    /**
     * Sets up the complete UI layout.
     */
    private void setupUI() {
        createEnhancedFormPanel();
        createTablePanel();
        layoutMainDialog();
    }

    /**
     * Creates the enhanced form panel with all search options.
     */
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

        // Date search section
        addDateSearchSection(formPanel, gbc);

        // Quick time filters section
        addQuickTimeSection(formPanel, gbc);

        // Action buttons section
        addActionButtonsSection(formPanel, gbc);

        queryFormPanel.add(formPanel, BorderLayout.CENTER);
    }

    /**
     * Adds instruction section.
     */
    private void addInstructionSection(JPanel formPanel, GridBagConstraints gbc) {
        JLabel titleLabel = new JLabel("Advanced File Event Search");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);

        // Reset for next sections
        gbc.gridwidth = 1;
    }

    /**
     * Adds basic search section (extension).
     */
    private void addBasicSearchSection(JPanel formPanel, GridBagConstraints gbc) {
        // Extension search row
        gbc.gridy = 1; gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Extension:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(extensionTextField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(submitButton, gbc);

        // Reset
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    }

    /**
     * Adds advanced search section (event type, directory, filename).
     */
    private void addAdvancedSearchSection(JPanel formPanel, GridBagConstraints gbc) {
        // Event Type row
        gbc.gridy = 2; gbc.gridx = 0;
        formPanel.add(new JLabel("Event Type:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(eventTypeComboBox, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Directory:"), gbc);

        gbc.gridx = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.4;
        formPanel.add(directoryPathTextField, gbc);

        // Filename row
        gbc.gridy = 3; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Filename:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(fileNameTextField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(advancedSearchButton, gbc);

        // Reset
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    }

    /**
     * Adds date search section.
     */
    private void addDateSearchSection(JPanel formPanel, GridBagConstraints gbc) {
        // Date range row
        gbc.gridy = 4; gbc.gridx = 0;
        formPanel.add(new JLabel("Start Date:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(startDateTextField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("End Date:"), gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        formPanel.add(endDateTextField, gbc);

        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(submitDateButton, gbc);

        // Reset
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    }

    /**
     * Adds quick time filter section.
     */
    private void addQuickTimeSection(JPanel formPanel, GridBagConstraints gbc) {
        JPanel quickTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quickTimePanel.add(new JLabel("Quick Time Filters:"));
        quickTimePanel.add(last24HoursButton);
        quickTimePanel.add(lastWeekButton);
        quickTimePanel.add(lastMonthButton);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 5;
        formPanel.add(quickTimePanel, gbc);

        // Reset
        gbc.gridwidth = 1;
    }

    /**
     * Adds action buttons section.
     */
    private void addActionButtonsSection(JPanel formPanel, GridBagConstraints gbc) {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        // Style the clear buttons differently
        clearButton.setBackground(new Color(255, 140, 140)); // Light red
        clearFieldsButton.setBackground(new Color(140, 140, 255)); // Light blue

        actionPanel.add(clearFieldsButton);
        actionPanel.add(clearButton);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 5;
        formPanel.add(actionPanel, gbc);
    }

    /**
     * Creates the table panel for displaying results.
     */
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Results label
        JLabel resultsLabel = new JLabel("Query Results:");
        resultsLabel.setFont(resultsLabel.getFont().deriveFont(Font.BOLD, 12f));
        tablePanel.add(resultsLabel, BorderLayout.NORTH);

        // Create table
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // Row
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Extension
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Filename
        table.getColumnModel().getColumn(3).setPreferredWidth(400);  // PATH
        table.getColumnModel().getColumn(4).setPreferredWidth(200);  // Event
        table.getColumnModel().getColumn(5).setPreferredWidth(250);  // Date/Time

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Store table panel reference
        queryDialog.add(tablePanel, createTableConstraints());
    }

    /**
     * Layouts the main dialog components.
     */
    private void layoutMainDialog() {
        GridBagConstraints gbc = new GridBagConstraints();

        // Form panel (top 25%)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        queryDialog.add(queryFormPanel, gbc);
    }

    /**
     * Creates constraints for table panel.
     */
    private GridBagConstraints createTableConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1.0; gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    // ========================= EVENT LISTENERS =========================

    /**
     * Sets up all event listeners.
     */
    private void setupEventListeners() {
        // Basic extension search
        submitButton.addActionListener(e -> handleExtensionQuery());

        // Date range search
        submitDateButton.addActionListener(e -> handleDateRangeQuery());

        // Advanced search (combines multiple criteria)
        advancedSearchButton.addActionListener(e -> handleAdvancedQuery());

        // Quick time filters
        last24HoursButton.addActionListener(e -> handleQuickTimeQuery(1, "hours"));
        lastWeekButton.addActionListener(e -> handleQuickTimeQuery(7, "days"));
        lastMonthButton.addActionListener(e -> handleQuickTimeQuery(30, "days"));

        // Clear results
        clearButton.addActionListener(e -> clearResults());

        // Clear all fields
        clearFieldsButton.addActionListener(e -> clearAllFields());
    }

    // ========================= QUERY HANDLERS =========================

    /**
     * Handles extension-based queries.
     */
    private void handleExtensionQuery() {
        String extension = getActualText(extensionTextField, "txt, pdf, java, html...");

        clearResults();

        try {
            if (extension.isEmpty()) {
                loadSampleData();
                showMessage("Showing all sample events", "All Files", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (!extension.startsWith(".")) {
                    extension = "." + extension;
                }
                filterSampleDataByExtension(extension);
                showMessage("Filtered by extension: " + extension, "Extension Search", JOptionPane.INFORMATION_MESSAGE);
            }

            checkNoResults("extension: " + extension);

        } catch (Exception ex) {
            showError("Extension query failed: " + ex.getMessage());
        }
    }

    /**
     * Handles date range queries.
     */
    private void handleDateRangeQuery() {
        String startDateStr = getActualText(startDateTextField, "yyyy-mm-dd");
        String endDateStr = getActualText(endDateTextField, "yyyy-mm-dd");

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            showMessage("Please enter both start and end dates\nFormat: yyyy-MM-dd",
                    "Missing Date Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
            LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);

            if (startDate.isAfter(endDate)) {
                showMessage("Start date must be before end date",
                        "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }

            clearResults();
            filterSampleDataByDateRange(startDate, endDate);

            showMessage("Filtered by date range: " + startDateStr + " to " + endDateStr,
                    "Date Range Search", JOptionPane.INFORMATION_MESSAGE);

            checkNoResults("date range: " + startDateStr + " to " + endDateStr);

        } catch (DateTimeParseException ex) {
            showMessage("Invalid date format. Please use: yyyy-MM-dd\nExample: 2023-10-15",
                    "Date Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showError("Date range query failed: " + ex.getMessage());
        }
    }

    /**
     * Handles advanced queries combining multiple criteria.
     */
    private void handleAdvancedQuery() {
        String extension = getActualText(extensionTextField, "txt, pdf, java, html...");
        String eventType = (String) eventTypeComboBox.getSelectedItem();
        String directory = getActualText(directoryPathTextField, "C:\\Documents, /home/user...");
        String filename = getActualText(fileNameTextField, "report, *.txt, Main.java...");

        clearResults();

        try {
            // Simulate advanced search with sample data
            loadSampleData();

            // Apply filters sequentially
            if (!extension.isEmpty()) {
                if (!extension.startsWith(".")) extension = "." + extension;
                filterTableByExtension(extension);
            }

            if (!eventType.equals("ALL")) {
                filterTableByEventType(eventType);
            }

            if (!directory.isEmpty()) {
                filterTableByDirectory(directory);
            }

            if (!filename.isEmpty()) {
                filterTableByFilename(filename);
            }

            // Show search summary
            StringBuilder summary = new StringBuilder("Advanced Search Applied:");
            if (!extension.isEmpty()) summary.append("\n• Extension: ").append(extension);
            if (!eventType.equals("ALL")) summary.append("\n• Event Type: ").append(eventType);
            if (!directory.isEmpty()) summary.append("\n• Directory: ").append(directory);
            if (!filename.isEmpty()) summary.append("\n• Filename: ").append(filename);

            showMessage(summary.toString(), "Advanced Search", JOptionPane.INFORMATION_MESSAGE);

            checkNoResults("advanced search criteria");

        } catch (Exception ex) {
            showError("Advanced query failed: " + ex.getMessage());
        }
    }

    /**
     * Handles quick time filter queries.
     */
    private void handleQuickTimeQuery(int amount, String unit) {
        clearResults();

        try {
            // Simulate recent events based on time filter
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = unit.equals("hours") ?
                    now.minusHours(amount) : now.minusDays(amount);

            // Load sample data filtered by time
            loadRecentSampleData(amount, unit);

            showMessage("Showing events from last " + amount + " " + unit,
                    "Quick Time Filter", JOptionPane.INFORMATION_MESSAGE);

            checkNoResults("last " + amount + " " + unit);

        } catch (Exception ex) {
            showError("Quick time query failed: " + ex.getMessage());
        }
    }

    /**
     * Clears all results from the table.
     */
    private void clearResults() {
        tableModel.setRowCount(0);
        showMessage("Results cleared", "Clear Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears all search fields and resets to default values.
     */
    private void clearAllFields() {
        // Reset fields to placeholder text
        setPlaceholderText(extensionTextField, "txt, pdf, java, html...");
        setPlaceholderText(directoryPathTextField, "C:\\Documents, /home/user...");
        setPlaceholderText(fileNameTextField, "report, *.txt, Main.java...");
        setPlaceholderText(startDateTextField, "yyyy-mm-dd");
        setPlaceholderText(endDateTextField, "yyyy-mm-dd");

        // Reset combo box to default
        eventTypeComboBox.setSelectedIndex(0); // Select "ALL"

        // Also clear results
        clearResults();

        showMessage("All fields cleared", "Clear Fields", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========================= PLACEHOLDER TEXT METHODS =========================

    /**
     * Sets placeholder text for a JTextField that appears in gray when empty.
     */
    private void setPlaceholderText(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        // Add focus listener to handle placeholder behavior
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
     * Gets the actual text from a field, excluding placeholder text.
     */
    private String getActualText(JTextField textField, String placeholder) {
        String text = textField.getText().trim();
        if (text.equals(placeholder) || textField.getForeground().equals(Color.GRAY)) {
            return "";
        }
        return text;
    }

    // ========================= FILTER METHODS =========================

    /**
     * Filters table by extension.
     */
    private void filterTableByExtension(String extension) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String rowExtension = (String) tableModel.getValueAt(i, 1);
            if (!extension.equalsIgnoreCase(rowExtension)) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Filters table by event type.
     */
    private void filterTableByEventType(String eventType) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String rowEvent = (String) tableModel.getValueAt(i, 4);
            if (!eventType.equalsIgnoreCase(rowEvent)) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Filters table by directory path.
     */
    private void filterTableByDirectory(String directory) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String rowPath = (String) tableModel.getValueAt(i, 3);
            if (!rowPath.toLowerCase().contains(directory.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Filters table by filename.
     */
    private void filterTableByFilename(String filename) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String rowFilename = (String) tableModel.getValueAt(i, 2);
            if (!rowFilename.toLowerCase().contains(filename.toLowerCase())) {
                tableModel.removeRow(i);
            }
        }
    }

    // ========================= SAMPLE DATA METHODS =========================

    /**
     * Loads sample data for testing.
     */
    private void loadSampleData() {
        tableModel.addRow(new Object[]{1, ".txt", "notes.txt", "C:/Documents/work", "CREATED", "2023-10-15 10:15:00"});
        tableModel.addRow(new Object[]{2, ".java", "Main.java", "C:/Projects/code", "MODIFIED", "2023-10-15 12:30:00"});
        tableModel.addRow(new Object[]{3, ".html", "index.html", "C:/Websites/home", "DELETED", "2023-10-14 09:00:00"});
        tableModel.addRow(new Object[]{4, ".css", "styles.css", "C:/Websites/design", "CREATED", "2023-10-14 11:00:00"});
        tableModel.addRow(new Object[]{5, ".js", "script.js", "C:/Websites/scripts", "ACCESSED", "2023-10-13 14:00:00"});
        tableModel.addRow(new Object[]{6, ".pdf", "paper.pdf", "C:/Documents/research", "ARCHIVED", "2023-10-12 18:45:00"});
        tableModel.addRow(new Object[]{7, ".txt", "report.txt", "C:/Reports", "MODIFIED", "2023-10-16 08:30:00"});
        tableModel.addRow(new Object[]{8, ".java", "Utils.java", "C:/Projects/utils", "CREATED", "2023-10-16 14:20:00"});
    }

    /**
     * Filters sample data by extension.
     */
    private void filterSampleDataByExtension(String extension) {
        if (extension.equals(".txt")) {
            tableModel.addRow(new Object[]{1, ".txt", "notes.txt", "C:/Documents", "CREATED", "2023-10-15 10:15:00"});
            tableModel.addRow(new Object[]{2, ".txt", "readme.txt", "C:/Projects", "MODIFIED", "2023-10-14 15:30:00"});
            tableModel.addRow(new Object[]{3, ".txt", "report.txt", "C:/Reports", "CREATED", "2023-10-16 08:30:00"});
        } else if (extension.equals(".java")) {
            tableModel.addRow(new Object[]{1, ".java", "Main.java", "C:/Code", "CREATED", "2023-10-15 09:00:00"});
            tableModel.addRow(new Object[]{2, ".java", "Utils.java", "C:/Code", "MODIFIED", "2023-10-14 11:15:00"});
            tableModel.addRow(new Object[]{3, ".java", "Test.java", "C:/Code/test", "DELETED", "2023-10-13 16:45:00"});
        } else if (extension.equals(".pdf")) {
            tableModel.addRow(new Object[]{1, ".pdf", "report.pdf", "C:/Documents", "CREATED", "2023-10-13 16:45:00"});
            tableModel.addRow(new Object[]{2, ".pdf", "manual.pdf", "C:/Docs", "ACCESSED", "2023-10-14 10:20:00"});
        }
    }

    /**
     * Filters sample data by date range.
     */
    private void filterSampleDataByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.of(2023, 10, 16)) && endDate.isAfter(LocalDate.of(2023, 10, 14))) {
            tableModel.addRow(new Object[]{1, ".txt", "filtered_file.txt", "C:/Temp", "CREATED", "2023-10-15 10:00:00"});
            tableModel.addRow(new Object[]{2, ".pdf", "report.pdf", "C:/Reports", "MODIFIED", "2023-10-15 15:30:00"});
            tableModel.addRow(new Object[]{3, ".java", "NewClass.java", "C:/Code", "CREATED", "2023-10-15 09:15:00"});
        }
    }

    /**
     * Loads sample data for recent time periods.
     */
    private void loadRecentSampleData(int amount, String unit) {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        tableModel.addRow(new Object[]{1, ".txt", "recent_file.txt", "C:/Temp", "CREATED", timeStr});
        tableModel.addRow(new Object[]{2, ".log", "app.log", "C:/Logs", "MODIFIED", timeStr});
        tableModel.addRow(new Object[]{3, ".java", "CurrentWork.java", "C:/Projects", "MODIFIED", timeStr});
    }

    // ========================= UTILITY METHODS =========================

    /**
     * Checks if no results and shows appropriate message.
     */
    private void checkNoResults(String criteria) {
        if (tableModel.getRowCount() == 0) {
            showMessage("No files found matching " + criteria, "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows an information/warning message.
     */
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(queryDialog, message, title, messageType);
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(queryDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Finalizes the dialog setup and shows it.
     */
    private void finalizeDialog() {
        queryDialog.pack();
        queryDialog.setSize(Math.max(1000, watcherView.mainFrame.getSize().width),
                Math.max(700, watcherView.mainFrame.getSize().height * 2/3));
        queryDialog.setMinimumSize(new Dimension(1000, 700));
        queryDialog.setLocationRelativeTo(watcherView.mainFrame);
        queryDialog.setVisible(true);
    }
}