package arcaneresearch.common.blocks.world.ore;

import net.minecraft.world.level.block.Block;
import arcaneresearch.api.aspects.Aspect;

import java.util.function.Supplier;

public enum ShardType {
    AIR(0, "air", Aspect.AIR),
    FIRE(1, "fire", Aspect.FIRE),
    WATER(2, "water", Aspect.WATER),
    EARTH(3, "earth", Aspect.EARTH),
    ORDER(4, "order", Aspect.ORDER),
    ENTROPY(5, "entropy", Aspect.ENTROPY),
    FLUX(6, "flux", Aspect.FLUX);

    private static final ShardType[] METADATA_LOOKUP = new ShardType[values().length];

    static {
        for (ShardType type : values()) {
            METADATA_LOOKUP[type.getMetadata()] = type;
        }
    }

    private final int metadata;
    private final String name;
    private final Aspect aspect;
    private Supplier<Block> ore;

    ShardType(int metadata, String unlocalizedName, Aspect aspect) {
        this.metadata = metadata;
        this.name = unlocalizedName;
        this.aspect = aspect;
    }

    public static int getMetaByAspect(Aspect a) {
        for (ShardType type : values()) {
            if (type.getAspect() == a) {
                return type.ordinal();
            }
        }
        return -1;
    }

    public static ShardType byMetadata(int metadata) {
        if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
            metadata = 0;
        }
        return METADATA_LOOKUP[metadata];
    }

    public int getMetadata() {
        return metadata;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public Block getOre() {
        if (ore != null) {
            return ore.get();
        }

        return switch (this) {
            case AIR -> arcaneresearch.init.ModBlocks.CRYSTAL_AIR.get();
            case FIRE -> arcaneresearch.init.ModBlocks.CRYSTAL_FIRE.get();
            case WATER -> arcaneresearch.init.ModBlocks.CRYSTAL_WATER.get();
            case EARTH -> arcaneresearch.init.ModBlocks.CRYSTAL_EARTH.get();
            case ORDER -> arcaneresearch.init.ModBlocks.CRYSTAL_ORDER.get();
            case ENTROPY -> arcaneresearch.init.ModBlocks.CRYSTAL_ENTROPY.get();
            case FLUX -> arcaneresearch.init.ModBlocks.CRYSTAL_FLUX.get();
        };
    }

    public void setOre(Block b) {
        this.ore = () -> b;
    }

    public void setOre(Supplier<Block> supplier) {
        this.ore = supplier;
    }

    public String getUnlocalizedName() {
        return name;
    }

    @Override
    public String toString() {
        return getUnlocalizedName();
    }

    public String getName() {
        return name;
    }
}
