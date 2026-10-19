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
public class VisNetHandler {
    public static HashMap<ResourceKey<Level>, HashMap<AuraPos, WeakReference<TileVisNode>>> sources = new HashMap<>();
    static ArrayList<AuraPos> cache = new ArrayList<>();
    private static HashMap<AuraPos, ArrayList<WeakReference<TileVisNode>>> nearbyNodes = new HashMap<>();
    public static int drainVis(Level level, BlockPos pos, Aspect aspect, int amount) {
        int drainedAmount = 0;
        AuraPos drainer = new AuraPos(level.dimension(), pos);
        if (!nearbyNodes.containsKey(drainer)) {
            calculateNearbyNodes(level, pos);
        }
        ArrayList<WeakReference<TileVisNode>> nodes = nearbyNodes.get(drainer);
        if (nodes != null && !nodes.isEmpty()) {
            for (WeakReference<TileVisNode> noderef : nodes) {
                TileVisNode node = noderef.get();
                if (node == null) {
                    continue;
                }
                int a = node.consumeVis(aspect, amount);
                drainedAmount += a;
                amount -= a;
                if (a > 0) {
                    BlockPos np = node.getBlockPos();
                    AuraFX.nodeSparkle(level, np, aspect.getColor());
                }
                if (amount <= 0) {
                    break;
                }
            }
        }
        return drainedAmount;
    }
    public static void addSource(Level level, TileVisNode vs) {
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.get(level.dimension());
        if (sourcelist == null) {
            sourcelist = new HashMap<>();
        }
        sourcelist.put(vs.getLocation(), new WeakReference<>(vs));
        sources.put(level.dimension(), sourcelist);
        nearbyNodes.clear();
    }
    public static boolean isNodeValid(WeakReference<TileVisNode> node) {
        if (node == null || node.get() == null || node.get().isRemoved()) {
            return false;
        }
        return true;
    }
    public static WeakReference<TileVisNode> addNode(Level level, TileVisNode vn) {
        WeakReference<TileVisNode> ref = new WeakReference<>(vn);
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.get(level.dimension());
        if (sourcelist == null) {
            sourcelist = new HashMap<>();
            return null;
        }
        ArrayList<Object[]> nearby = new ArrayList<>();
        for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (!isNodeValid(root)) {
                continue;
            }
            TileVisNode source = root.get();
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
                if ((Float) o[1] < dist && (vn.getAttunement() == -1
                        || ((TileVisNode) o[0]).getAttunement() == -1
                        || vn.getAttunement() == ((TileVisNode) o[0]).getAttunement())
                        && canNodeBeSeen(level, vn, (TileVisNode) o[0])) {
                    dist = (Float) o[1];
                    closest = (TileVisNode) o[0];
                }
            }
        }
        if (closest != null) {
            closest.getChildren().add(ref);
            nearbyNodes.clear();
            return new WeakReference<>(closest);
        }
        return null;
    }
    public static ArrayList<Object[]> findClosestNodes(TileVisNode target, TileVisNode parent, ArrayList<Object[]> in) {
        if (cache.size() > 512 || cache.contains(new AuraPos(parent.getLocation().dim(), parent.getBlockPos()))) {
            return in;
        }
        cache.add(new AuraPos(parent.getLocation().dim(), parent.getBlockPos()));
        for (WeakReference<TileVisNode> childWR : parent.getChildren()) {
            TileVisNode child = childWR.get();
            if (child != null && !child.equals(target) && !child.equals(parent)) {
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
        if (!cc1.dim().equals(cc2.dim())) {
            return -1.0F;
        }
        double distance = cc1.pos().distSqr(cc2.pos());
        return (distance > (range * range)) ? -1.0F : (float) distance;
    }
    private static void calculateNearbyNodes(Level level, BlockPos pos) {
        HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = sources.get(level.dimension());
        if (sourcelist == null) {
            sourcelist = new HashMap<>();
            return;
        }
        ArrayList<WeakReference<TileVisNode>> cn = new ArrayList<>();
        AuraPos drainer = new AuraPos(level.dimension(), pos);
        ArrayList<Object[]> nearby = new ArrayList<>();
        for (WeakReference<TileVisNode> root : sourcelist.values()) {
            if (!isNodeValid(root)) {
                continue;
            }
            TileVisNode source = root.get();
            TileVisNode closest = null;
            float range = Float.MAX_VALUE;
            float r = inRange(drainer, source.getLocation(), source.getRange());
            if (r > 0.0F) {
                range = r;
                closest = source;
            }
            ArrayList<WeakReference<TileVisNode>> children = new ArrayList<>();
            children = getAllChildren(source, children);
            for (WeakReference<TileVisNode> child : children) {
                TileVisNode n = child.get();
                if (n != null && !n.equals(root)) {
                    float r2 = inRange(n.getLocation(), drainer, n.getRange());
                    if (r2 > 0.0F && r2 < range) {
                        range = r2;
                        closest = n;
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
        for (WeakReference<TileVisNode> child : source.getChildren()) {
            TileVisNode n = child.get();
            Level level = n != null ? n.getLevel() : null;
            if (n != null && level != null && level.isLoaded(n.getBlockPos())) {
                list.add(child);
                list = getAllChildren(n, list);
            }
        }
        return list;
    }
    public static boolean canNodeBeSeen(Level level, TileVisNode source, TileVisNode target) {
        Vec3 from = new Vec3(source.getBlockPos().getX() + 0.5D,
                source.getBlockPos().getY() + 0.5D, source.getBlockPos().getZ() + 0.5D);
        Vec3 to = new Vec3(target.getBlockPos().getX() + 0.5D,
                target.getBlockPos().getY() + 0.5D, target.getBlockPos().getZ() + 0.5D);
        BlockHitResult mop = level.clip(new ClipContext(from, to,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        return mop == null || mop.getType() == HitResult.Type.MISS
                || (mop.getType() == HitResult.Type.BLOCK && mop.getBlockPos().equals(target.getBlockPos()));
    }
}
