package com.sidneysimmons.plentifulports.ui.component;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Custom window listener used to hook into the window status.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("customWindowListener")
public class CustomWindowListener extends WindowAdapter {

    @Autowired
    private AbstractApplicationContext context;

    @Override
    public void windowClosing(WindowEvent e) {
        // Tell spring to shutdown the beans
        context.close();
        log.info("Application stopped.");
    }

}
