package net.gudenau.launcher.ui.dialog;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class Dialog extends JPanel {
    final JPanel container = new JPanel(new GridBagLayout());
    private DialogPanel panel;
    
    protected Dialog() {
        super(new GridBagLayout());
        var constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        super.add(container, constraints);
        setOpaque(false);
    }
    
    public final void onAdded(DialogPanel panel) {
        this.panel = panel;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    panel.popDialog(Dialog.this);
                }
            }
        });
    }
    
    public void onClose() {}
    
    protected void close() {
        panel.popDialog(this);
    }
    
    protected void pushError(@NotNull String key) {
        panel.pushError(key);
    }
    
    protected void pushDialog(Dialog dialog) {
        panel.pushDialog(dialog);
    }
    
    @Override
    public Component add(Component comp) {
        return container.add(comp);
    }
    
    @Override
    public Component add(Component comp, int index) {
        return container.add(comp, index);
    }
    
    @Override
    public Component add(String name, Component comp) {
        return container.add(name, comp);
    }
    
    @Override
    public void add(PopupMenu popup) {
        container.add(popup);
    }
    
    @Override
    public void add(@NotNull Component comp, Object constraints) {
        container.add(comp, constraints);
    }
    
    protected <T extends Component> T add(@NotNull T component, GridBagConstraints constraints) {
        container.add(component, constraints);
        return component;
    }
    
    @Override
    public void add(Component comp, Object constraints, int index) {
        container.add(comp, constraints, index);
    }
}
