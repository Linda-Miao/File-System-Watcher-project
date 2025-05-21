package View;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QueryView {

    private final JButton submitButton;
    private final JTextField extensionTextField;
    private final JList<String> fileEventList;
    public QueryView(FileWatcherView watcherView){
        // Create a new dialog window for the query form
        JDialog queryDialog = new JDialog(watcherView.mainFrame, "QueryForm");

        // Create a label for the extension field with a helpful hint
        JLabel extensionLabel = new JLabel("Extension: (empty = ALL files)");

        // Use GridBagLayout to control the layout of the dialog components
        queryDialog.setLayout(new GridBagLayout());

        // Initialize the submit button and text field
        submitButton = new JButton("Submit");
        extensionTextField = new JTextField(10); // maximum number is 10 for dropdown.

        // Create a sample list with predefined file event descriptions
        fileEventList = new JList<>(new String[] { "Sample file that exceeds viewport width", "Another long event string"});

        /*TABLE PANEL CREATION */
        // Create a panel for the table (and/or list) with a BorderLayout
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Initialize the table model with column headers and no initial rows
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Row", "Extension", "Filename", "PATH", "Event", "Date/Time"}, 0);
        // Add test data to the table model
        tableModel.addRow(new Object[]{1, ".txt", "notes.txt", "/documents/work", "Created", "2023-10-15 10:15:00"});
        tableModel.addRow(new Object[]{2, ".java", "Main.java", "/projects/code", "Modified", "2023-10-15 12:30:00"});
        tableModel.addRow(new Object[]{3, ".html", "index.html", "/websites/home", "Deleted", "2023-10-14 09:00:00"});
        tableModel.addRow(new Object[]{4, ".css", "styles.css", "/websites/design", "Created", "2023-10-14 11:00:00"});
        tableModel.addRow(new Object[]{5, ".js", "script.js", "/websites/scripts", "Accessed", "2023-10-13 14:00:00"});
        tableModel.addRow(new Object[]{6, ".pdf", "paper.pdf", "/documents/research", "Archived", "2023-10-12 18:45:00"});

        // Create a JTable using the table model
        JTable table = new JTable(tableModel);

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

        // Force the horizontal scroll bar to always be visible
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Add the scroll pane (with the table) to the table panel
        tablePanel.add(scrollPane, BorderLayout.CENTER);


        // Set a fixed size for the table panel based on the main frame size
        tablePanel.setPreferredSize(new Dimension(watcherView.mainFrame.getSize().width/2, watcherView.mainFrame.getSize().height/2));


        // Fix the width of the file event list to exceed the viewport
        fileEventList.setFixedCellWidth(watcherView.mainFrame.getSize().width + 1000); // Exceeds the viewport width

        // Add an empty border to the table panel for aesthetics
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        /* FORM PANEL CREATION */
        // Create a form panel with a GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add the extension label to the form panel at (0, 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        extensionLabel = new JLabel("Extension: (empty = ALL files)");
        formPanel.add(extensionLabel, gbc);

        // Add the submit button to the form panel at (1, 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(submitButton, gbc);

        // Add the extension text field to the form panel at (0, 1)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(extensionTextField, gbc);

        /* ADD PANELS TO DIALOG  */
        // Add the form panel to the dialog at the top (15% height)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;       // Full width
        gbc.weighty = 0.15;      // 15% height
        gbc.fill = GridBagConstraints.BOTH; // Resize both horizontally and vertically
        queryDialog.add(formPanel, gbc);

        // Add the table panel to the dialog below the form panel (85% height)
        gbc.gridy = 1;
        gbc.weighty = 0.85;      // 85% height
        queryDialog.add(tablePanel, gbc);


        /* Query data dialog */
        // Set the dimensions of the dialog relative to the main frame
        queryDialog.setSize(watcherView.mainFrame.getSize().width*2, watcherView.mainFrame.getSize().height/2);
        // Center the query dialog relative to the main frame
        queryDialog.setLocationRelativeTo(watcherView.mainFrame);
        queryDialog.setVisible(true);  // Display the dialog


    }

}
