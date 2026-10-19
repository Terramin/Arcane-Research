package arcaneresearch.api.casters;
import net.minecraft.world.phys.Vec3;
public class Trajectory {
    public Vec3 source;
    public Vec3 direction;
    public Trajectory(Vec3 source, Vec3 direction) {
        this.source = source;
        this.direction = direction;
    }
    public Vec3 getPoint(double distance) {
        return source.add(direction.scale(distance));
    }
}
