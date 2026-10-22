package arcaneresearch.discovered.client;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import org.jspecify.annotations.Nullable;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNoteSetup;

 








 
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class NoteCompleteProperty {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "complete");

    private NoteCompleteProperty() {
    }

    @SubscribeEvent
    public static void register(RegisterConditionalItemModelPropertyEvent event) {
        event.register(ID, Complete.MAP_CODEC);
    }

    public record Complete() implements ConditionalItemModelProperty {
        public static final MapCodec<Complete> MAP_CODEC = MapCodec.unit(new Complete());

        @Override
        public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
                int seed, ItemDisplayContext displayContext) {
            return itemStack.getItem() == DiscoveredNoteSetup.getNoteItem()
                    && DiscoveredNoteData.read(itemStack).isComplete();
        }

        @Override
        public MapCodec<Complete> type() {
            return MAP_CODEC;
        }
    }
}
