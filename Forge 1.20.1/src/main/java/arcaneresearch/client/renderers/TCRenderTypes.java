package arcaneresearch.client.renderers;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@OnlyIn(Dist.CLIENT)
public final class TCRenderTypes extends RenderStateShard {
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> PORTAL = new ConcurrentHashMap<>();
    private static final Map<String, RenderType> MIRROR = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> OBJ_SOLID = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> OBJ_SOLID_CULLED = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> OBJ_OPAQUE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> GOLEM_GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE_NO_CULL = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> CUTOUT = new ConcurrentHashMap<>();
    private static volatile RenderType ORB_TENDRILS = null;
    private TCRenderTypes() {
        super("arcaneresearch", () -> {
        }, () -> {
        });
    }
    private static RenderType.CompositeState state(ResourceLocation texture,
                                                   RenderStateShard.TransparencyStateShard transparency,
                                                   RenderStateShard.WriteMaskStateShard writeMask) {
        return RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(transparency)
                .setCullState(RenderStateShard.CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setWriteMaskState(writeMask)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .createCompositeState(true);
    }
    public static RenderType additiveTextured(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_additive", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                state(tex, RenderStateShard.LIGHTNING_TRANSPARENCY,
                        RenderStateShard.COLOR_WRITE)));
    }
    public static RenderType additiveTexturedNoCull(ResourceLocation texture) {
        return ADDITIVE_NO_CULL.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_additive_nocull", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType cutoutTextured(ResourceLocation texture) {
        return CUTOUT.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_cutout_textured", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType orbTendrils() {
        RenderType type = ORB_TENDRILS;
        if (type == null) {
            synchronized (TCRenderTypes.class) {
                type = ORB_TENDRILS;
                if (type == null) {
                    type = RenderType.create(
                            "arcaneresearch_orb_tendrils", DefaultVertexFormat.POSITION_COLOR,
                            VertexFormat.Mode.TRIANGLE_FAN, 256, false, false,
                            RenderType.CompositeState.builder()
                                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                                    .setTextureState(RenderStateShard.NO_TEXTURE)
                                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                                    .setCullState(RenderStateShard.CULL)
                                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                                    .createCompositeState(false));
                    ORB_TENDRILS = type;
                }
            }
        }
        return type;
    }
    public static RenderType translucentPortal(ResourceLocation texture) {
        return PORTAL.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_portal", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType objSolid(ResourceLocation texture) {
        return OBJ_SOLID.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_obj_solid", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType objSolidCulled(ResourceLocation texture) {        return OBJ_SOLID_CULLED.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_obj_solid_culled", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType objSolidOpaque(ResourceLocation texture) {
        return OBJ_OPAQUE.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_obj_opaque", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_SOLID_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType golemGhost(ResourceLocation texture) {
        return GOLEM_GHOST.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_golem_ghost", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(true)));
    }
    public static RenderType ghostXray(ResourceLocation texture) {
        return GHOST.computeIfAbsent(texture, tex -> RenderType.create(
                "arcaneresearch_ghost", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .createCompositeState(false)));
    }
    public static RenderType mirrorLayer(ResourceLocation texture, boolean additive) {
        String name = (additive ? "arcaneresearch_mirror_add" : "arcaneresearch_mirror_alpha")
                + texture.getPath();
        return MIRROR.computeIfAbsent(name, key -> RenderType.create(
                key, DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS, 256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                texture, false, false))
                        .setTransparencyState(additive
                                ? RenderStateShard.LIGHTNING_TRANSPARENCY
                                : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.CULL)
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .createCompositeState(false)));
    }
}
