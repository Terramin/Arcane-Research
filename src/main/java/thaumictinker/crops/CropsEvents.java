package thaumictinker.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import arcaneresearch.init.ModItems;

 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CropsEvents {

    public static final int HOE_DAMAGE = 25;

    private CropsEvents() {
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = event.getItemStack();
        if (state.getBlock() instanceof InfusedGrainBlock crop && stack.is(ModItems.ELEMENTAL_HOE.get())) {
            Player player = event.getEntity();
            if (level instanceof ServerLevel serverLevel) {
                if (!crop.isMaxAge(state)) {
                    crop.growCrops(serverLevel, pos, state);
                }
                stack.hurtAndBreak(HOE_DAMAGE, player, (p) -> p.broadcastBreakEvent(event.getHand()));
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            } else {
                arcaneresearch.client.fx.FXDispatcher.INSTANCE.blockRunes(pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 0.2F, 0.9F, 0.4F, 20, 0.0F);
            }
        }
    }
}
