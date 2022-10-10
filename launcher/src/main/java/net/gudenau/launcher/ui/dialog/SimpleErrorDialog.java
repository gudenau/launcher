package net.gudenau.launcher.ui.dialog;

import java.awt.*;

import static net.gudenau.launcher.ui.UiUtils.button;
import static net.gudenau.launcher.ui.UiUtils.label;

public final class SimpleErrorDialog extends Dialog {
    public SimpleErrorDialog(String key) {
        var constraints = new GridBagConstraints();
        constraints.gridy = 0;
        add(label(key), constraints);
        
        constraints.gridy++;
        add(button("ui.dialog.error.okay", this::close), constraints);
    }
}
