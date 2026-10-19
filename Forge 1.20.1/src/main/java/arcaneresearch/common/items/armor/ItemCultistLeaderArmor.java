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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemCultistLeaderArmor extends ArmorItem implements IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> leaderModel;
    public ItemCultistLeaderArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_CULTIST_LEADER, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.RARE));
    }
    public static ItemCultistLeaderArmor createHelmet() {
        return new ItemCultistLeaderArmor(Type.HELMET);
    }
    public static ItemCultistLeaderArmor createChestplate() {
        return new ItemCultistLeaderArmor(Type.CHESTPLATE);
    }
    public static ItemCultistLeaderArmor createLeggings() {
        return new ItemCultistLeaderArmor(Type.LEGGINGS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.IRON_INGOT) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/cultist_leader_armor.png";
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
                if (leaderModel == null) {
                    leaderModel = new arcaneresearch.client.models.armor.LeaderArmorModel(
                            Minecraft.getInstance().getEntityModels().bakeLayer(
                                    arcaneresearch.client.models.armor.LeaderArmorModel.LAYER_LOCATION));
                }
                leaderModel.crouching = original.crouching;
                leaderModel.riding = original.riding;
                leaderModel.young = original.young;
                leaderModel.rightArmPose = original.rightArmPose;
                leaderModel.leftArmPose = original.leftArmPose;
                leaderModel.attackTime = original.attackTime;
                leaderModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        leaderModel.head.visible = true;
                        leaderModel.hat.visible = true;
                    }
                    case CHEST -> {
                        leaderModel.body.visible = true;
                        leaderModel.rightArm.visible = true;
                        leaderModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        leaderModel.body.visible = true;
                        leaderModel.rightLeg.visible = true;
                        leaderModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return leaderModel;
            }
        });
    }
}
