package arcaneresearch.common.items.resources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.IDustTrigger;
import arcaneresearch.common.items.ItemTCBase;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXMagicDustSparkles;
import arcaneresearch.init.ModSounds;
import java.util.List;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemMagicDust extends ItemTCBase {
    public ItemMagicDust() {
        super(new Properties().rarity(Rarity.UNCOMMON), "salis_mundus");
    }
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicDust)) {
            return;
        }
        Player player = event.getEntity();
        if (player == null || player.isCrouching()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        if (!player.mayUseItemAt(pos, face, stack)) {
            return;
        }
        for (IDustTrigger trigger : IDustTrigger.triggers) {
            if (trigger.getValidFace(level, player, pos, face) != null) {
                event.setUseBlock(Event.Result.DENY);
                return;
            }
        }
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        InteractionHand hand = context.getHand();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.FAIL;
        if (!player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }
        if (player.isCrouching()) {
            return InteractionResult.PASS;
        }
        player.swing(hand);
        for (IDustTrigger trigger : IDustTrigger.triggers) {
            IDustTrigger.Placement place = trigger.getValidFace(level, player, pos, face);
            if (place != null) {
                if (!level.isClientSide) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    trigger.execute(level, player, pos, place, face);
                    List<BlockPos> sparkles = trigger.sparkle(level, player, pos, place);
                    PacketHandler.sendToNear(
                            new PacketFXMagicDustSparkles(player, pos, context.getClickLocation(), hand, sparkles),
                            new PacketDistributor.TargetPoint(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    64.0, level.dimension()));
                    if (ModSounds.DUST.get() != null) {
                        level.playSound(null, pos, ModSounds.DUST.get(), SoundSource.PLAYERS,
                                0.33f, 1.0f + (float) level.random.nextGaussian() * 0.05f);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}