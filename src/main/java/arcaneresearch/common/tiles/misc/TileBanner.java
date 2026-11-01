package arcaneresearch.common.tiles.misc;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.basic.BlockBannerTC;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileBanner extends BlockEntity {

    private byte facing = 0;
    private Aspect aspect = null;
    private boolean onWall = false;

    @Nullable
    private AABB renderBoundingBox;

    public TileBanner(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANNER.get(), pos, state);
    }

    public byte getBannerFacing() {
        return facing;
    }

    public void setBannerFacing(byte face) {
        if (facing != face) {
            facing = face;
            setChanged();
        }
    }

    public boolean getWall() {
        return onWall;
    }

    public void setWall(boolean b) {
        if (onWall != b) {
            onWall = b;
            setChanged();
        }
    }

    public Aspect getAspect() {
        return aspect;
    }

    public void setAspect(Aspect aspect) {
        if (this.aspect != aspect) {
            this.aspect = aspect;
            setChanged();
        }
    }

    public int getColor() {
        if (level == null) {
            return -1;
        }
        if (getBlockState().getBlock() instanceof BlockBannerTC banner) {
            DyeColor dye = banner.getDyeColor();
            return dye != null ? dye.getFireworkColor() : -1;
        }
        return -1;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("facing", facing);
        if (aspect != null) {
            tag.putString("aspect", aspect.getTag());
        }
        tag.putBoolean("wall", onWall);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        facing = tag.getByte("facing");
        String as = tag.getString("aspect");
        aspect = !as.isEmpty() ? Aspect.getAspect(as) : null;
        onWall = tag.getBoolean("wall");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB bb = renderBoundingBox;
        if (bb == null) {
            bb = new AABB(
                    worldPosition.getX(), worldPosition.getY() - 1, worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1
            );
            renderBoundingBox = bb;
        }
        return bb;
    }
}