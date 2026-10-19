package arcaneresearch.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * Порт TC4 {@code IRunicArmor}: предмет даёт заряд рунного щита сам по себе
 * (в отличие от брони, где заряд пишет инфузия в NBT {@code TC.RUNIC}).
 */
public interface IRunicArmor {
    int getRunicCharge(ItemStack stack);
}
