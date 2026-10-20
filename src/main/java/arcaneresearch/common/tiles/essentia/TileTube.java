package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.casters.IInteractWithCaster;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import java.util.Random;
public class TileTube extends TileArcaneresearch implements IEssentiaTransport, IInteractWithCaster {
    public static final int TICK_FREQ = 5;
    public Direction facing = Direction.NORTH;
    public boolean[] openSides = {true, true, true, true, true, true};
    protected Aspect essentiaType = null;
    protected int essentiaAmount = 0;
    protected Aspect suctionType = null;
    protected int suction = 0;
    protected int venting = 0;
    protected int ventColor = 0;
    protected int count = 0;
    public TileTube(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileTube(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileTube tile) {
        if (tile.venting > 0) {
            tile.venting--;
        }
        if (tile.count == 0) {
            tile.count = level.random.nextInt(10);
        }
        if (tile.venting <= 0) {
            if (++tile.count % 2 == 0) {
                tile.calculateSuction(null, false, false);
                tile.checkVenting();
                if (tile.essentiaType != null && tile.essentiaAmount == 0) {
                    tile.essentiaType = null;
                }
            }
            if (tile.count % TICK_FREQ == 0 && tile.suction > 0) {
                tile.equalizeWithNeighbours(false);
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileTube tile) {
        if (tile.venting > 0) {
            tile.venting--;
            Random r = new Random(tile.hashCode() * 4);
            float rp = r.nextFloat() * 360.0f;
            float ry = r.nextFloat() * 360.0f;
            double fx = -Mth.sin(ry / 180.0f * (float) Math.PI) * Mth.cos(rp / 180.0f * (float) Math.PI);
            double fz = Mth.cos(ry / 180.0f * (float) Math.PI) * Mth.cos(rp / 180.0f * (float) Math.PI);
            double fy = -Mth.sin(rp / 180.0f * (float) Math.PI);
            FXDispatcher.INSTANCE.drawVentParticles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    fx / 5.0, fy / 5.0, fz / 5.0, tile.ventColor);
        }
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (essentiaType != null) {
            tag.putString("Type", essentiaType.getTag());
        }
        tag.putInt("Amount", essentiaAmount);
        tag.putInt("Side", facing.ordinal());
        byte[] sides = new byte[6];
        for (int i = 0; i < 6; i++) {
            sides[i] = (byte) (openSides[i] ? 1 : 0);
        }
        tag.putByteArray("Open", sides);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        essentiaType = Aspect.getAspect(tag.contains("Type")
                ? tag.getString("Type") : tag.getString("type"));
        essentiaAmount = tag.contains("Amount") ? tag.getInt("Amount") : tag.getInt("amount");
        int side = tag.contains("Side") ? tag.getInt("Side") : tag.getInt("side");
        if (side >= 0 && side < Direction.values().length) {
            facing = Direction.values()[side];
        }
        byte[] sides = tag.contains("Open") ? tag.getByteArray("Open") : tag.getByteArray("open");
        if (sides != null && sides.length == 6) {
            for (int i = 0; i < 6; i++) {
                openSides[i] = sides[i] == 1;
            }
        }
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (suctionType != null) {
            tag.putString("SType", suctionType.getTag());
        }
        tag.putInt("SAmount", suction);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        suctionType = Aspect.getAspect(tag.contains("SType")
                ? tag.getString("SType") : tag.getString("stype"));
        suction = tag.contains("SAmount") ? tag.getInt("SAmount") : tag.getInt("samount");
    }
    protected void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
        suction = 0;
        suctionType = null;
        for (Direction dir : Direction.values()) {
            if (directional && facing != dir.getOpposite()) continue;
            if (!isConnectable(dir)) continue;
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te instanceof IEssentiaTransport transport) {
                Direction opposite = dir.getOpposite();
                if (filter != null && transport.getSuctionType(opposite) != null
                        && transport.getSuctionType(opposite) != filter) {
                    continue;
                }
                if (filter == null && getEssentiaAmount(dir) > 0
                        && transport.getSuctionType(opposite) != null
                        && getEssentiaType(dir) != transport.getSuctionType(opposite)) {
                    continue;
                }
                if (filter != null && getEssentiaAmount(dir) > 0
                        && getEssentiaType(dir) != null
                        && transport.getSuctionType(opposite) != null
                        && getEssentiaType(dir) != transport.getSuctionType(opposite)) {
                    continue;
                }
                int neighborSuction = transport.getSuctionAmount(opposite);
                if (neighborSuction > 0 && neighborSuction > suction + 1) {
                    Aspect st = transport.getSuctionType(opposite);
                    if (st == null) st = filter;
                    setSuction(st, restrict ? neighborSuction / 2 : neighborSuction - 1);
                }
            }
        }
    }
    protected void checkVenting() {
        for (Direction dir : Direction.values()) {
            if (!isConnectable(dir)) continue;
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te instanceof IEssentiaTransport transport) {
                Direction opposite = dir.getOpposite();
                int neighborSuction = transport.getSuctionAmount(opposite);
                if (suction > 0 && (neighborSuction == suction || neighborSuction == suction - 1)
                        && suctionType != transport.getSuctionType(opposite)
                        && !(te instanceof TileTubeFilter)) {
                    int colorIndex = -1;
                    if (suctionType != null) {
                        colorIndex = ModConfig.aspectOrder.indexOf(suctionType);
                    }
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, colorIndex);
                    venting = 40;
                }
            }
        }
    }
    protected void equalizeWithNeighbours(boolean directional) {
        if (essentiaAmount > 0) return;
        for (Direction dir : Direction.values()) {
            if (directional && facing == dir.getOpposite()) continue;
            if (!isConnectable(dir)) continue;
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te instanceof IEssentiaTransport transport) {
                Direction opposite = dir.getOpposite();
                if (!transport.canOutputTo(opposite)) continue;
                Aspect ourSuction = getSuctionType(null);
                Aspect theirEssentia = transport.getEssentiaType(opposite);
                if (ourSuction != null && theirEssentia != null && ourSuction != theirEssentia) {
                    continue;
                }
                if (getSuctionAmount(null) <= transport.getSuctionAmount(opposite)) {
                    continue;
                }
                if (getSuctionAmount(null) < transport.getMinimumSuction()) {
                    continue;
                }
                Aspect toPull = ourSuction;
                if (toPull == null) {
                    toPull = theirEssentia;
                    if (toPull == null) {
                        toPull = transport.getEssentiaType(null);
                    }
                }
                int taken = transport.takeEssentia(toPull, 1, opposite);
                if (taken > 0) {
                    int added = addEssentia(toPull, taken, dir);
                    if (added > 0) {
                        if (level.random.nextInt(100) == 0) {
                            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
                        }
                        return;
                    }
                }
            }
        }
    }
    @Override
    public boolean isConnectable(Direction face) {
        return face != null && openSides[face.ordinal()];
    }
    @Override
    public boolean canInputFrom(Direction face) {
        return face != null && openSides[face.ordinal()];
    }
    @Override
    public boolean canOutputTo(Direction face) {
        return face != null && openSides[face.ordinal()];
    }
    @Override
    public void setSuction(Aspect aspect, int amount) {
        suctionType = aspect;
        suction = amount;
    }
    @Override
    public Aspect getSuctionType(Direction face) {
        return suctionType;
    }
    @Override
    public int getSuctionAmount(Direction face) {
        return suction;
    }
    @Override
    public Aspect getEssentiaType(Direction face) {
        return essentiaType;
    }
    @Override
    public int getEssentiaAmount(Direction face) {
        return essentiaAmount;
    }
    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (canOutputTo(face) && essentiaType == aspect && essentiaAmount > 0 && amount > 0) {
            essentiaAmount--;
            if (essentiaAmount <= 0) {
                essentiaType = null;
            }
            setChanged();
            return 1;
        }
        return 0;
    }
    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (canInputFrom(face) && essentiaAmount == 0 && amount > 0) {
            essentiaType = aspect;
            essentiaAmount++;
            setChanged();
            return 1;
        }
        return 0;
    }
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 0) {
            if (level != null && level.isClientSide) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        ModSounds.CREAK.get(), SoundSource.BLOCKS,
                        1.0f, 1.3f + level.random.nextFloat() * 0.2f, false
                );
            }
            return true;
        }
        if (id == 1) {
            if (level != null && level.isClientSide) {
                if (venting <= 0) {
                    level.playLocalSound(
                            worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                            SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                            0.1f, 1.0f + level.random.nextFloat() * 0.1f, false
                    );
                }
                venting = 50;
                if (param == -1 || param >= ModConfig.aspectOrder.size()) {
                    ventColor = 0xAAAAAA;
                } else {
                    ventColor = ModConfig.aspectOrder.get(param).getColor();
                }
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }
    public void toggleSide(Direction side) {
        if (side != null) {
            openSides[side.ordinal()] = !openSides[side.ordinal()];
            markDirtyAndSync();
            syncConnectionState();
            if (level != null) {
                BlockEntity te = level.getBlockEntity(worldPosition.relative(side));
                if (te instanceof TileTube otherTube) {
                    otherTube.openSides[side.getOpposite().ordinal()] = openSides[side.ordinal()];
                    otherTube.markDirtyAndSync();
                }
            }
        }
    }
    protected void syncConnectionState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockTube) {
            level.setBlock(worldPosition,
                    arcaneresearch.common.blocks.essentia.BlockTube.withConnections(level, worldPosition, state), 3);
        }
    }
    public boolean canConnectSide(Direction side) {
        if (level == null) return false;
        BlockEntity te = level.getBlockEntity(worldPosition.relative(side));
        return te instanceof IEssentiaTransport;
    }
    public void cycleFacing() {
        int current = facing.ordinal();
        for (int i = 1; i < 20; i++) {
            Direction newFacing = Direction.values()[(current + i) % 6];
            if (canConnectSide(newFacing.getOpposite()) && isConnectable(newFacing.getOpposite())) {
                facing = newFacing;
                markDirtyAndSync();
                syncConnectionState();
                return;
            }
        }
    }
    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player,
                                      BlockPos pos, Direction side, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            level.playSound(player, worldPosition, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                    0.9f + level.random.nextFloat() * 0.2f);
            player.swing(hand);
            cycleFacing();
            return true;
        }
        level.playSound(player, worldPosition, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                0.9f + level.random.nextFloat() * 0.2f);
        player.swing(hand);
        toggleSide(side);
        return true;
    }
    public AABB getCustomRenderBoundingBox() {
        return new AABB(
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1
        );
    }
}
