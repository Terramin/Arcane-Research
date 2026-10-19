package arcaneresearch.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.items.IGogglesDisplayExtended;
import arcaneresearch.client.renderers.ArchitectOverlayRenderer;
import arcaneresearch.client.renderers.ScanPopupRenderer;
import arcaneresearch.client.renderers.SealRenderer;
import arcaneresearch.client.renderers.WorldAspectRenderer;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.tiles.devices.TileArcaneEar;
import arcaneresearch.common.tiles.devices.TileRechargePedestal;
import arcaneresearch.common.tiles.devices.TileRedstoneRelay;

// NEOFORGE_PORT (26.3 render-state rework, verified against 26.3 jars):
// - onRenderLevelStage takes the AfterTranslucentBlocks subclass (no getStage());
//   partialTick via DeltaTracker (event no longer carries it); pose may be null.
// - onBlockHighlight moved to ExtractBlockOutlineRenderStateEvent (extraction phase,
//   cancellable, no PoseStack): draw bodies are renderer-phase no-ops, event logic kept 1:1.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();

        if (poseStack != null) {
            if (isHoldingSealDisplayer(player)) {
                SealRenderer.renderSeals(poseStack, partialTick, player);
            }

            if (!isHoldingThaumometer(player)) {
                ScanPopupRenderer.reset();
            }

            ItemStack archStack = ArchitectOverlayRenderer.getHeldArchitectStack(player);
            if (!archStack.isEmpty() && !((IArchitect) archStack.getItem()).useBlockHighlight(archStack)) {
                ArchitectOverlayRenderer.renderArchitect(poseStack, player, archStack, partialTick);
            }
        }

        WorldAspectRenderer.tick();
    }

    @SubscribeEvent
    public static void onBlockHighlight(ExtractBlockOutlineRenderStateEvent event) {
        // NEOFORGE_PORT: Camera.getEntity removed -> Minecraft camera entity (same viewer).
        if (!(Minecraft.getInstance().getCameraEntity() instanceof Player player)) {
            return;
        }
        BlockPos pos = event.getBlockPos();
        BlockEntity te = event.getLevel().getBlockEntity(pos);
        // NEOFORGE_PORT: extraction carries no PoseStack; draw bodies are renderer-phase
        // no-ops until the renderers are ported (logic below stays 1:1 so call sites go
        // live automatically).
        com.mojang.blaze3d.vertex.PoseStack poseStack = null;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        if (te instanceof TileRedstoneRelay relay) {
            WorldAspectRenderer.drawTextInAir(poseStack, pos.getX(), pos.getY() + 0.3,
                    pos.getZ(), partialTick, "Out: " + relay.getOutputStrength());
            WorldAspectRenderer.drawTextInAir(poseStack, pos.getX(), pos.getY() + 0.55,
                    pos.getZ(), partialTick, "In: " + relay.getInputThreshold());
        }

        if (EntityUtils.hasGoggles(player)) {

            if (te instanceof IGogglesDisplayExtended ext) {
                String[] lines = ext.getIGogglesText();
                if (lines != null) {
                    net.minecraft.world.phys.Vec3 off = ext.getIGogglesTextOffset();
                    for (int i = 0; i < lines.length; i++) {
                        WorldAspectRenderer.drawTextInAir(poseStack,
                                pos.getX() + off.x, pos.getY() + off.y + i * 0.25, pos.getZ() + off.z,
                                partialTick, lines[i]);
                    }
                }
            } else if (player.level().getBlockState(pos).getBlock()
                    instanceof IGogglesDisplayExtended extBlock) {
                String[] lines = extBlock.getIGogglesText();
                if (lines != null) {
                    net.minecraft.world.phys.Vec3 off = extBlock.getIGogglesTextOffset();
                    for (int i = 0; i < lines.length; i++) {
                        WorldAspectRenderer.drawTextInAir(poseStack,
                                pos.getX() + off.x, pos.getY() + off.y + i * 0.25, pos.getZ() + off.z,
                                partialTick, lines[i]);
                    }
                }
            }

            if (te instanceof IAspectContainer container) {
                AspectList aspects = container.getAspects();
                if (aspects != null && aspects.size() > 0) {
                    boolean spaceAbove = player.level().isEmptyBlock(pos.above());
                    Direction face = spaceAbove ? Direction.UP : event.getHitResult().getDirection();
                    double yOff = spaceAbove ? 0.4 : 0.0;
                    if (te instanceof TileRechargePedestal) {

                        yOff = 1.1;
                    }
                    WorldAspectRenderer.drawTagsOnContainer(poseStack,
                            pos.getX(), pos.getY() + yOff, pos.getZ(),
                            aspects, face, partialTick);
                }
            }

            if (te instanceof TileArcaneEar ear) {
                WorldAspectRenderer.drawTextInAir(poseStack, pos.getX(), pos.getY() + 1,
                        pos.getZ(), partialTick, "Note: " + ear.getNote());
            }

            net.minecraft.world.level.block.state.BlockState targetState =
                    player.level().getBlockState(pos);
            if (targetState.is(net.minecraft.world.level.block.Blocks.NOTE_BLOCK)) {
                if (player.tickCount % 5 == 0) {
                    arcaneresearch.common.lib.network.PacketHandler.sendToServer(
                            new arcaneresearch.common.lib.network.misc.PacketNote(pos.getX(), pos.getY(),
                                    pos.getZ(), player.level().dimension().identifier().toString()));
                }
                if (targetState.hasProperty(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE)) {
                    WorldAspectRenderer.drawTextInAir(poseStack, pos.getX(), pos.getY() + 1,
                            pos.getZ(), partialTick, "Note: " + targetState.getValue(
                                    net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE));
                }
            }
        }

        ItemStack archStack = ArchitectOverlayRenderer.getHeldArchitectStack(player);
        if (!archStack.isEmpty() && ((IArchitect) archStack.getItem()).useBlockHighlight(archStack)) {
            if (ArchitectOverlayRenderer.renderArchitect(poseStack, player, archStack, partialTick)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isHoldingSealDisplayer(Player player) {

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ISealDisplayer) {
            return true;
        }

        ItemStack offHand = player.getOffhandItem();
        return !offHand.isEmpty() && offHand.getItem() instanceof ISealDisplayer;
    }

    private static boolean isHoldingThaumometer(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ItemThaumometer) {
            return true;
        }

        ItemStack offHand = player.getOffhandItem();
        return !offHand.isEmpty() && offHand.getItem() instanceof ItemThaumometer;
    }
}