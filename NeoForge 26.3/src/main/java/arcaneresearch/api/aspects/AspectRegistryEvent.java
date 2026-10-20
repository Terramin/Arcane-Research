package arcaneresearch.api.aspects;

import net.neoforged.bus.api.Event;

public class AspectRegistryEvent extends Event {

    public AspectEventProxy register;

    public AspectRegistryEvent() {
        this.register = new AspectEventProxy();
    }

}
