package arcaneresearch.common.items.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.SwingAnimation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.IDustTrigger;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.ItemTCBase;
import arcaneresearch.init.ModSounds;

import java.util.List;

// NEOFORGE_PORT: @Mod.EventBusSubscriber(modid, bus = FORGE) -> @EventBusSubscriber(modid) (bus param removed in NeoForge; game-bus subscriber auto-registers, same as MazePersistence).
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ItemMagicDust extends ItemTCBase {

    public ItemMagicDust(net.minecraft.world.item.Item.Properties props) {
        super(props.rarity(Rarity.UNCOMMON), "salis_mundus");
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
                // NEOFORGE_PORT: Event.Result.DENY -> TriState.FALSE (NeoForge event uses TriState for useBlock/useItem, verified via javap).
                event.setUseBlock(TriState.FALSE);
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

        // NEOFORGE_PORT: swing(hand) -> swing(hand, SwingAnimation.DEFAULT, false) (verified via javap on LivingEntity).
        player.swing(hand, SwingAnimation.DEFAULT, false);

        for (IDustTrigger trigger : IDustTrigger.triggers) {
            IDustTrigger.Placement place = trigger.getValidFace(level, player, pos, face);
            if (place != null) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                trigger.execute(level, player, pos, place, face);

                if (level.isClientSide()) {
                    doSparkles(player, level, pos, context.getClickLocation(), hand, trigger, place);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @OnlyIn(Dist.CLIENT)
    private void doSparkles(Player player, Level level, BlockPos pos, Vec3 hitVec, InteractionHand hand, IDustTrigger trigger, IDustTrigger.Placement place) {
        float handOffset = (hand == InteractionHand.MAIN_HAND) ? 0.0f : 180.0f;
        double px = player.getX();
        double py = player.getBoundingBox().minY + player.getBbHeight() / 2.0f + 0.25;
        double pz = player.getZ();
        px += -net.minecraft.util.Mth.cos((player.getYRot() + handOffset) / 180.0f * (float) Math.PI) * 0.3f;
        pz += -net.minecraft.util.Mth.sin((player.getYRot() + handOffset) / 180.0f * (float) Math.PI) * 0.3f;
        Vec3 look = player.getLookAngle();
        px += look.x * 0.3;
        py += look.y * 0.3;
        pz += look.z * 0.3;
        Vec3 v1 = new Vec3(px, py, pz);
        Vec3 v2 = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).subtract(v1);
        java.util.Random rand = new java.util.Random();
        for (int cnt = 50, a = 0; a < cnt; ++a) {
            boolean floaty = a < cnt / 3;
            float r = 1.0f;
            // NEOFORGE_PORT: level.random -> level.getRandom() (field removed in 26.x, verified via javap on Level).
            float g = (189 + level.getRandom().nextInt(67)) / 255.0f;
            float b = (64 + level.getRandom().nextInt(192)) / 255.0f;
            FXDispatcher.INSTANCE.drawSimpleSparkle(rand, v1.x, v1.y, v1.z,
                    v2.x / 6.0 + level.getRandom().nextGaussian() * 0.05,
                    v2.y / 6.0 + level.getRandom().nextGaussian() * 0.05 + (floaty ? 0.05 : 0.15),
                    v2.z / 6.0 + level.getRandom().nextGaussian() * 0.05,
                    0.5f, r, g, b, level.getRandom().nextInt(5),
                    floaty ? (0.3f + level.getRandom().nextFloat() * 0.5f) : 0.85f,
                    floaty ? 0.2f : 0.5f, 16);
        }
        if (ModSounds.DUST.get() != null) {
            level.playSound(player, pos, ModSounds.DUST.get(), SoundSource.PLAYERS,
                    0.33f, 1.0f + (float) level.getRandom().nextGaussian() * 0.05f);
        }
        List<BlockPos> sparkles = trigger.sparkle(level, player, pos, place);
        if (sparkles != null) {
            for (BlockPos p : sparkles) {
                FXDispatcher.INSTANCE.drawBlockSparkles(p, hitVec);
            }
        }
    }
}
