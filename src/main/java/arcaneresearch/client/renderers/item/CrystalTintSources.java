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
