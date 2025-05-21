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
