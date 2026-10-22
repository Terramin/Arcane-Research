package thaumictinker.kami;

import javax.annotation.Nullable;

import arcaneresearch.api.items.IVisDiscountGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

 



 
public class IchorclothArmorItem extends ArmorItem implements IVisDiscountGear {

      
    public static final String ARMOR_TEXTURE = "thaumictinker:textures/model/ichor1.png";
    public static final String ARMOR_TEXTURE_OVERLAY = "thaumictinker:textures/model/ichor2.png";

    private final int visDiscount;

    public IchorclothArmorItem(Type type, Properties properties, int visDiscount) {
        super(KamiFeature.ICHORCLOTH_ARMOR, type, properties);
        this.visDiscount = visDiscount;
    }

     



 
    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
                                  EquipmentSlot slot, String type) {
        return type == null ? ARMOR_TEXTURE : ARMOR_TEXTURE_OVERLAY;
    }

    @Override
    public boolean canBeDepleted() {
        
        
        return false;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return visDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tc.visdiscount").append(": " + visDiscount + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
