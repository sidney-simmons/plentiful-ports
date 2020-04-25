package com.sidneysimmons.plentifulports.ui;

import com.sidneysimmons.plentifulports.ui.component.CustomMenuBar;
import com.sidneysimmons.plentifulports.ui.component.CustomWindowListener;
import com.sidneysimmons.plentifulports.ui.scene.GenericScene;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Manager class for working with the frame, the scenes, etc.
 * 
 * @author Sidney Simmons
 */
@Slf4j
@Component("frameManager")
public class FrameManager {

    @Resource(name = "customWindowListener")
    private CustomWindowListener customWindowListener;

    @Resource(name = "customMenuBar")
    private CustomMenuBar customMenuBar;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private JFrame frame;
    private GenericScene currentScene;
    private Map<Class<? extends GenericScene>, GenericScene> cachedScenes;

    /**
     * Initialize this manager.
     */
    @PostConstruct
    public void initialize() {
        // Initialize an empty cached scenes map
        cachedScenes = new HashMap<>();

        // Initialize the frame
        frame = new JFrame("Plentiful Ports");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.addWindowListener(customWindowListener);
        frame.setJMenuBar(customMenuBar);

        Dimension frameSize = new Dimension(800, 600);
        frame.setMinimumSize(frameSize);
        frame.setPreferredSize(frameSize);
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icon-32x32.png")));
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // Show the application
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Destroy this manager.
     */
    @PreDestroy
    public void destroy() {
        log.info("Shutting down cached scenes.");
        for (GenericScene cachedScene : cachedScenes.values()) {
            if (cachedScene == currentScene) {
                cachedScene.onHide();
            }
            cachedScene.onDestroy();
        }
        cachedScenes.clear();
    }

    /**
     * Close the application by simulating the window being closed.
     */
    public void closeApplication() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Repaint the application.
     */
    public void repaintApplication() {
        frame.validate();
        frame.repaint();
    }

    /**
     * Activate a given scene. Scenes are cached. Will create a new scene if it doesn't already exist.
     * 
     * @param sceneClass the scene class
     */
    public void activateScene(Class<? extends GenericScene> sceneClass) {
        log.info("Activating scene " + sceneClass.getSimpleName() + ".");

        // Use a cached scene or create a new scene
        if (!cachedScenes.containsKey(sceneClass)) {
            try {
                // Create a new scene of the given type and set the frame and root element
                GenericScene newScene = sceneClass.getDeclaredConstructor().newInstance();
                newScene.setFrame(frame);
                newScene.setRoot(new JPanel(new GridBagLayout()));

                // Inject any required spring beans
                beanFactory.autowireBean(newScene);

                // Call on create
                newScene.onCreate();
                cachedScenes.put(sceneClass, newScene);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new IllegalArgumentException("Scene class " + sceneClass + " can't be instantiated!");
            }
        }

        // Call hide hooks
        if (currentScene != null) {
            currentScene.onHide();
        }

        // Call show hooks
        GenericScene newScene = cachedScenes.get(sceneClass);
        currentScene = newScene;
        newScene.onShow();

        // Show the new scene
        frame.getContentPane().removeAll();
        frame.getContentPane().add(newScene.getRoot(), BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Return the scene with the given class.
     * 
     * @param <T> the type of class
     * @param sceneClass the type of class
     * @return the scene if it exists, null otherwise
     */
    @SuppressWarnings("unchecked")
    public <T extends GenericScene> T getScene(Class<T> sceneClass) {
        if (cachedScenes.containsKey(sceneClass)) {
            return (T) cachedScenes.get(sceneClass);
        } else {
            return null;
        }
    }

    /**
     * Show an error message. Invokes asynchronously via the swing utilities.
     * 
     * @param message the message
     * @param exception an exception (optional)
     * @param closeAction a close action (optional)
     */
    public void showErrorMessage(String message, Exception exception, Runnable closeAction) {
        SwingUtilities.invokeLater(() -> {
            // Build the content
            String content = message;
            if (exception != null) {
                content += " Please check the exception in the logs.";
            }

            // Show the dialog and possibly run the runnable
            JOptionPane.showOptionDialog(frame, content, "Error Occurred", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    null, null);
            if (closeAction != null) {
                closeAction.run();
            }
        });
    }

}
