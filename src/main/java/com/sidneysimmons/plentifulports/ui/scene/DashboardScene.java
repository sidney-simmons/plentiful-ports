package com.sidneysimmons.plentifulports.ui.scene;

import com.sidneysimmons.plentifulports.forwarding.ForwardingService;
import com.sidneysimmons.plentifulports.kubernetes.KubernetesGateway;
import com.sidneysimmons.plentifulports.kubernetes.domain.KubernetesContext;
import com.sidneysimmons.plentifulports.kubernetes.exception.KubernetesException;
import com.sidneysimmons.plentifulports.settings.SettingsService;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import com.sidneysimmons.plentifulports.settings.exception.SettingsException;
import com.sidneysimmons.plentifulports.thread.ThreadService;
import com.sidneysimmons.plentifulports.ui.FrameManager;
import com.sidneysimmons.plentifulports.ui.component.ComponentHelper;
import com.sidneysimmons.plentifulports.ui.component.CustomFont;
import com.sidneysimmons.plentifulports.ui.component.PortsMonitor;
import com.sidneysimmons.plentifulports.ui.component.PortsTable;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import javax.annotation.Resource;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;

/**
 * Dashboard scene.
 * 
 * @author Sidney Simmons
 */
@Slf4j
public class DashboardScene extends GenericScene {

    @Resource(name = "frameManager")
    private FrameManager frameManager;

    @Resource(name = "settingsService")
    private SettingsService settingsService;

    @Resource(name = "threadService")
    private ThreadService threadService;

    @Resource(name = "kubernetesGateway")
    private KubernetesGateway kubernetesGateway;

    @Resource(name = "portsTable")
    private PortsTable portsTable;

    @Resource(name = "portsMonitor")
    private PortsMonitor portsMonitor;

    @Resource(name = "forwardingService")
    private ForwardingService forwardingService;

    private JProgressBar progressIndicator;
    private JLabel currentContextLabel;

    @Override
    public void onCreate() {
        buildHeader(getRoot());
        buildPortsTable(getRoot());
        buildPortsMonitor(getRoot());
    }

    @Override
    public void onDestroy() {
        // Not needed yet
    }

    @Override
    public void onShow() {
        if (settingsService.settingsAreValid()) {
            if (!forwardingService.areServicesLoaded()) {
                loadServices();
            }
            threadService.execute(this::updateCurrentContext);
        } else {
            SwingUtilities.invokeLater(() -> frameManager.activateScene(SettingsScene.class));
        }
    }

    @Override
    public void onHide() {
        // Not needed yet
    }

    /**
     * Load services.
     */
    public void loadServices() {
        try {
            List<ServiceConfiguration> serviceConfigurations =
                    settingsService.readSettingsObject().getForwardingConfiguration().getServices();
            forwardingService.loadServices(serviceConfigurations);
        } catch (SettingsException e) {
            log.error("Can't read the settings.", e);
            frameManager.showErrorMessage("Can't read the settings.", e, null);
        }
    }

    /**
     * Update the current context;
     */
    private void updateCurrentContext() {
        SwingUtilities.invokeLater(() -> progressIndicator.setVisible(true));
        threadService.execute(() -> {
            try {
                KubernetesContext currentContext = kubernetesGateway.readCurrentContext();
                SwingUtilities.invokeLater(() -> currentContextLabel.setText(currentContext.getName()));
            } catch (KubernetesException e) {
                log.error("Can't update the current context.", e);
                frameManager.showErrorMessage("Can't update the current context.", e, null);
            }
            SwingUtilities.invokeLater(() -> progressIndicator.setVisible(false));
        });
    }

    /**
     * Build the header.
     * 
     * @param container the container
     */
    private void buildHeader(JPanel container) {
        currentContextLabel = new JLabel();
        currentContextLabel.setFont(CustomFont.BOLD);

        JLabel currentContextLabelLabel = new JLabel("Current context:");
        currentContextLabelLabel.setFont(CustomFont.BOLD);

        progressIndicator = new JProgressBar();
        progressIndicator.setIndeterminate(true);
        progressIndicator.setMinimumSize(new Dimension(100, Integer.MIN_VALUE));
        progressIndicator.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
        progressIndicator.setPreferredSize(new Dimension(100, Integer.MIN_VALUE));
        progressIndicator.setVisible(false);

        JPanel headerBar = new JPanel();
        headerBar.setLayout(new BoxLayout(headerBar, BoxLayout.LINE_AXIS));
        headerBar.add(currentContextLabelLabel);
        headerBar.add(Box.createRigidArea(new Dimension(5, 0)));
        headerBar.add(currentContextLabel);
        headerBar.add(Box.createHorizontalGlue());
        headerBar.add(progressIndicator);
        container.add(headerBar, ComponentHelper.gridBagConstraints(GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL,
                new Insets(10, 10, 0, 10), 0, 0, 1, 1, 0, 0, 0.0, 0.0));
    }

    /**
     * Build the ports table.
     * 
     * @param container the container
     */
    private void buildPortsTable(JPanel container) {
        container.add(portsTable, ComponentHelper.gridBagConstraints(GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL,
                new Insets(10, 10, 0, 10), 0, 1, 1, 1, 0, 0, 1.0, 0.0));
    }

    /**
     * Build the ports monitor.
     * 
     * @param container the container
     */
    private void buildPortsMonitor(JPanel container) {
        JScrollPane scrollPane = new JScrollPane(portsMonitor);
        container.add(scrollPane, ComponentHelper.gridBagConstraints(GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10), 0, 2, 1, 1, 0, 0, 1.0, 1.0));
    }

}
