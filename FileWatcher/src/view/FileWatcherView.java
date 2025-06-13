
package view;

/* TCSS 360 File watcher project */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyEvent;
import java.beans.*;
import java.awt.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.DatabaseManager; // Added for database access
import model.FileEvent; // Added for FileEvent creation
import model.EventType; // Added for EventType parsing

/**
 * FileWatcherView is the main GUI component for the File Watcher application.
 * It provides a user interface for monitoring file system changes in specified directories
 * with optional file extension filtering. The view implements the MVC pattern by using
 * PropertyChangeListener for communication with other components.
 * Key features:
 * - Directory path selection and monitoring
 * - File extension filtering
 * - Real-time file event display in a table
 * - Export functionality for file events
 * - Menu-driven interface with start/stop controls
 *
 * @author Mutahar Wafayee
 * @author Linda Miao
 * @version June 12, 2025
 */
public class FileWatcherView implements PropertyChangeListener {

    // -------Constants ------
    /** The dimension object used to get width and height of the screen. */
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    /** The width of the JFrame. */
    public static final int JFRAME_WIDTH = 720;

    /** The height of the JFrame. */
    public static final int JFRAME_HEIGHT = 500;

    /** The dimension of the GUI. */
    public static final Dimension GUI_DIMENSION = new Dimension(JFRAME_WIDTH, JFRAME_HEIGHT);

    /** Path to the UW icon for the JFrame. */
    public static final String ICON_PATH = "./Assets/icon.png";

    /** Title of the application. */
    public static final String WINDOW_TITLE = "File Watcher";

    /** Common file extensions available in the dropdown selector. */
    private static final String[] COMMON_EXTENSIONS = {
            "", ".txt", ".pdf", ".docx", ".xlsx", ".csv", ".py", ".java", ".html", ".css", ".js"
    };

    // ------Gui Components ------
    /** Main application frame. */
    public final JFrame mainFrame;

    /** Dropdown selector for file extensions. */
    public final JComboBox<String> extensionSelector;

    /** Text field for directory path input. */
    private final JTextField directorySelector;

    /** Main menu bar for the application. */
    private final JMenuBar menuBar;

    /** Button to submit/apply current settings. */
    public final JButton submitButton;

    /** Button to start file watching. */
    public final JButton startButton;

    /** Button to stop file watching. */
    public final JButton stopButton;

    /** Email button. */
    public final JButton emailButton;

    /** Menu item for starting file watching. */
    public final JMenuItem startMenuItem;

    /** Menu item for stopping file watching. */
    private final JMenuItem stopMenuItem;

    /** Menu item for querying database. */
    public final JMenuItem queryMenuItem;

    /** Menu item for closing the application. */
    private final JMenuItem closeMenuItem;

    /* Menu item for save to DB. */
    private final JMenuItem saveToDbMenuItem;

    /* export csv menu item. */
    private final JMenuItem exportMenuItem;

    /** Help menu containing contact information. */
    private final JMenu helpMenu;

    /** Table model for displaying file events. */
    private final DefaultTableModel tableModel;

    /** Table component for displaying file events. */
    private final JTable table;

    // ------File watching components--------
    /** Service for watching file system changes. */
    private WatchService watchService;

    /** Executor service for running file watching in background. */
    private ExecutorService executorService;

    /** Flag indicating if file watching is currently active. */
    private boolean isWatching = false;

    /** Currently monitored directory path. */
    private String currentWatchPath = "";

    /** Currently selected file extension filter. */
    private String currentExtension = "";

    /** PropertyChangeSupport for MVC communication. */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    // ------ Constructor -------

