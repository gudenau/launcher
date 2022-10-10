package net.gudenau.launcher.ui.dialog;

import net.gudenau.launcher.impl.profile.ProfileManager;

import javax.swing.*;
import java.awt.*;

import static net.gudenau.launcher.ui.UiUtils.*;

public final class AddProfileDialog extends Dialog {
    private final JTextField profileName;
    
    public AddProfileDialog() {
        var constraints = new GridBagConstraints();
        
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(label("launcher.ui.dialog.profile_add.name"), constraints);
        
        constraints.gridy++;
        profileName = add(textField("launcher.ui.dialog.profile_add.name_hint", this::addProfile), constraints);
        
        constraints.gridy++;
        constraints.gridwidth = 1;
        add(button("launcher.ui.dialog.profile_add.confirm", this::addProfile), constraints);
    
        constraints.gridx++;
        add(button("launcher.ui.dialog.profile_add.cancel", this::close), constraints);
    }
    
    private void addProfile() {
        var name = profileName.getText();
        if (name.isBlank()) {
            pushError("launcher.ui.dialog.profile_add.error_name");
            return;
        }
    
        var profile = ProfileManager.createProfile(name);
        if (profile.isEmpty()) {
            pushError("launcher.ui.dialog.profile_add.error_failed");
        } else {
            close();
        }
    }
}
