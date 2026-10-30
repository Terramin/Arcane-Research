package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import arcaneresearch.Arcaneresearch;

import java.util.HashMap;
import java.util.Map;









 







 
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class RunicShieldHud {

    private static final Identifier SHEET = Identifier.fromNamespaceAndPath("arcaneresearch",
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

    public static final GuiLayer RUNIC_SHIELD = (graphics, deltaTracker) -> {
        renderOverlay(graphics);
    };

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "runic_shield"), RUNIC_SHIELD);
    }

     



 
    private static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        
        
        
        boolean survival = false;
        try {
            survival = mc.gameMode != null && mc.gameMode.canHurtPlayer();
        } catch (Exception ignored) {
        }
        if (player == null || !player.isAlive() || mc.gui.hud.isHidden() || !survival) {
            return;
        }
        Integer id = player.getId();
        int max = total.getOrDefault(id, 0);
        int current = charge.getOrDefault(id, 0);
        if (max <= 0 || current <= 0) {
            return;
        }
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        
        
        
        
        
        
        
        
        
        
        
        float absorption = (float) Math.ceil(player.getAbsorptionAmount());
        float currentBase = player.getHealth() + absorption;
        float layoutBase = Math.max(player.getMaxHealth(), player.getHealth()) + absorption;
        int heartRows = Math.max(1, (int) Math.ceil(layoutBase / 2.0F / 10.0F));
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int currentHeartCells = Math.max(0, (int) Math.ceil(currentBase / 2.0F));
        int renderCount = Math.min(current, currentHeartCells);
        int baseX = sw / 2 - 91;
        int baseY = sh - 39;

        
        
        for (int a = 0; a < renderCount; a++) {
            int cellIndex = a;
            int col = cellIndex % 10;
            int row = cellIndex / 10;
            int px = baseX + col * 8;
            int py = baseY - row * rowHeight;
            int cell = a % 10;
            
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, px, py, 160, 16, 9, 9, 256, 256);
            
            
            graphics.pose().pushMatrix();
            graphics.pose().translate(px, py);
            graphics.pose().scale(0.5F, 0.5F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, 0, 0, cell * 16, 96, 16, 16,
                    16, 16, 256, 256,
                    ARGB.colorFromFloat(Mth.sin(player.tickCount / 4.0F + cell) * 0.4F + 0.6F,
                            1.0F, 0.75F, 0.24F));
            graphics.pose().popMatrix();
        }
    }
}
