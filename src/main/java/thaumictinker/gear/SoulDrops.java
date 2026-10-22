package thaumictinker.gear;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import arcaneresearch.api.aspects.Aspect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import thaumictinker.necromancy.MobAspect;
import thaumictinker.necromancy.NecromancyFeature;

 



 
public final class SoulDrops {

    private SoulDrops() {
    }

    @Nullable
    public static List<ItemStack> soulsFor(Entity entity) {
        MobAspect mob = MobAspect.forEntity(entity);
        if (mob == null) {
            return null;
        }
        List<ItemStack> souls = new ArrayList<>();
        for (Aspect aspect : mob.aspects()) {
            souls.add(NecromancyFeature.soulStack(aspect));
        }
        return souls;
    }
}
