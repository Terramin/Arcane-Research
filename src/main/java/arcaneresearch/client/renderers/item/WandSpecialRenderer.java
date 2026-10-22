package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;





























































public class WandSpecialRenderer implements SpecialModelRenderer<WandSpecialRenderer.WandData> {

    private static final Identifier WAND_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/wand.obj");
    private static final Identifier STAFF_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/staff.obj");

    
    private static final Identifier FALLBACK_ROD =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/rod_wood_mat");
    private static final Identifier FALLBACK_CAP =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/cap_iron_mat");
    private static final Identifier FALLBACK_FOCUS =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/focus");

    
    
    public record WandData(String rodTag, boolean staff, String capTag, boolean sceptre,
                           String focusId, int focusColor) {
    }

    
    
    
    private final boolean staff;

    public WandSpecialRenderer() {
        this(false);
    }

    public WandSpecialRenderer(boolean staff) {
        this.staff = staff;
    }

    
    
    private static volatile TurretObjModel WAND_OBJ = null;
    private static volatile boolean WAND_TRIED = false;
    private static volatile TurretObjModel STAFF_OBJ = null;
    private static volatile boolean STAFF_TRIED = false;

    private static TurretObjModel wandModel() {
        if (!WAND_TRIED) {
            synchronized (WandSpecialRenderer.class) {
                if (!WAND_TRIED) {
                    WAND_TRIED = true;
                    try {
                        
                        
                        WAND_OBJ = TurretObjModel.load(WAND_MODEL, false);
                    } catch (Throwable ignored) {
                        WAND_OBJ = null;
                    }
                }
            }
        }
        return WAND_OBJ;
    }

    private static TurretObjModel staffModel() {
        if (!STAFF_TRIED) {
            synchronized (WandSpecialRenderer.class) {
                if (!STAFF_TRIED) {
                    STAFF_TRIED = true;
                    try {
                        STAFF_OBJ = TurretObjModel.load(STAFF_MODEL, false);
                    } catch (Throwable ignored) {
                        STAFF_OBJ = null;
                    }
                }
            }
        }
        return STAFF_OBJ;
    }

