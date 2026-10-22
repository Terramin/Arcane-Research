package thaumictinker.kami.foci;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import thaumictinker.foci.TtFocusBase;
import thaumictinker.kami.gateway.GatewayTeleport;
import thaumictinker.kami.gateway.PearlLocation;
import thaumictinker.kami.gateway.SkyPearlItem;

 




 
public class RecallFocus extends TtFocusBase {

    public static final int CHANNEL_TICKS = 60;
    public static final int USE_DURATION = 72000;
    private static final AspectList COST = new AspectList().add(Aspect.AIR, 4000).add(Aspect.EARTH, 4000).add(Aspect.ORDER, 4000);

    public RecallFocus() {
        super("focusrecall", 0x9CF8FF);
    }

    @Override
    public net.minecraft.world.item.Rarity getRarity(ItemStack stack) {
        
        return net.minecraft.world.item.Rarity.EPIC;
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9CF8FF;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "RECALL";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.frugal};
    }

    @Nullable
    public static PearlLocation boundPearl(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            PearlLocation location = SkyPearlItem.location(player.getInventory().getItem(slot));
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player)) {
            return true;
        }
        int elapsed = USE_DURATION - useCount;
        
        
        
        
        if (!(player instanceof ServerPlayer serverPlayer) || elapsed <= CHANNEL_TICKS) {
            return false;
        }
        PearlLocation location = boundPearl(serverPlayer);
        IWand wandItem = (IWand) wand.getItem();
        if (location != null && location.dimension().equals(serverPlayer.level().dimension())
                && wandItem.consumeAllVis(wand, serverPlayer, COST.copy(), false, false)
                && GatewayTeleport.teleport(serverPlayer, location)) {
            wandItem.consumeAllVis(wand, serverPlayer, COST.copy(), true, false);
        }
        player.stopUsingItem();
        return false;
    }
}
