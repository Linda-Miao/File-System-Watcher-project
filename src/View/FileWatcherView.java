/* 360 file watcher project */
package View;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.beans.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.nio.file.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * FileWatcherView is the main GUI component for the File Watcher application.
 * @author Linda Miao
 * @version 1.0
 */
public final class FileWatcherView implements PropertyChangeListener {

    // -------Constants ------
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int JFRAME_WIDTH = WINDOW_DIMENSION.width / 2;
    public static final int JFRAME_HEIGHT = WINDOW_DIMENSION.height / 3;
    public static final Dimension GUI_DIMENSION = new Dimension(JFRAME_WIDTH, JFRAME_HEIGHT);
    public static final String ICON_PATH = "./Assets/icon.png";
    public static final String WINDOW_TITLE = "File Watcher";


    // Comprehensive extension list with all common file types
    private static final String[] COMMON_EXTENSIONS = {
            // Empty option for ALL files
            "",

            // Document files
            ".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt", ".pages",

            // Spreadsheet files
            ".xlsx", ".xls", ".csv", ".ods", ".numbers",

            // Presentation files
            ".pptx", ".ppt", ".odp", ".key",

            // Image files
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif",
            ".svg", ".webp", ".ico", ".raw", ".heic", ".heif",

            // Video files
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm",
            ".m4v", ".3gp", ".mpg", ".mpeg",

            // Audio files
            ".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a",
            ".opus", ".aiff",

            // Programming/Code files
            ".java", ".py", ".js", ".html", ".css", ".php", ".cpp",
            ".c", ".h", ".cs", ".swift", ".kt", ".go", ".rs", ".rb",
            ".scala", ".r", ".sql", ".sh", ".bat", ".ps1",

            // Web files
            ".json", ".xml", ".yaml", ".yml", ".md", ".scss", ".sass",
            ".less", ".ts", ".jsx", ".tsx", ".vue",".java",".js","html",

            // Archive files
            ".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz", ".dmg",
            ".pkg", ".deb", ".rpm",

            // Executable files
            ".exe", ".msi", ".app", ".deb", ".rpm", ".dmg", ".pkg",

            // Database files
            ".db", ".sqlite", ".mdb", ".accdb",

            // Design files
            ".psd", ".ai", ".sketch", ".fig", ".xd", ".indd",

            // 3D/CAD files
            ".dwg", ".dxf", ".obj", ".fbx", ".blend", ".max", ".maya",

            // Font files
            ".ttf", ".otf", ".woff", ".woff2", ".eot",

            // eBook files
            ".epub", ".mobi", ".azw", ".azw3",

            // Configuration files
            ".ini", ".cfg", ".conf", ".properties", ".toml",

            // Log files
            ".log", ".out", ".err",

            // Backup files
            ".bak", ".backup", ".old", ".tmp",

            // Virtual machine files
            ".vmdk", ".vdi", ".qcow2", ".ova", ".ovf",

            // Game files
            ".save", ".dat", ".bin", ".rom",

            // Misc files
            ".iso", ".img", ".toast", ".vcd"
    };

    // ------Gui Components ------
    protected final JFrame mainFrame;
    private final JComboBox<String> extensionSelector;
    private final JTextField directorySelector;
    private final JMenuBar menuBar;
    private final JButton submitButton;
    private final JButton startButton;
    private final JButton stopButton;
    private final JMenuItem startMenuItem;
    private final JMenuItem stopMenuItem;
    private final JMenuItem queryMenuItem;
    private final JMenuItem closeMenuItem;
    private final JMenu helpMenu;
    private final DefaultTableModel tableModel;
    private final JTable table;

    // ------File watching components--------
    private WatchService watchService;
    private ExecutorService executorService;
    private boolean isWatching = false;
    private String currentWatchPath = "";
    private String currentExtension = "";
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final List<Object[]> allTableData = new ArrayList<>();

