package arcaneresearch.lib.raytracer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import arcaneresearch.lib.math.MathHelper;
import arcaneresearch.lib.vec.BlockCoord;
import arcaneresearch.lib.vec.Cuboid6;
import arcaneresearch.lib.vec.Vector3;
import java.util.List;
public class RayTracer {
    private static ThreadLocal<RayTracer> t_inst;
    static {
        RayTracer.t_inst = new ThreadLocal<RayTracer>();
    }
    private Vector3 vec;
    private Vector3 vec2;
    private Vector3 s_vec;
    private double s_dist;
    private int s_side;
    private IndexedCuboid6 c_cuboid;
    public RayTracer() {
        vec = new Vector3();
        vec2 = new Vector3();
        s_vec = new Vector3();
    }
    public static RayTracer instance() {
        RayTracer inst = RayTracer.t_inst.get();
        if (inst == null) {
            RayTracer.t_inst.set(inst = new RayTracer());
        }
        return inst;
    }
    public static BlockHitResult retraceBlock(Level level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Vec3 headVec = getCorrectedHeadVec(player);
        Vec3 lookVec = player.getLookAngle();
        double reach = getBlockReachDistance(player);
        Vec3 endVec = headVec.add(lookVec.scale(reach));
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return null;
        }
        BlockHitResult hit = shape.clip(headVec, endVec, pos);
        return hit.getType() == HitResult.Type.MISS ? null : hit;
    }
    private static double getBlockReachDistance_server(ServerPlayer player) {
        return player.getAttributeValue(ForgeMod.BLOCK_REACH.get());
    }
    @OnlyIn(Dist.CLIENT)
    private static double getBlockReachDistance_client() {
        return Minecraft.getInstance().gameMode.getPickRange();
    }
    public static BlockHitResult retrace(Player player) {
        return retrace(player, getBlockReachDistance(player));
    }
    public static BlockHitResult retrace(Player player, double reach) {
        Vec3 headVec = getCorrectedHeadVec(player);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = headVec.add(lookVec.scale(reach));
        return player.level().clip(new ClipContext(headVec, endVec,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }
    public static Vec3 getCorrectedHeadVec(Player player) {
        Vec3 v = player.getEyePosition();
        if (!player.level().isClientSide && player instanceof ServerPlayer && player.isShiftKeyDown()) {
            v = v.subtract(0.0, 0.08, 0.0);
        }
        return v;
    }
    public static Vec3 getStartVec(Player player) {
        return getCorrectedHeadVec(player);
    }
    public static double getBlockReachDistance(Player player) {
        return player.level().isClientSide ? getBlockReachDistance_client()
                : ((player instanceof ServerPlayer serverPlayer) ? getBlockReachDistance_server(serverPlayer) : 5.0);
    }
    public static Vec3 getEndVec(Player player) {
        Vec3 headVec = getCorrectedHeadVec(player);
        Vec3 lookVec = player.getLookAngle();
        double reach = getBlockReachDistance(player);
        return headVec.add(lookVec.scale(reach));
    }
    private void traceSide(int side, Vector3 start, Vector3 end, Cuboid6 cuboid) {
        vec.set(start);
        Vector3 hit = null;
        switch (side) {
            case 0: {
                hit = vec.XZintercept(end, cuboid.min.y);
                break;
            }
            case 1: {
                hit = vec.XZintercept(end, cuboid.max.y);
                break;
            }
            case 2: {
                hit = vec.XYintercept(end, cuboid.min.z);
                break;
            }
            case 3: {
                hit = vec.XYintercept(end, cuboid.max.z);
                break;
            }
            case 4: {
                hit = vec.YZintercept(end, cuboid.min.x);
                break;
            }
            case 5: {
                hit = vec.YZintercept(end, cuboid.max.x);
                break;
            }
        }
        if (hit == null) {
            return;
        }
        switch (side) {
            case 0:
            case 1: {
                if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z)) {
                    return;
                }
                break;
            }
            case 2:
            case 3: {
                if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y)) {
                    return;
                }
                break;
            }
            case 4:
            case 5: {
                if (!MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z)) {
                    return;
                }
                break;
            }
        }
        double dist = vec2.set(hit).subtract(start).magSquared();
        if (dist < s_dist) {
            s_side = side;
            s_dist = dist;
            s_vec.set(vec);
        }
    }
    private boolean rayTraceCuboid(Vector3 start, Vector3 end, Cuboid6 cuboid) {
        s_dist = Double.MAX_VALUE;
        s_side = -1;
        for (int i = 0; i < 6; ++i) {
            traceSide(i, start, end, cuboid);
        }
        return s_side >= 0;
    }
    public ExtendedMOP rayTraceCuboid(Vector3 start, Vector3 end, Cuboid6 cuboid, BlockCoord pos, Object data) {
        return rayTraceCuboid(start, end, cuboid) ? new ExtendedMOP(s_vec, s_side, pos, data, s_dist) : null;
    }
    public ExtendedMOP rayTraceCuboid(Vector3 start, Vector3 end, Cuboid6 cuboid, Entity entity, Object data) {
        return rayTraceCuboid(start, end, cuboid) ? new ExtendedMOP(entity, s_vec, data, s_dist) : null;
    }
    public void rayTraceCuboids(Vector3 start, Vector3 end, List<IndexedCuboid6> cuboids, BlockCoord pos, Block block, List<ExtendedMOP> hitList) {
        for (IndexedCuboid6 cuboid : cuboids) {
            ExtendedMOP mop = rayTraceCuboid(start, end, cuboid, pos, cuboid.data);
            if (mop != null) {
                hitList.add(mop);
            }
        }
    }
}
