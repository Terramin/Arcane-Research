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

/** Enables the item renderer for jars that can carry an aspect label. */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JarItemModelEvents {
    private JarItemModelEvents() {
    }

    @SubscribeEvent
    public static void bakeJarItemModels(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (String path : new String[]{"jar_normal", "jar_void"}) {
            ModelResourceLocation key = new ModelResourceLocation(
                    ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, path), "inventory");
            BakedModel base = models.get(key);
            if (base != null && !(base instanceof JarItemModel)) {
                models.put(key, new JarItemModel(base));
            }
        }
    }
}
