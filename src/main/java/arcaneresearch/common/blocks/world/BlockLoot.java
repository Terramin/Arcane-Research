package arcaneresearch.common.blocks.world;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.lib.utils.Utils;
import arcaneresearch.init.ModSounds;
import java.util.List;
public class BlockLoot extends Block {
    private static final VoxelShape CRATE_SHAPE = Block.box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape URN_SHAPE = Block.box(2, 1, 2, 14, 13, 14);
    private final LootType lootType;
    private final boolean isUrn;
    public BlockLoot(LootType type, boolean isUrn) {
        super(createProperties(isUrn));
        this.lootType = type;
        this.isUrn = isUrn;
    }
    private static BlockBehaviour.Properties createProperties(boolean isUrn) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .mapColor(isUrn ? MapColor.TERRACOTTA_BROWN : MapColor.WOOD)
                .strength(0.15f, 0.0f)
                .noOcclusion();
        if (isUrn) {
            props.sound(ModSounds.URN_TYPE);
        } else {
            props.sound(SoundType.WOOD);
        }
        return props;
    }
    public static BlockLoot createCrateCommon() {
        return new BlockLoot(LootType.COMMON, false);
    }
    public static BlockLoot createCrateUncommon() {
        return new BlockLoot(LootType.UNCOMMON, false);
    }
    public static BlockLoot createCrateRare() {
        return new BlockLoot(LootType.RARE, false);
    }
    public static BlockLoot createUrnCommon() {
        return new BlockLoot(LootType.COMMON, true);
    }
    public static BlockLoot createUrnUncommon() {
        return new BlockLoot(LootType.UNCOMMON, true);
    }
    public static BlockLoot createUrnRare() {
        return new BlockLoot(LootType.RARE, true);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isUrn ? URN_SHAPE : CRATE_SHAPE;
    }
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new java.util.ArrayList<>();
        RandomSource rand = params.getLevel().getRandom();
        int dropCount = 1 + lootType.ordinal() + rand.nextInt(3);
        for (int i = 0; i < dropCount; i++) {
            ItemStack loot = Utils.generateLoot(lootType.ordinal(), rand);
            if (!loot.isEmpty()) {
                drops.add(loot.copy());
            }
        }
        return drops;
    }
    public LootType getLootType() {
        return lootType;
    }
    public boolean isUrn() {
        return isUrn;
    }
    public enum LootType {
        COMMON,
        UNCOMMON,
        RARE
    }
}
