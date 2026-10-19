package arcaneresearch.api.items;
import net.minecraft.world.phys.Vec3;
public interface IGogglesDisplayExtended {
    String[] getIGogglesText();
    default Vec3 getIGogglesTextOffset() {
        return new Vec3(0, 0, 0);
    }
}
