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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.aura.*;
public class AuraBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<BlockEntityType<AuraNodeTile>> AURA_NODE =
            BLOCK_ENTITIES.register("aura_node", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createNodeTile,
                            AuraBlocks.AURA_NODE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileJarNode>> JAR_NODE =
            BLOCK_ENTITIES.register("jar_node", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createJarTile,
                            AuraBlocks.JAR_NODE.get()).build(null));
    public static final RegistryObject<BlockEntityType<StabilizerTile>> STABILIZER_TILE =
            BLOCK_ENTITIES.register("node_stabilizer", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createStabilizerTile,
                                    AuraBlocks.STABILIZER.get(), AuraBlocks.STABILIZER_ADVANCED.get())
                            .build(null));
    public static final RegistryObject<BlockEntityType<TileNodeEnergized>> ENERGIZED_NODE =
            BLOCK_ENTITIES.register("energized_node", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEnergizedTile,
                            AuraBlocks.ENERGIZED_NODE.get(), AuraBlocks.AIRY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TransducerTile>> TRANSDUCER_TILE =
            BLOCK_ENTITIES.register("node_transducer", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createTransducerTile,
                            AuraBlocks.TRANSDUCER.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemTile>> AURA_TOTEM =
            BLOCK_ENTITIES.register("aura_totem", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createAuraTotem,
                            AuraBlocks.AURA_TOTEM.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPullTile>> AURA_TOTEM_PULL =
            BLOCK_ENTITIES.register("aura_totem_pull", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createAuraTotemPull,
                            AuraBlocks.AURA_TOTEM.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPushTile>> AURA_TOTEM_PUSH =
            BLOCK_ENTITIES.register("aura_totem_push", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createAuraTotemPush,
                            AuraBlocks.AURA_TOTEM.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.AuraTotemPoleTile>> AURA_TOTEM_POLE =
            BLOCK_ENTITIES.register("aura_totem_pole", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createAuraTotemPole,
                            AuraBlocks.AURA_TOTEM.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchAltarTile>> ELDRITCH_ALTAR =
            BLOCK_ENTITIES.register("eldritch_altar", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchAltar,
                            AuraBlocks.ELDRITCH_ALTAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.NodeConverterTile>> NODE_CONVERTER =
            BLOCK_ENTITIES.register("node_converter", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createNodeConverter,
                            AuraBlocks.NODE_CONVERTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchPortalTile>> ELDRITCH_PORTAL =
            BLOCK_ENTITIES.register("eldritch_portal", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchPortal,
                            AuraBlocks.ELDRITCH_PORTAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchTrapTile>> ELDRITCH_TRAP =
            BLOCK_ENTITIES.register("eldritch_trap", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchTrap,
                            AuraBlocks.ELDRITCH_TRAP.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchCrabTile>> ELDRITCH_CRAB =
            BLOCK_ENTITIES.register("eldritch_crab", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchCrab,
                            AuraBlocks.ELDRITCH_CRAB.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchObeliskTile>> ELDRITCH_OBELISK =
            BLOCK_ENTITIES.register("eldritch_obelisk", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchObelisk,
                            AuraBlocks.ELDRITCH_OBELISK.get()).build(null));
    public static final RegistryObject<BlockEntityType<arcaneresearch.common.tiles.aura.EldritchCapTile>> ELDRITCH_CAP =
            BLOCK_ENTITIES.register("eldritch_cap", () ->
                    BlockEntityType.Builder.of(AuraBlockEntities::createEldritchCap,
                            AuraBlocks.ELDRITCH_CAP.get()).build(null));
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
