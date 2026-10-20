package arcaneresearch.common.items.armor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.client.models.armor.CultistPlateArmorModel;
import arcaneresearch.common.entities.monster.EntityInhabitedZombie;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemCultistPlateArmor extends ArmorItem implements IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> cultistPlateModel;
    public ItemCultistPlateArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_CULTIST_PLATE, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON));
    }
    public static ItemCultistPlateArmor createHelmet() {
        return new ItemCultistPlateArmor(Type.HELMET);
    }
    public static ItemCultistPlateArmor createChestplate() {
        return new ItemCultistPlateArmor(Type.CHESTPLATE);
    }
    public static ItemCultistPlateArmor createLeggings() {
        return new ItemCultistPlateArmor(Type.LEGGINGS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (entity instanceof EntityInhabitedZombie) {
            return "arcaneresearch:textures/entity/armor/zombie_plate_armor.png";
        }
        return "arcaneresearch:textures/entity/armor/cultist_plate_armor.png";
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
                if (cultistPlateModel == null) {
                    cultistPlateModel = new CultistPlateArmorModel(
                            Minecraft.getInstance().getEntityModels().bakeLayer(
                                    CultistPlateArmorModel.LAYER_LOCATION));
                }
                cultistPlateModel.crouching = original.crouching;
                cultistPlateModel.riding = original.riding;
                cultistPlateModel.young = original.young;
                cultistPlateModel.rightArmPose = original.rightArmPose;
                cultistPlateModel.leftArmPose = original.leftArmPose;
                cultistPlateModel.attackTime = original.attackTime;
                cultistPlateModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        cultistPlateModel.head.visible = true;
                        cultistPlateModel.hat.visible = true;
                    }
                    case CHEST -> {
                        cultistPlateModel.body.visible = true;
                        cultistPlateModel.rightArm.visible = true;
                        cultistPlateModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        cultistPlateModel.body.visible = true;
                        cultistPlateModel.rightLeg.visible = true;
                        cultistPlateModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return cultistPlateModel;
            }
        });
    }
}
