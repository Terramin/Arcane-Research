package arcaneresearch.client.renderers.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModBlocks;
import org.jspecify.annotations.Nullable;









@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class FoliageTintSources {

    public static final Identifier LEAF_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "leaf");
    public static final Identifier GRASS_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "grass_ambient");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(LEAF_ID, LeafTint.MAP_CODEC);
        event.register(GRASS_ID, GrassTint.MAP_CODEC);
    }

    public record LeafTint() implements ItemTintSource {
        public static final MapCodec<LeafTint> MAP_CODEC = MapCodec.unit(new LeafTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() == ModBlocks.SILVERWOOD_LEAVES.get()) {
                return 0xFFFFFFFF;
            }
            return 0xFF000000 | FoliageColor.FOLIAGE_DEFAULT;
        }

        @Override
        public MapCodec<LeafTint> type() {
            return MAP_CODEC;
        }
    }

    public record GrassTint() implements ItemTintSource {
        public static final MapCodec<GrassTint> MAP_CODEC = MapCodec.unit(new GrassTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return 0xFF000000 | GrassColor.get(0.5D, 1.0D);
        }

        @Override
        public MapCodec<GrassTint> type() {
            return MAP_CODEC;
        }
    }
}
