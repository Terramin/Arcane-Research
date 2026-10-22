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
import arcaneresearch.common.items.consumables.ItemPhial;
import org.jspecify.annotations.Nullable;








@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class PhialTintSources {

    public static final Identifier PHIAL_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "phial");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(PHIAL_ID, PhialTint.MAP_CODEC);
    }

    public record PhialTint() implements ItemTintSource {
        public static final MapCodec<PhialTint> MAP_CODEC = MapCodec.unit(new PhialTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof ItemPhial) {
                AspectList aspects =
                        ((ItemPhial) stack.getItem()).getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return 0xFF000000 | aspects.getAspects()[0].getColor();
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<PhialTint> type() {
            return MAP_CODEC;
        }
    }
}
