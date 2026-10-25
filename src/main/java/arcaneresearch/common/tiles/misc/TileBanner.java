package arcaneresearch.common.tiles.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.basic.BlockBannerTC;
import arcaneresearch.init.ModBlockEntities;
public class TileBanner extends BlockEntity {
    private byte facing = 0;
    private Aspect aspect = null;
    private boolean onWall = false;
    public TileBanner(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANNER.get(), pos, state);
    }
    public byte getBannerFacing() {
        return facing;
    }
    public void setBannerFacing(byte face) {
        facing = face;
        setChanged();
    }
    public boolean getWall() {
        return onWall;
    }
    public void setWall(boolean b) {
        onWall = b;
        setChanged();
    }
    public Aspect getAspect() {
        return aspect;
    }
    public void setAspect(Aspect aspect) {
        this.aspect = aspect;
        setChanged();
    }
    public int getColor() {
        if (getBlockState().getBlock() instanceof BlockBannerTC banner) {
            DyeColor dye = banner.getDyeColor();
            return dye != null ? dye.getFireworkColor() : -1;
        }
        return -1;
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("facing", facing);
        tag.putString("aspect", aspect != null ? aspect.getTag() : "");
        tag.putBoolean("wall", onWall);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        facing = tag.getByte("facing");
        String as = tag.getString("aspect");
        if (as != null && !as.isEmpty()) {
            aspect = Aspect.getAspect(as);
        } else {
            aspect = null;
        }
        onWall = tag.getBoolean("wall");
    }
    @Override
    public CompoundTag getUpdateTag() {
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
        return new AABB(
                worldPosition.getX(), worldPosition.getY() - 1, worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1
        );
    }
}