    // ------ Constructor -------
    public FileWatcherView() {
        // Initialize buttons
        submitButton = new JButton("Submit");
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");

        // Initialize main frame
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setLocationRelativeTo(null);
        try {
            mainFrame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }
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
    private void addSampleData() {
        Object[][] sampleData = {
                {1, ".txt", "notes.txt", "/documents/work", "Created", "2023-10-15 10:15:00"},
                {2, ".java", "Main.java", "/projects/code", "Modified", "2023-10-15 12:30:00"},
                {3, ".html", "index.html", "/websites/home", "Deleted", "2023-10-14 09:00:00"},
                {4, ".css", "styles.css", "/websites/design", "Created", "2023-10-14 11:00:00"},
                {5, ".js", "script.js", "/websites/scripts", "Accessed", "2023-10-13 14:00:00"},
                {6, ".pdf", "paper.pdf", "/documents/research", "Archived", "2023-10-12 18:45:00"},
                {7, ".txt", "report.txt", "/reports", "Modified", "2023-10-16 08:30:00"},
                {8, ".java", "Utils.java", "/projects/utils", "Created", "2023-10-16 14:20:00"},
                {9, ".docx", "proposal.docx", "/documents/work", "Created", "2023-10-17 09:15:00"},
                {10, ".pdf", "manual.pdf", "/documents/research", "Accessed", "2023-10-17 11:30:00"}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
            allTableData.add(row.clone());
        }

        SwingUtilities.invokeLater(this::autoResizeMainWindow);
    }

    private void configureTable() {
        setupOptimalTableConfiguration();
    }

    private void setupOptimalTableConfiguration() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(350);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);

        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(1).setMinWidth(60);
        table.getColumnModel().getColumn(2).setMinWidth(120);
        table.getColumnModel().getColumn(3).setMinWidth(200);
        table.getColumnModel().getColumn(4).setMinWidth(80);
        table.getColumnModel().getColumn(5).setMinWidth(130);

        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(150);
        table.getColumnModel().getColumn(4).setMaxWidth(150);
        table.getColumnModel().getColumn(5).setMaxWidth(200);

