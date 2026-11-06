package arcaneresearch.common.items.casters;

import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CasterManager {

    private static final Map<Integer, Long> cooldownServer = new HashMap<>();
    private static final Map<Integer, Long> cooldownClient = new HashMap<>();
    private static final int AREA_SIZE = 3;
    private static final int POUCH_ID_MULTIPLIER = 1000;

    public static float getTotalVisDiscount(Player player) {
        if (player == null) {
            return 0.0f;
        }

        int total = CuriosCompat.getVisDiscountFromCurios(player);

        NonNullList<ItemStack> armor = player.getInventory().armor;
        for (ItemStack armorStack : armor) {
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(armorStack, player);
            }
        }

        MobEffect exhaust = ModEffects.VIS_EXHAUST.get();
        MobEffect infectious = ModEffects.INFECTIOUS_VIS_EXHAUST.get();

        MobEffectInstance exhaustEffect = player.getEffect(exhaust);
        MobEffectInstance infectiousEffect = player.getEffect(infectious);

        if (exhaustEffect != null || infectiousEffect != null) {
            int level1 = exhaustEffect != null ? exhaustEffect.getAmplifier() : 0;
            int level2 = infectiousEffect != null ? infectiousEffect.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * 10;
        }

        return total / 100.0f;
    }

    public static boolean consumeVisFromInventory(Player player, float cost) {
        if (player == null) {
            return false;
        }

        NonNullList<ItemStack> items = player.getInventory().items;
        for (int slot = items.size() - 1; slot >= 0; slot--) {
            ItemStack item = items.get(slot);
            if (!item.isEmpty() && item.getItem() instanceof ICaster caster) {
                if (caster.consumeVis(item, player, cost, true, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void changeFocus(ItemStack casterStack, Level level, Player player, String focusKey) {
        if (casterStack == null || player == null) {
            return;
        }
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }

        TreeMap<String, Integer> fociMap = new TreeMap<>();
        Map<Integer, Integer> pouchLocations = new HashMap<>();
        Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches = new HashMap<>();
        int pouchCount = 0;

        for (CuriosCompat.CuriosSlotRef ref
                : CuriosCompat.findCuriosSlotRefs(player, s -> s.getItem() instanceof ItemFocusPouch)) {
            ItemStack pouchStack = CuriosCompat.getCuriosStack(player, ref);
            if (pouchStack.isEmpty() || !(pouchStack.getItem() instanceof ItemFocusPouch pouch)) {
                continue;
            }
            pouchCount++;
            int refKey = -(curiosPouches.size() + 1);
            pouchLocations.put(pouchCount, refKey);
            curiosPouches.put(refKey, ref);

            NonNullList<ItemStack> inv = pouch.getInventory(pouchStack);
            for (int q = 0; q < inv.size(); q++) {
                ItemStack focusStack = inv.get(q);
                if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {
                    String sortKey = focus.getSortingHelper(focusStack);
                    if (sortKey != null) {
                        fociMap.put(sortKey, q + pouchCount * POUCH_ID_MULTIPLIER);
                    }
                }
            }
        }

        NonNullList<ItemStack> items = player.getInventory().items;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack item = items.get(slot);
            if (item.isEmpty()) {
                continue;
            }
            if (item.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(item);
                if (sortKey != null) {
                    fociMap.put(sortKey, slot);
                }
            }
            if (item.getItem() instanceof ItemFocusPouch pouch) {
                pouchCount++;
                pouchLocations.put(pouchCount, slot);
                NonNullList<ItemStack> inv = pouch.getInventory(item);
                for (int q = 0; q < inv.size(); q++) {
                    ItemStack focusStack = inv.get(q);
                    if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {
                        String sortKey = focus.getSortingHelper(focusStack);
                        if (sortKey != null) {
                            fociMap.put(sortKey, q + pouchCount * POUCH_ID_MULTIPLIER);
                        }
                    }
                }
            }
        }

        if ("REMOVE".equals(focusKey) || fociMap.isEmpty()) {
            removeFocus(casterStack, player);
            return;
        }
        if (focusKey == null) {
            return;
        }

        String newKey = focusKey;
        if (fociMap.get(newKey) == null) {
            newKey = fociMap.higherKey(newKey);
        }
        if (newKey == null || fociMap.get(newKey) == null) {
            newKey = fociMap.firstKey();
        }

        Integer locationBoxed = fociMap.get(newKey);
        if (locationBoxed == null) {
            return;
        }
        int location = locationBoxed;

        ItemStack newFocus = ItemStack.EMPTY;
        if (location >= 0 && location < POUCH_ID_MULTIPLIER) {
            if (location < items.size()) {
                newFocus = items.get(location).copy();
                items.set(location, ItemStack.EMPTY);
                player.getInventory().setChanged();
            }
        } else {
            int pouchId = location / POUCH_ID_MULTIPLIER;
            int focusSlot = location - pouchId * POUCH_ID_MULTIPLIER;
            Integer pouchSlotBoxed = pouchLocations.get(pouchId);
            if (pouchSlotBoxed != null) {
                newFocus = fetchFocusFromPouch(player, focusSlot, pouchSlotBoxed, curiosPouches);
            }
        }

        if (newFocus.isEmpty()) {
            return;
        }

        SoundEvent ticks = ModSounds.TICKS.get();
        player.playSound(ticks, 0.3f, 1.0f);

        ItemStack currentFocus = caster.getFocusStack(casterStack);
        boolean currentEmpty = currentFocus == null || currentFocus.isEmpty();

        if (currentEmpty
                || addFocusToPouch(player, currentFocus.copy(), pouchLocations, curiosPouches)
                || player.getInventory().add(currentFocus.copy())) {
            caster.setFocus(casterStack, ItemStack.EMPTY);
        }

        if (caster.getFocus(casterStack) == null) {
            caster.setFocus(casterStack, newFocus);
        } else if (!addFocusToPouch(player, newFocus, pouchLocations, curiosPouches)) {
            player.getInventory().add(newFocus);
        }
    }

    public static boolean removeFocus(ItemStack casterStack, Player player) {
        if (casterStack == null || player == null) {
            return false;
        }
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return false;
        }

        ItemStack currentFocus = caster.getFocusStack(casterStack);
        if (currentFocus == null || currentFocus.isEmpty()) {
            return false;
        }

        Map<Integer, Integer> pouchLocations = new HashMap<>();
        Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches = new HashMap<>();
        collectPouchLocations(player, pouchLocations, curiosPouches);

        if (addFocusToPouch(player, currentFocus.copy(), pouchLocations, curiosPouches)
                || player.getInventory().add(currentFocus.copy())) {
            caster.setFocus(casterStack, ItemStack.EMPTY);
            SoundEvent ticks = ModSounds.TICKS.get();
            player.playSound(ticks, 0.3f, 0.9f);
            return true;
        }
        return false;
    }

    private static int collectPouchLocations(Player player, Map<Integer, Integer> pouchLocations,
                                             Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches) {
        int pouchCount = 0;

        for (CuriosCompat.CuriosSlotRef ref
                : CuriosCompat.findCuriosSlotRefs(player, s -> s.getItem() instanceof ItemFocusPouch)) {
            pouchCount++;
            int refKey = -(curiosPouches.size() + 1);
            pouchLocations.put(pouchCount, refKey);
            curiosPouches.put(refKey, ref);
        }

        NonNullList<ItemStack> items = player.getInventory().items;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack item = items.get(slot);
            if (!item.isEmpty() && item.getItem() instanceof ItemFocusPouch) {
                pouchCount++;
                pouchLocations.put(pouchCount, slot);
            }
        }
        return pouchCount;
    }

    private static ItemStack fetchFocusFromPouch(Player player, int focusSlot, int pouchSlot,
                                                 Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches) {
        if (pouchSlot < 0) {
            CuriosCompat.CuriosSlotRef ref = curiosPouches.get(pouchSlot);
            if (ref == null) {
                return ItemStack.EMPTY;
            }
            ItemStack pouch = CuriosCompat.getCuriosStack(player, ref);
            if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
                return ItemStack.EMPTY;
            }
            return extractFocus(focusPouch, pouch, focusSlot, () -> CuriosCompat.setCuriosStack(player, ref, pouch));
        }

        NonNullList<ItemStack> items = player.getInventory().items;
        if (pouchSlot >= items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack pouch = items.get(pouchSlot);
        if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
            return ItemStack.EMPTY;
        }
        return extractFocus(focusPouch, pouch, focusSlot, () -> {
            items.set(pouchSlot, pouch);
            player.getInventory().setChanged();
        });
    }

    private static ItemStack extractFocus(ItemFocusPouch focusPouch, ItemStack pouch, int focusSlot,
                                          Runnable save) {
        NonNullList<ItemStack> inv = focusPouch.getInventory(pouch);
        if (focusSlot < 0 || focusSlot >= inv.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack focus = inv.get(focusSlot);
        if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = focus.copy();
        inv.set(focusSlot, ItemStack.EMPTY);
        focusPouch.setInventory(pouch, inv);
        save.run();
        return result;
    }

    private static boolean addFocusToPouch(Player player, ItemStack focus, Map<Integer, Integer> pouchLocations,
                                           Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches) {
        if (focus == null || focus.isEmpty()) {
            return false;
        }

        for (Map.Entry<Integer, Integer> entry : pouchLocations.entrySet()) {
            Integer valueBoxed = entry.getValue();
            if (valueBoxed == null) {
                continue;
            }
            int pouchSlot = valueBoxed;

            if (pouchSlot < 0) {
                CuriosCompat.CuriosSlotRef ref = curiosPouches.get(pouchSlot);
                if (ref == null) {
                    continue;
                }
                ItemStack pouch = CuriosCompat.getCuriosStack(player, ref);
                if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
                    continue;
                }
                if (tryInsertFocus(focusPouch, pouch, focus)) {
                    CuriosCompat.setCuriosStack(player, ref, pouch);
                    return true;
                }
                continue;
            }

            NonNullList<ItemStack> items = player.getInventory().items;
            if (pouchSlot >= items.size()) {
                continue;
            }
            ItemStack pouch = items.get(pouchSlot);
            if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
                continue;
            }
            if (tryInsertFocus(focusPouch, pouch, focus)) {
                items.set(pouchSlot, pouch);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean tryInsertFocus(ItemFocusPouch focusPouch, ItemStack pouch, ItemStack focus) {
        NonNullList<ItemStack> inv = focusPouch.getInventory(pouch);
        for (int q = 0; q < inv.size(); q++) {
            if (inv.get(q).isEmpty()) {
                inv.set(q, focus.copy());
                focusPouch.setInventory(pouch, inv);
                return true;
            }
        }
        return false;
    }

    public static void toggleMisc(ItemStack casterStack, Level level, Player player, int modifier) {
        if (casterStack == null) {
            return;
        }
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        if (caster.getFocus(casterStack) == null) {
            return;
        }

        FocusPackage fp = ItemFocus.getPackage(caster.getFocusStack(casterStack));
        if (!FocusEngine.doesPackageContainElement(fp, "arcaneresearch.PLAN")) {
            return;
        }

        int dim = getAreaDim(casterStack);

        if (modifier == 0) {
            int areaX = getAreaX(casterStack);
            int areaY = getAreaY(casterStack);
            int areaZ = getAreaZ(casterStack);
            int max = AREA_SIZE;

            if (dim == 0) {
                areaX++;
                areaY++;
                areaZ++;
            } else if (dim == 1) {
                areaX++;
            } else if (dim == 2) {
                areaZ++;
            } else if (dim == 3) {
                areaY++;
            }

            if (areaX > max) areaX = 0;
            if (areaY > max) areaY = 0;
            if (areaZ > max) areaZ = 0;

            setAreaX(casterStack, areaX);
            setAreaY(casterStack, areaY);
            setAreaZ(casterStack, areaZ);
        } else if (modifier == 1) {
            dim++;
            if (dim > 3) dim = 0;
            setAreaDim(casterStack, dim);
        }
    }

    private static int getAreaSize(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ICaster)) {
            return 0;
        }
        return AREA_SIZE;
    }

    public static int getAreaDim(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("aread")) {
            return tag.getInt("aread");
        }
        return 0;
    }

    public static int getAreaX(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        int size = getAreaSize(stack);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areax")) {
            return Math.min(tag.getInt("areax"), size);
        }
        return size;
    }

    public static int getAreaY(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        int size = getAreaSize(stack);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areay")) {
            return Math.min(tag.getInt("areay"), size);
        }
        return size;
    }

    public static int getAreaZ(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        int size = getAreaSize(stack);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areaz")) {
            return Math.min(tag.getInt("areaz"), size);
        }
        return size;
    }

    public static void setAreaX(ItemStack stack, int area) {
        if (stack != null) {
            stack.getOrCreateTag().putInt("areax", area);
        }
    }

    public static void setAreaY(ItemStack stack, int area) {
        if (stack != null) {
            stack.getOrCreateTag().putInt("areay", area);
        }
    }

    public static void setAreaZ(ItemStack stack, int area) {
        if (stack != null) {
            stack.getOrCreateTag().putInt("areaz", area);
        }
    }

    public static void setAreaDim(ItemStack stack, int dim) {
        if (stack != null) {
            stack.getOrCreateTag().putInt("aread", dim);
        }
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean client = entity.level().isClientSide();
        Map<Integer, Long> cooldowns = client ? cooldownClient : cooldownServer;
        Long endTime = cooldowns.get(entity.getId());
        return endTime != null && endTime > System.currentTimeMillis();
    }

    public static float getCooldown(LivingEntity entity) {
        if (entity == null) {
            return 0.0f;
        }
        if (entity.level().isClientSide()) {
            Long endTime = cooldownClient.get(entity.getId());
            if (endTime != null) {
                return Math.max(0, (endTime - System.currentTimeMillis()) / 1000.0f);
            }
        }
        return 0.0f;
    }

    public static void setCooldown(LivingEntity entity, int cooldownTicks) {
        if (entity == null) {
            return;
        }
        int id = entity.getId();

        if (cooldownTicks <= 0) {
            cooldownClient.remove(id);
            cooldownServer.remove(id);
            return;
        }

        long endTime = System.currentTimeMillis() + cooldownTicks * 50L;
        if (entity.level().isClientSide()) {
            cooldownClient.put(id, endTime);
        } else {
            cooldownServer.put(id, endTime);
        }
    }
}