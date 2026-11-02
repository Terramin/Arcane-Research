package arcaneresearch.common.lib.research;

import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

public class ScanPotion implements IScanThing {

    private final MobEffect effect;
    private String researchKey;

    public ScanPotion(MobEffect effect) {
        this.effect = effect;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return getEffect(player, obj) != null;
    }

    private MobEffectInstance getEffect(Player player, Object obj) {
        if (obj == null || effect == null) return null;

        if (obj instanceof LivingEntity living) {
            Collection<MobEffectInstance> effects = living.getActiveEffects();
            if (effects.isEmpty()) return null;

            for (MobEffectInstance instance : effects) {
                if (instance != null && instance.getEffect() == effect) {
                    return instance;
                }
            }
            return null;
        }

        if (player == null) return null;

        ItemStack stack = ScanningManager.getItemFromParams(player, obj);
        if (stack == null || stack.isEmpty()) return null;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (effects.isEmpty()) return null;

        for (MobEffectInstance instance : effects) {
            if (instance != null && instance.getEffect() == effect) {
                return instance;
            }
        }
        return null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (effect == null) return "";

        if (researchKey == null) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            researchKey = id != null ? "!" + id : "";
        }
        return researchKey;
    }
}