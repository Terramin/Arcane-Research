package arcaneresearch.common.lib.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.utils.Utils;

import java.util.function.Supplier;

public class RefiningLootModifier extends LootModifier {

    
    
    
    public static final Supplier<MapCodec<RefiningLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, RefiningLootModifier::new)));

    public RefiningLootModifier(java.util.Optional<net.minecraft.core.Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        if (!context.hasParameter(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        
        net.minecraft.world.item.ItemInstance toolInstance = context.getOptional(LootContextParams.TOOL);
        if (!(toolInstance instanceof ItemStack tool) || tool.isEmpty()) {
            return generatedLoot;
        }
        java.util.List<EnumInfusionEnchantment> list =
                EnumInfusionEnchantment.getInfusionEnchantments(tool);
        if (!list.contains(EnumInfusionEnchantment.REFINING)) {
            return generatedLoot;
        }
        int fortune = 1 + EnumInfusionEnchantment.getInfusionEnchantmentLevel(
                tool, EnumInfusionEnchantment.REFINING);
        float chance = fortune * 0.125f;
        boolean transmuted = false;
        for (int a = 0; a < generatedLoot.size(); a++) {
            ItemStack is = generatedLoot.get(a);
            ItemStack smr = Utils.findSpecialMiningResult(is, chance, context.getRandom());
            if (!ItemStack.isSameItem(is, smr)) {
                generatedLoot.set(a, smr);
                transmuted = true;
            }
        }
        if (transmuted) {
            Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
            if (origin != null) {
                ServerLevel level = context.getLevel();
                level.playSound(null, BlockPos.containing(origin), SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS, 0.2f,
                        0.7f + context.getRandom().nextFloat() * 0.2f);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
