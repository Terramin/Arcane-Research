package thaumictinker.camo;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

 



 
@OnlyIn(Dist.CLIENT)
public class CamoModel implements BakedModel {

    private final BakedModel base;

    public CamoModel(BakedModel base) {
        this.base = base;
    }

    @Override
    public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        if (state != null && state.getBlock() instanceof CamoBlock && data.has(CamoBlockEntity.CAMO)) {
            BlockState camo = data.get(CamoBlockEntity.CAMO);
            BakedModel camoModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(camo);
            return camoModel.getQuads(camo, side, random, ModelData.EMPTY, renderType);
        }
        return base.getQuads(state, side, random, data, renderType);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return data;
    }

    @Override
    public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource randomSource) {
        return base.getQuads(state, direction, randomSource);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return base.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon(ModelData data) {
        return base.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return base.getOverrides();
    }
}
