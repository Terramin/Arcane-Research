package arcaneresearch.common.blocks.world.plants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.Arcaneresearch;
import javax.annotation.Nullable;
public class BlockSaplingTC extends SaplingBlock {
    public BlockSaplingTC(AbstractTreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }
    public static BlockSaplingTC createGreatwood() {
        return new BlockSaplingTC(
                new AbstractTreeGrower() {
                    @Nullable
                    @Override
                    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
                        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                                new ResourceLocation(Arcaneresearch.MODID, "greatwood_tree"));
                    }
                },
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS));
    }
    public static BlockSaplingTC createSilverwood() {
        return new BlockSaplingTC(
                new AbstractTreeGrower() {
                    @Nullable
                    @Override
                    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
                        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                                new ResourceLocation(Arcaneresearch.MODID, "big_silverwood_tree"));
                    }
                },
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.QUARTZ)
                        .noCollission()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .lightLevel(state -> 5));
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this.asItem());
    }
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        if (!super.isValidBonemealTarget(level, pos, state, isClient)) {
            return false;
        }
        if (level instanceof net.minecraft.world.level.Level realLevel) {
            return realLevel.random.nextFloat() < 0.25f;
        }
        return true;
    }
    @Override
    public void advanceTree(net.minecraft.server.level.ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (this == arcaneresearch.init.ModBlocks.GREATWOOD_SAPLING.get()) {
            for (int i = 0; i >= -1; --i) {
                for (int j = 0; j >= -1; --j) {
                    if (isTwoByTwoOfType(level, pos, i, j)) {
                        BlockPos origin = pos.offset(i, 0, j);
                        net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(
                                Registries.CONFIGURED_FEATURE,
                                new ResourceLocation(Arcaneresearch.MODID, "big_magic_tree"));
                        var holder = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(key);
                        if (holder.isPresent()) {
                            BlockState air = Blocks.AIR.defaultBlockState();
                            level.setBlock(origin, air, 4);
                            level.setBlock(origin.offset(1, 0, 0), air, 4);
                            level.setBlock(origin.offset(0, 0, 1), air, 4);
                            level.setBlock(origin.offset(1, 0, 1), air, 4);
                            if (!holder.get().value().place(level, level.getChunkSource().getGenerator(),
                                    random, origin)) {
                                level.setBlock(origin, state, 4);
                                level.setBlock(origin.offset(1, 0, 0), state, 4);
                                level.setBlock(origin.offset(0, 0, 1), state, 4);
                                level.setBlock(origin.offset(1, 0, 1), state, 4);
                            }
                            return;
                        }
                    }
                }
            }
        }
        super.advanceTree(level, pos, state, random);
    }
    private boolean isTwoByTwoOfType(net.minecraft.world.level.LevelReader level, BlockPos pos, int dx, int dz) {
        Block block = this;
        return level.getBlockState(pos.offset(dx, 0, dz)).is(block)
                && level.getBlockState(pos.offset(dx + 1, 0, dz)).is(block)
                && level.getBlockState(pos.offset(dx, 0, dz + 1)).is(block)
                && level.getBlockState(pos.offset(dx + 1, 0, dz + 1)).is(block);
    }
}
