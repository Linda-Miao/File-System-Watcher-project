package View;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.awt.*;
import java.util.Arrays;
import java.nio.file.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FileWatcherView implements PropertyChangeListener{
    /** The dimension object used to get width and height of the screen.*/
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().
            getScreenSize();
    /**The width of the JFrame.*/
    public static final int JFRAME_WIDTH = WINDOW_DIMENSION.width / 2;

    /** The height of the JFrame.*/
    public static final int JFRAME_HEIGHT = WINDOW_DIMENSION.height / 3;

    /** The dimension of the GUI.*/
    public static final Dimension GUI_DIMENSION = new Dimension(JFRAME_WIDTH, JFRAME_HEIGHT);

    /** Path to the UW icon for the JFrame.*/
    public static final String ICON_PATH = "./Assets/icon.png";

    private final JList<String> fileEventList; //JList objects for displaying items.
    /** Title of the application.*/
    public static final String WINDOW_TITLE = "File Watcher";
    protected final JFrame mainFrame;
    private final JComboBox extensionSelector;
    private final JTextField directySelector;
    private final JMenuBar menuBar;
    /** Undo button of JButton. */
    private final JButton submitButton;
    private final JButton startButton;
    private final JButton stopButton;
    /** for JComboBox*/
    private final JComboBox<String>extensionDropdown; //for comboBox dropdown
    private final JTextField extensionTextField;
    private final String[] commonExtensions = {"", ".txt", ".pdf", ".docx", ".xlsx", ".csv", ".py", ".java", ".html", ".css", ".js"};
    private final JMenuItem startMenuItem;
    private final JMenuItem stopMenuItem;
    private final JMenuItem QueryMenuItem;
    private final JMenuItem closeMenuItem;
    private final JMenu helpMenu;
    private DefaultTableModel tableModel;  // Make it a class field
    private JTable table;
    private WatchService watchService;
    private ExecutorService executorService;
    private boolean isWatching = false;
    private String currentWatchPath = "";
    private String currentExtension = "";

    // PropertyChangeSupport for MVC communication
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /** The constructor.*/
    public FileWatcherView() {
        submitButton = new JButton("Submit");
        mainFrame = new JFrame(WINDOW_TITLE); //create a new JFrame Object(frame from field)
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setLocationRelativeTo(null);
        //Initializes fileEventList and sets its selection mode.
        fileEventList = new JList<>(new String[] {
                "Sample file that exceeds viewport width", "Another long event string",
                "File type: .txt", "Path: /this/is/a/long/path/to/a/file.txt"
        });//(information)test database for scrollbar work.

        fileEventList.setFixedCellWidth(JFRAME_WIDTH + 1000); // Exceeds the viewport width

        // Initialize JComboBox
        extensionSelector = new JComboBox<>(commonExtensions); // () may have file name late
        directySelector = new JTextField();
        try {
            directySelector.getDocument().insertString(0,"C:\\",null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        this.menuBar = new JMenuBar();
        startButton = new JButton("start"); // check the gray and work color late
        stopButton = new JButton("stop");  // check the gray and work color late

        // A menu-bar contains menus. A menu contains menu-items (or sub-Menu)
        JMenuBar menuBar;
        menuBar = new JMenuBar();
        mainFrame.setSize(400, 300);
        mainFrame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        // JMenuItem
        startMenuItem = new JMenuItem("Start");
        stopMenuItem = new JMenuItem("Stop");
        QueryMenuItem = new JMenuItem("Query Database(file extension)");
        closeMenuItem = new JMenuItem("Close");

        //JComboBox
        extensionDropdown = new JComboBox<>(commonExtensions);
        extensionTextField = new JTextField(10); // maximum number is 10 for dropdown.

        // Initial states
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        //menu bar
        helpMenu = new JMenu("Help");
        initComponents();
    }

    public void initComponents() {
        // Set layout for main frame
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setJMenuBar(this.menuBar);

        // Create form panel (top section)
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add PATH label and text field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel pathLabel = new JLabel("PATH:");
        formPanel.add(pathLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(directySelector, gbc);  // Only add one component here

        // Add Extension label and combBox
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel extensionLabel = new JLabel("Extension: (empty = ALL files)");
        formPanel.add(extensionLabel, gbc);  // Only add one component here

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        formPanel.add(extensionSelector, gbc);  // Only add one component here

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Space between buttons
        buttonPanel.add(stopButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Create table/list panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("File Watcher View:"));

        // Initialize the table model with column headers and no initial rows
        this.tableModel = new DefaultTableModel(new Object[]{"Row", "Extension", "Filename", "PATH", "Event", "Date/Time"}, 0);

        // Add test data to the table model
        tableModel.addRow(new Object[]{1, ".txt", "notes.txt", "/documents/work", "Created", "2023-10-15 10:15:00"});
        tableModel.addRow(new Object[]{2, ".java", "Main.java", "/projects/code", "Modified", "2023-10-15 12:30:00"});
        tableModel.addRow(new Object[]{3, ".html", "index.html", "/websites/home", "Deleted", "2023-10-14 09:00:00"});
        tableModel.addRow(new Object[]{4, ".css", "styles.css", "/websites/design", "Created", "2023-10-14 11:00:00"});
        tableModel.addRow(new Object[]{5, ".js", "script.js", "/websites/scripts", "Accessed", "2023-10-13 14:00:00"});
        tableModel.addRow(new Object[]{6, ".pdf", "paper.pdf", "/documents/research", "Archived", "2023-10-12 18:45:00"});

        // Create a JTable using the table model
        this.table = new JTable(tableModel);
        // Prevent the table from resizing columns to fit within the viewport
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Disable auto-resizing of columns

        // Set preferred column widths to exceed the viewport width
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // Row
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Extension
        table.getColumnModel().getColumn(2).setPreferredWidth(300); // Filename
        table.getColumnModel().getColumn(3).setPreferredWidth(400); // PATH
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Event
        table.getColumnModel().getColumn(5).setPreferredWidth(250); // Date/Time

        // Wrap the table in a JScrollPane to enable scrolling
        JScrollPane scrollPane = new JScrollPane(table);

        // Add JList with scroll pane
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main frame
        mainFrame.add(formPanel, BorderLayout.NORTH);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // Set frame properties
        fileJMenuItem();
        menuBarJMenuItem();
        editMenuJMenuItem();
        collectActionPerformed();

        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        mainFrame.revalidate();
        mainFrame.repaint();

        System.out.println("Frame contents: " + Arrays.toString(mainFrame.getContentPane().getComponents()));
        System.out.println("Table panel contents: " + Arrays.toString(tablePanel.getComponents()));
    }

    //Helper function for ActionListener(submit, stop, start)
    public void collectActionPerformed(){

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Started");

                // Get current settings
                currentWatchPath = directySelector.getText().trim();
                currentExtension = (String) extensionSelector.getSelectedItem();

                if (currentWatchPath.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Please enter a path first and click Submit",
                            "No Path Set",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Fire property change for controller
                support.firePropertyChange("startWatching", null, currentWatchPath);
                startFileWatching();
                // Update UI
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                startMenuItem.setEnabled(false);
                stopMenuItem.setEnabled(true);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Stopped");

                // FIRE PROPERTY CHANGE FOR CONTROLLER
                support.firePropertyChange("stopWatching", null, null);

                // Stop file watching
                stopFileWatching();

                // Update UI
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                startMenuItem.setEnabled(true);
                stopMenuItem.setEnabled(false);
            }
        });

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = directySelector.getText().trim();
                String extension = (String) extensionSelector.getSelectedItem();

                // Fire property change for controller
                support.firePropertyChange("submitPath", null, path);

                // Apply the settings and prepare for watching
                System.out.println("Configured: Path=" + path + ", Extension=" + extension);

                // Enable start button and show user it's ready
                startButton.setEnabled(true);
                JOptionPane.showMessageDialog(mainFrame,
                        "Settings applied! Click 'Start' to begin watching:\nPath: " + path +
                                "\nExtension: " + (extension.isEmpty() ? "ALL files" : extension),
                        "Ready to Watch",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Listener for selection change
        extensionDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedType = (String) extensionDropdown.getSelectedItem();
                System.out.println(selectedType);

                // Fire property change for extension selection
                support.firePropertyChange("extensionChanged", null, selectedType);
            }
        });
    }

    // helper function for implement start and stop
    private void startFileWatching() {
        try {
            // Create watch service
            watchService = FileSystems.getDefault().newWatchService();
            Path pathToWatch = Paths.get(currentWatchPath);

            // Register the path for file events
            pathToWatch.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            isWatching = true;

            // Start watching in a separate thread
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                while (isWatching) {
                    try {
                        WatchKey key = watchService.take(); // Wait for events

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            Path fileName = (Path) event.context();
                            String fileNameStr = fileName.toString();

                            // Filter by extension if specified
                            if (currentExtension != null && !currentExtension.isEmpty()) {
                                if (!fileNameStr.toLowerCase().endsWith(currentExtension.toLowerCase())) {
                                    continue; // Skip files that don't match extension
                                }
                            }

                            // Add event to table (on EDT thread)
                            SwingUtilities.invokeLater(() -> {
                                addFileEventToTable(fileNameStr, kind.name(), currentWatchPath);
                            });
                        }

                        key.reset(); // Reset the key to receive further events

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
        // Get file extension
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot);
        }

        // Get current time
        String currentTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Add new row to table
        int rowNumber = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{
                rowNumber,
                extension,
                fileName,
                path,
                eventType,
                currentTime
        });

        // Auto-scroll to the new row
        table.scrollRectToVisible(table.getCellRect(tableModel.getRowCount() - 1, 0, true));

        System.out.println("File event added: " + fileName + " - " + eventType);
    }

    //helper function for table model
    public DefaultTableModel getTableModel() {
        return this.tableModel;
    }

    //helper function for JMenuItem (Help-> item->contact Us)
    public void editMenuJMenuItem() {
        JMenuItem contactUs = new JMenuItem("Contact Us");
        helpMenu.add(contactUs);
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

        JLabel label = new JLabel(content, SwingConstants.CENTER); // Centers content horizontally
        label.setVerticalAlignment(SwingConstants.CENTER);         // Centers content vertically

        Image image = new ImageIcon(ICON_PATH).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel icon = new JLabel(new ImageIcon(image));

        dialog.add(icon);
        dialog.add(label);

        // Action listeners for submit button
        contactUs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setSize(mainFrame.getSize().width/ 2, mainFrame.getSize().height/ 2);
                dialog.setLocationRelativeTo(mainFrame);
                System.out.println("Contact Us"); //for test
                dialog.setVisible(true);
            }
        });
    }

    //helper function for menuBar
    public void menuBarJMenuItem(){
        // Create menu bar
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(startMenuItem);
        fileMenu.add(stopMenuItem);
        fileMenu.add(QueryMenuItem);
        fileMenu.add(closeMenuItem);

        JMenu editMenu = new JMenu("Edit");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        //Create Edit menu items:
        JMenuItem exportMenuItem = new JMenuItem("Export Results...");
        JMenuItem sortMenuItem = new JMenuItem("Sort Events");
        JMenuItem clearMenuItem = new JMenuItem("Clear All Events");
        JMenuItem bookmarkMenuItem = new JMenuItem("Bookmark Event");

        // Add menu items to Edit menu
        editMenu.add(exportMenuItem);
        editMenu.addSeparator(); // Add a separator line
        editMenu.add(sortMenuItem);
        editMenu.add(clearMenuItem);
        editMenu.addSeparator();
        editMenu.add(bookmarkMenuItem);

        // call EditMenuListeners ()
        EditMenuListeners(exportMenuItem, sortMenuItem, clearMenuItem, bookmarkMenuItem);
    }

    //Helper function for Edit listener
    private void EditMenuListeners(JMenuItem exportMenuItem, JMenuItem sortMenuItem,
                                   JMenuItem clearMenuItem, JMenuItem bookmarkMenuItem) {

        // Export Results action
        exportMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Export Results clicked");
                exportResults();
            }
        });

        // Sort Events action
        sortMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Sort Events clicked");
                sortEvents();
            }
        });

        // Clear All Events action
        clearMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Clear All Events clicked");
                clearAllEvents();
            }
        });

        // Bookmark Event action
        bookmarkMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Bookmark Event clicked");
                bookmarkEvent();
            }
        });
    }

    // PlaceHolder function for edit.
    private void exportResults() {
        // for export
        JOptionPane.showMessageDialog(mainFrame,
                "Export functionality will be implemented here",
                "Export Results",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void sortEvents() {
        // for sort
        JOptionPane.showMessageDialog(mainFrame,
                "Sort functionality will be implemented here",
                "Sort Events",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearAllEvents() {
        // for clear up
        JOptionPane.showMessageDialog(mainFrame,
                "Clear functionality will be implemented here",
                "Clear All Events",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void bookmarkEvent() {
        //  for bookmark
        JOptionPane.showMessageDialog(mainFrame,
                "Bookmark functionality will be implemented here",
                "Bookmark Event",
                JOptionPane.INFORMATION_MESSAGE);
    }

    //Helper function for file JMenuItem
    public void fileJMenuItem(){
        // property change list for file JMenuItemStart
        startMenuItem.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for start button
        startMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Started");

                // Fire property change for menu item
                support.firePropertyChange("startWatching", null, directySelector.getText().trim());
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                startMenuItem.setEnabled(false);
                stopMenuItem.setEnabled(true);
            }
        });

        // Property change list for file JMenuItemStart
        stopMenuItem.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for start button
        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Stopped");

                // Fire property change for menu item
                support.firePropertyChange("stopWatching", null, null);

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                startMenuItem.setEnabled(true);
                stopMenuItem.setEnabled(false);
            }
        });

        // property change list for file QueryMenuItem
        QueryMenuItem.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for QueryMenuItem
        QueryMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Query Database(file extension)");

                // Fire property change for query
                support.firePropertyChange("queryDatabase", null, extensionSelector.getSelectedItem());

                QueryMenuItem.setEnabled(false);// will see
                QueryMenuItem.setEnabled(true); // will see
                showQueryDialog();
            }
        });

        // Action listeners for QueryMenuItem
        closeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Close");
                closeMenuItem.setEnabled(false);// will see
                closeMenuItem.setEnabled(true); // will see
                System.exit(0); // click "close" then the window will close.
            }
        });
    }

    public void showQueryDialog(){
        QueryView queryView = new QueryView(this);
    }

    @java.lang.Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        switch (name){
            case "fileEventAdded":
                System.out.println("fileEventAdded");
                break;
            case "fileEventsCleared":
                System.out.println("fileEventsCleared");
                break;
        }
    }

    // ===PropertyChangeSupport for MVC Communication===
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // Helper methods for controller communication
    public String getSelectedExtension() {
        return (String) extensionSelector.getSelectedItem();
    }

    public String getSelectedPath() {
        return directySelector.getText().trim();
    }

    // Method for controller to add events to the table
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

            // Auto-scroll to the new row
            table.scrollRectToVisible(table.getCellRect(tableModel.getRowCount() - 1, 0, true));
        });
    }

    // Method for controller to display messages
    public void displayMessage(String message, String title) {
        JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Method for controller to display errors
    public void displayError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Method for controller to show the frame
    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);
    }

    // Method to clear the table
    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
        });
    }
}
