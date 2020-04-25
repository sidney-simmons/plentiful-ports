package com.sidneysimmons.plentifulports.ui.component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextArea;
import org.springframework.stereotype.Component;

/**
 * Custom text area for showing a ports "monitor". Sort of like a real-time log.
 * 
 * @author Sidney Simmons
 */
@Component("portsMonitor")
public class PortsMonitor extends JTextArea {

    private static final long serialVersionUID = 1L;

    private StringBuilder currentLines = new StringBuilder();
    private Integer maximumLineCount = 100;
    private Integer currentLineCount = 0;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    /**
     * Constructor.
     */
    public PortsMonitor() {
        setFont(CustomFont.MONOSPACE);
    }

    /**
     * Add a message to the monitor.
     * 
     * @param message the message
     */
    public synchronized void addMessage(String message) {
        // Delete the oldest line if we're at the max
        if (currentLineCount >= maximumLineCount) {
            currentLines.delete(0, currentLines.indexOf("\n") + 1);
        } else {
            currentLineCount++;
        }

        // Append the message
        if (currentLines.length() > 0) {
            currentLines.append("\n");
        }
        Integer beginningOfLatestLine = currentLines.length();
        currentLines.append(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(dateTimeFormatter) + " - " + message);

        // Update the text area
        setText(currentLines.toString());
        setCaretPosition(beginningOfLatestLine);
    }

}
