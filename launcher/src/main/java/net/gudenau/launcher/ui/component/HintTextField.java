package net.gudenau.launcher.ui.component;

import javax.swing.*;
import java.awt.*;

// From https://stackoverflow.com/a/24571681
public final class HintTextField extends JTextField {
    private final String hint;
    
    public HintTextField(String hint) {
        this.hint = hint;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (getText().isEmpty() && g instanceof Graphics2D g2d) {
            var height = getHeight();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var insets = getInsets();
            var fontMetrics = g2d.getFontMetrics();
            int background = getBackground().getRGB();
            int foreground = getForeground().getRGB();
            int mask = 0xFEFEFEFE;
            int color = ((background & mask) >>> 1) + ((foreground & mask) >>> 1);
            g2d.setColor(new Color(color, true));
            g2d.drawString(hint, insets.left, (height + fontMetrics.getAscent()) / 2 - 2);
        }
    }
}
