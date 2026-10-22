package thaumictinker.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import arcaneresearch.api.research.ResearchCategories;

 













 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TtIntegration {

    public static final String TT_CATEGORY = "TT_CATEGORY";

    private TtIntegration() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            init();
        });
    }

    public static void init() {
        
        
        
        ResearchCategories.registerCategory(TT_CATEGORY, null,
                new arcaneresearch.api.aspects.AspectList()
                        .add(arcaneresearch.api.aspects.Aspect.MAGIC, 20)
                        .add(arcaneresearch.api.aspects.Aspect.SOUL, 15)
                        .add(arcaneresearch.api.aspects.Aspect.FLUX, 10),
                new ResourceLocation("thaumictinker", "textures/misc/r_enchanting.png"),
                new ResourceLocation("arcaneresearch", "textures/gui/gui_research_back_1.jpg"),
                new ResourceLocation("arcaneresearch", "textures/gui/gui_research_back_over.png"));
        TtResearchLoader.load();

        
        
        thaumictinker.necromancy.NecromancyFeature.aspects();
        thaumictinker.kami.KamiFeature.aspects();
    }
}
