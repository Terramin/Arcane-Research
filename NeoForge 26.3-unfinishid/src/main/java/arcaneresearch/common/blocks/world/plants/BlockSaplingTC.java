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

// NEOFORGE_PORT: 1:1 from Forge common/blocks/world/plants/BlockSaplingTC.java.
// REQUIRED changes (vanilla 26.x, verified via javap/javap -c on 26.3.0.23-beta):
//   - AbstractTreeGrower (subclassable) -> TreeGrower (final, data-key based).
//     Anonymous growers become `new TreeGrower(name, trees, megaTrees, flowerTrees,
//     shortestTreeType)`; single-tree growers put their key in `trees` and as shortest.
//     Feature keys resolve via Registries.FEATURE (ConfiguredFeature registry removed;
//     ids arcaneresearch:greatwood_tree/big_magic_tree/big_silverwood_tree match
//     ModFeatures registrations).
//   - advanceTree 2x2 logic: same shape, Registries.FEATURE holder lookup.
//   - LevelReader.getRandom() -> getRandom() (protected field).
// getCloneItemStack/getFlammability/getFireSpreadSpeed/isValidBonemealTarget unchanged
// (vanilla/IBlockExtension signatures kept).
public class BlockSaplingTC extends SaplingBlock {

    public BlockSaplingTC(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    private static TreeGrower singleTreeGrower(String name, String featurePath) {
        var key = ResourceKey.create(Registries.FEATURE,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, featurePath));
        return new TreeGrower(name, WeightedList.of(key), WeightedList.of(), WeightedList.of(), key);
    }

    // NEOFORGE_PORT: Forge grew the 2x2 greatwood mega-tree (big_magic_tree) in a
    // custom advanceTree. 26.x TreeGrower handles 2x2 mega-trees itself via the
    // megaTrees list, so the grower carries both keys and no override is needed.
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

    // NEOFORGE_PORT: no advanceTree override — SaplingBlock.advanceTree grows
    // through the TreeGrower above (2x2 greatwood -> big_magic_tree mega-tree).
}
