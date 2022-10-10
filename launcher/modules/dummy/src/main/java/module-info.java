import net.gudenau.dummy.Dummy;
import net.gudenau.launcher.api.Plugin;

module dummy {
    requires launcher;
    
    provides Plugin with Dummy;
}
