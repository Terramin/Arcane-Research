package thaumictinker.kami.tool;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


 



 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class AwakenedToolsEvents {

    private AwakenedToolsEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Direction face = event.getFace();
        if (player != null && face != null) {
            ToolHandler.LAST_FACE.put(player.getId(), face);
        }
    }
}
