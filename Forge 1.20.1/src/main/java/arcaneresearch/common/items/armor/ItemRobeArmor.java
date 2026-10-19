package arcaneresearch.common.items.armor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.client.models.armor.RobeArmorModel;
import arcaneresearch.init.ModItems;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemRobeArmor extends ArmorItem implements IVisDiscountGear, DyeableLeatherItem, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    private static final int DEFAULT_COLOR = 0x6A4C00;
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> robeModelOuter;
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> robeModelInner;
    public ItemRobeArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_SPECIAL, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON));
    }
    public static ItemRobeArmor createChest() {
        return new ItemRobeArmor(Type.CHESTPLATE);
    }
    public static ItemRobeArmor createLegs() {
        return new ItemRobeArmor(Type.LEGGINGS);
    }
    public static ItemRobeArmor createBoots() {
        return new ItemRobeArmor(Type.BOOTS);
    }
    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return this.getType() == Type.BOOTS ? 2 : 3;
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.ENCHANTED_FABRIC.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return type == null ? "arcaneresearch:textures/entity/armor/robes_2.png"
                    : "arcaneresearch:textures/entity/armor/robes_2_overlay.png";
        }
        return type == null ? "arcaneresearch:textures/entity/armor/robes_1.png"
                : "arcaneresearch:textures/entity/armor/robes_1_overlay.png";
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
                HumanoidModel<LivingEntity> robeModel;
                if (slot == EquipmentSlot.LEGS) {
                    if (robeModelInner == null) {
                        robeModelInner = new RobeArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        RobeArmorModel.INNER_LAYER_LOCATION));
                    }
                    robeModel = robeModelInner;
                } else {
                    if (robeModelOuter == null) {
                        robeModelOuter = new RobeArmorModel(
                                Minecraft.getInstance().getEntityModels().bakeLayer(
                                        RobeArmorModel.LAYER_LOCATION));
                    }
                    robeModel = robeModelOuter;
                }
                robeModel.crouching = original.crouching;
                robeModel.riding = original.riding;
                robeModel.young = original.young;
                robeModel.rightArmPose = original.rightArmPose;
                robeModel.leftArmPose = original.leftArmPose;
                robeModel.attackTime = original.attackTime;
                robeModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        robeModel.head.visible = true;
                        robeModel.hat.visible = true;
                    }
                    case CHEST -> {
                        robeModel.body.visible = true;
                        robeModel.rightArm.visible = true;
                        robeModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        robeModel.body.visible = true;
                        robeModel.rightLeg.visible = true;
                        robeModel.leftLeg.visible = true;
                    }
                    case FEET -> {
                        robeModel.rightLeg.visible = true;
                        robeModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return robeModel;
            }
        });
    }
    @Override
    public int getColor(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("display");
        return tag != null && tag.contains("color") ? tag.getInt("color") : DEFAULT_COLOR;
    }
    @Override
    public boolean hasCustomColor(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("display");
        return tag != null && tag.contains("color");
    }
    @Override
    public void clearColor(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("display");
        if (tag != null && tag.contains("color")) {
            tag.remove("color");
        }
    }
    @Override
    public void setColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color", color);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER_CAULDRON)) {
            ItemStack stack = context.getItemInHand();
            if (hasCustomColor(stack)) {
                if (!level.isClientSide()) {
                    clearColor(stack);
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useOn(context);
    }
}