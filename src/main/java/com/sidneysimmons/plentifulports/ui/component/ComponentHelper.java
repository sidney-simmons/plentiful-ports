package com.sidneysimmons.plentifulports.ui.component;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.JLabel;

/**
 * Helper methods for working with UI components.
 * 
 * @author Sidney Simmons
 */
public class ComponentHelper {

    /**
     * Private constructor.
     */
    private ComponentHelper() {
        // No need to instantiate this
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static JLabel underlinedLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
        return label;
    }

    /**
     * Build a set of grid bag constraints.
     * 
     * @param anchor the anchor
     * @param fill the fill
     * @param insets the insets
     * @param gridX the gridX
     * @param gridY the gridY
     * @param gridWidth the gridWidth
     * @param gridHeight the gridHeight
     * @param ipadX the ipadX
     * @param ipadY the ipadY
     * @param weightX the weightX
     * @param weightY the weightY
     * @return the grid bag constraints
     */
    public static GridBagConstraints gridBagConstraints(Integer anchor, Integer fill, Insets insets, Integer gridX, Integer gridY,
            Integer gridWidth, Integer gridHeight, Integer ipadX, Integer ipadY, Double weightX, Double weightY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        constraints.gridwidth = gridWidth;
        constraints.gridheight = gridHeight;
        constraints.ipadx = ipadX;
        constraints.ipady = ipadY;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        return constraints;
    }

}
