package arcaneresearch.common.lib.research;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;

public class ScanPotion implements IScanThing {

    
    
    private final Holder<MobEffect> effect;

    public ScanPotion(Holder<MobEffect> effect) {
        this.effect = effect;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return getEffect(player, obj) != null;
    }

    private MobEffectInstance getEffect(Player player, Object obj) {
        if (obj == null || effect == null) {
            return null;
        }
        if (obj instanceof LivingEntity living) {
            for (MobEffectInstance instance : living.getActiveEffects()) {
                if (instance.getEffect() == effect) {
                    return instance;
                }
            }
        } else {
            ItemStack stack = ScanningManager.getItemFromParams(player, obj);
            if (stack != null && !stack.isEmpty()) {
                
                PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                for (MobEffectInstance instance : contents.getAllEffects()) {
                    if (instance.getEffect() == effect) {
                        return instance;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        if (effect == null) {
            return null;
        }

        return "!" + net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString();
    }
}
