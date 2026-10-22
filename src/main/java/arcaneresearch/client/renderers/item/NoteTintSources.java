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
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.helper.TintHelper;
import org.jspecify.annotations.Nullable;





@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class NoteTintSources {

    public static final Identifier NOTE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "note");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(NOTE_ID, NoteTint.MAP_CODEC);
    }

    public record NoteTint() implements ItemTintSource {
        public static final MapCodec<NoteTint> MAP_CODEC = MapCodec.unit(new NoteTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            try {
                return TintHelper.opaque(TintHelper.researchNoteColor(
                        stack, 1, DiscoveredNoteData.read(stack)));
            } catch (Exception e) {
                return 0xFFFFFFFF;
            }
        }

        @Override
        public MapCodec<NoteTint> type() {
            return MAP_CODEC;
        }
    }
}
