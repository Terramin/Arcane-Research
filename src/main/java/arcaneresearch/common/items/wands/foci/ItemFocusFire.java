package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.WandEntities;
import arcaneresearch.common.entities.projectile.EntityEmber;
import arcaneresearch.common.entities.projectile.EntityFocusProjectile;
import arcaneresearch.common.items.casters.foci.FocusEffectFire;
import arcaneresearch.common.lib.wands.WandSounds;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemFocusFire extends ItemFocusBasic {

    private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 2);
    private static final AspectList COST_BEAM = new AspectList().add(Aspect.FIRE, 2).add(Aspect.ORDER, 2);
    private static final AspectList COST_BALL = new AspectList().add(Aspect.FIRE, 8).add(Aspect.ENTROPY, 8);

    public static final FocusUpgradeType fireball = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/fireball.png"),
            "focus.upgrade.fireball.name", "focus.upgrade.fireball.text",
            new AspectList().add(Aspect.DARKNESS, 1));
    public static final FocusUpgradeType firebeam = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/firebeam.png"),
            "focus.upgrade.firebeam.name", "focus.upgrade.firebeam.text",
            new AspectList().add(Aspect.ENERGY, 1).add(Aspect.AIR, 1));

    // Per-player таймер звука: общий экземпляр предмета не должен шарить таймер между игроками
    private static final Map<UUID, Long> SOUND_DELAY = new HashMap<>();

    public ItemFocusFire(net.minecraft.world.item.Item.Properties props) {
        super("fire", 15028484, props);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, fireball)) {
            return COST_BALL.copy();
        }
        if (isUpgradedWith(focusstack, firebeam)) {
            return COST_BEAM.copy();
        }
        return COST_BASE.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public float getTurretCorrection(ItemStack focusstack) {
        return isUpgradedWith(focusstack, fireball) ? 3.0F : 0.0F;
    }

    @Override
    public float getTurretRange(ItemStack focusstack) {
        return isUpgradedWith(focusstack, fireball) ? 32.0F
                : (isUpgradedWith(focusstack, firebeam) ? 16.0F : 12.0F);
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return isUpgradedWith(focusstack, fireball) ? 20 : 0;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focusstack) {
        return !isUpgradedWith(focusstack, fireball);
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focusstack) {
        return isUpgradedWith(focusstack, fireball) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        ItemStack focusStack = wand.getFocusStack(wandstack);
        int potency = wand.getFocusPotency(wandstack);

        if (isUpgradedWith(focusStack, fireball)) {
            if (!level.isClientSide()) {
                FocusPackage pack = new FocusPackage(entity);
                pack.setPower(1.0F + potency * 0.25F);
                FocusEffectFire fire = new FocusEffectFire();
                fire.initialize();
                var powerSetting = fire.getSetting("power");
                if (powerSetting != null) {
                    powerSetting.setValue(Math.min(5, 1 + potency));
                }
                var durationSetting = fire.getSetting("duration");
                if (durationSetting != null) {
                    durationSetting.setValue(isUpgradedWith(focusStack, FocusUpgradeType.alchemistsfire) ? 3 : 1);
                }
                fire.setPackage(pack);
                pack.addNode(fire);
                Vec3 eye = entity.getEyePosition();
                Vec3 look = entity.getLookAngle();
                Trajectory traj = new Trajectory(eye, look);
                EntityFocusProjectile proj = new EntityFocusProjectile(
                        pack, 1.5F, traj, EntityFocusProjectile.SPECIAL_NONE);
                level.addFreshEntity(proj);
                level.levelEvent(LevelEvent.SOUND_GHAST_WARNING, BlockPos.containing(entity.position()), 0);
            }
        } else {
            long now = System.currentTimeMillis();
            Long lastSound = SOUND_DELAY.get(entity.getUUID());
            if (!level.isClientSide() && (lastSound == null || lastSound < now)) {
                level.playSound(null, entity.blockPosition(), WandSounds.fireloop(),
                        SoundSource.PLAYERS, 0.33F, 2.0F);
                SOUND_DELAY.put(entity.getUUID(), now + 500L);
            }
            if (!level.isClientSide()) {
                boolean beam = isUpgradedWith(focusStack, firebeam);
                float scatter = beam ? 0.25F : 15.0F;
                int count = 2 + potency;
                for (int a = 0; a < count; a++) {
                    EntityEmber orb = new EntityEmber(WandEntities.EMBER.get(), level, entity, scatter);
                    orb.damage = 2 + potency;
                    if (beam) {
                        orb.damage += 0.5F;
                        orb.damage *= 1.5F;
                        orb.duration = 30;
                    }
                    orb.firey = getUpgradeLevel(focusStack, FocusUpgradeType.alchemistsfire);
                    Vec3 motion = orb.getDeltaMovement();
                    orb.setPos(orb.getX() + motion.x, orb.getY() + motion.y, orb.getZ() + motion.z);
                    level.addFreshEntity(orb);
                }
            }
        }
        return true;
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.alchemistsfire)
                || !isUpgradedWith(focusstack, fireball)
                || !isUpgradedWith(focusstack, FocusUpgradeType.alchemistsfire);
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                    FocusUpgradeType.alchemistsfire};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, fireball, firebeam};
            default -> null;
        };
    }
}