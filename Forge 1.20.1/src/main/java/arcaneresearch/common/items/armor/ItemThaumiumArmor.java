package arcaneresearch.common.items.armor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.client.models.armor.ThaumiumArmorModel;
import arcaneresearch.init.ModItems;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemThaumiumArmor extends ArmorItem implements IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> thaumiumModelOuter;
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> thaumiumModelInner;
    public ItemThaumiumArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_THAUMIUM, type,
                new Item.Properties().rarity(Rarity.UNCOMMON));
    }
    public static ItemThaumiumArmor createHelmet() {
        return new ItemThaumiumArmor(Type.HELMET);
    }
    public static ItemThaumiumArmor createChestplate() {
        return new ItemThaumiumArmor(Type.CHESTPLATE);
    }
    public static ItemThaumiumArmor createLeggings() {
        return new ItemThaumiumArmor(Type.LEGGINGS);
    }
    public static ItemThaumiumArmor createBoots() {
        return new ItemThaumiumArmor(Type.BOOTS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return "arcaneresearch:textures/entity/armor/thaumium_2.png";
        }
        return "arcaneresearch:textures/entity/armor/thaumium_1.png";
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
                HumanoidModel<LivingEntity> thaumiumModel;
                if (slot == EquipmentSlot.LEGS) {
                    if (thaumiumModelInner == null) {
                        thaumiumModelInner = new ThaumiumArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        ThaumiumArmorModel.INNER_LAYER_LOCATION));
                    }
                    thaumiumModel = thaumiumModelInner;
                } else {
                    if (thaumiumModelOuter == null) {
                        thaumiumModelOuter = new ThaumiumArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        ThaumiumArmorModel.LAYER_LOCATION));
                    }
                    thaumiumModel = thaumiumModelOuter;
                }
                thaumiumModel.crouching = original.crouching;
                thaumiumModel.riding = original.riding;
                thaumiumModel.young = original.young;
                thaumiumModel.rightArmPose = original.rightArmPose;
                thaumiumModel.leftArmPose = original.leftArmPose;
                thaumiumModel.attackTime = original.attackTime;
                thaumiumModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        thaumiumModel.head.visible = true;
                        thaumiumModel.hat.visible = true;
                    }
                    case CHEST -> {
                        thaumiumModel.body.visible = true;
                        thaumiumModel.rightArm.visible = true;
                        thaumiumModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        thaumiumModel.body.visible = true;
                        thaumiumModel.rightLeg.visible = true;
                        thaumiumModel.leftLeg.visible = true;
                    }
                    case FEET -> {
                        thaumiumModel.rightLeg.visible = true;
                        thaumiumModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return thaumiumModel;
            }
        });
    }
}
