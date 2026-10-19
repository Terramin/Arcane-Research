package arcaneresearch.client.renderers.item;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class CustomRendererItemModel implements BakedModel {
    private final BakedModel base;
    public CustomRendererItemModel(BakedModel base) {
        this.base = base;
    }
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand) {
        return this.base.getQuads(state, side, rand);
    }
    @Override
    public boolean useAmbientOcclusion() {
        return this.base.useAmbientOcclusion();
    }
    @Override
    public boolean isGui3d() {
        return this.base.isGui3d();
    }
    @Override
    public boolean usesBlockLight() {
        return this.base.usesBlockLight();
    }
    @Override
    public boolean isCustomRenderer() {
        return true;
    }
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.base.getParticleIcon();
    }
    @Override
    public ItemOverrides getOverrides() {
        return this.base.getOverrides();
    }
    @Override
    public ItemTransforms getTransforms() {
        return this.base.getTransforms();
    }
}
