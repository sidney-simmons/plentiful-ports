package com.sidneysimmons.plentifulports.settings.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Settings validity.
 * 
 * @author Sidney Simmons
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingsValidity {

    private Boolean valid;
    private String message;

    public SettingsValidity(Boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

}
