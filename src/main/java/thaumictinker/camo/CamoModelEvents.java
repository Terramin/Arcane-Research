package thaumictinker.camo;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.fml.common.Mod;

import arcaneresearch.Arcaneresearch;

import java.util.List;
import java.util.Map;

 



 
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CamoModelEvents {

    private CamoModelEvents() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        models.replaceAll((key, model) -> isCamoBlockModel(key) ? new CamoModel(model) : model);
    }

    private static boolean isCamoBlockModel(ResourceLocation id) {
        if (!Arcaneresearch.MODID.equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.equals("ethereal_platform")
                || path.equals("transvector_interface")
                || (path.startsWith("transvector_dislocator_") && !path.endsWith("item"));
    }
}
