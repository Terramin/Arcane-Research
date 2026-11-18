package arcaneresearch.common.items.tools;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemElementalPickaxe extends PickaxeItem {
    public ItemElementalPickaxe() {
        super(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 1, -2.8F,
                new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || isPvPEnabled(attacker.level()))) {
            target.setSecondsOnFire(2);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private boolean isPvPEnabled(Level level) {
        if (level.getServer() != null) {
            return level.getServer().isPvpAllowed();
        }
        return true;
    }
}
