package com.sidneysimmons.plentifulports.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidneysimmons.plentifulports.settings.domain.ForwardingConfiguration;
import com.sidneysimmons.plentifulports.settings.domain.PortConfiguration;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import com.sidneysimmons.plentifulports.settings.domain.Settings;
import com.sidneysimmons.plentifulports.settings.domain.SettingsValidity;
import com.sidneysimmons.plentifulports.settings.exception.SettingsException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Service for working with settings.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("settingsService")
public final class SettingsService {

    @Resource(name = "objectMapper")
    private ObjectMapper objectMapper;

    /**
     * Check if the settings file exists.
     * 
     * @return true if the settings file exists, false otherwise
     */
    public Boolean settingsFileExists() {
        return resolveSettingsFile().exists();
    }

    /**
     * Check if the settings file is readable and writable.
     * 
     * @return true if the settings file is readable and writable, false otherwise
     */
    public Boolean settingsFileIsReadableAndWritable() {
        File settingsFile = resolveSettingsFile();
        return settingsFile.canRead() && settingsFile.canWrite();
    }

    /**
     * Check if the settings are valid. This means they can be read from the file, parsed into an object, and the object is valid.
     * 
     * @return true if the settings are valid, false otherwise
     */
    public Boolean settingsAreValid() {
        Boolean settingsAreValid = false;
        try {
            Settings settings = readSettingsObject();
            settingsAreValid = validateSettingsObject(settings).getValid();
        } catch (SettingsException e) {
            // Nothing to do here
        }
        return settingsAreValid;
    }

    /**
     * Check if the settings object is valid. This checks the services, ports, etc.
     * 
     * @param settingsObject the settings object
     * @return a settings validity result
     */
    public SettingsValidity validateSettingsObject(Settings settingsObject) {
        if (settingsObject == null) {
            return new SettingsValidity(false, "Settings object is null.");
        }

        ForwardingConfiguration forwardingConfiguration = settingsObject.getForwardingConfiguration();
        if (forwardingConfiguration == null) {
            return new SettingsValidity(false, "Forwarding configuration is null.");
        }

        List<ServiceConfiguration> services = forwardingConfiguration.getServices();
        if (CollectionUtils.isEmpty(services)) {
            return new SettingsValidity(false, "List of forwarding services is null or empty.");
        }

        for (ServiceConfiguration service : services) {
            if (StringUtils.isBlank(service.getServiceName())) {
                return new SettingsValidity(false, "Service name is null, empty, or blank.");
            }

            if (StringUtils.isBlank(service.getServiceNamespace())) {
                return new SettingsValidity(false, "Service namespace is null, empty, or blank.");
            }

            List<PortConfiguration> forwardingPorts = service.getPorts();
            if (CollectionUtils.isEmpty(forwardingPorts)) {
                return new SettingsValidity(false, "List of forwarding ports is null or empty.");
            }

            for (PortConfiguration forwardingPort : forwardingPorts) {
                try {
                    Integer.parseInt(forwardingPort.getLocal());
                } catch (NumberFormatException e) {
                    return new SettingsValidity(false, "Local forwarding port [" + forwardingPort.getLocal() + "] isn't a valid integer.");
                }

                try {
                    Integer.parseInt(forwardingPort.getRemote());
                } catch (NumberFormatException e) {
                    return new SettingsValidity(false,
                            "Remote forwarding port [" + forwardingPort.getRemote() + "] isn't a valid integer.");
                }
            }
        }
        return new SettingsValidity(true, null);
    }

    /**
     * Read the settings object.
     * 
     * @return the settings object
     * @throws SettingsException thrown if the settings can't be read
     */
    public Settings readSettingsObject() throws SettingsException {
        String settingsString = readSettingsString();
        try {
            return objectMapper.readValue(settingsString, Settings.class);
        } catch (IOException e) {
            throw new SettingsException("Can't read the settings object.", e);
        }
    }

    /**
     * Read the settings string.
     * 
     * @return the settings string
     * @throws SettingsException thrown if the settings can't be read
     */
    public String readSettingsString() throws SettingsException {
        File settingsFile = resolveSettingsFile();
        try {
            return FileUtils.readFileToString(settingsFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SettingsException("Can't read the settings string.", e);
        }
    }

    /**
     * Write the settings object to disk. Creates the file and the parent directories if they don't exist.
     * 
     * @param settingsObject the settings object
     * @throws SettingsException thrown if the settings can't be written
     */
    public void writeSettingsObject(Settings settingsObject) throws SettingsException {
        File settingsFile = resolveSettingsFile();
        log.info("Writing settings to " + settingsFile.getAbsolutePath() + ".");
        settingsFile.getParentFile().mkdirs();
        try {
            settingsFile.createNewFile();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, settingsObject);
        } catch (IOException e) {
            throw new SettingsException("Can't write the settings object.", e);
        }
    }

    /**
     * Format the given settings object into a settings string.
     * 
     * @param settingsObject the settings object
     * @return the settings string
     * @throws RuntimeException thrown if the settings can't be formatted - this should never happen
     */
    public String formatSettings(Settings settingsObject) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(settingsObject);
        } catch (JsonProcessingException e) {
            log.error("Can't format the given settings object - this should never happen.", e);
            throw new RuntimeException("Can't format the given settings object - this should never happen.", e);
        }
    }

    /**
     * Parse the settings string into a settings object.
     * 
     * @param settingsString the settings string
     * @return the settings object
     * @throws SettingsException if the settings string can't be parsed
     */
    public Settings parseSettings(String settingsString) throws SettingsException {
        try {
            return objectMapper.readValue(settingsString, Settings.class);
        } catch (IOException e) {
            throw new SettingsException("Can't parse the settings string.", e);
        }
    }

    /**
     * Build a default settings object for when a user doesn't already have one. Meant to be used as an example.
     * 
     * @return the default settings
     */
    public Settings buildDefaultSettingsObject() {
        ServiceConfiguration springBootService = new ServiceConfiguration();
        springBootService.setServiceName("spring-boot-service");
        springBootService.setServiceNamespace("default");
        springBootService.getPorts().add(new PortConfiguration("9090", "9090"));

        ServiceConfiguration zookeeperService = new ServiceConfiguration();
        zookeeperService.setServiceName("zookeeper-service");
        zookeeperService.setServiceNamespace("default");
        zookeeperService.getPorts().add(new PortConfiguration("2181", "2181"));
        zookeeperService.getPorts().add(new PortConfiguration("2888", "2888"));
        zookeeperService.getPorts().add(new PortConfiguration("8080", "8080"));

        ServiceConfiguration postgresService = new ServiceConfiguration();
        postgresService.setServiceName("postgres-service");
        postgresService.setServiceNamespace("default");
        postgresService.getPorts().add(new PortConfiguration("5432", "5432"));

        ForwardingConfiguration forwardingConfiguration = new ForwardingConfiguration();
        forwardingConfiguration.getServices().add(springBootService);
        forwardingConfiguration.getServices().add(zookeeperService);
        forwardingConfiguration.getServices().add(postgresService);

        Settings defaultSettings = new Settings();
        defaultSettings.setForwardingConfiguration(forwardingConfiguration);
        return defaultSettings;
    }

    /**
     * Resolve the settings file. The file is located in the user's home directory within a ".plentiful-ports" directory. This will return the
     * file object even if the actual file doesn't exist.
     * 
     * @return the settings file
     */
    public File resolveSettingsFile() {
        return new File(System.getProperty("user.home") + "/.plentiful-ports/settings.json");
    }

}
