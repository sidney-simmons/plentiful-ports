package com.sidneysimmons.plentifulports;

import com.sidneysimmons.plentifulports.settings.SettingsService;
import com.sidneysimmons.plentifulports.settings.domain.Settings;
import com.sidneysimmons.plentifulports.settings.exception.SettingsException;
import com.sidneysimmons.plentifulports.ui.FrameManager;
import com.sidneysimmons.plentifulports.ui.scene.DashboardScene;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Runs the application.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("appRunner")
public class AppRunner {

    @Resource(name = "frameManager")
    private FrameManager frameManager;

    @Resource(name = "settingsService")
    private SettingsService settingsService;

    /**
     * Run startup sequence and forward to the dashboard scene.
     */
    public void run() {
        // Check if the settings exist - if not create a default
        if (!settingsService.settingsFileExists()) {
            log.info("Settings file doesn't exist - creating default now.");
            try {
                Settings defaultSettings = settingsService.buildDefaultSettingsObject();
                settingsService.writeSettingsObject(defaultSettings);
            } catch (SettingsException e) {
                frameManager.showErrorMessage("Can't create a default settings file.", e, frameManager::closeApplication);
                return;
            }
        }

        // Check that the settings file is readable and writable
        if (!settingsService.settingsFileIsReadableAndWritable()) {
            frameManager.showErrorMessage("Settings file exists but is not readable and/or writable.", null,
                    frameManager::closeApplication);
            return;
        }

        // Send the user to the dashboard
        frameManager.activateScene(DashboardScene.class);
    }

}
