package com.sidneysimmons.plentifulports.ui.component;

import com.sidneysimmons.plentifulports.settings.domain.PortConfiguration;
import com.sidneysimmons.plentifulports.settings.domain.ServiceConfiguration;
import com.sidneysimmons.plentifulports.ui.FrameManager;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Resource;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import org.springframework.stereotype.Component;

/**
 * Ports table singleton.
 * 
 * @author Sidney Simmons
 */
@Component("portsTable")
public class PortsTable extends JPanel {

    private static final long serialVersionUID = 1L;

    @Resource(name = "frameManager")
    private FrameManager frameManager;

    private JLabel emptyTableRow;
    private Map<ServiceConfiguration, List<JComponent>> currentServices = new HashMap<>();

    /**
     * Constructor.
     */
    public PortsTable() {
        super(new GridBagLayout());

        emptyTableRow = new JLabel("- no services -");
        emptyTableRow.setHorizontalAlignment(SwingConstants.CENTER);

        setBackground(Color.WHITE);
        setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        buildColumns();
        showEmptyTableRow();
    }

    /**
     * Add a given service to the table.
     * 
     * @param serviceConfiguration the service configuration
     * @param toggleHandler a toggle handler
     */
    public void addServiceToTable(ServiceConfiguration serviceConfiguration, ItemListener toggleHandler) {
        // Hide the empty table row if it's currently showing
        if (currentServices.isEmpty()) {
            hideEmptyTableRow();
        }

        // Add the service to the table
        JLabel serviceNameLabel = new JLabel(serviceConfiguration.getServiceName());
        JLabel serviceNamespaceLabel = new JLabel(serviceConfiguration.getServiceNamespace());
        JLabel servicePortsLabel = new JLabel(formatPorts(serviceConfiguration.getPorts()));
        JCheckBox serviceActiveLabel = new JCheckBox();
        serviceActiveLabel.addItemListener(toggleHandler);

        List<JComponent> components = Arrays.asList(serviceNameLabel, serviceNamespaceLabel, servicePortsLabel, serviceActiveLabel);
        currentServices.put(serviceConfiguration, components);

        // Grid Y is the # of current services + 1 to account for the header row
        int gridY = currentServices.size() + 1;
        add(serviceNameLabel, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, gridY, 1, 1, 0, 0, 1.0, 0.0));
        add(serviceNamespaceLabel, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 1, gridY, 1, 1, 0, 0, 1.0, 0.0));
        add(servicePortsLabel, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 2, gridY, 1, 1, 0, 0, 1.0, 0.0));
        add(serviceActiveLabel, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTH, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 3, gridY, 1, 1, 0, 0, 0.0, 0.0));

        // Repaint
        frameManager.repaintApplication();
    }

    /**
     * Clear the ports table.
     */
    public void clearTable() {
        for (Entry<ServiceConfiguration, List<JComponent>> entry : currentServices.entrySet()) {
            for (JComponent component : entry.getValue()) {
                remove(component);
            }
        }
        currentServices.clear();
        showEmptyTableRow();

        // Repaint
        frameManager.repaintApplication();
    }

    /**
     * Set the toggle for a given service.
     * 
     * @param serviceConfiguration the service configuration
     * @param checked whether or not the toggle should be checked
     */
    public void setToggle(ServiceConfiguration serviceConfiguration, Boolean checked) {
        if (currentServices.containsKey(serviceConfiguration)) {
            List<JComponent> components = currentServices.get(serviceConfiguration);
            JCheckBox toggle = (JCheckBox) components.get(3);
            toggle.setSelected(checked);
        }
    }

    /**
     * Show the empty table row.
     */
    private void showEmptyTableRow() {
        hideEmptyTableRow();
        add(emptyTableRow, ComponentHelper.gridBagConstraints(GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(10, 10, 10, 10), 0, 1, 4, 1, 0, 0, 1.0, 0.0));
    }

    /**
     * Hide the empty table row.
     */
    private void hideEmptyTableRow() {
        remove(emptyTableRow);
    }

    /**
     * Build the columns.
     */
    private void buildColumns() {
        JLabel serviceNameColumn = ComponentHelper.underlinedLabel("Service", CustomFont.BOLD);
        add(serviceNameColumn, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0, 1, 1, 0, 0, 1.0, 0.0));

        JLabel serviceNamespaceColumn = ComponentHelper.underlinedLabel("Namespace", CustomFont.BOLD);
        add(serviceNamespaceColumn, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 1, 0, 1, 1, 0, 0, 1.0, 0.0));

        JLabel servicePortsColumn = ComponentHelper.underlinedLabel("Ports", CustomFont.BOLD);
        add(servicePortsColumn, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 2, 0, 1, 1, 0, 0, 1.0, 0.0));

        JLabel serviceActiveColumn = ComponentHelper.underlinedLabel("Active", CustomFont.BOLD);
        add(serviceActiveColumn, ComponentHelper.gridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 3, 0, 1, 1, 0, 0, 0.0, 0.0));
    }

    /**
     * Format the given list of ports.
     * 
     * @param ports the ports
     * @return a formatted string
     */
    private String formatPorts(List<PortConfiguration> ports) {
        StringBuilder builder = new StringBuilder();
        for (PortConfiguration port : ports) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(port.getLocal() + ":" + port.getRemote());
        }
        return builder.toString();
    }

}
