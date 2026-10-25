package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.WandEntities;
import arcaneresearch.common.entities.projectile.EntityFrostShard;
import arcaneresearch.common.lib.wands.WandSounds;

public class ItemFocusFrost extends ItemFocusBasic {

    private static final AspectList COST_BASE = new AspectList().add(Aspect.WATER, 2).add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 2);
    private static final AspectList COST_SCATTER = new AspectList().add(Aspect.WATER, 2).add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(Aspect.AIR, 2);
    private static final AspectList COST_BOULDER = new AspectList().add(Aspect.WATER, 2).add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(Aspect.EARTH, 2);

    public static final FocusUpgradeType scattershot = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/scattershot.png"),
            "focus.upgrade.scattershot.name", "focus.upgrade.scattershot.text",
            new AspectList().add(Aspect.COLD, 1).add(Aspect.AVERSION, 1));
    public static final FocusUpgradeType iceboulder = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/iceboulder.png"),
            "focus.upgrade.iceboulder.name", "focus.upgrade.iceboulder.text",
            new AspectList().add(Aspect.COLD, 1).add(Aspect.CRYSTAL, 1));

    public ItemFocusFrost() {
        super("frost", 5204428);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, iceboulder)) {
            return COST_BOULDER.copy();
        }
        if (isUpgradedWith(focusstack, scattershot)) {
            return COST_SCATTER.copy();
        }
        return COST_BASE.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public float getTurretCorrection(ItemStack focusstack) {
        return isUpgradedWith(focusstack, scattershot) ? 1.0F : 15.0F;
    }

    @Override
    public float getTurretRange(ItemStack focusstack) {
        return isUpgradedWith(focusstack, scattershot) ? 20.0F : super.getTurretRange(focusstack);
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        boolean upgraded = isUpgradedWith(focusstack, scattershot) || isUpgradedWith(focusstack, iceboulder);
        return upgraded ? 500 : 200;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }

        ItemStack focusStack = wand.getFocusStack(wandstack);
        int frosty = getUpgradeLevel(focusStack, FocusUpgradeType.alchemistsfrost);
        int potency = wand.getFocusPotency(wandstack);

        if (isUpgradedWith(focusStack, scattershot)) {
            int count = 5 + potency * 2;
            for (int a = 0; a < count; a++) {
                EntityFrostShard shard = new EntityFrostShard(WandEntities.FROST_SHARD.get(), level, entity, 8.0F);
                shard.setDamage(1.0F);
                shard.fragile = true;
                shard.setFrosty(frosty);
                level.addFreshEntity(shard);
            }
        } else if (isUpgradedWith(focusStack, iceboulder)) {
            EntityFrostShard shard = new EntityFrostShard(WandEntities.FROST_SHARD.get(), level, entity, 1.0F);
            shard.setDamage(4 + potency * 2);
            shard.bounce = 0.8D;
            shard.bounceLimit = 6;
            shard.setFrosty(frosty);
            level.addFreshEntity(shard);
        } else {
            EntityFrostShard shard = new EntityFrostShard(WandEntities.FROST_SHARD.get(), level, entity, 1.0F);
            shard.setDamage((float) (3.0D + potency * 1.5D));
            shard.setFrosty(frosty);
            level.addFreshEntity(shard);
        }

        level.playSound(null, entity.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.4F,
                1.0F + level.random.nextFloat() * 0.1F);
        return true;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.alchemistsfrost};
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, scattershot,
                        iceboulder, FocusUpgradeType.alchemistsfrost};
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.alchemistsfrost};
            default:
                break;
        }
        return null;
    }
}