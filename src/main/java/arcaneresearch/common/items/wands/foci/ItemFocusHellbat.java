package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.entities.WandEntities;
import arcaneresearch.common.entities.monster.EntityHellbat;
import arcaneresearch.common.lib.wands.WandResearchHelper;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.lib.wands.WandTargets;

public class ItemFocusHellbat extends ItemFocusBasic {

    private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 5).add(Aspect.AIR, 5);
    private static final AspectList COST_BOMB = new AspectList().add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 10).add(Aspect.AIR, 5);
    private static final AspectList COST_DEVIL = new AspectList().add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 5).add(Aspect.AIR, 5).add(Aspect.EARTH, 5);

    public static final FocusUpgradeType batbombs = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/batbombs.png"),
            "focus.upgrade.batbombs.name", "focus.upgrade.batbombs.text",
            new AspectList().add(Aspect.ENERGY, 1).add(Aspect.TRAP, 1));
    public static final FocusUpgradeType devilbats = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/devilbats.png"),
            "focus.upgrade.devilbats.name", "focus.upgrade.devilbats.text",
            new AspectList().add(Aspect.PROTECT, 1));
    public static final FocusUpgradeType vampirebats = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/vampirebats.png"),
            "focus.upgrade.vampirebats.name", "focus.upgrade.vampirebats.text",
            new AspectList().add(Aspect.DESIRE, 1).add(Aspect.LIFE, 1));

    public ItemFocusHellbat(net.minecraft.world.item.Item.Properties props) {
        super("bat", 14431746, props);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, devilbats)) {
            return COST_DEVIL.copy();
        }
        if (isUpgradedWith(focusstack, batbombs)) {
            return COST_BOMB.copy();
        }
        return COST_BASE.copy();
    }

    @Override
    public boolean canBePlacedInTurret() {
        return true;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 1000;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return false;
        }
        Entity pointed = WandTargets.getPointedEntity(level, entity, 32.0D, LivingEntity.class);
        Vec3 look = entity.getLookAngle();
        double px = entity.getX() - Mth.cos(entity.getYRot() / 180.0F * (float) Math.PI) * 0.16F
                + look.x * 0.5D;
        double py = entity.getBoundingBox().minY + entity.getBbHeight() / 2.0F + 0.25D - 0.05D + look.y * 0.5D;
        double pz = entity.getZ() - Mth.sin(entity.getYRot() / 180.0F * (float) Math.PI) * 0.16F
                + look.z * 0.5D;
        if (pointed instanceof LivingEntity target) {
            if (!level.isClientSide()) {
                if (target instanceof Player
                        && (!(level instanceof net.minecraft.server.level.ServerLevel pvpLevel) || !pvpLevel.isPvpAllowed())) {
                    return false;
                }
                EntityHellbat bat = new EntityHellbat(WandEntities.HELLBAT.get(), level);
                bat.setPos(px, py + bat.getBbHeight(), pz);
                bat.setYRot(entity.getYRot());
                bat.setTarget(target);
                bat.setIsBatHanging(false);
                bat.setIsSummoned(true);
                bat.owner = entity;
                bat.damBonus = wand.getFocusPotency(wandstack);
                bat.setIsDevil(isUpgradedWith(wand.getFocusStack(wandstack), devilbats));
                bat.setIsExplosive(isUpgradedWith(wand.getFocusStack(wandstack), batbombs));
                bat.setIsVampire(isUpgradedWith(wand.getFocusStack(wandstack), vampirebats));
                bat.updateHellbatAttack();

                bat.setPersistenceRequired();
                if (level.addFreshEntity(bat)) {

                    level.levelEvent(null, 2004, new BlockPos((int) px, (int) py, (int) pz), 0);
                    level.playSound(null, bat.blockPosition(), WandSounds.ice(), SoundSource.PLAYERS, 0.2F,
                            0.95F + level.getRandom().nextFloat() * 0.1F);
                } else {
                    level.playSound(null, entity.blockPosition(), WandSounds.wandFail(), SoundSource.PLAYERS,
                            0.1F, 0.8F + level.getRandom().nextFloat() * 0.1F);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
            case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, batbombs, devilbats};
            case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, vampirebats};
            default -> null;
        };
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(vampirebats) || WandResearchHelper.isResearchComplete(player, "VAMPBAT");
    }
}