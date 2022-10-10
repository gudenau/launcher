import net.gudenau.launcher.api.Plugin;
import net.gudenau.launcher.api.account.AccountProvider;
import net.gudenau.launcher.api.game.GameProvider;

module launcher {
    requires java.desktop;
    requires jdk.unsupported;
    
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    
    requires com.google.gson;
    
    requires it.unimi.dsi.fastutil;
    
    exports net.gudenau.launcher;
    exports net.gudenau.launcher.api;
    exports net.gudenau.launcher.api.game;
    exports net.gudenau.launcher.api.resource;
    exports net.gudenau.launcher.api.util;
    exports net.gudenau.launcher.api.util.functional;
    
    uses GameProvider;
    uses Plugin;
    uses AccountProvider;
    
    // TODO Remove this when Idea stops losing it's mind.
    requires org.jetbrains.annotations;
}
