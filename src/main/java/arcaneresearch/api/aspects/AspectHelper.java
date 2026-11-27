package arcaneresearch.api.aspects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** Registry-backed aspect lookups for items and entities. */
public class AspectHelper {
    private static final Map<String, AspectList> objectTags = new HashMap<>();
    private static final Map<String, AspectList> entityTags = new HashMap<>();
    private static final Map<String, List<EntityVariant>> entityVariants = new HashMap<>();

    /** One NBT-specific entity registration. */
    private record EntityVariant(List<EntityTagRules.Condition> conditions, AspectList aspects) {
    }

    // ---------------------------------------------------------------- items

    /**
     * Resolves aspects using the original cascade: exact tag, tag without the
     * damage component, then the bare item id.
     *
     * @param stack item to look up
     * @return the matched aspects, or null when the item is not registered
     */
    @Nullable
    public static AspectList getObjectAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        for (String key : AspectLookupRules.candidates(itemId, tag)) {
            AspectList found = objectTags.get(key);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Registers aspects for an item, replacing any previous registration.
     *
     * @param stack item whose NBT tag is part of the lookup key
     * @param aspects aspects to store (copied)
     */
    public static void putOverride(ItemStack stack, AspectList aspects) {
        ResourceLocation itemId = itemId(stack);
        if (itemId != null) {
            putOverride(itemId, stack.getTag(), aspects);
        }
    }

    /**
     * Registers aspects for an item, adding to any previous registration.
     *
     * @param stack item whose NBT tag is part of the lookup key
     * @param aspects aspects to add (copied)
     */
    public static void putMerge(ItemStack stack, AspectList aspects) {
        ResourceLocation itemId = itemId(stack);
        if (itemId != null) {
            putMerge(itemId, stack.getTag(), aspects);
        }
    }

    /**
     * Registers aspects for an item id (no NBT), replacing any previous value.
     *
     * @param itemId registry id of the item
     * @param aspects aspects to store (copied)
     */
    public static void putOverride(ResourceLocation itemId, AspectList aspects) {
        putOverride(itemId, null, aspects);
    }

    /**
     * Registers aspects for an item id (no NBT), adding to any previous value.
     *
     * @param itemId registry id of the item
     * @param aspects aspects to add (copied)
     */
    public static void putMerge(ResourceLocation itemId, AspectList aspects) {
        putMerge(itemId, null, aspects);
    }

    /** Backwards-compatible alias for {@link #putOverride(ItemStack, AspectList)}. */
    public static void registerObjectTag(ItemStack stack, AspectList aspects) {
        putOverride(stack, aspects);
    }

    /** Backwards-compatible alias for {@link #putOverride(ResourceLocation, AspectList)}. */
    public static void registerObjectTag(ResourceLocation itemId, AspectList aspects) {
        putOverride(itemId, aspects);
    }

    /**
     * Stores a canonical key for the given id/tag pair.
     *
     * @param tag NBT that is part of the key; null for a bare id
     */
    private static void putOverride(ResourceLocation itemId, @Nullable CompoundTag tag,
                                    @Nullable AspectList aspects) {
        if (itemId == null || aspects == null) {
            return;
        }
        objectTags.put(AspectLookupRules.key(itemId, tag), aspects.copy());
    }

    /**
     * Adds aspects to the existing entry for the canonical id/tag key.
     *
     * @param tag NBT that is part of the key; null for a bare id
     */
    private static void putMerge(ResourceLocation itemId, @Nullable CompoundTag tag,
                                 @Nullable AspectList aspects) {
        if (itemId == null || aspects == null) {
            return;
        }
        String key = AspectLookupRules.key(itemId, tag);
        AspectList existing = objectTags.get(key);
        if (existing == null) {
            objectTags.put(key, aspects.copy());
            return;
        }
        AspectList merged = existing.copy();
        for (Aspect aspect : aspects.getAspects()) {
            if (aspect != null) {
                merged.merge(aspect, aspects.getAmount(aspect));
            }
        }
        objectTags.put(key, merged);
    }

    /** @return the registry id of the stack, or null for empty stacks */
    @Nullable
    private static ResourceLocation itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    // -------------------------------------------------------------- entities

    /**
     * Registers base aspects for an entity, replacing any previous value.
     *
     * @param entityId registry id of the entity type
     * @param aspects aspects to store (copied)
     */
    public static void registerEntityTag(ResourceLocation entityId, AspectList aspects) {
        if (entityId != null && aspects != null) {
            entityTags.put(entityId.toString(), aspects.copy());
        }
    }

    /**
     * Registers an NBT-specific aspect variant for an entity. The last variant
     * whose conditions all match wins when resolving a living entity.
     *
     * @param entityId registry id of the entity type
     * @param conditions NBT fields the entity must have
     * @param aspects aspects to store (copied)
     */
    public static void registerEntityVariant(ResourceLocation entityId,
                                             List<EntityTagRules.Condition> conditions,
                                             AspectList aspects) {
        if (entityId == null || aspects == null) {
            return;
        }
        entityVariants.computeIfAbsent(entityId.toString(), key -> new ArrayList<>())
                .add(new EntityVariant(List.copyOf(conditions), aspects.copy()));
    }

    /**
     * Returns the aspects registered for an entity id, ignoring NBT variants.
     *
     * @param entityId registry id of the entity type
     * @return base aspects, or null when the entity is not registered
     */
    @Nullable
    public static AspectList getEntityAspects(@Nullable ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return entityTags.get(entityId.toString());
    }

    /**
     * Resolves entity aspects like the 1.12.2 original: NBT-specific variants
     * are checked in registration order and the last match wins; the plain id
     * registration is the fallback.
     *
     * @param entity entity to inspect
     * @return matched aspects, or null when the entity is not registered
     */
    @Nullable
    public static AspectList getEntityAspects(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) {
            return null;
        }
        List<EntityVariant> variants = entityVariants.get(entityId.toString());
        if (variants != null && !variants.isEmpty()) {
            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
            AspectList found = null;
            for (EntityVariant variant : variants) {
                if (EntityTagRules.matches(tag, variant.conditions())) {
                    found = variant.aspects();
                }
            }
            if (found != null) {
                return found;
            }
        }
        return getEntityAspects(entityId);
    }

