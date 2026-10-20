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
public class ItemFocusShock extends ItemFocusBasic {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.AIR, 8);
    private static final AspectList COST_CHAIN = new AspectList().add(Aspect.AIR, 8).add(Aspect.WATER, 8);
    private static final AspectList COST_GROUND = new AspectList().add(Aspect.AIR, 8).add(Aspect.EARTH, 8);
    public static FocusUpgradeType chainlightning = new FocusUpgradeType(
            new ResourceLocation("arcaneresearch", "foci/chainlightning.png"),
            "focus.upgrade.chainlightning.name", "focus.upgrade.chainlightning.text",
            new AspectList().add(Aspect.ENERGY, 1));
    public static FocusUpgradeType earthshock = new FocusUpgradeType(
            new ResourceLocation("arcaneresearch", "foci/earthshock.png"),
            "focus.upgrade.earthshock.name", "focus.upgrade.earthshock.text",
            new AspectList().add(Aspect.ENERGY, 1));
    public ItemFocusShock() {
        super("shock", 10466239);
    }
    public static void shootLightning(Level level, LivingEntity caster, double xx, double yy, double zz) {
        Vec3 look = caster.getLookAngle();
        double px = caster.getX()
                + (-Mth.cos(caster.getYRot() / 180.0F * (float) Math.PI) * 0.06F)
                + look.x * 0.5D;
        double py = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.25D - 0.06D
                + look.y * 0.5D;
        double pz = caster.getZ()
                + (-Mth.sin(caster.getYRot() / 180.0F * (float) Math.PI) * 0.06F)
                + look.z * 0.5D;
        if (level.isClientSide && caster instanceof Player) {
            try {
                Vec3 tip = WandFX.tipStart(caster);
                if (tip != null) {
                    px = tip.x;
                    py = tip.y;
                    pz = tip.z;
                }
            } catch (Throwable ignored) {
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
        return isUpgradedWith(focusstack, chainlightning) ? 500
                : (isUpgradedWith(focusstack, earthshock) ? 1000 : 250);
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
            if (!level.isClientSide) {
                EntityShockOrb orb = new EntityShockOrb(WandEntities.SHOCK_ORB.get(), level, entity);
                orb.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
                orb.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
                orb.area += getUpgradeLevel(focusStack, FocusUpgradeType.enlarge) * 2;
                orb.damage += (int) (wand.getFocusPotency(wandstack) * 1.33D);
                level.addFreshEntity(orb);
                level.playSound(null, orb.blockPosition(), WandSounds.zap(), SoundSource.PLAYERS, 1.0F,
                        1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
            }
            return true;
        }
        doLightningBolt(wandstack, entity, useCount);
        return true;
    }
    public void doLightningBolt(ItemStack stack, LivingEntity caster, int count) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return;
        }
        Level level = caster.level();
        int potency = wand.getFocusPotency(stack);
        Entity pointed = WandTargets.getPointedEntity(level, caster, 0.0D, 20.0D, 1.1F);
        if (level.isClientSide) {
            BlockHitResult mop = BlockUtils.getTargetBlock(level, caster, false);
            Vec3 look = caster.getLookAngle();
            double px = caster.getX() + look.x * 10.0D;
            double py = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.25D
                    + look.y * 10.0D;
            double pz = caster.getZ() + look.z * 10.0D;
            if (mop != null && mop.getType() == HitResult.Type.BLOCK) {
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
        if (pointed instanceof LivingEntity target
                && (!(target instanceof Player) || !(caster instanceof Player)
                || (level.getServer() != null && level.getServer().isPvpAllowed()))) {
            int chain = getUpgradeLevel(wand.getFocusStack(stack), chainlightning) * 2;
            target.hurt(caster.damageSources().lightningBolt(), (chain > 0 ? 6 : 4) + potency);
            if (chain > 0) {
                chain += getUpgradeLevel(wand.getFocusStack(stack), FocusUpgradeType.enlarge) * 2;
                LivingEntity center = target;
                Set<Integer> hitIds = new HashSet<>();
                hitIds.add(target.getId());
                while (chain > 0) {
                    chain--;
                    List<Entity> nearby = level.getEntitiesOfClass(Entity.class,
                            new AABB(center.getX() - 8.0D, center.getY() - 8.0D, center.getZ() - 8.0D,
                                    center.getX() + 8.0D, center.getY() + 8.0D, center.getZ() + 8.0D),
                            e -> e instanceof LivingEntity && !hitIds.contains(e.getId()) && !e.isRemoved()
                                    && !(e instanceof Player && caster instanceof Player
                                    && (level.getServer() == null || !level.getServer().isPvpAllowed()))
                                    && !(e instanceof OwnableEntity own && own.getOwnerUUID() != null
                                    && own.getOwnerUUID().equals(caster.getUUID())));
                    double best = Double.MAX_VALUE;
                    Entity closest = null;
                    for (Entity e : nearby) {
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
                            new net.minecraftforge.network.PacketDistributor.TargetPoint(
                                    center.getX(), center.getY(), center.getZ(), 64.0D, level.dimension()));
                    hitIds.add(closest.getId());
                    closest.hurt(caster.damageSources().lightningBolt(), 4 + potency);
                    if (closest instanceof LivingEntity living) {
                        center = living;
                    } else {
                        break;
                    }
                }
            }
        }
    }
    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        if (type.equals(FocusUpgradeType.enlarge) && !isUpgradedWith(focusstack, chainlightning)
                && !isUpgradedWith(focusstack, earthshock)) {
            return false;
        }
        return true;
    }
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, chainlightning,
                        earthshock};
            case 4:
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.enlarge};
            default:
                break;
        }
        return null;
    }
}
