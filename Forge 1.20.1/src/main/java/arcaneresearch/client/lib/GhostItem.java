package arcaneresearch.client.lib;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
@OnlyIn(Dist.CLIENT)
public final class GhostItem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMPOSITE_CLASS_NAME = "net.minecraft.client.renderer.RenderType$CompositeRenderType";
    private static final String COMPOSITE_STATE_CLASS_NAME = "net.minecraft.client.renderer.RenderType$CompositeState";
    private GhostItem() {
    }
    public static void render(ItemStack stack, ItemDisplayContext context, float alpha,
                              PoseStack poseStack, MultiBufferSource bufferSource,
                              Level level, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        GhostAdapter adapter = new GhostAdapter(bufferSource, alpha);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, adapter, level, 0);
        adapter.flush();
    }
    private static final class GhostAdapter implements MultiBufferSource {
        private final MultiBufferSource parent;
        private final float alpha;
        private final Set<RenderType> issued = new LinkedHashSet<>();
        GhostAdapter(MultiBufferSource parent, float alpha) {
            this.parent = parent;
            this.alpha = alpha;
        }
        @Override
        public VertexConsumer getBuffer(RenderType type) {
            RenderType ghost = GhostTypes.ghostify(type);
            this.issued.add(ghost);
            return new ScaledAlphaConsumer(parent.getBuffer(ghost), alpha);
        }
        void flush() {
            if (parent instanceof MultiBufferSource.BufferSource bufferSource) {
                for (RenderType type : issued) {
                    bufferSource.endBatch(type);
                }
            }
            issued.clear();
        }
    }
    private static final class ScaledAlphaConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alphaScale;
        static final float TINT_R = 0.85F;
        static final float TINT_G = 0.72F;
        static final float TINT_B = 1.0F;
        ScaledAlphaConsumer(VertexConsumer delegate, float alphaScale) {
            this.delegate = delegate;
            this.alphaScale = alphaScale;
        }
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return delegate.vertex(x, y, z);
        }
        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return delegate.color((int) (r * TINT_R), (int) (g * TINT_G), (int) (b * TINT_B),
                    (int) (a * alphaScale));
        }
        @Override
        public VertexConsumer uv(float u, float v) {
            return delegate.uv(u, v);
        }
        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return delegate.overlayCoords(u, v);
        }
        @Override
        public VertexConsumer uv2(int u, int v) {
            return delegate.uv2(u, v);
        }
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return delegate.normal(x, y, z);
        }
        @Override
        public void endVertex() {
            delegate.endVertex();
        }
        @Override
        public void vertex(float x, float y, float z, float r, float g, float b, float a,
                           float u, float v, int overlay, int light, float nx, float ny, float nz) {
            delegate.vertex(x, y, z,
                    r * TINT_R, g * TINT_G, b * TINT_B, a * alphaScale,
                    u, v, overlay, light, nx, ny, nz);
        }
        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor((int) (r * TINT_R), (int) (g * TINT_G), (int) (b * TINT_B),
                    (int) (a * alphaScale));
        }
        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
        @Override
        public VertexConsumer color(float r, float g, float b, float a) {
            return delegate.color(r * TINT_R, g * TINT_G, b * TINT_B, a * alphaScale);
        }
    }
    private static final class GhostTypes {
        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();
        private static boolean loggedFailure = false;
        static RenderType ghostify(RenderType base) {
            if (base != null && isComposite(base)) {
                RenderType ghost = CACHE.get(base);
                if (ghost == null) {
                    ghost = buildGhost(base);
                    CACHE.put(base, ghost);
                }
                return ghost;
            }
            return base;
        }
        private static boolean isComposite(RenderType type) {
            return type.getClass().getName().equals(COMPOSITE_CLASS_NAME);
        }
        private static RenderType buildGhost(RenderType base) {
            try {
                Object baseState = compositeState(base);
                if (baseState == null) {
                    return fallback(base);
                }
                boolean worldFormat = instanceField(base, VertexFormat.class) == DefaultVertexFormat.BLOCK;
                RenderType ghost = worldFormat ? buildEntityGhost(base, baseState) : buildTransparencySwapGhost(base, baseState);
                return ghost != null ? ghost : fallback(base);
            } catch (Throwable t) {
                return fallback(base);
            }
        }
        private static RenderType buildTransparencySwapGhost(RenderType base, Object baseState) {
            try {
                Object lightningState = compositeState(RenderType.lightning());
                if (lightningState == null) {
                    return null;
                }
                Constructor<?> ctor = compositeStateCtor();
                if (ctor == null) {
                    return null;
                }
                ctor.setAccessible(true);
                Class<?>[] paramTypes = ctor.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = shard(baseState, paramTypes[i]);
                }
                for (int i = 0; i < paramTypes.length; i++) {
                    if (RenderStateShard.TransparencyStateShard.class.isAssignableFrom(paramTypes[i])) {
                        Object lightningShard = shard(lightningState, paramTypes[i]);
                        if (lightningShard != null) {
                            args[i] = lightningShard;
                        }
                        break;
                    }
                }
                return createGhostType(base, base, ctor.newInstance(args));
            } catch (Throwable t) {
                return null;
            }
        }
        private static RenderType buildEntityGhost(RenderType base, Object baseState) {
            try {
                RenderType template = RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
                Object templateState = compositeState(template);
                Object lightningState = compositeState(RenderType.lightning());
                if (templateState == null || lightningState == null) {
                    return null;
                }
                Constructor<?> ctor = compositeStateCtor();
                if (ctor == null) {
                    return null;
                }
                ctor.setAccessible(true);
                Class<?>[] paramTypes = ctor.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    if (RenderStateShard.TextureStateShard.class.isAssignableFrom(paramTypes[i])) {
                        args[i] = shard(baseState, paramTypes[i]);
                    } else if (RenderStateShard.TransparencyStateShard.class.isAssignableFrom(paramTypes[i])) {
                        args[i] = shard(lightningState, paramTypes[i]);
                    } else {
                        args[i] = shard(templateState, paramTypes[i]);
                    }
                }
                return createGhostType(base, template, ctor.newInstance(args));
            } catch (Throwable t) {
                return null;
            }
        }
        private static RenderType createGhostType(RenderType base, Object formatHolder, Object ghostState) {
            Method create = createMethod();
            if (create == null) {
                return null;
            }
            try {
                return (RenderType) create.invoke(null,
                        base + "_tc_ghost",
                        instanceField(formatHolder, VertexFormat.class),
                        instanceField(formatHolder, VertexFormat.Mode.class),
                        singleIntField(base),
                        false, false,
                        ghostState);
            } catch (Throwable t) {
                return null;
            }
        }
        private static RenderType fallback(RenderType base) {
            if (!loggedFailure) {
                loggedFailure = true;
                LOGGER.warn("arcaneresearch: ghost render type unavailable, falling back to opaque render", new Throwable());
            }
            return base;
        }
        private static Object compositeState(Object composite) {
            for (Method method : composite.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == 0
                        && method.getReturnType().getName().equals(COMPOSITE_STATE_CLASS_NAME)) {
                    try {
                        method.setAccessible(true);
                        return method.invoke(composite);
                    } catch (Throwable ignored) {
                    }
                }
            }
            return null;
        }
        private static Constructor<?> compositeStateCtor() throws ClassNotFoundException {
            Class<?> stateClass = Class.forName(COMPOSITE_STATE_CLASS_NAME);
            for (Constructor<?> ctor : stateClass.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 14) {
                    return ctor;
                }
            }
            return null;
        }
        private static Method createMethod() {
            for (Method method : RenderType.class.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                Class<?>[] pt = method.getParameterTypes();
                if (pt.length == 7
                        && pt[0] == String.class
                        && pt[1] == VertexFormat.class
                        && pt[2] == VertexFormat.Mode.class
                        && pt[3] == int.class
                        && pt[4] == boolean.class
                        && pt[5] == boolean.class
                        && pt[6].getName().equals(COMPOSITE_STATE_CLASS_NAME)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            return null;
        }
        private static Object instanceField(Object holder, Class<?> fieldType) throws ReflectiveOperationException {
            for (Class<?> klass = holder.getClass(); klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                for (Field field : klass.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && field.getType() == fieldType) {
                        field.setAccessible(true);
                        return field.get(holder);
                    }
                }
            }
            throw new NoSuchFieldException(fieldType.getName());
        }
        private static Object singleIntField(Object holder) throws ReflectiveOperationException {
            for (Class<?> klass = holder.getClass(); klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                for (Field field : klass.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                        field.setAccessible(true);
                        return field.get(holder);
                    }
                }
            }
            throw new NoSuchFieldException("int (bufferSize)");
        }
        private static Object shard(Object state, Class<?> shardType) throws ReflectiveOperationException {
            for (Field field : state.getClass().getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getType() == shardType) {
                    field.setAccessible(true);
                    return field.get(state);
                }
            }
            throw new NoSuchFieldException(shardType.getName());
        }
    }
}
