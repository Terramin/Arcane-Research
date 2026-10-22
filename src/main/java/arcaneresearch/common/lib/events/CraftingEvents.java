package arcaneresearch.common.lib.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModItems;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class CraftingEvents {

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        int warp = ArcaneresearchApi.getWarp(crafted);
        if (!ModConfig.wussMode && warp > 0 && !player.level().isClientSide()) {
            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp, IPlayerWarp.EnumWarpType.NORMAL);
        }

        if (ModItems.LABEL_FILLED != null && crafted.getItem() == ModItems.LABEL_FILLED.get()
                && crafted.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
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

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
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
                for (var tag : crafted.getItem().builtInRegistryHolder().tags().toList()) {
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

    
    
    
    
    
    public static int getVanillaBurnTime(net.minecraft.world.level.Level level, ItemStack fuel) {
        if (fuel.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return 0;
        }
        if (!fuel.has(net.minecraft.core.component.DataComponents.COOKING_FUEL)) {
            return 0;
        }
        net.minecraft.world.level.storage.loot.LootParams params =
                new net.minecraft.world.level.storage.loot.LootParams.Builder(serverLevel)
                        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY);
        net.minecraft.world.level.storage.loot.LootContext ctx =
                new net.minecraft.world.level.storage.loot.LootContext.Builder(params)
                        .create(java.util.Optional.empty());
        return net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.getFromItem(
                fuel,
                net.minecraft.core.component.DataComponents.COOKING_FUEL,
                net.minecraft.world.item.component.CookingFuel::burnTime,
                ctx, 0);
    }

    public static int getBurnTimeTotal(net.minecraft.world.level.Level level, ItemStack fuel) {
        return Math.max(getBurnTime(fuel), getVanillaBurnTime(level, fuel));
    }

    
    
    
    public static ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        net.minecraft.world.item.ItemStackTemplate remainder = stack.getCraftingRemainder();
        return remainder != null ? remainder.create() : ItemStack.EMPTY;
    }

    
    
    
    
}
