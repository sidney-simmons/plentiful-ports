package com.sidneysimmons.plentifulports.settings.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forwarding port.
 * 
 * @author Sidney Simmons
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortConfiguration {

    public PortConfiguration(String local, String remote) {
        this.local = local;
        this.remote = remote;
    }

    private String local;
    private String remote;

}
