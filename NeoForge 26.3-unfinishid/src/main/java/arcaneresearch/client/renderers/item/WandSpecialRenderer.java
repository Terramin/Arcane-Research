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

// NEOFORGE_PORT (26.3): Forge WandMeshModel (baked composite rod+cap+sceptre+focus
// from wand.obj/staff.obj) + WandModelEvents (bakeWandModels, registerWandColors)
// -> SpecialModelRenderer. BakedModel/ItemOverrides item baking is gone in 26.3;
// geometry submits as custom quads, display comes from the minecraft:special base
// (models/item/wand.json, models/item/staff.json carry the full 8-context display
// 1:1 from WandTransforms.java — see below).
//
// Forge display authority (read the original fully):
// WandModelEvents.bakeWandModels REPLACES the baked model with WandMeshModel and
// calls initialize(WandTransforms.WAND/STAFF) — the JSON display block NEVER
// applied (neither the old "thirdperson"/"firstperson" keys nor anything else).
// Effective Forge hand transform = WandTransforms firstperson values
// (rot [0,-90,25], trans [1.13,3.2,1.13]x0.0625, scale 0.68 — the same numbers
// vanilla item/generated uses, confirmed by the model docs).
//
// Display math, VERIFIED against 26.3 sources
// (minecraft-patched-*-sources.jar: ItemTransform.apply, ItemTransforms,
// ItemStackRenderState.LayerRenderState, SpecialModelWrapper, ModelRenderProperties):
// - Forge 1.20.1 final matrix on baked quads q (raw OBJ corner-space coords —
//   MeshModel.bakeQuad emits positions verbatim, no +-0.5) was
//   Display(T*R*S) then ItemRenderer.translate(-0.5): F = D*C*q, C = T(-0.5).
// - Neo 26.3 folds that trailing C INTO ItemTransform.apply
//   (NO_TRANSFORM -> C; listed context -> T*R*S*rightRot*C, rotation as a single
//   rotationXYZ quat, rightRotation zero here), and LayerRenderState.submit
//   applies the base display (ModelRenderProperties from the special "base"
//   model) BEFORE special submit. So the pose entering submit() is already
//   D*C — emitting the same raw corner-space q gives D*C*q = F EXACTLY, in
//   every context. NO extra translate in submit: a T(+0.5) here would yield
//   D*C*P*q = D*q, shifting the wand +0.5 model units (~0.34 blocks in hand),
//   and would disagree with WandTipHelper (which replicates D*C and matches
//   the live render matrix — beam would detach from the crystal).
// Docs (docs.neoforged.net/docs/resources/client/models/items): the special
// "base" is "the parent model to read the particle texture and display
// transformation from" — the 8 WandTransforms slots verbatim in the base
// (left/right hand identical, as in the constants; staff differs only in gui).
// Dead unsplit "thirdperson"/"firstperson" keys stay absent (1.20.1
// deserializer ignored them too). NOTE for the JSON owner: 26.3 adds a 9th
// slot, fixedFromBottom (ON_SHELF); absent -> NO_TRANSFORM (= bare C), so
// wands on shelves render shifted -0.5. Recommend mirroring "fixed" there;
// java cannot fix that (report only).
//
// Forge part-index -> OBJ group mapping (verified group order in both files:
// wand.obj = rod, cap1, cap2, focus, crossbar; staff.obj = rod, cap1, cap2, focus):
//   rod ctor     -> parts[0] (rod) with rod texture (+drop crossbar if !staff)
//   cap ctor     -> parts[1]+parts[2] (cap1, cap2) with cap texture
//   sceptre ctor -> parts[4] (crossbar) with cap texture (wand.obj only)
//   focus ctor   -> parts[3] (focus) with focus texture
// Per-part textures come from WandRod/WandCap/ItemFocusBasic 1:1 (item-style ids
// "arcaneresearch:item/wand/*" -> raw texture path "arcaneresearch:textures/...png").
// The Forge focus tint (registerWandColors, tintIndex 1) is applied here as the
// focus-group vertex color (custom geometry has no tintIndex pipeline).
// Emission uses TCRenderTypes.objSolidFlat (Thaumaturge ENTITY_CUTOUT_FLAT
// pattern: NO_CARDINAL_LIGHTING + no cull): Forge baked wand quads with
// shade=false, so the raw ENTITY_CUTOUT graded side faces by normal and culled
// backfaces, visibly diverging from Forge. hasFoil is intentionally ignored
// (Thaumaturge WandItemSpecialRenderer also ignores it; every other mod
// special renderer does the same): re-emitting the same UV geometry with a
// glint RenderType double-draws over the base and is the prime suspect for the
// "Missing elements in vertex" render-frame crash (foil is always true:
// ItemWand.isFoil() returns true unconditionally).
public class WandSpecialRenderer implements SpecialModelRenderer<WandSpecialRenderer.WandData> {

