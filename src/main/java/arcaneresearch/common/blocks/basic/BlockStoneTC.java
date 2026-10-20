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
    public static BlockStoneTC create(boolean canSpawnMobs) {
        return new BlockStoneTC(
                Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(2.0f, 10.0f)
                        .sound(SoundType.STONE),
                canSpawnMobs
        );
    }
    public static BlockStoneTC createUnbreakable() {
        return new BlockStoneTC(
                Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(-1.0f, 3600000.0f)
                        .sound(SoundType.STONE)
                        .noLootTable(),
                false
        );
    }
    public static BlockStoneTC createReinforced() {
        return new BlockStoneTC(
                Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(15.0f, 1000.0f)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops(),
                true
        );
    }
    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos,
                                net.minecraft.world.entity.SpawnPlacements.Type type,
                                net.minecraft.world.entity.EntityType<?> entityType) {
        return canSpawnMobs;
    }
    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return state.getDestroySpeed(level, pos) >= 0.0F;
    }
}
