package arcaneresearch.common.lib.events;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModItems;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingEvents {
    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;
        int warp = ArcaneresearchApi.getWarp(crafted);
        if (!ModConfig.wussMode && warp > 0 && !player.level().isClientSide) {
            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp, IPlayerWarp.EnumWarpType.NORMAL);
        }
        if (ModItems.LABEL_FILLED != null && crafted.getItem() == ModItems.LABEL_FILLED.get() && crafted.hasTag()) {
            try {
                var matrix = event.getInventory();
                for (int slot = 0; slot < matrix.getContainerSize(); slot++) {
                    ItemStack inSlot = matrix.getItem(slot);
                    if (!inSlot.isEmpty()
                            && inSlot.getItem() instanceof arcaneresearch.common.items.consumables.ItemPhial) {
                        inSlot.grow(1);
                        matrix.setItem(slot, inSlot);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            int stackHash = ResearchManager.createItemStackHash(crafted.copy());
            if (ResearchManager.craftingReferences.contains(stackHash)) {
                ResearchManager.completeResearch(player, "[#]" + stackHash);
            } else {
                ItemStack simpleStack = new ItemStack(crafted.getItem(), crafted.getCount());
                int simpleHash = ResearchManager.createItemStackHash(simpleStack);
                if (ResearchManager.craftingReferences.contains(simpleHash)) {
                    ResearchManager.completeResearch(player, "[#]" + simpleHash);
                }
            }
            try {
                for (var tag : crafted.getTags().toList()) {
                    int tagHash = ("tag:" + tag.location()).hashCode();
                    if (ResearchManager.craftingReferences.contains(tagHash)) {
                        ResearchManager.completeResearch(player, "[#]" + tagHash);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (ModItems.PRIMORDIAL_PEARL == null) return;
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if ((!left.isEmpty() && left.getItem() == ModItems.PRIMORDIAL_PEARL.get()) ||
                (!right.isEmpty() && right.getItem() == ModItems.PRIMORDIAL_PEARL.get())) {
            event.setCanceled(true);
        }
    }
    public static int getBurnTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (ModItems.ALUMENTUM != null && fuel.getItem() == ModItems.ALUMENTUM.get()) {
            return 4800;
        }
        if (arcaneresearch.init.ModBlocks.GREATWOOD_LOG != null
                && fuel.is(arcaneresearch.init.ModBlocks.GREATWOOD_LOG.get().asItem())) {
            return 500;
        }
        if (arcaneresearch.init.ModBlocks.SILVERWOOD_LOG != null
                && fuel.is(arcaneresearch.init.ModBlocks.SILVERWOOD_LOG.get().asItem())) {
            return 400;
        }
        return 0;
    }
    @SubscribeEvent
    public static void onFurnaceFuel(net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent event) {
        int burnTime = getBurnTime(event.getItemStack());
        if (burnTime > 0) {
            event.setBurnTime(burnTime);
        }
    }
}
