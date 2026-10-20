package arcaneresearch.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;

// NEOFORGE_PORT: new registry (no Forge counterpart; Forge drew custom-geometry FX through
// immediate-mode Particle#render, removed in 26.x). Custom-geometry effects (beams, streams,
// runes, planes) implement BeamEffect below: they tick here at client-tick rate (same as the
// old particle engine) and submit world-space quads via SubmitCustomGeometryEvent
// (the old PARTICLE_SHEET pass equivalent).
public final class BeamEngine {

    public interface BeamEffect {
        /** Advance simulation (old Particle#tick body). Return false when finished. */
        boolean beamTick();

        /**
         * Emit quads with camera-relative coords (same numbers as the old immediate code)
         * into a fresh identity PoseStack (mirrors the old view-space buffer).
         */
        void emitQuads(VertexConsumer consumer, net.minecraft.client.Camera camera, float partialTicks);

        /** RenderType binding this effect's texture (old setShaderTexture + blend state). */
        RenderType beamRenderType();

        /**
         * Frame-exact variant (Forge computed the animated frame once from
         * age+partialTicks and used it for both texture bind and emission).
         * Effects with animated textures override this; the default keeps the
         * static bind so existing effects are untouched.
         */
        default java.util.List<RenderType> beamRenderTypes(float partialTicks) {
            return beamRenderTypes();
        }

        /**
         * All types this effect emits, in emission order. Multi-texture effects
         * (beam body + source glow + impact flash) override this plus the typed
         * emitQuads below; single-texture effects use the defaults.
         */
        default java.util.List<RenderType> beamRenderTypes() {
            return java.util.List.of(beamRenderType());
        }

        /** Emit ONLY quads for the given type (default: everything, single-type effects). */
        default void emitQuads(VertexConsumer consumer, net.minecraft.client.Camera camera,
                float partialTicks, RenderType type) {
            emitQuads(consumer, camera, partialTicks);
        }
    }

    private static final List<BeamEffect> ACTIVE = new ArrayList<>();

    private BeamEngine() {
    }

    // NEOFORGE_PORT (26.3): Forge kept these in the vanilla ParticleEngine, which
    // is cleared on every level change (setLevel); mirror that here.
    private static net.minecraft.client.multiplayer.ClientLevel trackedLevel;

    private static void syncLevel() {
        net.minecraft.client.multiplayer.ClientLevel current = Minecraft.getInstance().level;
        if (current != trackedLevel) {
            ACTIVE.clear();
            trackedLevel = current;
        }
    }

    public static void add(BeamEffect effect) {
        if (effect == null) {
            return;
        }
        synchronized (ACTIVE) {
            syncLevel();
            ACTIVE.add(effect);
        }
    }

    public static void clear() {
        synchronized (ACTIVE) {
            ACTIVE.clear();
        }
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Events {
        record Job(BeamEffect effect, RenderType type) {
        }

        @SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            synchronized (ACTIVE) {
                syncLevel();
                // NEOFORGE_PORT (26.3): same gate as vanilla particleEngine.tick()
                // (frozen while paused or /tick freeze), as in Forge.
                if (mc.level == null || mc.isPaused() || !mc.level.tickRateManager().runsNormally()) {
                    return;
                }
                Iterator<BeamEffect> it = ACTIVE.iterator();
                while (it.hasNext()) {
                    boolean alive;
                    try {
                        alive = it.next().beamTick();
                    } catch (Exception e) {
                        alive = false;
                    }
                    if (!alive) {
                        it.remove();
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onSubmitGeometry(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
            List<BeamEffect> snapshot;
            synchronized (ACTIVE) {
                if (ACTIVE.isEmpty()) {
                    return;
                }
                snapshot = new ArrayList<>(ACTIVE);
            }
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
            // Expand (effect, type) pairs in emission order, then group consecutive same-type.
            java.util.List<Job> jobs = new ArrayList<>();
            for (BeamEffect effect : snapshot) {
                List<RenderType> types;
                try {
                    types = effect.beamRenderTypes(partialTicks);
                } catch (Exception ignored) {
                    continue;
                }
                for (RenderType type : types) {
                    jobs.add(new Job(effect, type));
                }
            }
            RenderType current = null;
            List<Job> batch = new ArrayList<>();
            for (Job job : jobs) {
                if (current != null && !current.equals(job.type())) {
                    submitBatch(event, current, batch, camera, partialTicks);
                    batch.clear();
                }
                current = job.type();
                batch.add(job);
            }
            if (!batch.isEmpty()) {
                submitBatch(event, current, batch, camera, partialTicks);
            }
        }

        private static void submitBatch(
                net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event,
                RenderType type, List<Job> batch,
                net.minecraft.client.Camera camera, float partialTicks) {
            PoseStack poseStack = new PoseStack();
            // NEOFORGE_PORT (deferred-callback fix): submitCustomGeometry runs the
            // lambda at feature-render time, AFTER this method returns (see
            // WandSpecialRenderer.emitWindowed). The caller reuses/clears `batch`,
            // so the lambda must capture a snapshot, not the live list —
            // otherwise all but the last batch emit zero quads (invisible beam).
            List<Job> snapshot = new ArrayList<>(batch);
            event.getSubmitNodeCollector().submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                for (Job job : snapshot) {
                    try {
                        job.effect().emitQuads(consumer, camera, partialTicks, job.type());
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    // NEOFORGE_PORT: RenderType has identity equality; cache per texture so batch
    // grouping and type dispatch (multi-texture effects) compare stable instances.
    private static final java.util.Map<Identifier, RenderType> ADDITIVE_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_NOWRITE_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_NOWRITE_CULL_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Additive textured beam look (old blend 770,1 + emissive): explicit LIGHTNING layer. */
    public static RenderType additiveBeam(Identifier texture) {
        return ADDITIVE_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::additiveTexturedNoCull);
    }

    /** Alpha-blended textured look (old defaultBlendFunc): explicit TRANSLUCENT layer. */
    public static RenderType normalBeam(Identifier texture) {
        return NORMAL_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentPortal);
    }

    /**
     * Alpha-blended textured look with depth write off (Forge defaultBlendFunc +
     * depthMask(false): VoidStream ribbon, no cull).
     */
    public static RenderType normalBeamNoDepth(Identifier texture) {
        return NORMAL_NOWRITE_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentNoDepthWrite);
    }

    /**
     * Alpha-blended textured look with depth write off and cull on (Forge
     * defaultBlendFunc + depthMask(false), cull kept: Sonic cone).
     */
    public static RenderType normalBeamNoDepthCull(Identifier texture) {
        return NORMAL_NOWRITE_CULL_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentNoDepthWriteCull);
    }
}