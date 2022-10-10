package net.gudenau.launcher.ui.renderer;

import net.gudenau.launcher.api.util.LanguageManager;

import javax.swing.*;
import java.awt.*;

public final class UserCellRenderer extends JLabel implements ListCellRenderer<Object> {
    public UserCellRenderer() {
        setHorizontalAlignment(RIGHT);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(String.valueOf(value));
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        return this;
    }
}
