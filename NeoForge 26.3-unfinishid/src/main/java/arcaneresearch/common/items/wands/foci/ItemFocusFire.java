package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.WandEntities;
import arcaneresearch.common.entities.projectile.EntityEmber;
import arcaneresearch.common.lib.wands.WandSounds;

public class ItemFocusFire extends ItemFocusBasic {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 2);
    private static final AspectList COST_BEAM = new AspectList().add(Aspect.FIRE, 2).add(Aspect.ORDER, 2);
    private static final AspectList COST_BALL = new AspectList().add(Aspect.FIRE, 8).add(Aspect.ENTROPY, 8);

    public static FocusUpgradeType fireball = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/fireball.png"),
            "focus.upgrade.fireball.name", "focus.upgrade.fireball.text",
            new AspectList().add(Aspect.DARKNESS, 1));
    public static FocusUpgradeType firebeam = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/firebeam.png"),
            "focus.upgrade.firebeam.name", "focus.upgrade.firebeam.text",
            new AspectList().add(Aspect.ENERGY, 1).add(Aspect.AIR, 1));
    private long soundDelay = 0L;

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
        return isUpgradedWith(focusstack, fireball) ? 1000 : 0;
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
        if (isUpgradedWith(focusStack, fireball)) {
            if (!level.isClientSide()) {
                arcaneresearch.api.casters.FocusPackage pack =
                        new arcaneresearch.api.casters.FocusPackage(entity);
                pack.setPower(1.0F + wand.getFocusPotency(wandstack) * 0.25F);
                arcaneresearch.common.items.casters.foci.FocusEffectFire fire =
                        new arcaneresearch.common.items.casters.foci.FocusEffectFire();
                fire.initialize();
                if (fire.getSetting("power") != null) {
                    fire.getSetting("power").setValue(
                            Math.min(5, 1 + wand.getFocusPotency(wandstack)));
                }
                if (fire.getSetting("duration") != null) {
                    fire.getSetting("duration").setValue(
                            isUpgradedWith(focusStack, FocusUpgradeType.alchemistsfire) ? 3 : 1);
                }
                fire.setPackage(pack);
                pack.addNode(fire);
                net.minecraft.world.phys.Vec3 eye = entity.getEyePosition();
                net.minecraft.world.phys.Vec3 look = entity.getLookAngle();
                arcaneresearch.api.casters.Trajectory traj =
                        new arcaneresearch.api.casters.Trajectory(eye, look);
                arcaneresearch.common.entities.projectile.EntityFocusProjectile proj =
                        new arcaneresearch.common.entities.projectile.EntityFocusProjectile(
                                pack, 1.5F, traj,
                                arcaneresearch.common.entities.projectile.EntityFocusProjectile.SPECIAL_NONE);
                level.addFreshEntity(proj);
                // NEOFORGE_PORT: Level.levelEvent now takes the causing entity first (null = none).
                level.levelEvent(null, LevelEvent.SOUND_GHAST_WARNING, net.minecraft.core.BlockPos.containing(entity.position()), 0);
            }
        } else {
            if (!level.isClientSide() && soundDelay < System.currentTimeMillis()) {
                level.playSound(null, entity.blockPosition(), WandSounds.fireloop(), SoundSource.PLAYERS,
                        0.33F, 2.0F);
                soundDelay = System.currentTimeMillis() + 500L;
            }
            if (!level.isClientSide()) {
                float scatter = isUpgradedWith(focusStack, firebeam) ? 0.25F : 15.0F;
                for (int a = 0; a < 2 + wand.getFocusPotency(wandstack); a++) {
                    EntityEmber orb = new EntityEmber(WandEntities.EMBER.get(), level, entity, scatter);
                    orb.damage = 2 + wand.getFocusPotency(wandstack);
                    if (isUpgradedWith(focusStack, firebeam)) {
                        orb.damage += 0.5F;
                        orb.damage *= 1.5F;
                        orb.duration = 30;
                    }
                    orb.firey = getUpgradeLevel(focusStack, FocusUpgradeType.alchemistsfire);
                    net.minecraft.world.phys.Vec3 motion = orb.getDeltaMovement();
                    orb.setPos(orb.getX() + motion.x, orb.getY() + motion.y, orb.getZ() + motion.z);
                    level.addFreshEntity(orb);
                }
            }
        }
        return true;
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        if (type.equals(FocusUpgradeType.alchemistsfire) && isUpgradedWith(focusstack, fireball)
                && isUpgradedWith(focusstack, FocusUpgradeType.alchemistsfire)) {
            return false;
        }
        return true;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.alchemistsfire};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, fireball, firebeam};
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.alchemistsfire};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            default:
                break;
        }
        return null;
    }
}
