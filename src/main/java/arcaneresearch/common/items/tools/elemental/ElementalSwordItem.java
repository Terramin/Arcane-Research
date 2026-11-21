package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import java.util.List;

public class ElementalSwordItem extends Item implements ElementalTool {
    public ElementalSwordItem(Item.Properties props) {
        super(ElementalTool.createProperties(props)
                .sword(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 3, -2.4F));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        int ticks = getUseDuration(stack, user) - remainingUseDuration;
        Vec3 motion = user.getDeltaMovement();
        if (motion.y < 0.0D) {
            user.setDeltaMovement(motion.x, motion.y / 1.2D, motion.z);
            user.fallDistance /= 1.2F;
        }
        motion = user.getDeltaMovement();
        double lift = motion.y + 0.08D;
        if (lift > 0.5D) {
            lift = 0.2D;
        }
        user.setDeltaMovement(motion.x, lift, motion.z);
        if (user instanceof ServerPlayer serverPlayer) {
            arcaneresearch.common.lib.utils.EntityUtils.resetFloatCounter(serverPlayer);
        }
        List<Entity> targets = level.getEntities(user, user.getBoundingBox().inflate(2.5D));
        if (!targets.isEmpty()) {
            for (Entity entity : targets) {
                if (entity instanceof LivingEntity living && !(entity instanceof Player)
                        && !living.isRemoved() && user.getVehicle() != entity) {
                    Vec3 p = user.position();
                    Vec3 t = living.position();
                    double distance = p.distanceTo(t) + 0.1D;
                    Vec3 r = t.subtract(p);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            r.x / 2.5D / distance, r.y / 2.5D / distance, r.z / 2.5D / distance));
                }
            }
        }
        if (!level.isClientSide() && ticks % 20 == 0) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(),
                    arcaneresearch.init.ModSounds.WIND.get(), SoundSource.PLAYERS,
                    0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            stack.hurtAndBreak(1, user, InteractionHand.MAIN_HAND);
        }
    }
}
