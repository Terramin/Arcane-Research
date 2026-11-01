package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.devices.TileBellows;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileTubeBuffer extends TileTube implements IAspectContainer {

    public static final int MAX_AMOUNT = 10;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final byte[] EMPTY_CHOKE = {0, 0, 0, 0, 0, 0};

    public AspectList aspects = new AspectList();
    public byte[] chokedSides = {0, 0, 0, 0, 0, 0};
    private int tickCount = 0;
    private int bellows = -1;

    public TileTubeBuffer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileTubeBuffer(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TUBE_BUFFER.get(), pos, state);
    }

    public static void serverTickBuffer(Level level, BlockPos pos, BlockState state, TileTubeBuffer tile) {
        tile.tickCount++;
        if (tile.bellows < 0 || tile.tickCount % 20 == 0) {
            tile.updateBellows();
        }
        if (tile.tickCount % 5 == 0 && tile.aspects.visSize() < MAX_AMOUNT) {
            tile.fillBuffer();
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        aspects.writeToNBT(tag);
        byte[] sides = new byte[6];
        for (int i = 0; i < 6; i++) {
            sides[i] = (byte) (openSides[i] ? 1 : 0);
        }
        tag.putByteArray("Open", sides);
        tag.putByteArray("Choke", chokedSides);
        if (facing != null) {
            tag.putInt("Side", facing.ordinal());
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        aspects.readFromNBT(tag);
        if (tag.contains("Open") || tag.contains("open")) {
            byte[] sides = tag.contains("Open") ? tag.getByteArray("Open") : tag.getByteArray("open");
            if (sides.length == 6) {
                for (int i = 0; i < 6; i++) {
                    openSides[i] = sides[i] == 1;
                }
            }
        }
        if (tag.contains("Choke") || tag.contains("choke")) {
            byte[] choke = tag.contains("Choke") ? tag.getByteArray("Choke") : tag.getByteArray("choke");
            chokedSides = choke.length == 6 ? choke : EMPTY_CHOKE.clone();
        } else {
            chokedSides = EMPTY_CHOKE.clone();
        }
        int side = tag.contains("Side") ? tag.getInt("Side") : tag.getInt("side");
        if (side >= 0 && side < DIRECTIONS.length) {
            facing = DIRECTIONS[side];
        }
    }

    private void updateBellows() {
        bellows = TileBellows.getBellows(level, worldPosition, DIRECTIONS);
    }

    private void fillBuffer() {
        for (Direction dir : DIRECTIONS) {
            if (!canInputFrom(dir)) continue;
            assert level != null;
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, dir);
            if (te instanceof IEssentiaTransport transport) {
                Direction opposite = dir.getOpposite();
                if (transport.getEssentiaAmount(opposite) > 0) {
                    int ourSuction = getSuctionAmount(dir);
                    if (transport.getSuctionAmount(opposite) < ourSuction
                            && ourSuction >= transport.getMinimumSuction()) {
                        Aspect toTake = transport.getEssentiaType(opposite);
                        if (toTake != null) {
                            int taken = transport.takeEssentia(toTake, 1, opposite);
                            if (taken > 0) {
                                addToContainer(toTake, taken);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        if (amount != 1 || tag == null) return amount;
        if (aspects.visSize() < MAX_AMOUNT) {
            aspects.add(tag, amount);
            markDirtyAndSync();
            return 0;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        if (tag != null && aspects.getAmount(tag) >= amount) {
            aspects.remove(tag, amount);
            markDirtyAndSync();
            return true;
        }
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return tag != null && aspects.getAmount(tag) >= amount;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return tag != null ? aspects.getAmount(tag) : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return super.getMinimumSuction();
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        byte choke = chokedSides[face.ordinal()];
        if (choke == 2) return 0;
        if (bellows <= 0 || choke == 1) return 1;
        return bellows * 32;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        Aspect[] aspectArray = aspects.getAspects();
        if (aspectArray.length > 0 && level != null) {
            return aspectArray[level.random.nextInt(aspectArray.length)];
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return aspects.visSize();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (aspect == null || !canOutputTo(face)) return 0;
        assert level != null;
        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, face);
        int requestingSuction = 0;
        if (te instanceof IEssentiaTransport transport) {
            requestingSuction = transport.getSuctionAmount(face.getOpposite());
        }
        for (Direction dir : DIRECTIONS) {
            if (dir == face || !canOutputTo(dir)) continue;
            BlockEntity otherTe = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, dir);
            if (otherTe instanceof IEssentiaTransport transport) {
                Direction opposite = dir.getOpposite();
                int otherSuction = transport.getSuctionAmount(opposite);
                Aspect otherSuctionType = transport.getSuctionType(opposite);
                if ((otherSuctionType == aspect || otherSuctionType == null)
                        && requestingSuction < otherSuction
                        && getSuctionAmount(dir) < otherSuction) {
                    return 0;
                }
            }
        }
        int available = aspects.getAmount(aspect);
        if (amount > available) amount = available;
        if (amount <= 0) return 0;
        return takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? (amount - addToContainer(aspect, amount)) : 0;
    }

    public void cycleChoke(Direction side) {
        int ord = side.ordinal();
        byte choke = (byte) (chokedSides[ord] + 1);
        if (choke > 2) choke = 0;
        chokedSides[ord] = choke;
        markDirtyAndSync();
    }

    public int getChokeState(Direction side) {
        return chokedSides[side.ordinal()];
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player,
                                      BlockPos pos, Direction side, InteractionHand hand) {
        player.swing(hand);
        if (player.isShiftKeyDown()) {
            level.playSound(player, worldPosition, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.6f,
                    2.0f + level.random.nextFloat() * 0.2f);
            if (!level.isClientSide) {
                cycleChoke(side);
            }
            return true;
        }
        level.playSound(player, worldPosition, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                0.9f + level.random.nextFloat() * 0.2f);
        toggleSide(side);
        return true;
    }
}