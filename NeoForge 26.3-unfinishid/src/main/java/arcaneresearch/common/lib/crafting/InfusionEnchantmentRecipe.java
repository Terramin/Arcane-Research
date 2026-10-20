package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher; // NEOFORGE_PORT: package move
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.init.ModRecipeSerializers;

import java.util.List;
import java.util.Random;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/InfusionEnchantmentRecipe.java.
// 26.x changes only: super() follows the ported InfusionRecipeType ctor (no id, nullable
// central input — enchantments have no central requirement); MAP_CODEC/STREAM_CODEC added
// (same JSON keys as the old serializer: "enchantment", "components", "aspects").
// Matching/enchanting/warp logic VERBATIM.
public class InfusionEnchantmentRecipe extends InfusionRecipeType {

    public static final MapCodec<InfusionEnchantmentRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            // NEOFORGE_PORT (26.3): unknown names fail as a DataResult (Forge's fromJson threw,
            // and the recipe manager skipped that one recipe) instead of throwing from apply().
            Codec.STRING.validate(name -> {
                try {
                    EnumInfusionEnchantment.valueOf(name.toUpperCase());
                    return com.mojang.serialization.DataResult.success(name);
                } catch (IllegalArgumentException e) {
                    return com.mojang.serialization.DataResult.error(() -> "Unknown infusion enchantment '" + name + "'");
                }
            }).fieldOf("enchantment").forGetter(r -> r.enchantment.name()),
            Ingredient.CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(r -> r.getComponents()),
            AspectList.MAP_CODEC.codec().optionalFieldOf("aspects", new AspectList()).forGetter(r -> r.getAspects())
    ).apply(inst, (name, components, aspects) -> {
        NonNullList<Ingredient> comps = NonNullList.create();
        comps.addAll(components);
        return new InfusionEnchantmentRecipe(EnumInfusionEnchantment.valueOf(name.toUpperCase()), aspects, comps);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, r -> r.enchantment.name(),
                    AspectList.STREAM_CODEC, r -> r.getAspects(),
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.getComponents(),
                    (name, aspects, components) -> {
                        NonNullList<Ingredient> comps = NonNullList.create();
                        comps.addAll(components);
                        return new InfusionEnchantmentRecipe(
                                EnumInfusionEnchantment.valueOf(name.toUpperCase()), aspects, comps);
                    }
            );

    public static final RecipeSerializer<InfusionEnchantmentRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public final EnumInfusionEnchantment enchantment;

    public InfusionEnchantmentRecipe(EnumInfusionEnchantment ench, AspectList as, NonNullList<Ingredient> components) {
        super("", null, components, as, null, ench.research, 4);
        this.enchantment = ench;
    }

    @Override
    public boolean matchesInfusion(List<ItemStack> input, ItemStack central, Level level, Player player) {
        if (central == null || central.isEmpty() || !ArcaneresearchCapabilities.isResearchKnown(player, getResearch())) {
            return false;
        }

        if (EnumInfusionEnchantment.getInfusionEnchantmentLevel(central, enchantment) >= enchantment.maxLevel) {
            return false;
        }

        if (!enchantment.toolClasses.contains("all")) {
            // NEOFORGE_PORT: ToolAction system removed in 26.x; tool classes map to
            // vanilla item tags (axe/pickaxe/shovel/sword/hoe). The old attack-damage
            // "weapon" check reads the ATTRIBUTE_MODIFIERS component instead.
            net.minecraft.world.item.ItemStack centralStack = central;
            boolean cool = false;

            // NEOFORGE_PORT (26.3): Forge read getAttributeModifiers(MAINHAND) (defaults + event);
            // NeoForge's ItemStack#getAttributeModifiers() is the same computation, filtered to
            // entries that apply to the main hand.
            net.minecraft.world.item.component.ItemAttributeModifiers itemMods =
                    centralStack.getAttributeModifiers();
            if (itemMods != null && enchantment.toolClasses.contains("weapon")) {
                for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : itemMods.modifiers()) {
                    if (entry.slot().test(EquipmentSlot.MAINHAND)
                            && entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                        cool = true;
                        break;
                    }
                }
            }

            // NEOFORGE_PORT (26.3): Forge matched AxeItem/PickaxeItem/... subclasses (incl. this
            // mod's thaumium/void/elemental tools). Vanilla tools are in the vanilla tags, the
            // mod's tools in arcaneresearch:tools/* (same members as the Forge subclasses).
            if (!cool) {
                if (enchantment.toolClasses.contains("axe") && isToolClass(centralStack, net.minecraft.tags.ItemTags.AXES, "axes"))
                    cool = true;
                if (enchantment.toolClasses.contains("pickaxe") && isToolClass(centralStack, net.minecraft.tags.ItemTags.PICKAXES, "pickaxes"))
                    cool = true;
                if (enchantment.toolClasses.contains("shovel") && isToolClass(centralStack, net.minecraft.tags.ItemTags.SHOVELS, "shovels"))
                    cool = true;
                if (enchantment.toolClasses.contains("sword") && isToolClass(centralStack, net.minecraft.tags.ItemTags.SWORDS, "swords"))
                    cool = true;
                if (enchantment.toolClasses.contains("hoe") && isToolClass(centralStack, net.minecraft.tags.ItemTags.HOES, "hoes"))
                    cool = true;
            }
            // FIX (Forge parity): 1.20.1 matched DiggerItem via canPerformAction's whole
            // DEFAULT_DIGGER_ACTIONS set (axe+pickaxe+shovel+hoe at once), so the primal
            // crusher (Forge DiggerItem) was eligible for every digger-class infusion
            // enchantment. 26.x has no dig abilities (ItemAbilities dropped *_DIG) and no
            // DiggerItem class at all (data-driven Tool items), and the port's crusher
            // extends plain Item without a Tool component, so the tag checks above never
            // matched it. Restore that eligibility explicitly: the crusher counts for all
            // four digger classes, as the Forge DiggerItem did.
            if (!cool) {
                boolean diggerClass = enchantment.toolClasses.contains("axe")
                        || enchantment.toolClasses.contains("pickaxe")
                        || enchantment.toolClasses.contains("shovel")
                        || enchantment.toolClasses.contains("hoe");
                if (diggerClass && centralStack.getItem() instanceof arcaneresearch.common.items.tools.ItemPrimalCrusher) {
                    cool = true;
                }
            }

            // NEOFORGE_PORT: ArmorItem.getType removed; armor slot comes from the EQUIPPABLE
            // component (HEAD/CHEST/LEGS/FEET map to the old helm/chest/legs/boots classes).
            // Only real armor counts (Forge: instanceof ArmorItem) - humanoid armor always
            // carries an ARMOR modifier, elytra/pumpkins/heads (equippable, no ARMOR) do not.
            if (!cool) {
                var equippable = central.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                String at = "none";
                if (equippable != null && hasArmorModifier(itemMods)) {
                    switch (equippable.slot()) {
                        case HEAD -> at = "helm";
                        case CHEST -> at = "chest";
                        case LEGS -> at = "legs";
                        case FEET -> at = "boots";
                        default -> at = "none";
                    }
                }
                if (!at.equals("none")
                        && (enchantment.toolClasses.contains("armor") || enchantment.toolClasses.contains(at))) {
                    cool = true;
                }
            }

            if (!cool && (enchantment.toolClasses.contains("bauble")
                    || enchantment.toolClasses.contains("amulet")
                    || enchantment.toolClasses.contains("belt")
                    || enchantment.toolClasses.contains("ring"))) {

                if (CuriosCompat.isCuriosLoaded()) {

                    try {
                        Class<?> curioInterface = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
                        if (curioInterface.isAssignableFrom(central.getItem().getClass())) {
                            cool = true;
                        }
                    } catch (ClassNotFoundException ignored) {

                    }
                }
            }

            if (!cool && central.getItem() instanceof IRechargable && enchantment.toolClasses.contains("chargable")) {
                cool = true;
            }

            if (!cool && central.getItem() instanceof arcaneresearch.api.wands.IWand
                    && enchantment.toolClasses.contains("wand")) {
                cool = true;
            }

            if (!cool) {
                return false;
            }
        }

        return RecipeMatcher.findMatches(input, getComponents()) != null;
    }

    private static boolean isToolClass(ItemStack stack, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> vanillaTag,
                                       String modToolTag) {
        return stack.is(vanillaTag) || stack.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID,
                        "tools/" + modToolTag)));
    }

    private static boolean hasArmorModifier(net.minecraft.world.item.component.ItemAttributeModifiers mods) {
        if (mods == null) {
            return false;
        }
        for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : mods.modifiers()) {
            if (entry.attribute().is(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getRecipeOutput(Player player, ItemStack input, List<ItemStack> comps) {
        if (input == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = input.copy();
        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(out, enchantment);
        if (cl >= enchantment.maxLevel) {
            return ItemStack.EMPTY;
        }

        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        Random rand = new Random(System.nanoTime());
        if (rand.nextInt(10) < el.size()) {
            int base = 1;
            net.minecraft.nbt.CompoundTag warpTag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(input);
            if (warpTag != null) {
                base += warpTag.getByteOr("TC.WARP", (byte) 0);
            }
            final byte warpOut = (byte) base;
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, out, t -> t.put("TC.WARP", ByteTag.valueOf(warpOut)));
        }

        EnumInfusionEnchantment.addInfusionEnchantment(out, enchantment, cl + 1);
        return out;
    }

    @Override
    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> comps) {
        AspectList out = new AspectList();
        if (input == null || input.isEmpty()) {
            return out;
        }

        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(input, enchantment) + 1;
        if (cl > enchantment.maxLevel) {
            return out;
        }

        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        int otherEnchantments = el.size();
        if (el.contains(enchantment)) {
            --otherEnchantments;
        }

        float modifier = cl + otherEnchantments * 0.33f;
        for (Aspect a : super.getAspects().getAspects()) {
            out.add(a, (int) (super.getAspects().getAmount(a) * modifier));
        }

        return out;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<InfusionEnchantmentRecipe> getSerializer() {
        return ModRecipeSerializers.INFUSION_ENCHANTMENT.get();
    }
}
