package thaumictinker.util;

import javax.annotation.Nullable;

import arcaneresearch.api.aspects.Aspect;

 



 
public final class AspectCombination {

    private AspectCombination() {
    }

    @Nullable
    public static Aspect combine(Aspect first, Aspect second) {
        if (first == null || second == null) {
            return null;
        }
        Aspect result = Aspect.mixList.get((first.getTag() + second.getTag()).hashCode());
        if (result == null) {
            result = Aspect.mixList.get((second.getTag() + first.getTag()).hashCode());
        }
        return result;
    }
}