    /**
     * Constructs a new FileWatcherView with all necessary GUI components initialized.
     * Sets up the main frame, buttons, menu items, and table for displaying file events.
     * Also initializes default values and component states.
     */
    public FileWatcherView() {
        // Initialize buttons
        submitButton = new JButton("Submit");
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        emailButton = new JButton("Email Report");

        // Initialize main frame
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize form components
        extensionSelector = new JComboBox<>(COMMON_EXTENSIONS);
        directorySelector = new JTextField();

        // Initialize menu components
        menuBar = new JMenuBar();
        helpMenu = new JMenu("Help");
        startMenuItem = new JMenuItem("Start");
        stopMenuItem = new JMenuItem("Stop");
        queryMenuItem = new JMenuItem("Query Database");
        closeMenuItem = new JMenuItem("Close");
        saveToDbMenuItem = new JMenuItem("Save to Database");
        exportMenuItem = new JMenuItem("Export Results...");

        // Initialize table model with headers
        tableModel = new DefaultTableModel(
                new Object[]{"Row", "Extension", "Filename", "Path", "Event", "Date/Time"}, 0
        );

        // Create table with model
        table = new JTable(tableModel);
        configureTable();

        // Set initial button states
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        // Initialize all components
        initComponents();
    }

    // ---------Initialization methods ------


