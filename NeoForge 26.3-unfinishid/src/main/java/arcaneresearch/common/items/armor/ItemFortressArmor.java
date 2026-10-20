package arcaneresearch.common.items.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). ArmorItem.Type -> ArmorType; the worn piece is tracked in
// the armorType field (Item no longer knows its own slot).
// NEOFORGE_PORT: net.minecraftforge.* -> net.neoforged.* (@Mod.EventBusSubscriber was removed;
// use net.neoforged.fml.common.EventBusSubscriber).
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ItemFortressArmor extends Item implements IGoggles, IRevealer, IRunicArmor {

    /**
     * Паритет TC4: вся таум-броня — IRunicArmor с нулевым базовым зарядом
     * (пул собирается из TC.RUNIC от рунического аугмента).
     */
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    // NEOFORGE_PORT: replaces the removed ArmorItem.getType() for per-piece logic.
    private final ArmorType armorType;

    public ItemFortressArmor(Item.Properties props, ArmorType type) {
        // NEOFORGE_PORT: super(material, type, props) -> super(props.humanoidArmor(material, type)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_FORTRESS, type)
                // NEOFORGE_PORT (26.3): humanoidArmor() uses vanilla per-slot units (11/16/15/13);
                // Forge indexed {13,15,16,11} by ArmorItem.Type ordinal -> restore Forge durability.
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_FORTRESS, type))
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.armorType = type;
    }

    public static ItemFortressArmor createHelmet(Item.Properties props) {
        return new ItemFortressArmor(props, ArmorType.HELMET);
    }

    public static ItemFortressArmor createChestplate(Item.Properties props) {
        return new ItemFortressArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemFortressArmor createLeggings(Item.Properties props) {
        return new ItemFortressArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    // NEOFORGE_PORT: NBT root tag -> CUSTOM_DATA via ArcaneresearchInvHelper. getCustomTag
    // returns a COPY, so every mutation is written back with setCustomTag.
    public static boolean hasGoggles(ItemStack stack) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        return tag != null && tag.contains("goggles");
    }

    public static void attachGoggles(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.putBoolean("goggles", true);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    public static void removeGoggles(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("goggles")) {
            root.remove("goggles");
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    public static boolean hasMask(ItemStack stack) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        return tag != null && tag.contains("mask");
    }

    public static int getMaskType(ItemStack stack) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        return tag != null ? tag.getIntOr("mask", 0) : 0;
    }

    public static void setMask(ItemStack stack, int maskType) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.putInt("mask", maskType);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static int countFortressPieces(Player player) {
        int count = 0;
        // NEOFORGE_PORT: LivingEntity.getArmorSlots() REMOVED in 26.x (verified via javap) ->
        // iterate HEAD/CHEST/LEGS/FEET with getItemBySlot (same convention as ported PlayerEvents).
        for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(armorSlot);
            if (!armor.isEmpty() && armor.getItem() instanceof ItemFortressArmor) {
                count++;
            }
        }
        return count;
    }

    public static int getSetBonus(Player player) {
        int q = 0;
        int ar = 0;
        for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
            ItemStack piece = player.getItemBySlot(armorSlot);
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
        for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
            ItemStack piece = player.getItemBySlot(armorSlot);
            if (!piece.isEmpty() && piece.getItem() instanceof ItemFortressArmor) {
                return 1;
            }
        }
        return 0;
    }

    private static double getPieceRatio(DamageSource source, int defense) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            return defense / 35.0D;
        } else if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return defense / 20.0D;
        } else if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return 0.0D;
        }
        return defense / 25.0D;
    }

    // NEOFORGE_PORT: LivingHurtEvent REMOVED in NeoForge (verified via javap on the NeoForge jar).
    // NEOFORGE_PORT (26.3): Forge rescaled the PRE-armor amount (LivingHurtEvent, top of
    // actuallyHurt) and vanilla armor then absorbed the rescaled amount. LivingDamageEvent.Pre runs
    // after armor/enchant/resistance, so rescaling there applied the factor to an already
    // armor-reduced amount (armor absorption is not linear in damage). Same approach as
    // EntityEvents.onEntityHurt: an ARMOR reduction callback, which runs inside actuallyHurt with
    // container.getNewDamage() == Forge's LivingHurtEvent amount. LOWEST priority appends it after
    // the EntityEvents (other Forge LivingHurt handlers) callback; their pre-armor result is
    // recovered from the chained reduction, then this handler's factor is applied on top.
    // NEOFORGE_PORT: CombatRules.getDamageAfterAbsorb(float, float, float) signature changed in 26.x ->
    // getDamageAfterAbsorb(LivingEntity, float, DamageSource, float, float) (verified via javap).
    // NEOFORGE_PORT: ArmorItem.getDefense()/getToughness()/getType() are gone with ArmorItem ->
    // per-piece defense comes from the ARMORMAT_FORTRESS defense map keyed by armorType.
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide()) {
            return;
        }
        event.addReductionModifier(DamageContainer.Reduction.ARMOR,
                (container, reductionIn) -> applyFortress(living, container, reductionIn));
    }

    private static float armorAbsorb(LivingEntity living, DamageSource source, float amount) {
        if (amount <= 0.0F || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return amount;
        }
        return CombatRules.getDamageAfterAbsorb(living, amount, source, living.getArmorValue(),
                (float) living.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
    }

    /** Pre-armor amount whose vanilla armor result is {@code postArmor} (absorb is strictly increasing). */
    private static float preArmorAmount(LivingEntity living, DamageSource source, float before, float postArmor) {
        if (postArmor <= 0.0F || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return Math.max(0.0F, postArmor);
        }
        if (Math.abs(armorAbsorb(living, source, before) - postArmor) <= 1.0E-5F * Math.max(1.0F, before)) {
            return before;
        }
        float lo = 0.0F;
        float hi = Math.max(before, postArmor) * 2.0F + 1.0F;
        for (int i = 0; i < 64 && armorAbsorb(living, source, hi) < postArmor; i++) {
            hi *= 2.0F;
        }
        for (int i = 0; i < 64; i++) {
            float mid = (lo + hi) * 0.5F;
            if (armorAbsorb(living, source, mid) < postArmor) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return hi;
    }

    private static float applyFortress(LivingEntity living, DamageContainer container, float reductionIn) {
        List<ItemStack> pieces = new ArrayList<>();
        for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
            ItemStack armor = living.getItemBySlot(armorSlot);
            if (!armor.isEmpty() && armor.getItem() instanceof ItemFortressArmor) {
                pieces.add(armor);
            }
        }
        if (pieces.isEmpty()) {
            return reductionIn;
        }
        DamageSource source = container.getSource();

        if (source.is(DamageTypeTags.IS_FALL)) {
            return reductionIn;
        }
        float before = container.getNewDamage();
        // Forge LivingHurtEvent amount (after any earlier LivingHurt handler).
        float amount = preArmorAmount(living, source, before, before - reductionIn);

        float armorValue = (float) living.getArmorValue();
        float toughnessValue = (float) living.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float vanillaFactor = CombatRules.getDamageAfterAbsorb(living, 1.0F, source, armorValue, toughnessValue);
        if (vanillaFactor <= 1.0E-6F) {
            return reductionIn;
        }

        float fortArmor = 0.0F;
        float fortToughness = 0.0F;
        double remaining = 1.0D;
        for (ItemStack piece : pieces) {
            ItemFortressArmor armor = (ItemFortressArmor) piece.getItem();
            int defense = ArcaneresearchArmorMaterials.ARMORMAT_FORTRESS.defense()
                    .getOrDefault(armor.getArmorType(), 0);
            fortArmor += defense;
            fortToughness += ArcaneresearchArmorMaterials.ARMORMAT_FORTRESS.toughness();
            remaining *= (1.0D - getPieceRatio(source, defense));
        }
        double otherFactor = CombatRules.getDamageAfterAbsorb(living, 1.0F, source,
                Math.max(0.0F, armorValue - fortArmor),
                Math.max(0.0F, toughnessValue - fortToughness));

        double bonusAbsorb = 0.0D;
        if (living instanceof Player player) {
            bonusAbsorb = Math.min(0.20D, (getSetBonus(player) + getSetToughness(player)) * 0.04D);
        }
        double specialFactor = otherFactor * remaining * (1.0D - bonusAbsorb);
        float after = Math.max(0.0F, (float) (amount * specialFactor / vanillaFactor));
        // Vanilla armor (and the later enchant/resistance/absorption steps) then apply to `after`.
        return before - armorAbsorb(living, source, after);
    }

    // NEOFORGE_PORT: Forge isValidRepairItem accepted the thaumium ingot item (or the material match).
    // Item.isValidRepairItem was removed in 26.x (verified via javap - repair is now data-driven
    // via the ArmorMaterial repairIngredient tag); ARMORMAT_FORTRESS maps to the repairs_thaumium
    // data tag (thaumium ingot, as Forge used), so no override is needed (or possible).

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    @Override
    // NEOFORGE_PORT: appendHoverText 4-arg (ItemStack, Level, List, TooltipFlag) REMOVED in 26.x ->
    // 5-arg (ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag).
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        if (hasGoggles(stack)) {
            tooltip.accept(Component.translatable("item.arcaneresearch.goggles")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }

        if (hasMask(stack)) {
            int maskType = getMaskType(stack);
            tooltip.accept(Component.translatable("item.fortress_helm.mask." + maskType)
                    .withStyle(ChatFormatting.GOLD));
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {

        return hasGoggles(itemstack);
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {

        return hasGoggles(itemstack);
    }

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.
}
