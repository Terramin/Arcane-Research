package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.projectile.EntityGrapple;
import arcaneresearch.common.lib.wands.WandSounds;

public class ItemFocusGrapple extends ItemFocusBasic {

    private static final AspectList COST = new AspectList().add(Aspect.WATER, 10).add(Aspect.AIR, 10);

    public static final FocusUpgradeType sticky = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/sticky.png"),
            "focus.upgrade.sticky.name", "focus.upgrade.sticky.text",
            new AspectList().add(Aspect.TRAP, 1));

    private static final FocusUpgradeType[] RANK_1_2 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency};
    private static final FocusUpgradeType[] RANK_3 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency, sticky};
    private static final FocusUpgradeType[] RANK_4_5 = {
            FocusUpgradeType.frugal, FocusUpgradeType.potency};

    public ItemFocusGrapple() {
        super("grapple", 1381887);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return COST.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return false;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 250;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (!level.isClientSide) {
            InteractionHand hand = entity.getMainHandItem().getItem() instanceof IWand
                    ? InteractionHand.MAIN_HAND
                    : InteractionHand.OFF_HAND;
            EntityGrapple grapple = new EntityGrapple(level, entity, hand);
            grapple.ampl = wand.getFocusPotency(wandstack);
            grapple.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
            grapple.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(grapple);
            level.playSound(null, entity.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.3F,
                    0.8F + level.random.nextFloat() * 0.1F);
        }
        return true;
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