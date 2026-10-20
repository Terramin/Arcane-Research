package arcaneresearch.common.items.armor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.client.models.armor.VoidArmorModel;
import arcaneresearch.init.ModItems;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemVoidArmor extends ArmorItem implements IWarpingGear, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> voidModelOuter;
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> voidModelInner;
    public ItemVoidArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_VOID, type,
                new Item.Properties().rarity(Rarity.UNCOMMON));
    }
    public static ItemVoidArmor createHelmet() {
        return new ItemVoidArmor(Type.HELMET);
    }
    public static ItemVoidArmor createChestplate() {
        return new ItemVoidArmor(Type.CHESTPLATE);
    }
    public static ItemVoidArmor createLeggings() {
        return new ItemVoidArmor(Type.LEGGINGS);
    }
    public static ItemVoidArmor createBoots() {
        return new ItemVoidArmor(Type.BOOTS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && stack.isDamaged() && entity != null
                && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return "arcaneresearch:textures/entity/armor/void_2.png";
        }
        return "arcaneresearch:textures/entity/armor/void_1.png";
    }
    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            @Nonnull
            @OnlyIn(Dist.CLIENT)
            @SuppressWarnings("unchecked")
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                HumanoidModel<LivingEntity> voidModel;
                if (slot == EquipmentSlot.LEGS) {
                    if (voidModelInner == null) {
                        voidModelInner = new VoidArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        VoidArmorModel.INNER_LAYER_LOCATION));
                    }
                    voidModel = voidModelInner;
                } else {
                    if (voidModelOuter == null) {
                        voidModelOuter = new VoidArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        VoidArmorModel.LAYER_LOCATION));
                    }
                    voidModel = voidModelOuter;
                }
                voidModel.crouching = original.crouching;
                voidModel.riding = original.riding;
                voidModel.young = original.young;
                voidModel.rightArmPose = original.rightArmPose;
                voidModel.leftArmPose = original.leftArmPose;
                voidModel.attackTime = original.attackTime;
                voidModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        voidModel.head.visible = true;
                        voidModel.hat.visible = true;
                    }
                    case CHEST -> {
                        voidModel.body.visible = true;
                        voidModel.rightArm.visible = true;
                        voidModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        voidModel.body.visible = true;
                        voidModel.rightLeg.visible = true;
                        voidModel.leftLeg.visible = true;
                    }
                    case FEET -> {
                        voidModel.rightLeg.visible = true;
                        voidModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return voidModel;
            }
        });
    }
}
