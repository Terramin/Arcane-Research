package arcaneresearch.api.casters;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
public abstract class FocusEffect extends FocusNode {
    @Override
    public EnumUnitType getType() {
        return EnumUnitType.EFFECT;
    }
    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[] { EnumSupplyType.TARGET };
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return null;
    }
    public abstract boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num);
    public float getDamageForDisplay(float finalPower) {
        return 0;
    }
    public abstract void renderParticleFX(Level level, double posX, double posY, double posZ,
                                          double motionX, double motionY, double motionZ);
    public void onCast(Entity caster) {
    }
}
