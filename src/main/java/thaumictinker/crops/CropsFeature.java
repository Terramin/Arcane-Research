package thaumictinker.crops;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import arcaneresearch.api.aspects.Aspect;
import thaumictinker.registry.TtRegistry;

 




 
public final class CropsFeature {

    public static final InfusedFarmlandBlock INFUSED_FARMLAND =
            (InfusedFarmlandBlock) TtRegistry.blockWithItem("infused_farmland",
                    new InfusedFarmlandBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIRT)
                            .strength(0.6F)
                            .sound(SoundType.GRAVEL)));

    public static final InfusedGrainBlock INFUSED_GRAIN =
            (InfusedGrainBlock) TtRegistry.blockWithItem("infused_grain_crop",
                    new InfusedGrainBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.CROP)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<InfusedFarmlandBlockEntity> INFUSED_FARMLAND_ENTITY =
            TtRegistry.blockEntity("infused_farmland",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(InfusedFarmlandBlockEntity::new, INFUSED_FARMLAND)
                            .build(null));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<InfusedGrainBlockEntity> INFUSED_GRAIN_ENTITY =
            TtRegistry.blockEntity("infused_grain_crop",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(InfusedGrainBlockEntity::new, INFUSED_GRAIN)
                            .build(null));

    public static final net.minecraft.world.item.Item INFUSED_SEEDS;

    static {
        InfusedSeedsItem seeds = new InfusedSeedsItem(INFUSED_GRAIN,
                new net.minecraft.world.item.Item.Properties().rarity(Rarity.RARE));
        INFUSED_SEEDS = TtRegistry.itemWithVariants("infused_seeds", seeds, seeds);
    }

    public static final net.minecraft.world.item.Item AER_GRAIN = grain("aer_infused_grain", Aspect.AIR);
    public static final net.minecraft.world.item.Item IGNIS_GRAIN = grain("ignis_infused_grain", Aspect.FIRE);
    public static final net.minecraft.world.item.Item TERRA_GRAIN = grain("terra_infused_grain", Aspect.EARTH);
    public static final net.minecraft.world.item.Item AQUA_GRAIN = grain("aqua_infused_grain", Aspect.WATER);

    private static net.minecraft.world.item.Item grain(String name, Aspect aspect) {
        return TtRegistry.itemWithTab(name,
                () -> new InfusedGrainItem(aspect, new net.minecraft.world.item.Item.Properties()));
    }

    private CropsFeature() {
    }

      
    public static void load() {
    }
}
