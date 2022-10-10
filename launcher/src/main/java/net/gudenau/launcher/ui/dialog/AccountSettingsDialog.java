package net.gudenau.launcher.ui.dialog;

import net.gudenau.launcher.impl.profile.Profile;

public class AccountSettingsDialog extends Dialog {
    private final Profile profile;
    
    public AccountSettingsDialog(Profile profile) {
        this.profile = profile;
    }
}