        System.out.println("Main table configured with optimal sizing and horizontal scrolling");
    }

    public void initComponents() {
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setJMenuBar(menuBar);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        mainFrame.add(formPanel, BorderLayout.NORTH);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        setupMenuBar();
        setupActionListeners();
        setupAutoResizeFeatures();

        autoResizeMainWindow();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void setupAutoResizeFeatures() {
        tableModel.addTableModelListener(e -> {
            SwingUtilities.invokeLater(this::autoResizeMainWindow);
        });

        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    table.revalidate();
                    table.repaint();
                });
            }
        });

        System.out.println("Auto-resize features enabled for main window");
    }

    private void autoResizeMainWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int optimalWidth = calculateOptimalWidth();
        int optimalHeight = calculateOptimalHeight();

        int maxWidth = (int) (screenSize.width * 0.9);
        int maxHeight = (int) (screenSize.height * 0.85);

        int finalWidth = Math.min(optimalWidth, maxWidth);
        int finalHeight = Math.min(optimalHeight, maxHeight);

        finalWidth = Math.max(finalWidth, 800);
        finalHeight = Math.max(finalHeight, 600);

        mainFrame.setSize(finalWidth, finalHeight);
        mainFrame.setLocationRelativeTo(null);

        System.out.println(" Main window auto-resized to: " + finalWidth + "x" + finalHeight);
    }

    private int calculateOptimalWidth() {
        if (table == null) return 1000;

        int totalColumnWidth = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            totalColumnWidth += table.getColumnModel().getColumn(i).getPreferredWidth();
        }

        return totalColumnWidth + 100;
    }

    private int calculateOptimalHeight() {
        int formHeight = 120;
        int rowCount = Math.max(tableModel.getRowCount(), 8);
        int rowHeight = 25;
        int tableHeight = (rowCount * rowHeight) + 60;
        tableHeight = Math.min(tableHeight, 600);

        return formHeight + tableHeight + 80;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(new JLabel("PATH:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(directorySelector, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Extension: (empty = ALL files)"), gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        formPanel.add(extensionSelector, gbc);

        gbc.gridx = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(stopButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("File Watcher View:"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    // -------Event listener setup ------
    private void setupActionListeners() {
        startButton.addActionListener(e -> handleStartAction());
        stopButton.addActionListener(e -> handleStopAction());
        submitButton.addActionListener(e -> handleSubmitAction());
        extensionSelector.addActionListener(e -> handleExtensionSelection());

        directorySelector.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handlePathChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { handlePathChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { handlePathChange(); }
        });

        startMenuItem.addActionListener(e -> handleStartAction());
        stopMenuItem.addActionListener(e -> handleStopAction());
        queryMenuItem.addActionListener(e -> handleQueryAction());
        closeMenuItem.addActionListener(e -> System.exit(0));
    }

    private void handleExtensionSelection() {
        String selectedExtension = (String) extensionSelector.getSelectedItem();

        System.out.println("Extension selected: " + selectedExtension);

        submitButton.setEnabled(true);
        submitButton.setBackground(null);
        submitButton.setToolTipText("Click Submit to filter database by " +
                (selectedExtension != null && selectedExtension.isEmpty() ? "ALL files" : selectedExtension));
    }

    private void handlePathChange() {
        String currentPath = directorySelector.getText().trim();

        submitButton.setEnabled(true);
        submitButton.setBackground(null);
        submitButton.setToolTipText("Path changed - click Submit to filter database");

        System.out.println("Path changed to: " + currentPath + " - Submit available");
    }

    private void handleSubmitAction() {
        String path = directorySelector.getText().trim();
        String extension = (String) extensionSelector.getSelectedItem();

        support.firePropertyChange("submitPath", null, path);

        System.out.println("SUBMIT clicked - Filtering database by Extension: '" + extension + "', Path: '" + path + "'");

        if (extension != null && !extension.trim().isEmpty()) {
            filterExistingDatabase(extension, path);
        } else {
            showAllDatabaseData();
        }

        submitButton.setEnabled(true);
        submitButton.setBackground(null);
        submitButton.setToolTipText("Database filtered - change criteria and submit again");

        int resultCount = tableModel.getRowCount();
        int totalCount = allTableData.size();

        String message = "Database Search Complete!\n\n" +
                "Search Criteria:\n" +
                "• Extension: " + (extension == null || extension.isEmpty() ? "ALL files" : extension) + "\n" +
                "• Path: " + (path.isEmpty() || path.equals("C:\\") ? "ALL paths" : path) + "\n\n" +
                "Results: Found " + resultCount + " of " + totalCount + " files in database\n\n" +
                "Use START/STOP to monitor new files from your computer.";

        JOptionPane.showMessageDialog(mainFrame, message, "Database Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterExistingDatabase(String extensionFilter, String pathFilter) {
        tableModel.setRowCount(0);

        int rowNumber = 1;
        int matchCount = 0;

        for (Object[] row : allTableData) {
            String fileExtension = row[1] != null ? row[1].toString() : "";
            String filePath = row[3] != null ? row[3].toString() : "";

            boolean extensionMatch = fileExtension.equalsIgnoreCase(extensionFilter);

            boolean pathMatch = true;
            if (pathFilter != null && !pathFilter.trim().isEmpty() && !pathFilter.equals("C:\\")) {
                pathMatch = filePath.toLowerCase().contains(pathFilter.toLowerCase());
            }

            if (extensionMatch && pathMatch) {
                Object[] filteredRow = row.clone();
                filteredRow[0] = rowNumber++;
                tableModel.addRow(filteredRow);
                matchCount++;
            }
        }

        SwingUtilities.invokeLater(this::autoResizeMainWindow);

        System.out.println("Database filtered - Extension: '" + extensionFilter +
                "' - Found: " + matchCount + " files");

        if (matchCount == 0) {
            JOptionPane.showMessageDialog(mainFrame,
                    "No files found in database matching:\nExtension: " + extensionFilter +
                            (pathFilter != null && !pathFilter.equals("C:\\") ? "\nPath: " + pathFilter : ""),
                    "No Matches Found",
                    JOptionPane.INFORMATION_MESSAGE);

            showAllDatabaseData();
        }
    }

    private void showAllDatabaseData() {
        tableModel.setRowCount(0);

        int rowNumber = 1;
        for (Object[] row : allTableData) {
            Object[] displayRow = row.clone();
            displayRow[0] = rowNumber++;
            tableModel.addRow(displayRow);
        }

        SwingUtilities.invokeLater(this::autoResizeMainWindow);
        System.out.println("Showing all " + allTableData.size() + " files from database");
    }

    private void handleStartAction() {
        System.out.println("Started");
        currentWatchPath = directorySelector.getText().trim();
        currentExtension = (String) extensionSelector.getSelectedItem();

        if (currentWatchPath.isEmpty() || currentWatchPath.equals("C:\\")) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please enter a valid path first",
                    "No Path Set",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        support.firePropertyChange("startWatching", null, currentWatchPath);
        startFileWatching();
        updateButtonStates(false, true);
    }

    private void handleStopAction() {
        System.out.println("Stopped");
        support.firePropertyChange("stopWatching", null, null);
        stopFileWatching();
        updateButtonStates(true, false);
    }

    private void handleQueryAction() {
        System.out.println("Query Database(file extension)");
        support.firePropertyChange("queryDatabase", null, extensionSelector.getSelectedItem());
        showQueryDialog();
    }

    private void updateButtonStates(boolean startEnabled, boolean stopEnabled) {
        startButton.setEnabled(startEnabled);
        stopButton.setEnabled(stopEnabled);
        startMenuItem.setEnabled(startEnabled);
        stopMenuItem.setEnabled(stopEnabled);
    }

    // ------- File watching methods --------
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
                            "\nExtension: " + (currentExtension != null && currentExtension.isEmpty() ? "ALL files" : currentExtension),
                    "Watching Started",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error starting file watcher: " + e.getMessage(),
                    "Watch Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processFileEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();
        Path fileName = (Path) event.context();
        String fileNameStr = fileName.toString();

        if (currentExtension != null && !currentExtension.isEmpty()) {
            if (!fileNameStr.toLowerCase().endsWith(currentExtension.toLowerCase())) {
                return;
            }
        }

        SwingUtilities.invokeLater(() -> addFileEventToTable(fileNameStr, kind.name(), currentWatchPath));
    }

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

    private void addFileEventToTable(String fileName, String eventType, String path) {
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot);
        }

        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int rowNumber = tableModel.getRowCount() + 1;

        Object[] newRow = {
                rowNumber,
                extension,
                fileName,
                path,
                eventType,
                currentTime
        };

        tableModel.addRow(newRow);
        allTableData.add(newRow.clone());

        SwingUtilities.invokeLater(this::autoResizeMainWindow);

        table.scrollRectToVisible(table.getCellRect(tableModel.getRowCount() - 1, 0, true));
        System.out.println("File event added: " + fileName + " - " + eventType);
    }

    // --------- Menu setup methods -------
    private void setupMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(startMenuItem);
        fileMenu.add(stopMenuItem);
        fileMenu.add(queryMenuItem);
        fileMenu.add(closeMenuItem);

        JMenu editMenu = new JMenu("Edit");
        setupEditMenu(editMenu);

        setupHelpMenu();

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
    }

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

        exportMenuItem.addActionListener(e -> exportResults());
        sortMenuItem.addActionListener(e -> sortEvents());
        clearMenuItem.addActionListener(e -> clearAllEvents());
        bookmarkMenuItem.addActionListener(e -> bookmarkEvent());
    }

    private void setupHelpMenu() {
        JMenuItem contactUs = new JMenuItem("Contact Us");
        helpMenu.add(contactUs);

        contactUs.addActionListener(e -> showContactDialog());
    }

    private void showContactDialog() {
        JDialog dialog = new JDialog(mainFrame, "Contact Us", false);
        dialog.setLayout(new GridLayout(1, 2));

        String content = "<html>" +
                "<div style='text-align: center;'>" +
                "<br><br><br><br>" +
                "Manager: John Smith<br>" +
                "Email: help@example.com<br>" +
                "Phone: +1-123-456-7890" +
                "</div>" +
                "</html>";

        JLabel label = new JLabel(content, SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        try {
            Image image = new ImageIcon(ICON_PATH).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel icon = new JLabel(new ImageIcon(image));
            dialog.add(icon);
        } catch (Exception e) {
            dialog.add(new JLabel("Icon not available"));
        }

        dialog.add(label);
        dialog.setSize(mainFrame.getSize().width / 2, mainFrame.getSize().height / 2);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    // ----------Export and utility methods
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

        JButton exportCSVButton = new JButton("Export to CSV");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        exportDialog.add(exportCSVButton, gbc);
        gbc.gridx = 1;
        exportDialog.add(cancelButton, gbc);

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

    private File createCSVExport() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File csvFile = new File("FileWatcher_Export_" + timestamp + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Row,Extension,Filename,Path,Event,Date/Time");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object value = tableModel.getValueAt(i, j);
                    String cellValue = value != null ? value.toString() : "";

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

    private void sortEvents() {
        JOptionPane.showMessageDialog(mainFrame,
                "Sort functionality will be implemented here",
                "Sort Events",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearAllEvents() {
        int result = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to clear all events?\nThis action cannot be undone.",
                "Confirm Clear All Events",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            allTableData.clear();

            extensionSelector.setSelectedIndex(0);
            directorySelector.setText("C:\\");

            submitButton.setEnabled(true);
            submitButton.setBackground(null);
            submitButton.setToolTipText("No data - add sample data or start monitoring");

            SwingUtilities.invokeLater(this::autoResizeMainWindow);
            JOptionPane.showMessageDialog(mainFrame,
                    "All events have been cleared.\nUse File menu to reload sample data or start monitoring new files.",
                    "Events Cleared",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void bookmarkEvent() {
        JOptionPane.showMessageDialog(mainFrame,
                "Bookmark functionality will be implemented here",
                "Bookmark Event",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showQueryDialog() {
        try {
            new QueryView(this);
            System.out.println("🔍 Advanced search (QueryView) opened successfully");
        } catch (Exception e) {
            System.err.println("Error opening QueryView: " + e.getMessage());
            JOptionPane.showMessageDialog(mainFrame,
                    "Advanced search functionality requires QueryView class.\n" +
                            "Please ensure QueryView.java is in your project.",
                    "QueryView Not Available",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------MVC communication methods ------------
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    //------Public API methods -----
    public String getSelectedExtension() {
        return (String) extensionSelector.getSelectedItem();
    }

    public String getSelectedPath() {
        return directorySelector.getText().trim();
    }

    public void addEventToTable(String fileName, String extension, String path, String eventType, String timestamp) {
        SwingUtilities.invokeLater(() -> {
            int rowNumber = tableModel.getRowCount() + 1;
            Object[] newRow = {
                    rowNumber,
                    extension,
                    fileName,
                    path,
                    eventType,
                    timestamp
            };

            tableModel.addRow(newRow);
            allTableData.add(newRow.clone());

            autoResizeMainWindow();
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
            allTableData.clear();

            submitButton.setEnabled(true);
            submitButton.setBackground(null);
            submitButton.setToolTipText("Click to apply current settings");

            autoResizeMainWindow();
        });
    }
}





