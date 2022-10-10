package net.gudenau.launcher.ui;

import net.gudenau.launcher.api.util.LanguageManager;
import net.gudenau.launcher.ui.component.HintTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class UiUtils {
    public static void center(@NotNull Window window, @Nullable Window parent) {
        Rectangle bounds;
        if (parent == null) {
            bounds = MouseInfo.getPointerInfo().getDevice().getDefaultConfiguration().getBounds();
        } else {
            bounds = parent.getBounds();
        }
        
        window.setLocation(
            bounds.x + (bounds.width - window.getWidth()) / 2,
            bounds.y + (bounds.height - window.getHeight()) / 2
        );
    }
    
    public static JLabel label(String translationKey) {
        return new JLabel(LanguageManager.get().translate(translationKey));
    }
    
    public static JButton button(String labelKey, Runnable action) {
        var button = new JButton(LanguageManager.get().translate(labelKey));
        button.addActionListener((e) -> action.run());
        return button;
    }
    
    public static JTextField textField(@Nullable String hint, Runnable action) {
        var field = hint == null ? new JTextField() : new HintTextField(LanguageManager.get().translate(hint));
        field.addActionListener((e) -> action.run());
        return field;
    }
    
    public static Border border(String label) {
        return BorderFactory.createTitledBorder(LanguageManager.get().translate(label));
    }
}
