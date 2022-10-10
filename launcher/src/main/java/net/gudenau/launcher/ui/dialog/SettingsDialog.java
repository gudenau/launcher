package net.gudenau.launcher.ui.dialog;

import net.gudenau.launcher.impl.profile.Profile;
import net.gudenau.launcher.impl.util.Configuration;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static net.gudenau.launcher.ui.UiUtils.*;

public final class SettingsDialog extends Dialog {
    private final Map<Configuration<?>, Component> globalSettings = new HashMap<>();
    private final Profile profile;
    
    private boolean abort = false;
    
    public SettingsDialog(Profile profile) {
        this.profile = profile;
        
        var constraints = new GridBagConstraints();
        
        constraints.weighty = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(createProfilePanel(), constraints);
        
        constraints.gridy = 1;
        add(createGlobalPanel(), constraints);
        
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        add(button("launcher.ui.dialog.settings.apply", this::close), constraints);
    
        constraints.gridx = 1;
        add(button("launcher.ui.dialog.settings.cancel", () -> {
            abort = true;
            close();
        }), constraints);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onClose() {
        if (abort) {
            return;
        }
        
        globalSettings.forEach((config, component) -> {
            if (component instanceof JCheckBox checkBox) {
                ((Configuration<Boolean>) config).set(checkBox.isSelected());
            } else if (component instanceof JSpinner spinner) {
                if (spinner.getModel() instanceof SpinnerNumberModel model) {
                    ((Configuration<Integer>) config).set(model.getNumber().intValue());
                }
            }
        });
    }
    
    private JPanel createProfilePanel() {
        var panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(border("launcher.ui.dialog.settings.profile"));
        
        panel.add(button("launcher.ui.dialog.settings.profile.accounts", () -> pushDialog(new AccountSettingsDialog(profile))));
        
        return panel;
    }
    
    private JPanel createGlobalPanel() {
        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(border("launcher.ui.dialog.settings.global"));
    
        var constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        
        Configuration.values().stream()
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .forEachOrdered((config) -> {
                var value = config.get();
                constraints.gridx = 0;
                constraints.anchor = GridBagConstraints.WEST;
                panel.add(label("launcher.setting." + config.name()), constraints);
                
                Component component;
                if (value instanceof Boolean bool) {
                    component = new JCheckBox((String) null, bool);
                } else if (value instanceof Integer integer) {
                    component = new JSpinner(new SpinnerNumberModel(integer.intValue(), 0, 9999, 1));
                } else {
                    component = new JLabel(String.valueOf(value));
                }
                
                constraints.gridx = 1;
                constraints.anchor = GridBagConstraints.EAST;
                panel.add(component, constraints);
                globalSettings.put(config, component);
                
                constraints.gridy++;
            });
        
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(label("launcher.ui.dialog.settings.global_warning"), constraints);
        
        return panel;
    }
}
