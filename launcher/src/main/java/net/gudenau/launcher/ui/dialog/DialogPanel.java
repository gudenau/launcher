package net.gudenau.launcher.ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public final class DialogPanel extends JPanel {
    private static final Color COLOR_SHADED = new Color(0, 0, 0, 127);
    
    private final MouseListener mouseAdapter = new MouseAdapter() {};
    private final KeyListener keyAdapter = new KeyAdapter() {};
    private final Stack<Dialog> dialogs = new Stack<>();
    
    private final Container sibling;
    private boolean swallow = false;
    private Map<Component, Boolean> siblingState = Map.of();
    
    public DialogPanel(Container sibling) {
        super(new BorderLayout());
        
        this.sibling = sibling;
        
        setOpaque(false);
    }
    
    private void swallow(boolean swallow) {
        if (this.swallow == swallow) {
            return;
        }
        this.swallow = swallow;
        
        if (swallow) {
            addMouseListener(mouseAdapter);
            addKeyListener(keyAdapter);
            siblingState = disable(sibling);
        } else {
            removeMouseListener(mouseAdapter);
            removeKeyListener(keyAdapter);
            siblingState.forEach(Component::setEnabled);
        }
    }
    
    private Map<Component, Boolean> disable(Component component) {
        var map = new HashMap<Component, Boolean>();
        disable(map, component);
        return Collections.unmodifiableMap(map);
    }
    
    private void disable(HashMap<Component, Boolean> map, Component component) {
        map.put(component, component.isEnabled());
        component.setEnabled(false);
        if (component instanceof Container container) {
            for (var child : container.getComponents()) {
                disable(map, child);
            }
        }
    }
    
    public void pushDialog(Dialog container) {
        container.onAdded(this);
        
        for (var dialog : dialogs) {
            dialog.setVisible(false);
        }
        dialogs.push(container);
        add(container, BorderLayout.CENTER);
        swallow(true);
        invalidate();
        repaint();
        wiggle();
    }
    
    public void popDialog(Dialog dialog) {
        if (!dialogs.isEmpty() && dialogs.peek() == dialog) {
            popDialog();
        }
    }
    
    public void popDialog() {
        if (!dialogs.isEmpty()) {
            dialogs.pop().onClose();
            removeAll();
            if (!dialogs.isEmpty()) {
                var dialog = dialogs.peek();
                dialog.setVisible(true);
                add(dialog);
            } else {
                swallow(false);
            }
            invalidate();
            repaint();
            wiggle();
        }
    }
    
    //FIXME This is stupid.
    private void wiggle() {
        var frame = (JFrame) getRootPane().getParent();
        var width = frame.getWidth();
        var height = frame.getHeight();
        frame.setSize(width + 1, height);
        frame.setSize(width, height);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (!dialogs.isEmpty()) {
            for (int i = 0, dialogsSize = dialogs.size() - 1; i < dialogsSize; i++) {
                var container = dialogs.get(i).container;
                g.translate(container.getX(), container.getY());
                try {
                    container.paint(g);
                } finally {
                    g.translate(-container.getX(), -container.getY());
                }
            }
    
            var color = g.getColor();
            g.setColor(COLOR_SHADED);
            var bounds = getBounds();
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setColor(color);
        }
    }
    
    public void pushError(String key) {
        pushDialog(new SimpleErrorDialog(key));
    }
}
