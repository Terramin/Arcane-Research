package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.AuraTotemPoleTile;
import arcaneresearch.common.tiles.aura.AuraTotemPullTile;
import arcaneresearch.common.tiles.aura.AuraTotemPushTile;
import arcaneresearch.common.tiles.aura.AuraTotemTile;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import arcaneresearch.common.tiles.aura.EldritchCapTile;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;
import arcaneresearch.common.tiles.aura.EldritchObeliskTile;
import arcaneresearch.common.tiles.aura.EldritchPortalTile;
import arcaneresearch.common.tiles.aura.EldritchTrapTile;
import arcaneresearch.common.tiles.aura.NodeConverterTile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.aura.*;

public class AuraBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Arcaneresearch.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AuraNodeTile>> AURA_NODE =
            BLOCK_ENTITIES.register("aura_node", () ->
                    new BlockEntityType<>(AuraBlockEntities::createNodeTile,
                            AuraBlocks.AURA_NODE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileJarNode>> JAR_NODE =
            BLOCK_ENTITIES.register("jar_node", () ->
                    new BlockEntityType<>(AuraBlockEntities::createJarTile,
                            AuraBlocks.JAR_NODE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StabilizerTile>> STABILIZER_TILE =
            BLOCK_ENTITIES.register("node_stabilizer", () ->
                    new BlockEntityType<>(AuraBlockEntities::createStabilizerTile,
                                    AuraBlocks.STABILIZER.get(), AuraBlocks.STABILIZER_ADVANCED.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileNodeEnergized>> ENERGIZED_NODE =
            BLOCK_ENTITIES.register("energized_node", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEnergizedTile,
                            AuraBlocks.ENERGIZED_NODE.get(), AuraBlocks.AIRY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransducerTile>> TRANSDUCER_TILE =
            BLOCK_ENTITIES.register("node_transducer", () ->
                    new BlockEntityType<>(AuraBlockEntities::createTransducerTile,
                            AuraBlocks.TRANSDUCER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemTile>> AURA_TOTEM =
            BLOCK_ENTITIES.register("aura_totem", () ->
                    new BlockEntityType<>(AuraBlockEntities::createAuraTotem,
                            AuraBlocks.AURA_TOTEM.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPullTile>> AURA_TOTEM_PULL =
            BLOCK_ENTITIES.register("aura_totem_pull", () ->
                    new BlockEntityType<>(AuraBlockEntities::createAuraTotemPull,
                            AuraBlocks.AURA_TOTEM.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPushTile>> AURA_TOTEM_PUSH =
            BLOCK_ENTITIES.register("aura_totem_push", () ->
                    new BlockEntityType<>(AuraBlockEntities::createAuraTotemPush,
                            AuraBlocks.AURA_TOTEM.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPoleTile>> AURA_TOTEM_POLE =
            BLOCK_ENTITIES.register("aura_totem_pole", () ->
                    new BlockEntityType<>(AuraBlockEntities::createAuraTotemPole,
                            AuraBlocks.AURA_TOTEM.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchAltarTile>> ELDRITCH_ALTAR =
            BLOCK_ENTITIES.register("eldritch_altar", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchAltar,
                            AuraBlocks.ELDRITCH_ALTAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.NodeConverterTile>> NODE_CONVERTER =
            BLOCK_ENTITIES.register("node_converter", () ->
                    new BlockEntityType<>(AuraBlockEntities::createNodeConverter,
                            AuraBlocks.NODE_CONVERTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchPortalTile>> ELDRITCH_PORTAL =
            BLOCK_ENTITIES.register("eldritch_portal", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchPortal,
                            AuraBlocks.ELDRITCH_PORTAL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchTrapTile>> ELDRITCH_TRAP =
            BLOCK_ENTITIES.register("eldritch_trap", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchTrap,
                            AuraBlocks.ELDRITCH_TRAP.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchCrabTile>> ELDRITCH_CRAB =
            BLOCK_ENTITIES.register("eldritch_crab", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchCrab,
                            AuraBlocks.ELDRITCH_CRAB.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchObeliskTile>> ELDRITCH_OBELISK =
            BLOCK_ENTITIES.register("eldritch_obelisk", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchObelisk,
                            AuraBlocks.ELDRITCH_OBELISK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<arcaneresearch.common.tiles.aura.EldritchCapTile>> ELDRITCH_CAP =
            BLOCK_ENTITIES.register("eldritch_cap", () ->
                    new BlockEntityType<>(AuraBlockEntities::createEldritchCap,
                            AuraBlocks.ELDRITCH_CAP.get()));

    private AuraBlockEntities() {
    }

    private static StabilizerTile createStabilizerTile(net.minecraft.core.BlockPos pos,
                                                       net.minecraft.world.level.block.state.BlockState state) {
        return new StabilizerTile(STABILIZER_TILE.get(), pos, state);
    }

    private static TileNodeEnergized createEnergizedTile(net.minecraft.core.BlockPos pos,
                                                         net.minecraft.world.level.block.state.BlockState state) {
        return new TileNodeEnergized(ENERGIZED_NODE.get(), pos, state);
    }

    private static TransducerTile createTransducerTile(net.minecraft.core.BlockPos pos,
                                                       net.minecraft.world.level.block.state.BlockState state) {
        return new TransducerTile(TRANSDUCER_TILE.get(), pos, state);
    }

    private static AuraNodeTile createNodeTile(net.minecraft.core.BlockPos pos,
                                               net.minecraft.world.level.block.state.BlockState state) {
        return new AuraNodeTile(AURA_NODE.get(), pos, state);
    }

    private static TileJarNode createJarTile(net.minecraft.core.BlockPos pos,
                                              net.minecraft.world.level.block.state.BlockState state) {
        return new TileJarNode(JAR_NODE.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.AuraTotemTile createAuraTotem(net.minecraft.core.BlockPos pos,
                                                                      net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.AuraTotemTile(AURA_TOTEM.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.AuraTotemPullTile createAuraTotemPull(net.minecraft.core.BlockPos pos,
                                                                              net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.AuraTotemPullTile(AURA_TOTEM_PULL.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.AuraTotemPushTile createAuraTotemPush(net.minecraft.core.BlockPos pos,
                                                                              net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.AuraTotemPushTile(AURA_TOTEM_PUSH.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.AuraTotemPoleTile createAuraTotemPole(net.minecraft.core.BlockPos pos,
                                                                              net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.AuraTotemPoleTile(AURA_TOTEM_POLE.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchAltarTile createEldritchAltar(net.minecraft.core.BlockPos pos,
                                                                              net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchAltarTile(ELDRITCH_ALTAR.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.NodeConverterTile createNodeConverter(net.minecraft.core.BlockPos pos,
                                                                               net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.NodeConverterTile(NODE_CONVERTER.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchPortalTile createEldritchPortal(net.minecraft.core.BlockPos pos,
                                                                                 net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchPortalTile(ELDRITCH_PORTAL.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchTrapTile createEldritchTrap(net.minecraft.core.BlockPos pos,
                                                                            net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchTrapTile(ELDRITCH_TRAP.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchCrabTile createEldritchCrab(net.minecraft.core.BlockPos pos,
                                                                            net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchCrabTile(ELDRITCH_CRAB.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchObeliskTile createEldritchObelisk(net.minecraft.core.BlockPos pos,
                                                                                 net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchObeliskTile(ELDRITCH_OBELISK.get(), pos, state);
    }

    private static arcaneresearch.common.tiles.aura.EldritchCapTile createEldritchCap(net.minecraft.core.BlockPos pos,
                                                                          net.minecraft.world.level.block.state.BlockState state) {
        return new arcaneresearch.common.tiles.aura.EldritchCapTile(ELDRITCH_CAP.get(), pos, state);
    }
}
