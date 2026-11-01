package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class NodeConverterTile extends TileArcaneresearch {
    public int count = -1;
    public int status;
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
        if (level == null || tile.count <= 50) return;
        if (!level.random.nextBoolean()) return;

        double x = pos.getX() + 0.25D + level.random.nextFloat() * 0.5D;
        double z = pos.getZ() + 0.25D + level.random.nextFloat() * 0.5D;

        arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawLightningFlash(
                x, pos.getY() + 0.5D, z, 0.6F, 0.3F, 0.9F, 0.5F, 1.0F);
    }

    protected void serverTick() {
        if (level == null || level.isClientSide) return;

        if (this.count == -1) {
            checkStatus();
        }

        BlockPos belowPos = this.worldPosition.below();

        if (isStagingAiry(belowPos)) {
            if (this.stage > 0) this.stage--;

            if (this.stage <= 0) {
                finishStaging();
                checkStatus();
            } else {
                setChanged();
            }
        }

        if (this.status == 1 && this.count >= 1000 && !isStagingAiry(belowPos)) {
            BlockEntity below = level.getBlockEntity(belowPos);
            Block auraNode = AuraBlocks.AURA_NODE.get();

            if (below instanceof AuraNodeTile node && level.getBlockState(belowPos).is(auraNode)) {

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
        }

        if (this.status == 0) {
            if (this.count > 0) this.count--;
        } else if (this.count < 1000) {
            this.count++;
            drainBelow();
        }

        if (this.count > 1000) {
            this.count = 1000;
        }
    }

    protected void drainBelow() {
        if (level == null || level.isClientSide) return;

        BlockPos belowPos = this.worldPosition.below();
        BlockEntity target = level.getBlockEntity(belowPos);

        if (target == null) return;

        AspectList aspects;

        if (target instanceof TileNodeEnergized energized) {
            aspects = energized.getAspects();
        } else if (target instanceof AuraNodeTile node) {
            aspects = node.getAspects();
        } else {
            return;
        }

        Aspect[] present = aspects.getAspects();
        if (present.length == 0) return;

        Aspect pick = present[level.random.nextInt(present.length)];
        if (pick == null) return;

        boolean took;

        if (target instanceof TileNodeEnergized energized) {
            took = energized.takeFromContainer(pick, 1);
        } else {
            took = ((AuraNodeTile) target).takeFromContainer(pick, 1);
        }

        if (!took) return;

        if (this.count % 5 == 0 || aspects.getAspects().length == 0) {
            target.setChanged();
            BlockState state = level.getBlockState(belowPos);
            level.sendBlockUpdated(belowPos, state, state, 3);
        }
    }

    public void checkStatus() {
        if (level == null || level.isClientSide) return;

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
                && (!hasStabilizer()
                || !(below instanceof TileNodeEnergized)
                || !belowState.is(AuraBlocks.ENERGIZED_NODE.get()))) {

            explodify();
            this.status = 0;
            this.count = 50;
        } else if (belowState.is(AuraBlocks.AURA_NODE.get()) && below instanceof AuraNodeTile && hasStabilizer()) {

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
        if (level == null) return false;

        BlockState state = level.getBlockState(pos);

        return state.is(AuraBlocks.AIRY.get())
                && state.getValue(BlockAiry.TYPE) == BlockAiry.AiryType.STAGING;
    }

    protected void finishStaging() {
        convertStaging(true);
    }

    protected void restoreNode() {
        convertStaging(false);
    }

    private void convertStaging(boolean toEnergized) {
        if (level == null || level.isClientSide) return;

        BlockPos belowPos = this.worldPosition.below();

        if (!isStagingAiry(belowPos)) return;

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

        Block targetBlock = toEnergized
                ? AuraBlocks.ENERGIZED_NODE.get()
                : AuraBlocks.AURA_NODE.get();

        level.removeBlock(belowPos, false);
        level.setBlockAndUpdate(belowPos, targetBlock.defaultBlockState());

        BlockEntity fresh = level.getBlockEntity(belowPos);

        if (toEnergized) {
            if (fresh instanceof TileNodeEnergized newEnergized) {
                newEnergized.setNodeModifier(mod);
                newEnergized.setNodeType(type);
                newEnergized.setAspects(base);
                if (nid != null) newEnergized.setId(nid);
                newEnergized.setupNode();
            }
        } else {
            if (fresh instanceof AuraNodeTile node) {
                node.setNodeModifier(mod);
                node.setNodeType(type);
                node.setAspects(base);
                if (nid != null) node.setId(nid);
                node.setChanged();
            }
        }

        this.stage = 0;
    }

    protected boolean hasStabilizer() {
        if (level == null) return false;

        BlockPos stabPos = this.worldPosition.below(2);
        BlockEntity tile = level.getBlockEntity(stabPos);

        return tile instanceof StabilizerTile && !level.hasNeighborSignal(stabPos);
    }

    protected void explodify() {
        if (level == null || level.isClientSide) return;

        BlockPos belowPos = this.worldPosition.below();

        level.removeBlock(belowPos, false);
        level.explode(null,
                belowPos.getX() + 0.5D, belowPos.getY() + 0.5D, belowPos.getZ() + 0.5D,
                3.0F, Level.ExplosionInteraction.NONE);

        Block goo = arcaneresearch.init.ModBlocks.FLUX_GOO.get();
        Block gas = arcaneresearch.init.ModBlocks.FLUX_GAS.get();

        for (int a = 0; a < 50; a++) {
            int xx = belowPos.getX() + level.random.nextInt(8) - level.random.nextInt(8);
            int yy = belowPos.getY() + level.random.nextInt(8) - level.random.nextInt(8);
            int zz = belowPos.getZ() + level.random.nextInt(8) - level.random.nextInt(8);

            BlockPos scatter = new BlockPos(xx, yy, zz);

            if (!level.isEmptyBlock(scatter)) continue;

            Block target = yy < belowPos.getY() ? goo : gas;
            level.setBlock(scatter, target.defaultBlockState(), 3);
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 10 && param == 10 && level != null && level.isClientSide) {
            spawnBurstFX(level);
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnBurstFX(Level level) {
        if (level == null) return;

        arcaneresearch.client.fx.FXDispatcher.INSTANCE.burst(
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() - 0.5D,
                this.worldPosition.getZ() + 0.5D, 1.0F);

        level.playLocalSound(
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() - 0.5D,
                this.worldPosition.getZ() + 0.5D,
                arcaneresearch.init.ModSounds.CRAFT_FAIL.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F, false);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("status", this.status);
        tag.putInt("count", this.count);
        tag.putInt("stage", this.stage);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        this.status = tag.getInt("status");
        this.count = tag.getInt("count");
        this.stage = tag.getInt("stage");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("status", this.status);
        tag.putInt("count", this.count);
        tag.putInt("stage", this.stage);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.status = tag.getInt("status");
        this.count = tag.getInt("count");
        this.stage = tag.getInt("stage");
    }
}