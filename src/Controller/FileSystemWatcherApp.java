package Controller;

import View.FileWatcherView;

import javax.swing.*;
import java.awt.*;

public class FileSystemWatcherApp {

    public static void main(String[] arg){
        FileWatcherView mf = new FileWatcherView(); //(width, height)
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        if(defaults.get("Table.alternateRowColor") == null){
            defaults.put("Table.alternateRowColor", new Color(240, 240, 240));
        }
    }
}

/*
For "Query Database", you can do:

Query by file size
Combined queries (e.g., all .txt files modified in the last 24 hours)
Query for the k-th most frequent viewed file (that's one of the most popular questions on Leetcode)


For "Edit" menu, you can do:

Export query results to CSV/TXT file
Sort events by different columns (filename, time, event type)
Bookmark important events for later reference
Add notes to specific file events (could be stored in the database)


 */