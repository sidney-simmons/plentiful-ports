package com.sidneysimmons.plentifulports.ui.scene;

import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.Data;

/**
 * Generic scene. Meant to be extended by more specific scenes.
 * 
 * @author Sidney Simmons
 */
@Data
public abstract class GenericScene {

    private JFrame frame;
    private JPanel root;

    /**
     * Called once when the scene is first created.
     */
    public abstract void onCreate();

    /**
     * Called once when the scene is destroyed.
     */
    public abstract void onDestroy();

    /**
     * Called every time the scene is shown.
     */
    public abstract void onShow();

    /**
     * Called every time the scene is hidden.
     */
    public abstract void onHide();

}
