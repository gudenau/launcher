package net.gudenau.launcher;

import net.gudenau.launcher.api.resource.ResourceManager;
import net.gudenau.launcher.api.resource.ResourceProvider;
import net.gudenau.launcher.api.util.Architecture;
import net.gudenau.launcher.api.util.Logger;
import net.gudenau.launcher.api.util.OperatingSystem;
import net.gudenau.launcher.coms.Communications;
import net.gudenau.launcher.coms.ContainerControl;
import net.gudenau.launcher.impl.account.AccountManagerImpl;
import net.gudenau.launcher.impl.profile.ProfileManager;
import net.gudenau.launcher.impl.util.LanguageManagerImpl;
import net.gudenau.launcher.impl.util.ThreadUtil;
import net.gudenau.launcher.plugin.PluginLoader;
import net.gudenau.launcher.ui.LoadingScreen;
import net.gudenau.launcher.ui.MainScreen;

import java.io.IOException;

public class Launcher {
    private static final Logger LOGGER = Logger.forName("launcher");
    
    public static final String NAMESPACE = "launcher";
    
    public static void main(String[] args) {
        LOGGER.info("""
            Launcher version: %s
            Operating system: %s
            Architecture: %s
            """,
            Versions.LAUNCHER_VERSION,
            OperatingSystem.get(),
            Architecture.get()
        );
    
        ThreadUtil.waitFor(() -> {
            ResourceManager.get().registerProvider(ResourceProvider.of(NAMESPACE, Launcher.class));
            
            try {
                LanguageManagerImpl.INSTANCE.init();
            } catch (IOException e) {
                LOGGER.fatal(e, "Failed to read languages");
            }
        }, () -> {
            if(args.length != 2) {
                Communications.disable();
                LOGGER.info("Container manager disabled, no pipes");
                return;
            }
            try {
                var readPipe = Integer.parseInt(args[0]);
                var writePipe = Integer.parseInt(args[1]);
                if(readPipe == -1 || writePipe == -1) {
                    Communications.disable();
                    LOGGER.info("Container manager disabled, no pipes");
                    return;
                }
                Communications.init(readPipe, writePipe);
                LOGGER.info("Container version: %s", ContainerControl.version());
            } catch (Throwable e) {
                Communications.disable();
                LOGGER.info(e, "Container manager disabled");
            }
        }, LoadingScreen::show);
        
        try {
            PluginLoader.init();
        } catch (IOException e) {
            LOGGER.fatal(e, "Failed to init plugins");
        }
    
        try {
            LanguageManagerImpl.INSTANCE.init();
        } catch (IOException e) {
            LOGGER.fatal(e, "Failed to read languages");
        }
    
        ProfileManager.init();
        AccountManagerImpl.init();
        MainScreen.init();
    }
}
