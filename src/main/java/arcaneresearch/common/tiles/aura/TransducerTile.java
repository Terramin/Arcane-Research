package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.AuraFX;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.TileArcaneresearch;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TransducerTile extends TileArcaneresearch {
    public int count = -1;
    public int status = 0;

    public TransducerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.status = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TransducerTile tile) {
        if (tile.count == -1) {
            tile.checkStatus(level);
        }
        BlockPos below = pos.below();
        if (tile.status == 1 && tile.count >= 1000) {
            BlockEntity target = level.getBlockEntity(below);
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
                }
                tile.checkStatus(level);
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                AuraFX.nodeSparkle(level, below, 0xFFFFFF);
            }
        }
        if (tile.status == 2 && tile.count <= 50) {
            BlockEntity target = level.getBlockEntity(below);
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
                    for (Aspect a : node.getAspects().getAspects()) {
                        node.takeFromContainer(a, node.getAspects().getAmount(a));
                    }
                    node.setChanged();
                }
                tile.status = 0;
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (tile.status == 0 || !powered) {
            if (tile.count > 0) {
                tile.count--;
                tile.setChanged();
            }
        } else if (tile.count < 1000) {
            tile.count++;
            tile.setChanged();
            BlockEntity target = level.getBlockEntity(below);
            if (target instanceof AuraNodeTile nd) {
                AspectList al = nd.getAspects();
                if (al.getAspects().length > 0) {
                    Aspect[] all = al.getAspects();
                    Aspect pick = all[level.getRandom().nextInt(all.length)];
                    if (pick != null) {
                        nd.takeFromContainer(pick, 1);
                        if (tile.count % 5 == 0 || nd.getAspects().visSize() == 0) {
                            nd.setChanged();
                            level.sendBlockUpdated(below, nd.getBlockState(), nd.getBlockState(), 3);
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

        if (tile.count <= 50) {
            return;
        }
        if (!level.getRandom().nextBoolean()) {
            return;
        }

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        nodeBoltFx(level,
                x + 0.25D + level.getRandom().nextFloat() * 0.5D, y + 0.5D, z + 0.25D + level.getRandom().nextFloat() * 0.5D,
                x + 0.5D, y - 0.5D, z + 0.5D);
        if (level.getRandom().nextBoolean() && tile.hasStabilizer(level)) {
            nodeBoltFx(level,
                    x + 0.25D + level.getRandom().nextFloat() * 0.5D, y - 1.5D, z + 0.25D + level.getRandom().nextFloat() * 0.5D,
                    x + 0.5D, y - 0.5D, z + 0.5D);
        }
    }

    public static void nodeBoltFx(Level level, double x1, double y1, double z1,
                                  double x2, double y2, double z2) {
        int segments = 8;
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double jitter = i == 0 || i == segments ? 0.0D : 0.18D;
            double px = x1 + (x2 - x1) * t + (level.getRandom().nextDouble() - 0.5D) * jitter;
            double py = y1 + (y2 - y1) * t + (level.getRandom().nextDouble() - 0.5D) * jitter;
            double pz = z1 + (z2 - z1) * t + (level.getRandom().nextDouble() - 0.5D) * jitter;
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz,
                        2, 0.02D, 0.02D, 0.02D, 0.1D);
            } else {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

     


 
    private boolean hasStabilizer(Level level) {
        try {
            BlockPos stabPos = worldPosition.offset(0, -2, 0);
            return level.getBlockEntity(stabPos) instanceof StabilizerTile
                    && !level.hasNeighborSignal(stabPos);
        } catch (Exception e) {
            return false;
        }
    }

    public void checkStatus(Level level) {
        if (this.count == -1) {
            this.count = 0;
        }
        BlockPos below = worldPosition.below();
        BlockEntity target = level.getBlockEntity(below);
        Block belowBlock = level.getBlockState(below).getBlock();
        Block auraNode;
        Block energizedNode;
        try {
            auraNode = AuraBlocks.AURA_NODE.get();
            energizedNode = AuraBlocks.ENERGIZED_NODE.get();
        } catch (Exception e) {
            return;
        }
        if (this.status == 2 && this.count > 50
                && (!hasStabilizer(level) || belowBlock != energizedNode)) {

            level.removeBlock(below, false);
            level.explode((net.minecraft.world.entity.Entity) null, below.getX() + 0.5D, below.getY() + 0.5D, below.getZ() + 0.5D,
                    3.0F, Level.ExplosionInteraction.BLOCK);
            this.status = 0;
            this.count = 50;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        } else if (level.hasNeighborSignal(worldPosition) && belowBlock == auraNode && hasStabilizer(level)
                && target instanceof AuraNodeTile) {
            this.status = 1;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        } else if (belowBlock == energizedNode && target instanceof TileNodeEnergized) {
            this.status = 2;
            this.count = 1000;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        } else {
            this.status = 0;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("tcStatus", this.status);
        tag.putInt("tcCount", this.count);
    }

      
    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putInt("tcStatus", this.status);
        tag.putInt("tcCount", this.count);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        try {
            this.status = tag.getIntOr("tcStatus", 0);
            this.count = tag.getIntOr("tcCount", 0);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        try {
            this.status = tag.getIntOr("tcStatus", 0);
            this.count = tag.getIntOr("tcCount", 0);
        } catch (Exception ignored) {
        }
    }
}
