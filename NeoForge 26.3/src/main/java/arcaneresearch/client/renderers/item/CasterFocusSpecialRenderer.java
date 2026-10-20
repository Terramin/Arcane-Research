package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.jspecify.annotations.Nullable;
import arcaneresearch.common.items.casters.ItemCaster;

import java.util.function.Consumer;

// NEOFORGE_PORT (26.3): CasterItemRenderer (BEWLR) -> SpecialModelRenderer.
// Used as an overlay in a composite item model (base gauntlet mesh + focus).
// Forge renderFocusOnGauntlet() offsets 1:1; per-context branch via the
// "preset" param (submit has no display context): gui / ground / fixed / hand.
public class CasterFocusSpecialRenderer implements SpecialModelRenderer<ItemStackRenderState> {

    private final String preset;

    public CasterFocusSpecialRenderer(String preset) {
        this.preset = preset;
    }

    @Override
    public @Nullable ItemStackRenderState extractArgument(ItemStack stack) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack == null || focusStack.isEmpty()) {
            return null;
        }
        try {
            ItemStackRenderState state = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                    state, focusStack, ItemDisplayContext.FIXED,
                    Minecraft.getInstance().level, null, 0);
            return state;
        } catch (Exception e) {
            return null;
        }
    }

    private static ItemStack getFocusStack(ItemStack casterStack) {
        if (casterStack.getItem() instanceof ItemCaster caster) {
            return caster.getFocusStack(casterStack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void getExtents(Consumer<org.joml.Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0, 0, 0));
        output.accept(new org.joml.Vector3f(1, 1, 1));
    }

    @Override
    public void submit(@Nullable ItemStackRenderState focus, PoseStack poseStack,
                       SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (focus == null || focus.isEmpty()) {
            return;
        }
        poseStack.pushPose();

        // NEOFORGE_PORT: Forge per-displayContext branches (verbatim numbers);
        // preset wired per-context in the item JSON (display_context select).
        switch (preset) {
            case "firstperson" -> {
                poseStack.translate(0.0625f, -0.125f, 0.0625f);
                poseStack.rotate(Axis.XP.rotationDegrees(-90));
                poseStack.rotate(Axis.ZP.rotationDegrees(180));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case "thirdperson" -> {
                poseStack.translate(0.125f, -0.0625f, 0.3125f);
                poseStack.rotate(Axis.XP.rotationDegrees(-90));
                poseStack.rotate(Axis.ZP.rotationDegrees(180));
                poseStack.scale(0.6f, 0.6f, 0.6f);
            }
            case "ground", "fixed" -> {
                poseStack.translate(0.0f, -0.5f, 0.0f);
                poseStack.rotate(Axis.YP.rotationDegrees(180));
                poseStack.rotate(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
            case "gui" -> {
                poseStack.translate(-0.375f, -0.3125f, 0.0f);
                poseStack.rotate(Axis.YP.rotationDegrees(180));
                poseStack.rotate(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.75f, 0.75f, 0.75f);
            }
            default -> {
                // NEOFORGE_PORT: Forge default branch (HEAD and others).
                float cx = 8.0f / 16.0f;
                float cy = 4.5f / 16.0f;
                float cz = 10.25f / 16.0f;
                poseStack.translate(cx, cy, cz);
                poseStack.rotate(Axis.XP.rotationDegrees(90));
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
        }

        focus.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, outlineColor);

        poseStack.popPose();
    }

    public record Unbaked(String preset) implements SpecialModelRenderer.Unbaked<ItemStackRenderState> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("preset", "gui").forGetter(Unbaked::preset)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<ItemStackRenderState> bake(SpecialModelRenderer.BakingContext context) {
            return new CasterFocusSpecialRenderer(preset);
        }
    }
}