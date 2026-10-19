package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
public class TileCentrifuge extends TileArcaneresearch implements IAspectContainer, IEssentiaTransport {
    public static final int PROCESS_TIME = 39;
    public Aspect aspectIn = null;
    public Aspect aspectOut = null;
    public int progress = 0;
    public float rotation = 0.0f;
    public float rotationSpeed = 0.0f;
    private int count = 0;
    public TileCentrifuge(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileCentrifuge(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CENTRIFUGE.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCentrifuge tile) {
        if (tile.gettingPower()) {
            return;
        }
        if (tile.aspectOut == null && tile.aspectIn == null && ++tile.count % 5 == 0) {
            tile.drawEssentia();
        }
        if (tile.progress > 0) {
            tile.progress--;
        }
        if (tile.aspectOut == null && tile.aspectIn != null && tile.progress == 0) {
            tile.processEssentia();
        }
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCentrifuge tile) {
        float pr = tile.rotation % 180.0f;
        if (tile.aspectIn != null && !tile.gettingPower() && tile.rotationSpeed < 20.0f) {
            tile.rotationSpeed += 2.0f;
        }
        if ((tile.aspectIn == null || tile.gettingPower()) && tile.rotationSpeed > 0.0f) {
            tile.rotationSpeed -= 0.5f;
        }
        tile.rotation += tile.rotationSpeed;
        float npr = tile.rotation % 180.0f;
        if (tile.rotationSpeed > 0.0f && pr >= 160.0f && npr <= 20.0f) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    ModSounds.PUMP.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (aspectIn != null) {
            tag.putString("aspectIn", aspectIn.getTag());
        }
        if (aspectOut != null) {
            tag.putString("aspectOut", aspectOut.getTag());
        }
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        aspectIn = tag.contains("aspectIn") ? Aspect.getAspect(tag.getString("aspectIn")) : null;
        aspectOut = tag.contains("aspectOut") ? Aspect.getAspect(tag.getString("aspectOut")) : null;
        if (aspectIn == null && tag.contains("Aspect")) {
            aspectIn = Aspect.getAspect(tag.getString("Aspect"));
        }
    }
    private boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }
    private void processEssentia() {
        if (level == null || aspectIn == null) return;
        Aspect[] components = aspectIn.getComponents();
        if (components == null || components.length != 2) {
            aspectIn = null;
            setChanged();
            return;
        }
        aspectOut = components[level.random.nextInt(2)];
        aspectIn = null;
        setChanged();
        syncTile(false);
    }
    private void drawEssentia() {
        if (level == null) return;
        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, Direction.DOWN);
        if (!(te instanceof IEssentiaTransport transport)) {
            return;
        }
        if (!transport.canOutputTo(Direction.UP)) {
            return;
        }
        if (transport.getEssentiaAmount(Direction.UP) <= 0) {
            return;
        }
        if (transport.getSuctionAmount(Direction.UP) >= getSuctionAmount(Direction.DOWN)) {
            return;
        }
        if (getSuctionAmount(Direction.DOWN) < transport.getMinimumSuction()) {
            return;
        }
        Aspect taken = transport.getEssentiaType(Direction.UP);
        if (taken == null || taken.isPrimal()) {
            return;
        }
        if (transport.takeEssentia(taken, 1, Direction.UP) == 1) {
            aspectIn = taken;
            progress = PROCESS_TIME;
            setChanged();
            syncTile(false);
        }
    }
    @Override
    public AspectList getAspects() {
        AspectList list = new AspectList();
        if (aspectOut != null) {
            list.add(aspectOut, 1);
        }
        return list;
    }
    @Override
    public void setAspects(AspectList aspects) {
    }
    @Override
    public int addToContainer(Aspect tag, int amt) {
        if (amt > 0 && aspectOut == null) {
            aspectOut = tag;
            setChanged();
            syncTile(false);
            return amt - 1;
        }
        return amt;
    }
    @Override
    public boolean takeFromContainer(Aspect tag, int amt) {
        if (aspectOut != null && tag == aspectOut) {
            aspectOut = null;
            setChanged();
            syncTile(false);
            return true;
        }
        return false;
    }
    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }
    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return amt == 1 && tag == aspectOut && aspectOut != null;
    }
    @Override
    public boolean doesContainerContain(AspectList list) {
        for (Aspect tt : list.getAspects()) {
            if (tt == aspectOut) {
                return true;
            }
        }
        return false;
    }
    @Override
    public int containerContains(Aspect tag) {
        return (tag == aspectOut) ? 1 : 0;
    }
    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }
    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }
    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }
    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }
    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }
    @Override
    public int getSuctionAmount(Direction face) {
        if (gettingPower() || face != Direction.DOWN) {
            return 0;
        }
        return aspectIn == null ? 128 : 64;
    }
    @Override
    public Aspect getEssentiaType(Direction face) {
        return aspectOut;
    }
    @Override
    public int getEssentiaAmount(Direction face) {
        return aspectOut != null ? 1 : 0;
    }
    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return (canOutputTo(face) && takeFromContainer(aspect, amount)) ? amount : 0;
    }
    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (canInputFrom(face) && aspectIn == null && aspect != null && !aspect.isPrimal()) {
            aspectIn = aspect;
            progress = PROCESS_TIME;
            setChanged();
            syncTile(false);
            return 1;
        }
        return 0;
    }
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    public Aspect getAspect() {
        return aspectOut != null ? aspectOut : aspectIn;
    }
    public int getAmount() {
        return (aspectOut != null || aspectIn != null) ? 1 : 0;
    }
    public float getProgress() {
        return PROCESS_TIME > 0 ? 1.0f - (float) progress / PROCESS_TIME : 0;
    }
}
