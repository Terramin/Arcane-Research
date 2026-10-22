package thaumictinker.enchanting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import arcaneresearch.init.ModSounds;

 



 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TtEnchantmentEvents {

    private static final double SLOW_FALL_MIN_MOTION = -0.0784000015258789D;
    private static final double SLOW_FALL_MIN_DISTANCE = 2.9D;
    private static final String STRIKE_TAG = "thaumictinker:strike";
    private static final String TUNNEL_TAG = "thaumictinker:tunnel";

    private TtEnchantmentEvents() {
    }

    private static int lastTarget(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(STRIKE_TAG) ? data.getInt(STRIKE_TAG) : 0;
    }

    private static void setLastTarget(Player player, int target) {
        player.getPersistentData().putInt(STRIKE_TAG, target);
    }

    private static float tunnelDirection(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(TUNNEL_TAG) ? data.getFloat(TUNNEL_TAG) : Float.NaN;
    }

    private static void setTunnelDirection(Player player, float direction) {
        player.getPersistentData().putFloat(TUNNEL_TAG, direction);
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            event.setAmount(modifyDamage(attacker, event.getEntity(), event.getAmount()));
        }
    }

    public static float modifyDamage(LivingEntity attacker, LivingEntity target, float damage) {
        ItemStack held = attacker.getMainHandItem();
        if (held.isEmpty()) {
            return damage;
        }
        float amount = damage;
        if (attacker instanceof Player player) {
            int pounce = TtEnchantments.level(player.getItemBySlot(EquipmentSlot.LEGS), TtEnchantments.POUNCE);
            if (pounce > 0) {
                BlockPos below = BlockPos.containing(player.getX(), Mth.floor(player.getEyeY()) - 1, player.getZ());
                if (player.level().getBlockState(below).isAir()) {
                    amount *= 1 + 0.25D * pounce;
                }
            }
        }
        int finalStrike = TtEnchantments.level(held, TtEnchantments.FINAL_STRIKE);
        if (finalStrike > 0 && attacker.getRandom().nextInt(20 - finalStrike) == 0) {
            amount *= 3;
        }
        int valiance = TtEnchantments.level(held, TtEnchantments.VALIANCE);
        if (valiance > 0 && attacker.getHealth() / attacker.getMaxHealth() < 0.5F) {
            amount *= 1 + 0.1F * valiance;
        }
        int focused = TtEnchantments.level(held, TtEnchantments.FOCUSED_STRIKE);
        int dispersed = TtEnchantments.level(held, TtEnchantments.DISPERSED_STRIKE);
        if (focused > 0 || dispersed > 0) {
            int successive = attacker instanceof Player attackerPlayer && lastTarget(attackerPlayer) == target.getId() ? 1 : 0;
            if (focused > 0) {
                amount /= 2;
                amount += (float) (0.5D * successive * amount * focused);
            }
            if (dispersed > 0) {
                amount *= 1 + (successive / 5);
                amount /= 1 + successive * 2;
            }
            if (attacker instanceof Player attackerPlayer) {
                setLastTarget(attackerPlayer, target.getId());
            }
        }
        int vampirism = TtEnchantments.level(held, TtEnchantments.VAMPIRISM);
        if (vampirism > 0) {
            attacker.heal(vampirism);
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.ZAP.get(),
                    SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return amount;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            applySlowFall(event.player);
        }
    }

    public static void applySlowFall(Player player) {
        int slowFall = TtEnchantments.maxArmorLevel(player, TtEnchantments.SLOW_FALL);
        if (slowFall > 0 && !player.isShiftKeyDown()
                && player.getDeltaMovement().y < SLOW_FALL_MIN_MOTION
                && player.fallDistance >= SLOW_FALL_MIN_DISTANCE) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y / (1 + slowFall * 0.33D), motion.z);
            player.fallDistance = (float) Math.max(SLOW_FALL_MIN_DISTANCE, player.fallDistance - slowFall / 3.0D);
            if (player.level().isClientSide()) {
                player.level().addParticle(ParticleTypes.CLOUD,
                        player.getX() + 0.25, player.getY() - 1, player.getZ() + 0.25,
                        -motion.x, motion.y, -motion.z);
            }
        }
    }

    @SubscribeEvent
    static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        event.setDuration(quickDrawDuration(event.getEntity(), event.getItem(), event.getDuration()));
    }

    public static int quickDrawDuration(LivingEntity entity, ItemStack using, int remaining) {
        if (!(entity instanceof Player player) || using.getUseAnimation() != UseAnim.BOW) {
            return remaining;
        }
        int quickDraw = TtEnchantments.level(player.getMainHandItem(), TtEnchantments.QUICK_DRAW);
        if (quickDraw > 0 && (using.getUseDuration() - remaining) % (6 - quickDraw) == 0) {
            return remaining - 1;
        }
        return remaining;
    }

    @SubscribeEvent
    static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyAscentBoost(player);
        }
    }

    public static void applyAscentBoost(Player player) {
        int boost = TtEnchantments.maxArmorLevel(player, TtEnchantments.ASCENT_BOOST);
        if (boost >= 1 && !player.isShiftKeyDown()) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y * (boost + 2) / 2.0D, motion.z);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyShockwave(player, event.getDistance());
        }
    }

    public static void applyShockwave(Player player, double distance) {
        int shockwave = TtEnchantments.level(player.getItemBySlot(EquipmentSlot.FEET), TtEnchantments.SHOCKWAVE);
        if (shockwave > 0 && distance > 3 && player.level() instanceof ServerLevel level) {
            AABB area = new AABB(player.getX() - 10, player.getY() - 10, player.getZ() - 10,
                    player.getX() + 10, player.getY() + 10, player.getZ() + 10);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (target != player) {
                    target.hurt(level.damageSources().fall(), (float) (0.1F * shockwave * distance));
                }
            }
        }
    }

    @SubscribeEvent
    static void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (TtEnchantments.level(player.getMainHandItem(), TtEnchantments.TUNNEL) > 0) {
            setTunnelDirection(player, player.getYRot());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(player.blockPosition());
        float speed = event.getNewSpeed();
        Float result = breakSpeed(player, held, event.getState(), pos, speed);
        if (result == null) {
            event.setCanceled(true);
        } else {
            event.setNewSpeed(result);
        }
    }

    public static Float breakSpeed(Player player, ItemStack held, BlockState state, BlockPos pos, float speed) {
        float newSpeed = speed;
        float hardness = state.getDestroySpeed(player.level(), pos);
        int shatter = TtEnchantments.level(held, TtEnchantments.SHATTER);
        if (shatter > 0) {
            if (hardness > 20.0F) {
                newSpeed *= 3 * shatter;
            } else {
                newSpeed *= 0.8F;
            }
        }
        int tunnel = TtEnchantments.level(held, TtEnchantments.TUNNEL);
        if (tunnel > 0) {
            float oldDirection = tunnelDirection(player);
            if (!Float.isNaN(oldDirection)) {
                if (Math.abs(oldDirection - player.getYRot()) < 50.0D) {
                    newSpeed *= (float) (1 + 0.2D * tunnel);
                } else {
                    newSpeed *= 0.3F;
                }
            }
        }
        int desintegrate = TtEnchantments.level(held, TtEnchantments.DESINTEGRATE);
        int autoSmelt = TtEnchantments.level(held, TtEnchantments.AUTO_SMELT);
        if (desintegrate > 0 && hardness <= 1.5F) {
            if (!player.level().isClientSide()) {
                held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
            return Float.MAX_VALUE;
        }
        
        
        if (desintegrate > 0 || autoSmelt > 0) {
            return null;
        }
        return newSpeed;
    }
}
