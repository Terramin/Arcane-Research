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
import arcaneresearch.common.blocks.basic.BlockCandle;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors candle entries 1:1.
// Forge registered one lambda per CANDLE_* block item returning
// candle.getColor().getMapColor().col (any tintIndex). 26.x item tints are
// data-driven ItemTintSource records wired per item via the "tints" array in
// assets/arcaneresearch/items/candle_*.json (index = tintIndex, same verified
// pattern as NitorTintSources). One source covers all 16 with identical values.
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class CandleTintSources {

    public static final Identifier CANDLE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "candle");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(CANDLE_ID, CandleTint.MAP_CODEC);
    }

    public record CandleTint() implements ItemTintSource {
        public static final MapCodec<CandleTint> MAP_CODEC = MapCodec.unit(new CandleTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof BlockCandle candle) {
                return 0xFF000000 | candle.getColor().getMapColor().col;
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<CandleTint> type() {
            return MAP_CODEC;
        }
    }
}
