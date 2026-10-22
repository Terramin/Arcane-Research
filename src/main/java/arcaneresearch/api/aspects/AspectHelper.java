package arcaneresearch.api.aspects;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class AspectHelper {

    private static Map<String, AspectList> objectTags = new HashMap<>();

    private static Map<String, AspectList> entityTags = new HashMap<>();

    public static AspectList getObjectAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }

        
        
        String specificKey = componentKey(stack);
        if (specificKey != null) {
            AspectList specific = objectTags.get(itemId + "#" + specificKey);
            if (specific != null) {
                return specific;
            }
        }
        String key = itemId.toString();

        return objectTags.get(key);
    }

    public static void registerObjectTag(ItemStack stack, AspectList aspects) {
        if (stack == null || stack.isEmpty() || aspects == null) {
            return;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null) {
            String specificKey = componentKey(stack);
            if (specificKey != null) {
                objectTags.put(itemId + "#" + specificKey, aspects);
            } else {
                objectTags.put(itemId.toString(), aspects);
            }
        }
    }

      
    private static String componentKey(ItemStack stack) {
        net.minecraft.core.component.DataComponentPatch patch = stack.getComponentsPatch();
        if (patch.isEmpty()) {
            return null;
        }
        return net.minecraft.core.component.DataComponentPatch.CODEC
                .encodeStart(arcaneresearch.common.lib.utils.RegistryNbt.ops(), patch)
                .result().map(Object::toString).orElseGet(patch::toString);
    }

    public static void registerObjectTag(Identifier itemId, AspectList aspects) {
        if (itemId != null && aspects != null) {
            objectTags.put(itemId.toString(), aspects);
        }
    }

    public static void registerEntityTag(Identifier entityId, AspectList aspects) {
        if (entityId != null && aspects != null) {
            entityTags.put(entityId.toString(), aspects);
        }
    }

    public static AspectList getEntityAspects(Identifier entityId) {
        if (entityId == null) {
            return null;
        }
        return entityTags.get(entityId.toString());
    }

    public static AspectList getEntityAspects(Entity entity) {
        if (entity == null) {
            return null;
        }
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return getEntityAspects(entityId);
    }

    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        if (aspect1 == null || aspect2 == null) {
            return null;
        }

        int hash1 = (aspect1.getTag() + aspect2.getTag()).hashCode();
        int hash2 = (aspect2.getTag() + aspect1.getTag()).hashCode();

        Aspect result = Aspect.mixList.get(hash1);
        if (result == null) {
            result = Aspect.mixList.get(hash2);
        }

        return result;
    }

    public static boolean canCombine(Aspect aspect1, Aspect aspect2) {
        return getCombinationResult(aspect1, aspect2) != null;
    }

    public static void clearTags() {
        objectTags.clear();
        entityTags.clear();
    }

    public static AspectList cullTags(AspectList temp) {
        return cullTags(temp, 7);
    }

    public static AspectList cullTags(AspectList temp, int cap) {
        AspectList temp2 = new AspectList();
        for (Aspect tag : temp.getAspects()) {
            if (tag != null) {
                temp2.add(tag, temp.getAmount(tag));
            }
        }
        while (temp2 != null && temp2.size() > cap) {
            Aspect lowest = null;
            float low = 32767.0F;
            for (Aspect tag : temp2.getAspects()) {
                if (tag != null) {
                    float ta = temp2.getAmount(tag);
                    if (tag.isPrimal()) {
                        ta *= 0.9F;
                    } else {
                        if (!tag.getComponents()[0].isPrimal()) {
                            ta *= 1.1F;
                            if (!tag.getComponents()[0].getComponents()[0].isPrimal()) {
                                ta *= 1.05F;
                            }
                            if (!tag.getComponents()[0].getComponents()[1].isPrimal()) {
                                ta *= 1.05F;
                            }
                        }
                        if (!tag.getComponents()[1].isPrimal()) {
                            ta *= 1.1F;
                            if (!tag.getComponents()[1].getComponents()[0].isPrimal()) {
                                ta *= 1.05F;
                            }
                            if (!tag.getComponents()[1].getComponents()[1].isPrimal()) {
                                ta *= 1.05F;
                            }
                        }
                    }
                    if (ta < low) {
                        low = ta;
                        lowest = tag;
                    }
                }
            }
            temp2.aspects.remove(lowest);
        }
        return temp2;
    }

    public static int getObjectTagCount() {
        return objectTags.size();
    }

    public static int getEntityTagCount() {
        return entityTags.size();
    }
}
