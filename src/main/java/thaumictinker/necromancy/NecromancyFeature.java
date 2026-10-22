package thaumictinker.necromancy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.aspects.Aspect;
import thaumictinker.registry.TtRegistry;

 



 
public final class NecromancyFeature {

    public static final String SUMMON = "SUMMON";
    public static final String SUMMON_TABLET = "SUMMON0";
    public static final int ASPECT_COUNT = 17;

      
    public static final List<Aspect> ASPECTS = List.of(
            Aspect.WATER,     
            Aspect.MAN,       
            Aspect.AIR,       
            Aspect.FLIGHT,    
            Aspect.FIRE,      
            Aspect.MAGIC,     
            Aspect.UNDEAD,    
            Aspect.LIFE,      
            Aspect.BEAST,     
            Aspect.FLUX,      
            Aspect.EARTH,     
            Aspect.ELDRITCH,  
            Aspect.MOTION,    
            Aspect.METAL,     
            Aspect.COLD,      
            Aspect.DARKNESS,  
            Aspect.PLANT      
    );

    private static final String[] BASE_IDS = {
            "aqua_soul_aspect", "humanus_soul_aspect", "aer_soul_aspect",
            "volatus_soul_aspect", "ignis_soul_aspect", "praecantatio_soul_aspect",
            "exanimis_soul_aspect", "victus_soul_aspect", "bestia_soul_aspect",
            "vitium_soul_aspect", "terra_soul_aspect", "alienis_soul_aspect",
            "motus_soul_aspect", "metallum_soul_aspect", "gelum_soul_aspect",
            "tenebrae_soul_aspect", "herba_soul_aspect"
    };
    private static final String[] CONDENSED_IDS = {
            "condensed_aqua_soul_aspect", "condensed_humanus_soul_aspect", "condensed_aer_soul_aspect",
            "condensed_volatus_soul_aspect", "condensed_ignis_soul_aspect", "condensed_praecantatio_soul_aspect",
            "condensed_exanimis_soul_aspect", "condensed_victus_soul_aspect", "condensed_bestia_soul_aspect",
            "condensed_vitium_soul_aspect", "condensed_terra_soul_aspect", "condensed_alienis_soul_aspect",
            "condensed_motus_soul_aspect", "condensed_metallum_soul_aspect", "condensed_gelum_soul_aspect",
            "condensed_tenebrae_soul_aspect", "condensed_herba_soul_aspect"
    };
    private static final String[] INFUSED_IDS = {
            "infused_aqua_soul_aspect", "infused_humanus_soul_aspect", "infused_aer_soul_aspect",
            "infused_volatus_soul_aspect", "infused_ignis_soul_aspect", "infused_praecantatio_soul_aspect",
            "infused_exanimis_soul_aspect", "infused_victus_soul_aspect", "infused_bestia_soul_aspect",
            "infused_vitium_soul_aspect", "infused_terra_soul_aspect", "infused_alienis_soul_aspect",
            "infused_motus_soul_aspect", "infused_metallum_soul_aspect", "infused_gelum_soul_aspect",
            "infused_tenebrae_soul_aspect", "infused_herba_soul_aspect"
    };

    public static final List<SoulAspectItem> BASE = new ArrayList<>();
    public static final List<SoulAspectItem> CONDENSED = new ArrayList<>();
    public static final List<SoulAspectItem> INFUSED = new ArrayList<>();

    static {
        for (int index = 0; index < ASPECT_COUNT; index++) {
            Aspect aspect = ASPECTS.get(index);
            BASE.add(soul(aspect, SoulAspectItem.Form.BASE, BASE_IDS[index], true));
            CONDENSED.add(soul(aspect, SoulAspectItem.Form.CONDENSED, CONDENSED_IDS[index], false));
            INFUSED.add(soul(aspect, SoulAspectItem.Form.INFUSED, INFUSED_IDS[index], true));
        }
    }

    public static final NecromancyTabletBlock TABLET =
            (NecromancyTabletBlock) TtRegistry.blockWithItem("necromancy_tablet",
                    new NecromancyTabletBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F, 50.0F)
                            .sound(SoundType.METAL)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<NecromancyTabletBlockEntity> TABLET_ENTITY =
            TtRegistry.blockEntity("necromancy_tablet",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(NecromancyTabletBlockEntity::new, TABLET)
                            .build(null));

    private NecromancyFeature() {
    }

    private static SoulAspectItem soul(Aspect aspect, SoulAspectItem.Form form, String name, boolean inTab) {
        SoulAspectItem item = new SoulAspectItem(
                new net.minecraft.world.item.Item.Properties().stacksTo(16), aspect, form);
        if (inTab) {
            TtRegistry.itemWithTab(name, () -> item);
        } else {
            TtRegistry.item(name, () -> item);
        }
        return item;
    }

      
    public static void load() {
    }

     



 
    public static void aspects() {
        for (int index = 0; index < ASPECT_COUNT; index++) {
            arcaneresearch.api.ArcaneresearchApi.registerObjectTag(
                    new ItemStack(BASE.get(index)),
                    new arcaneresearch.api.aspects.AspectList().add(ASPECTS.get(index), 8));
        }
    }

    public static ItemStack soulStack(Aspect aspect) {
        return new ItemStack(BASE.get(ASPECTS.indexOf(aspect)));
    }

    public static ItemStack infusedStack(Aspect aspect) {
        return new ItemStack(INFUSED.get(ASPECTS.indexOf(aspect)));
    }
}
