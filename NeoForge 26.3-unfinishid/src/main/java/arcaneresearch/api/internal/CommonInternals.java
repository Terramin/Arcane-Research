package arcaneresearch.api.internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApi.EntityTags;
import arcaneresearch.api.ArcaneresearchApi.SmeltBonus;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommonInternals {

    public static Map<String, Identifier> jsonLocs = new HashMap<>();

    public static List<EntityTags> scanEntities = new ArrayList<>();

    public static Map<Identifier, IArcaneresearchRecipe> craftingRecipeCatalog = new ConcurrentHashMap<>();

    public static Map<Identifier, Object> craftingRecipeCatalogFake = new HashMap<>();

    public static List<SmeltBonus> smeltingBonus = new ArrayList<>();

    public static ConcurrentHashMap<String, AspectList> objectTags = new ConcurrentHashMap<>();

    public static Map<Object, Integer> warpMap = new HashMap<>();

    public static Map<String, ItemStack> seedList = new HashMap<>();

    public static IArcaneresearchRecipe getCatalogRecipe(Identifier key) {
        return craftingRecipeCatalog.get(key);
    }

    public static Object getCatalogRecipeFake(Identifier key) {
        return craftingRecipeCatalogFake.get(key);
    }

    // NEOFORGE_PORT: ItemStack.save(CompoundTag)/setTag removed in 26.x; ids derive from
    // the CODEC encoding instead (same purpose: stable per-stack identity hash).
    public static int generateUniqueItemstackId(ItemStack stack) {
        ItemStack sc = stack.copy();
        sc.setCount(1);
        return ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, sc)
                .result().map(Object::toString).orElse("").hashCode();
    }

    public static int generateUniqueItemstackIdStripped(ItemStack stack) {
        ItemStack sc = stack.copy();
        sc.setCount(1);
        sc.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, sc)
                .result().map(Object::toString).orElse("").hashCode();
    }
}
