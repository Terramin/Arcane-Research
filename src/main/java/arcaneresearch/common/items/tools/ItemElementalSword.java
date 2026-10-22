package arcaneresearch.common.items.tools;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModSounds;

import java.util.List;

public class ItemElementalSword extends Item {

    public ItemElementalSword(Item.Properties props) {
        
        super(props
                .rarity(Rarity.RARE)
                .sword(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 3, -2.4F));
    }

    
    
    
    

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        
        return ItemUseAnimation.BLOCK;
    }

    
    
    
    
    
    @net.neoforged.fml.common.EventBusSubscriber(modid = arcaneresearch.Arcaneresearch.MODID, value = Dist.CLIENT)
    public static final class ClientExtensions {
        private ClientExtensions() {
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onRegisterClientExtensions(
                net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
            event.registerItem(arcaneresearch.client.renderers.item.SwordBlockAnimation.HAND_EXTENSION,
                    arcaneresearch.init.ModItems.ELEMENTAL_SWORD.get());
        }
    }

     



 
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
            boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        
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
            EntityUtils.resetFloatCounter(serverPlayer);
        }

        List<Entity> targets = level.getEntities(user, user.getBoundingBox().inflate(2.5D));
        if (!targets.isEmpty()) {
            for (Entity entity : targets) {
                if (!(entity instanceof Player) && entity instanceof LivingEntity
                        && !entity.isRemoved()
                        && (user.getVehicle() == null || user.getVehicle() != entity)) {
                    Vec3 p = user.position();
                    Vec3 t = entity.position();
                    double distance = p.distanceTo(t) + 0.1D;
                    Vec3 r = t.subtract(p);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            r.x / 2.5D / distance, r.y / 2.5D / distance, r.z / 2.5D / distance));
                }
            }
        }
        if (level.isClientSide()) {
            doClientUseFX(level, user);
        } else if (ticks == 0 || ticks % 20 == 0) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.WIND.get(),
                    SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        if (ticks % 20 == 0 && !level.isClientSide()) {
            
            
            
            
            
            stack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel) level, user, broken -> {
                if (user instanceof ServerPlayer brokenBy) {
                    brokenBy.awardStat(net.minecraft.stats.Stats.ITEM_BROKEN.get(broken.getItem()));
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientUseFX(Level level, LivingEntity user) {
        int miny = (int) (user.getBoundingBox().minY - 2.0D);
        if (user.onGround()) {
            miny = Mth.floor(user.getBoundingBox().minY);
        }
        
        
        
        double spiralY = user.getBoundingBox().minY + user.getBbHeight() / 2.0F;
        float spiralRad = 1.5F;
        if (FXDispatcher.isLocalFirstPerson(user)) {
            spiralY = user.getBoundingBox().minY + 0.2D;
            spiralRad = 0.8F;
        }
        for (int a = 0; a < 5; a++) {
            FXDispatcher.INSTANCE.smokeSpiral(user.getX(), spiralY, user.getZ(),
                    spiralRad, level.getRandom().nextInt(360), miny, 14540253);
        }
        if (user.onGround()) {
            float r1 = level.getRandom().nextFloat() * 360.0F;
            float mx = -Mth.sin(r1 / 180.0F * (float) Math.PI) / 5.0F;
            float mz = Mth.cos(r1 / 180.0F * (float) Math.PI) / 5.0F;
            level.addParticle(ParticleTypes.SMOKE, user.getX(),
                    user.getBoundingBox().minY + 0.1D, user.getZ(), mx, 0.0D, mz);
        }
    }
}
