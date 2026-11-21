package arcaneresearch.client.renderers;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IGoggles;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Floating damage/heal numbers shown above entities while goggles are equipped.
 * Ported to the 26.x {@link SubmitCustomGeometryEvent} + {@code SubmitNodeCollector}
 * pipeline (the old RenderLevelStageEvent buffer rendering no longer exists).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class DamageHealthTextRenderer {

    private static final List<TextDisplay> DISPLAYS = new CopyOnWriteArrayList<>();
    private static final Random RANDOM = new Random();

    private record TextDisplay(String text, BlockPos pos, long start, float scale, int color,
                               double seconds, double dx, double dz) {
    }

    private DamageHealthTextRenderer() {
    }

    public static void showText(String text, BlockPos pos, float scale, int color, double seconds) {
        DISPLAYS.add(new TextDisplay(text, pos.immutable(), System.currentTimeMillis(), scale,
                color, seconds, -0.2D + RANDOM.nextDouble(), -0.2D + RANDOM.nextDouble()));
    }

    private static boolean hasGogglesEquipped() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IGoggles) {
            return true;
        }
        final boolean[] found = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> inv.getCurios().values().forEach(slots -> {
            for (int i = 0; i < slots.getSlots(); i++) {
                if (slots.getStacks().getStackInSlot(i).getItem() instanceof IGoggles) {
                    found[0] = true;
                }
            }
        }));
        return found[0];
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide() || !hasGogglesEquipped()) {
            return;
        }
        float amount = event.getHealthDamage();
        String text = amount < 10000.0F ? "-" + String.format("%.1f", amount) : "Infinitely";
        showText(text, event.getEntity().blockPosition(), 0.02F, 0xFFFF0000, 2.0D);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide() || !hasGogglesEquipped()) {
            return;
        }
        float amount = event.getAmount();
        String text = amount < 10000.0F ? "+" + String.format("%.1f", amount) : "Infinitely";
        showText(text, event.getEntity().blockPosition(), 0.01F, 0xFF00FF00, 1.4D);
    }

    @SubscribeEvent
    public static void onSubmit(SubmitCustomGeometryEvent event) {
        if (!hasGogglesEquipped()) {
            if (!DISPLAYS.isEmpty()) {
                DISPLAYS.clear();
            }
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        var cameraState = event.getLevelRenderState().cameraRenderState;
        Vec3 cameraPos = cameraState.pos;
        Font font = mc.font;
        PoseStack poseStack = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();
        long now = System.currentTimeMillis();

        for (Iterator<TextDisplay> it = DISPLAYS.iterator(); it.hasNext(); ) {
            TextDisplay display = it.next();
            long elapsed = now - display.start;
            if (elapsed >= display.seconds * 1000.0D) {
                it.remove();
                continue;
            }
            double x = display.pos.getX() + 0.5D + display.dx - cameraPos.x;
            double y = display.pos.getY() + Math.sin(elapsed / 1000.0D) * 0.5D - cameraPos.y;
            double z = display.pos.getZ() + 0.5D + display.dz - cameraPos.z;
            if (x * x + y * y + z * z > 64.0D * 64.0D) {
                continue;
            }
            FormattedCharSequence seq = Component.literal(display.text).getVisualOrderText();
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.rotate(cameraState.orientation);
            poseStack.scale(-display.scale, -display.scale, display.scale);
            collector.submitText(poseStack, -font.width(seq) / 2.0F, 0.0F, seq, false,
                    Font.DisplayMode.NORMAL, 0x40000000, display.color, 0, 0);
            poseStack.popPose();
        }
    }
}
