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
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.essentia.BlockJarItem;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors jar entries 1:1.
// Forge bound one lambda to JAR_NORMAL + JAR_VOID: tintIndex 1 + BlockJarItem ->
// aspects[0] color, else white. 26.x item tints are positional (array index =
// tintIndex, verified NeoForge docs pattern, same as BannerTintSources): the jar
// item JSONs declare "tints": [constant white, arcaneresearch:jar_aspect] on every
// fill-variant model (liquid faces carry tintindex 1, same as Forge).
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class JarTintSources {

    public static final Identifier JAR_ASPECT_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "jar_aspect");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(JAR_ASPECT_ID, AspectTint.MAP_CODEC);
    }

    public record AspectTint() implements ItemTintSource {
        public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof BlockJarItem jarItem) {
                AspectList aspects = jarItem.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return 0xFF000000 | aspects.getAspects()[0].getColor();
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<AspectTint> type() {
            return MAP_CODEC;
        }
    }
}
