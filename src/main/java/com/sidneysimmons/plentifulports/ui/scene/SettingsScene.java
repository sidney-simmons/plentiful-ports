package com.sidneysimmons.plentifulports.ui.scene;

import com.sidneysimmons.plentifulports.settings.SettingsService;
import com.sidneysimmons.plentifulports.settings.domain.Settings;
import com.sidneysimmons.plentifulports.settings.domain.SettingsValidity;
import com.sidneysimmons.plentifulports.settings.exception.SettingsException;
import com.sidneysimmons.plentifulports.ui.FrameManager;
import com.sidneysimmons.plentifulports.ui.component.CustomFont;
import com.sidneysimmons.plentifulports.util.CustomStringUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.annotation.Resource;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Settings scene.
 * 
 * @author Sidney Simmons
 */
@Slf4j
public class SettingsScene extends GenericScene {

    @Resource(name = "frameManager")
    private FrameManager frameManager;

    @Resource(name = "settingsService")
    private SettingsService settingsService;

    private JLabel validityLabel;
    private JButton saveButton;
    private JButton undoButton;
    private JTextArea settingsEditorTextArea;
    private String originalSettingsString;

    @Override
    public void onCreate() {
        buildStatusBar(getRoot());
        buildSettingsEditor(getRoot());
        buildButtonBar(getRoot());
    }

    @Override
    public void onDestroy() {
        // Not needed yet
    }

    @Override
    public void onShow() {
        try {
            Settings settings = settingsService.readSettingsObject();
            SettingsValidity settingsValidity = settingsService.validateSettingsObject(settings);
            if (!settingsValidity.getValid()) {
                log.error("Settings aren't valid. " + settingsValidity.getMessage());
                frameManager.showErrorMessage("Settings aren't valid. " + settingsValidity.getMessage(), null, null);
                setValidityStatus(false);
            } else {
                setValidityStatus(true);
            }
            originalSettingsString = settingsService.formatSettings(settings);
        } catch (SettingsException e1) {
            try {
                log.error("Settings can't be parsed.");
                frameManager.showErrorMessage("Settings can't be parsed.", e1, null);
                originalSettingsString = settingsService.readSettingsString();
                setValidityStatus(false);
            } catch (SettingsException e2) {
                log.error("Settings can't be read from file.");
                frameManager.showErrorMessage("Settings can't be read from file.", e2, null);
                originalSettingsString = "";
                setValidityStatus(false);
            }
        }
        setSettingsEditorText(originalSettingsString);

        // Disable the save/undo buttons
        saveButton.setEnabled(false);
        undoButton.setEnabled(false);
    }

    @Override
    public void onHide() {
        // Not needed yet
    }

    /**
     * Set the settings editor text area text. Also makes sure the caret position is at the top.
     * 
     * @param text the text
     */
    private void setSettingsEditorText(String text) {
        settingsEditorTextArea.setText(CustomStringUtils.normalizeLineEndings(text));
        settingsEditorTextArea.setCaretPosition(0);
    }

    /**
     * Set the settings validity status.
     * 
     * @param isValid boolean indicating whether or not the settings status is valid
     */
    private void setValidityStatus(Boolean isValid) {
        if (isValid) {
            validityLabel.setText("VALID");
        } else {
            validityLabel.setText("INVALID");
        }
    }

    /**
     * Handle the undo button being pressed.
     */
    private void handleUndo() {
        if (undoButton.isEnabled()) {
            setSettingsEditorText(originalSettingsString);
        }
    }

    /**
     * Handle when the text in the editor changes.
     */
    private void handleEditorChange() {
        if (CustomStringUtils.equalsIgnoreLineEnds(originalSettingsString, settingsEditorTextArea.getText())) {
            saveButton.setEnabled(false);
            undoButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
            undoButton.setEnabled(true);
        }
    }

    /**
     * Handle the save button being pressed.
     */
    private void handleSave() {
        // Return the if the button isn't enabled
        if (!saveButton.isEnabled()) {
            return;
        }

        // Parse the settings
        Settings settings = null;
        try {
            settings = settingsService.parseSettings(settingsEditorTextArea.getText());
        } catch (SettingsException e) {
            log.error("Settings can't be parsed.", e);
            frameManager.showErrorMessage("Settings can't be parsed.", e, null);
            return;
        }

        // Validate the settings
        SettingsValidity settingsValidity = settingsService.validateSettingsObject(settings);
        if (!settingsValidity.getValid()) {
            log.error("Settings aren't valid. " + settingsValidity.getMessage());
            frameManager.showErrorMessage("Settings aren't valid. " + settingsValidity.getMessage(), null, null);
            return;
        }

        // Save the settings
        try {
            settingsService.writeSettingsObject(settings);
        } catch (SettingsException e) {
            log.error("Can't write settings.", e);
            frameManager.showErrorMessage("Can't write settings.", e, null);
            return;
        }

        // Update the editor and manually invoke the change listener because the editor technically hasn't changed
        originalSettingsString = settingsService.formatSettings(settings);
        setSettingsEditorText(originalSettingsString);
        setValidityStatus(true);
    }

    /**
     * Build the status bar.
     * 
     * @param container the container
     */
    private void buildStatusBar(JPanel container) {
        JLabel filePathLabel = new JLabel(settingsService.resolveSettingsFile().getAbsolutePath());
        filePathLabel.setFont(CustomFont.BOLD);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(10, 10, 0, 10);
        container.add(filePathLabel, constraints);

        validityLabel = new JLabel();
        validityLabel.setFont(CustomFont.BOLD);
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_END;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(10, 10, 0, 10);
        container.add(validityLabel, constraints);
    }

    /**
     * Build the settings editor.
     * 
     * @param container the container
     */
    private void buildSettingsEditor(JPanel container) {
        settingsEditorTextArea = new JTextArea();
        settingsEditorTextArea.setFont(CustomFont.MONOSPACE);
        settingsEditorTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleEditorChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleEditorChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleEditorChange();
            }
        });
        JScrollPane textScrollPane = new JScrollPane(settingsEditorTextArea);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(10, 10, 0, 10);
        container.add(textScrollPane, constraints);
    }

    /**
     * Build the button bar.
     * 
     * @param container the container
     */
    private void buildButtonBar(JPanel container) {
        undoButton = new JButton("Undo");
        undoButton.addActionListener(event -> handleUndo());

        saveButton = new JButton("Save");
        saveButton.addActionListener(event -> handleSave());

        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.LINE_AXIS));
        buttonBar.add(Box.createHorizontalGlue());
        buttonBar.add(undoButton);
        buttonBar.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonBar.add(saveButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.FIRST_LINE_END;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(10, 10, 10, 10);
        container.add(buttonBar, constraints);
    }

}