    /**
     * Configures the table appearance and column properties.
     * Sets up column widths, resize behavior, and scroll pane settings.
     */
    private void configureTable() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // Row
        table.getColumnModel().getColumn(1).setPreferredWidth(70);  // Extension
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Filename
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // PATH
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Event
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Date/Time
    }

    /**
     * Initializes and configures all GUI components including layout, panels,
     * and event listeners. This method sets up the complete user interface.
     */
    public void initComponents() {
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setJMenuBar(menuBar);

        // Create and configure form panel
        JPanel formPanel = createFormPanel();

        // Create and configure table panel
        JPanel tablePanel = createTablePanel();

        // Add panels to main frame
        mainFrame.add(formPanel, BorderLayout.NORTH);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // Configure menus and event listeners
        setupMenuBar();
        setupActionListeners();

        // Finalize frame setup
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        mainFrame.revalidate();
        mainFrame.repaint();

    }

    /**
     * Creates and configures the form panel containing path input,
     * extension selector, and control buttons.
     *
     * @return JPanel configured form panel
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add PATH label and text field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(new JLabel("Path:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(directorySelector, gbc);

        // Add Extension label and selector
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Extension: (empty = ALL files)"), gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        formPanel.add(extensionSelector, gbc);

        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(stopButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(emailButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    /**
     * Creates and configures the table panel for displaying file events.
     *
     * @return JPanel configured table panel with scroll pane
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("File Watcher View:"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    // -------Event listener setup

    /**
     * Sets up all action listeners for buttons and menu items.
     * Configures event handling for start, stop, submit, and extension selection.
     */
    private void setupActionListeners() {
        // Start button action
        startButton.addActionListener(e -> handleStartAction());

        // Stop button action
        stopButton.addActionListener(e -> handleStopAction());

        // Submit button action
        submitButton.addActionListener(e -> handleSubmitAction());

        // Menu item actions
        startMenuItem.addActionListener(e -> handleStartAction());
        stopMenuItem.addActionListener(e -> handleStopAction());
        queryMenuItem.addActionListener(e -> handleQueryAction());
        closeMenuItem.addActionListener(e -> System.exit(0));
    }

    /* returns main frame. */
    public JFrame getMainFrame() {
        return mainFrame;
    }

    /* returns save to database menu item. */
    public JMenuItem getSaveToDbMenuItem() {
        return saveToDbMenuItem;
    }

    /* returns close menu item. */
    public JMenuItem getCloseMenuItem() {
        return closeMenuItem;
    }
    /**
     * Handles the start action for both button and menu item.
     * Validates input and begins file watching if conditions are met.
     */
    private void handleStartAction() {

        currentWatchPath = directorySelector.getText().trim();
        currentExtension = (String) extensionSelector.getSelectedItem();

        if (currentWatchPath.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please enter a path first and click Submit",
                    "No Path Set",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentExtension.isEmpty()) {
            startFileWatching();
        }


        support.firePropertyChange("startWatching", null, currentWatchPath);

        updateButtonStates(false, true);
    }

    /**
     * Handles the stop action for both button and menu item.
     * Stops file watching and updates UI states.
     */
    private void handleStopAction() {

        support.firePropertyChange("stopWatching", null, null);
        stopFileWatching();
        extensionSelector.setEnabled(true);
        submitButton.setEnabled(true);
        updateButtonStates(true, false);
    }

    /**
     * Handles the submit action for applying current settings.
     * Validates and displays current configuration.
     */
    private void handleSubmitAction() {
        startButton.setEnabled(true);
        submitButton.setEnabled(false);
        extensionSelector.setEnabled(false);

    }

    /**
     * Handles the query database action.
     * Opens the query dialog for database operations.
     */
    private void handleQueryAction() {

        support.firePropertyChange("queryDatabase", null, extensionSelector.getSelectedItem());

    }

    /**
     * Updates the enabled state of start/stop buttons and menu items.
     *
     * @param startEnabled whether start controls should be enabled
     * @param stopEnabled whether stop controls should be enabled
     */
    private void updateButtonStates(final boolean startEnabled, final boolean stopEnabled) {
        startButton.setEnabled(startEnabled);
        stopButton.setEnabled(stopEnabled);
        startMenuItem.setEnabled(startEnabled);
        stopMenuItem.setEnabled(stopEnabled);
    }

    // ------- File watching methods --------
    /**
     * Starts the file watching service for the specified directory.
     * Creates a background thread to monitor file system events and
     * updates the table with detected changes.
     */
    private void startFileWatching() {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path pathToWatch = Paths.get(currentWatchPath);
                pathToWatch.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                isWatching = true;
                executorService = Executors.newSingleThreadExecutor();

                executorService.submit(() -> {
                    while (isWatching) {
                        try {
                            WatchKey key = watchService.take();
                            for (WatchEvent<?> event : key.pollEvents()) {
                                processFileEvent(event);
                            }
                            key.reset();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }
                });

            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error starting file watcher: " + e.getMessage(),
                        "Watch Error",
                        JOptionPane.ERROR_MESSAGE);
            }

    }

    /**
     * Processes a single file system event and adds it to the table if it matches
     * the current extension filter.
     *
     * @param event the file system event to process
     */
    private void processFileEvent(final WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();
        Path fileName = (Path) event.context();
        String fileNameStr = fileName.toString();

        // Apply extension filter if specified
        if (currentExtension != null && !currentExtension.isEmpty()) {
            if (!fileNameStr.toLowerCase().endsWith(currentExtension.toLowerCase())) {
                return;
            }
        }

        SwingUtilities.invokeLater(() -> addFileEventToTable(fileNameStr, kind.name(), currentWatchPath));
    }

    /**
     * Stops the file watching service and cleans up resources.
     * Shuts down the executor service and closes the watch service.
     */
    private void stopFileWatching() {
        isWatching = false;
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }

        } catch (IOException e) {
            System.err.println("Error stopping file watcher: " + e.getMessage());
        }
    }

    /**
     * Adds a new file event to the table display.
     * Extracts file extension, formats timestamp, and updates the table model.
     *
     * @param fileName the name of the file that triggered the event
     * @param eventType the type of file system event (CREATE, MODIFY, DELETE)
     * @param path the directory path where the event occurred
     */
    private void addFileEventToTable(final String fileName, final String eventType, final String path) {
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot);
        }

        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int rowNumber = tableModel.getRowCount() + 1;

        tableModel.addRow(new Object[]{
                rowNumber,
                extension,
                fileName,
                path,
                eventType,
                currentTime
        });

        // Scroll to show the new event
        table.scrollRectToVisible(table.getCellRect(tableModel.getRowCount() - 1, 0, true));

    }

    // --------- Menu setup methods

    /**
     * Sets up the complete menu bar with File, Edit, and Help menus.
     * Configures all menu items and their respective action listeners.
     */
    private void setupMenuBar() {
        // Create File menu
        JMenu fileMenu = new JMenu("File");

        startMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + W shortcut
        fileMenu.add(startMenuItem);

        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + E shortcut
        fileMenu.add(stopMenuItem);

        queryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + Q shortcut
        fileMenu.add(queryMenuItem);

        saveToDbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + D shortcut
        saveToDbMenuItem.addActionListener(e -> saveTableToDatabase()); // Added action listener
        fileMenu.add(saveToDbMenuItem);

        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + O shortcut
        fileMenu.add(closeMenuItem);

        // Create Edit menu
        JMenu editMenu = new JMenu("Edit");
        setupEditMenu(editMenu);

        // Setup Help menu
        setupHelpMenu();

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
    }

    /**
     * Sets up the Edit menu with export, sort, clear, and bookmark options.
     *
     * @param editMenu the Edit menu to configure
     */
    private void setupEditMenu(final JMenu editMenu) {
        JMenuItem clearMenuItem = new JMenuItem("Clear All Events");

        exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + R shortcut
        editMenu.add(exportMenuItem);
        editMenu.addSeparator();

        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + Y shortcut
        editMenu.add(clearMenuItem);
        editMenu.addSeparator();

        // Setup action listeners for edit menu items
        exportMenuItem.addActionListener(e -> exportResults());
        clearMenuItem.addActionListener(e -> clearAllEvents());
    }

    /**
     * Sets up the Help menu with contact information dialog.
     */
    private void setupHelpMenu() {
        JMenuItem contactUs = new JMenuItem("Contact Us");
        contactUs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + H shortcut
        helpMenu.add(contactUs);
        contactUs.addActionListener(e -> showContactDialog());

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK)); // Ctrl + J shortcut
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.addActionListener(e -> {
            String message = """
            FileWatcher Application
            -----------------------------
            This program monitors a selected directory for file system events,
            such as file creation, modification, or deletion.
            
            It uses Java's WatchService and logs events to a local database.
            """;

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "About FileWatcher",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Shows the contact information dialog.
     */
    private void showContactDialog() {
        JDialog dialog = new JDialog(mainFrame, "Contact Us", false);
        JPanel panel = new JPanel(new GridBagLayout());

        String content = """
                <html>
                <div style='text-align: center;'>
                    
                    Manager: John Smith<br>
                    Email: help@example.com<br>
                    Phone: +1-123-456-7890               
                </div>
                </html>
                """;

        JLabel label = new JLabel(content, SwingConstants.CENTER);
        panel.add(label);


        dialog.add(panel);
        dialog.setSize(250, 100);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    // ----------Export and utility methods

    /**
     * Handles the export results functionality.
     * Shows export dialog and provides CSV export option.
     */
    private void exportResults() {
        JDialog exportDialog = new JDialog(mainFrame, "Export Results", true);
        exportDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        exportDialog.setSize(500, 200);
        exportDialog.setLocationRelativeTo(mainFrame);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel messageLabel = new JLabel("<html><center>CSV Export functionality is available.<br><br>" +
                "Would you like to export to CSV file?</center></html>");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        exportDialog.add(messageLabel, gbc);

        // Create export and cancel buttons
        JButton exportCSVButton = new JButton("Export to CSV");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        exportDialog.add(exportCSVButton, gbc);
        gbc.gridx = 1;
        exportDialog.add(cancelButton, gbc);

        // Setup button actions
        exportCSVButton.addActionListener(e -> {
            try {
                File csvFile = createCSVExport();
                JOptionPane.showMessageDialog(exportDialog,
                        "CSV file exported successfully!\nSaved as: " + csvFile.getName(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                exportDialog.dispose();
                exportMenuItem.setEnabled(false);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(exportDialog,
                        "Error creating CSV file: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }



        });

        cancelButton.addActionListener(e -> exportDialog.dispose());
        exportDialog.setVisible(true);

    }

    /* returns csv export button. */
    public JMenuItem getExportCSVMenuItem() {
        return exportMenuItem;
    }

    /**
     * Creates a CSV file export of the current table data.
     *
     * @return File object representing the created CSV file
     * @throws IOException if file creation fails
     */
    private File createCSVExport() throws IOException {
        File csvFile = new File("Report.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Write CSV header
            writer.println("Row,Extension,Filename,Path,Event,Date/Time");

            // Write table data
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object value = tableModel.getValueAt(i, j);
                    String cellValue = value != null ? value.toString() : "";

                    // Escape quotes and wrap in quotes if contains comma
                    if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                        cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                    }

                    row.append(cellValue);
                    if (j < tableModel.getColumnCount() - 1) {
                        row.append(",");
                    }
                }
                writer.println(row);
            }
        }

        return csvFile;
    }

    /**
     * Saves the current table data to the database, mirroring the CSV export functionality.
     * Iterates over table rows, creates FileEvent objects, and saves them to the database.
     */
    public void saveTableToDatabase() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        int successCount = 0;
        int failCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String extension = tableModel.getValueAt(i, 1).toString();
                String fileName = tableModel.getValueAt(i, 2).toString();
                String path = tableModel.getValueAt(i, 3).toString();
                String eventTypeStr = tableModel.getValueAt(i, 4).toString();
                String timestampStr = tableModel.getValueAt(i, 5).toString();

                EventType eventType = EventType.valueOf(eventTypeStr);
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                FileEvent event = new FileEvent(fileName, extension, path, eventType, timestamp);
                dbManager.saveToDatabase(event);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to save event at row " + (i + 1) + ": " + e.getMessage());
                failCount++;
            }
        }

        saveToDbMenuItem.setEnabled(false);
        JOptionPane.showMessageDialog(mainFrame,
                String.format("Database save complete: %d events saved, %d failed", successCount, failCount));
    }

    /**
     * Placeholder method for sort events functionality.
     * Shows information dialog indicating feature is not yet implemented.
     */
    private void sortEvents() {
        JOptionPane.showMessageDialog(mainFrame,
                "Sort functionality will be implemented here",
                "Sort Events",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears all events from the table after user confirmation.
     * Prompts user with confirmation dialog before clearing data.
     */
    private void clearAllEvents() {
        int result = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to clear all events?\nThis action cannot be undone.",
                "Confirm Clear All Events",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            JOptionPane.showMessageDialog(mainFrame,
                    "All events have been cleared.",
                    "Events Cleared",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Placeholder method for bookmark event functionality.
     * Shows information dialog indicating feature is not yet implemented.
     */
    private void bookmarkEvent() {
        JOptionPane.showMessageDialog(mainFrame,
                "Bookmark functionality will be implemented here",
                "Bookmark Event",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the query dialog for database operations.
     * Creates and displays a new QueryView instance.
     */
    public void showQueryDialog() {
        new QueryView(this);
    }

    // ----------MVC communication methods ------------
    /**
     * Handles property change events from other components in the MVC architecture.
     *
     * @param evt the property change event containing the change details
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        switch (name) {
            case "fileEventAdded":
                break;
            case "fileEventsCleared":
                break;
            default:
                break;
        }
    }

    /**
     * Adds a PropertyChangeListener to this component.
     *
     * @param listener the PropertyChangeListener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from this component.
     *
     * @param listener the PropertyChangeListener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    //------Public API methods -----

    /**
     * Gets the currently selected file extension from the dropdown.
     *
     * @return the selected extension string, or empty string if "all files" is selected
     */
    public String getSelectedExtension() {
        return (String) extensionSelector.getSelectedItem();
    }

    /**
     * Gets the currently entered directory path.
     *
     * @return the directory path string, trimmed of whitespace
     */
    public String getSelectedPath() {
        return directorySelector.getText().trim();
    }

    /**
     * Adds a file event to the table display from external sources.
     * Thread-safe method that can be called from any thread.
     *
     * @param fileName the name of the file
     * @param extension the file extension
     * @param path the directory path
     * @param eventType the type of event (CREATE, MODIFY, DELETE)
     * @param timestamp the timestamp of the event
     */
    public void addEventToTable(final String fileName, final String extension, final String path, final String eventType, final String timestamp) {
        SwingUtilities.invokeLater(() -> {
            int rowNumber = tableModel.getRowCount() + 1;
            tableModel.addRow(new Object[]{
                    rowNumber,
                    extension,
                    fileName,
                    path,
                    eventType,
                    timestamp
            });
            table.scrollRectToVisible(table.getCellRect(tableModel.getRowCount() - 1, 0, true));
        });
    }

    public void displayMessage(final String message, final String title) {
        JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayError(final String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void setVisible(final boolean visible) {
        mainFrame.setVisible(visible);
    }

    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
        });
    }
}