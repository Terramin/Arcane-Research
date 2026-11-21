package arcaneresearch.api.portals;

import net.minecraft.world.phys.Vec3;

public interface IPortalShape {

    Vec3 getPortalPlanePos(float partialTicks);

    float getPortalYaw(float partialTicks);

    Vec3 getFacingNormal(float partialTicks);

    double[] getContourBounds(float partialTicks);
}
