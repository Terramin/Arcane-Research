package thaumictinker.awakenedarmor;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.foci.DeflectFocus;
import thaumictinker.gas.BrightNitorItem;

 





 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class AwakenedArmorEvents {

    public static final java.util.UUID STEP_MODIFIER_ID =
            java.util.UUID.fromString("f2b58f4e-1cfb-47a0-be67-dd3d61a48e4f");
    public static final double STEP_BONUS = 0.4;
    public static final int FIRE_RESISTANCE_AMPLIFIER = 10;
    public static final int HASTE_AMPLIFIER = 1;
    public static final int HASTE_DURATION = 2;
    public static final float JUMP_BOOST = 0.3F;
    public static final int LIGHT_STEPS = 5;
    public static final int EFFECT_DURATION = 202;

    public static final String FLIGHT_GRANTED_TAG = "thaumictinker_flight_granted";

    private AwakenedArmorEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tick(event.player);
    }

    public static void tick(Player player) {
        updateStepHeight(player);
        if (player instanceof ServerPlayer serverPlayer) {
            updateFlight(serverPlayer);
        }
        if (AwakenedArmor.isActive(player, EquipmentSlot.CHEST, AwakenedArmorFeature.ROBES)) {
            DeflectFocus.protect(player);
        }
        if (player.level().isClientSide()) {
            tickBootsMovement(player);
            return;
        }
        tickCowl(player);
        tickLeggings(player);
        tickBoots(player);
    }

    private static void refresh(Player player, MobEffect effect, int duration, int amplifier) {
        MobEffectInstance existing = player.getEffect(effect);
        if (existing == null || existing.getDuration() <= duration && existing.getAmplifier() <= amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
        }
    }

    private static void tickCowl(Player player) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(AwakenedArmorFeature.COWL)) {
            return;
        }
        if (AwakenedArmor.isActive(player, EquipmentSlot.HEAD, AwakenedArmorFeature.COWL)) {
            if (player.isEyeInFluid(FluidTags.WATER)) {
                player.setAirSupply(300);
                refresh(player, MobEffects.NIGHT_VISION, EFFECT_DURATION, 0);
            }
            if (player.isEyeInFluid(FluidTags.LAVA)) {
                player.setAirSupply(300);
                refresh(player, MobEffects.BLINDNESS, EFFECT_DURATION, 0);
            }
        }
        int food = player.getFoodData().getFoodLevel();
        if (food > 0 && food < 18 && player.isHurt() && player.tickCount % 80 == 0) {
            player.heal(1.0F);
        }
    }

    private static void tickLeggings(Player player) {
        if (!AwakenedArmor.isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS)) {
            return;
        }
        refresh(player, MobEffects.FIRE_RESISTANCE, EFFECT_DURATION, FIRE_RESISTANCE_AMPLIFIER);
        Level level = player.level();
        int x = Mth.floor(player.getX());
        int y = (int) player.getY() + 1;
        int z = Mth.floor(player.getZ());
        BrightNitorItem.placeNitorGas(level, new BlockPos(x, y, z), true);
        float yaw = Mth.wrapDegrees(player.getYRot() + 90.0F) * (float) Math.PI / 180.0F;
        double lookX = Math.cos(yaw);
        double lookZ = Math.sin(yaw);
        double length = Math.sqrt(lookX * lookX + lookZ * lookZ);
        lookX /= length;
        lookZ /= length;
        double stepX = lookX;
        double stepZ = lookZ;
        for (int index = 0; index < LIGHT_STEPS; index++) {
            stepX += lookX;
            stepZ += lookZ;
            BrightNitorItem.placeNitorGas(level, new BlockPos(x + (int) stepX, y, z + (int) stepZ), false);
        }
    }

    private static void tickBoots(Player player) {
        if (!AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS)) {
            return;
        }
        MobEffectInstance existing = player.getEffect(MobEffects.DIG_SPEED);
        if (existing == null || existing.getDuration() <= 1 && existing.getAmplifier() <= HASTE_AMPLIFIER) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, HASTE_DURATION, HASTE_AMPLIFIER, true, false));
        }
        player.resetFallDistance();
        BlockPos below = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getY()) - 1, Mth.floor(player.getZ()));
        if (player.level().getBlockState(below).is(Blocks.DIRT)) {
            player.level().setBlock(below, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        }
    }

    private static void tickBootsMovement(Player player) {
        if (!player.isLocalPlayer() || !AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS)) {
            return;
        }
        boolean flying = player.getAbilities().flying;
        if ((player.onGround() || flying) && player.zza > 0.0F) {
            player.moveRelative(flying ? 0.075F : 0.15F, new Vec3(0.0, 0.0, 1.0));
        }
        if (!player.onGround() && !flying) {
            float airControl = (player.isSprinting() ? 0.05F : 0.04F) - 0.02F;
            player.moveRelative(airControl, new Vec3(player.xxa, 0.0, player.zza));
        }
        player.resetFallDistance();
    }

    public static void updateStepHeight(Player player) {
        AttributeInstance attribute = player.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attribute == null) {
            return;
        }
        boolean wanted = !player.isShiftKeyDown() && AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS);
        boolean present = attribute.getModifier(STEP_MODIFIER_ID) != null;
        if (wanted && !present) {
            attribute.addTransientModifier(new AttributeModifier(STEP_MODIFIER_ID, "awakened_boots_step",
                    STEP_BONUS, AttributeModifier.Operation.ADDITION));
        } else if (!wanted && present) {
            attribute.removeModifier(STEP_MODIFIER_ID);
        }
    }

    public static void updateFlight(ServerPlayer player) {
        boolean granted = player.getPersistentData().getBoolean(FLIGHT_GRANTED_TAG);
        boolean wanted = AwakenedArmor.isActive(player, EquipmentSlot.CHEST, AwakenedArmorFeature.ROBES);
        if (wanted) {
            if (!player.getAbilities().mayfly || !granted) {
                boolean changed = !player.getAbilities().mayfly;
                player.getAbilities().mayfly = true;
                player.getPersistentData().putBoolean(FLIGHT_GRANTED_TAG, true);
                if (changed) {
                    player.onUpdateAbilities();
                }
            }
        } else if (granted) {
            revokeFlight(player);
        }
    }

    public static void revokeFlight(ServerPlayer player) {
        player.getPersistentData().remove(FLIGHT_GRANTED_TAG);
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    public static void release(Player player, EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                removeOwned(player, MobEffects.NIGHT_VISION, 0);
                removeOwned(player, MobEffects.BLINDNESS, 0);
            }
            case CHEST -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    updateFlight(serverPlayer);
                }
            }
            case LEGS -> removeOwned(player, MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_AMPLIFIER);
            case FEET -> {
                removeOwned(player, MobEffects.DIG_SPEED, HASTE_AMPLIFIER);
                updateStepHeight(player);
            }
            default -> {
            }
        }
    }

    private static void removeOwned(Player player, MobEffect effect, int amplifier) {
        MobEffectInstance existing = player.getEffect(effect);
        if (existing != null && existing.isAmbient() && existing.getAmplifier() == amplifier
                && existing.getDuration() <= EFFECT_DURATION) {
            player.removeEffect(effect);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (event.getFrom().getItem() instanceof AwakenedArmorItem && !event.getTo().is(event.getFrom().getItem())) {
            release(player, event.getSlot());
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player
                && AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS)) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, JUMP_BOOST, 0.0));
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player
                && player.getItemBySlot(EquipmentSlot.FEET).is(AwakenedArmorFeature.BOOTS)) {
            event.setDamageMultiplier(0.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIncomingDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && event.getSource().is(DamageTypeTags.IS_FIRE)
                && AwakenedArmor.isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS)) {
            event.setCanceled(true);
            player.heal(event.getAmount());
        }
    }
}
