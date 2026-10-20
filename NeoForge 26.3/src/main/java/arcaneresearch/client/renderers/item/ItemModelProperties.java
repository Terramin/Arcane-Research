package arcaneresearch.client.renderers.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.common.blocks.essentia.BlockJarItem;
import arcaneresearch.common.items.casters.ItemCaster;
import arcaneresearch.common.items.wands.ItemWandCap;
import arcaneresearch.init.ModItems;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.x item models): vanilla ItemProperties + model "overrides"
// are gone. Per-variant models (jar fill, grapple/pearl/charm type, mirror
// link, wand cap) move to minecraft:select in assets/arcaneresearch/items/*.json
// backed by these SelectItemModelProperty<String> providers (values "0".."6",
// mirroring the old float predicates 1:1). All reads go through Data Components
// (CUSTOM_DATA via ArcaneresearchInvHelper, damage API) — never root NBT.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class ItemModelProperties {

    @SubscribeEvent
    public static void register(RegisterSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fill"), Fill.TYPE);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "type"), Variant.TYPE);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "linked"), Linked.TYPE);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cap"), Cap.TYPE);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus"), Focus.TYPE);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "staff"), Staff.TYPE);
    }

    // Jar fill level 0..4 (BlockJarItem.getFill).
    public record Fill() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Fill, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Fill()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            return String.valueOf((int) BlockJarItem.getFill(itemStack));
        }

        @Override
        public SelectItemModelProperty.Type<Fill, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }

    // Per-item variant: grapple loaded (CustomData "loaded"), primordial pearl
    // damage tiers, like the old per-item "arcaneresearch:type" predicates.
    // (Verdant charms are separate items in the port — no predicate needed.)
    public record Variant() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Variant, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Variant()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            if (itemStack.is(ModItems.GRAPPLE_GUN.get())) {
                net.minecraft.nbt.CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(itemStack);
                return (tag != null && tag.getByteOr("loaded", (byte) 0) == 1) ? "1" : "0";
            }
            if (itemStack.is(ModItems.PRIMORDIAL_PEARL.get())) {
                int damage = itemStack.getDamageValue();
                return damage < 3 ? "0" : (damage < 6 ? "1" : "2");
            }
            return "0";
        }

        @Override
        public SelectItemModelProperty.Type<Variant, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }

    // Mirror linked state (CustomData "linkX" presence).
    public record Linked() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Linked, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Linked()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            net.minecraft.nbt.CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(itemStack);
            return (tag != null && tag.contains("linkX")) ? "1" : "0";
        }

        @Override
        public SelectItemModelProperty.Type<Linked, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }

    // Wand cap material index (ItemWandCap.getCapIndex, 0 = iron default).
    public record Cap() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Cap, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Cap()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            return String.valueOf((int) ItemWandCap.getCapIndex(itemStack));
        }

        @Override
        public SelectItemModelProperty.Type<Cap, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }

    // Caster focus presence (Forge "arcaneresearch:focus" ItemProperty 1:1:
    // 1 when ItemCaster.getFocus(stack) != null, else 0). Switches the base
    // mesh between the plain gauntlet and the socket-nub variant.
    public record Focus() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Focus, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Focus()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            if (itemStack.getItem() instanceof ItemCaster itemCaster
                    && itemCaster.getFocus(itemStack) != null) {
                return "1";
            }
            return "0";
        }

        @Override
        public SelectItemModelProperty.Type<Focus, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }

    // Wand staff variant (Forge wand vs staff inventory models 1:1: 1 when the
    // rod isStaff(), else 0). Switches the special base (own display each) and
    // the OBJ mesh (staff.obj vs wand.obj); hand transforms match in both.
    public record Staff() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<Staff, String> TYPE =
                SelectItemModelProperty.Type.create(MapCodec.unit(new Staff()), Codec.STRING);

        @Override
        public String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                          int seed, ItemDisplayContext displayContext) {
            if (itemStack.getItem() instanceof arcaneresearch.api.wands.IWand wand) {
                try {
                    arcaneresearch.api.wands.WandRod rod = wand.getRod(itemStack);
                    if (rod != null && rod.isStaff()) {
                        return "1";
                    }
                } catch (Exception ignored) {
                }
            }
            return "0";
        }

        @Override
        public SelectItemModelProperty.Type<Staff, String> type() {
            return TYPE;
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }
    }
}
