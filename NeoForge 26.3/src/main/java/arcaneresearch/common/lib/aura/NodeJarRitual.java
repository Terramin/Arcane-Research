package arcaneresearch.common.lib.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.tiles.aura.TileJarNode;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 common/lib/aura/NodeJarRitual.java (176 lines).
// Changes: level.isClientSide field -> isClientSide() (kept); level.random -> getRandom();
// Forge PacketDistributor.TargetPoint -> PacketHandler.sendToNear(packet, level, x, y, z, range).
public class NodeJarRitual {
    private NodeJarRitual() {
    }

    public static boolean createNodeJar(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        for (int xx = -2; xx <= 0; xx++) {
            for (int yy = -3; yy <= 0; yy++) {
                for (int zz = -2; zz <= 0; zz++) {
                    BlockPos origin = pos.offset(xx, yy, zz);
                    AspectList cost = new AspectList().add(Aspect.FIRE, 70).add(Aspect.EARTH, 70)
                            .add(Aspect.ORDER, 70).add(Aspect.AIR, 70).add(Aspect.ENTROPY, 70)
                            .add(Aspect.WATER, 70);
                    if (fitNodeJar(level, origin)
                            && wand.consumeAllVis(stack, player, cost, false, true)) {
                        if (!level.isClientSide()) {
                            replaceNodeJar(level, origin);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static boolean fitNodeJar(Level level, BlockPos origin) {

        int[][][] blueprint = {
                {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
                {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
                {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
                {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    BlockPos current = origin.offset(xx, 2 - yy, zz);
                    BlockState state = level.getBlockState(current);
                    if (blueprint[yy][xx][zz] == 1
                            && !state.is(BlockTags.WOODEN_SLABS)) {
                        return false;
                    }
                    if (blueprint[yy][xx][zz] == 2 && !isRitualGlass(state)) {
                        return false;
                    }
                    if (blueprint[yy][xx][zz] == 3) {
                        BlockEntity tile = level.getBlockEntity(current);
                        if (tile == null || !(tile instanceof INode) || tile instanceof TileJarNode) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void replaceNodeJar(Level level, BlockPos origin) {
        // Ритуал банки с узлом: кокон (плиты + стекло) расходуется,
        // в центре остаётся только банка с узлом. Поэтому сначала снимаем
        // данные ноды, затем с партиклами разбираем клетку и ставим банку.
        int[][][] blueprint = {
                {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
                {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
                {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
                {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};
        BlockPos center = origin.offset(1, 0, 1);
        BlockEntity centerTile = level.getBlockEntity(center);
        if (!(centerTile instanceof INode node)) {
            return;
        }
        AspectList na = node.getAspects().copy();
        NodeType nt = node.getNodeType();
        int nm = -1;
        if (node.getNodeModifier() != null) {
            nm = node.getNodeModifier().ordinal();
        }
        if (level.getRandom().nextFloat() < 0.75F) {
            if (node.getNodeModifier() == null) {
                nm = NodeModifier.PALE.ordinal();
            } else if (node.getNodeModifier() == NodeModifier.BRIGHT) {
                nm = -1;
            } else if (node.getNodeModifier() == NodeModifier.PALE) {
                nm = NodeModifier.FADING.ordinal();
            }
        }
        String nid = node.getId();
        Block jarBlock;
        try {
            jarBlock = AuraBlocks.JAR_NODE.get();
        } catch (Exception e) {
            return;
        }
        // Разбираем мультиблок с анимацией Bamf на каждом блоке кокона,
        // как это делают остальные жезловые ритуалы (setBlockSparkle).
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    int code = blueprint[yy][xx][zz];
                    if (code == 3) {
                        continue;
                    }
                    BlockPos current = origin.offset(xx, 2 - yy, zz);
                    sendBamf(level, current, -9999, false, true);
                    level.removeBlock(current, false);
                }
            }
        }
        node.setAspects(new AspectList());
        level.removeBlock(center, false);
        level.setBlockAndUpdate(center, jarBlock.defaultBlockState());
        BlockEntity tile = level.getBlockEntity(center);
        if (tile instanceof TileJarNode jar) {
            jar.setAspects(na);
            if (nm >= 0) {
                jar.setNodeModifier(NodeModifier.values()[nm]);
            } else {
                jar.setNodeModifier(null);
            }
            jar.setNodeType(nt);
            jar.setId(nid);
            jar.setAnimate(System.currentTimeMillis() + 1000L);
            // setAnimate уже делает syncTile(true), но дублируем sendBlockUpdated
            // как в TC4 (block event 9) чтобы гарантировать ререндер.
            jar.syncTile(true);
        }
        sendBamf(level, center, -9999, true, true);
        level.sendBlockUpdated(center, jarBlock.defaultBlockState(),
                level.getBlockState(center), 3);
        level.playSound(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D,
                WandSounds.wand(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean isRitualGlass(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.GLASS) {
            return true;
        }
        // любое ванильное стекло (цветное / тонированное) тоже подходит
        return block instanceof net.minecraft.world.level.block.StainedGlassBlock
                || block instanceof net.minecraft.world.level.block.TintedGlassBlock;
    }

    private static void sendBamf(Level level, BlockPos pos, int color, boolean sound, boolean flair) {
        try {
            // NEOFORGE_PORT: Forge PacketDistributor.TargetPoint removed ->
            // PacketHandler.sendToNear(packet, level, x, y, z, range) (same 32-block radius).
            PacketHandler.sendToNear(new PacketFXBlockBamf(pos, color, sound, flair, null),
                    level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32.0D);
        } catch (Exception ignored) {
        }
    }
}
