package arcaneresearch.lib.raytracer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.lib.vec.BlockCoord;
import arcaneresearch.lib.vec.Vector3;

public class ExtendedMOP implements Comparable<ExtendedMOP> {
    public double dist;
    public Vec3 hitVec;
    public Direction sideHit;
    public BlockPos blockPos;
    public Entity entityHit;
    public int subHit;
    public Object hitInfo;

    public ExtendedMOP(Entity entity, Vector3 hit, Object data, double dist) {
        entityHit = entity;
        hitVec = hit.vec3();
        setData(data);
        this.dist = dist;
    }

    public ExtendedMOP(Vector3 hit, int side, BlockCoord pos, Object data, double dist) {
        hitVec = hit.vec3();
        sideHit = (side >= 0) ? Direction.values()[side] : Direction.UP;
        blockPos = pos.pos();
        setData(data);
        this.dist = dist;
    }

    public void setData(Object data) {
        if (data instanceof Integer) {
            subHit = (int) data;
        }
        hitInfo = data;
    }

    public int compareTo(ExtendedMOP o) {
        return (dist == o.dist) ? 0 : ((dist < o.dist) ? -1 : 1);
    }
}
