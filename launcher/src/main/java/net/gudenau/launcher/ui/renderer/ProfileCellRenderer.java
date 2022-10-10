package net.gudenau.launcher.ui.renderer;

import net.gudenau.launcher.impl.profile.Profile;

import javax.swing.*;
import java.awt.*;

public final class ProfileCellRenderer extends JLabel implements ListCellRenderer<Profile> {
    public ProfileCellRenderer() {
        setHorizontalAlignment(RIGHT);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends Profile> list, Profile value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value != null) {
            setText(value.name());
        } else {
            setText(null);
        }
    
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
