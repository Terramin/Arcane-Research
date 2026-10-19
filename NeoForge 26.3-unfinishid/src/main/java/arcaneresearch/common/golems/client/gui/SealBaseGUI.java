package arcaneresearch.common.golems.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.client.gui.screens.SealScreen;
import arcaneresearch.common.menu.SealMenu;

@Deprecated
public class SealBaseGUI extends SealScreen {
    public SealBaseGUI(Inventory playerInventory, Level level, ISealEntity seal) {
        super(new SealMenu(0, playerInventory, seal), playerInventory,
                Component.translatable("gui.arcaneresearch.seal"));
    }
}