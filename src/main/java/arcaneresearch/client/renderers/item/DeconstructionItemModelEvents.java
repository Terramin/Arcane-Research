package arcaneresearch.client.renderers.item;

import arcaneresearch.Arcaneresearch;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/** Marks the deconstruction table item as custom-rendered. */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DeconstructionItemModelEvents {
    private DeconstructionItemModelEvents() {
    }

    @SubscribeEvent
    public static void bakeItemModel(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        ModelResourceLocation key = new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "deconstruction_table"), "inventory");
        BakedModel base = models.get(key);
        if (base != null && !(base instanceof CustomRendererItemModel)) {
            models.put(key, new CustomRendererItemModel(base));
        }
    }
}
