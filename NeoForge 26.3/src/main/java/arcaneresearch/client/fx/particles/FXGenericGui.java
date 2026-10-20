package arcaneresearch.client.fx.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;

public class FXGenericGui extends FXGeneric {

    private boolean wasInGui;

    public FXGenericGui(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.wasInGui = Minecraft.getInstance().gui.screen() != null;
    }

    public FXGenericGui(ClientLevel level, double x, double y, double z,
                        double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);
        this.wasInGui = Minecraft.getInstance().gui.screen() != null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.wasInGui && Minecraft.getInstance().gui.screen() == null) {
            this.remove();
        }
    }


    // NEOFORGE_PORT: old render() used 0.1x size; base extract path with override.
    @Override
    public float getQuadSize(float partialTicks) {
        return 0.1f * this.quadSize;
    }
}