package arcaneresearch.api.internal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
    public static Map<String, ResourceLocation> jsonLocs = new HashMap<>();
    public static List<EntityTags> scanEntities = new ArrayList<>();
    public static Map<ResourceLocation, IArcaneresearchRecipe> craftingRecipeCatalog = new ConcurrentHashMap<>();
    public static Map<ResourceLocation, Object> craftingRecipeCatalogFake = new HashMap<>();
    public static List<SmeltBonus> smeltingBonus = new ArrayList<>();
    public static ConcurrentHashMap<String, AspectList> objectTags = new ConcurrentHashMap<>();
    public static Map<Object, Integer> warpMap = new HashMap<>();
    public static Map<String, ItemStack> seedList = new HashMap<>();
    public static IArcaneresearchRecipe getCatalogRecipe(ResourceLocation key) {
        return craftingRecipeCatalog.get(key);
    }
    public static Object getCatalogRecipeFake(ResourceLocation key) {
        return craftingRecipeCatalogFake.get(key);
    }
    public static int generateUniqueItemstackId(ItemStack stack) {
        ItemStack sc = stack.copy();
        sc.setCount(1);
        CompoundTag nbt = new CompoundTag();
        sc.save(nbt);
        return nbt.toString().hashCode();
    }
    public static int generateUniqueItemstackIdStripped(ItemStack stack) {
        ItemStack sc = stack.copy();
        sc.setCount(1);
        sc.setTag(null);
        CompoundTag nbt = new CompoundTag();
        sc.save(nbt);
        return nbt.toString().hashCode();
    }
}
