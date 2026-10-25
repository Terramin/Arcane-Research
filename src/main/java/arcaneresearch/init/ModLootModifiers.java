package arcaneresearch.init;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.loot.RefiningLootModifier;
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Arcaneresearch.MODID);
    public static final RegistryObject<Codec<RefiningLootModifier>> REFINING =
            LOOT_MODIFIERS.register("refining", RefiningLootModifier.CODEC);
}
