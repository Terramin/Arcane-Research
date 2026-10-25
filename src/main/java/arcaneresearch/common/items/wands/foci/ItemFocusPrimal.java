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
import arcaneresearch.common.entities.projectile.EntityPrimalOrb;
import arcaneresearch.common.lib.wands.WandSounds;
import java.util.Random;
public class ItemFocusPrimal extends ItemFocusBasic {
    public static FocusUpgradeType seeker = new FocusUpgradeType(
            new ResourceLocation("arcaneresearch", "foci/seeker.png"),
            "focus.upgrade.seeker.name", "focus.upgrade.seeker.text",
            new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1));
    public ItemFocusPrimal() {
        super("primal", 10854849);
    }
    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        Random rand = new Random(System.currentTimeMillis() / 200L);
        return new AspectList().add(Aspect.WATER, 10 + rand.nextInt(5) * 2).add(Aspect.AIR, 10 + rand.nextInt(5) * 2)
                .add(Aspect.EARTH, 10 + rand.nextInt(5) * 2).add(Aspect.FIRE, 10 + rand.nextInt(5) * 2)
                .add(Aspect.ORDER, 10 + rand.nextInt(5) * 2).add(Aspect.ENTROPY, 10 + rand.nextInt(5) * 2);
    }
    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }
    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 500;
    }
    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (!level.isClientSide) {
            EntityPrimalOrb orb = new EntityPrimalOrb(WandEntities.PRIMAL_ORB.get(), level, entity,
                    isUpgradedWith(wand.getFocusStack(wandstack), seeker));
            orb.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
            orb.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(orb);
            level.playSound(null, orb.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.3F,
                    0.8F + level.random.nextFloat() * 0.1F);
        }
        return true;
    }
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, seeker};
            case 4:
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal};
            default:
                break;
        }
        return null;
    }
}
