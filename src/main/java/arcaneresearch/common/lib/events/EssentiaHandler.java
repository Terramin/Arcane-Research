package arcaneresearch.common.lib.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IAspectSource;
import arcaneresearch.api.internal.WorldCoordinates;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXEssentiaSource;
import arcaneresearch.common.tiles.devices.TileMirrorEssentia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class EssentiaHandler {
    public static final ConcurrentHashMap<String, EssentiaSourceFX> sourceFX = new ConcurrentHashMap<>();
    private static final int DELAY = 2000;
    private static final ConcurrentHashMap<WorldCoordinates, ArrayList<WorldCoordinates>> sources = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<WorldCoordinates, Long> sourcesDelay = new ConcurrentHashMap<>();
    private static BlockEntity lastTarget = null;
    private static BlockEntity lastSource = null;
    private static Aspect lastAspect = null;
    private static int lastExt = 0;

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, int ext) {
        return drainEssentia(tile, aspect, direction, range, false, ext);
    }

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null || aspect == null) return false;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return false;
        }
        for (WorldCoordinates worldCoordinates : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(worldCoordinates.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (as.takeFromContainer(aspect, 1)) {
                sendEssentiaFX(tile, sourceTile.getBlockPos(), aspect.getColor(), ext);
                return true;
            }
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static boolean drainEssentiaWithConfirmation(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null || aspect == null) return false;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return false;
        }
        for (WorldCoordinates worldCoordinates : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(worldCoordinates.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (as.doesContainerContainAmount(aspect, 1)) {
                lastSource = sourceTile;
                lastAspect = aspect;
                lastTarget = tile;
                lastExt = ext;
                return true;
            }
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static void confirmDrain() {
        if (lastSource instanceof IAspectSource as && lastAspect != null && lastTarget != null) {
            if (as.takeFromContainer(lastAspect, 1)) {
                sendEssentiaFX(lastTarget, lastSource.getBlockPos(), lastAspect.getColor(), lastExt);
            }
        }
        lastSource = null;
        lastAspect = null;
        lastTarget = null;
    }

    public static boolean addEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null || aspect == null) return false;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return false;
        }
        BlockPos targetPos = tile.getBlockPos();
        int color = aspect.getColor();
        ArrayList<WorldCoordinates> empties = null;
        for (WorldCoordinates worldCoordinates : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(worldCoordinates.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (!as.doesContainerAccept(aspect)) continue;
            if (as.getAspects() == null || as.getAspects().visSize() == 0) {
                if (empties == null) empties = new ArrayList<>();
                empties.add(worldCoordinates);
            } else if (as.addToContainer(aspect, 1) <= 0) {
                sendEssentiaFX(sourceTile, targetPos, color, ext);
                return true;
            }
        }
        if (empties != null) {
            for (WorldCoordinates source : empties) {
                if (source == null || source.pos == null) continue;
                BlockEntity sourceTile = level.getBlockEntity(source.pos);
                if (!(sourceTile instanceof IAspectSource as)) continue;
                if (as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                    sendEssentiaFX(sourceTile, targetPos, color, ext);
                    return true;
                }
            }
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static boolean findEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null || aspect == null) return false;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return false;
        }
        for (WorldCoordinates worldCoordinates : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(worldCoordinates.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (as.doesContainerContainAmount(aspect, 1)) return true;
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static BlockPos findEssentiaSourcePos(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null || aspect == null) return null;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return null;
        }
        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(source.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (as.doesContainerContainAmount(aspect, 1)) return source.pos;
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return null;
    }

    public static boolean canAcceptEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null || aspect == null) return false;
        Level level = tile.getLevel();
        WorldCoordinates tileLoc = new WorldCoordinates(tile);
        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        if (sourceList == null) {
            getSources(level, tileLoc, direction, range);
            sourceList = sources.get(tileLoc);
            if (sourceList == null) return false;
        }
        for (WorldCoordinates worldCoordinates : sourceList) {
            BlockEntity sourceTile = level.getBlockEntity(worldCoordinates.pos);
            if (!(sourceTile instanceof IAspectSource as)) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (!as.isBlocked() && as.doesContainerAccept(aspect)) return true;
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    private static void getSources(Level level, WorldCoordinates tileLoc, Direction direction, int range) {
        Long delay = sourcesDelay.get(tileLoc);
        if (delay != null) {
            if (delay > System.currentTimeMillis()) return;
            sourcesDelay.remove(tileLoc);
        }
        Direction searchDir = direction;
        int start = 0;
        if (searchDir == null) {
            start = -range;
            searchDir = Direction.UP;
        }
        Direction.Axis axis = searchDir.getAxis();
        int stepX = searchDir.getStepX();
        int stepY = searchDir.getStepY();
        int stepZ = searchDir.getStepZ();
        int baseX = tileLoc.pos.getX();
        int baseY = tileLoc.pos.getY();
        int baseZ = tileLoc.pos.getZ();
        BlockEntity selfTile = level.getBlockEntity(tileLoc.pos);
        TileMirrorEssentia selfMirror = selfTile instanceof TileMirrorEssentia m ? m : null;
        ArrayList<WorldCoordinates> sourceList = new ArrayList<>();
        for (int aa = -range; aa <= range; ++aa) {
            for (int bb = -range; bb <= range; ++bb) {
                for (int cc = start; cc < range; ++cc) {
                    if (aa == 0 && bb == 0 && cc == 0) continue;
                    int dx, dy, dz;
                    if (axis == Direction.Axis.Y) {
                        dx = aa;
                        dy = cc * stepY;
                        dz = bb;
                    } else if (axis == Direction.Axis.Z) {
                        dx = aa;
                        dy = bb;
                        dz = cc * stepZ;
                    } else {
                        dx = cc * stepX;
                        dy = aa;
                        dz = bb;
                    }
                    int xx = baseX + dx;
                    int yy = baseY + dy;
                    int zz = baseZ + dz;
                    BlockPos pos = new BlockPos(xx, yy, zz);
                    BlockEntity te = level.getBlockEntity(pos);
                    if (!(te instanceof IAspectSource)) continue;
                    if (selfMirror != null && te instanceof TileMirrorEssentia mirror) {
                        if (selfMirror.linkX == xx && selfMirror.linkY == yy && selfMirror.linkZ == zz
                                && selfMirror.linkDimension != null
                                && selfMirror.linkDimension.equals(mirror.getLevel() != null ? mirror.getLevel().dimension() : level.dimension())) {
                            continue;
                        }
                        if (mirror.linkX == baseX && mirror.linkY == baseY && mirror.linkZ == baseZ
                                && mirror.linkDimension != null
                                && mirror.linkDimension.equals(level.dimension())) {
                            continue;
                        }
                    }
                    sourceList.add(new WorldCoordinates(pos, level));
                }
            }
        }
        if (!sourceList.isEmpty()) {
            sourceList.sort(Comparator.comparingDouble(wc -> wc.getDistanceSquaredToWorldCoordinates(tileLoc)));
            sources.put(tileLoc, sourceList);
        } else {
            sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        }
    }

    public static void refreshSources(BlockEntity tile) {
        if (tile == null) return;
        WorldCoordinates key = new WorldCoordinates(tile);
        sources.remove(key);
        sourcesDelay.remove(key);
    }

    public static void refreshSources(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        WorldCoordinates key = new WorldCoordinates(pos, level);
        sources.remove(key);
        sourcesDelay.remove(key);
    }

    public static void sendEssentiaFX(BlockEntity destTile, BlockPos sourcePos, int color, int ext) {
        if (destTile == null || sourcePos == null) return;
        if (destTile.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos destPos = destTile.getBlockPos();
            PacketFXEssentiaSource packet = new PacketFXEssentiaSource(destPos, sourcePos, color, ext);
            PacketHandler.sendToAllTrackingChunk(packet, serverLevel, destPos);
        }
    }

    public static class EssentiaSourceFX {
        public BlockPos start;
        public BlockPos end;
        public int color;
        public int ext;

        public EssentiaSourceFX(BlockPos start, BlockPos end, int color, int ext) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.ext = ext;
        }
    }
}