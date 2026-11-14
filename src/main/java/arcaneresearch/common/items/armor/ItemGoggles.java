package arcaneresearch.common.items.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;
import javax.annotation.Nullable;

public class ItemGoggles extends ArmorItem implements IVisDiscountGear, IRevealer, IGoggles, ICurioItem, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public ItemGoggles() {
        super(ArcaneresearchMaterials.ARMORMAT_SPECIAL, Type.HELMET,
                new Item.Properties()
                        .stacksTo(1)
                        .durability(350)
                        .rarity(Rarity.RARE));
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.BRASS_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/googles.png";
    }

    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> gogglesArmorModel;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (gogglesArmorModel == null) {
                    gogglesArmorModel = new HumanoidModel<>(
                            Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
                }
                gogglesArmorModel.setAllVisible(false);
                gogglesArmorModel.crouching = original.crouching;
                gogglesArmorModel.riding = original.riding;
                gogglesArmorModel.young = original.young;
                return gogglesArmorModel;
            }
        });
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("head");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
