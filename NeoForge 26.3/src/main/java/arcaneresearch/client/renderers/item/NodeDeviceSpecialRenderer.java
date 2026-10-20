package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import arcaneresearch.client.renderers.tile.NodeConverterRenderer;
import arcaneresearch.client.renderers.tile.StabilizerRenderer;
import arcaneresearch.common.items.aura.ItemNodeDevice;

import java.util.function.Consumer;

// NEOFORGE_PORT (26.3): NodeDeviceItemRenderer (BEWLR) -> SpecialModelRenderer.
// Forge renderByItem() body 1:1 (ground lift, center, per-context applyDisplay,
// uncenter, device world offset centering, OBJ device, static pistons, per-kind
// glow, no bubble on the item). The base model (node_device_base) carries no
// display transforms (Forge builtin/entity had none), so the whole Forge pose
// is reproduced here via the "preset" param (submit has no display context),
// wired per-context in the item JSONs (display_context select).
public class NodeDeviceSpecialRenderer implements SpecialModelRenderer<Void> {

    private final ItemNodeDevice.Kind kind;
    private final String preset;

    public NodeDeviceSpecialRenderer(ItemNodeDevice.Kind kind, String preset) {
        this.kind = kind;
        this.preset = preset;
    }

    @Override
    public @Nullable Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0, 0, 0));
        output.accept(new org.joml.Vector3f(1, 1, 1));
    }

    @Override
    public void submit(@Nullable Void arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        if ("ground".equals(preset)) {
            poseStack.translate(0.0D, 0.15D, 0.0D);
        }
        // Центр модели — иначе повороты display уводят девайс вбок.
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyDisplay(preset, poseStack);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        // Центровка по вертикали зависит от кадра девайса: стабилизатор
        // стоит на земле y 0..0.75 (центр 0.375 → +0.125), конвертер/
        // трансдьюсер — y 0.25..1.0 (центр 0.625 → -0.1).
        switch (kind) {
            case ADVANCED -> {
                poseStack.translate(0.0D, 0.125D, 0.0D);
                StabilizerRenderer.renderDevice(poseStack, collector,
                        20, 2, lightCoords, playerTicks(), false);
            }
            case TRANSDUCER, CONVERTER -> {
                poseStack.translate(0.0D, -0.1D, 0.0D);
                NodeConverterRenderer.renderDevice(poseStack, collector,
                        20, 0, lightCoords);
            }
            default -> {
                poseStack.translate(0.0D, 0.125D, 0.0D);
                StabilizerRenderer.renderDevice(poseStack, collector,
                        20, 1, lightCoords, playerTicks(), false);
            }
        }
        poseStack.popPose();
    }

    /** Ванильный display блока: ротации в градусах + скейл под контекст (Forge 1:1). */
    private static void applyDisplay(String preset, PoseStack poseStack) {
        switch (preset) {
            case "gui" -> {
                poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(30.0F));
                poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.625F, 0.625F, 0.625F);
            }
            case "fixed" -> poseStack.scale(0.5F, 0.5F, 0.5F);
            case "ground" -> poseStack.scale(0.25F, 0.25F, 0.25F);
            case "thirdperson" -> {
                poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(75.0F));
                poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.375F, 0.375F, 0.375F);
            }
            case "firstperson_right" -> {
                poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
            }
            case "firstperson_left" -> {
                poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.4F, 0.4F, 0.4F);
            }
            default -> poseStack.scale(0.625F, 0.625F, 0.625F);
        }
    }

    private static int playerTicks() {
        try {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                return net.minecraft.client.Minecraft.getInstance().player.tickCount;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public record Unbaked(String kind, String preset) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.fieldOf("kind").forGetter(Unbaked::kind),
                        Codec.STRING.optionalFieldOf("preset", "gui").forGetter(Unbaked::preset)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            ItemNodeDevice.Kind parsed;
            try {
                parsed = ItemNodeDevice.Kind.valueOf(kind);
            } catch (Exception e) {
                parsed = ItemNodeDevice.Kind.STABILIZER;
            }
            return new NodeDeviceSpecialRenderer(parsed, preset);
        }
    }
}