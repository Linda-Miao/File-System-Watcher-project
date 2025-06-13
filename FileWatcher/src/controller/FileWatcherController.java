package controller;

import model.*;
import view.FileWatcherView;
import view.QueryView;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The controller for the File Watcher application, bridging the Model and View.
 * Handles user actions, file watching, and database operations.
 */
public class FileWatcherController implements PropertyChangeListener {

    private final FileSystemModel model;
    private final FileWatcherView view;
    private final FileWatcher fileWatcher;
    private final DatabaseManager dbManager;
    private final QueryManager queryManager;

    public FileWatcherController(FileWatcherView view) {
        this.view = view;

        // Initialize Model components
        this.model = new FileSystemModel();
        this.dbManager = DatabaseManager.getInstance();
        this.queryManager = new QueryManager();
        this.fileWatcher = new FileWatcher(model);

        // Link Model to View
        model.addPropertyChangeListener(this);
        view.addPropertyChangeListener(this);

        // Set up event handlers
        setupViewListeners();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileWatcherView view = new FileWatcherView();
            new FileWatcherController(view); // Connects View and Model
        });
    }

    /**
     * Sets up listeners for View actions (e.g., button clicks).
     */
    private void setupViewListeners() {
        // Start watching directory
        view.startButton.addActionListener(e -> startWatching());

        // Stop watching
        view.stopButton.addActionListener(e -> stopWatching());

        // Submit directory path/extensions
        view.submitButton.addActionListener(e -> updateSettings());

        // Query database
        view.queryMenuItem.addActionListener(e -> openQueryView());

        // Ask if the user wants to save the data before exit
        view.getMainFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        view.getMainFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (view.getSaveToDbMenuItem().isEnabled()) {
                    int choice = JOptionPane.showConfirmDialog(
                            view.getMainFrame(),
                            "Do you want to save the data to the database before exiting?",
                            "Confirm Exit",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        view.saveTableToDatabase(); // Save data to the database
                        System.exit(0); // Exit after saving
                    } else if (choice == JOptionPane.NO_OPTION) {
                        System.exit(0); // Exit without saving
                    }
                    // Cancel: do nothing, so the window stays open
                } else {
                    // If the menu item is disabled (data already saved), exit directly
                    System.exit(0);
                }
            }
        });

        //closeMenuItem.addActionListener(e -> System.exit(0));
        view.getCloseMenuItem().addActionListener(e -> {
            // Check if the "Save to Database" menu item is enabled
            if (view.getSaveToDbMenuItem().isEnabled()) {
                int choice = JOptionPane.showConfirmDialog(
                        view.getMainFrame(), // Use the main frame instead of null for better context
                        "Do you want to save the data to the database before exiting?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    view.saveTableToDatabase(); // Your method to save data
                    System.exit(0);
                } else if (choice == JOptionPane.NO_OPTION) {
                    System.exit(0);
                }
                // If CANCEL or CLOSE is selected, do nothing
            } else {
                // If the menu item is disabled (data already saved), exit directly
                System.exit(0);
            }
        });

        view.emailButton.addActionListener(e -> {

            if (!view.getExportCSVMenuItem().isEnabled()) {

                try {
                    // Prompt for recipient
                    String recipient = JOptionPane.showInputDialog(view.getMainFrame(), "Enter recipient email:");
                    if (recipient == null || recipient.trim().isEmpty()) return;

                    // Get report file path (modify this if your report is named differently)
                    File reportFile = new File("Report.csv"); // or whatever your file is

                    // Sender credentials (use your actual Gmail and app password)
                    String senderEmail = "mutahar.yazdan@yahoo.com";
                    String senderPassword = "xwdwjhawyiulriss";

                    EmailService emailService = new EmailService(senderEmail, senderPassword);

                    emailService.sendEmailWithAttachment(
                            recipient,
                            "File Watcher Results Report",
                            "Please find the attached results from File Watcher.",
                            reportFile
                    );

                    JOptionPane.showMessageDialog(view.getMainFrame(), "Email sent successfully!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(view.getMainFrame(),
                            "Failed to send email:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(view.getMainFrame(), "Please Export results first!");
            }
        });



    }

    /**
     * Starts watching the directory specified in the View.
     */
    private void startWatching() {
        String directoryPath = view.getSelectedPath();
        String extension = view.getSelectedExtension();

        if (directoryPath.isEmpty()) {
            view.displayError("Please enter a directory path first.");
            return;
        }

        try {
            Set<String> extensions = new HashSet<>();
            if (extension != null && !extension.isEmpty()) {
                extensions.add(extension);
            }

            // Update Model with extensions
            model.setFileExtensions(List.of(extension));

            // Start FileWatcher
            Path path = Paths.get(directoryPath);
            fileWatcher.startWatching(path, extensions);

            view.displayMessage(
                    "Watching directory: " + directoryPath +
                            "\nExtensions: " + (extensions.isEmpty() ? "ALL" : extensions),
                    "File Watcher Started"
            );

        } catch (Exception e) {
            view.displayError("Failed to start watching: " + e.getMessage());
        }
    }

    /**
     * Stops the file watcher.
     */
    private void stopWatching() {
        fileWatcher.stopWatching();
        view.displayMessage("File watching stopped.", "Stopped");
    }

    /**
     * Updates Model with current directory/extensions from View.
     */
    private void updateSettings() {
        String directoryPath = view.getSelectedPath();
        String extension = view.getSelectedExtension();

        if (directoryPath.isEmpty()) {
            view.displayError("Directory path cannot be empty.");
            return;
        }

        Set<String> extensions = new HashSet<>();
        if (extension != null && !extension.isEmpty()) {
            extensions.add(extension);
        }

        model.setFileExtensions(List.of(extension));
        view.displayMessage(
                "Settings updated:\nPath: " + directoryPath +
                        "\nExtension: " + (extension.isEmpty() ? "ALL" : extension),
                "Settings Applied"
        );
    }

    /**
     * Opens the QueryView dialog for database queries.
     */
    private void openQueryView() {
        new QueryView(view); // QueryView handles its own logic
    }

    /**
     * Handles property changes from the Model (e.g., new file events).
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "fileEventAdded":
                FileEvent event = (FileEvent) evt.getNewValue();
                dbManager.saveToDatabase(event);
                SwingUtilities.invokeLater(() -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedTime = event.getMyTimeStamp().format(formatter);

                    view.addEventToTable(
                            event.getMyFileName(),
                            event.getMyFileExtension(),
                            event.getMyPath(),
                            event.getMyEventType().toString(),
                            formattedTime
                    );
                });
                break;


            case "startWatching":
                view.startButton.setEnabled(false);
                view.stopButton.setEnabled(true);
                break;

            case "stopWatching":
                view.startButton.setEnabled(true);
                view.stopButton.setEnabled(false);
                break;
        }
    }
}