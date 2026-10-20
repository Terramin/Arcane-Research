package arcaneresearch.client.models.block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.renderers.block.CrystalModel;
import javax.annotation.Nullable;
@OnlyIn(Dist.CLIENT)
public class CrystalBakedModel extends CrystalModel {
    public CrystalBakedModel(@Nullable TextureAtlasSprite sprite) {
        super(sprite);
    }
    public static CrystalBakedModel create() {
        return new CrystalBakedModel(null);
    }
}
