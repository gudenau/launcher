package net.gudenau.launcher.ui;

import net.gudenau.launcher.impl.util.ThreadUtil;

import javax.swing.*;
import java.awt.*;

public final class LoadingScreen {
    public static void show() {
        ThreadUtil.waitForUi(LoadingScreen::doShow);
    }
    
    private static JFrame frame;
    private static JProgressBar progressBar;
    
    private static void doShow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable ignored) {}
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        frame = new JFrame("Loading...");
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(new JPanel(new BorderLayout()) {
            {
                setBackground(new Color(0, 0, 0, 0));
                add(new JLabel("Loading..."), BorderLayout.CENTER);
                add(progressBar, BorderLayout.SOUTH);
            }
            
            @Override
            public void paintComponent(Graphics g) {
                Object antiAliasing = null;
                if (g instanceof Graphics2D g2d) {
                    var aa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                    if (aa != RenderingHints.VALUE_ANTIALIAS_ON) {
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        antiAliasing = aa;
                    }
                }
                
                g.setColor(new Color(255, 255, 255));
                g.fillOval(0, 0, getWidth(), getHeight());
                
                if (antiAliasing != null) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing);
                }
                
                super.paintComponent(g);
            }
        });
        UiUtils.center(frame, null);
        frame.setVisible(true);
    }
    
    static void destroy(Window window) {
        UiUtils.center(window, frame);
        frame.setVisible(false);
        window.setVisible(true);
        frame.dispose();
    }
    
    public static void setMaxProgress(int size) {
        ThreadUtil.submitUi(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(size);
        });
    }
    
    public static void incrementProgress() {
        ThreadUtil.submitUi(() ->
            progressBar.setValue(progressBar.getValue() + 1)
        );
    }
}