    private static final Identifier WAND_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/wand.obj");
    private static final Identifier STAFF_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/staff.obj");

    // Forge WandModelEvents.registerWandMesh icon defaults, kept as raw-texture fallbacks.
    private static final Identifier FALLBACK_ROD =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/rod_wood_mat");
    private static final Identifier FALLBACK_CAP =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/cap_iron_mat");
    private static final Identifier FALLBACK_FOCUS =
            Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/focus");

    // Immutable snapshot for model-identity caching (SpecialModelWrapper appends
    // the argument as an identity element; records give equals/hashCode).
    public record WandData(String rodTag, boolean staff, String capTag, boolean sceptre,
                           String focusId, int focusColor) {
    }

    // NEOFORGE_PORT: staff routing is static per item-model branch (items/wand.json
    // select on arcaneresearch:staff); the baked renderer draws its fixed mesh, so
    // staff rods get Forge staffTransforms (staff.json display) 1:1.
    private final boolean staff;

    public WandSpecialRenderer() {
        this(false);
    }

    public WandSpecialRenderer(boolean staff) {
        this.staff = staff;
    }

    // Cache: TurretObjModel.load on a failed resource retries (and log-spams)
    // every frame without a TRIED guard.
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
                        // Forge parity: WandMeshModel sampled raw vt with no mirror
                        // (sprite.getV(vt * 16)), so load flipV=false.
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
            // Forge resolveStack fell back to the base flat model when rod/cap
            // were missing; the special layer has no such fallback, so emit the
            // default wood/iron wand instead of rendering nothing.
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
        // Measured OBJ bounds (wand y -0.125..1.263, staff y -0.625..1.263,
        // x/z 0.4..0.6); covers both variants so the layer never mis-culls.
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
        // Focus tint (Forge registerWandColors, tintIndex 1).
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
        // NEOFORGE_PORT (display-convention change, VERIFIED against 26.3 sources:
        // ItemTransform.apply ends with T(-0.5) — the old ItemRenderer.translate(-0.5)
        // folded in — and LayerRenderState.submit applies the base display BEFORE
        // special submit): the entering pose is already D*C on raw corner-space OBJ
        // coords = Forge final exactly. Emit with NO extra shift (a T(+0.5) here
        // would give D*q, +0.5 model units off Forge and off the WandTipHelper
        // solution, detaching the beam from the crystal).
        try {
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(rodTex), model, "rod",
                    lightCoords, 1.0F, 1.0F, 1.0F, rodTex);
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "cap1",
                    lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "cap2",
                    lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            if (arg.sceptre()) {
                // Forge built the sceptre part only for non-staff wands; staff.obj
                // has no "crossbar" group so the hasGroup guard no-ops there too.
                emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(capTex), model, "crossbar",
                        lightCoords, 1.0F, 1.0F, 1.0F, capTex);
            }
            if (!arg.focusId().isEmpty()) {
                emitFramed(poseStack, collector, TCRenderTypes.objSolidFlat(focusTex), model, "focus",
                        lightCoords, fr, fg, fb, focusTex);
            }
            // NEOFORGE_PORT: no manual foil re-emission (see header): vanilla
            // Shield/Trident handle foil via submitModel glint, never by
            // re-emitting custom geometry; Thaumaturge ignores hasFoil too.
        } finally {
            poseStack.popPose();
        }
    }

    // Forge cap/rod/focus texture ids are item-style ("arcaneresearch:item/...",
    // resolved via the stitched atlas at bake); direct RenderType textures need
    // the raw path ("arcaneresearch:textures/...png").
    private static Identifier rawTexture(@Nullable Identifier id, Identifier fallback) {
        Identifier src = id != null ? id : fallback;
        return Identifier.fromNamespaceAndPath(src.getNamespace(), "textures/" + src.getPath() + ".png");
    }

    // NEOFORGE_PORT (animated strip parity): Forge atlas sprites showed one
    // 16x16 frame (mcmeta animation); raw sampling shows the whole strip
    // squashed (rod_blaze_mat 16x320, rod_primal 32x640, thaumium/void caps,
    // animated foci). Crop emission to the live frame (f/n, 1/n).
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
        // Capture the frame window as lambda finals (the geometry callback
        // runs at feature-render time, after emit returns).
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
