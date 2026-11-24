package arcaneresearch.client.renderers.item;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Marks jar items as custom-rendered without losing their baked geometry.
 *
 * <p>The jar item models use fill-level overrides. If the wrapper delegated
 * {@code getOverrides()} to the base model, {@code ItemRenderer.getModel()}
 * would resolve them to a plain block model and the custom renderer would
 * never be invoked. Keeping overrides empty preserves the wrapper; the
 * renderer resolves the original overrides itself via {@link #base()}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class JarItemModel extends CustomRendererItemModel {
    public JarItemModel(BakedModel base) {
        super(base);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
