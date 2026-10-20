package arcaneresearch.common.lib.enchantment;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum EnumInfusionEnchantment {

    COLLECTOR(ImmutableSet.of("axe", "pickaxe", "shovel", "weapon"), 1, "INFUSIONENCHANTMENT"),
    DESTRUCTIVE(ImmutableSet.of("axe", "pickaxe", "shovel"), 1, "INFUSIONENCHANTMENT"),
    BURROWING(ImmutableSet.of("axe", "pickaxe"), 1, "INFUSIONENCHANTMENT"),
    SOUNDING(ImmutableSet.of("pickaxe"), 4, "INFUSIONENCHANTMENT"),
    REFINING(ImmutableSet.of("pickaxe"), 4, "INFUSIONENCHANTMENT"),
    ARCING(ImmutableSet.of("weapon"), 4, "INFUSIONENCHANTMENT"),
    ESSENCE(ImmutableSet.of("weapon"), 5, "INFUSIONENCHANTMENT"),

    VISBATTERY(ImmutableSet.of("chargable"), 3, "?"),
    VISCHARGE(ImmutableSet.of("chargable"), 1, "?"),

    SWIFT(ImmutableSet.of("boots"), 4, "IEARMOR"),
    AGILE(ImmutableSet.of("legs"), 1, "IEARMOR"),
    INFESTED(ImmutableSet.of("chest"), 1, "IETAINT"),

    LAMPLIGHT(ImmutableSet.of("axe", "pickaxe", "shovel"), 1, "INFUSIONENCHANTMENT"),

    MAGIC_EFFICIENCY(ImmutableSet.of("wand"), 3, "?");

    public static final String NBT_KEY = "infench";

    public final Set<String> toolClasses;
    public final int maxLevel;
    public final String research;

    EnumInfusionEnchantment(Set<String> toolClasses, int maxLevel, String research) {
        this.toolClasses = toolClasses;
        this.maxLevel = maxLevel;
        this.research = research;
    }

    public static ListTag getInfusionEnchantmentTagList(ItemStack stack) {
        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same NBT_KEY list shape).
        if (stack == null || stack.isEmpty()
                || !arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(stack)) {
            return null;
        }
        CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag == null || !tag.contains(NBT_KEY)) {
            return null;
        }
        return tag.getListOrEmpty(NBT_KEY);
    }

    public static List<EnumInfusionEnchantment> getInfusionEnchantments(ItemStack stack) {
        ListTag nbttaglist = getInfusionEnchantmentTagList(stack);
        List<EnumInfusionEnchantment> list = new ArrayList<>();

        if (nbttaglist != null) {
            for (int j = 0; j < nbttaglist.size(); ++j) {
                CompoundTag tag = nbttaglist.getCompoundOrEmpty(j);
                int k = tag.getShortOr("id", (short) 0);
                if (k >= 0 && k < values().length) {
                    list.add(values()[k]);
                }
            }
        }
        return list;
    }

    public static int getInfusionEnchantmentLevel(ItemStack stack, EnumInfusionEnchantment enchantment) {
        ListTag nbttaglist = getInfusionEnchantmentTagList(stack);

        if (nbttaglist != null) {
            for (int j = 0; j < nbttaglist.size(); ++j) {
                CompoundTag tag = nbttaglist.getCompoundOrEmpty(j);
                int k = tag.getShortOr("id", (short) 0);
                int l = tag.getShortOr("lvl", (short) 0);
                if (k >= 0 && k < values().length && values()[k] == enchantment) {
                    return l;
                }
            }
        }
        return 0;
    }

    public static boolean hasInfusionEnchantment(ItemStack stack, EnumInfusionEnchantment enchantment) {
        return getInfusionEnchantmentLevel(stack, enchantment) > 0;
    }

    public static void addInfusionEnchantment(ItemStack stack, EnumInfusionEnchantment ie, int level) {
        if (stack == null || stack.isEmpty() || level > ie.maxLevel) {
            return;
        }

        // NEOFORGE_PORT: getOrCreateCustomTag returns a copy; written back at every exit.
        CompoundTag stackTag = arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        ListTag nbttaglist;

        if (stackTag.contains(NBT_KEY)) {
            nbttaglist = stackTag.getListOrEmpty(NBT_KEY);

            for (int j = 0; j < nbttaglist.size(); ++j) {
                CompoundTag tag = nbttaglist.getCompoundOrEmpty(j);
                int k = tag.getShortOr("id", (short) 0);
                int l = tag.getShortOr("lvl", (short) 0);

                if (k == ie.ordinal()) {

                    if (level <= l) {
                        return;
                    }
                    tag.putShort("lvl", (short) level);
                    arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(stack, stackTag);
                    return;
                }
            }
        } else {
            nbttaglist = new ListTag();
        }

        CompoundTag nbttagcompound = new CompoundTag();
        nbttagcompound.putShort("id", (short) ie.ordinal());
        nbttagcompound.putShort("lvl", (short) level);
        nbttaglist.add(nbttagcompound);

        if (nbttaglist.size() > 0) {
            stackTag.put(NBT_KEY, nbttaglist);
            arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(stack, stackTag);
        }
    }

    public static void removeInfusionEnchantment(ItemStack stack, EnumInfusionEnchantment ie) {
        if (stack == null || stack.isEmpty()
                || !arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(stack)) {
            return;
        }

        CompoundTag stackTag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (stackTag == null || !stackTag.contains(NBT_KEY)) {
            return;
        }

        ListTag nbttaglist = stackTag.getListOrEmpty(NBT_KEY);
        for (int j = nbttaglist.size() - 1; j >= 0; --j) {
            CompoundTag tag = nbttaglist.getCompoundOrEmpty(j);
            int k = tag.getShortOr("id", (short) 0);
            if (k == ie.ordinal()) {
                nbttaglist.remove(j);
                break;
            }
        }

        if (nbttaglist.isEmpty()) {
            stackTag.remove(NBT_KEY);
        }
        // NEOFORGE_PORT: stackTag is a copy; write it back (remove path included).
        arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(stack, stackTag);
    }

    public String getDisplayName() {
        String name = this.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
