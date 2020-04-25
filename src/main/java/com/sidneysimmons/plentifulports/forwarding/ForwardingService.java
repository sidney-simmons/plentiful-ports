package com.sidneysimmons.plentifulports.forwarding;

import com.sidneysimmons.plentifulports.forwarding.domain.ForwardingRunnable;
import com.sidneysimmons.plentifulports.kubernetes.KubernetesGateway;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import com.sidneysimmons.plentifulports.ui.component.PortsMonitor;
import com.sidneysimmons.plentifulports.ui.component.PortsTable;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service for forwarding the ports.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("forwardingService")
public final class ForwardingService {

    @Resource(name = "portsTable")
    private PortsTable portsTable;

    @Resource(name = "portsMonitor")
    private PortsMonitor portsMonitor;

    @Resource(name = "kubernetesGateway")
    private KubernetesGateway kubernetesGateway;

    private Map<ServiceConfiguration, ForwardingRunnable> runnables = new HashMap<>();

    @PreDestroy
    public void destroy() {
        log.info("Shutting down forwarding.");
        stopServices();
    }

    /**
     * Stop all services.
     */
    public synchronized void stopServices() {
        log.info("Stopping all services.");
        for (Entry<ServiceConfiguration, ForwardingRunnable> entry : runnables.entrySet()) {
            disableForwarding(entry.getKey());
            entry.setValue(null);
        }
    }

    /**
     * Load the given services. This does not start any forwarding.
     * 
     * @param serviceConfigurations the services
     */
    public synchronized void loadServices(List<ServiceConfiguration> serviceConfigurations) {
        // Clear the UI table
        log.info("Loading services.");
        SwingUtilities.invokeLater(portsTable::clearTable);

        // Disable any existing forwarding
        for (Entry<ServiceConfiguration, ForwardingRunnable> entry : runnables.entrySet()) {
            disableForwarding(entry.getKey());
            entry.setValue(null);
        }

        // Clear the runnable map and add the new services
        runnables.clear();
        for (ServiceConfiguration serviceConfiguration : serviceConfigurations) {
            SwingUtilities.invokeLater(() -> portsTable.addServiceToTable(serviceConfiguration,
                    event -> handleToggle(serviceConfiguration, event.getStateChange() == ItemEvent.SELECTED)));
            runnables.put(serviceConfiguration, null);
        }
    }

    /**
     * Check if services are currently loaded.
     * 
     * @return true if services are loaded, false otherwise
     */
    public synchronized Boolean areServicesLoaded() {
        return !runnables.isEmpty();
    }

    /**
     * Handle when a given service has been toggled on or off.
     * 
     * @param serviceConfiguration the service that was toggled
     * @param checked whether or not the checkbox is checked
     */
    private synchronized void handleToggle(ServiceConfiguration serviceConfiguration, Boolean checked) {
        if (checked) {
            enableForwarding(serviceConfiguration);
        } else {
            disableForwarding(serviceConfiguration);
        }
    }

    /**
     * Enable the forwarding for a given service.
     * 
     * @param serviceConfiguration the service
     */
    private synchronized void enableForwarding(ServiceConfiguration serviceConfiguration) {
        ForwardingRunnable runnable = runnables.get(serviceConfiguration);
        if (runnable == null || !runnable.isAlive()) {
            log.info("Enabling forwarding for " + serviceConfiguration + ".");
            runnable = new ForwardingRunnable(serviceConfiguration, portsTable, portsMonitor, kubernetesGateway);
            runnables.put(serviceConfiguration, runnable);
            new Thread(runnable).start();
        }
    }

    /**
     * Disable the forwarding for a given service.
     * 
     * @param serviceConfiguration the service
     */
    private synchronized void disableForwarding(ServiceConfiguration serviceConfiguration) {
        ForwardingRunnable runnable = runnables.get(serviceConfiguration);
        if (runnable != null && runnable.isAlive()) {
            log.info("Disabling forwarding for " + serviceConfiguration + ".");
            runnable.kill();
            runnables.put(serviceConfiguration, null);
        }
    }

}
