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
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.client.models.armor.VoidRobeArmorModel;
import arcaneresearch.init.ModItems;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class ItemVoidRobeArmor extends ArmorItem
        implements IVisDiscountGear, IGoggles, IRevealer, IWarpingGear, DyeableLeatherItem, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    private static final int DEFAULT_COLOR = 6961280;
    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> voidRobeModel;
    public ItemVoidRobeArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_VOIDROBE, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.EPIC));
    }
    public static ItemVoidRobeArmor createHelmet() {
        return new ItemVoidRobeArmor(Type.HELMET);
    }
    public static ItemVoidRobeArmor createChestplate() {
        return new ItemVoidRobeArmor(Type.CHESTPLATE);
    }
    public static ItemVoidRobeArmor createLeggings() {
        return new ItemVoidRobeArmor(Type.LEGGINGS);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return type == null ? "arcaneresearch:textures/entity/armor/void_robe_armor_overlay.png"
                : "arcaneresearch:textures/entity/armor/void_robe_armor.png";
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && stack.isDamaged() && entity.tickCount % 20 == 0
                && entity instanceof LivingEntity living) {
            stack.setDamageValue(stack.getDamageValue() - 1);
            for (ItemStack armorPiece : living.getArmorSlots()) {
                if (armorPiece == stack && stack.isDamaged()) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    break;
                }
            }
        }
    }
    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }
    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return this.getType() == Type.HELMET;
    }
    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return this.getType() == Type.HELMET;
    }
    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 3;
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
                if (voidRobeModel == null) {
                    voidRobeModel = new VoidRobeArmorModel(
                            Minecraft.getInstance().getEntityModels().bakeLayer(
                                    VoidRobeArmorModel.LAYER_LOCATION));
                }
                voidRobeModel.crouching = original.crouching;
                voidRobeModel.riding = original.riding;
                voidRobeModel.young = original.young;
                voidRobeModel.rightArmPose = original.rightArmPose;
                voidRobeModel.leftArmPose = original.leftArmPose;
                voidRobeModel.attackTime = original.attackTime;
                voidRobeModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        voidRobeModel.head.visible = true;
                        voidRobeModel.hat.visible = true;
                    }
                    case CHEST -> {
                        voidRobeModel.body.visible = true;
                        voidRobeModel.rightArm.visible = true;
                        voidRobeModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        voidRobeModel.body.visible = true;
                        voidRobeModel.rightLeg.visible = true;
                        voidRobeModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }
                return voidRobeModel;
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
