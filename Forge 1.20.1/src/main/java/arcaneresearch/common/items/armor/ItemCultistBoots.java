package arcaneresearch.common.items.armor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;
import javax.annotation.Nullable;
public class ItemCultistBoots extends ArmorItem implements IVisDiscountGear, IWarpingGear, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    public ItemCultistBoots() {
        super(ArmorMaterials.IRON, Type.BOOTS,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON));
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/cultistboots.png";
    }
    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 1;
    }
    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
