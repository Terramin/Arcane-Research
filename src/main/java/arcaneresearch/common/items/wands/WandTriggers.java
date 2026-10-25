package arcaneresearch.common.items.wands;

import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.EldritchOculusRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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
import net.minecraftforge.network.PacketDistributor;

import static com.mojang.text2speech.Narrator.LOGGER;

public class WandTriggers implements IWandTriggerManager {
    private static boolean triggersRegistered = false;

    private static final DirectionProperty FACING_HORIZONTAL =
            DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public static synchronized void registerTriggers() {
        if (triggersRegistered) {
            return;
        }
        if (!AuraBlocks.ELDRITCH_ALTAR.isPresent()) {
            LOGGER.warn("ELDRITCH_ALTAR is not registered yet - skipping trigger registration");
            return;
        }

        try {
            WandTriggerRegistry.registerWandBlockTrigger(new WandTriggers(), 11,
                    Blocks.OBSIDIAN.defaultBlockState());
            WandTriggerRegistry.registerWandBlockTrigger(new WandTriggers(), 6,
                    AuraBlocks.ELDRITCH_ALTAR.get().defaultBlockState());
            triggersRegistered = true;
        } catch (Exception e) {
            LOGGER.error("Failed to register WandTriggers", e);
        }
    }

    private static <T extends Comparable<T>> BlockState withIfHas(BlockState state, Property<T> prop, T value) {
        if (state == null || value == null) {
            return state;
        }
        if (!state.hasProperty(prop)) {
            return state;
        }
        try {
            return state.setValue(prop, value);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("withIfHas: value {} is not valid for property '{}' on {}",
                    value, prop.getName(), state.getBlock());
            return state;
        }
    }

    private static void sendBamf(Level level, BlockPos pos, int color, boolean sound) {
        PacketHandler.sendToNear(new PacketFXBlockBamf(pos, color, sound, true, null),
                new PacketDistributor.TargetPoint(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32.0D, level.dimension()));
    }

    public static boolean createGolemPress(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }

