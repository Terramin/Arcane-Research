package arcaneresearch.client.renderers.block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.obj.MeshLoader;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.lib.obj.MeshPart;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class CrystalModel implements BakedModel {
    public static final ModelProperty<Boolean> UP = new ModelProperty<>();
    public static final ModelProperty<Boolean> DOWN = new ModelProperty<>();
    public static final ModelProperty<Boolean> NORTH = new ModelProperty<>();
    public static final ModelProperty<Boolean> SOUTH = new ModelProperty<>();
    public static final ModelProperty<Boolean> EAST = new ModelProperty<>();
    public static final ModelProperty<Boolean> WEST = new ModelProperty<>();
    public static final ResourceLocation OBJ_LOCATION =
            new ResourceLocation(Arcaneresearch.MODID, "models/obj/crystal.obj");
    public static final ResourceLocation TEXTURE_LOCATION =
            new ResourceLocation(Arcaneresearch.MODID, "block/crystal");
    ResourceLocation model;
    MeshModel sourceMesh;
    TextureAtlasSprite tex;
    public CrystalModel(TextureAtlasSprite tex2) {
        model = OBJ_LOCATION;
        tex = tex2;
        try {
            sourceMesh = new MeshLoader().loadFromResource(model);
            for (MeshPart mp : sourceMesh.parts) {
                mp.tintIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static CrystalModel create() {
        return new CrystalModel(null);
    }
    private static boolean isRockOn(BlockAndTintGetter level, BlockPos neighborPos, Direction side) {
        BlockState neighbor = level.getBlockState(neighborPos);
        return BlockCrystalTC.isValidSupport(neighbor, level, neighborPos, side.getOpposite());
    }
    private static boolean getFlag(ModelData data, ModelProperty<Boolean> property) {
        Boolean value = data.get(property);
        return value != null && value;
    }
    protected TextureAtlasSprite resolveSprite() {
        TextureAtlasSprite result = tex;
        if (result == null) {
            try {
                result = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(TEXTURE_LOCATION);
            } catch (Exception e) {
                result = null;
            }
            tex = result;
        }
        return result;
    }
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        if (level == null || pos == null || !(state.getBlock() instanceof BlockCrystalTC)) {
            return data;
        }
        return ModelData.builder()
                .with(UP, isRockOn(level, pos.above(), Direction.UP))
                .with(DOWN, isRockOn(level, pos.below(), Direction.DOWN))
                .with(NORTH, isRockOn(level, pos.north(), Direction.NORTH))
                .with(SOUTH, isRockOn(level, pos.south(), Direction.SOUTH))
                .with(EAST, isRockOn(level, pos.east(), Direction.EAST))
                .with(WEST, isRockOn(level, pos.west(), Direction.WEST))
                .build();
    }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData data,
                                    @Nullable net.minecraft.client.renderer.RenderType renderType) {
        if (side == null && state != null && state.getBlock() instanceof BlockCrystalTC) {
            List<BakedQuad> ret = new ArrayList<BakedQuad>();
            long baseSeed = rand.nextLong();
            int m = ((BlockCrystalTC) state.getBlock()).getGrowth(state) + 1;
            MeshModel mm = sourceMesh.clone();
            try {
                if (data != null) {
                    if (!getFlag(data, UP) || !getFlag(data, DOWN) || !getFlag(data, EAST)
                            || !getFlag(data, WEST) || !getFlag(data, NORTH) || !getFlag(data, SOUTH)) {
                        if (getFlag(data, UP)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            MeshModel mod = mm.clone();
                            mod.rotate(Math.toRadians(180.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
                            for (BakedQuad quad : mod.bakeModel(getParticleTexture())) {
                                ret.add(quad);
                            }
                        }
                        if (getFlag(data, DOWN)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed + 5L));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            for (BakedQuad quad2 : mm.bakeModel(getParticleTexture())) {
                                ret.add(quad2);
                            }
                        }
                        if (getFlag(data, EAST)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed + 10L));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            MeshModel mod = mm.clone();
                            mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                            mod.rotate(Math.toRadians(270.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f));
                            for (BakedQuad quad : mod.bakeModel(getParticleTexture())) {
                                ret.add(quad);
                            }
                        }
                        if (getFlag(data, WEST)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed + 15L));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            MeshModel mod = mm.clone();
                            mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                            mod.rotate(Math.toRadians(90.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
                            for (BakedQuad quad : mod.bakeModel(getParticleTexture())) {
                                ret.add(quad);
                            }
                        }
                        if (getFlag(data, NORTH)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed + 20L));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            MeshModel mod = mm.clone();
                            mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
                            for (BakedQuad quad : mod.bakeModel(getParticleTexture())) {
                                ret.add(quad);
                            }
                        }
                        if (getFlag(data, SOUTH)) {
                            List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
                            Collections.shuffle(c, new java.util.Random(baseSeed + 25L));
                            mm.parts.clear();
                            for (int a = 0; a < m; ++a) {
                                mm.parts.add(sourceMesh.parts.get(c.get(a)));
                            }
                            MeshModel mod = mm.clone();
                            mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                            mod.rotate(Math.toRadians(180.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f));
                            for (BakedQuad quad : mod.bakeModel(getParticleTexture())) {
                                ret.add(quad);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
            }
            return ret;
        }
        return List.of();
    }
    @Override
    public boolean useAmbientOcclusion() {
        return true;
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
        return resolveSprite();
    }
    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return resolveSprite();
    }
    public TextureAtlasSprite getParticleTexture() {
        return resolveSprite();
    }
    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction,
                                    RandomSource randomSource) {
        return getQuads(state, direction, randomSource, ModelData.EMPTY, null);
    }
}
