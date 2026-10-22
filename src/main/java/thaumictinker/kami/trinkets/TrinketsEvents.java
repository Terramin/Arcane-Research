package thaumictinker.kami.trinkets;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


 

 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class TrinketsEvents {

    private TrinketsEvents() {
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player
                && CatAmuletItem.isWearing(player)
                && CatAmuletItem.inRange(player, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Creeper creeper)) {
            return;
        }
        boolean present = creeper.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof WearerAvoidGoal);
        if (!present) {
            creeper.goalSelector.addGoal(3, new WearerAvoidGoal(creeper));
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()
                || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }
        BlockState state = player.level().getBlockState(event.getPos());
        ProtoclayItem.swapTool(player, state);
    }
}
