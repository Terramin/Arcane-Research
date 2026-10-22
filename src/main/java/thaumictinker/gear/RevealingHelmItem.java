package thaumictinker.gear;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IVisDiscountGear;

 



 
public class RevealingHelmItem extends ArmorItem implements IVisDiscountGear, IRevealer, IGoggles, ICurioItem {

    public static final int VIS_DISCOUNT = 5;

    public RevealingHelmItem(Properties properties) {
        super(ArcaneresearchMaterials.ARMORMAT_THAUMIUM, Type.HELMET, properties);
    }

    @Override
    public String getArmorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/thaumium_1.png";
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return VIS_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tc.visdiscount").append(": " + VIS_DISCOUNT + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("head");
    }
}
