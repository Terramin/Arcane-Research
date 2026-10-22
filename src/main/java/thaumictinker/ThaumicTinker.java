package thaumictinker;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thaumictinker.registry.TtRegistry;

@Mod(TtCore.MODID)
public final class ThaumicTinker {

    public static final Logger LOGGER = LoggerFactory.getLogger(TtCore.MODID);

    public ThaumicTinker(IEventBus modEventBus) {
        
    }

    public ThaumicTinker() {
        this(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
