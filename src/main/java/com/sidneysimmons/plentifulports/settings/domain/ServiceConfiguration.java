package com.sidneysimmons.plentifulports.settings.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forwarding service.
 * 
 * @author Sidney Simmons
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfiguration {

    private String serviceName;
    private String serviceNamespace;
    private List<PortConfiguration> ports = new ArrayList<>();

    @Override
    public String toString() {
        return serviceName + " (" + serviceNamespace + ")";
    }

}