    @Override
    public @Nullable WandData extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return null;
        }
        WandRod rod = wand.getRod(stack);
        WandCap cap = wand.getCap(stack);
        if (rod == null || cap == null) {
            
            
            
            return new WandData("wood", false, "iron", false, "", -1);
        }
        ItemFocusBasic focus = null;
        try {
            focus = wand.getFocus(stack);
        } catch (Exception ignored) {
        }
        String focusId = focus != null ? focus.getFocusId() : "";
        int focusColor = -1;
        if (focus != null) {
            try {
                focusColor = focus.getFocusColor(wand.getFocusStack(stack));
            } catch (Exception ignored) {
                focusColor = -1;
            }
        }
        boolean sceptre = false;
        try {
            sceptre = wand.isSceptre(stack);
        } catch (Exception ignored) {
        }
        return new WandData(rod.getTag(), rod.isStaff(), cap.getTag(), sceptre, focusId, focusColor);
    }

    @Override
    public void getExtents(Consumer<org.joml.Vector3fc> output) {
        
        
        output.accept(new org.joml.Vector3f(0.4F, -0.63F, 0.4F));
        output.accept(new org.joml.Vector3f(0.6F, 1.27F, 0.6F));
    }

    @Override
    public void submit(@Nullable WandData arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (arg == null) {
            return;
        }
        TurretObjModel model = this.staff ? staffModel() : wandModel();
        if (model == null) {
            return;
        }
        WandRod rod = null;
        WandCap cap = null;
        try {
            rod = WandRod.getRod(arg.rodTag());
            cap = WandCap.caps.get(arg.capTag());
        } catch (Exception ignored) {
        }
        Identifier rodTex = rawTexture(rod != null ? rod.getTexture() : null, FALLBACK_ROD);
        Identifier capTex = rawTexture(cap != null ? cap.getTexture() : null, FALLBACK_CAP);
        
        float fr = 1.0F, fg = 1.0F, fb = 1.0F;
        ItemFocusBasic focus = null;
        if (!arg.focusId().isEmpty()) {
            try {
                focus = ItemFocusBasic.foci.get(arg.focusId());
            } catch (Exception ignored) {
            }
        }
        Identifier focusTex = rawTexture(focus != null ? focus.getFocusTexture() : null, FALLBACK_FOCUS);
        if (focus != null && arg.focusColor() != -1) {
            fr = ((arg.focusColor() >> 16) & 0xFF) / 255.0F;
            fg = ((arg.focusColor() >> 8) & 0xFF) / 255.0F;
            fb = (arg.focusColor() & 0xFF) / 255.0F;
        }
        poseStack.pushPose();
        
        
        
        
        
        
        
        try {
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(rodTex), model, "rod",
                    lightCoords, 1.0F, 1.0F, 1.0F, rodTex);
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "cap1",
                    lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "cap2",
                    lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            if (arg.sceptre()) {
                
                
                emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "crossbar",
                        lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            }
            if (!arg.focusId().isEmpty()) {
                
                
                
                
                emitFramed(poseStack, collector, TCRenderTypes.objAlphaFlat(focusTex), model, "focus",
                        lightCoords, fr, fg, fb, focusTex);
            }
            
            
            
        } finally {
            poseStack.popPose();
        }
    }

    
    
    
    private static Identifier rawTexture(@Nullable Identifier id, Identifier fallback) {
        Identifier src = id != null ? id : fallback;
        return Identifier.fromNamespaceAndPath(src.getNamespace(), "textures/" + src.getPath() + ".png");
    }

    
    
    
    
    private record Anim(int frames, int frametime, int[] seq) {
    }

    private static final java.util.Map<Identifier, Anim> ANIM_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static Anim animFor(Identifier tex) {
        Anim cached = ANIM_CACHE.get(tex);
        if (cached != null) {
            return cached;
        }
        Anim resolved = loadAnim(tex);
        ANIM_CACHE.put(tex, resolved);
        return resolved;
    }

    private static Anim loadAnim(Identifier tex) {
        int frames = 1;
        try {
            var res = net.minecraft.client.Minecraft.getInstance().getResourceManager()
                    .getResourceOrThrow(tex);
            try (var in = res.open()) {
                var img = javax.imageio.ImageIO.read(in);
                if (img != null && img.getWidth() > 0 && img.getHeight() > img.getWidth()
                        && img.getHeight() % img.getWidth() == 0) {
                    frames = img.getHeight() / img.getWidth();
                }
            }
        } catch (Exception ignored) {
        }
        int frametime = 1;
        int[] seq = null;
        try {
            var metaId = Identifier.fromNamespaceAndPath(tex.getNamespace(), tex.getPath() + ".mcmeta");
            var res = net.minecraft.client.Minecraft.getInstance().getResourceManager()
                    .getResourceOrThrow(metaId);
            try (var in = res.open();
                 var reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
                var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("animation") && json.get("animation").isJsonObject()) {
                    var anim = json.getAsJsonObject("animation");
                    if (anim.has("frametime")) {
                        frametime = Math.max(1, anim.get("frametime").getAsInt());
                    }
                    if (anim.has("frames") && anim.get("frames").isJsonArray()) {
                        var arr = anim.getAsJsonArray("frames");
                        if (!arr.isEmpty()) {
                            seq = new int[arr.size()];
                            for (int i = 0; i < arr.size(); i++) {
                                var el = arr.get(i);
                                seq[i] = el.isJsonObject() && el.getAsJsonObject().has("index")
                                        ? el.getAsJsonObject().get("index").getAsInt() : el.getAsInt();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (seq != null) {
            int max = 0;
            for (int s : seq) {
                max = Math.max(max, s);
            }
            frames = Math.max(frames, max + 1);
        }
        return new Anim(Math.max(1, frames), Math.max(1, frametime), seq);
    }

    private static int curFrame(Anim anim) {
        int len = anim.seq() != null ? anim.seq().length : anim.frames();
        if (len <= 1) {
            return 0;
        }
        long tick = 0L;
        try {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null) {
                tick = level.getGameTime();
            } else {
                tick = System.currentTimeMillis() / 50L;
            }
        } catch (Exception ignored) {
        }
        int idx = (int) ((tick / Math.max(1, anim.frametime())) % len);
        if (anim.seq() != null) {
            return Math.floorMod(anim.seq()[idx], Math.max(1, anim.frames()));
        }
        return Math.floorMod(idx, Math.max(1, anim.frames()));
    }

    private static void emit(PoseStack poseStack, SubmitNodeCollector collector,
                             net.minecraft.client.renderer.rendertype.RenderType type,
                             TurretObjModel model, String group, int light,
                             float r, float g, float b) {
        emitWindowed(poseStack, collector, type, model, group, light, r, g, b, 0.0F, 1.0F);
    }

    private static void emitWindowed(PoseStack poseStack, SubmitNodeCollector collector,
                                     net.minecraft.client.renderer.rendertype.RenderType type,
                                     TurretObjModel model, String group, int light,
                                     float r, float g, float b,
                                     float vOffset, float vScale) {
        if (!model.hasGroup(group)) {
            return;
        }
        
        
        final float off = vOffset;
        final float scale = vScale;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderGroup(work, consumer, group, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F,
                    off, scale);
        });
    }

    private static void emitFramed(PoseStack poseStack, SubmitNodeCollector collector,
                                   net.minecraft.client.renderer.rendertype.RenderType type,
                                   TurretObjModel model, String group, int light,
                                   float r, float g, float b, Identifier tex) {
        Anim anim = animFor(tex);
        if (anim.frames() <= 1) {
            emit(poseStack, collector, type, model, group, light, r, g, b);
            return;
        }
        int frame = curFrame(anim);
        emitWindowed(poseStack, collector, type, model, group, light, r, g, b,
                frame / (float) anim.frames(), 1.0F / anim.frames());
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<WandData> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<WandData> bake(SpecialModelRenderer.BakingContext context) {
            return new WandSpecialRenderer(false);
        }
    }

    public record StaffUnbaked() implements SpecialModelRenderer.Unbaked<WandData> {
        public static final MapCodec<StaffUnbaked> MAP_CODEC = MapCodec.unit(new StaffUnbaked());

        @Override
        public MapCodec<StaffUnbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<WandData> bake(SpecialModelRenderer.BakingContext context) {
            return new WandSpecialRenderer(true);
        }
    }
}
