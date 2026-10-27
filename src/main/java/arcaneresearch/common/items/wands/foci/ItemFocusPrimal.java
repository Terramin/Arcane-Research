package arcaneresearch.common.items.wands.foci;

import net.minecraft.resources.Identifier;
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
    public static final FocusUpgradeType seeker = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/seeker.png"),
            "focus.upgrade.seeker.name", "focus.upgrade.seeker.text",
            new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1));

    private static final long COST_WINDOW_MS = 200L;
    private static long cachedWindow = -1L;
    private static AspectList cachedCost = null;

    private static synchronized AspectList getWindowCost() {
        long window = System.currentTimeMillis() / COST_WINDOW_MS;
        if (window != cachedWindow) {
            cachedWindow = window;
            Random rand = new Random(window);
            cachedCost = new AspectList()
                    .add(Aspect.WATER, 10 + rand.nextInt(5) * 2)
                    .add(Aspect.AIR, 10 + rand.nextInt(5) * 2)
                    .add(Aspect.EARTH, 10 + rand.nextInt(5) * 2)
                    .add(Aspect.FIRE, 10 + rand.nextInt(5) * 2)
                    .add(Aspect.ORDER, 10 + rand.nextInt(5) * 2)
                    .add(Aspect.ENTROPY, 10 + rand.nextInt(5) * 2);
        }
        return cachedCost.copy();
    }

    public ItemFocusPrimal(net.minecraft.world.item.Item.Properties props) {
        super("primal", 10854849, props);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return getWindowCost();
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
        if (level.isClientSide()) {
            return true;
        }
        if (!WandEntities.PRIMAL_ORB.isBound()) {
            return false;
        }

        EntityPrimalOrb orb = new EntityPrimalOrb(WandEntities.PRIMAL_ORB.get(), level, entity,
                isUpgradedWith(wand.getFocusStack(wandstack), seeker));
        orb.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());
        orb.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(orb);
        level.playSound(null, entity.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.3F,
                0.8F + level.getRandom().nextFloat() * 0.1F);
        return true;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 2, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, seeker};
            default -> null;
        };
    }
}