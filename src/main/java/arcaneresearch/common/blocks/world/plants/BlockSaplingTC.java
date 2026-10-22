package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.Arcaneresearch;













public class BlockSaplingTC extends SaplingBlock {

    public BlockSaplingTC(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    private static TreeGrower singleTreeGrower(String name, String featurePath) {
        var key = ResourceKey.create(Registries.FEATURE,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, featurePath));
        return new TreeGrower(name, WeightedList.of(key), WeightedList.of(), WeightedList.of(), key);
    }

    
    
    
    private static TreeGrower megaTreeGrower(String name, String featurePath, String megaFeaturePath) {
        var key = ResourceKey.create(Registries.FEATURE,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, featurePath));
        var mega = ResourceKey.create(Registries.FEATURE,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, megaFeaturePath));
        return new TreeGrower(name, WeightedList.of(key), WeightedList.of(mega), WeightedList.of(), key);
    }

    public static BlockSaplingTC createGreatwood(BlockBehaviour.Properties props) {
        return new BlockSaplingTC(
                megaTreeGrower("greatwood", "greatwood_tree", "big_magic_tree"),
                props
                        .mapColor(MapColor.PLANT)
                        .noCollision()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS));
    }

    public static BlockSaplingTC createSilverwood(BlockBehaviour.Properties props) {
        return new BlockSaplingTC(
                singleTreeGrower("silverwood", "big_silverwood_tree"),
                props
                        .mapColor(MapColor.QUARTZ)
                        .noCollision()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .lightLevel(state -> 5));
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
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
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        if (!super.isValidBonemealTarget(level, pos, state, source)) {
            return false;
        }
        if (level instanceof net.minecraft.world.level.Level realLevel) {
            return realLevel.getRandom().nextFloat() < 0.25f;
        }
        return true;
    }

    
    
    
    @Override
    public void advanceTree(net.minecraft.server.level.ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (this == arcaneresearch.init.ModBlocks.GREATWOOD_SAPLING.get()) {
            for (int i = 0; i >= -1; --i) {
                for (int j = 0; j >= -1; --j) {
                    if (isTwoByTwoOfType(level, pos, i, j)) {
                        this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
                        return;
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
