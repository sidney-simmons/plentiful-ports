package com.sidneysimmons.plentifulports.settings.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Settings.
 * 
 * @author Sidney Simmons
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {

    private ForwardingConfiguration forwardingConfiguration;

}
