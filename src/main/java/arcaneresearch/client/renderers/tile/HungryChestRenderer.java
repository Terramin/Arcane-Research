package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.devices.TileHungryChest;

@OnlyIn(Dist.CLIENT)
public class HungryChestRenderer implements BlockEntityRenderer<TileHungryChest> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/chesthungry.png");
    private static final RenderType CHEST_TYPE = RenderType.entityCutout(TEXTURE);
    private static final Quaternionf[] FACING_ROTATIONS = new Quaternionf[6];

    static {
        for (Direction dir : Direction.values()) {
            FACING_ROTATIONS[dir.ordinal()] = Axis.YP.rotationDegrees(-dir.toYRot());
        }
    }

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelPart.getChild("bottom");
        this.lid = modelPart.getChild("lid");
        this.lock = modelPart.getChild("lock");
    }

    @Override
    public void render(@NotNull TileHungryChest tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (lid == null || bottom == null || lock == null) return;

        Direction facing = Direction.SOUTH;
        Level level = tile.getLevel();
        if (level != null) {
            BlockState state = tile.getBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction d = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                facing = d;
            }
        }

        float lidAngle = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTicks;
        lidAngle = 1.0f - lidAngle;
        lidAngle = 1.0f - lidAngle * lidAngle * lidAngle;
        lid.xRot = -(lidAngle * ((float) Math.PI / 2.0f));
        lock.xRot = lid.xRot;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(FACING_ROTATIONS[facing.ordinal()]);
            poseStack.translate(-0.5, -0.5, -0.5);
            VertexConsumer vertexConsumer = buffer.getBuffer(CHEST_TYPE);
            lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            lock.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        } finally {
            poseStack.popPose();
        }
    }
}