package arcaneresearch.client.renderers.item;

import arcaneresearch.api.wands.IFocusHostWand;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class WandDisplayModel implements BakedModel {
    private final BakedModel base;
    private final BakedModel staff;
    private final ItemOverrides overrides;

    public WandDisplayModel(BakedModel base, BakedModel staff) {
        this.base = base;
        this.staff = staff;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                if (stack.getItem() instanceof IFocusHostWand wand && wand.isStaff(stack)) {
                    return staff;
                }
                return original;
            }
        };
    }

    @Override
    public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(@Nullable BlockState state,
                                                                              @Nullable Direction side,
                                                                              RandomSource rand) {
        return base.getQuads(state, side, rand);
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
        return true;
    }

    @Override
    public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }
}
