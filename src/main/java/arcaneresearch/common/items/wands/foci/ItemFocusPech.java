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
import arcaneresearch.common.entities.projectile.EntityPechBlast;
import arcaneresearch.common.lib.wands.WandSounds;

public class ItemFocusPech extends ItemFocusBasic {

    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2).add(Aspect.WATER, 2);
    private static final AspectList COST_ALL = new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1)
            .add(Aspect.EARTH, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1).add(Aspect.WATER, 1);

    public static final FocusUpgradeType nightshade = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/nightshade.png"),
            "focus.upgrade.nightshade.name", "focus.upgrade.nightshade.text",
            new AspectList().add(Aspect.LIFE, 1).add(Aspect.FLUX, 1).add(Aspect.ENERGY, 1));

    public ItemFocusPech() {
        super("pech", 2267460);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return isUpgradedWith(focusstack, nightshade) ? COST_ALL.copy() : COST.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public float getTurretCorrection(ItemStack focusstack) {
        return 12.0F;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 20;
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
        int potency = wand.getFocusPotency(wandstack);
        int extend = wand.getFocusExtend(wandstack);
        boolean nightshadeOn = isUpgradedWith(focusStack, nightshade);

        EntityPechBlast blast = new EntityPechBlast(WandEntities.PECH_BLAST.get(), level, entity,
                potency, extend, nightshadeOn);
        blast.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
        blast.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(blast);
        level.playSound(null, entity.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.4F,
                1.0F + level.random.nextFloat() * 0.1F);
        return true;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.extend};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.extend};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, nightshade};
            default:
                break;
        }
        return null;
    }
}