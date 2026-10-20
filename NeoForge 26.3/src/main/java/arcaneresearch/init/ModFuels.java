package arcaneresearch.init;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import arcaneresearch.Arcaneresearch;

/**
 * NEOFORGE_PORT: furnace fuels. Forge 1.20.1 used Item.getBurnTime + FurnaceFuelBurnTimeEvent
 * (alumentum 4800, greatwood log 500, silverwood log 400) and vanilla fuel tags for the planks
 * (#minecraft:planks 300) and slabs (#minecraft:wooden_slabs 150). In 26.x a fuel is the
 * COOKING_FUEL item component pointing at a context_int_provider (burn time, halved in fast
 * furnaces like vanilla); the custom ones live in data/arcaneresearch/context_int_provider/cooking.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class ModFuels {

    private ModFuels() {
    }

    private static ResourceKey<ContextIntProvider> key(String path) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cooking/" + path));
    }

    private static void fuel(ModifyDefaultComponentsEvent event, ItemLike item, ResourceKey<ContextIntProvider> burnTime) {
        event.modify(item, (components, context, it) -> components.set(DataComponents.COOKING_FUEL,
                new CookingFuel(burnTime, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)));
    }

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        fuel(event, ModItems.ALUMENTUM.get(), key("time_alumentum"));
        fuel(event, ModBlocks.GREATWOOD_LOG.get(), key("time_greatwood_log"));
        fuel(event, ModBlocks.SILVERWOOD_LOG.get(), key("time_silverwood_log"));
        fuel(event, ModBlocks.GREATWOOD_PLANKS.get(), ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        fuel(event, ModBlocks.SILVERWOOD_PLANKS.get(), ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
        fuel(event, ModBlocks.GREATWOOD_SLAB.get(), ContextIntProviders.COOKING_TIME_WOOD_SLABS);
        fuel(event, ModBlocks.SILVERWOOD_SLAB.get(), ContextIntProviders.COOKING_TIME_WOOD_SLABS);
    }
}
