package arcaneresearch.common.lib.loot;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
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
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.utils.Utils;
import java.util.function.Supplier;
public class RefiningLootModifier extends LootModifier {
    public static final Supplier<Codec<RefiningLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, RefiningLootModifier::new)));
    public RefiningLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        if (!context.hasParam(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) {
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
            Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
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
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
