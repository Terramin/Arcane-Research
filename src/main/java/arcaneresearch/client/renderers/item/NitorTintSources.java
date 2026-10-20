package arcaneresearch.client.renderers.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.blocks.misc.BlockNitor;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors nitor entries 1:1.
// Forge registered one lambda per NITOR_* block item returning
// BlockNitor.getColorValue() (any tintIndex). 26.x item tints are data-driven
// ItemTintSource records wired per item via the "tints" array in
// assets/arcaneresearch/items/nitor_*.json (index = tintIndex, same verified
// pattern as CrystalTintSources). One source covers all 16 with identical values.
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class NitorTintSources {

    public static final Identifier NITOR_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "nitor");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(NITOR_ID, NitorTint.MAP_CODEC);
    }

    public record NitorTint() implements ItemTintSource {
        public static final MapCodec<NitorTint> MAP_CODEC = MapCodec.unit(new NitorTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof BlockNitor nitor) {
                return 0xFF000000 | nitor.getColorValue();
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<NitorTint> type() {
            return MAP_CODEC;
        }
    }
}
