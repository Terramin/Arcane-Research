package arcaneresearch.common.items.armor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.client.models.armor.CultistRobeArmorModel;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemCultistRobeArmor extends ArmorItem implements IVisDiscountGear, IWarpingGear, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> cultistRobeModel;
    public ItemCultistRobeArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_CULTIST_ROBE, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON));
    }
    public static ItemCultistRobeArmor createHelmet() {
        return new ItemCultistRobeArmor(Type.HELMET);
    }
    public static ItemCultistRobeArmor createChestplate() {
        return new ItemCultistRobeArmor(Type.CHESTPLATE);
    }
    public static ItemCultistRobeArmor createLeggings() {
        return new ItemCultistRobeArmor(Type.LEGGINGS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/cultist_robe_armor.png";
    }
    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 1;
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
                if (cultistRobeModel == null) {
                    cultistRobeModel = new CultistRobeArmorModel(
                            Minecraft.getInstance().getEntityModels().bakeLayer(
                                    CultistRobeArmorModel.LAYER_LOCATION));
                }
                cultistRobeModel.crouching = original.crouching;
                cultistRobeModel.riding = original.riding;
                cultistRobeModel.young = original.young;
                cultistRobeModel.rightArmPose = original.rightArmPose;
                cultistRobeModel.leftArmPose = original.leftArmPose;
                cultistRobeModel.attackTime = original.attackTime;
                cultistRobeModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        cultistRobeModel.head.visible = true;
                        cultistRobeModel.hat.visible = true;
                    }
                    case CHEST -> {
                        cultistRobeModel.body.visible = true;
                        cultistRobeModel.rightArm.visible = true;
                        cultistRobeModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        cultistRobeModel.body.visible = true;
                        cultistRobeModel.rightLeg.visible = true;
                        cultistRobeModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return cultistRobeModel;
            }
        });
    }
}
