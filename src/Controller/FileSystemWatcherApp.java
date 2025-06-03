package Controller;
import View.FileWatcherView;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FileSystemWatcherApp {

    public static void main(String[] arg){

        System.out.println(" Starting File System Watcher App...");
        System.out.println(" PropertyChange Testing Enabled!");

        // Create the view
        FileWatcherView mf = new FileWatcherView(); //(width, height)

        // Set up UI defaults
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        if(defaults.get("Table.alternateRowColor") == null){
            defaults.put("Table.alternateRowColor", new Color(240, 240, 240));
        }

        // Add propertyChange testing
        PropertyChangeListener testListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("\n PROPERTY CHANGE EVENT DETECTED!");
                System.out.println("    Property Name: " + evt.getPropertyName());
                System.out.println("    Old Value: " + evt.getOldValue());
                System.out.println("    New Value: " + evt.getNewValue());
                System.out.println("    Source Class: " + evt.getSource().getClass().getSimpleName());
                System.out.println("========================================");

                // Optional: Show what this means
                switch(evt.getPropertyName()) {
                    case "submitPath":
                        System.out.println("   Controller should: Save path configuration");
                        break;
                    case "startWatching":
                        System.out.println("   Controller should: Start file monitoring");
                        break;
                    case "stopWatching":
                        System.out.println("   Controller should: Stop file monitoring");
                        break;
                    case "extensionChanged":
                        System.out.println("   Controller should: Update extension filter");
                        break;
                    case "queryDatabase":
                        System.out.println("  Controller should: Open database query");
                        break;
                    default:
                        System.out.println("   Unknown event type");
                }
                System.out.println();
            }
        };

        // Connect the test listener to the view
        mf.addPropertyChangeListener(testListener);
        System.out.println(" View created and PropertyChange listener attached!");
        System.out.println(" Instructions:");
        System.out.println("   - Click Submit button →show 'submitPath' event");
        System.out.println("   - Click Start button → show 'startWatching' event");
        System.out.println("   - Click Stop button → show 'stopWatching' event");
        System.out.println("   - Change Extension → show 'extensionChanged' event");
        System.out.println("   - Use Query menu → show 'queryDatabase' event");
        System.out.println(" Watch the console output when it interact with the GUI!");
        System.out.println("==========================================\n");
    }
}
