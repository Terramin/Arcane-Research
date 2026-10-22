package thaumictinker.kami.trinkets;

import javax.annotation.Nullable;

import arcaneresearch.common.items.wands.ItemFocusPouch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.extensions.IForgeMenuType;

import thaumictinker.registry.TtRegistry;

 




 
public final class TrinketsFeature {

    public static final String BLOCK_TALISMAN_RESEARCH = "BLOCK_TALISMAN";
    public static final String PLACEMENT_MIRROR_RESEARCH = "PLACEMENT_MIRROR";
    public static final String PROTOCLAY_RESEARCH = "PROTOCLAY";
    public static final String ICHOR_POUCH_RESEARCH = "ICHOR_POUCH";
    public static final String CAT_AMULET_RESEARCH = "CAT_AMULET";

     






 
    public static ItemFocusPouch focusPouch() {
        return (ItemFocusPouch) arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_POUCH.get();
    }

    public static final BlackHoleRingItem BLOCK_TALISMAN = (BlackHoleRingItem) TtRegistry.itemWithTab(
            "block_talisman", () -> new BlackHoleRingItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final WorldshaperGlassItem PLACEMENT_MIRROR = (WorldshaperGlassItem) TtRegistry.itemWithTab(
            "placement_mirror", () -> new WorldshaperGlassItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final ProtoclayItem PROTOCLAY = (ProtoclayItem) TtRegistry.itemWithTab(
            "protoclay", () -> new ProtoclayItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final IchorPouchItem ICHOR_POUCH = (IchorPouchItem) TtRegistry.itemWithTab(
            "ichor_pouch", IchorPouchItem::new);

    public static final CatAmuletItem CAT_AMULET = (CatAmuletItem) TtRegistry.itemWithTab(
            "cat_amulet", () -> new CatAmuletItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final MenuType<IchorPouchMenu> ICHOR_POUCH_MENU =
            TtRegistry.menu("ichor_pouch",
                    IForgeMenuType.create((containerId, inventory, buf) -> new IchorPouchMenu(containerId, inventory)));

    private TrinketsFeature() {
    }

    public static void load() {
    }

    

    @Nullable
    public static ResourceLocation mirrorBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("MirrorBlock", Tag.TAG_STRING)) {
            return null;
        }
        return new ResourceLocation(tag.getString("MirrorBlock"));
    }

    public static void setMirrorBlock(ItemStack stack, ResourceLocation id) {
        stack.getOrCreateTag().putString("MirrorBlock", id.toString());
    }

    public static int mirrorSize(ItemStack stack, int fallback) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("MirrorSize", Tag.TAG_INT) ? tag.getInt("MirrorSize") : fallback;
    }

    public static void setMirrorSize(ItemStack stack, int size) {
        stack.getOrCreateTag().putInt("MirrorSize", size);
    }
}
