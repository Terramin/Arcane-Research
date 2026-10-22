package arcaneresearch.common.items.wands;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchInvHelper;
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
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.TreeMap;



public class WandManager {
    static HashMap<Integer, Long> cooldownServer = new HashMap<>();
    static HashMap<Integer, Long> cooldownClient = new HashMap<>();
    public WandTriggers triggers = new WandTriggers();

    public static int getBaseChargeRate(Player player, boolean currentItem, int slot) {
        if (WandResearchHelper.isResearchComplete(player, "NODETAPPER2") && slot < 9) {
            return 3;
        }
        if (WandResearchHelper.isResearchComplete(player, "NODETAPPER1") && slot < 9) {
            return 2;
        }
        return currentItem ? 1 : 0;
    }

    public static float getTotalVisDiscount(Player player, Aspect aspect) {
        if (player == null) {
            return 0.0F;
        }
        int total = 0;
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
        } catch (Exception ignored) {
        }
        total += curiosTotal[0];
        
        
        for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(armorSlot);
            if (!armor.isEmpty() && armor.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(armor, player);
            }
        }
        MobEffectInstance visExhaust = null;
        MobEffectInstance infectious = null;
        try {
            
            visExhaust = player.getEffect(ModEffects.VIS_EXHAUST);
            infectious = player.getEffect(ModEffects.INFECTIOUS_VIS_EXHAUST);
        } catch (Exception ignored) {
        }
        if (visExhaust != null || infectious != null) {
            int level1 = visExhaust == null ? 0 : visExhaust.getAmplifier();
            int level2 = infectious == null ? 0 : infectious.getAmplifier();
            total -= (Math.max(level1, level2) + 1) * 10;
        }
        return total / 100.0F;
    }

    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        
        try {
            final boolean[] done = {false};
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach(
                    (slotId, slotHandler) -> {
                        for (int i = 0; i < slotHandler.getStacks().getSlots() && !done[0]; i++) {
                            ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                            if (!worn.isEmpty()
                                    && worn.getItem() instanceof arcaneresearch.common.items.baubles.ItemAmuletVis amulet
                                    && amulet.consumeAllVis(worn, player, cost, true, true)) {
                                done[0] = true;
                            }
                        }
                    }));
            if (done[0]) {
                return true;
            }
        } catch (Exception ignored) {
        }
        
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        
        
        for (int a = inv.getNonEquipmentItems().size() - 1; a >= 0; a--) {
            ItemStack stack = inv.getItem(a);
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
        int pouchcount = 0;
        ItemStack item = ItemStack.EMPTY;
        java.util.List<CurioPouchRef> curioPouches = new java.util.ArrayList<>();
        final int[] pouchCountBox = {0};
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((slotId, slotHandler) -> {
                for (int i = 0; i < slotHandler.getStacks().getSlots(); i++) {
                    ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                    if (!worn.isEmpty() && worn.getItem() instanceof ItemFocusPouch pouch) {
                        pouchCountBox[0]++;
                        curioPouches.add(new CurioPouchRef(slotHandler, i));
                        NonNullList<ItemStack> pinv = pouch.getInventory(worn);
                        for (int q = 0; q < pinv.size(); q++) {
                            ItemStack f = pinv.get(q);
                            if (!f.isEmpty() && f.getItem() instanceof ItemFocusBasic basic) {
                                foci.put(basic.getSortingHelper(f), q + pouchCountBox[0] * 1000);
                            }
                        }
                    }
                }
            }));
        } catch (Exception ignored) {
        }
        pouchcount = pouchCountBox[0];
        for (int ci = 0; ci < curioPouches.size(); ci++) {
            pouches.put(ci + 1, -1000 - ci);
        }
        
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        
        for (int a = 0; a < inv.getNonEquipmentItems().size(); a++) {
            item = inv.getItem(a);
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
        if (focus.equals("REMOVE") || foci.isEmpty()) {
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
                item = inv.getItem(loc).copy();
            } else {
                int pid = loc / 1000;
                if (pouches.containsKey(pid)) {
                    int pouchslot = pouches.get(pid);
                    int focusslot = loc - pid * 1000;
                    if (pouchslot <= -1000) {
                        CurioPouchRef ref = curioPouches.get(-1000 - pouchslot);
                        ItemStack pouch = ref.handler().getStacks().getStackInSlot(ref.slot()).copy();
                        item = fetchFocusFromCurioPouch(player, focusslot, pouch, ref);
                    } else {
                        ItemStack pouch = inv.getItem(pouchslot).copy();
                        item = fetchFocusFromPouch(player, focusslot, pouch, pouchslot);
                    }
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
                                           java.util.List<CurioPouchRef> curioPouches) {
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
        
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("aread")) {
            return tag.getIntOr("aread", 0);
        }
        return 0;
    }

    public static int getAreaX(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand) || wand.getFocus(stack) == null) {
            return 1;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("areax")) {
            int a = tag.getIntOr("areax", 0);
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
            }
            return a;
        }
        return wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
    }

    public static int getAreaY(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand) || wand.getFocus(stack) == null) {
            return 1;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("areay")) {
            int a = tag.getIntOr("areay", 0);
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
            }
            return a;
        }
        return wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
    }

    public static int getAreaZ(ItemStack stack) {
        if (!(stack.getItem() instanceof IWand wand) || wand.getFocus(stack) == null) {
            return 1;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("areaz")) {
            int a = tag.getIntOr("areaz", 0);
            if (a > wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack))) {
                a = wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
            }
            return a;
        }
        return wand.getFocus(stack).getMaxAreaSize(wand.getFocusStack(stack));
    }

    public static void setAreaX(ItemStack stack, int area) {
        
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putInt("areax", area);
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    public static void setAreaY(ItemStack stack, int area) {
        
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putInt("areay", area);
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    public static void setAreaZ(ItemStack stack, int area) {
        
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putInt("areaz", area);
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    public static void setAreaDim(ItemStack stack, int dim) {
        
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putInt("aread", dim);
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    static boolean isOnCooldown(LivingEntity entity) {
        
        if (entity.level().isClientSide() && cooldownClient.containsKey(entity.getId())) {
            return cooldownClient.get(entity.getId()) > System.currentTimeMillis();
        }
        if (!entity.level().isClientSide() && cooldownServer.containsKey(entity.getId())) {
            return cooldownServer.get(entity.getId()) > System.currentTimeMillis();
        }
        return false;
    }

    public static float getCooldown(LivingEntity entity) {
        if (entity.level().isClientSide() && cooldownClient.containsKey(entity.getId())) {
            return (float) (cooldownClient.get(entity.getId()) - System.currentTimeMillis()) / 1000.0F;
        }
        return 0.0F;
    }

    public static void setCooldown(LivingEntity entity, int cd) {
        if (cd == 0) {
            cooldownClient.remove(entity.getId());
            cooldownServer.remove(entity.getId());
        } else if (entity.level().isClientSide()) {
            cooldownClient.put(entity.getId(), System.currentTimeMillis() + cd);
        } else {
            cooldownServer.put(entity.getId(), System.currentTimeMillis() + cd);
        }
    }

    private record CurioPouchRef(
            top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler handler, int slot) {
    }
}
