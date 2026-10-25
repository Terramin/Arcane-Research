package arcaneresearch.api.aspects;
import net.minecraftforge.eventbus.api.Event;
public class AspectRegistryEvent extends Event {
    public AspectEventProxy register;
    public AspectRegistryEvent() {
        this.register = new AspectEventProxy();
    }
}
