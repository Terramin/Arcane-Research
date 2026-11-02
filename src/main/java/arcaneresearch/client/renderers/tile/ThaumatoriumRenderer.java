package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ThaumatoriumRenderer implements BlockEntityRenderer<TileThaumatorium> {
    private final ItemRenderer itemRenderer;

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(@NotNull TileThaumatorium tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        ArrayList<Integer> recipeHash = tile.recipeHash;
        if (recipeHash == null || recipeHash.isEmpty()) return;

        Direction facing = Direction.NORTH;
        BlockState state = tile.getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        Minecraft mc = Minecraft.getInstance();
        Entity camera = mc.getCameraEntity();
        int ticks = camera != null ? camera.tickCount : 0;
        float time = ticks + partialTicks;
        int hashSize = recipeHash.size();
        Integer hashBoxed = recipeHash.get((ticks / 20) % hashSize);
        if (hashBoxed == null) return;
        int hash = hashBoxed;
        CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(hash);
        if (recipe == null) return;
        ItemStack output = recipe.getRecipeOutput();
        if (output == null || output.isEmpty()) return;
        ItemStack display = output.copy();
        display.setCount(1);
        if (display.isEmpty()) return;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5f + facing.getStepX() / 1.99f,
                    1.125f + Mth.sin(time * 0.1f) * 0.06f,
                    0.5f + facing.getStepZ() / 1.99f);
            float rotation = switch (facing) {
                case EAST -> 90.0f;
                case WEST -> 270.0f;
                case NORTH -> 180.0f;
                default -> 0.0f;
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.YP.rotationDegrees((time * 3.0f) % 360.0f));
            poseStack.scale(0.75f, 0.75f, 0.75f);
            itemRenderer.renderStatic(display, ItemDisplayContext.FIXED, packedLight,
                    packedOverlay, poseStack, buffer, level, 0);
        } finally {
            poseStack.popPose();
        }
    }
}