/* 360 file watcher project */
package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.beans.*;
import java.awt.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
 * @author Linda Miao
 * @version 1.0
 */
public final class FileWatcherView implements PropertyChangeListener {

    // -------Constants ------
    /** The dimension object used to get width and height of the screen. */
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    /** The width of the JFrame. */
    public static final int JFRAME_WIDTH = WINDOW_DIMENSION.width / 2;

    /** The height of the JFrame. */
    public static final int JFRAME_HEIGHT = WINDOW_DIMENSION.height / 3;

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
    protected final JFrame mainFrame;

    /** Dropdown selector for file extensions. */
    private final JComboBox<String> extensionSelector;

    /** Text field for directory path input. */
    private final JTextField directorySelector;

    /** Main menu bar for the application. */
    private final JMenuBar menuBar;

    /** Button to submit/apply current settings. */
    private final JButton submitButton;

    /** Button to start file watching. */
    private final JButton startButton;

    /** Button to stop file watching. */
    private final JButton stopButton;

    /** Menu item for starting file watching. */
    private final JMenuItem startMenuItem;

    /** Menu item for stopping file watching. */
    private final JMenuItem stopMenuItem;

    /** Menu item for querying database. */
    private final JMenuItem queryMenuItem;

    /** Menu item for closing the application. */
    private final JMenuItem closeMenuItem;

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

        // Initialize main frame
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize form components
        extensionSelector = new JComboBox<>(COMMON_EXTENSIONS);
        directorySelector = new JTextField();

        // Set default directory path
        try {
            directorySelector.getDocument().insertString(0, "C:\\", null);
        } catch (BadLocationException e) {
            System.err.println("Error setting default directory: " + e.getMessage());
        }

        // Initialize menu components
        menuBar = new JMenuBar();
        helpMenu = new JMenu("Help");
        startMenuItem = new JMenuItem("Start");
        stopMenuItem = new JMenuItem("Stop");
        queryMenuItem = new JMenuItem("Query Database(file extension)");
        closeMenuItem = new JMenuItem("Close");

        // Initialize table model with headers
        tableModel = new DefaultTableModel(
                new Object[]{"Row", "Extension", "Filename", "PATH", "Event", "Date/Time"}, 0
        );

        // Add sample data for demonstration
        addSampleData();

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
     * Adds sample data to the table model for demonstration purposes.
     * This method populates the table with example file events to show
     * the expected format and layout.
     */
    private void addSampleData() {
        tableModel.addRow(new Object[]{1, ".txt", "notes.txt", "/documents/work", "Created", "2023-10-15 10:15:00"});
        tableModel.addRow(new Object[]{2, ".java", "Main.java", "/projects/code", "Modified", "2023-10-15 12:30:00"});
        tableModel.addRow(new Object[]{3, ".html", "index.html", "/websites/home", "Deleted", "2023-10-14 09:00:00"});
        tableModel.addRow(new Object[]{4, ".css", "styles.css", "/websites/design", "Created", "2023-10-14 11:00:00"});
        tableModel.addRow(new Object[]{5, ".js", "script.js", "/websites/scripts", "Accessed", "2023-10-13 14:00:00"});
        tableModel.addRow(new Object[]{6, ".pdf", "paper.pdf", "/documents/research", "Archived", "2023-10-12 18:45:00"});
    }

    /**
     * Configures the table appearance and column properties.
     * Sets up column widths, resize behavior, and scroll pane settings.
     */
    private void configureTable() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // Row
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Extension
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Filename
        table.getColumnModel().getColumn(3).setPreferredWidth(400);  // PATH
        table.getColumnModel().getColumn(4).setPreferredWidth(200);  // Event
        table.getColumnModel().getColumn(5).setPreferredWidth(250);  // Date/Time
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
        formPanel.add(new JLabel("PATH:"), gbc);

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

    /**
     * Handles the start action for both button and menu item.
     * Validates input and begins file watching if conditions are met.
     */
    private void handleStartAction() {
        System.out.println("Started");
        currentWatchPath = directorySelector.getText().trim();
        currentExtension = (String) extensionSelector.getSelectedItem();

        if (currentWatchPath.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please enter a path first and click Submit",
                    "No Path Set",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        support.firePropertyChange("startWatching", null, currentWatchPath);
        startFileWatching();
        updateButtonStates(false, true);
    }

    /**
     * Handles the stop action for both button and menu item.
     * Stops file watching and updates UI states.
     */
    private void handleStopAction() {
        System.out.println("Stopped");
        support.firePropertyChange("stopWatching", null, null);
        stopFileWatching();
        updateButtonStates(true, false);
    }

    /**
     * Handles the submit action for applying current settings.
     * Validates and displays current configuration.
     */
    private void handleSubmitAction() {
        String path = directorySelector.getText().trim();
        String extension = (String) extensionSelector.getSelectedItem();
        support.firePropertyChange("submitPath", null, path);

        System.out.println("Configured: Path=" + path + ", Extension=" + extension);
        startButton.setEnabled(true);

        JOptionPane.showMessageDialog(mainFrame,
                "Settings applied! Click 'Start' to begin watching:\nPath: " + path +
                        "\nExtension: " + (extension.isEmpty() ? "ALL files" : extension),
                "Ready to Watch",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles the query database action.
     * Opens the query dialog for database operations.
     */
    private void handleQueryAction() {
        System.out.println("Query Database(file extension)");
        support.firePropertyChange("queryDatabase", null, extensionSelector.getSelectedItem());
        showQueryDialog();
    }

    /**
     * Updates the enabled state of start/stop buttons and menu items.
     *
     * @param startEnabled whether start controls should be enabled
     * @param stopEnabled whether stop controls should be enabled
     */
    private void updateButtonStates(boolean startEnabled, boolean stopEnabled) {
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
                        System.err.println("Error in file watching: " + e.getMessage());
                    }
                }
            });

