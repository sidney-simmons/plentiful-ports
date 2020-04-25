package com.sidneysimmons.plentifulports.kubernetes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sidneysimmons.plentifulports.kubernetes.domain.KubernetesContext;
import com.sidneysimmons.plentifulports.kubernetes.domain.KubernetesService;
import com.sidneysimmons.plentifulports.kubernetes.exception.KubernetesException;
import com.sidneysimmons.plentifulports.settings.domain.PortConfiguration;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Class for interacting with kubernetes.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("kubernetesGateway")
public final class KubernetesGateway {

    /**
     * Read the current kubernetes context.
     * 
     * @return the current context
     * @throws KubernetesException thrown if the context can't be read
     */
    public KubernetesContext readCurrentContext() throws KubernetesException {
        KubernetesContext context = null;
        try {
            // Create the process
            log.info("Reading current kubernetes context.");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command("kubectl", "config", "current-context");
            Process process = processBuilder.start();

            // Read in the result
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        context = new KubernetesContext();
                        context.setName(line.trim());
                    }
                }
            }

            // Wait for the process to complete
            Boolean success = process.waitFor(5, TimeUnit.SECONDS);
            if (!success) {
                throw new RuntimeException("Can't read kubernetes current context.");
            }
        } catch (Exception e) {
            throw new KubernetesException("Error integrating with kubernetes.", e);
        }
        return context;
    }

    /**
     * Read all available kubernetes contexts.
     * 
     * @return a list of available contexts
     * @throws KubernetesException thrown if the contexts can't be read
     */
    public List<KubernetesContext> readAvailableContexts() throws KubernetesException {
        List<KubernetesContext> contexts = new ArrayList<>();
        try {
            // Create the process
            log.info("Reading available kubernetes contexts.");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command("kubectl", "config", "get-contexts", "-o", "name");
            Process process = processBuilder.start();

            // Read in the result
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        KubernetesContext context = new KubernetesContext();
                        context.setName(line.trim());
                        contexts.add(context);
                    }
                }
            }

            // Wait for the process to complete
            Boolean success = process.waitFor(5, TimeUnit.SECONDS);
            if (!success) {
                throw new RuntimeException("Can't read available kubernetes contexts.");
            }
        } catch (Exception e) {
            throw new KubernetesException("Error integrating with kubernetes.", e);
        }
        return contexts;
    }

    /**
     * Read all the kubernetes services.
     * 
     * @return a list of services
     * @throws KubernetesException thrown if the services can't be read
     */
    public List<KubernetesService> readServices() throws KubernetesException {
        List<KubernetesService> services = new ArrayList<>();
        try {
            // Create the process
            log.info("Reading kubernetes services.");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command("kubectl", "get", "services", "-o", "json");
            Process process = processBuilder.start();

            // Read in the result
            StringBuilder builder = new StringBuilder();
            try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        builder.append(line.trim());
                    }
                }
            }

            // Wait for the process to complete
            Boolean success = process.waitFor(5, TimeUnit.SECONDS);
            if (!success) {
                throw new RuntimeException("Can't read kubernetes services.");
            }

            // Parse the service objects from the JSON
            JsonNode response = new ObjectMapper().readValue(builder.toString(), JsonNode.class);
            ArrayNode serviceNodes = (ArrayNode) response.findPath("items");
            for (JsonNode serviceNode : serviceNodes) {
                KubernetesService service = new KubernetesService();
                service.setName(serviceNode.findPath("metadata").findPath("name").asText("N/A"));
                service.setNamespace(serviceNode.findPath("metadata").findPath("namespace").asText("N/A"));
                services.add(service);
            }
        } catch (Exception e) {
            throw new KubernetesException("Error integrating with kubernetes.", e);
        }
        return services;
    }

    /**
     * Build the forward ports process. The process is not started in this method.
     * 
     * @param serviceConfiguration the service configuration
     * @return the process builder
     */
    public ProcessBuilder buildForwardPortsProcess(ServiceConfiguration serviceConfiguration) {
        // Create the process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);

        // Build the command
        List<String> command = new ArrayList<>(Arrays.asList("kubectl", "port-forward", "-n", serviceConfiguration.getServiceNamespace(),
                "service/" + serviceConfiguration.getServiceName()));
        command.addAll(formatPorts(serviceConfiguration.getPorts()));
        processBuilder.command(command);
        return processBuilder;
    }

    /**
     * Format the given list of ports.
     * 
     * @param ports the ports
     * @return a formatted string
     */
    private List<String> formatPorts(List<PortConfiguration> ports) {
        List<String> formattedPorts = new ArrayList<>();
        for (PortConfiguration port : ports) {
            formattedPorts.add(port.getLocal() + ":" + port.getRemote());
        }
        return formattedPorts;
    }

}
