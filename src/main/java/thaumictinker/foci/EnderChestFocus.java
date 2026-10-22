package thaumictinker.foci;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

 

 
public class EnderChestFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 100).add(Aspect.ORDER, 100);

    public EnderChestFocus() {
        super("focusenderchest", 0x132223);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x132223;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "ENDERCHEST";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (!level.isClientSide) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> ChestMenu.threeRows(containerId, inventory, menuPlayer.getEnderChestInventory()),
                    Component.translatable("container.enderchest")));
            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
