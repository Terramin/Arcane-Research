package thaumictinker.foci;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.init.ModSounds;

 




 
public class HealFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 45).add(Aspect.WATER, 45);
    private static final Map<String, Integer> PROGRESS = new HashMap<>();

    public HealFocus() {
        super("focusheal", 0xFD00D6);
    }

    private static String key(Player player) {
        return (player.level().isClientSide ? "R" : "S") + player.getUUID();
    }

      
    public static void reset(Player player) {
        PROGRESS.remove(key(player));
    }

    public static int threshold(int potency) {
        return 30 - potency * 10 / 3;
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFD00D6;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "HEAL";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.treasure, FocusUpgradeType.potency};
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player) || !player.isHurt()) {
            return false;
        }
        IWand wandItem = (IWand) wand.getItem();
        int potency = wandItem.getFocusPotency(wand);
        String progressKey = key(player);
        int progress = PROGRESS.getOrDefault(progressKey, 0) + 1;
        PROGRESS.put(progressKey, progress);
        FociFx.sparkle(level, player,
                player.getX() + level.random.nextFloat() - 0.5F,
                player.getY() + level.random.nextFloat(),
                player.getZ() + level.random.nextFloat() - 0.5F,
                1.0F, 0.1F, 0.85F);
        if (progress < threshold(potency)) {
            return false;
        }
        PROGRESS.put(progressKey, 0);
        if (level instanceof ServerLevel serverLevel
                && wandItem.consumeAllVis(wand, player, COST.copy(), !level.isClientSide, false)) {
            player.heal(1.0F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        return false;
    }
}
