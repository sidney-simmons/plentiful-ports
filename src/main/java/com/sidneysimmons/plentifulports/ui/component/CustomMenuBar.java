package com.sidneysimmons.plentifulports.ui.component;

import com.sidneysimmons.plentifulports.ui.FrameManager;
import com.sidneysimmons.plentifulports.ui.scene.DashboardScene;
import com.sidneysimmons.plentifulports.ui.scene.SettingsScene;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.springframework.stereotype.Component;

/**
 * Custom menu bar for the application.
 * 
 * @author Sidney Simmons
 */
@Component("customMenuBar")
public class CustomMenuBar extends JMenuBar {

    @Resource(name = "frameManager")
    private transient FrameManager frameManager;

    private static final long serialVersionUID = 1L;

    @PostConstruct
    public void initialize() {
        add(buildNavigateMenu());
        add(buildActionsMenu());
    }

    /**
     * Build the navigate menu.
     * 
     * @return the navigate menu
     */
    private JMenu buildNavigateMenu() {
        JMenuItem dashboardMenuItem = new JMenuItem("Dashboard...");
        dashboardMenuItem.addActionListener(event -> frameManager.activateScene(DashboardScene.class));

        JMenuItem settingsMenutItem = new JMenuItem("Settings...");
        settingsMenutItem.addActionListener(event -> frameManager.activateScene(SettingsScene.class));

        JMenu navigateMenu = new JMenu("Navigate");
        navigateMenu.add(dashboardMenuItem);
        navigateMenu.add(settingsMenutItem);
        return navigateMenu;
    }

    /**
     * Build the actions menu.
     * 
     * @return the actions menu
     */
    private JMenu buildActionsMenu() {
        JMenuItem loadServicesMenuItem = new JMenuItem("Load services from settings");
        loadServicesMenuItem.addActionListener(event -> {
            DashboardScene dashboardScene = frameManager.getScene(DashboardScene.class);
            if (dashboardScene != null) {
                dashboardScene.loadServices();
            }
        });

        JMenu navigateMenu = new JMenu("Actions");
        navigateMenu.add(loadServicesMenuItem);
        return navigateMenu;
    }

}
