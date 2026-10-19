package arcaneresearch.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.common.blocks.BlockTC;

public class BlockStoneTC extends BlockTC {

    private final boolean canSpawnMobs;

    public BlockStoneTC(Properties properties, boolean canSpawnMobs) {
        super(properties);
        this.canSpawnMobs = canSpawnMobs;
    }

    public static BlockStoneTC create(net.minecraft.world.level.block.state.BlockBehaviour.Properties props, boolean canSpawnMobs) {
        return new BlockStoneTC(
                props
                        .mapColor(MapColor.STONE)
                        .strength(2.0f, 10.0f)
                        .sound(SoundType.STONE),
                canSpawnMobs
        );
    }

    public static BlockStoneTC createUnbreakable(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        return new BlockStoneTC(
                props
                        .mapColor(MapColor.STONE)
                        .strength(-1.0f, 3600000.0f)
                        .sound(SoundType.STONE)
                        .noLootTable(),
                false
        );
    }

    public static BlockStoneTC createReinforced(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        return new BlockStoneTC(
                props
                        .mapColor(MapColor.STONE)
                        .strength(15.0f, 1000.0f)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops(),
                true
        );
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos,
                                net.minecraft.world.entity.SpawnPlacementType type,
                                net.minecraft.world.entity.EntityType<?> entityType) {
        return canSpawnMobs;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return state.getDestroySpeed(level, pos) >= 0.0F;
    }
}
