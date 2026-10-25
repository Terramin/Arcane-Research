package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.projectile.EntityHomingShard;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.lib.wands.WandTargets;

public class ItemFocusShard extends ItemFocusBasic {

    private static final AspectList COST = new AspectList().add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(Aspect.AIR, 1);
    private static final AspectList COST_P = new AspectList().add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(Aspect.WATER, 1).add(Aspect.AIR, 1);

    public static final FocusUpgradeType persistant = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/persistant.png"),
            "focus.upgrade.persistant.name", "focus.upgrade.persistant.text",
            new AspectList().add(Aspect.PROTECT, 1).add(Aspect.AVERSION, 1).add(Aspect.ENERGY, 1));

    private static final FocusUpgradeType[] RANK_1_TO_4 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency};
    private static final FocusUpgradeType[] RANK_5 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency, persistant};

    public ItemFocusShard() {
        super("shard", 10037693);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return isUpgradedWith(focusstack, persistant) ? COST_P.copy() : COST.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 300;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        Entity pointed = WandTargets.getPointedEntity(level, entity, 0.0D, 32.0D, 1.1F);
        if (!(pointed instanceof LivingEntity target)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }

        ItemStack focusStack = wand.getFocusStack(wandstack);
        int potency = wand.getFocusPotency(wandstack);
        boolean persistentOn = isUpgradedWith(focusStack, persistant);

        EntityHomingShard blast = new EntityHomingShard(level, entity, target, potency, persistentOn);
        blast.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
        blast.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(blast);
        level.playSound(null, entity.blockPosition(), WandSounds.scan(), SoundSource.PLAYERS, 0.3F,
                1.1F + level.random.nextFloat() * 0.1F);
        return true;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 2, 3, 4 -> RANK_1_TO_4;
            case 5 -> RANK_5;
            default -> null;
        };
    }
}