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
    public static FocusUpgradeType persistant = new FocusUpgradeType(
            new ResourceLocation("arcaneresearch", "foci/persistant.png"),
            "focus.upgrade.persistant.name", "focus.upgrade.persistant.text",
            new AspectList().add(Aspect.PROTECT, 1).add(Aspect.AVERSION, 1).add(Aspect.ENERGY, 1));
    public ItemFocusShard() {
        super("shard", 10037693);
    }
    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, persistant)) {
            return COST_P.copy();
        }
        return COST.copy();
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
        if (pointed instanceof LivingEntity target) {
            if (!level.isClientSide) {
                EntityHomingShard blast = new EntityHomingShard(level, entity,
                        target, wand.getFocusPotency(wandstack),
                        isUpgradedWith(wand.getFocusStack(wandstack), persistant));
                blast.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
                blast.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(blast);
                level.playSound(null, blast.blockPosition(), WandSounds.scan(), SoundSource.PLAYERS, 0.3F,
                        1.1F + level.random.nextFloat() * 0.1F);
            }
            return true;
        }
        return false;
    }
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
            case 2:
            case 3:
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, persistant};
            default:
                break;
        }
        return null;
    }
}
