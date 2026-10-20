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
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import org.jspecify.annotations.Nullable;

// NEOFORGE_PORT (26.3): Forge Arcaneresearch.onRegisterItemColors crystal entries 1:1.
// Forge bound two lambdas (RegisterColorHandlersEvent.Item, now removed):
//   crystal_essence -> aspects[0] color (any tintIndex), else white;
//   7 crystal block items -> fixed Aspect color each (any tintIndex).
// 26.x item tints are data-driven ItemTintSource records wired per item via the
// "tints" array in assets/arcaneresearch/items/crystal_*.json (index = tintIndex,
// same verified pattern as WandTintSources). One source covers all 8 items with
// identical values (essence reads the stack aspect, blocks read their fixed aspect).
// Opaque OR (0xFF000000 |): Forge ItemColors ignored alpha (RGB only); 26.3 tint
// layers are ARGB, so alpha is forced for the same visible result (same transport
// adaptation as the crystal BlockTintSource in Arcaneresearch).
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class CrystalTintSources {

    public static final Identifier CRYSTAL_ASPECT_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crystal_aspect");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(CRYSTAL_ASPECT_ID, AspectTint.MAP_CODEC);
    }

    public record AspectTint() implements ItemTintSource {
        public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof ItemCrystalEssence crystal) {
                AspectList aspects = crystal.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return 0xFF000000 | aspects.getAspects()[0].getColor();
                }
                return 0xFFFFFFFF;
            }
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof BlockCrystalTC crystal) {
                return 0xFF000000 | crystal.getAspect().getColor();
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<AspectTint> type() {
            return MAP_CODEC;
        }
    }
}