            JOptionPane.showMessageDialog(mainFrame,
                    "File watching started!\nMonitoring: " + currentWatchPath +
                            "\nExtension: " + (currentExtension.isEmpty() ? "ALL files" : currentExtension),
                    "Watching Started",
                    JOptionPane.INFORMATION_MESSAGE);

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
    private void processFileEvent(WatchEvent<?> event) {
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
            JOptionPane.showMessageDialog(mainFrame,
                    "File watching stopped.",
                    "Watching Stopped",
                    JOptionPane.INFORMATION_MESSAGE);
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
    private void addFileEventToTable(String fileName, String eventType, String path) {
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
        System.out.println("File event added: " + fileName + " - " + eventType);
    }

    // --------- Menu setup methods

    /**
     * Sets up the complete menu bar with File, Edit, and Help menus.
     * Configures all menu items and their respective action listeners.
     */
    private void setupMenuBar() {
        // Create File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(startMenuItem);
        fileMenu.add(stopMenuItem);
        fileMenu.add(queryMenuItem);
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
    private void setupEditMenu(JMenu editMenu) {
        JMenuItem exportMenuItem = new JMenuItem("Export Results...");
        JMenuItem sortMenuItem = new JMenuItem("Sort Events");
        JMenuItem clearMenuItem = new JMenuItem("Clear All Events");
        JMenuItem bookmarkMenuItem = new JMenuItem("Bookmark Event");

        editMenu.add(exportMenuItem);
        editMenu.addSeparator();
        editMenu.add(sortMenuItem);
        editMenu.add(clearMenuItem);
        editMenu.addSeparator();
        editMenu.add(bookmarkMenuItem);

        // Setup action listeners for edit menu items
        exportMenuItem.addActionListener(e -> exportResults());
        sortMenuItem.addActionListener(e -> sortEvents());
        clearMenuItem.addActionListener(e -> clearAllEvents());
        bookmarkMenuItem.addActionListener(e -> bookmarkEvent());
    }

    /**
     * Sets up the Help menu with contact information dialog.
     */
    private void setupHelpMenu() {
        JMenuItem contactUs = new JMenuItem("Contact Us");
        helpMenu.add(contactUs);

        contactUs.addActionListener(e -> showContactDialog());
    }

    /**
     * Shows the contact information dialog.
     */
    private void showContactDialog() {
        JDialog dialog = new JDialog(mainFrame, "Contact Us", false);
        dialog.setLayout(new GridLayout(1, 2));

        String content = """
                <html>
                <div style='text-align: center;'>
                    <br><br><br><br>
                    Manager: John Smith<br>
                    Email: help@example.com<br>
                    Phone: +1-123-456-7890
                </div>
                </html>
                """;

        JLabel label = new JLabel(content, SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        Image image = new ImageIcon(ICON_PATH).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel icon = new JLabel(new ImageIcon(image));

        dialog.add(icon);
        dialog.add(label);
        dialog.setSize(mainFrame.getSize().width / 2, mainFrame.getSize().height / 2);
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

    /**
     * Creates a CSV file export of the current table data.
     *
     * @return File object representing the created CSV file
     * @throws IOException if file creation fails
     */
    private File createCSVExport() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File csvFile = new File("FileWatcher_Export_" + timestamp + ".csv");

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
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        switch (name) {
            case "fileEventAdded":
                System.out.println("fileEventAdded");
                break;
            case "fileEventsCleared":
                System.out.println("fileEventsCleared");
                break;
            default:
                System.out.println("Unknown property change: " + name);
                break;
        }
    }

    /**
     * Adds a PropertyChangeListener to this component.
     *
     * @param listener the PropertyChangeListener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from this component.
     *
     * @param listener the PropertyChangeListener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
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
       // return directSelector.getText().trim();
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
    public void addEventToTable(String fileName, String extension, String path, String eventType, String timestamp) {
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

    public void displayMessage(String message, String title) {
        JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
        });
    }
}