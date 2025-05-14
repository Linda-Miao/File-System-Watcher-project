package controller;

import model.FileWatcherModel;
import view.FileWatcherView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FileWatcherController implements PropertyChangeListener {

    private final FileWatcherModel model;
    private final FileWatcherView view;

    public FileWatcherController() {
        model = new FileWatcherModel();
        view = new FileWatcherView();

        // Register this controller to listen for view events
        view.addPropertyChangeListener(this);

        // Start the view
        view.start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if ("startWatching".equals(propertyName)) {
            String directoryPath = (String) evt.getNewValue();
            view.displayWatchingMessage(directoryPath);

            new Thread(() -> {
                try {
                    model.watchDirectory(directoryPath);
                } catch (Exception e) {
                    view.displayError("Error watching directory: " + e.getMessage());
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        new FileWatcherController(); // Launch the program
    }
}
