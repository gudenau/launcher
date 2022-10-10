package net.gudenau.launcher.ui;

import net.gudenau.launcher.api.util.LanguageManager;
import net.gudenau.launcher.impl.profile.Profile;
import net.gudenau.launcher.impl.profile.ProfileManager;
import net.gudenau.launcher.impl.util.Configuration;
import net.gudenau.launcher.impl.util.ThreadUtil;
import net.gudenau.launcher.ui.dialog.AddProfileDialog;
import net.gudenau.launcher.ui.dialog.Dialog;
import net.gudenau.launcher.ui.dialog.DialogPanel;
import net.gudenau.launcher.ui.dialog.SettingsDialog;
import net.gudenau.launcher.ui.renderer.ProfileCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

import static net.gudenau.launcher.ui.UiUtils.button;

public final class MainScreen {
    public static void init() {
        ThreadUtil.submitUi(() -> LoadingScreen.destroy(new MainScreen().frame));
    }
    
    private final JFrame frame = new JFrame("Launcher");
    private final DialogPanel dialogPanel;
    private final JPanel contentPanel;
    
    private MainScreen() {
        frame.setSize(640, 480);
        frame.setMinimumSize(frame.getSize());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setupTrayIcon();
        
        contentPanel = createContentPanel();
        dialogPanel = new DialogPanel(contentPanel);
        
        frame.setContentPane(new JLayeredPane(){{
            add(dialogPanel);
            add(contentPanel);
            
            moveToFront(dialogPanel);
            
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    layout();
                }
    
                @Override
                public void componentMoved(ComponentEvent e) {
                    layout();
                }
    
                @Override
                public void componentShown(ComponentEvent e) {
                    layout();
                }
    
                @Override
                public void componentHidden(ComponentEvent e) {
                    layout();
                }
                
                private void layout() {
                    var bounds = getBounds();
                    for (var component : getComponents()) {
                        component.setBounds(bounds);
                    }
                }
            });
        }});
        
        if (ProfileManager.profiles().isEmpty()) {
            pushDialog(new AddProfileDialog());
        }
    }
    
    private JPanel createContentPanel() {
        var root = new JPanel(new GridBagLayout());
        
        var constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        root.add(createMenuPanel(), constraints);
    
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        root.add(createGamePanel(), constraints);
    
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 2;
        constraints.weighty = 1;
        root.add(createInfoPanel(), constraints);
        
        return root;
    }
    
    private JPanel createGamePanel() {
        var panel = new JPanel(new BorderLayout());
        
        var root = new DefaultMutableTreeNode();
        var mc = new DefaultMutableTreeNode("Minecraft");
        root.add(mc);
        mc.add(new DefaultMutableTreeNode("Vanilla"));
        mc.add(new DefaultMutableTreeNode("Fabric"));
        
        var model = new DefaultTreeModel(root);
        var tree = new JTree(model);
        tree.setRootVisible(false);
        
        var scrollPane = new JScrollPane(tree);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(tree, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createInfoPanel() {
        var panel = new JPanel();
        panel.setBackground(Color.RED);
        return panel;
    }
    
    private JPanel createMenuPanel() {
        var panel = new JPanel(new GridBagLayout());
        
        var constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.weightx = 1;
        JComboBox<Profile> profileBox = new JComboBox<>(ProfileManager.model());
        profileBox.setRenderer(new ProfileCellRenderer());
        panel.add(profileBox, constraints);
    
        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(button("launcher.ui.profile_add", () -> pushDialog(new AddProfileDialog())), constraints);
        
        constraints.gridx = 3;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(button("launcher.ui.settings", () -> {
            var profile = profileBox.getModel().getSelectedItem();
            if (profile == null || profile instanceof Profile) {
                pushDialog(new SettingsDialog((Profile) profile));
            }
        }), constraints);
        
        return panel;
    }
    
    public void pushDialog(Dialog container) {
        dialogPanel.pushDialog(container);
    }
    
    public void popDialog() {
        dialogPanel.popDialog();
    }
    
    public void pushErrorDialog(String key) {
        dialogPanel.pushError(key);
    }
    
    private void setupTrayIcon() {
        if (Configuration.DISABLE_HIDING.get()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return;
        }
        
        if (!SystemTray.isSupported()) {
            var state = new Object() {
                boolean controlDown = false;
            };
            
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.isControlDown()) {
                        state.controlDown = true;
                    }
                }
    
                @Override
                public void keyReleased(KeyEvent e) {
                    if (!e.isControlDown()) {
                        state.controlDown = false;
                    }
                }
            });
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (state.controlDown) {
                        frame.dispose();
                    } else {
                        frame.setState(JFrame.ICONIFIED);
                    }
                }
            });
            return;
        }
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
            }
        });
        
        var menu = new PopupMenu();
        BiConsumer<String, Runnable> adder = (label, action) -> {
            var item = new MenuItem(label);
            item.addActionListener((e) -> action.run());
            menu.add(item);
        };
        Runnable show = () -> {
            if (frame.isVisible()) {
                return;
            }
            UiUtils.center(frame, null);
            frame.setVisible(true);
        };
        adder.accept("Show", show);
        adder.accept("Exit", () -> System.exit(0));
        
        var icon = new TrayIcon(new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR), "Launcher");
        icon.addActionListener((e) -> show.run());
        icon.setPopupMenu(menu);
        
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException e) {
            throw new RuntimeException("Failed to create tray icon", e);
        }
    }
}
