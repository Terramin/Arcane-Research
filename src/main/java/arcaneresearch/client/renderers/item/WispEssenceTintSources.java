package arcaneresearch.client.renderers.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.helper.TintHelper;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors wisp-essence entry 1:1.
// Forge bound one lambda (RegisterColorHandlersEvent.Item, now removed) to
// WISP_ESSENCE: 0xFF000000 | TintHelper.wispEssenceAspect(stack) for any tintIndex.
// 26.x item tints are data-driven ItemTintSource records wired per item via the
// "tints" array in assets/arcaneresearch/items/wisp_essence.json (index = tintIndex,
// same pattern as Crystal/Jar/Nitor/Banner tint sources).
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class WispEssenceTintSources {

    public static final Identifier ESSENCE_ASPECT_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "essence_aspect");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ESSENCE_ASPECT_ID, AspectTint.MAP_CODEC);
    }

    public record AspectTint() implements ItemTintSource {
        public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return 0xFF000000 | (TintHelper.wispEssenceAspect(stack) & 0xFFFFFF);
        }

        @Override
        public MapCodec<AspectTint> type() {
            return MAP_CODEC;
        }
    }
}
