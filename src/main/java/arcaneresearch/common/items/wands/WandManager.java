package arcaneresearch.common.items.wands;

import arcaneresearch.common.items.baubles.ItemAmuletVis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.items.wands.foci.ItemFocusTrade;
import arcaneresearch.common.lib.wands.WandResearchHelper;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.init.ModEffects;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WandManager {
    private static final Logger LOGGER = LogManager.getLogger("Arcaneresearch/WandManager");

    static HashMap<Integer, Long> cooldownServer = new HashMap<>();
    static HashMap<Integer, Long> cooldownClient = new HashMap<>();

    @SuppressWarnings("resource")
    private static boolean isClientSide(LivingEntity entity) {
        return entity.level().isClientSide;
    }

    public static int getBaseChargeRate(Player player, boolean currentItem, int slot) {
        if (slot < 9) {
            if (WandResearchHelper.isResearchComplete(player, "NODETAPPER2")) {
                return 3;
            }
            if (WandResearchHelper.isResearchComplete(player, "NODETAPPER1")) {
                return 2;
            }
        }
        return currentItem ? 1 : 0;
    }

    public static float getTotalVisDiscount(Player player, Aspect aspect) {
        if (player == null) {
            return 0.0F;
        }

        int total = 0;

        if (ModList.get().isLoaded("curios")) {
            final int[] curiosTotal = {0};
            try {
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getStacks().getSlots(); i++) {
                        ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                        if (!worn.isEmpty() && worn.getItem() instanceof IVisDiscountGear gear) {
                            curiosTotal[0] += gear.getVisDiscount(worn, player);
                        }
                    }
                }));
            } catch (Exception e) {
                LOGGER.warn("Failed to scan Curios for {}", player.getName().getString(), e);
            }
            total += curiosTotal[0];
        }

        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty() && armor.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(armor, player);
            }
        }

        if (ModEffects.VIS_EXHAUST.isPresent() && ModEffects.INFECTIOUS_VIS_EXHAUST.isPresent()) {
            MobEffectInstance visExhaust = player.getEffect(ModEffects.VIS_EXHAUST.get());
            MobEffectInstance infectious = player.getEffect(ModEffects.INFECTIOUS_VIS_EXHAUST.get());
            if (visExhaust != null || infectious != null) {
                int level1 = visExhaust != null ? visExhaust.getAmplifier() : -1;
                int level2 = infectious != null ? infectious.getAmplifier() : -1;
                total -= (Math.max(level1, level2) + 1) * 10;
            }
        }

        return total / 100.0F;
    }

    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        if (ModList.get().isLoaded("curios")) {
            try {
                final boolean[] done = {false};
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    if (done[0]) return;
                    for (var slotHandler : handler.getCurios().values()) {
                        for (int i = 0; i < slotHandler.getStacks().getSlots(); i++) {
                            ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                            if (!worn.isEmpty()
                                    && worn.getItem() instanceof ItemAmuletVis amulet
                                    && amulet.consumeAllVis(worn, player, cost, false, true)) {
                                done[0] = true;
                                break;
                            }
                        }
                        if (done[0]) break;
                    }
                });
                if (done[0]) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to consume vis from Curios for {}", player.getName().getString(), e);
            }
        }

        NonNullList<ItemStack> inv = player.getInventory().items;
        for (int a = inv.size() - 1; a >= 0; a--) {
            ItemStack stack = inv.get(a);
            if (!stack.isEmpty() && stack.getItem() instanceof IWand wand
                    && wand.consumeAllVis(stack, player, cost, false, true)) {
                return true;
            }
        }
        return false;
    }

    public static void changeFocus(ItemStack wandStack, Level level, Player player, String focus) {
        if (!(wandStack.getItem() instanceof IWand wand)) {
            return;
        }

        TreeMap<String, Integer> foci = new TreeMap<>();
        HashMap<Integer, Integer> pouches = new HashMap<>();
        ItemStack item = ItemStack.EMPTY;
        List<CurioPouchRef> curioPouches = new ArrayList<>();
        AtomicInteger pouchCounter = new AtomicInteger();

        try {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((slotId, slotHandler) -> {
                for (int i = 0; i < slotHandler.getStacks().getSlots(); i++) {
                    ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                    if (!worn.isEmpty() && worn.getItem() instanceof ItemFocusPouch pouch) {
                        int pouchId = pouchCounter.incrementAndGet();
                        curioPouches.add(new CurioPouchRef(slotHandler, i));
                        NonNullList<ItemStack> pinv = pouch.getInventory(worn);
                        for (int q = 0; q < pinv.size(); q++) {
                            ItemStack f = pinv.get(q);
                            if (!f.isEmpty() && f.getItem() instanceof ItemFocusBasic basic) {
                                foci.put(basic.getSortingHelper(f), q + pouchId * 1000);
                            }
                        }
                    }
                }
            }));
        } catch (Exception e) {
            LOGGER.warn("Failed to scan Curios pouches for {}", player.getName().getString(), e);
        }

        int pouchcount = pouchCounter.get();
        for (int ci = 0; ci < curioPouches.size(); ci++) {
            pouches.put(ci + 1, -1000 - ci);
        }

        NonNullList<ItemStack> inv = player.getInventory().items;
        for (int a = 0; a < inv.size(); a++) {
            item = inv.get(a);
            if (!item.isEmpty() && item.getItem() instanceof ItemFocusBasic basic) {
                foci.put(basic.getSortingHelper(item), a);
            }
            if (!item.isEmpty() && item.getItem() instanceof ItemFocusPouch pouch) {
                pouchcount++;
                pouches.put(pouchcount, a);
                NonNullList<ItemStack> pinv = pouch.getInventory(item);
                for (int q = 0; q < pinv.size(); q++) {
                    ItemStack f = pinv.get(q);
                    if (!f.isEmpty() && f.getItem() instanceof ItemFocusBasic basic) {
                        foci.put(basic.getSortingHelper(f), q + pouchcount * 1000);
                    }
                }
            }
        }

        if ("REMOVE".equals(focus) || foci.isEmpty()) {
            if (wand.getFocus(wandStack) != null
                    && (addFocusToPouch(player, wand.getFocusStack(wandStack).copy(), pouches, curioPouches)
                    || player.getInventory().add(wand.getFocusStack(wandStack).copy()))) {
                wand.setFocus(wandStack, null);
                level.playSound(null, player.blockPosition(), WandSounds.cameraticls(),
                        SoundSource.PLAYERS, 0.3F, 0.9F);
            }
            return;
        }

        if (focus != null) {
            String newkey = focus;
            if (!foci.containsKey(newkey)) {
                newkey = foci.higherKey(newkey);
            }
            if (newkey == null || !foci.containsKey(newkey)) {
                newkey = foci.firstKey();
            }
            int loc = foci.get(newkey);

            if (loc < 1000 && loc >= 0) {
                item = inv.get(loc).copy();
            } else {
                int pid = loc / 1000;
                if (!pouches.containsKey(pid)) {
                    return;
                }
                int pouchslot = pouches.get(pid);
                int focusslot = loc - pid * 1000;
                if (pouchslot <= -1000) {
                    CurioPouchRef ref = curioPouches.get(-1000 - pouchslot);
                    ItemStack pouch = ref.handler().getStacks().getStackInSlot(ref.slot()).copy();
                    item = fetchFocusFromCurioPouch(player, focusslot, pouch, ref);
                } else {
                    ItemStack pouch = inv.get(pouchslot).copy();
                    item = fetchFocusFromPouch(player, focusslot, pouch, pouchslot);
                }
            }

            if (!item.isEmpty()) {
                if (loc < 1000 && loc >= 0) {
                    player.getInventory().setItem(loc, ItemStack.EMPTY);
                }
            } else {
                return;
            }

            level.playSound(null, player.blockPosition(), WandSounds.cameraticls(),
                    SoundSource.PLAYERS, 0.3F, 1.0F);

            if (wand.getFocus(wandStack) != null
                    && (addFocusToPouch(player, wand.getFocusStack(wandStack).copy(), pouches, curioPouches)
                    || player.getInventory().add(wand.getFocusStack(wandStack).copy()))) {
                wand.setFocus(wandStack, null);
            }

            if (wand.getFocus(wandStack) == null) {
                wand.setFocus(wandStack, item);
            } else if (!addFocusToPouch(player, item, pouches, curioPouches)) {
                player.getInventory().add(item);
            }
        }
    }

    private static ItemStack fetchFocusFromPouch(Player player, int focusid, ItemStack pouch, int pouchslot) {
        ItemStack focus = ItemStack.EMPTY;
        if (!(pouch.getItem() instanceof ItemFocusPouch pouchItem)) {
            return focus;
        }
        NonNullList<ItemStack> pinv = pouchItem.getInventory(pouch);
        if (focusid < 0 || focusid >= pinv.size()) {
            return focus;
        }
        ItemStack contents = pinv.get(focusid);
        if (!contents.isEmpty() && contents.getItem() instanceof ItemFocusBasic) {
            focus = contents.copy();
            pinv.set(focusid, ItemStack.EMPTY);
            pouchItem.setInventory(pouch, pinv);
            if (pouchslot >= 0) {
                player.getInventory().setItem(pouchslot, pouch);
                player.getInventory().setChanged();
            }
        }
        return focus;
    }

    private static ItemStack fetchFocusFromCurioPouch(Player player, int focusid, ItemStack pouch,
                                                      CurioPouchRef ref) {
        ItemStack focus = ItemStack.EMPTY;
        if (!(pouch.getItem() instanceof ItemFocusPouch pouchItem)) {
            return focus;
        }
        NonNullList<ItemStack> pinv = pouchItem.getInventory(pouch);
        if (focusid < 0 || focusid >= pinv.size()) {
            return focus;
        }
        ItemStack contents = pinv.get(focusid);
        if (!contents.isEmpty() && contents.getItem() instanceof ItemFocusBasic) {
            focus = contents.copy();
            pinv.set(focusid, ItemStack.EMPTY);
            pouchItem.setInventory(pouch, pinv);
            ref.handler().getStacks().setStackInSlot(ref.slot(), pouch);
        }
        return focus;
    }

    private static boolean addFocusToPouch(Player player, ItemStack focus, HashMap<Integer, Integer> pouches,
                                           List<CurioPouchRef> curioPouches) {
        for (int pouchslot : pouches.values()) {
            if (pouchslot < 0) {
                continue;
            }
            ItemStack pouch = player.getInventory().getItem(pouchslot);
            if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch pouchItem)) {
                continue;
            }
            NonNullList<ItemStack> pinv = pouchItem.getInventory(pouch);
            for (int q = 0; q < pinv.size(); q++) {
                if (pinv.get(q).isEmpty()) {
                    pinv.set(q, focus.copy());
                    pouchItem.setInventory(pouch, pinv);
                    player.getInventory().setItem(pouchslot, pouch);
                    player.getInventory().setChanged();
                    return true;
                }
            }
        }
        for (CurioPouchRef ref : curioPouches) {
            ItemStack pouch = ref.handler().getStacks().getStackInSlot(ref.slot());
            if (pouch.isEmpty() || !(pouch.getItem() instanceof ItemFocusPouch pouchItem)) {
                continue;
            }
            NonNullList<ItemStack> pinv = pouchItem.getInventory(pouch);
            for (int q = 0; q < pinv.size(); q++) {
                if (pinv.get(q).isEmpty()) {
                    pinv.set(q, focus.copy());
                    pouchItem.setInventory(pouch, pinv);
                    ref.handler().getStacks().setStackInSlot(ref.slot(), pouch);
                    return true;
                }
            }
        }
        return false;
    }

    public static void toggleMisc(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return;
        }
        if (wand.getFocus(stack) != null && wand.getFocus(stack) instanceof IArchitect
                && wand.getFocus(stack).isUpgradedWith(wand.getFocusStack(stack), FocusUpgradeType.architect)) {
            int dim = getAreaDim(stack);
            if (player.isCrouching()) {
                dim++;
                if (dim > (wand.getFocusStack(stack).getItem() instanceof ItemFocusTrade ? 2 : 3)) {
                    dim = 0;
                }
                setAreaDim(stack, dim);
            } else {
                int areax = getAreaX(stack);
                int areay = getAreaY(stack);
                int areaz = getAreaZ(stack);
                if (dim == 0) {
                    areax++;
                    areaz++;
                    areay++;
                } else if (dim == 1) {
                    areax++;
                } else if (dim == 2) {
                    areaz++;
                } else if (dim == 3) {
                    areay++;
                }
                if (areax > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                    areax = 0;
                }
                if (areaz > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                    areaz = 0;
                }
                if (areay > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                    areay = 0;
                }
                setAreaX(stack, areax);
                setAreaY(stack, areay);
                setAreaZ(stack, areaz);
            }
        }
    }

    public static int getAreaDim(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("aread")) {
            return tag.getInt("aread");
        }
        return 0;
    }

    public static int getAreaX(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return 1;
        }
        ItemFocusBasic focus = wand.getFocus(stack);
        if (focus == null) {
            return 1;
        }

        ItemStack focusStack = wand.getFocusStack(stack);
        int maxArea = focus.getMaxAreaSize(focusStack);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areax")) {
            int a = tag.getInt("areax");
            if (a > maxArea) {
                a = maxArea;
            }
            return a;
        }
        return maxArea;
    }

    public static int getAreaY(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return 1;
        }
        ItemFocusBasic focus = wand.getFocus(stack);
        if (focus == null) {
            return 1;
        }

        ItemStack focusStack = wand.getFocusStack(stack);
        int maxArea = focus.getMaxAreaSize(focusStack);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areay")) {
            int a = tag.getInt("areay");
            if (a > maxArea) {
                a = maxArea;
            }
            return a;
        }
        return maxArea;
    }

    public static int getAreaZ(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return 1;
        }
        ItemFocusBasic focus = wand.getFocus(stack);
        if (focus == null) {
            return 1;
        }

        ItemStack focusStack = wand.getFocusStack(stack);
        int maxArea = focus.getMaxAreaSize(focusStack);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("areaz")) {
            int a = tag.getInt("areaz");
            if (a > maxArea) {
                a = maxArea;
            }
            return a;
        }
        return maxArea;
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

    private static long cdToMillis(int cd) {
        return cd * 50L;
    }

    static boolean isOnCooldown(LivingEntity entity) {
        Map<Integer, Long> map = isClientSide(entity) ? cooldownClient : cooldownServer;
        Long end = map.get(entity.getId());
        return end != null && end > System.currentTimeMillis();
    }

    public static float getCooldown(LivingEntity entity) {
        Map<Integer, Long> map = isClientSide(entity) ? cooldownClient : cooldownServer;
        Long end = map.get(entity.getId());
        if (end == null) {
            return 0.0F;
        }

        long remaining = end - System.currentTimeMillis();
        return remaining > 0 ? (float) remaining / 1000.0F : 0.0F;
    }

    public static void setCooldown(LivingEntity entity, int cd) {
        if (cd <= 0) {
            cooldownClient.remove(entity.getId());
            cooldownServer.remove(entity.getId());
            return;
        }

        long endTime = System.currentTimeMillis() + cdToMillis(cd);
        if (isClientSide(entity)) {
            cooldownClient.put(entity.getId(), endTime);
        } else {
            cooldownServer.put(entity.getId(), endTime);
        }
    }

    public static void clearCooldown(LivingEntity entity) {
        cooldownClient.remove(entity.getId());
        cooldownServer.remove(entity.getId());
    }

    private record CurioPouchRef(
            top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler handler, int slot) {
    }
}