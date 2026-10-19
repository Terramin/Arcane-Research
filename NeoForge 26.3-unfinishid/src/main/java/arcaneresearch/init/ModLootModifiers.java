package arcaneresearch.init;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.loot.RefiningLootModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModLootModifiers.java (same entry, same name).
// REQUIRED changes only:
//   DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS)
//     -> create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS)
//   Codec -> MapCodec (26.x registry holds MapCodec); RegistryObject -> DeferredHolder.
public class ModLootModifiers {
    // Use exact registry value type so DeferredRegister generic matches.
    // NEOFORGE_PORT: 26.x registry holds MapCodec (was Codec in Forge).
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Arcaneresearch.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RefiningLootModifier>> REFINING =
            LOOT_MODIFIERS.register("refining", RefiningLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIERS.register(modEventBus);
    }
}
