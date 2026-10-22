package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.blocks.aura.BlockAiry;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.tiles.aura.StabilizerTile;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

 





 
public class NodeConverterTile extends TileArcaneresearch {
    public int count = -1;
    public int status = 0;
    public int stage = 0;

    public NodeConverterTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
        this.status = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NodeConverterTile tile) {
        tile.serverTick();
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, NodeConverterTile tile) {
        if (tile.count > 50 && level.getRandom().nextBoolean()) {
            double x = pos.getX() + 0.25D + level.getRandom().nextFloat() * 0.5D;
            double z = pos.getZ() + 0.25D + level.getRandom().nextFloat() * 0.5D;
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawLightningFlash(
                    x, pos.getY() + 0.5D, z, 0.6F, 0.3F, 0.9F, 0.5F, 1.0F);
        }
    }

    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (this.count == -1) {
            checkStatus();
        }
        BlockPos belowPos = this.worldPosition.below();
        if (isStagingAiry(belowPos)) {
            if (this.stage > 0) {
                this.stage--;
            }
            if (this.stage <= 0) {
                finishStaging();
                checkStatus();
            }
            setChanged();
        }
        BlockEntity below = level.getBlockEntity(belowPos);
        if (this.status == 1 && this.count >= 1000 && !isStagingAiry(belowPos)) {
            if (below instanceof AuraNodeTile node && !(below instanceof TileNodeEnergized)
                    && level.getBlockState(belowPos).is(AuraBlocks.AURA_NODE.get())) {
                AspectList base = node.getAspects().copy();
                NodeType type = node.getNodeType();
                NodeModifier mod = node.getNodeModifier();
                String nid = node.getId();
                level.removeBlock(belowPos, false);
                level.setBlockAndUpdate(belowPos,
                        AuraBlocks.AIRY.get().defaultBlockState()
                                .setValue(BlockAiry.TYPE, BlockAiry.AiryType.STAGING));
                BlockEntity fresh = level.getBlockEntity(belowPos);
                if (fresh instanceof TileNodeEnergized staging) {
                    staging.setNodeModifier(mod);
                    staging.setNodeType(type);
                    staging.setAspects(base);
                    staging.setId(nid);
                    staging.setupNode();
                }
                this.stage = 60;
            }
            checkStatus();
            setChanged();
            level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        if (this.status == 0) {
            if (this.count > 0) {
                this.count--;
            }
        } else if (this.count < 1000) {
            this.count++;
            drainBelow();
        }
        if (this.count > 1000) {
            this.count = 1000;
        }
    }

    protected void drainBelow() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockEntity target = level.getBlockEntity(this.worldPosition.below());
        Aspect[] present = null;
        if (target instanceof TileNodeEnergized energized) {
            present = energized.getAspects().getAspects();
        } else if (target instanceof AuraNodeTile node) {
            present = node.getAspects().getAspects();
        }
        if (present == null || present.length == 0) {
            return;
        }
        Aspect pick = present[level.getRandom().nextInt(present.length)];
        boolean took;
        if (target instanceof TileNodeEnergized energized) {
            took = energized.takeFromContainer(pick, 1);
        } else {
            took = ((AuraNodeTile) target).takeFromContainer(pick, 1);
        }
        if (!took) {
            return;
        }
        Aspect[] rest = target instanceof TileNodeEnergized energized
                ? energized.getAspects().getAspects()
                : ((AuraNodeTile) target).getAspects().getAspects();
        if (this.count % 5 == 0 || rest.length == 0) {
            BlockPos belowPos = this.worldPosition.below();
            target.setChanged();
            level.sendBlockUpdated(belowPos, level.getBlockState(belowPos),
                    level.getBlockState(belowPos), 3);
        }
    }

    public void checkStatus() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (this.count == -1) {
            this.count = 0;
        }
        BlockPos belowPos = this.worldPosition.below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockEntity below = level.getBlockEntity(belowPos);
        if (isStagingAiry(belowPos)) {
            if (hasStabilizer()) {
                this.status = 1;
            } else {
                restoreNode();
                this.status = 0;
            }
        } else if (this.status == 2 && this.count > 50
                && (!hasStabilizer() || !(below instanceof TileNodeEnergized)
                || !belowState.is(AuraBlocks.ENERGIZED_NODE.get()))) {
            explodify();
            this.status = 0;
            this.count = 50;
        } else if (belowState.is(AuraBlocks.AURA_NODE.get())
                && below instanceof AuraNodeTile && !(below instanceof TileNodeEnergized)
                && hasStabilizer()) {
            this.status = 1;
        } else if (belowState.is(AuraBlocks.ENERGIZED_NODE.get())
                && below instanceof TileNodeEnergized) {
            this.status = 2;
            this.count = 1000;
        } else {
            this.status = 0;
        }
        setChanged();
        level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
    }

    protected boolean isStagingAiry(BlockPos pos) {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(AuraBlocks.AIRY.get())
                && state.getValue(BlockAiry.TYPE) == BlockAiry.AiryType.STAGING;
    }

    protected void finishStaging() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos belowPos = this.worldPosition.below();
        if (!isStagingAiry(belowPos)) {
            return;
        }
        BlockEntity staging = level.getBlockEntity(belowPos);
        AspectList base = new AspectList();
        NodeType type = NodeType.NORMAL;
        NodeModifier mod = null;
        String nid = null;
        if (staging instanceof TileNodeEnergized energized) {
            base = energized.getAuraBase().copy();
            type = energized.getNodeType();
            mod = energized.getNodeModifier();
            nid = energized.getId();
        }
        level.removeBlock(belowPos, false);
        level.setBlockAndUpdate(belowPos, AuraBlocks.ENERGIZED_NODE.get().defaultBlockState());
        BlockEntity fresh = level.getBlockEntity(belowPos);
        if (fresh instanceof TileNodeEnergized energized) {
            energized.setNodeModifier(mod);
            energized.setNodeType(type);
            energized.setAspects(base);
            if (nid != null) {
                energized.setId(nid);
            }
            energized.setupNode();
        }
        this.stage = 0;
    }

    protected void restoreNode() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos belowPos = this.worldPosition.below();
        if (!isStagingAiry(belowPos)) {
            return;
        }
        BlockEntity staging = level.getBlockEntity(belowPos);
        AspectList base = new AspectList();
        NodeType type = NodeType.NORMAL;
        NodeModifier mod = null;
        String nid = null;
        if (staging instanceof TileNodeEnergized energized) {
            base = energized.getAuraBase().copy();
            type = energized.getNodeType();
            mod = energized.getNodeModifier();
            nid = energized.getId();
        }
        level.removeBlock(belowPos, false);
        level.setBlockAndUpdate(belowPos, AuraBlocks.AURA_NODE.get().defaultBlockState());
        BlockEntity fresh = level.getBlockEntity(belowPos);
        if (fresh instanceof AuraNodeTile node) {
            node.setNodeModifier(mod);
            node.setNodeType(type);
            node.setAspects(base);
            if (nid != null) {
                node.setId(nid);
            }
            node.setChanged();
        }
        this.stage = 0;
    }

    protected boolean hasStabilizer() {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        
        BlockPos stabPos = this.worldPosition.below(2);
        BlockEntity tile = level.getBlockEntity(stabPos);
        return tile instanceof StabilizerTile && !level.hasNeighborSignal(stabPos);
    }

    protected void explodify() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos belowPos = this.worldPosition.below();
        level.removeBlock(belowPos, false);
        level.explode((net.minecraft.world.entity.Entity) null, belowPos.getX() + 0.5D, belowPos.getY() + 0.5D, belowPos.getZ() + 0.5D,
                3.0F, Level.ExplosionInteraction.NONE);
        for (int a = 0; a < 50; a++) {
            int xx = belowPos.getX() + level.getRandom().nextInt(8) - level.getRandom().nextInt(8);
            int yy = belowPos.getY() + level.getRandom().nextInt(8) - level.getRandom().nextInt(8);
            int zz = belowPos.getZ() + level.getRandom().nextInt(8) - level.getRandom().nextInt(8);
            BlockPos scatter = new BlockPos(xx, yy, zz);
            if (!level.isEmptyBlock(scatter)) {
                continue;
            }
            if (yy < belowPos.getY()) {
                level.setBlock(scatter,
                        arcaneresearch.init.ModBlocks.FLUX_GOO.get().defaultBlockState(), 3);
            } else {
                level.setBlock(scatter,
                        arcaneresearch.init.ModBlocks.FLUX_GAS.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        Level level = getLevel();
        if (id == 10 && param == 10 && level != null && level.isClientSide()) {
            spawnBurstFX(level);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnBurstFX(Level level) {
        arcaneresearch.client.fx.FXDispatcher.INSTANCE.burst(
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() - 0.5D,
                this.worldPosition.getZ() + 0.5D, 1.0F);
        level.playLocalSound(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() - 0.5D,
                this.worldPosition.getZ() + 0.5D,
                arcaneresearch.init.ModSounds.CRAFT_FAIL.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F, false);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putInt("status", this.status);
        tag.putInt("count", this.count);
        tag.putInt("stage", this.stage);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        this.status = tag.getIntOr("status", 0);
        this.count = tag.getIntOr("count", 0);
        this.stage = tag.getIntOr("stage", 0);
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("status", this.status);
        tag.putInt("count", this.count);
        tag.putInt("stage", this.stage);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.status = tag.getIntOr("status", 0);
        this.count = tag.getIntOr("count", 0);
        this.stage = tag.getIntOr("stage", 0);
    }
}
