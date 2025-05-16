package View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.awt.*;
import java.util.Arrays;

/*
BorderLayout for the main containers
GridBagLayout for the form sections
BoxLayout for groups of related components (like the buttons)
 */
public final class FileWatcherView implements PropertyChangeListener{

    /** The dimension object used to get width and height of the screen.*/
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().
            getScreenSize();
    /**The width of the JFrame.*/
    public static final int JFRAME_WIDTH = WINDOW_DIMENSION.width / 3;

    /** The height of the JFrame.*/
    public static final int JFRAME_HEIGHT = WINDOW_DIMENSION.height / 3;

    /**
     * The dimension of the GUI.
     */
    public static final Dimension GUI_DIMENSION = new Dimension(JFRAME_WIDTH, JFRAME_HEIGHT);


    /**
     * Path to the UW icon for the JFrame.
     */
    public static final String ICON_PATH = "./Assets/icon.png";

    /**
     * icon object for the UW icon.
     */
    //public static final ImageIcon WHEEL_ICON = new ImageIcon(WHEEL_ICON_PATH);

    private final JList<String> fileEventList; //JList objects for displaying items.
    /**
     * Title of the application.
     */
    public static final String WINDOW_TITLE = "File Watcher";

    private final JFrame mainFrame;
    private final JComboBox extensionSelector;
    private final JTextField directySelector;
    private final JMenuBar menuBar;
    private JMenu menu;         // each menu in the menu-bar
   // private JMenuItem menuItem; // an item in a menu
    //private JLabel label;//this for contenPane and layout
    private JComboBox box;

    /** Undo button of JButton. */
    private final JButton submitButton;
    private final JButton startButton;
    private final JButton stopButton;
    /** for JComboBox*/
    private JComboBox<String>extensionDropdown; //for combobox dropdown
    private JTextField extensionTextField;
    private final String[] commonExtensions = {"", ".txt", ".pdf", ".docx", ".xlsx", ".csv", ".py", ".java", ".html", ".css", ".js"};

    private final JMenuItem startMenuItem;
    private final JMenuItem stopMenuItem;
    private final JMenuItem QueryMenuItem;
    private final JMenuItem closeMenuItem;
    private final JMenu helpMenu;



    /*

      fileMenu.add(new JMenuItem("Start"));
        fileMenu.add(new JMenuItem("Stop"));
        fileMenu.add(new JMenuItem("Query Database(file extension)"));
        fileMenu.add(new JMenuItem("Close"));
     */

    /**
     * The constructor.
     */
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
        this.menuBar = new JMenuBar();
       startButton = new JButton("start"); // check the gray and work color late
        stopButton = new JButton("stop");  // check the gray and work color late

        // A menu-bar contains menus. A menu contains menu-items (or sub-Menu)
        JMenuBar menuBar;   // the menu-bar
        JMenu menu;         // each menu in the menu-bar
        JMenuItem menuItem; // an item in a menu
        menuBar = new JMenuBar();

       // box = new JComboBox<>(commonExtensions);
// Remove this redundant/unnecessary line, as it's already redefined in `initComponents()` or unused.
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

        //Jmenu bar
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

        // Add Extension label and combobox
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

        // Create column headers for the JList
        String[] columnNames = {"Row", "Extension", "Filename", "PATH", "Event"};
        JPanel headerPanel = new JPanel(new GridLayout(1, columnNames.length));
        for (String columnName : columnNames) {
            headerPanel.add(new JLabel(columnName));
        }
        tablePanel.add(headerPanel, BorderLayout.NORTH);

        // Add JList with scroll pane
        JScrollPane scrollPane = new JScrollPane(fileEventList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main frame
        mainFrame.add(formPanel, BorderLayout.NORTH);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // Set frame properties



//        // property change list for start button
//        startButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
//            @Override
//            public void propertyChange(PropertyChangeEvent e){
//                System.out.println(" " + e.getNewValue());
//            }
//        });
//
//        // property change list for start button
//        stopButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
//            @Override
//            public void propertyChange(PropertyChangeEvent e){
//                System.out.println(" " + e.getNewValue());
//            }
//        });
//
//        // Action listeners for start button
//        startButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Started");
//                startButton.setEnabled(false);
//                stopButton.setEnabled(true);startButton.setEnabled(false);
//                startMenuItem.setEnabled(false);
//                stopMenuItem.setEnabled(true);
//            }
//        });
//
//        // Action listeners for stop button
//        stopButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Stoped");
//
//
//                startButton.setEnabled(true);
//                stopButton.setEnabled(false);
//                startMenuItem.setEnabled(true);
//                stopMenuItem.setEnabled(false);
//            }
//        });
//        // property change list for submit button
//        submitButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
//            @Override
//            public void propertyChange(PropertyChangeEvent e){
//                System.out.println(" " + e.getNewValue());
//            }
//        });
//
//        // Action listeners for submit button
//        submitButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Submit");
//                //stopButton.setEnabled(false);
//                //startButton.setEnabled(true);
//            }
//        });

        // Listener for selection change
        extensionDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedType = (String) extensionDropdown.getSelectedItem();
                System.out.println(selectedType);
            }
        });
        fileJMenuItem();
        menuBarJMenuItem();
        editMenuJMenuItem();
        collectActionPerformed();
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        //mainFrame.pack();
        mainFrame.revalidate();
        mainFrame.repaint();
        System.out.println("Frame contents: " + Arrays.toString(mainFrame.getContentPane().getComponents()));
        System.out.println("Table panel contents: " + Arrays.toString(tablePanel.getComponents()));

    }


    //helper function for Actionlistener(submit, stop, start)
    public void collectActionPerformed(){
        // property change list for start button
        startButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // property change list for start button
        stopButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for start button
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Started");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);startButton.setEnabled(false);
                startMenuItem.setEnabled(false);
                stopMenuItem.setEnabled(true);
            }
        });

        // Action listeners for stop button
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Stoped");

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                startMenuItem.setEnabled(true);
                stopMenuItem.setEnabled(false);
            }
        });
        // property change list for submit button
        submitButton.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for submit button
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Submit");
                //stopButton.setEnabled(false);
                //startButton.setEnabled(true);
            }
        });

        // Listener for selection change
        extensionDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedType = (String) extensionDropdown.getSelectedItem();
                System.out.println(selectedType);
            }
        });
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
                    Name: John Smith<br>
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
//new ImageIcon(ICON_PATH)


    //helper function for JMenubar
    public void menuBarJMenuItem(){

        // Create menu bar
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(startMenuItem);
        fileMenu.add(stopMenuItem);
        fileMenu.add(QueryMenuItem);
        fileMenu.add(closeMenuItem);


        JMenu editMenu = new JMenu("Edit");
        //JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
    }


    //helper function for file JMenuItem
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
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                startMenuItem.setEnabled(false);
                stopMenuItem.setEnabled(true);
            }
        });


        // property change list for file JMenuItemStart
        stopMenuItem.addPropertyChangeListener("enable" , new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e){
                System.out.println(" " + e.getNewValue());
            }
        });

        // Action listeners for start button
        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Stoped");
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
                QueryMenuItem.setEnabled(false);// will see
                QueryMenuItem.setEnabled(true); // will see
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

    }

    /*

    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        if (s.equals("click")) {
            // create a dialog Box
            JDialog d = new JDialog(f, "dialog Box");

            // create a label

     */

    @java.lang.Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {

    }
}