package arcaneresearch.client.events;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRenderEvents {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (isHoldingSealDisplayer(player)) {
            SealRenderer.renderSeals(
                    event.getPoseStack(),
                    event.getPartialTick(),
                    player
            );
        }
        if (!isHoldingThaumometer(player)) {
            ScanPopupRenderer.reset();
        }
        ItemStack archStack = ArchitectOverlayRenderer.getHeldArchitectStack(player);
        if (!archStack.isEmpty() && !((IArchitect) archStack.getItem()).useBlockHighlight(archStack)) {
            ArchitectOverlayRenderer.renderArchitect(event.getPoseStack(), player, archStack,
                    event.getPartialTick());
        }
        WorldAspectRenderer.tick();
    }
    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (!(event.getCamera().getEntity() instanceof Player player)) {
            return;
        }
        BlockPos pos = event.getTarget().getBlockPos();
        BlockEntity te = player.level().getBlockEntity(pos);
        if (te instanceof TileRedstoneRelay relay) {
            WorldAspectRenderer.drawTextInAir(event.getPoseStack(), pos.getX(), pos.getY() + 0.3,
                    pos.getZ(), event.getPartialTick(), "Out: " + relay.getOutputStrength());
            WorldAspectRenderer.drawTextInAir(event.getPoseStack(), pos.getX(), pos.getY() + 0.55,
                    pos.getZ(), event.getPartialTick(), "In: " + relay.getInputThreshold());
        }
        if (EntityUtils.hasGoggles(player)) {
            if (te instanceof IGogglesDisplayExtended ext) {
                String[] lines = ext.getIGogglesText();
                if (lines != null) {
                    net.minecraft.world.phys.Vec3 off = ext.getIGogglesTextOffset();
                    for (int i = 0; i < lines.length; i++) {
                        WorldAspectRenderer.drawTextInAir(event.getPoseStack(),
                                pos.getX() + off.x, pos.getY() + off.y + i * 0.25, pos.getZ() + off.z,
                                event.getPartialTick(), lines[i]);
                    }
                }
            } else if (player.level().getBlockState(pos).getBlock()
                    instanceof IGogglesDisplayExtended extBlock) {
                String[] lines = extBlock.getIGogglesText();
                if (lines != null) {
                    net.minecraft.world.phys.Vec3 off = extBlock.getIGogglesTextOffset();
                    for (int i = 0; i < lines.length; i++) {
                        WorldAspectRenderer.drawTextInAir(event.getPoseStack(),
                                pos.getX() + off.x, pos.getY() + off.y + i * 0.25, pos.getZ() + off.z,
                                event.getPartialTick(), lines[i]);
                    }
                }
            }
            if (te instanceof IAspectContainer container) {
                AspectList aspects = container.getAspects();
                if (aspects != null && aspects.size() > 0) {
                    boolean spaceAbove = player.level().isEmptyBlock(pos.above());
                    Direction face = spaceAbove ? Direction.UP : event.getTarget().getDirection();
                    double yOff = spaceAbove ? 0.4 : 0.0;
                    if (te instanceof TileRechargePedestal) {
                        yOff = 1.1;
                    }
                    WorldAspectRenderer.drawTagsOnContainer(event.getPoseStack(),
                            pos.getX(), pos.getY() + yOff, pos.getZ(),
                            aspects, face, event.getPartialTick());
                }
            }
            if (te instanceof TileArcaneEar ear) {
                WorldAspectRenderer.drawTextInAir(event.getPoseStack(), pos.getX(), pos.getY() + 1,
                        pos.getZ(), event.getPartialTick(), "Note: " + ear.getNote());
            }
            net.minecraft.world.level.block.state.BlockState targetState =
                    player.level().getBlockState(pos);
            if (targetState.is(net.minecraft.world.level.block.Blocks.NOTE_BLOCK)) {
                if (player.tickCount % 5 == 0) {
                    arcaneresearch.common.lib.network.PacketHandler.sendToServer(
                            new arcaneresearch.common.lib.network.misc.PacketNote(pos.getX(), pos.getY(),
                                    pos.getZ(), player.level().dimension().location().toString()));
                }
                if (targetState.hasProperty(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE)) {
                    WorldAspectRenderer.drawTextInAir(event.getPoseStack(), pos.getX(), pos.getY() + 1,
                            pos.getZ(), event.getPartialTick(), "Note: " + targetState.getValue(
                                    net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE));
                }
            }
        }
        ItemStack archStack = ArchitectOverlayRenderer.getHeldArchitectStack(player);
        if (!archStack.isEmpty() && ((IArchitect) archStack.getItem()).useBlockHighlight(archStack)) {
            if (ArchitectOverlayRenderer.renderArchitect(event.getPoseStack(), player, archStack,
                    event.getPartialTick())) {
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
