package arcaneresearch.common.lib.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.tiles.aura.TileJarNode;
import org.jetbrains.annotations.NotNull;

public class NodeJarRitual {
    private static final int[][][] BLUEPRINT = {
            {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 3, 2}, {2, 2, 2}},
            {{2, 2, 2}, {2, 2, 2}, {2, 2, 2}}};

    private NodeJarRitual() {
    }

    public static boolean createNodeJar(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (stack == null || stack.isEmpty() || player == null || level == null || pos == null) {
            return false;
        }
        if (!(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        for (int xx = -2; xx <= 0; xx++) {
            for (int yy = -3; yy <= 0; yy++) {
                for (int zz = -2; zz <= 0; zz++) {
                    BlockPos origin = pos.offset(xx, yy, zz);
                    if (!fitNodeJar(level, origin)) {
                        continue;
                    }
                    AspectList cost = new AspectList().add(Aspect.FIRE, 70).add(Aspect.EARTH, 70)
                            .add(Aspect.ORDER, 70).add(Aspect.AIR, 70).add(Aspect.ENTROPY, 70)
                            .add(Aspect.WATER, 70);
                    if (wand.consumeAllVis(stack, player, cost, false, true)) {
                        if (!level.isClientSide) {
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

    public static boolean fitNodeJar(@NotNull Level level, @NotNull BlockPos origin) {
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    int code = BLUEPRINT[yy][xx][zz];
                    BlockPos current = origin.offset(xx, 2 - yy, zz);
                    BlockState state = level.getBlockState(current);
                    if (code == 1 && !state.is(BlockTags.WOODEN_SLABS)) {
                        return false;
                    }
                    if (code == 2 && !isRitualGlass(state)) {
                        return false;
                    }
                    if (code == 3) {
                        BlockEntity tile = level.getBlockEntity(current);
                        if (!(tile instanceof INode) || tile instanceof TileJarNode) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void replaceNodeJar(Level level, BlockPos origin) {
        if (level == null || origin == null) {
            return;
        }
        BlockPos center = origin.offset(1, 0, 1);
        BlockEntity centerTile = level.getBlockEntity(center);
        if (!(centerTile instanceof INode node)) {
            return;
        }
        AspectList nodeAspects = node.getAspects();
        AspectList na = nodeAspects != null ? nodeAspects.copy() : new AspectList();
        NodeType nt = node.getNodeType();
        NodeModifier currentMod = node.getNodeModifier();
        int nm = currentMod != null ? currentMod.ordinal() : -1;
        if (level.random.nextFloat() < 0.75F) {
            if (currentMod == null) {
                nm = NodeModifier.PALE.ordinal();
            } else if (currentMod == NodeModifier.BRIGHT) {
                nm = -1;
            } else if (currentMod == NodeModifier.PALE) {
                nm = NodeModifier.FADING.ordinal();
            }
        }
        String nid = node.getId();
        if (AuraBlocks.JAR_NODE == null) {
            return;
        }
        Block jarBlock = AuraBlocks.JAR_NODE.get();
        for (int yy = 0; yy < 4; yy++) {
            for (int xx = 0; xx < 3; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    if (BLUEPRINT[yy][xx][zz] == 3) {
                        continue;
                    }
                    BlockPos current = origin.offset(xx, 2 - yy, zz);
                    sendBamf(level, current, false);
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
            jar.setNodeModifier(nm >= 0 ? NodeModifier.values()[nm] : null);
            if (nt != null) {
                jar.setNodeType(nt);
            }
            if (nid != null) {
                jar.setId(nid);
            }
            jar.setAnimate(System.currentTimeMillis() + 1000L);
            jar.syncTile(true);
        }
        sendBamf(level, center, true);
        level.sendBlockUpdated(center, jarBlock.defaultBlockState(),
                level.getBlockState(center), 3);
        SoundEvent sound = WandSounds.wand();
        level.playSound(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D,
                sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean isRitualGlass(BlockState state) {
        if (state == null) {
            return false;
        }
        Block block = state.getBlock();
        if (block == Blocks.GLASS) {
            return true;
        }
        return block instanceof StainedGlassBlock || block instanceof TintedGlassBlock;
    }

    private static void sendBamf(Level level, BlockPos pos, boolean sound) {
        if (level == null || pos == null) {
            return;
        }
        try {
            PacketHandler.sendToNear(new PacketFXBlockBamf(pos, -9999, sound, true, null),
                    new PacketDistributor.TargetPoint(
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32.0D,
                            level.dimension()));
        } catch (Exception ignored) {
        }
    }
}