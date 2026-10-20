package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (26.3): vanilla ZombieRenderer; texture hook now takes State.
public class InhabitedZombieRenderer extends ZombieRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/czombie.png");

    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }

    // NEOFORGE_PORT (26.3): Forge ItemCultistPlateArmor.getArmorTexture returned the zombie
    // plate texture for this entity; the armor layer resolves textures during this submit.
    @Override
    public void submit(ZombieRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        arcaneresearch.client.ArmorClientSetup.inhabitedZombieWearer = true;
        try {
            super.submit(state, poseStack, collector, camera);
        } finally {
            arcaneresearch.client.ArmorClientSetup.inhabitedZombieWearer = false;
        }
    }
}