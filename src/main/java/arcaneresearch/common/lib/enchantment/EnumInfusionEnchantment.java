package arcaneresearch.common.lib.enchantment;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private static final EnumInfusionEnchantment[] VALUES = values();

    public final Set<String> toolClasses;
    public final int maxLevel;
    public final String research;

    EnumInfusionEnchantment(Set<String> toolClasses, int maxLevel, String research) {
        this.toolClasses = toolClasses;
        this.maxLevel = maxLevel;
        this.research = research;
    }

    public static ListTag getInfusionEnchantmentTagList(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_KEY, Tag.TAG_LIST)) {
            return null;
        }
        return tag.getList(NBT_KEY, Tag.TAG_COMPOUND);
    }

    public static List<EnumInfusionEnchantment> getInfusionEnchantments(ItemStack stack) {
        ListTag nbttaglist = getInfusionEnchantmentTagList(stack);
        List<EnumInfusionEnchantment> list = new ArrayList<>();
        if (nbttaglist != null) {
            final int size = nbttaglist.size();
            for (int j = 0; j < size; ++j) {
                CompoundTag tag = nbttaglist.getCompound(j);
                if (tag == null) {
                    continue;
                }
                int k = tag.getShort("id");
                if (k >= 0 && k < VALUES.length) {
                    list.add(VALUES[k]);
                }
            }
        }
        return list;
    }

    public static int getInfusionEnchantmentLevel(ItemStack stack, EnumInfusionEnchantment enchantment) {
        if (enchantment == null) {
            return 0;
        }
        ListTag nbttaglist = getInfusionEnchantmentTagList(stack);
        if (nbttaglist != null) {
            final int size = nbttaglist.size();
            for (int j = 0; j < size; ++j) {
                CompoundTag tag = nbttaglist.getCompound(j);
                if (tag == null) {
                    continue;
                }
                int k = tag.getShort("id");
                int l = tag.getShort("lvl");
                if (k >= 0 && k < VALUES.length && VALUES[k] == enchantment) {
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
        if (stack == null || stack.isEmpty() || ie == null || level <= 0 || level > ie.maxLevel) {
            return;
        }
        CompoundTag stackTag = stack.getOrCreateTag();
        ListTag nbttaglist;
        if (stackTag.contains(NBT_KEY, Tag.TAG_LIST)) {
            nbttaglist = stackTag.getList(NBT_KEY, Tag.TAG_COMPOUND);
            final int size = nbttaglist.size();
            for (int j = 0; j < size; ++j) {
                CompoundTag tag = nbttaglist.getCompound(j);
                if (tag == null) {
                    continue;
                }
                int k = tag.getShort("id");
                int l = tag.getShort("lvl");
                if (k == ie.ordinal()) {
                    if (level <= l) {
                        return;
                    }
                    tag.putShort("lvl", (short) level);
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
        stackTag.put(NBT_KEY, nbttaglist);
    }

    public static void removeInfusionEnchantment(ItemStack stack, EnumInfusionEnchantment ie) {
        if (stack == null || stack.isEmpty() || ie == null) {
            return;
        }
        CompoundTag stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains(NBT_KEY, Tag.TAG_LIST)) {
            return;
        }
        ListTag nbttaglist = stackTag.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int j = nbttaglist.size() - 1; j >= 0; --j) {
            CompoundTag tag = nbttaglist.getCompound(j);
            if (tag == null) {
                continue;
            }
            int k = tag.getShort("id");
            if (k == ie.ordinal()) {
                nbttaglist.remove(j);
                break;
            }
        }
        if (nbttaglist.isEmpty()) {
            stackTag.remove(NBT_KEY);
        }
    }

    public String getDisplayName() {
        String name = this.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}