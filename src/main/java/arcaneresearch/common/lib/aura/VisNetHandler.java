package arcaneresearch.common.lib.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.tiles.aura.TileVisNode;
import arcaneresearch.common.tiles.aura.TileVisNode.AuraPos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class VisNetHandler {
    public static HashMap<ResourceKey<Level>, HashMap<AuraPos, WeakReference<TileVisNode>>> sources = new HashMap<>();
    static ArrayList<AuraPos> cache = new ArrayList<>();
    private static final ConcurrentHashMap<AuraPos, ArrayList<WeakReference<TileVisNode>>> nearbyNodes = new ConcurrentHashMap<>();

    public static int drainVis(Level level, BlockPos pos, Aspect aspect, int amount) {
        if (level == null || pos == null || aspect == null || amount <= 0) return 0;
        int drainedAmount = 0;
        AuraPos drainer = new AuraPos(level.dimension(), pos);
        if (!nearbyNodes.containsKey(drainer)) {
            calculateNearbyNodes(level, pos);
        }
        ArrayList<WeakReference<TileVisNode>> nodes = nearbyNodes.get(drainer);
        if (nodes != null && !nodes.isEmpty()) {
            for (WeakReference<TileVisNode> noderef : nodes) {
                if (noderef == null) continue;
                TileVisNode node = noderef.get();
                if (node == null) continue;
                int a = node.consumeVis(aspect, amount);
                drainedAmount += a;
                amount -= a;
                if (a > 0) {
                    BlockPos np = node.getBlockPos();
                    AuraFX.nodeSparkle(level, np, aspect.getColor());
                }
                if (amount <= 0) break;
            }
        }
        return drainedAmount;
    }

    public static void addSource(Level level, TileVisNode vs) {
        if (level == null || vs == null || vs.getLocation() == null) return;
        ResourceKey<Level> dim = level.dimension();
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.computeIfAbsent(dim, k -> new HashMap<>());
        sourcelist.put(vs.getLocation(), new WeakReference<>(vs));
        nearbyNodes.clear();
    }

    public static boolean isNodeValid(WeakReference<TileVisNode> node) {
        if (node == null) return false;
        TileVisNode n = node.get();
        return n != null && !n.isRemoved();
    }

    public static WeakReference<TileVisNode> addNode(Level level, TileVisNode vn) {
        if (level == null || vn == null || vn.getLocation() == null || vn.getRange() < 0) return null;
        WeakReference<TileVisNode> ref = new WeakReference<>(vn);
        ResourceKey<Level> dim = level.dimension();
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.computeIfAbsent(dim, k -> new HashMap<>());
        ArrayList<Object[]> nearby = new ArrayList<>();
        cache.clear();
        for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (!isNodeValid(root)) continue;
            TileVisNode source = root.get();
            if (source == null || source.getLocation() == null) continue;
            float r = inRange(vn.getLocation(), source.getLocation(), vn.getRange());
            if (r > 0.0F) {
                nearby.add(new Object[]{source, r - (vn.getRange() * 2)});
            }
            nearby = findClosestNodes(vn, source, nearby);
            cache.clear();
        }
        float dist = Float.MAX_VALUE;
        TileVisNode closest = null;
        if (!nearby.isEmpty()) {
            for (Object[] o : nearby) {
                if (o == null || o.length < 2 || !(o[0] instanceof TileVisNode candidate) || !(o[1] instanceof Float f))
                    continue;
                if (f < dist
                        && (vn.getAttunement() == -1
                        || candidate.getAttunement() == -1
                        || vn.getAttunement() == candidate.getAttunement())
                        && canNodeBeSeen(level, vn, candidate)) {
                    dist = f;
                    closest = candidate;
                }
            }
        }
        if (closest != null) {
            ArrayList<WeakReference<TileVisNode>> children = closest.getChildren();
            if (children != null) {
                children.add(ref);
            }
            nearbyNodes.clear();
            return new WeakReference<>(closest);
        }
        return null;
    }

    public static ArrayList<Object[]> findClosestNodes(TileVisNode target, TileVisNode parent, ArrayList<Object[]> in) {
        if (target == null || parent == null || in == null) return in != null ? in : new ArrayList<>();
        if (parent.getLocation() == null) return in;
        if (cache.size() > 512 || cache.contains(new AuraPos(parent.getLocation().dim(), parent.getBlockPos()))) {
            return in;
        }
        cache.add(new AuraPos(parent.getLocation().dim(), parent.getBlockPos()));
        ArrayList<WeakReference<TileVisNode>> children = parent.getChildren();
        if (children == null) return in;
        for (WeakReference<TileVisNode> childWR : children) {
            if (childWR == null) continue;
            TileVisNode child = childWR.get();
            if (child == null || child.getLocation() == null) continue;
            if (!child.equals(target) && !child.equals(parent)) {
                float r2 = inRange(child.getLocation(), target.getLocation(), target.getRange());
                if (r2 > 0.0F) {
                    in.add(new Object[]{child, r2});
                }
                in = findClosestNodes(target, child, in);
            }
        }
        return in;
    }

    private static float inRange(AuraPos cc1, AuraPos cc2, int range) {
        if (cc1 == null || cc2 == null || cc1.dim() == null || cc2.dim() == null || cc1.pos() == null || cc2.pos() == null) {
            return -1.0F;
        }
        if (!cc1.dim().equals(cc2.dim())) return -1.0F;
        double distance = cc1.pos().distSqr(cc2.pos());
        return (distance > (range * range)) ? -1.0F : (float) distance;
    }

    private static void calculateNearbyNodes(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        ResourceKey<Level> dim = level.dimension();
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.computeIfAbsent(dim, k -> new HashMap<>());
        ArrayList<WeakReference<TileVisNode>> cn = new ArrayList<>();
        AuraPos drainer = new AuraPos(dim, pos);
        for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (!isNodeValid(root)) continue;
            TileVisNode source = root.get();
            if (source == null || source.getLocation() == null) continue;
            TileVisNode closest = null;
            float range = Float.MAX_VALUE;
            float r = inRange(drainer, source.getLocation(), source.getRange());
            if (r > 0.0F) {
                range = r;
                closest = source;
            }
            ArrayList<WeakReference<TileVisNode>> children = getAllChildren(source, new ArrayList<>());
            for (WeakReference<TileVisNode> childWR : children) {
                if (childWR == null) continue;
                TileVisNode n2 = childWR.get();
                if (n2 == null || n2.getLocation() == null) continue;
                if (!n2.equals(source)) {
                    float r2 = inRange(n2.getLocation(), drainer, n2.getRange());
                    if (r2 > 0.0F && r2 < range) {
                        range = r2;
                        closest = n2;
                    }
                }
            }
            if (closest != null) {
                cn.add(new WeakReference<>(closest));
            }
        }
        nearbyNodes.put(drainer, cn);
    }

    private static ArrayList<WeakReference<TileVisNode>> getAllChildren(TileVisNode source,
                                                                        ArrayList<WeakReference<TileVisNode>> list) {
        if (source == null) return list;
        if (list == null) list = new ArrayList<>();
        ArrayList<WeakReference<TileVisNode>> children = source.getChildren();
        if (children == null) return list;
        for (WeakReference<TileVisNode> childWR : children) {
            if (childWR == null) continue;
            TileVisNode child = childWR.get();
            if (child == null) continue;
            Level level = child.getLevel();
            if (level == null) continue;
            BlockPos bp = child.getBlockPos();
            if (!level.isLoaded(bp)) continue;
            list.add(childWR);
            getAllChildren(child, list);
        }
        return list;
    }

    public static boolean canNodeBeSeen(Level level, TileVisNode source, TileVisNode target) {
        if (level == null || source == null || target == null) return false;
        BlockPos sourcePos = source.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        Vec3 from = new Vec3(sourcePos.getX() + 0.5D, sourcePos.getY() + 0.5D, sourcePos.getZ() + 0.5D);
        Vec3 to = new Vec3(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);
        BlockHitResult mop = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        if (mop.getType() == HitResult.Type.MISS) return true;
        return mop.getType() == HitResult.Type.BLOCK
                && targetPos.equals(mop.getBlockPos());
    }
}