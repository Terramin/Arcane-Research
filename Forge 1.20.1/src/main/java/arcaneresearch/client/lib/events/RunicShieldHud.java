package arcaneresearch.client.lib.events;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import java.util.HashMap;
import java.util.Map;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RunicShieldHud {
    private static final ResourceLocation SHEET = new ResourceLocation("arcaneresearch",
            "textures/misc/particles_1710.png");
    private static final Map<Integer, Integer> charge = new HashMap<>();
    private static final Map<Integer, Integer> total = new HashMap<>();
    public static void applySync(int playerId, int current, int max) {
        if (max <= 0) {
            charge.remove(playerId);
            total.remove(playerId);
            return;
        }
        charge.put(playerId, Math.min(max, Math.max(0, current)));
        total.put(playerId, max);
    }
    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.PLAYER_HEALTH.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isAlive() || !(mc.gui instanceof ForgeGui forgeGui)
                || !forgeGui.shouldDrawSurvivalElements()) {
            return;
        }
        Integer id = player.getId();
        int max = total.getOrDefault(id, 0);
        int current = charge.getOrDefault(id, 0);
        if (max <= 0 || current <= 0) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();
        float base = Math.max(player.getMaxHealth(), player.getHealth())
                + (float) Math.ceil(player.getAbsorptionAmount());
        int heartRows = Math.max(1, (int) Math.ceil(base / 2.0F / 10.0F));
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int heartCells = Math.max(0, (int) Math.ceil(base / 2.0F));
        int overlaid = Math.min(current, heartCells);
        int extraRowStart = heartRows * 10;
        int baseX = sw / 2 - 91;
        int baseY = sh - 39;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int a = 0; a < current; a++) {
            int cellIndex = a < overlaid ? a : extraRowStart + (a - overlaid);
            int col = cellIndex % 10;
            int row = cellIndex / 10;
            int px = baseX + col * 8;
            int py = baseY - row * rowHeight;
            int cell = a % 10;
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(SHEET, px, py, 160, 16, 9, 9, 256, 256);
            graphics.pose().pushPose();
            graphics.pose().translate(px, py, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.setColor(1.0F, 0.75F, 0.24F,
                    Mth.sin(player.tickCount / 4.0F + cell) * 0.4F + 0.6F);
            graphics.blit(SHEET, 0, 0, cell * 16, 96, 16, 16, 256, 256);
            graphics.pose().popPose();
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
