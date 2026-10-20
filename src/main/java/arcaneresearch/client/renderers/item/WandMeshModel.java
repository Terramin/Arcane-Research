package arcaneresearch.client.renderers.item;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.client.lib.obj.MeshLoader;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.lib.obj.MeshPart;
import java.util.*;
@OnlyIn(Dist.CLIENT)
public class WandMeshModel implements BakedModel {
    private static final Map<Direction, List<BakedQuad>> EMPTY_FACE_QUADS = new EnumMap<>(Direction.class);
    public static Map<String, BakedModel> cache = new java.util.concurrent.ConcurrentHashMap<>();
    public static volatile ItemTransforms staffTransforms = null;
    static Map<String, WandMeshModel> variants = new java.util.concurrent.ConcurrentHashMap<>();
    static {
        for (Direction dir : Direction.values()) {
            EMPTY_FACE_QUADS.put(dir, new ArrayList<>());
        }
    }
    String variant;
    ResourceLocation model;
    ItemTransforms transforms;
    List<BakedQuad> faceQuads;
    List<BakedQuad> generalQuads;
    MeshModel sourceMesh;
    TextureAtlasSprite iconSprite;
    private ResourceLocation iconPath;
    private volatile boolean baked = false;
    public WandMeshModel(String variant, WandCap cap) {
        setup(variant, false);
        this.sourceMesh.parts.get(4).material.DiffuseTextureMap = cap.getTexture().toString();
        this.sourceMesh.parts.remove(3);
        this.sourceMesh.parts.remove(2);
        this.sourceMesh.parts.remove(1);
        this.sourceMesh.parts.remove(0);
        variants.put(variant, this);
    }
    public WandMeshModel(String variant, ItemFocusBasic focus) {
        setup(variant, false);
        this.sourceMesh.parts.remove(4);
        this.sourceMesh.parts.get(3).material.DiffuseTextureMap = focus.getFocusTexture().toString();
        this.sourceMesh.parts.remove(2);
        this.sourceMesh.parts.remove(1);
        this.sourceMesh.parts.remove(0);
        variants.put(variant, this);
    }
    public WandMeshModel(String variant, WandRod rod) {
        setup(variant, rod.isStaff());
        this.sourceMesh.parts.get(0).material.DiffuseTextureMap = rod.getTexture().toString();
        if (!rod.isStaff()) {
            this.sourceMesh.parts.remove(4);
        }
        this.sourceMesh.parts.remove(3);
        this.sourceMesh.parts.remove(2);
        this.sourceMesh.parts.remove(1);
        variants.put(variant, this);
    }
    public WandMeshModel(String variant, WandCap cap, boolean staff) {
        setup(variant, staff);
        this.sourceMesh.parts.get(1).material.DiffuseTextureMap = cap.getTexture().toString();
        this.sourceMesh.parts.get(2).material.DiffuseTextureMap = cap.getTexture().toString();
        if (!staff) {
            this.sourceMesh.parts.remove(4);
        }
        this.sourceMesh.parts.remove(3);
        this.sourceMesh.parts.remove(0);
        variants.put(variant, this);
    }
    public WandMeshModel(String itemName) {
        setup(itemName, false);
    }
    static List<BakedQuad> bakePerPart(MeshModel mesh) {
        List<BakedQuad> out = new ArrayList<>();
        if (mesh == null || mesh.parts == null) {
            return out;
        }
        for (MeshPart part : mesh.parts) {
            TextureAtlasSprite sprite = null;
            try {
                if (part.material != null) {
                    if (part.material.DiffuseTextureMap != null) {
                        sprite = spriteOrNull(part.material.DiffuseTextureMap);
                    } else if (part.material.AmbientTextureMap != null) {
                        sprite = spriteOrNull(part.material.AmbientTextureMap);
                    }
                }
            } catch (Exception ignored) {
            }
            MeshModel single = new MeshModel();
            single.positions = mesh.positions;
            single.normals = mesh.normals;
            single.texCoords = mesh.texCoords;
            single.parts = new ArrayList<>();
            single.parts.add(part);
            try {
                out.addAll(single.bakeModel(sprite));
            } catch (Exception ignored) {
            }
        }
        return out;
    }
    private static TextureAtlasSprite spriteOrNull(String name) {
        try {
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(new ResourceLocation(name));
        } catch (Exception e) {
            return null;
        }
    }
    public void setup(String variant, boolean staff) {
        this.variant = variant;
        if (staff) {
            this.model = new ResourceLocation("arcaneresearch", "models/obj/staff.obj");
        } else {
            this.model = new ResourceLocation("arcaneresearch", "models/obj/wand.obj");
        }
        this.faceQuads = new ArrayList<>();
        this.generalQuads = new ArrayList<>();
        try {
            this.generalQuads.clear();
            this.sourceMesh = new MeshLoader().loadFromResource(this.model);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    public void initialize(ItemTransforms cameraTransforms, ResourceLocation icon) {
        this.transforms = cameraTransforms;
        this.iconPath = icon;
    }
    private synchronized void ensureBaked() {
        if (this.baked) {
            return;
        }
        this.baked = true;
        if (this.iconPath != null) {
            try {
                this.iconSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(this.iconPath);
            } catch (Exception e) {
                this.iconSprite = null;
            }
        }
        if (this.sourceMesh != null) {
            this.generalQuads = bakePerPart(this.sourceMesh);
        }
    }
    public BakedModel resolveStack(ItemStack stack) {
        ensureBaked();
        if (!(stack.getItem() instanceof IWand wand)) {
            return this;
        }
        WandRod rod = wand.getRod(stack);
        WandCap cap = wand.getCap(stack);
        if (rod == null || cap == null) {
            return this;
        }
        ItemFocusBasic focus = wand.getFocus(stack);
        String cacheKey = (rod.isStaff() ? "staff" : (wand.isSceptre(stack) ? "sceptre" : "wand")) + rod.getTag()
                + cap.getTag();
        if (focus != null) {
            cacheKey = cacheKey + focus.getFocusId();
        }
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        List<BakedQuad> quads = Lists.newArrayList();
        String key = (rod.isStaff() ? "staff" : "wand") + rod.getTag();
        if (!variants.containsKey(key)) {
            ensureBaked();
            return this;
        }
        WandMeshModel rodModel = variants.get(key);
        rodModel.ensureBaked();
        quads.addAll(rodModel.generalQuads);
        key = (rod.isStaff() ? "staff" : "wand") + cap.getTag();
        if (!variants.containsKey(key)) {
            ensureBaked();
            return this;
        }
        WandMeshModel capModel = variants.get(key);
        capModel.ensureBaked();
        quads.addAll(capModel.generalQuads);
        if (wand.isSceptre(stack)) {
            key = "sceptre" + cap.getTag();
            if (!variants.containsKey(key)) {
                ensureBaked();
                return this;
            }
            WandMeshModel sceptreModel = variants.get(key);
            sceptreModel.ensureBaked();
            quads.addAll(sceptreModel.generalQuads);
        }
        if (focus != null) {
            key = "focus" + focus.getFocusId();
            if (!variants.containsKey(key)) {
                ensureBaked();
                return this;
            }
            WandMeshModel focusModel = variants.get(key);
            focusModel.ensureBaked();
            quads.addAll(focusModel.generalQuads);
        }
        BakedModel model = new SimpleBakedModel(quads,
                EMPTY_FACE_QUADS, false, false, false,
                this.iconSprite, rod.isStaff() && staffTransforms != null ? staffTransforms : this.transforms,
                ItemOverrides.EMPTY);
        if (model != null) {
            cache.put(cacheKey, model);
        }
        return model;
    }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource rand) {
        ensureBaked();
        return face != null ? this.faceQuads : this.generalQuads;
    }
    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }
    @Override
    public boolean isGui3d() {
        return false;
    }
    @Override
    public boolean usesBlockLight() {
        return false;
    }
    @Override
    public boolean isCustomRenderer() {
        return false;
    }
    @Override
    public TextureAtlasSprite getParticleIcon() {
        ensureBaked();
        if (this.iconSprite == null) {
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
        }
        return this.iconSprite;
    }
    @Override
    public ItemOverrides getOverrides() {
        return WandOverrides.INSTANCE;
    }
    @Override
    public ItemTransforms getTransforms() {
        return this.transforms != null ? this.transforms : ItemTransforms.NO_TRANSFORMS;
    }
    private static class WandOverrides extends ItemOverrides {
        static final WandOverrides INSTANCE = new WandOverrides();
        @Override
        public BakedModel resolve(BakedModel original, ItemStack stack,
                                  @Nullable net.minecraft.client.multiplayer.ClientLevel level,
                                  @Nullable net.minecraft.world.entity.LivingEntity entity, int seed) {
            if (stack.getItem() instanceof IWand && original instanceof WandMeshModel wandModel) {
                return wandModel.resolveStack(stack);
            }
            return original;
        }
    }
}
