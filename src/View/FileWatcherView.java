package View;

import javax.swing.*;
import java.beans.*;
import java.awt.*;


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
    //public static final String WHEEL_ICON_PATH = "./files/wheel_small.png";

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

    /** Undo button of JButton. */

    private final JButton startButton;
    private final JButton stopButton;



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
        menuBar = new JMenuBar();
       startButton = new JButton("start"); // check the gray and work color late
        stopButton = new JButton("stop");  // check the gray and work color late

        // A menu-bar contains menus. A menu contains menu-items (or sub-Menu)
        JMenuBar menuBar;   // the menu-bar
        JMenu menu;         // each menu in the menu-bar
        JMenuItem menuItem; // an item in a menu
        menuBar = new JMenuBar();
// Remove this redundant/unnecessary line, as it's already redefined in `initComponents()` or unused.
        initComponents();
    }

    public void initComponents(){
        // First Menu
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu helpMenu = new JMenu("Help");
        JMenu QMenu = new JMenu("Query Database(file extension)");
        menuBar.add(fileMenu);  // the menu-bar adds this menu
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        menuBar.add(QMenu);

        JMenuItem startMenuItem = new JMenuItem("Start");
        menu.add(startMenuItem); // the menu adds this item
        JMenuItem stopMenuItem = new JMenuItem("Stop");
        menu.add(stopMenuItem); // the menu adds this item
        JMenuItem closeMenuItem = new JMenuItem("Close");
        menu.add(closeMenuItem); // the menu adds this item
        JLabel pathLabel = new JLabel("PATH");
        JLabel extensionLabel = new JLabel("Extension: (empty = ALL files)");
        JLabel fileWatcherViewLabel = new JLabel("File Watcher View");
    }
    public void PropertyChange(PropertyChangeEvent e){

    }

    public void showQueryDialog(){

    }


    @java.lang.Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {

    }
}