    // -------------------------------------------------------------- helpers

    /**
     * Returns the aspect produced by mixing two aspects, if the pair is known.
     *
     * @param aspect1 first component
     * @param aspect2 second component
     * @return the combination, or null when the pair cannot be combined
     */
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

    /**
     * Checks whether two aspects have a known combination.
     *
     * @param aspect1 first component
     * @param aspect2 second component
     * @return true when {@link #getCombinationResult} returns a non-null aspect
     */
    public static boolean canCombine(Aspect aspect1, Aspect aspect2) {
        return getCombinationResult(aspect1, aspect2) != null;
    }

    /** Clears item aspects, entity aspects and entity NBT variants. */
    public static void clearTags() {
        objectTags.clear();
        entityTags.clear();
        entityVariants.clear();
    }

    /**
     * Culls to the legacy cap of seven aspects.
     *
     * @param temp source list
     * @return a new list with at most seven aspects
     */
    public static AspectList cullTags(AspectList temp) {
        return cullTags(temp, 7);
    }

    /**
     * Culls to at most {@code cap} aspects. Composite aspects are weighted more
     * heavily than primal ones, so primal aspects are dropped first.
     *
     * @param temp source list, may be null
     * @param cap maximum number of aspects
     * @return a new, non-null list
     */
    public static AspectList cullTags(AspectList temp, int cap) {
        if (temp == null) {
            return new AspectList();
        }
        AspectList temp2 = new AspectList();
        for (Aspect tag : temp.getAspects()) {
            if (tag != null) {
                temp2.add(tag, temp.getAmount(tag));
            }
        }
        while (temp2.size() > cap) {
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
            if (lowest == null) {
                break;
            }
            temp2.aspects.remove(lowest);
        }
        return temp2;
    }

    /** @return number of registered item aspect entries */
    public static int getObjectTagCount() {
        return objectTags.size();
    }

    /** @return number of registered base entity aspect entries */
    public static int getEntityTagCount() {
        return entityTags.size();
    }
}
