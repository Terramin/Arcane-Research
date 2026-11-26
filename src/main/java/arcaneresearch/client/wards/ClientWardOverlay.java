package arcaneresearch.client.wards;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.common.items.wands.foci.ItemFocusWarding;
import arcaneresearch.common.wards.WardClientRegistry;
import arcaneresearch.common.wards.WardEntry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Client mirror of the server ward registry. The outline is rendered every
 * frame for every warded block in view, exactly like TC4's TileWardedRenderer:
 * a translucent connected-texture cage, gold for the owner and red for other
 * players, but only while a wand with the warding focus is held.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ClientWardOverlay {
    private ClientWardOverlay() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || !isHoldingWardingFocus(player)) {
            return;
        }

        Map<BlockPos, WardEntry> snapshot =
                WardClientRegistry.snapshot(mc.level.dimension().location().toString());
        if (snapshot.isEmpty()) {
            return;
        }

        var camera = event.getCamera();
        var cameraPos = camera.getPosition();
        var pose = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        int tick = player.tickCount;
        for (Map.Entry<BlockPos, WardEntry> ward : snapshot.entrySet()) {
            BlockPos pos = ward.getKey();
            if (!event.getFrustum().isVisible(
                    new net.minecraft.world.phys.AABB(pos, pos.offset(1, 1, 1)))) {
                continue;
            }
            WardEntry entry = ward.getValue();
            boolean owner = entry.isOwnedBy(player.getUUID());
            // TC4 TileWardedRenderer: three independent sine phases.
            float r = (float) Math.sin(tick / 2.0F + pos.getX()) * 0.2F + 0.8F;
            float g = (float) Math.sin(tick / 3.0F + pos.getY()) * 0.2F + (owner ? 0.7F : 0.28F);
            float b = (float) Math.sin(tick / 4.0F + pos.getZ()) * 0.2F + 0.28F;
            float alpha = owner ? 0.5F : 0.25F;
            WardRenderHelper.renderBlock(pose, pos,
                    other -> isConnected(snapshot, other, entry),
                    r, g, b, alpha, cameraPos);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private static boolean isConnected(Map<BlockPos, WardEntry> wards, BlockPos pos, WardEntry entry) {
        WardEntry other = wards.get(pos);
        return other != null && other.getOwnerUuid().equals(entry.getOwnerUuid());
    }

    /** Hit effect: the original BlockWarded spawns its hemisphere FX on attack. */
    @SubscribeEvent
    public static void onLeftClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            spawnWardHitFx(event.getLevel(), event.getPos(), event.getFace());
        }
    }

    /** Called both from the left-click event and from the dig-prevention mixin. */
    public static void spawnWardHitFx(Level level, BlockPos pos, Direction face) {
        if (face != null && isWardedForClient(level.dimension().location().toString(), pos)) {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.blockWard(
                    pos.getX(), pos.getY(), pos.getZ(), face, 0.6F, 0.6F, 0.6F);
        }
    }

    private static boolean isWardedForClient(String dimension, BlockPos pos) {
        return WardClientRegistry.get(dimension, pos) != null;
    }

    private static boolean isHoldingWardingFocus(Player player) {
        // TC4 TileWardedRenderer checks the main hand only.
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof IFocusHost wand
                && wand.getFocus(stack) instanceof ItemFocusWarding;
    }
}
