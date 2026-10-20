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
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.items.consumables.ItemLabel;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors label entry 1:1.
// Forge bound LABEL_FILLED: tintIndex 1 + ItemLabel -> getAspect(stack) color,
// else white. 26.x item tints are positional (array index = tintIndex, verified
// NeoForge docs pattern, same as JarTintSources): label_filled.json declares
// "tints": [constant white (base layer0), arcaneresearch:label (overlay layer1)].
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class LabelTintSources {

    public static final Identifier LABEL_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "label");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(LABEL_ID, LabelTint.MAP_CODEC);
    }

    public record LabelTint() implements ItemTintSource {
        public static final MapCodec<LabelTint> MAP_CODEC = MapCodec.unit(new LabelTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof ItemLabel label) {
                Aspect aspect = label.getAspect(stack);
                if (aspect != null) {
                    return 0xFF000000 | aspect.getColor();
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<LabelTint> type() {
            return MAP_CODEC;
        }
    }
}
