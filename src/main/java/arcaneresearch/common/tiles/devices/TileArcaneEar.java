package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import arcaneresearch.common.lib.events.WorldEvents;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import java.util.ArrayList;
import java.util.WeakHashMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileArcaneEar extends TileArcaneresearch {

    public static final int MAX_NOTE = 24;
    public static final int MAX_RANGE_SQ = 4096;
    public static WeakHashMap<Level, ArrayList<int[]>> noteBlockEvents = new WeakHashMap<>();
    public byte note = 0;
    public byte instrument = 0;
    public int redstoneSignal = 0;

    private boolean isToggle = false;
    private boolean toggleState = false;

    public TileArcaneEar(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileArcaneEar(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileArcaneEar tile) {

        if (tile.redstoneSignal > 0) {
            tile.redstoneSignal--;
            if (tile.redstoneSignal == 0) {
                tile.updateRedstone(state, false);
            }
        }

        String dimKey = level.dimension().identifier().toString();
        ArrayList<WorldEvents.NoteBlockData> events = WorldEvents.noteBlockEvents.get(dimKey);
        if (events != null && !events.isEmpty()) {
            for (WorldEvents.NoteBlockData data : events) {

                if (data.instrument == tile.instrument && data.note == tile.note) {

                    double distSq = new net.minecraft.world.phys.Vec3(
                            pos.getX(), pos.getY(), pos.getZ()).distanceToSqr(
                            data.x + 0.5, data.y + 0.5, data.z + 0.5);
                    if (distSq <= MAX_RANGE_SQ) {
                        tile.onNoteDetected(state);
                        break;
                    }
                }
            }
        }
    }

    public static void registerNoteBlockEvent(Level level, BlockPos pos, int instrument, int note) {
        String dimKey = level.dimension().identifier().toString();
        WorldEvents.noteBlockEvents
                .computeIfAbsent(dimKey, k -> new ArrayList<>())
                .add(new WorldEvents.NoteBlockData(pos.getX(), pos.getY(), pos.getZ(), instrument, note));
    }

    public static void clearNoteBlockEvents(Level level) {
        WorldEvents.noteBlockEvents.remove(level.dimension().identifier().toString());
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        tag.putByte("note", note);
        tag.putByte("tone", instrument);
        tag.putBoolean("Toggle", isToggle);
        tag.putBoolean("ToggleState", toggleState);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        note = tag.read("Note", com.mojang.serialization.Codec.BYTE).isPresent() ? tag.getByteOr("Note", (byte) 0) : tag.getByteOr("note", (byte) 0);

        instrument = tag.read("Instrument", com.mojang.serialization.Codec.BYTE).isPresent() ? tag.getByteOr("Instrument", (byte) 0) : tag.getByteOr("tone", (byte) 0);
        if (note < 0) note = 0;
        if (note > MAX_NOTE) note = MAX_NOTE;
        isToggle = tag.getBooleanOr("Toggle", false);
        toggleState = tag.getBooleanOr("ToggleState", false);
    }

    private void onNoteDetected(BlockState state) {
        if (level == null) return;

        triggerNote(true);

        if (isToggle) {

            toggleState = !toggleState;
            updateRedstone(state, toggleState);
            setChanged();
        } else {

            redstoneSignal = 10;
            updateRedstone(state, true);
        }
    }

    private void updateRedstone(BlockState state, boolean powered) {
        if (level == null) return;

        Direction facing = getFacing().getOpposite();

        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            boolean currentEnabled = state.getValue(BlockStateProperties.ENABLED);
            if (currentEnabled != powered) {
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.ENABLED, powered), 3);
            }
        }

        level.updateNeighborsAt(worldPosition, state.getBlock());
        level.updateNeighborsAt(worldPosition.relative(facing), state.getBlock());
    }

    public void changePitch() {
        note = (byte) ((note + 1) % (MAX_NOTE + 1));
        setChanged();
    }

    public void updateInstrument() {
        if (level == null) return;

        try {
            Direction facing = getFacing().getOpposite();
            BlockPos behindPos = worldPosition.relative(facing);
            BlockState behindState = level.getBlockState(behindPos);

            instrument = getInstrumentFromBlock(behindState);
            setChanged();
        } catch (Exception e) {

        }
    }

    private byte getInstrumentFromBlock(BlockState state) {

        NoteBlockInstrument noteInstrument = state.instrument();

        return switch (noteInstrument) {
            case HARP -> 0;
            case BASEDRUM -> 1;
            case SNARE -> 2;
            case HAT -> 3;
            case BASS -> 4;
            case FLUTE -> 5;
            case BELL -> 6;
            case GUITAR -> 7;
            case CHIME -> 8;
            case XYLOPHONE -> 9;
            case IRON_XYLOPHONE -> 10;
            case COW_BELL -> 11;
            case DIDGERIDOO -> 12;
            case BIT -> 13;
            case BANJO -> 14;
            case PLING -> 15;
            default -> 0;
        };
    }

    public void triggerNote(boolean playSound) {
        if (level == null || !playSound) return;

        Direction facing = getFacing().getOpposite();
        BlockPos behindPos = worldPosition.relative(facing);
        BlockState behindState = level.getBlockState(behindPos);

        byte soundInstrument = getInstrumentFromBlock(behindState);

        level.blockEvent(worldPosition, getBlockState().getBlock(), soundInstrument, note);
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.NORTH;
    }

    public byte getNote() {
        return note;
    }

    public byte getInstrument() {
        return instrument;
    }

    public boolean isToggleMode() {
        return isToggle;
    }

    public void setToggleMode(boolean toggle) {
        this.isToggle = toggle;
        setChanged();
    }

    public boolean getToggleState() {
        return toggleState;
    }

    public int getRedstoneOutput() {
        if (isToggle) {
            return toggleState ? 15 : 0;
        }
        return redstoneSignal > 0 ? 15 : 0;
    }
}
