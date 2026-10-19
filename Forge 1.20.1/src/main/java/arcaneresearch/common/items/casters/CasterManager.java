package arcaneresearch.common.items.casters;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
public class CasterManager {
    private static final Map<Integer, Long> cooldownServer = new HashMap<>();
    private static final Map<Integer, Long> cooldownClient = new HashMap<>();
    public static float getTotalVisDiscount(Player player) {
        if (player == null) {
            return 0.0f;
        }
        int total = 0;
        total += CuriosCompat.getVisDiscountFromCurios(player);
        for (int slot = 0; slot < 4; slot++) {
            ItemStack armorStack = player.getInventory().armor.get(slot);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(armorStack, player);
            }
        }
        MobEffectInstance exhaustEffect = player.getEffect(ModEffects.VIS_EXHAUST.get());
        MobEffectInstance infectiousEffect = player.getEffect(ModEffects.INFECTIOUS_VIS_EXHAUST.get());
        if (exhaustEffect != null || infectiousEffect != null) {
            int level1 = exhaustEffect != null ? exhaustEffect.getAmplifier() : 0;
            int level2 = infectiousEffect != null ? infectiousEffect.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * 10;
        }
        return total / 100.0f;
    }
    public static boolean consumeVisFromInventory(Player player, float cost) {
        for (int slot = player.getInventory().items.size() - 1; slot >= 0; slot--) {
            ItemStack item = player.getInventory().items.get(slot);
            if (!item.isEmpty() && item.getItem() instanceof ICaster caster) {
                if (caster.consumeVis(item, player, cost, true, false)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void changeFocus(ItemStack casterStack, Level level, Player player, String focusKey) {
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
            pouchLocations.put(pouchCount, -(curiosPouches.size() + 1));
            curiosPouches.put(-(curiosPouches.size() + 1), ref);
            NonNullList<ItemStack> inv = pouch.getInventory(pouchStack);
            for (int q = 0; q < inv.size(); q++) {
                ItemStack focusStack = inv.get(q);
                if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {
                    String sortKey = focus.getSortingHelper(focusStack);
                    if (sortKey != null) {
                        fociMap.put(sortKey, q + pouchCount * 1000);
                    }
                }
            }
        }
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = player.getInventory().items.get(slot);
            if (item.isEmpty()) continue;
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
                            fociMap.put(sortKey, q + pouchCount * 1000);
                        }
                    }
                }
            }
        }
        if ("REMOVE".equals(focusKey) || fociMap.isEmpty()) {
            removeFocus(casterStack, player);
            return;
        }
        if (focusKey != null) {
            String newKey = focusKey;
            if (fociMap.get(newKey) == null) {
                newKey = fociMap.higherKey(newKey);
            }
            if (newKey == null || fociMap.get(newKey) == null) {
                newKey = fociMap.firstKey();
            }
            int location = fociMap.get(newKey);
            ItemStack newFocus = ItemStack.EMPTY;
            if (location >= 0 && location < 1000) {
                newFocus = player.getInventory().items.get(location).copy();
                player.getInventory().items.set(location, ItemStack.EMPTY);
                player.getInventory().setChanged();
            } else {
                int pouchId = location / 1000;
                int focusSlot = location - pouchId * 1000;
                if (pouchLocations.containsKey(pouchId)) {
                    int pouchSlot = pouchLocations.get(pouchId);
                    newFocus = fetchFocusFromPouch(player, focusSlot, pouchSlot, curiosPouches);
                }
            }
            if (newFocus.isEmpty()) {
                return;
            }
            player.playSound(ModSounds.TICKS.get(), 0.3f, 1.0f);
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
    }
    public static boolean removeFocus(ItemStack casterStack, Player player) {
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
            player.playSound(ModSounds.TICKS.get(), 0.3f, 0.9f);
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
            pouchLocations.put(pouchCount, -(curiosPouches.size() + 1));
            curiosPouches.put(-(curiosPouches.size() + 1), ref);
        }
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = player.getInventory().items.get(slot);
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
            CuriosCompat.setCuriosStack(player, ref, pouch);
            return result;
        }
        if (pouchSlot >= player.getInventory().items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack pouch = player.getInventory().items.get(pouchSlot);
        if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
            return ItemStack.EMPTY;
        }
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
        player.getInventory().items.set(pouchSlot, pouch);
        player.getInventory().setChanged();
        return result;
    }
    private static boolean addFocusToPouch(Player player, ItemStack focus, Map<Integer, Integer> pouchLocations,
                                           Map<Integer, CuriosCompat.CuriosSlotRef> curiosPouches) {
        for (Map.Entry<Integer, Integer> entry : pouchLocations.entrySet()) {
            int pouchSlot = entry.getValue();
            if (pouchSlot < 0) {
                CuriosCompat.CuriosSlotRef ref = curiosPouches.get(pouchSlot);
                if (ref == null) {
                    continue;
                }
                ItemStack pouch = CuriosCompat.getCuriosStack(player, ref);
                if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
                    continue;
                }
                NonNullList<ItemStack> inv = focusPouch.getInventory(pouch);
                for (int q = 0; q < inv.size(); q++) {
                    if (inv.get(q).isEmpty()) {
                        inv.set(q, focus.copy());
                        focusPouch.setInventory(pouch, inv);
                        CuriosCompat.setCuriosStack(player, ref, pouch);
                        return true;
                    }
                }
                continue;
            }
            if (pouchSlot >= player.getInventory().items.size()) {
                continue;
            }
            ItemStack pouch = player.getInventory().items.get(pouchSlot);
            if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch focusPouch)) {
                continue;
            }
            NonNullList<ItemStack> inv = focusPouch.getInventory(pouch);
            for (int q = 0; q < inv.size(); q++) {
                if (inv.get(q).isEmpty()) {
                    inv.set(q, focus.copy());
                    focusPouch.setInventory(pouch, inv);
                    player.getInventory().items.set(pouchSlot, pouch);
                    player.getInventory().setChanged();
                    return true;
                }
            }
        }
        return false;
    }
    public static void toggleMisc(ItemStack casterStack, Level level, Player player, int modifier) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        ItemFocus focus = (ItemFocus) caster.getFocus(casterStack);
        if (focus == null) return;
        FocusPackage fp = ItemFocus.getPackage(caster.getFocusStack(casterStack));
        if (fp != null && FocusEngine.doesPackageContainElement(fp, "arcaneresearch.PLAN")) {
            int dim = getAreaDim(casterStack);
            if (modifier == 0) {
                int areaX = getAreaX(casterStack);
                int areaY = getAreaY(casterStack);
                int areaZ = getAreaZ(casterStack);
                int max = getAreaSize(casterStack);
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
    }
    private static int getAreaSize(ItemStack stack) {
        if (!(stack.getItem() instanceof ICaster)) {
            return 0;
        }
        return 3;
    }
    public static int getAreaDim(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("aread")) {
            return tag.getInt("aread");
        }
        return 0;
    }
    public static int getAreaX(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areax")) {
            return Math.min(tag.getInt("areax"), getAreaSize(stack));
        }
        return getAreaSize(stack);
    }
    public static int getAreaY(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areay")) {
            return Math.min(tag.getInt("areay"), getAreaSize(stack));
        }
        return getAreaSize(stack);
    }
    public static int getAreaZ(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areaz")) {
            return Math.min(tag.getInt("areaz"), getAreaSize(stack));
        }
        return getAreaSize(stack);
    }
    public static void setAreaX(ItemStack stack, int area) {
        stack.getOrCreateTag().putInt("areax", area);
    }
    public static void setAreaY(ItemStack stack, int area) {
        stack.getOrCreateTag().putInt("areay", area);
    }
    public static void setAreaZ(ItemStack stack, int area) {
        stack.getOrCreateTag().putInt("areaz", area);
    }
    public static void setAreaDim(ItemStack stack, int dim) {
        stack.getOrCreateTag().putInt("aread", dim);
    }
    public static boolean isOnCooldown(LivingEntity entity) {
        Map<Integer, Long> cooldowns = entity.level().isClientSide() ? cooldownClient : cooldownServer;
        Long endTime = cooldowns.get(entity.getId());
        return endTime != null && endTime > System.currentTimeMillis();
    }
    public static float getCooldown(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            Long endTime = cooldownClient.get(entity.getId());
            if (endTime != null) {
                return Math.max(0, (endTime - System.currentTimeMillis()) / 1000.0f);
            }
        }
        return 0.0f;
    }
    public static void setCooldown(LivingEntity entity, int cooldownTicks) {
        if (cooldownTicks == 0) {
            cooldownClient.remove(entity.getId());
            cooldownServer.remove(entity.getId());
        } else {
            long endTime = System.currentTimeMillis() + cooldownTicks * 50L;
            if (entity.level().isClientSide()) {
                cooldownClient.put(entity.getId(), endTime);
            } else {
                cooldownServer.put(entity.getId(), endTime);
            }
        }
    }
}
