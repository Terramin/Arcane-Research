package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.blocks.aura.BlockEnergizedNode;
import arcaneresearch.common.lib.aura.AuraFX;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.TileArcaneresearch;
import org.jetbrains.annotations.NotNull;

public class TransducerTile extends TileArcaneresearch {
    public int count = -1;
    public int status;

    public TransducerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.status = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TransducerTile tile) {
        if (level == null) return;

        if (tile.count == -1) {
            tile.checkStatus(level);
        }

        BlockPos below = pos.below();
        BlockEntity target = level.getBlockEntity(below);

        if (tile.status == 1 && tile.count >= 1000) {
            if (target instanceof AuraNodeTile node) {
                AspectList base = node.getAspectsBase().copy();
                NodeType type = node.getNodeType();
                NodeModifier mod = node.getNodeModifier();

                Block energized;
                try {
                    energized = AuraBlocks.ENERGIZED_NODE.get();
                } catch (Exception e) {
                    return;
                }

                level.setBlockAndUpdate(below, energized.defaultBlockState());

                BlockEntity fresh = level.getBlockEntity(below);
                if (fresh instanceof TileNodeEnergized en) {
                    en.setNodeModifier(mod);
                    en.setNodeType(type);
                    en.setId(node.getId());
                    en.setAspects(base);
                    en.setupNode();
                    en.syncAspectsToClients();
                }

                tile.checkStatus(level);
                tile.setChanged();
                level.blockEvent(pos, state.getBlock(), 10, 10);
                level.sendBlockUpdated(pos, state, state, 3);
                AuraFX.nodeSparkle(level, below, 0xFFFFFF);
                return;
            }
        }

        if (tile.status == 2 && tile.count <= 50) {
            if (target instanceof TileNodeEnergized en) {
                AspectList base = en.getAuraBase().copy();
                NodeType type = en.getNodeType();
                NodeModifier mod = en.getNodeModifier();

                Block nodeBlock;
                try {
                    nodeBlock = AuraBlocks.AURA_NODE.get();
                } catch (Exception e) {
                    return;
                }

                level.setBlockAndUpdate(below, nodeBlock.defaultBlockState());

                BlockEntity fresh = level.getBlockEntity(below);
                if (fresh instanceof AuraNodeTile node) {
                    node.setNodeModifier(mod);
                    node.setNodeType(type);
                    node.setId(en.getId());
                    node.setAspects(base);

                    Aspect[] snapshot = node.getAspects().getAspects();
                    for (Aspect a : snapshot) {
                        if (a != null) {
                            node.takeFromContainer(a, node.getAspects().getAmount(a));
                        }
                    }

                    node.setChanged();
                }

                tile.status = 0;
                tile.setChanged();
                level.blockEvent(pos, state.getBlock(), 10, 10);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        boolean powered = level.hasNeighborSignal(pos);

        // Thaumcraft never resets `status` when power is lost. The counter
        // simply winds down; if power returns before the node reverts, charging
        // resumes from the current count. Resetting status here would leave
        // the converter stuck in the decrementing branch forever.
        if (tile.status == 0 || !powered) {
            if (tile.count > 0) {
                tile.count--;
                tile.setChanged();
            }
        } else if (tile.count < 1000) {
            tile.count++;
            tile.setChanged();

            if (target instanceof AuraNodeTile nd) {
                AspectList al = nd.getAspects();
                Aspect[] all = al.getAspects();

                if (all.length > 0) {
                    Aspect pick = all[level.random.nextInt(all.length)];

                    if (pick != null) {
                        nd.takeFromContainer(pick, 1);

                        if (tile.count % 5 == 0 || nd.getAspects().visSize() == 0) {
                            nd.setChanged();
                            BlockState ndState = nd.getBlockState();
                            level.sendBlockUpdated(below, ndState, ndState, 3);
                        }
                    }
                }
            }

        }

        if (tile.count > 1000) {
            tile.count = 1000;
            tile.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TransducerTile tile) {
        if (level == null) return;

        boolean powered = level.hasNeighborSignal(pos);
        boolean withStabilizer = tile.hasStabilizer(level);
        if (tile.status == 0 || !powered) {
            if (tile.count > 0) {
                tile.count--;
                if (tile.count > 50) {
                    spawnClientBolts(level, pos, withStabilizer);
                }
            }
        } else if (tile.count < 1000) {
            tile.count++;
            // Thaumcraft keeps the arcs inside this branch only: once count
            // reaches 1000 the conversion is done and the bolts stop.
            if (tile.count > 50) {
                spawnClientBolts(level, pos, withStabilizer);
            }
        }

        if (tile.count > 1000) {
            tile.count = 1000;
        }
    }

    /** Port of Thaumcraft's ClientProxy.nodeBolt calls in TileNodeConverter. */
    static void spawnClientBolts(Level level, BlockPos pos, boolean withStabilizer) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        if (level.random.nextBoolean()) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.nodeBolt(
                    x + 0.25D + level.random.nextFloat() * 0.5D, y + 0.5D,
                    z + 0.25D + level.random.nextFloat() * 0.5D,
                    x + 0.5D, y - 0.5D, z + 0.5D, level.random.nextLong());
        }

        if (withStabilizer && level.random.nextBoolean()) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.nodeBolt(
                    x + 0.25D + level.random.nextFloat() * 0.5D, y - 1.5D,
                    z + 0.25D + level.random.nextFloat() * 0.5D,
                    x + 0.5D, y - 0.5D, z + 0.5D, level.random.nextLong());
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 10 && param == 10 && level != null && level.isClientSide) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.burst(
                    worldPosition.getX() + 0.5D, worldPosition.getY() - 0.5D,
                    worldPosition.getZ() + 0.5D, 1.0F);

            level.playLocalSound(
                    worldPosition.getX() + 0.5D, worldPosition.getY() - 0.5D,
                    worldPosition.getZ() + 0.5D,
                    arcaneresearch.init.ModSounds.CRAFT_FAIL.get(),
                    SoundSource.BLOCKS, 0.5F, 1.0F, false);
            return true;
        }

        return super.triggerEvent(id, param);
    }

    private boolean hasStabilizer(Level level) {
        if (level == null) return false;

        BlockPos stabPos = worldPosition.offset(0, -2, 0);

        return level.getBlockEntity(stabPos) instanceof StabilizerTile
                && !level.hasNeighborSignal(stabPos);
    }

    public void checkStatus(Level level) {
        if (level == null) return;

        if (this.count == -1) {
            this.count = 0;
        }

        BlockPos below = worldPosition.below();
        BlockEntity target = level.getBlockEntity(below);

        Block belowBlock = target != null
                ? target.getBlockState().getBlock()
                : level.getBlockState(below).getBlock();

        Block auraNode;
        Block energizedNode;
        try {
            auraNode = AuraBlocks.AURA_NODE.get();
            energizedNode = AuraBlocks.ENERGIZED_NODE.get();
        } catch (Exception e) {
            return;
        }

        boolean stabilizer = hasStabilizer(level);

        if (this.status == 2 && this.count > 50
                && (!stabilizer || belowBlock != energizedNode)) {

            BlockEnergizedNode.explodify(level, below);
            this.status = 0;
            this.count = 50;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        if (level.hasNeighborSignal(worldPosition)
                && belowBlock == auraNode
                && stabilizer
                && target instanceof AuraNodeTile) {

            this.status = 1;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        if (belowBlock == energizedNode && target instanceof TileNodeEnergized) {
            this.status = 2;
            this.count = 1000;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        this.status = 0;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("tcStatus", this.status);
        tag.putInt("tcCount", this.count);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("tcStatus", this.status);
        tag.putInt("tcCount", this.count);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.status = tag.getInt("tcStatus");
        this.count = tag.getInt("tcCount");
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        this.status = tag.getInt("tcStatus");
        this.count = tag.getInt("tcCount");
    }
}