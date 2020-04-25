package com.sidneysimmons.plentifulports;

import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Initializes and runs the application.
 *
 * @author Sidney Simmons
 */
@Slf4j
public class AppInitializer {

    /**
     * Entry point of the application.
     * 
     * @param args the input args
     */
    public static void main(String[] args) {
        // Log some startup information
        logStartupDetails();

        // Configure the environment
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.sidneysimmons");

        // Run the application
        AppRunner appRunner = (AppRunner) context.getBean("appRunner");
        SwingUtilities.invokeLater(appRunner::run);
    }

    /**
     * Log the startup details. Meant to be used for troubleshooting.
     */
    private static void logStartupDetails() {
        log.info("Application started.");
        log.info("User home: " + System.getProperty("user.home"));
    }

}
