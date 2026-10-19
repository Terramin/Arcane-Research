package arcaneresearch.client.renderers.item;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.item.CustomRendererItemModel;
import java.util.Map;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public class EldritchPortalModelEvents {
    @SubscribeEvent
    public static void bakePortalModel(ModelEvent.ModifyBakingResult event) {
        try {
            Map<ResourceLocation, BakedModel> models = event.getModels();
            ModelResourceLocation key = new ModelResourceLocation(
                    new ResourceLocation(Arcaneresearch.MODID, "eldritch_portal"), "inventory");
            BakedModel base = models.get(key);
            if (base != null && !base.isCustomRenderer()) {
                models.put(key, new CustomRendererItemModel(base));
            }
        } catch (Exception ignored) {
        }
    }
}
