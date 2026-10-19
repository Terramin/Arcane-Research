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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EssentiaHandler {

    public static final ConcurrentHashMap<String, EssentiaSourceFX> sourceFX = new ConcurrentHashMap<>();
    private static final int DELAY = 2000;
    private static final HashMap<WorldCoordinates, ArrayList<WorldCoordinates>> sources = new HashMap<>();
    private static final HashMap<WorldCoordinates, Long> sourcesDelay = new HashMap<>();
    private static BlockEntity lastTarget = null;
    private static BlockEntity lastSource = null;
    private static Aspect lastAspect = null;
    private static int lastExt = 0;

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, int ext) {
        return drainEssentia(tile, aspect, direction, range, false, ext);
    }

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null) return false;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return false;
            return drainEssentia(tile, aspect, direction, range, ignoreMirror, ext);
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource as)) {
                continue;
            }

            if (as.isBlocked()) continue;

            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;

            if (as.takeFromContainer(aspect, 1)) {

                sendEssentiaFX(tile, source.pos, aspect.getColor(), ext);
                return true;
            }
        }

        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static boolean drainEssentiaWithConfirmation(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null) return false;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return false;
            return drainEssentiaWithConfirmation(tile, aspect, direction, range, ignoreMirror, ext);
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource)) {
                continue;
            }

            IAspectSource as = (IAspectSource) sourceTile;
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
        if (lastSource != null && lastAspect != null && lastTarget != null) {
            IAspectSource as = (IAspectSource) lastSource;
            if (as.takeFromContainer(lastAspect, 1)) {
                sendEssentiaFX(lastTarget, lastSource.getBlockPos(), lastAspect.getColor(), lastExt);
            }
        }
        lastSource = null;
        lastAspect = null;
        lastTarget = null;
    }

    public static boolean addEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror, int ext) {
        if (tile == null || tile.getLevel() == null) return false;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return false;
            return addEssentia(tile, aspect, direction, range, ignoreMirror, ext);
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);
        ArrayList<WorldCoordinates> empties = new ArrayList<>();

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource)) {
                continue;
            }

            IAspectSource as = (IAspectSource) sourceTile;
            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;

            if (as.doesContainerAccept(aspect)) {
                if (as.getAspects() == null || as.getAspects().visSize() == 0) {
                    empties.add(source);
                } else if (as.addToContainer(aspect, 1) <= 0) {
                    sendEssentiaFX(sourceTile, tile.getBlockPos(), aspect.getColor(), ext);
                    return true;
                }
            }
        }

        for (WorldCoordinates source : empties) {
            if (source == null || source.pos == null) continue;

            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource)) {
                continue;
            }

            IAspectSource as = (IAspectSource) sourceTile;
            if (aspect != null && as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                sendEssentiaFX(sourceTile, tile.getBlockPos(), aspect.getColor(), ext);
                return true;
            }
        }

        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static boolean findEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null) return false;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return false;
            return findEssentia(tile, aspect, direction, range, ignoreMirror);
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource as)) {
                continue;
            }

            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;

            if (as.doesContainerContainAmount(aspect, 1)) {
                return true;
            }
        }

        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static BlockPos findEssentiaSourcePos(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null) return null;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return null;
            return findEssentiaSourcePos(tile, aspect, direction, range, ignoreMirror);
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource as)) {
                continue;
            }

            if (as.isBlocked()) continue;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;

            if (as.doesContainerContainAmount(aspect, 1)) {
                return source.pos;
            }
        }

        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return null;
    }

    public static boolean canAcceptEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range, boolean ignoreMirror) {
        if (tile == null || tile.getLevel() == null) return false;

        WorldCoordinates tileLoc = new WorldCoordinates(tile);

        if (!sources.containsKey(tileLoc)) {
            getSources(tile.getLevel(), tileLoc, direction, range);
            if (!sources.containsKey(tileLoc)) return false;
        }

        ArrayList<WorldCoordinates> sourceList = sources.get(tileLoc);

        for (WorldCoordinates source : sourceList) {
            BlockEntity sourceTile = tile.getLevel().getBlockEntity(source.pos);

            if (sourceTile == null || !(sourceTile instanceof IAspectSource)) {
                continue;
            }

            IAspectSource as = (IAspectSource) sourceTile;
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) continue;
            if (!as.isBlocked() && as.doesContainerAccept(aspect)) {
                return true;
            }
        }

        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    private static void getSources(Level level, WorldCoordinates tileLoc, Direction direction, int range) {

        if (sourcesDelay.containsKey(tileLoc)) {
            long delay = sourcesDelay.get(tileLoc);
            if (delay > System.currentTimeMillis()) {
                return;
            }
            sourcesDelay.remove(tileLoc);
        }

        ArrayList<WorldCoordinates> sourceList = new ArrayList<>();

        int start = 0;
        Direction searchDir = direction;
        if (searchDir == null) {
            start = -range;
            searchDir = Direction.UP;
        }

        for (int aa = -range; aa <= range; ++aa) {
            for (int bb = -range; bb <= range; ++bb) {
                for (int cc = start; cc < range; ++cc) {
                    if (aa == 0 && bb == 0 && cc == 0) continue;

                    int xx = tileLoc.pos.getX();
                    int yy = tileLoc.pos.getY();
                    int zz = tileLoc.pos.getZ();

                    if (searchDir.getAxis() == Direction.Axis.Y) {
                        xx += aa;
                        yy += cc * searchDir.getStepY();
                        zz += bb;
                    } else if (searchDir.getAxis() == Direction.Axis.Z) {
                        xx += aa;
                        yy += bb;
                        zz += cc * searchDir.getStepZ();
                    } else {
                        xx += cc * searchDir.getStepX();
                        yy += aa;
                        zz += bb;
                    }

                    BlockEntity te = level.getBlockEntity(new BlockPos(xx, yy, zz));
                    if (te instanceof IAspectSource) {

                        BlockEntity sourceTile = level.getBlockEntity(tileLoc.pos);
                        if (sourceTile instanceof TileMirrorEssentia && te instanceof TileMirrorEssentia mirror) {
                            TileMirrorEssentia src = (TileMirrorEssentia) sourceTile;
                            if (src.linkX == xx && src.linkY == yy && src.linkZ == zz
                                    && src.linkDimension.equals(mirror.getLevel() != null ? mirror.getLevel().dimension() : level.dimension())) {
                                continue;
                            }
                            if (mirror.linkX == tileLoc.pos.getX() && mirror.linkY == tileLoc.pos.getY()
                                    && mirror.linkZ == tileLoc.pos.getZ()
                                    && mirror.linkDimension.equals(level.dimension())) {
                                continue;
                            }
                        }
                        sourceList.add(new WorldCoordinates(new BlockPos(xx, yy, zz), level));
                    }
                }
            }
        }

        if (!sourceList.isEmpty()) {

            ArrayList<WorldCoordinates> sortedList = new ArrayList<>();
            for (WorldCoordinates wc : sourceList) {
                double dist = wc.getDistanceSquaredToWorldCoordinates(tileLoc);
                boolean inserted = false;

                for (int a = 0; a < sortedList.size(); ++a) {
                    double d2 = sortedList.get(a).getDistanceSquaredToWorldCoordinates(tileLoc);
                    if (dist < d2) {
                        sortedList.add(a, wc);
                        inserted = true;
                        break;
                    }
                }

                if (!inserted) {
                    sortedList.add(wc);
                }
            }

            sources.put(tileLoc, sortedList);
        } else {
            sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        }
    }

    public static void refreshSources(BlockEntity tile) {
        if (tile != null) {
            sources.remove(new WorldCoordinates(tile));
        }
    }

    public static void refreshSources(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        WorldCoordinates key = new WorldCoordinates(pos, level);
        sources.remove(key);
        sourcesDelay.remove(key);
    }

    public static void sendEssentiaFX(BlockEntity destTile, BlockPos sourcePos, int color, int ext) {
        if (destTile.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos destPos = destTile.getBlockPos();
            PacketFXEssentiaSource packet = new PacketFXEssentiaSource(
                    destPos, sourcePos, color, ext);

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
