package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.client.fx.WandFX;
import arcaneresearch.common.entities.WandEntities;
import arcaneresearch.common.entities.projectile.EntityShockOrb;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXZap;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.lib.wands.WandTargets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemFocusShock extends ItemFocusBasic {

    private static final AspectList COST_BASE = new AspectList().add(Aspect.AIR, 8);
    private static final AspectList COST_CHAIN = new AspectList().add(Aspect.AIR, 8).add(Aspect.WATER, 8);
    private static final AspectList COST_GROUND = new AspectList().add(Aspect.AIR, 8).add(Aspect.EARTH, 8);

    public static final FocusUpgradeType chainlightning = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/chainlightning.png"),
            "focus.upgrade.chainlightning.name", "focus.upgrade.chainlightning.text",
            new AspectList().add(Aspect.ENERGY, 1));
    public static final FocusUpgradeType earthshock = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/earthshock.png"),
            "focus.upgrade.earthshock.name", "focus.upgrade.earthshock.text",
            new AspectList().add(Aspect.ENERGY, 1));

    private static final FocusUpgradeType[] RANK_1_2 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency};
    private static final FocusUpgradeType[] RANK_3 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency, chainlightning, earthshock};
    private static final FocusUpgradeType[] RANK_4_5 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};

    public ItemFocusShock() {
        super("shock", 10466239);
    }

    public static void shootLightning(Level level, LivingEntity caster, double xx, double yy, double zz) {
        if (!level.isClientSide) {
            return;
        }
        Vec3 look = caster.getLookAngle();
        double px = caster.getX()
                + (-Mth.cos(caster.getYRot() / 180.0F * (float) Math.PI) * 0.06F)
                + look.x * 0.5D;
        double py = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.25D - 0.06D
                + look.y * 0.5D;
        double pz = caster.getZ()
                + (-Mth.sin(caster.getYRot() / 180.0F * (float) Math.PI) * 0.06F)
                + look.z * 0.5D;
        if (caster instanceof Player) {
            try {
                Vec3 tip = WandFX.tipStart(caster);
                px = tip.x;
                py = tip.y;
                pz = tip.z;
            } catch (Exception ignored) {
                //TODO 1
            }
        }
        WandFX.arcBolt(level, px, py, pz, xx, yy, zz);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, chainlightning)) {
            return COST_CHAIN.copy();
        }
        if (isUpgradedWith(focusstack, earthshock)) {
            return COST_GROUND.copy();
        }
        return COST_BASE.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public float getTurretCorrection(ItemStack focusstack) {
        return isUpgradedWith(focusstack, earthshock) ? 5.0F : 0.0F;
    }

    @Override
    public float getTurretRange(ItemStack focusstack) {
        return 20.0F;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        // ВНИМАНИЕ: 500/1000/250 тиков = 25/50/12.5 сек. Проверьте контракт getActivationCooldown.
        return 20;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focusstack) {
        return isUpgradedWith(focusstack, earthshock) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        ItemStack focusStack = wand.getFocusStack(wandstack);
        if (isUpgradedWith(focusStack, earthshock)) {
            if (level.isClientSide) {
                return true;
            }
            if (!WandEntities.SHOCK_ORB.isPresent()) {
                return false;
            }
            EntityShockOrb orb = new EntityShockOrb(WandEntities.SHOCK_ORB.get(), level, entity);
            Vec3 spawn = handSpawn(entity);
            orb.setPos(spawn.x, spawn.y, spawn.z);
            orb.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
            orb.area += getUpgradeLevel(focusStack, FocusUpgradeType.enlarge) * 2;
            orb.damage += (int) (wand.getFocusPotency(wandstack) * 1.33D);
            level.addFreshEntity(orb);
            level.playSound(null, entity.blockPosition(), WandSounds.zap(), SoundSource.PLAYERS, 1.0F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
            return true;
        }
        doLightningBolt(wandstack, entity, useCount);
        return true;
    }
    private static Vec3 handSpawn(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        boolean mainWand = caster.getMainHandItem().getItem() instanceof IWand;
        boolean rightArm = mainWand == (caster.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT);
        double side = rightArm ? 0.30D : -0.30D;
        return new Vec3(caster.getX(), caster.getEyeY() - 0.1D, caster.getZ())
                .add(look.scale(0.35D))
                .add(right.scale(side));
    }

    public void doLightningBolt(ItemStack stack, LivingEntity caster, int count) {
        if (caster == null || !(stack.getItem() instanceof IWand wand)) {
            return;
        }
        Level level = caster.level();
        int potency = wand.getFocusPotency(stack);
        Entity pointed = WandTargets.getPointedEntityOrClose(level, caster, 0.0D, 20.0D, 1.1F,
                2.5D, 0.35D);

        if (level.isClientSide) {
            BlockHitResult mop = BlockUtils.getTargetBlock(level, caster, false);
            Vec3 look = caster.getLookAngle();
            double px = caster.getX() + look.x * 10.0D;
            double py = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.25D
                    + look.y * 10.0D;
            double pz = caster.getZ() + look.z * 10.0D;
            if (mop.getType() == HitResult.Type.BLOCK) {
                px = mop.getLocation().x;
                py = mop.getLocation().y;
                pz = mop.getLocation().z;
                for (int a = 0; a < 5; a++) {
                    WandFX.sparkle(level,
                            (float) px + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                            (float) py + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                            (float) pz + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F,
                            2.0F + level.random.nextFloat(), 2,
                            0.05F + level.random.nextFloat() * 0.05F);
                }
            }
            if (pointed != null) {
                px = pointed.getX();
                py = pointed.getBoundingBox().minY + pointed.getBbHeight() / 2.0F;
                pz = pointed.getZ();
                for (int a = 0; a < 5; a++) {
                    WandFX.sparkle(level,
                            (float) px + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                            (float) py + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                            (float) pz + (level.random.nextFloat() - level.random.nextFloat()) * 0.6F,
                            2.0F + level.random.nextFloat(), 2,
                            0.05F + level.random.nextFloat() * 0.05F);
                }
            }
            shootLightning(level, caster, px, py, pz);
            return;
        }

        level.playSound(null, caster.blockPosition(), WandSounds.shock(), SoundSource.PLAYERS, 0.25F, 1.0F);
        if (!(pointed instanceof LivingEntity target)) {
            return;
        }
        boolean pvpAllowed = !(target instanceof Player) || !(caster instanceof Player)
                || (level.getServer() != null && level.getServer().isPvpAllowed());
        if (!pvpAllowed) {
            return;
        }

        ItemStack focusStack = wand.getFocusStack(stack);
        int chain = getUpgradeLevel(focusStack, chainlightning) * 2;
        target.hurt(caster.damageSources().lightningBolt(), (chain > 0 ? 6 : 4) + potency);

        if (chain <= 0) {
            return;
        }
        chain += getUpgradeLevel(focusStack, FocusUpgradeType.enlarge) * 2;

        LivingEntity center = target;
        Set<Integer> hitIds = new HashSet<>();
        hitIds.add(target.getId());
        UUID casterUuid = caster.getUUID();

        while (chain-- > 0) {
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center.getX() - 8.0D, center.getY() - 8.0D, center.getZ() - 8.0D,
                            center.getX() + 8.0D, center.getY() + 8.0D, center.getZ() + 8.0D),
                    e -> {
                        if (hitIds.contains(e.getId()) || e.isRemoved()) return false;
                        if (e instanceof Player && caster instanceof Player) {
                            var server = level.getServer();
                            if (server == null || !server.isPvpAllowed()) return false;
                        }
                        if (e instanceof OwnableEntity own) {
                            UUID ownerUuid = own.getOwnerUUID();
                            return ownerUuid == null || !ownerUuid.equals(casterUuid);
                        }
                        return true;
                    });
            LivingEntity closest = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity e : nearby) {
                double dd = e.distanceTo(center);
                if (dd < best) {
                    best = dd;
                    closest = e;
                }
            }
            if (closest == null) {
                break;
            }
            PacketHandler.sendToNear(
                    new PacketFXZap(center.position(), closest.position(), 0xFFFFFF, 1.0F),
                    new PacketDistributor.TargetPoint(
                            center.getX(), center.getY(), center.getZ(), 64.0D, level.dimension()));
            hitIds.add(closest.getId());
            closest.hurt(caster.damageSources().lightningBolt(), 4 + potency);
            center = closest;
        }
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.enlarge) || isUpgradedWith(focusstack, chainlightning)
                || isUpgradedWith(focusstack, earthshock);
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 2 -> RANK_1_2;
            case 3 -> RANK_3;
            case 4, 5 -> RANK_4_5;
            default -> null;
        };
    }
}