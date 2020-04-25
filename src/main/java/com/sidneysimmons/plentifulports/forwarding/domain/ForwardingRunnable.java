package com.sidneysimmons.plentifulports.forwarding.domain;

import com.sidneysimmons.plentifulports.kubernetes.KubernetesGateway;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import com.sidneysimmons.plentifulports.ui.component.PortsMonitor;
import com.sidneysimmons.plentifulports.ui.component.PortsTable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Runnable to initiate the port forwarding with kubernetes.
 * 
 * @author Sidney Simmons
 */
@Slf4j
public class ForwardingRunnable implements Runnable {

    private ServiceConfiguration serviceConfiguration;
    private PortsTable portsTable;
    private PortsMonitor portsMonitor;
    private KubernetesGateway kubernetesGateway;

    private Process forwardingProcess;
    private boolean isAlive = false;

    /**
     * Constructor.
     * 
     * @param serviceConfiguration the service configuration
     * @param portsTable the ports table
     * @param portsMonitor the ports monitor
     * @param kubernetesGateway the kubernetes gateway
     */
    public ForwardingRunnable(ServiceConfiguration serviceConfiguration, PortsTable portsTable, PortsMonitor portsMonitor,
            KubernetesGateway kubernetesGateway) {
        this.serviceConfiguration = serviceConfiguration;
        this.portsTable = portsTable;
        this.portsMonitor = portsMonitor;
        this.kubernetesGateway = kubernetesGateway;
    }

    @Override
    public void run() {
        // Set the alive flag to true
        logMessage("Port forwarding started for " + serviceConfiguration + ".");
        isAlive = true;

        try {
            // Execute the process
            ProcessBuilder processBuilder = kubernetesGateway.buildForwardPortsProcess(serviceConfiguration);
            forwardingProcess = processBuilder.start();

            // Read in the result
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(forwardingProcess.getInputStream()))) {
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        portsMonitor.addMessage(line.trim());
                    }
                }
            }

            // Wait for the process to complete
            forwardingProcess.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Forwarding did not shut down gracefully.", e);
        }

        // Set the alive flag to false and toggle the UI checkbox
        isAlive = false;
        portsTable.setToggle(serviceConfiguration, false);
        logMessage("Port forwarding stopped for " + serviceConfiguration + ".");
    }

    /**
     * Kill the forwarding process if it's currently alive.
     */
    public void kill() {
        if (isAlive()) {
            forwardingProcess.destroy();
        }
    }

    /**
     * Return this runnable's alive status.
     * 
     * @return true if the runnable is alive, false otherwise
     */
    public Boolean isAlive() {
        return isAlive;
    }

    /**
     * Log a message to the log and also the ports monitor.
     * 
     * @param message the message
     */
    private void logMessage(String message) {
        log.info(message);
        portsMonitor.addMessage(message);
    }

}
