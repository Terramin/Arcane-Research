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






public final class BeamEngine {

    public interface BeamEffect {
          
        boolean beamTick();

         


 
        void emitQuads(VertexConsumer consumer, net.minecraft.client.Camera camera, float partialTicks);

          
        RenderType beamRenderType();

         




 
        default java.util.List<RenderType> beamRenderTypes(float partialTicks) {
            return beamRenderTypes();
        }

         



 
        default java.util.List<RenderType> beamRenderTypes() {
            return java.util.List.of(beamRenderType());
        }

          
        default void emitQuads(VertexConsumer consumer, net.minecraft.client.Camera camera,
                float partialTicks, RenderType type) {
            emitQuads(consumer, camera, partialTicks);
        }
    }

    private static final List<BeamEffect> ACTIVE = new ArrayList<>();

    private BeamEngine() {
    }

    
    
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

    
    
    private static final java.util.Map<Identifier, RenderType> ADDITIVE_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_NOWRITE_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Identifier, RenderType> NORMAL_NOWRITE_CULL_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

      
    public static RenderType additiveBeam(Identifier texture) {
        return ADDITIVE_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::additiveTexturedNoCull);
    }

      
    public static RenderType normalBeam(Identifier texture) {
        return NORMAL_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentPortal);
    }

     


 
    public static RenderType normalBeamNoDepth(Identifier texture) {
        return NORMAL_NOWRITE_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentNoDepthWrite);
    }

     


 
    public static RenderType normalBeamNoDepthCull(Identifier texture) {
        return NORMAL_NOWRITE_CULL_CACHE.computeIfAbsent(texture,
                arcaneresearch.client.renderers.TCRenderTypes::translucentNoDepthWriteCull);
    }
}