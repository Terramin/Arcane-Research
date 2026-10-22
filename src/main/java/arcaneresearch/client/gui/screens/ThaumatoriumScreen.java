package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import arcaneresearch.common.menu.ThaumatoriumMenu;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;
import java.util.HashMap;

public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_thaumatorium.png");
    private static final HashMap<Integer, CrucibleRecipe> recipeCache = new HashMap<>();
    private final TileThaumatorium tile;
    private final ArrayList<Integer> hashList = new ArrayList<>();
    private int index = 0;
    private long lastHLUpdate = 0;

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 175, 216);
        this.inventoryLabelY = this.imageHeight - 94;
        this.tile = menu.getBlockEntity();
    }

    private static CrucibleRecipe getRecipeCached(int hash) {
        if (recipeCache.containsKey(hash)) return recipeCache.get(hash);
        CrucibleRecipe cr = ArcaneresearchApi.getCrucibleRecipeFromHash(hash);
        if (cr != null) recipeCache.put(hash, cr);
        return cr;
    }

    
    private void renderHoverTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int px = 0, py = 0, q = 0, idx = 0;
        for (int hash : hashList) {
            if (q++ < index * 2) continue;
            CrucibleRecipe cr = getRecipeCached(hash);
            if (cr == null) continue;
            int xx = mouseX - (leftPos + 48 + px * 16);
            int yy = mouseY - (topPos + 56 + py * 16);
            if (xx >= 0 && yy >= 0 && xx < 16 && yy < 16) {
                graphics.setTooltipForNextFrame(font, cr.getRecipeOutput(), mouseX, mouseY);
                break;
            }
            if (++px > 1) {
                px = 0;
                py++;
            }
            if (++idx >= 6) break;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        
        int k = leftPos;
        int l = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k, l, 0, 0, imageWidth, imageHeight, 256, 256);

        long t = System.currentTimeMillis();
        if (t > lastHLUpdate) {
            hashList.clear();
            hashList.addAll(tile.generateRecipeHashlist());
            lastHLUpdate = t + 500L;
        }
        if (!hashList.isEmpty()) {
            if (index > hashList.size() / 2) index = hashList.size() / 2;
            if (index < 0 || hashList.size() <= 6) index = 0;
            if (hashList.size() > 6) {
                if (index > 0) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + 82, l + 56, 176, 56, 8, 11, 256, 256);
                if (index < hashList.size() / 2.0f - 3.0f) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + 82, l + 93, 176, 93, 8, 11, 256, 256);
            }
        }
        drawAspects(graphics, k, l, t);
        if (tile.maxRecipes > 1) {
            String text = tile.recipeHash.size() + "/" + tile.maxRecipes;
            graphics.pose().pushMatrix();
            graphics.pose().translate(k + 64, l + 48);
            graphics.pose().scale(0.5f, 0.5f);
            int ll = font.width(text) / 2;
            graphics.text(font, text, -ll, 0, 0xFFFFFFFF, false);
            graphics.pose().popMatrix();
        }
        drawOutput(graphics, k, l, t);
        renderHoverTooltips(graphics, mouseX, mouseY);
    }

    private void drawAspects(GuiGraphicsExtractor graphics, int k, int l, long time) {
        if (tile.recipeHash.isEmpty()) return;
        int hash = tile.recipeHash.get((int) (time / 1000L % tile.recipeHash.size()));
        if (!tile.recipeHash.contains(hash)) return;
        CrucibleRecipe cr = getRecipeCached(hash);
        if (cr == null) return;
        int count = 0, px = 0, py = 0;
        for (Aspect aspect : cr.getAspects().getAspectsSortedByName()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + 98 + 16 * px, l + 40 + 20 * py, 176, 4, 12, 3, 256, 256);
            int fill = (int) (tile.essentia.getAmount(aspect) / (float) cr.getAspects().getAmount(aspect) * 12.0f);
            fill = Math.max(0, Math.min(12, fill));
            int color = aspect.getColor();
            
            int fillTint = 0xFF000000 | (color & 0xFFFFFF);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, k + 98 + 16 * px, l + 40 + 20 * py, 176, 0, fill, 3, 256, 256, fillTint);
            if (++px > 1) {
                px = 0;
                py++;
            }
            if (++count >= 8) break;
        }
        count = 0;
        px = 0;
        py = 0;
        for (Aspect aspect : cr.getAspects().getAspectsSortedByName()) {
            AspectRenderer.drawAspect(graphics, k + 96 + 16 * px, l + 24 + 20 * py, aspect, (float) cr.getAspects().getAmount(aspect));
            if (++px > 1) {
                px = 0;
                py++;
            }
            if (++count >= 8) break;
        }
    }

    private void drawOutput(GuiGraphicsExtractor graphics, int x, int y, long time) {
        int px = 0, py = 0, q = 0, idx = 0;
        for (int hash : hashList) {
            if (q++ < index * 2) continue;
            CrucibleRecipe cr = getRecipeCached(hash);
            if (cr == null) continue;
            drawOutputIcon(graphics, x + 48 + px * 16, y + 56 + py * 16, cr, time);
            if (++px > 1) {
                px = 0;
                py++;
            }
            if (++idx >= 6) break;
        }
    }

    private void drawOutputIcon(GuiGraphicsExtractor graphics, int x, int y, CrucibleRecipe cr, long time) {
        if (tile.recipeHash.contains(cr.hash)) {
            
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 176, 8, 16, 16, 256, 256, 0xB3FFFFFF);
            if (tile.recipeHash.size() > 1) {
                int hash = tile.recipeHash.get((int) (time / 1000L % tile.recipeHash.size()));
                if (hash == cr.hash) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 176, 8, 16, 16, 256, 256, 0xB3FFFFFF);
            }
        }
        ItemStack out = cr.getRecipeOutput().copy();
        arcaneresearch.client.gui.GuiRenderHelper.renderItemWithDecorations(graphics, font, out, x, y);
    }

    @Override
    
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        boolean handled = super.mouseClicked(event, doubleClick);
        if (minecraft == null || minecraft.player == null) return handled;
        int gx = leftPos;
        int gy = topPos;

        int px = 0, py = 0, q = 0, idx = 0;
        for (int hash : hashList) {
            if (q++ < index * 2) continue;
            if (getRecipeCached(hash) == null) continue;
            int x = (int) (mx - (gx + 48 + px * 16));
            int y = (int) (my - (gy + 56 + py * 16));
            if (x >= 0 && y >= 0 && x < 16 && y < 16) {
                PacketHandler.sendToServer(new PacketSelectThaumotoriumRecipeToServer(minecraft.player, tile.getBlockPos(), hash));
                minecraft.player.playSound(ModSounds.HH_ON.get(), 0.3f, 1.0f);
                lastHLUpdate = 0L;
                return true;
            }
            if (++px > 1) {
                px = 0;
                py++;
            }
            if (++idx >= 6) break;
        }
        if (hashList.size() > 6) {
            if (index > 0) {
                int x2 = (int) (mx - (gx + 82));
                int y2 = (int) (my - (gy + 56));
                if (x2 >= 0 && y2 >= 0 && x2 < 8 && y2 < 11) {
                    index--;
                    minecraft.player.playSound(ModSounds.CLACK.get(), 0.4f, 1.0f);
                    lastHLUpdate = 0L;
                    return true;
                }
            }
            if (index < hashList.size() / 2.0f - 3.0f) {
                int x2 = (int) (mx - (gx + 82));
                int y2 = (int) (my - (gy + 93));
                if (x2 >= 0 && y2 >= 0 && x2 < 8 && y2 < 11) {
                    index++;
                    minecraft.player.playSound(ModSounds.CLACK.get(), 0.4f, 1.0f);
                    lastHLUpdate = 0L;
                    return true;
                }
            }
        }
        return handled;
    }
}