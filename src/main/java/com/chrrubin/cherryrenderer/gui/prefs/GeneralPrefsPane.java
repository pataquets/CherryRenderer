package com.chrrubin.cherryrenderer.gui.prefs;

import com.chrrubin.cherryrenderer.prefs.AutoCheckUpdatePreference;
import com.chrrubin.cherryrenderer.prefs.AutoSaveSnapshotsPreference;
import com.chrrubin.cherryrenderer.prefs.FriendlyNamePreference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.logging.Logger;

public class GeneralPrefsPane extends GridPane implements IPrefsPane {
    private final Logger LOGGER = Logger.getLogger(GeneralPrefsPane.class.getName());
    @FXML
    private TextField nameTextField;
    @FXML
    private CheckBox autosaveCheckBox;
    @FXML
    private CheckBox updateCheckBox;

    private FriendlyNamePreference friendlyNamePreference = new FriendlyNamePreference();
    private AutoSaveSnapshotsPreference autoSaveSnapshotsPreference = new AutoSaveSnapshotsPreference();
    private AutoCheckUpdatePreference autoCheckUpdatePreference = new AutoCheckUpdatePreference();

    public GeneralPrefsPane(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/prefs/GeneralPrefs.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.setClassLoader(getClass().getClassLoader());
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        nameTextField.setText(friendlyNamePreference.get());
        autosaveCheckBox.setSelected(autoSaveSnapshotsPreference.get());
        updateCheckBox.setSelected(autoCheckUpdatePreference.get());
    }

    @Override
    public void resetToDefaults() {
        friendlyNamePreference.reset();
        autoSaveSnapshotsPreference.reset();
        autoCheckUpdatePreference.reset();
    }

    @Override
    public void savePreferences() {
        String friendlyName = nameTextField.getText().trim();
        if(friendlyName.equals("") || friendlyName.length() > 64){
            throw new RuntimeException("Friendly name must not be empty or more than 64 characters!");
        }

        friendlyNamePreference.put(friendlyName);
        autoSaveSnapshotsPreference.put(autosaveCheckBox.isSelected());
        autoCheckUpdatePreference.put(updateCheckBox.isSelected());

        LOGGER.finer(friendlyNamePreference.getKey() + " has been set to " + friendlyName);
        LOGGER.finer(autoSaveSnapshotsPreference.getKey() + " has been set to " + updateCheckBox.isSelected());
        LOGGER.finer(autoCheckUpdatePreference.getKey() + " has been set to " + autosaveCheckBox.isSelected());
    }
}