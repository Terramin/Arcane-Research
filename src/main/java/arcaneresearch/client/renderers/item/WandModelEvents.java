package arcaneresearch.client.renderers.item;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.items.wands.WandItems;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WandModelEvents {
    @SubscribeEvent
    public static void registerStaffModel(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "staff"), "inventory"));
    }

    @SubscribeEvent
    public static void prepareWandModels(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation wandKey = new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "wand"), "inventory");
        ModelResourceLocation staffKey = new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "staff"), "inventory");
        BakedModel wandBase = event.getModels().get(wandKey);
        BakedModel staffBase = event.getModels().get(staffKey);
        if (wandBase == null) {
            return;
        }
        BakedModel staff = staffBase != null
                ? new CustomRendererItemModel(new SimpleBakedModel(List.of(),
                        new EnumMap<>(Direction.class), false, false, false,
                        staffBase.getParticleIcon(), staffBase.getTransforms(), ItemOverrides.EMPTY))
                : new CustomRendererItemModel(wandBase);
        event.getModels().put(wandKey, new WandDisplayModel(new CustomRendererItemModel(wandBase), staff));
    }

    @SubscribeEvent
    public static void registerWandColors(RegisterColorHandlersEvent.Item event) {
        try {
            WandItems.initWands();
        } catch (Exception ignored) {
        }
    }
}
