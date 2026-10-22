package thaumictinker.kami.foci;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import thaumictinker.foci.TtFocusBase;

 




 
public class XpDrainFocus extends TtFocusBase {

    public static final int XP_USE = 15;
    public static final int VIS_PER_TICK = 500;

    private static IntSupplier ticks = () -> 0;
    private static boolean clientTicks;

    private int lastGiven;

    public XpDrainFocus() {
        super("focusxpdrain", 0xFFFFFF);
    }

    @Override
    public net.minecraft.world.item.Rarity getRarity(ItemStack stack) {
        
        return net.minecraft.world.item.Rarity.EPIC;
    }

    public static void useClientTicks(IntSupplier source) {
        ticks = source;
        clientTicks = true;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "XPDRAIN";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return clientTicks ? Mth.hsvToRgb(ticks.getAsInt() * 2 % 360 / 360.0F, 1.0F, 1.0F) : 0xFFFFFF;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return new AspectList();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.Focus.cost2"));
        tooltip.add(Component.literal(" §a")
                .append(Component.translatable("ttinkerer.ttmisc.experience"))
                .append("§r x " + XP_USE));
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.frugal};
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player) || player.level().isClientSide()
                || !(level instanceof ServerLevel)) {
            return true;
        }
        IWand wandItem = (IWand) wand.getItem();
        List<Aspect> primals = new ArrayList<>(Aspect.getPrimalAspects());
        int max = wandItem.getMaxVis(wand);
        Aspect aspectToAdd = null;
        int takes = 0;
        while (aspectToAdd == null && takes < 7) {
            lastGiven = lastGiven >= primals.size() - 1 ? 0 : lastGiven + 1;
            Aspect aspect = primals.get(lastGiven);
            if (wandItem.getVis(wand, aspect) < max) {
                aspectToAdd = aspect;
            }
            takes++;
        }
        if (aspectToAdd != null && player.totalExperience >= XP_USE) {
            player.giveExperiencePoints(-XP_USE);
            wandItem.addVis(wand, aspectToAdd, Math.min(max, wandItem.getVis(wand, aspectToAdd) + VIS_PER_TICK), false);
        }
        return true;
    }
}
