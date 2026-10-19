package arcaneresearch.client.renderers.item;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
@OnlyIn(Dist.CLIENT)
public final class WandTransforms {
	private WandTransforms() {
	}
	public static final ItemTransforms WAND = new ItemTransforms(
			display(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F, 0.55F, 0.55F),
			display(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F, 0.55F, 0.55F),
			display(0.0F, -90.0F, 25.0F, 1.13F, 3.2F, 1.13F, 0.68F, 0.68F, 0.68F),
			display(0.0F, -90.0F, 25.0F, 1.13F, 3.2F, 1.13F, 0.68F, 0.68F, 0.68F),
			display(0.0F, 180.0F, 0.0F, 0.0F, 13.0F, 7.0F, 1.0F, 1.0F, 1.0F),
			display(0.0F, 0.0F, -45.0F, -0.33F, -0.33F, -0.33F, 0.75F, 0.75F, 0.75F),
			display(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.5F, 0.5F, 0.5F),
			display(0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
			ImmutableMap.of());
	public static final ItemTransforms STAFF = new ItemTransforms(
			display(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F, 0.55F, 0.55F),
			display(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F, 0.55F, 0.55F),
			display(0.0F, -90.0F, 25.0F, 1.13F, 3.2F, 1.13F, 0.68F, 0.68F, 0.68F),
			display(0.0F, -90.0F, 25.0F, 1.13F, 3.2F, 1.13F, 0.68F, 0.68F, 0.68F),
			display(0.0F, 180.0F, 0.0F, 0.0F, 13.0F, 7.0F, 1.0F, 1.0F, 1.0F),
			display(0.0F, 0.0F, -45.0F, 0.25F, 0.25F, 0.25F, 0.66F, 0.66F, 0.66F),
			display(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.5F, 0.5F, 0.5F),
			display(0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
			ImmutableMap.of());
	private static ItemTransform display(float rx, float ry, float rz, float tx, float ty, float tz, float sx, float sy, float sz) {
		Vector3f rotation = new Vector3f(rx, ry, rz);
		Vector3f translation = new Vector3f(tx * 0.0625F, ty * 0.0625F, tz * 0.0625F);
		translation.set(Mth.clamp(translation.x, -5.0F, 5.0F), Mth.clamp(translation.y, -5.0F, 5.0F), Mth.clamp(translation.z, -5.0F, 5.0F));
		Vector3f scale = new Vector3f(sx, sy, sz);
		scale.set(Mth.clamp(scale.x, -4.0F, 4.0F), Mth.clamp(scale.y, -4.0F, 4.0F), Mth.clamp(scale.z, -4.0F, 4.0F));
		return new ItemTransform(rotation, translation, scale);
	}
}