        BlockState pistonTarget = Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP);
        BlockPos start = null;
        outer:
        for (int yy = -1; yy < 2; yy++) {
            for (int xx = -1; xx < 2; xx++) {
                for (int zz = -1; zz < 2; zz++) {
                    if (level.getBlockState(pos.offset(xx, yy, zz)) == pistonTarget) {
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
        if (!BlocksTC.tableStone.isPresent()) {
            return false;
        }

        Block tableStone = BlocksTC.tableStone.get();
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
        BlockPos anvilPos = start.relative(face).relative(perp);
        BlockPos cauldronPos = anvilPos.relative(perp.getClockWise(Direction.Axis.Y));
        BlockPos tablePos = start.relative(face);

        if (level.getBlockState(anvilPos).getBlock() != Blocks.ANVIL) {
            return false;
        }
        if (level.getBlockState(cauldronPos).getBlock() != Blocks.CAULDRON) {
            return false;
        }

        if (!BlocksTC.placeholderBars.isPresent()
                || !BlocksTC.placeholderAnvil.isPresent()
                || !BlocksTC.placeholderCauldron.isPresent()
                || !BlocksTC.placeholderTable.isPresent()
                || !BlocksTC.golemBuilder.isPresent()) {
            return false;
        }

        AspectList cost = new AspectList()
                .add(Aspect.FIRE, 50)
                .add(Aspect.ORDER, 50)
                .add(Aspect.AIR, 50);
        if (!wand.consumeAllVis(stack, player, cost, false, true)) {
            return false;
        }

        setBlockSparkle(level, start.above(), BlocksTC.placeholderBars.get().defaultBlockState());
        setBlockSparkle(level, anvilPos, BlocksTC.placeholderAnvil.get().defaultBlockState());
        setBlockSparkle(level, cauldronPos, BlocksTC.placeholderCauldron.get().defaultBlockState());
        setBlockSparkle(level, tablePos, BlocksTC.placeholderTable.get().defaultBlockState());

        BlockState builder = BlocksTC.golemBuilder.get().defaultBlockState();
        builder = withIfHas(builder, IBlockFacingHorizontal.FACING, face);
        setBlockSparkle(level, start, builder);

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                WandSounds.wand(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static boolean createThaumonomicon(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand) || wand.getFocus(stack) != null) {
            return false;
        }
        if (!ModItems.THAUMONOMICON.isPresent()) {
            return false;
        }

        ItemStack book = new ItemStack(ModItems.THAUMONOMICON.get());
        level.removeBlock(pos, false);
        EntitySpecialItem dropped = specialItemForThaumonomicon(level, pos, book);
        dropped.setDeltaMovement(0.0D, 0.0D, 0.0D);
        level.addFreshEntity(dropped);
        sendBamf(level, pos, -9999, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static boolean createCrucible(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (!BlocksTC.crucible.isPresent()) {
            return false;
        }

        Block crucible = BlocksTC.crucible.get();
        level.setBlockAndUpdate(pos, crucible.defaultBlockState());
        sendBamf(level, pos, -9999, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static boolean createInfusionAltar(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (!fitInfusionAltar(level, pos)) {
            return false;
        }

        AspectList cost = new AspectList()
                .add(Aspect.FIRE, 75)
                .add(Aspect.EARTH, 75)
                .add(Aspect.ORDER, 75)
                .add(Aspect.AIR, 75)
                .add(Aspect.ENTROPY, 75)
                .add(Aspect.WATER, 75);

        if (!wand.consumeAllVis(stack, player, cost, false, true)) {
            return false;
        }

        replaceInfusionAltar(level, pos);
        return true;
    }

    public static boolean fitInfusionAltar(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!BlocksTC.stoneArcane.isPresent()) {
            return false;
        }

        Block pillar = BlocksTC.stoneArcane.get();
        Block[][][] blueprint = {
                {{pillar, null, pillar}, {null, null, null}, {pillar, null, pillar}},
                {{pillar, null, pillar}, {null, null, null}, {pillar, null, pillar}}};

        BlockState first = level.getBlockState(pos.offset(-1, -2, -1));
        if (first != level.getBlockState(pos.offset(1, -2, -1))
                || first != level.getBlockState(pos.offset(1, -2, 1))
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
        if (level.isClientSide) {
            return;
        }
        if (!BlocksTC.pillarArcane.isPresent()) {
            return;
        }
        Block pillar = BlocksTC.pillarArcane.get();
        BlockState pillarBase = pillar.defaultBlockState();

        // Захват старого состояния pos (для sendBlockUpdated ниже, если понадобится)
        BlockState oldMatrixState = level.getBlockState(pos);

        setBlockSparkle(level, pos.offset(-1, -1, -1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(1, -1, -1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(1, -1, 1), Blocks.BEDROCK.defaultBlockState());
        setBlockSparkle(level, pos.offset(-1, -1, 1), Blocks.BEDROCK.defaultBlockState());

        setBlockSparkle(level, pos.offset(-1, -2, -1),
                withIfHas(pillarBase, FACING_HORIZONTAL, Direction.EAST));
        setBlockSparkle(level, pos.offset(1, -2, -1),
                withIfHas(pillarBase, FACING_HORIZONTAL, Direction.SOUTH));
        setBlockSparkle(level, pos.offset(1, -2, 1),
                withIfHas(pillarBase, FACING_HORIZONTAL, Direction.WEST));
        setBlockSparkle(level, pos.offset(-1, -2, 1),
                withIfHas(pillarBase, FACING_HORIZONTAL, Direction.NORTH));

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileInfusionMatrix matrix) {
            matrix.active = true;
            matrix.setChanged();
            level.sendBlockUpdated(pos, oldMatrixState, level.getBlockState(pos), 3);
        }

        sendBamf(level, pos, -9999, true);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void setBlockSparkle(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state);
        sendBamf(level, pos, -9999, false);
    }

    public static boolean createThaumatorium(ItemStack stack, Player player, Level level, BlockPos pos,
                                             Direction side) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        if (!BlocksTC.metalAlchemical.isPresent()
                || !BlocksTC.crucible.isPresent()
                || !BlocksTC.thaumatorium.isPresent()
                || !BlocksTC.thaumatoriumTop.isPresent()) {
            return false;
        }

        Block metal = BlocksTC.metalAlchemical.get();
        Block crucible = BlocksTC.crucible.get();
        Block thaumatorium = BlocksTC.thaumatorium.get();
        Block thaumatoriumTop = BlocksTC.thaumatoriumTop.get();

        BlockState metalState = metal.defaultBlockState();
        BlockState crucibleState = crucible.defaultBlockState();

        if (level.getBlockState(pos.above()) != metalState
                || level.getBlockState(pos.below()) != crucibleState) {
            if (level.getBlockState(pos.below()) == metalState
                    && level.getBlockState(pos.offset(0, -2, 0)) == crucibleState) {
                pos = pos.below();
            } else if (level.getBlockState(pos.above()) == metalState
                    && level.getBlockState(pos.offset(0, -2, 0)) == metalState) {
                pos = pos.above();
            } else {
                return false;
            }
        }

        AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.ORDER, 100).add(Aspect.WATER, 100);
        if (!wand.consumeAllVis(stack, player, cost, false, true)) {
            return false;
        }

        BlockState base = withIfHas(thaumatorium.defaultBlockState(), IBlockFacingHorizontal.FACING, side);
        base = withIfHas(base, IBlockEnabled.ENABLED, true);
        BlockState top = withIfHas(thaumatoriumTop.defaultBlockState(), IBlockFacingHorizontal.FACING, side);
        top = withIfHas(top, IBlockEnabled.ENABLED, false);

        BlockState oldBase = level.getBlockState(pos);
        BlockState oldTop = level.getBlockState(pos.above());
        level.setBlock(pos, base, 0);
        level.setBlock(pos.above(), top, 0);
        level.neighborChanged(pos, thaumatorium, pos);
        level.neighborChanged(pos.above(), thaumatoriumTop, pos.above());
        level.sendBlockUpdated(pos, oldBase, base, 0);
        level.sendBlockUpdated(pos.above(), oldTop, top, 0);
        sendBamf(level, pos, -9999, true);
        sendBamf(level, pos.above(), -9999, true);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static boolean createInfernalFurnace(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }

        AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50);

        for (int xx = -2; xx <= 0; xx++) {
            for (int yy = -2; yy <= 0; yy++) {
                for (int zz = -2; zz <= 0; zz++) {
                    BlockPos origin = pos.offset(xx, yy, zz);
                    if (fitInfernalFurnace(level, origin)
                            && wand.consumeAllVis(stack, player, cost, false, true)) {
                        replaceInfernalFurnace(level, origin);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean fitInfernalFurnace(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return false;
        }

        Block brick = Blocks.NETHER_BRICKS;
        Block obsidian = Blocks.OBSIDIAN;

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
                        if (yy == 1 && !barsFound && block == Blocks.IRON_BARS
                                && ((xx == 1) ^ (zz == 1))) {
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
        if (!BlocksTC.infernalFurnace.isPresent()
                || !BlocksTC.placeholderNetherbrick.isPresent()
                || !BlocksTC.placeholderObsidian.isPresent()) {
            return;
        }

        Block furnace = BlocksTC.infernalFurnace.get();
        Block phBrick = BlocksTC.placeholderNetherbrick.get();
        Block phObsidian = BlocksTC.placeholderObsidian.get();

        int[][][] blueprint = {{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}}, {{0, 1, 0}, {1, 3, 1}, {0, 1, 0}},
                {{0, 1, 0}, {1, 4, 1}, {0, 1, 0}}};
        BlockPos center = pos.offset(1, 1, 1);

        for (Direction face : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(center.relative(face)).getBlock() == Blocks.IRON_BARS) {
                BlockState st = furnace.defaultBlockState();
                st = withIfHas(st, IBlockFacingHorizontal.FACING, face.getOpposite());
                setBlockSparkle(level, center, st);
                break;
            }
        }

        BlockState phBrickState = phBrick.defaultBlockState();
        BlockState phObsidianState = phObsidian.defaultBlockState();

        for (int yy = 0; yy < 3; yy++) {
            for (int zz = 0; zz < 3; zz++) {
                for (int xx = 0; xx < 3; xx++) {
                    BlockPos current = pos.offset(xx, yy, zz);
                    if (level.getBlockState(current).getBlock() == Blocks.IRON_BARS) {
                        level.removeBlock(current, false);
                    } else if (blueprint[yy][xx][zz] == 0) {
                        setBlockSparkle(level, current, phBrickState);
                    } else if (blueprint[yy][xx][zz] == 1) {
                        setBlockSparkle(level, current, phObsidianState);
                    }
                }
            }
        }
        level.playSound(null, pos.getX() + 1.5D, pos.getY() + 1.5D, pos.getZ() + 1.5D, WandSounds.wand(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean createOculus(ItemStack stack, Player player, Level level, BlockPos pos) {
        return EldritchOculusRitual.createOculus(stack, player, level, pos);
    }

    @SuppressWarnings("unused")
    private static EntitySpecialItem specialItemForThaumonomicon(Level level, BlockPos pos, ItemStack book) {
        return new EntitySpecialItem(level, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, book);
    }

    @SuppressWarnings("unused")
    private static EntityType<?> specialItemType() {
        return ModEntities.SPECIAL_ITEM.isPresent() ? ModEntities.SPECIAL_ITEM.get() : null;
    }

    @Override
    public boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, int event) {
        String playerName = player.getGameProfile().getName();

        switch (event) {
            case 0:
                return createThaumonomicon(wand, player, level, pos);
            case 1:
                return createCrucible(wand, player, level, pos);
            case 2:
                if (WandResearchHelper.isResearchComplete(level, playerName, "INFERNALFURNACE")) {
                    return createInfernalFurnace(wand, player, level, pos);
                }
                break;
            case 3:
                if (WandResearchHelper.isResearchComplete(level, playerName, "INFUSION")) {
                    return createInfusionAltar(wand, player, level, pos);
                }
                break;
            case 4:
            case 5:
                if (WandResearchHelper.isResearchComplete(level, playerName, "THAUMATORIUM")) {
                    return createThaumatorium(wand, player, level, pos, side);
                }
                break;
            case 6:
                if (WandResearchHelper.isResearchComplete(level, playerName, "LEGACYOCULUS")
                        || WandResearchHelper.isResearchComplete(level, playerName, "OCULUS")) {
                    return createOculus(wand, player, level, pos);
                }
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                if (WandResearchHelper.isResearchComplete(level, playerName, "MINDCLOCKWORK")) {
                    return createGolemPress(wand, player, level, pos);
                }
                break;
            case 11:
                if (WandResearchHelper.isResearchComplete(level, playerName, "LEGACYJARNODE")
                        || WandResearchHelper.isResearchComplete(level, playerName, "NODEJAR")) {
                    return arcaneresearch.common.lib.aura.NodeJarRitual.createNodeJar(wand, player, level, pos);
                }
                break;
            default:
                break;
        }
        return false;
    }
}
