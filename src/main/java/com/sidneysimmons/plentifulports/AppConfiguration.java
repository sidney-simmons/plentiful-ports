package com.sidneysimmons.plentifulports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration.
 * 
 * @author Sidney Simmons
 */
@Configuration
public class AppConfiguration {

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}