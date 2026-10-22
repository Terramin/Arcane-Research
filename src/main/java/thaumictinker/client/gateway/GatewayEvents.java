package thaumictinker.client.gateway;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.kami.gateway.SkyPearlItem;
import thaumictinker.kami.gateway.WarpGateBlockEntity;

 



 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, value = Dist.CLIENT)
public final class GatewayEvents {

    private GatewayEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }
        AABB box = player.getBoundingBox();
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                for (int y = Mth.floor(box.minY) - 2; y <= Mth.floor(box.minY); y++) {
                    if (minecraft.level.getBlockEntity(new BlockPos(x, y, z)) instanceof WarpGateBlockEntity gate
                            && gate.standingZone().intersects(box)) {
                        minecraft.setScreen(new WarpGateDestinationsScreen(gate));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player != null && SkyPearlItem.isAttuned(event.getItemStack())) {
            PearlInfo.add(event.getItemStack(), player.level().dimension(), player.position(),
                    event.getToolTip(), false);
        }
    }
}
