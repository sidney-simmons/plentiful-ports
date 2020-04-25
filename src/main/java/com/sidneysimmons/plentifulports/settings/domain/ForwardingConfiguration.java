package com.sidneysimmons.plentifulports.settings.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forwarding configuration.
 * 
 * @author Sidney Simmons
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForwardingConfiguration {

    private List<ServiceConfiguration> services = new ArrayList<>();

}
