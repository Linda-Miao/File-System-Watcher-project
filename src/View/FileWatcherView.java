package View;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.Arrays;


/*
BorderLayout for the main containers
GridBagLayout for the form sections
BoxLayout for groups of related components (like the buttons)

 */
public final class FileWatcherView implements PropertyChangeListener{

    /**
     * The dimension object used to get width and height of the screen.
     */
    public static final Dimension WINDOW_DIMENSION = Toolkit.getDefaultToolkit().
            getScreenSize();
    /**
     * The width of the JFrame.
     */
    public static final int JFRAME_WIDTH = WINDOW_DIMENSION.width / 3;

    /**
     * The height of the JFrame.
     */
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

    private JList fileEventList; //JList objects for displaying items.
    /**
     * Title of the application.
     */
    public static final String WINDOW_TITLE = "File Watcher";

    private JFrame mainFrame;
    private JComboBox extensionSelector;
    private JTextField directySelector;
    private JMenuBar menuBar;
    private JMenu menu;         // each menu in the menu-bar
    private JMenuItem menuItem; // an item in a menu
    //private JLabel label;//this for contenPane and layout
    private JComboBox box;

    /** Undo button of JButton. */

    private final JButton startButton;
    private final JButton stopButton;
    /** for JComboBox*/
    private JComboBox<String>extensionDropdown; //for combobox dropdown
    private JTextField extensionTextField;
    private final String[] commonExtensions = {"", ".txt", ".pdf", ".docx", ".xlsx", ".csv", ".py", ".java", ".html", ".css", ".js"};
    /**
     * The constructor.
     */
    public FileWatcherView( ) {
        mainFrame = new JFrame(WINDOW_TITLE); //create a new JFrame Object(frame from field)
      //Initializes fileEventList and sets its selection mode.
        fileEventList= new JList();
        // Initialize JComboBox
        extensionSelector = new JComboBox( ); // () may have file name late
        directySelector = new JTextField();
        this.menuBar = new JMenuBar();
       startButton = new JButton("start"); // check the gray and work color late
        stopButton = new JButton("stop");  // check the gray and work color late

        // A menu-bar contains menus. A menu contains menu-items (or sub-Menu)
        JMenuBar menuBar;   // the menu-bar
        JMenu menu;         // each menu in the menu-bar
        JMenuItem menuItem; // an item in a menu
        menuBar = new JMenuBar();

        box = new JComboBox(commonExtensions);
// Remove this redundant/unnecessary line, as it's already redefined in `initComponents()` or unused.
        mainFrame.setSize(400, 300);
        mainFrame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        initComponents();
        System.out.println(mainFrame.getJMenuBar().getMenuCount());
        System.out.println(Arrays.toString(this.menuBar.getComponents()));

        //JComboBox
        extensionDropdown = new JComboBox<>(commonExtensions);
        extensionTextField = new JTextField(10); // maximum number is 10 for dropdown.
    }

    public void initComponents() {
        // Set layout for main frame
        mainFrame.setLayout(new BorderLayout());

        // Create menu bar
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Start"));
        fileMenu.add(new JMenuItem("Stop"));
        fileMenu.add(new JMenuItem("Query Database(file extension)"));
        fileMenu.add(new JMenuItem("Close"));

        JMenu editMenu = new JMenu("Edit");
        JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

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
        formPanel.add(directySelector, gbc);
        formPanel.add(extensionTextField, gbc);

        // Add Extension label and combobox
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel extensionLabel = new JLabel("Extension: (empty = ALL files)");
        formPanel.add(extensionLabel, gbc);
        formPanel.add(extensionTextField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        formPanel.add(extensionSelector, gbc);
        formPanel.add(extensionTextField, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JButton("Submit"), gbc);

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
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main frame
        mainFrame.add(formPanel, BorderLayout.NORTH);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // Set frame properties
        mainFrame.setSize(GUI_DIMENSION);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }


    //JComboBox

    public void PropertyChange(PropertyChangeEvent e){

    }

    public void showQueryDialog(){

    }


    @java.lang.Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {

    }
}