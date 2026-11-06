package arcaneresearch.common.items.armor;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.client.models.armor.FortressArmorModel;
import arcaneresearch.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemFortressArmor extends ArmorItem implements IGoggles, IRevealer, IRunicArmor {

    private static final String ARMOR_TEXTURE = "arcaneresearch:textures/entity/armor/fortress_armor.png";
    private static final String TAG_GOGGLES = "goggles";
    private static final String TAG_MASK = "mask";
    private static final int MAX_MASK_TYPE = 3;

    @OnlyIn(Dist.CLIENT)
    private static HumanoidModel<LivingEntity> fortressModel;

    public ItemFortressArmor(Type type) {
        super(ArcaneresearchMaterials.ARMORMAT_FORTRESS, type,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.RARE));
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public static ItemFortressArmor createHelmet() {
        return new ItemFortressArmor(Type.HELMET);
    }

    public static ItemFortressArmor createChestplate() {
        return new ItemFortressArmor(Type.CHESTPLATE);
    }

    public static ItemFortressArmor createLeggings() {
        return new ItemFortressArmor(Type.LEGGINGS);
    }

    public static boolean hasGoggles(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        var tag = stack.getTag();
        return tag != null && tag.contains(TAG_GOGGLES);
    }

    public static void attachGoggles(ItemStack stack) {
        if (stack != null) {
            stack.getOrCreateTag().putBoolean(TAG_GOGGLES, true);
        }
    }

    public static void removeGoggles(ItemStack stack) {
        if (stack == null) {
            return;
        }
        var tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_GOGGLES);
        }
    }

    public static boolean hasMask(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        var tag = stack.getTag();
        return tag != null && tag.contains(TAG_MASK);
    }

    public static int getMaskType(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        var tag = stack.getTag();
        return tag != null ? tag.getInt(TAG_MASK) : 0;
    }

    public static void setMask(ItemStack stack, int maskType) {
        if (stack != null) {
            stack.getOrCreateTag().putInt(TAG_MASK, maskType);
        }
    }

    public static int countFortressPieces(Player player) {
        if (player == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (!armor.isEmpty() && armor.getItem() instanceof ItemFortressArmor) {
                count++;
            }
        }
        return count;
    }

    public static int getSetBonus(Player player) {
        if (player == null) {
            return 0;
        }
        int q = 0;
        int ar = 0;
        for (ItemStack piece : player.getArmorSlots()) {
            if (!piece.isEmpty() && piece.getItem() instanceof ItemFortressArmor) {
                if (hasMask(piece)) {
                    ar++;
                }
                q++;
                if (q <= 1) {
                    ar++;
                }
            }
        }
        return ar;
    }

    public static int getSetToughness(Player player) {
        if (player == null) {
            return 0;
        }
        for (ItemStack piece : player.getArmorSlots()) {
            if (!piece.isEmpty() && piece.getItem() instanceof ItemFortressArmor) {
                return 1;
            }
        }
        return 0;
    }

    private static double getPieceRatio(DamageSource source, int defense) {
        if (source == null) {
            return defense / 25.0D;
        }
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return defense / 35.0D;
        }
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return defense / 20.0D;
        }
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return 0.0D;
        }
        return defense / 25.0D;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity living = event.getEntity();
        if (living == null || living.level().isClientSide) {
            return;
        }

        int count = 0;
        for (ItemStack armor : living.getArmorSlots()) {
            if (!armor.isEmpty() && armor.getItem() instanceof ItemFortressArmor) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }

        DamageSource source = event.getSource();
        if (source == null || source.is(DamageTypeTags.IS_FALL)) {
            return;
        }

        float amount = event.getAmount();
        float armorValue = (float) living.getArmorValue();
        float toughnessValue = (float) living.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float vanillaFactor = CombatRules.getDamageAfterAbsorb(1.0F, armorValue, toughnessValue);
        if (vanillaFactor <= 1.0E-6F) {
            return;
        }

        float fortArmor = 0.0F;
        float fortToughness = 0.0F;
        double remaining = 1.0D;

        for (ItemStack piece : living.getArmorSlots()) {
            if (piece.isEmpty() || !(piece.getItem() instanceof ItemFortressArmor)) {
                continue;
            }
            ArmorItem armor = (ArmorItem) piece.getItem();
            fortArmor += armor.getDefense();
            fortToughness += armor.getToughness();
            int defenseForType = ArcaneresearchMaterials.ARMORMAT_FORTRESS.getDefenseForType(armor.getType());
            remaining *= (1.0D - getPieceRatio(source, defenseForType));
        }

        double otherFactor = CombatRules.getDamageAfterAbsorb(
                1.0F,
                Math.max(0.0F, armorValue - fortArmor),
                Math.max(0.0F, toughnessValue - fortToughness)
        );

        double bonusAbsorb = 0.0D;
        if (living instanceof Player player) {
            bonusAbsorb = Math.min(0.20D, (getSetBonus(player) + getSetToughness(player)) * 0.04D);
        }

        double specialFactor = otherFactor * remaining * (1.0D - bonusAbsorb);
        event.setAmount((float) (amount * specialFactor / vanillaFactor));
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        Item thaumium = ModItems.THAUMIUM_INGOT.get();
        if (repair.is(thaumium)) {
            return true;
        }
        return super.isValidRepairItem(toRepair, repair);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ARMOR_TEXTURE;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (hasGoggles(stack)) {
            tooltip.add(Component.translatable("item.arcaneresearch.goggles")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (hasMask(stack)) {
            int maskType = getMaskType(stack);
            tooltip.add(Component.translatable("item.fortress_helm.mask." + maskType)
                    .withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return hasGoggles(itemstack);
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return hasGoggles(itemstack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            @Nonnull
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                Minecraft mc = Minecraft.getInstance();
                if (fortressModel == null) {
                    fortressModel = new FortressArmorModel(
                            mc.getEntityModels().bakeLayer(FortressArmorModel.LAYER_LOCATION));
                }

                if (original != null) {
                    fortressModel.crouching = original.crouching;
                    fortressModel.riding = original.riding;
                    fortressModel.young = original.young;
                    fortressModel.rightArmPose = original.rightArmPose;
                    fortressModel.leftArmPose = original.leftArmPose;
                    fortressModel.attackTime = original.attackTime;
                }

                fortressModel.setAllVisible(false);
                switch (slot) {
                    case HEAD -> {
                        fortressModel.head.visible = true;
                        fortressModel.hat.visible = true;
                    }
                    case CHEST -> {
                        fortressModel.body.visible = true;
                        fortressModel.rightArm.visible = true;
                        fortressModel.leftArm.visible = true;
                    }
                    case LEGS -> {
                        fortressModel.body.visible = true;
                        fortressModel.rightLeg.visible = true;
                        fortressModel.leftLeg.visible = true;
                    }
                    default -> {
                    }
                }

                int setCount = 0;
                boolean hasGoggles = false;
                int mask = -1;

                if (entity != null) {
                    for (ItemStack armor : entity.getArmorSlots()) {
                        if (!armor.isEmpty() && armor.getItem() instanceof ItemFortressArmor) {
                            setCount++;
                        }
                    }

                    ItemStack helmStack = entity.getItemBySlot(EquipmentSlot.HEAD);
                    if (!helmStack.isEmpty() && helmStack.getItem() instanceof ItemFortressArmor) {
                        hasGoggles = ItemFortressArmor.hasGoggles(helmStack);
                        if (ItemFortressArmor.hasMask(helmStack)) {
                            int maskType = ItemFortressArmor.getMaskType(helmStack);
                            if (maskType >= 0 && maskType < MAX_MASK_TYPE) {
                                mask = maskType;
                            }
                        }
                    }
                }

                ((FortressArmorModel) fortressModel).applyVariant(setCount, hasGoggles, mask);
                return fortressModel;
            }
        });
    }
}