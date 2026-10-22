package thaumictinker.tablet;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import thaumictinker.TtCore;

 


 
@Mod.EventBusSubscriber(modid = TtCore.MODID)
public final class TabletCapabilities {

    private TabletCapabilities() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof DynamismTabletBlockEntity tablet)) {
            return;
        }
        event.addCapability(new ResourceLocation("thaumictinker", "tablet_items"), new ICapabilityProvider() {
            private final LazyOptional<IItemHandler> handler =
                    LazyOptional.of(() -> new InvWrapper(tablet));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
                return capability == ForgeCapabilities.ITEM_HANDLER ? handler.cast() : LazyOptional.empty();
            }
        });
    }
}
