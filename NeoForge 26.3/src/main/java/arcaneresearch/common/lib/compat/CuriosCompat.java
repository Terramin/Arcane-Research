package arcaneresearch.common.lib.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import arcaneresearch.api.items.IVisDiscountGear;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CuriosCompat {

    private static Boolean curiosLoaded = null;

    public static boolean isCuriosLoaded() {
        if (curiosLoaded == null) {
            curiosLoaded = ModList.get().isLoaded("curios");
        }
        return curiosLoaded;
    }

    public static int getVisDiscountFromCurios(Player player) {
        if (!isCuriosLoaded() || player == null) {
            return 0;
        }

        try {
            return CuriosHelper.getVisDiscount(player);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {

            curiosLoaded = false;
            return 0;
        }
    }

    public static void forEachCuriosItem(Player player, Consumer<ItemStack> consumer) {
        if (!isCuriosLoaded() || player == null) {
            return;
        }

        try {
            CuriosHelper.forEachItem(player, consumer);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
        }
    }

    public static void forEachCuriosItemWithSlot(Player player, BiConsumer<String, ItemStack> consumer) {
        if (!isCuriosLoaded() || player == null) {
            return;
        }

        try {
            CuriosHelper.forEachItemWithSlot(player, consumer);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
        }
    }

    public static List<ItemStack> findCuriosItems(Player player, Predicate<ItemStack> predicate) {        List<ItemStack> result = new ArrayList<>();
        if (!isCuriosLoaded() || player == null) {
            return result;
        }

        try {
            return CuriosHelper.findItems(player, predicate);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
            return result;
        }
    }

    public static boolean isEquipped(Player player, net.minecraft.world.item.Item item) {
        return !findCuriosItems(player, s -> !s.isEmpty() && s.getItem() == item).isEmpty();
    }

    /**
     * Предметы только из curio-слотов головы (identifier "head"):
     * очки работают либо в шлеме, либо здесь.
     */
    public static List<ItemStack> findHeadSlotItems(Player player, Predicate<ItemStack> predicate) {
        List<ItemStack> result = new ArrayList<>();
        if (!isCuriosLoaded() || player == null) {
            return result;
        }
        try {
            for (CuriosSlotRef ref : CuriosHelper.findRefs(player, predicate)) {
                if (ref.slotId() != null && ref.slotId().equalsIgnoreCase("head")) {
                    ItemStack stack = CuriosHelper.getStack(player, ref);
                    if (!stack.isEmpty() && predicate.test(stack)) {
                        result.add(stack);
                    }
                }
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
        }
        return result;
    }

    public static List<CuriosSlotRef> findCuriosSlotRefs(Player player, Predicate<ItemStack> predicate) {
        List<CuriosSlotRef> result = new ArrayList<>();
        if (!isCuriosLoaded() || player == null) {
            return result;
        }
        try {
            return CuriosHelper.findRefs(player, predicate);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
            return result;
        }
    }

    public static ItemStack getCuriosStack(Player player, CuriosSlotRef ref) {
        if (!isCuriosLoaded() || player == null || ref == null) {
            return ItemStack.EMPTY;
        }
        try {
            return CuriosHelper.getStack(player, ref);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
            return ItemStack.EMPTY;
        }
    }

    public static void setCuriosStack(Player player, CuriosSlotRef ref, ItemStack stack) {
        if (!isCuriosLoaded() || player == null || ref == null) {
            return;
        }
        try {
            CuriosHelper.setStack(player, ref, stack);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            curiosLoaded = false;
        }
    }

    public record CuriosSlotRef(String slotId, int index) {
    }

    private static class CuriosHelper {

        static int getVisDiscount(Player player) {
            AtomicInteger total = new AtomicInteger(0);

            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack stack = slotHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof IVisDiscountGear gear) {
                            total.addAndGet(gear.getVisDiscount(stack, player));
                        }
                    }
                });
            });

            return total.get();
        }

        static void forEachItem(Player player, Consumer<ItemStack> consumer) {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack stack = slotHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            consumer.accept(stack);
                        }
                    }
                });
            });
        }

        static void forEachItemWithSlot(Player player, BiConsumer<String, ItemStack> consumer) {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack stack = slotHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            consumer.accept(slotId + ":" + i, stack);
                        }
                    }
                });
            });
        }

        static List<ItemStack> findItems(Player player, Predicate<ItemStack> predicate) {
            List<ItemStack> result = new ArrayList<>();

            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack stack = slotHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && predicate.test(stack)) {
                            result.add(stack);
                        }
                    }
                });
            });

            return result;
        }

        static List<CuriosSlotRef> findRefs(Player player, Predicate<ItemStack> predicate) {
            List<CuriosSlotRef> result = new ArrayList<>();

            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((slotId, slotHandler) -> {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack stack = slotHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && predicate.test(stack)) {
                            result.add(new CuriosSlotRef(slotId, i));
                        }
                    }
                });
            });

            return result;
        }

        static ItemStack getStack(Player player, CuriosSlotRef ref) {
            AtomicReference<ItemStack> out = new AtomicReference<>(ItemStack.EMPTY);

            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                var slotHandler = handler.getCurios().get(ref.slotId());
                if (slotHandler != null && ref.index() >= 0 && ref.index() < slotHandler.getSlots()) {
                    out.set(slotHandler.getStacks().getStackInSlot(ref.index()));
                }
            });

            return out.get();
        }

        static void setStack(Player player, CuriosSlotRef ref, ItemStack stack) {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                var slotHandler = handler.getCurios().get(ref.slotId());
                if (slotHandler != null && ref.index() >= 0 && ref.index() < slotHandler.getSlots()) {
                    slotHandler.getStacks().setStackInSlot(ref.index(), stack);
                }
            });
        }
    }
}
