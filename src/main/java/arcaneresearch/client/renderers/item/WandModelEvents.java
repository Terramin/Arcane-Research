package arcaneresearch.client.renderers.item;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.common.items.wands.WandItems;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WandModelEvents {
    @SubscribeEvent
    public static void registerWandPartModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(new ResourceLocation("arcaneresearch", "wand/parts_stitch"), "inventory"));
        event.register(new ModelResourceLocation(new ResourceLocation("arcaneresearch", "staff"), "inventory"));
    }
    @SubscribeEvent
    public static void bakeWandModels(ModelEvent.ModifyBakingResult event) {
        try {
            WandItems.initWands();
        } catch (Exception ignored) {
        }
        WandMeshModel.cache.clear();
        var models = event.getModels();
        registerWandMesh(models, "wand", false);
        registerWandMesh(models, "staff", true);
    }
    private static void registerWandMesh(java.util.Map<ResourceLocation, BakedModel> models,
                                         String itemName, boolean staff) {
        ModelResourceLocation key = new ModelResourceLocation(new ResourceLocation("arcaneresearch", itemName), "inventory");
        ResourceLocation icon = new ResourceLocation("arcaneresearch",
                staff ? "item/wand/rod_greatwood_mat" : "item/wand/rod_wood_mat");
        // 26.3-паритет: display-трансформы живут в JSON (models/item/wand.json
        // / staff.json — дословный порт display-блока 26.3). Забираем их у
        // запечённого плейсхолдера ДО подмены на мешь-модель; WandTransforms
        // остаётся fallback-константами (числа совпадают с JSON).
        ItemTransforms jsonTransforms = null;
        BakedModel placeholder = models.get(key);
        if (placeholder != null) {
            try {
                jsonTransforms = placeholder.getTransforms();
            } catch (Exception ignored) {
            }
        }
        WandMeshModel wandModel = new WandMeshModel(itemName);
        ItemTransforms init = jsonTransforms != null
                ? jsonTransforms : (staff ? WandTransforms.STAFF : WandTransforms.WAND);
        wandModel.initialize(init, icon);
        models.put(key, wandModel);
        if (staff) {
            WandMeshModel.staffTransforms = wandModel.getTransforms();
        }
        for (WandCap cap : WandCap.caps.values()) {
            String capKey = (staff ? "staff" : "wand") + cap.getTag();
            WandMeshModel capModel = new WandMeshModel(capKey, cap, staff);
            capModel.initialize(wandModel.getTransforms(), icon);
            if (!staff) {
                String sceptreKey = "sceptre" + cap.getTag();
                WandMeshModel sceptreModel = new WandMeshModel(sceptreKey, cap);
                sceptreModel.initialize(wandModel.getTransforms(), icon);
            }
        }
        for (WandRod rod : WandRod.rods.values()) {
            if (rod.isStaff() != staff) {
                continue;
            }
            String rodKey = (staff ? "staff" : "wand") + rod.getTag();
            WandMeshModel rodModel = new WandMeshModel(rodKey, rod);
            rodModel.initialize(wandModel.getTransforms(), icon);
        }
        for (ItemFocusBasic focus : ItemFocusBasic.foci.values()) {
            String focusKey = "focus" + focus.getFocusId();
            WandMeshModel focusModel = new WandMeshModel(focusKey, focus);
            focusModel.initialize(wandModel.getTransforms(), icon);
        }
    }
    @SubscribeEvent
    public static void registerWandColors(RegisterColorHandlersEvent.Item event) {
        try {
            event.register((stack, tintIndex) ->
                    arcaneresearch.helper.TintHelper.wandFocusColor(stack, tintIndex),
                    WandItems.WAND.get());
        } catch (Exception ignored) {
        }
    }
}
