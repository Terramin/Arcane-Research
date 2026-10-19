package arcaneresearch.common.items.wands;
import arcaneresearch.common.lib.aura.EldritchOculusRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.blocks.BlocksTC;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.IWandTriggerManager;
import arcaneresearch.api.wands.WandTriggerRegistry;
import arcaneresearch.common.blocks.IBlockEnabled;
import arcaneresearch.common.blocks.IBlockFacingHorizontal;
import arcaneresearch.common.entities.EntitySpecialItem;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.common.lib.wands.WandResearchHelper;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import arcaneresearch.common.tiles.crafting.TilePedestal;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
public class WandTriggers implements IWandTriggerManager {
    private static boolean triggersRegistered = false;
    public static synchronized void registerTriggers() {
        if (triggersRegistered) {
            return;
        }
        triggersRegistered = true;
        try {
            WandTriggerRegistry.registerWandBlockTrigger(new WandTriggers(), 11,
                    net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState());
        } catch (Exception ignored) {
        }
        try {
            WandTriggerRegistry.registerWandBlockTrigger(new WandTriggers(), 6,
                    arcaneresearch.common.blocks.aura.AuraBlocks.ELDRITCH_ALTAR.get().defaultBlockState());
        } catch (Exception ignored) {
        }
    }
    private static <T extends Comparable<T>> BlockState withIfHas(BlockState state, Property<T> prop, T value) {
        try {
            if (state.hasProperty(prop)) {
                return state.setValue(prop, value);
            }
        } catch (Exception ignored) {
        }
        return state;
    }
    private static void sendBamf(Level level, BlockPos pos, int color, boolean sound, boolean flair) {
        PacketHandler.sendToNear(new PacketFXBlockBamf(pos, color, sound, flair, null),
                new net.minecraftforge.network.PacketDistributor.TargetPoint(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32.0D, level.dimension()));
    }
    public static boolean createGolemPress(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        BlockPos start = null;
        outer:
        for (int yy = -1; yy < 2; yy++) {
            for (int xx = -1; xx < 2; xx++) {
                for (int zz = -1; zz < 2; zz++) {
                    if (level.getBlockState(pos.offset(xx, yy, zz)) == Blocks.PISTON.defaultBlockState()
                            .setValue(PistonBaseBlock.FACING, Direction.UP)) {
                        start = pos.offset(xx, yy, zz);
                        break outer;
                    }
                }
            }
        }
        if (start == null) {
            return false;
        }
        if (level.getBlockState(start.above()).getBlock() != Blocks.IRON_BARS) {
            return false;
        }
        Block tableStone;
        try {
            tableStone = BlocksTC.tableStone.get();
        } catch (Exception e) {
            return false;
        }
        Direction face = null;
        for (Direction f : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(start.relative(f)).getBlock() == tableStone) {
                face = f;
                break;
            }
        }
        if (face == null) {
            return false;
        }
        Direction perp = face.getClockWise(Direction.Axis.Y);
        if (level.getBlockState(start.relative(face).relative(perp)).getBlock() != Blocks.ANVIL) {
            return false;
        }
        if (level.getBlockState(start.relative(face).relative(perp).relative(perp.getClockWise(Direction.Axis.Y)))
                .getBlock() != Blocks.CAULDRON) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (wand.consumeAllVis(stack, player,
                new AspectList().add(Aspect.FIRE, 50).add(Aspect.ORDER, 50).add(Aspect.AIR, 50), false, true)) {
            try {
                setBlockSparkle(level, start.above(), BlocksTC.placeholderBars.get().defaultBlockState());
                setBlockSparkle(level, start.relative(face).relative(perp),
                        BlocksTC.placeholderAnvil.get().defaultBlockState());
                setBlockSparkle(level,
                        start.relative(face).relative(perp).relative(perp.getClockWise(Direction.Axis.Y)),
                        BlocksTC.placeholderCauldron.get().defaultBlockState());
                setBlockSparkle(level, start.relative(face),
                        BlocksTC.placeholderTable.get().defaultBlockState());
                BlockState builder = BlocksTC.golemBuilder.get().defaultBlockState();
                builder = withIfHas(builder, IBlockFacingHorizontal.FACING, face);
                setBlockSparkle(level, start, builder);
            } catch (Exception e) {
                return false;
            }
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    WandSounds.wand(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }
    public static boolean createThaumonomicon(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand) || wand.getFocus(stack) != null) {
            return false;
        }
        level.removeBlock(pos, false);
        ItemStack book;
        try {
            book = new ItemStack(ModItems.THAUMONOMICON.get());
        } catch (Exception e) {
            return false;
        }
        EntitySpecialItem dropped = new EntitySpecialItem(level, pos.getX() + 0.5D,
                pos.getY() + 0.3D, pos.getZ() + 0.5D, book);
        try {
            dropped.setDeltaMovement(0.0D, 0.0D, 0.0D);
        } catch (Exception ignored) {
        }
        level.addFreshEntity(dropped);
        sendBamf(level, pos, -9999, true, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
    public static boolean createCrucible(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        Block crucible;
        try {
            crucible = BlocksTC.crucible.get();
        } catch (Exception e) {
            return false;
        }
        level.removeBlock(pos, false);
        level.setBlockAndUpdate(pos, crucible.defaultBlockState());
        sendBamf(level, pos, -9999, true, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
    public static boolean createInfusionAltar(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (fitInfusionAltar(level, pos) && wand.consumeAllVis(stack, player,
                new AspectList().add(Aspect.FIRE, 75).add(Aspect.EARTH, 75).add(Aspect.ORDER, 75)
                        .add(Aspect.AIR, 75).add(Aspect.ENTROPY, 75).add(Aspect.WATER, 75),
                false, true)) {
            if (!level.isClientSide) {
                replaceInfusionAltar(level, pos);
                return true;
            }
            return false;
        }
        return false;
    }
    public static boolean fitInfusionAltar(Level level, BlockPos pos) {
        Block pillar;
        try {
            pillar = BlocksTC.stoneArcane.get();
        } catch (Exception e) {
            return false;
        }
        Block[][][] blueprint = {
                {{pillar, null, pillar}, {null, null, null}, {pillar, null, pillar}},
                {{pillar, null, pillar}, {null, null, null}, {pillar, null, pillar}}};
        BlockState first = level.getBlockState(pos.offset(-1, -2, -1));
        if (first != level.getBlockState(pos.offset(1, -2, -1)) || first != level.getBlockState(pos.offset(1, -2, 1))
                || first != level.getBlockState(pos.offset(-1, -2, 1))) {
            return false;
        }
        for (int yy = 0; yy < 2; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockPos current = pos.offset(xx - 1, -(yy + 1), zz - 1);
                    if (blueprint[yy][xx][zz] == null) {
                        if (xx == 1 && zz == 1 && yy == 1) {
                            BlockEntity tile = level.getBlockEntity(current);
                            if (!(tile instanceof TilePedestal)) {
                                return false;
                            }
                        } else if (!level.isEmptyBlock(current)) {
                            return false;
                        }
                    } else {
                        BlockState state = level.getBlockState(current);
                        if (state.getBlock() != blueprint[yy][xx][zz]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public static void replaceInfusionAltar(Level level, BlockPos pos) {
        BlockState first = level.getBlockState(pos.offset(-1, -2, -1));
        Block pillar;
        try {
            pillar = BlocksTC.pillarArcane.get();
        } catch (Exception e) {
            return;
        }
        setBlockSparkle(level, pos.offset(-1, -1, -1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(1, -1, -1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(1, -1, 1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(-1, -1, 1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(-1, -2, -1),
                withIfHas(pillar.defaultBlockState(), net.minecraft.world.level.block.state.properties.DirectionProperty.create("facing", Direction.Plane.HORIZONTAL), Direction.EAST));
        setBlockSparkle(level, pos.offset(1, -2, -1),
                withIfHas(pillar.defaultBlockState(), net.minecraft.world.level.block.state.properties.DirectionProperty.create("facing", Direction.Plane.HORIZONTAL), Direction.SOUTH));
        setBlockSparkle(level, pos.offset(1, -2, 1),
                withIfHas(pillar.defaultBlockState(), net.minecraft.world.level.block.state.properties.DirectionProperty.create("facing", Direction.Plane.HORIZONTAL), Direction.WEST));
        setBlockSparkle(level, pos.offset(-1, -2, 1),
                withIfHas(pillar.defaultBlockState(), net.minecraft.world.level.block.state.properties.DirectionProperty.create("facing", Direction.Plane.HORIZONTAL), Direction.NORTH));
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileInfusionMatrix matrix) {
            matrix.active = true;
            matrix.setChanged();
            level.sendBlockUpdated(pos, first, level.getBlockState(pos), 3);
        }
        sendBamf(level, pos, -9999, true, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    private static void setBlockSparkle(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state);
        sendBamf(level, pos, -9999, false, true);
    }
    public static boolean createThaumatorium(ItemStack stack, Player player, Level level, BlockPos pos,
                                             Direction side) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        Block metal;
        Block crucible;
        Block thaumatorium;
        try {
            metal = BlocksTC.metalAlchemical.get();
            crucible = BlocksTC.crucible.get();
            thaumatorium = BlocksTC.thaumatorium.get();
        } catch (Exception e) {
            return false;
        }
        if (level.getBlockState(pos.above()) != metal.defaultBlockState()
                || level.getBlockState(pos.below()) != crucible.defaultBlockState()) {
            if (level.getBlockState(pos.below()) == metal.defaultBlockState()
                    && level.getBlockState(pos.offset(0, -2, 0)) == crucible.defaultBlockState()) {
                pos = pos.below();
            } else if (level.getBlockState(pos.above()) == metal.defaultBlockState()
                    && level.getBlockState(pos.offset(0, -2, 0)) == metal.defaultBlockState()) {
                pos = pos.above();
            } else {
                return false;
            }
        }
        if (!wand.consumeAllVis(stack, player,
                new AspectList().add(Aspect.FIRE, 50).add(Aspect.ORDER, 100).add(Aspect.WATER, 100), false, true)) {
            return false;
        }
        if (level.isClientSide) {
            return false;
        }
        BlockState base = withIfHas(thaumatorium.defaultBlockState(), IBlockFacingHorizontal.FACING, side);
        base = withIfHas(base, IBlockEnabled.ENABLED, true);
        BlockState top = withIfHas(BlocksTC.thaumatoriumTop.get().defaultBlockState(), IBlockFacingHorizontal.FACING, side);
        top = withIfHas(top, IBlockEnabled.ENABLED, false);
        BlockState oldBase = level.getBlockState(pos);
        BlockState oldTop = level.getBlockState(pos.above());
        level.setBlock(pos, base, 0);
        level.setBlock(pos.above(), top, 0);
        level.neighborChanged(pos, thaumatorium, pos);
        level.neighborChanged(pos.above(), thaumatorium, pos.above());
        level.sendBlockUpdated(pos, oldBase, base, 0);
        level.sendBlockUpdated(pos.above(), oldTop, top, 0);
        sendBamf(level, pos, -9999, true, true);
        sendBamf(level, pos.above(), -9999, true, true);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), WandSounds.wand(), SoundSource.PLAYERS, 1.0F,
                1.0F);
        return true;
    }
    public static boolean createInfernalFurnace(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        for (int xx = -2; xx <= 0; xx++) {
            for (int yy = -2; yy <= 0; yy++) {
                for (int zz = -2; zz <= 0; zz++) {
                    BlockPos origin = pos.offset(xx, yy, zz);
                    if (fitInfernalFurnace(level, origin) && wand.consumeAllVis(stack, player,
                            new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50), false, true)) {
                        if (!level.isClientSide) {
                            replaceInfernalFurnace(level, origin);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }
    public static boolean fitInfernalFurnace(Level level, BlockPos pos) {
        Block obsidian = Blocks.OBSIDIAN;
        Block brick = Blocks.NETHER_BRICKS;
        Block bars = Blocks.IRON_BARS;
        Block[][][] blueprint = {
                {{brick, obsidian, brick}, {obsidian, Blocks.BEDROCK, obsidian}, {brick, obsidian, brick}},
                {{brick, obsidian, brick}, {obsidian, Blocks.LAVA, obsidian}, {brick, obsidian, brick}},
                {{brick, obsidian, brick}, {obsidian, obsidian, obsidian}, {brick, obsidian, brick}}};
        boolean barsFound = false;
        for (int yy = 0; yy < 3; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockPos current = pos.offset(xx, -yy + 2, zz);
                    Block block = level.getBlockState(current).getBlock();
                    if (level.isEmptyBlock(current)) {
                        block = Blocks.BEDROCK;
                    }
                    if (block != blueprint[yy][xx][zz]) {
                        if (yy == 1 && !barsFound && block == bars && xx != zz && (xx == 1 || zz == 1)) {
                            barsFound = true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return barsFound;
    }
    public static void replaceInfernalFurnace(Level level, BlockPos pos) {
        int[][][] blueprint = {{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}}, {{0, 1, 0}, {1, 3, 1}, {0, 1, 0}},
                {{0, 1, 0}, {1, 4, 1}, {0, 1, 0}}};
        BlockPos center = pos.offset(1, 1, 1);
        Block furnace;
        try {
            furnace = BlocksTC.infernalFurnace.get();
        } catch (Exception e) {
            return;
        }
        for (Direction face : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(center.relative(face)).getBlock() == Blocks.IRON_BARS) {
                BlockState st = furnace.defaultBlockState();
                st = withIfHas(st, IBlockFacingHorizontal.FACING, face.getOpposite());
                setBlockSparkle(level, center, st);
                break;
            }
        }
        Block phBrick;
        Block phObsidian;
        try {
            phBrick = BlocksTC.placeholderNetherbrick.get();
            phObsidian = BlocksTC.placeholderObsidian.get();
        } catch (Exception e) {
            return;
        }
        for (int yy = 0; yy < 3; yy++) {
            for (int zz = 0; zz < 3; zz++) {
                for (int xx = 0; xx < 3; xx++) {
                    BlockPos current = pos.offset(xx, yy, zz);
                    if (level.getBlockState(current).getBlock() == Blocks.IRON_BARS) {
                        level.removeBlock(current, false);
                    } else if (blueprint[yy][xx][zz] == 0) {
                        setBlockSparkle(level, current, phBrick.defaultBlockState());
                    } else if (blueprint[yy][xx][zz] == 1) {
                        setBlockSparkle(level, current, phObsidian.defaultBlockState());
                    }
                }
            }
        }
        level.playSound(null, pos.getX() + 1.5D, pos.getY() + 1.5D, pos.getZ() + 1.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    private static boolean createOculus(ItemStack stack, Player player, Level level, BlockPos pos) {
        return arcaneresearch.common.lib.aura.EldritchOculusRitual.createOculus(stack, player, level, pos);
    }
    @SuppressWarnings("unused")
    private static EntitySpecialItem specialItemForThaumonomicon(Level level, BlockPos pos, ItemStack book) {
        return new EntitySpecialItem(level, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, book);
    }
    @SuppressWarnings("unused")
    private static net.minecraft.world.entity.EntityType<?> specialItemType() {
        try {
            return ModEntities.SPECIAL_ITEM.get();
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side,
                                  int event) {
        switch (event) {
            case 0:
                return createThaumonomicon(wand, player, level, pos);
            case 1:
                return createCrucible(wand, player, level, pos);
            case 2:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "INFERNALFURNACE")) {
                    return createInfernalFurnace(wand, player, level, pos);
                }
                break;
            case 3:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "INFUSION")) {
                    return createInfusionAltar(wand, player, level, pos);
                }
                break;
            case 4:
            case 5:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "THAUMATORIUM")) {
                    return createThaumatorium(wand, player, level, pos, side);
                }
                break;
            case 6:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "LEGACYOCULUS")
                        || WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "OCULUS")) {
                    return createOculus(wand, player, level, pos);
                }
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "MINDCLOCKWORK")) {
                    return createGolemPress(wand, player, level, pos);
                }
                break;
            case 11:
                if (WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "LEGACYJARNODE")
                        || WandResearchHelper.isResearchComplete(level, player.getGameProfile().getName(), "NODEJAR")) {
                    return arcaneresearch.common.lib.aura.NodeJarRitual.createNodeJar(wand, player, level, pos);
                }
                break;
            default:
                break;
        }
        return false;
    }
}
