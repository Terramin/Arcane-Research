package arcaneresearch.common.lib.crafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.common.items.wands.WandItems;
import arcaneresearch.common.lib.wands.WandResearchHelper;
public class ArcaneWandRecipe {
    private ArcaneWandRecipe() {
    }
    private static ItemStack getSlot(CraftingContainer inv, int x, int y) {
        if (x < 0 || y < 0 || x >= inv.getWidth() || y >= inv.getHeight()) {
            return ItemStack.EMPTY;
        }
        return inv.getItem(x + y * inv.getWidth());
    }
    public static ItemStack getCraftingResult(CraftingContainer inv) {
        ItemStack out = ItemStack.EMPTY;
        String bc = null;
        String br = null;
        ItemStack cap1 = getSlot(inv, 0, 2);
        ItemStack cap2 = getSlot(inv, 2, 0);
        ItemStack rod = getSlot(inv, 1, 1);
        if (!getSlot(inv, 0, 0).isEmpty() || !getSlot(inv, 0, 1).isEmpty() || !getSlot(inv, 1, 0).isEmpty()
                || !getSlot(inv, 1, 2).isEmpty() || !getSlot(inv, 2, 1).isEmpty() || !getSlot(inv, 2, 2).isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!cap1.isEmpty() && !cap2.isEmpty() && !rod.isEmpty() && checkItemEquals(cap1, cap2)) {
            for (WandCap wc : WandCap.caps.values()) {
                if (matchesCapComponent(cap1, wc)) {
                    bc = wc.getTag();
                    break;
                }
            }
            for (WandRod wr : WandRod.rods.values()) {
                if (checkItemEquals(rod, wr.getItem())) {
                    br = wr.getTag();
                    break;
                }
            }
            if (bc != null && br != null) {
                out = WandItems.createWandStack();
                if (out.getItem() instanceof IWand w) {
                    w.setCap(out, WandCap.caps.get(bc));
                    w.setRod(out, WandRod.rods.get(br));
                }
            }
        }
        return out;
    }
    public static AspectList getVisCost(CraftingContainer inv) {
        AspectList al = new AspectList();
        int cc = -1;
        int cr = -1;
        ItemStack cap1 = getSlot(inv, 0, 2);
        ItemStack cap2 = getSlot(inv, 2, 0);
        ItemStack rod = getSlot(inv, 1, 1);
        if (!getSlot(inv, 0, 0).isEmpty() || !getSlot(inv, 0, 1).isEmpty() || !getSlot(inv, 1, 0).isEmpty()
                || !getSlot(inv, 1, 2).isEmpty() || !getSlot(inv, 2, 1).isEmpty() || !getSlot(inv, 2, 2).isEmpty()) {
            return al;
        }
        if (!cap1.isEmpty() && !cap2.isEmpty() && !rod.isEmpty() && checkItemEquals(cap1, cap2)) {
            for (WandCap wc : WandCap.caps.values()) {
                if (matchesCapComponent(cap1, wc)) {
                    cc = wc.getCraftCost();
                    break;
                }
            }
            for (WandRod wr : WandRod.rods.values()) {
                if (checkItemEquals(rod, wr.getItem())) {
                    cr = wr.getCraftCost();
                    break;
                }
            }
            if (cc >= 0 && cr >= 0) {
                int cost = wandCrystalCost(cc, cr);
                for (Aspect as : Aspect.getPrimalAspects()) {
                    al.add(as, cost);
                }
            }
        }
        return al;
    }
    public static int wandCrystalCost(int capCost, int rodCost) {
        return Math.min(capCost + rodCost, 64);
    }
    public static int wandVisCost(int capCost, int rodCost) {
        return Math.min((capCost + rodCost) * 30, 1200);
    }
    public static boolean matches(CraftingContainer inv, Player player) {
        ItemStack cap1 = getSlot(inv, 0, 2);
        ItemStack cap2 = getSlot(inv, 2, 0);
        ItemStack rod = getSlot(inv, 1, 1);
        if (!getSlot(inv, 0, 0).isEmpty() || !getSlot(inv, 0, 1).isEmpty() || !getSlot(inv, 1, 0).isEmpty()
                || !getSlot(inv, 1, 2).isEmpty() || !getSlot(inv, 2, 1).isEmpty() || !getSlot(inv, 2, 2).isEmpty()) {
            return false;
        }
        return checkMatch(cap1, cap2, rod, player);
    }
    public static ItemStack getSceptreResult(CraftingContainer inv) {
        SceptreParts parts = readSceptre(inv);
        if (parts == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = WandItems.createWandStack();
        if (out.getItem() instanceof IWand w) {
            w.setCap(out, WandCap.caps.get(parts.cap));
            w.setRod(out, WandRod.rods.get(parts.rod));
            arcaneresearch.common.items.wands.ItemWand.setSceptre(out);
        }
        return out;
    }
    public static AspectList getSceptreVisCost(CraftingContainer inv) {
        AspectList al = new AspectList();
        SceptreParts parts = readSceptre(inv);
        if (parts == null) {
            return al;
        }
        WandCap cap = WandCap.caps.get(parts.cap);
        WandRod rod = WandRod.rods.get(parts.rod);
        if (cap == null || rod == null) {
            return al;
        }
        int amount = Math.min((int) ((cap.getCraftCost() + rod.getCraftCost()) * 1.5F), 96);
        for (Aspect as : Aspect.getPrimalAspects()) {
            al.add(as, amount);
        }
        return al;
    }
    public static boolean matchesSceptre(CraftingContainer inv, Player player) {
        SceptreParts parts = readSceptre(inv);
        if (parts == null) {
            return false;
        }
        return checkSceptreMatch(parts, player);
    }
    private record SceptreParts(String cap, String rod) {
    }
    private static SceptreParts readSceptre(CraftingContainer inv) {
        ItemStack cap1 = getSlot(inv, 1, 0);
        ItemStack cap2 = getSlot(inv, 2, 1);
        ItemStack cap3 = getSlot(inv, 0, 2);
        ItemStack rod = getSlot(inv, 1, 1);
        ItemStack salis = getSlot(inv, 2, 0);
        if (!getSlot(inv, 0, 0).isEmpty() || !getSlot(inv, 0, 1).isEmpty()
                || !getSlot(inv, 1, 2).isEmpty() || !getSlot(inv, 2, 2).isEmpty()) {
            return null;
        }
        if (cap1.isEmpty() || cap2.isEmpty() || cap3.isEmpty() || rod.isEmpty() || salis.isEmpty()) {
            return null;
        }
        if (!checkItemEquals(cap1, cap2) || !checkItemEquals(cap1, cap3)) {
            return null;
        }
        if (!salis.is(arcaneresearch.init.ModItems.SALIS_MUNDUS.get())) {
            return null;
        }
        String bc = null;
        String br = null;
        for (WandCap wc : WandCap.caps.values()) {
            if (matchesCapComponent(cap1, wc)) {
                bc = wc.getTag();
                break;
            }
        }
        for (WandRod wr : WandRod.rods.values()) {
            if (checkItemEquals(rod, wr.getItem())) {
                br = wr.getTag();
                break;
            }
        }
        if (bc == null || br == null) {
            return null;
        }
        return new SceptreParts(bc, br);
    }
    private static boolean checkSceptreMatch(SceptreParts parts, Player player) {
        boolean bc = false;
        boolean br = false;
        for (WandCap wc : WandCap.caps.values()) {
            if (wc.getTag().equals(parts.cap)
                    && (player == null || WandResearchHelper.isResearchComplete(player, wc.getResearch()))) {
                bc = true;
                break;
            }
        }
        for (WandRod wr : WandRod.rods.values()) {
            if (wr.getTag().equals(parts.rod)
                    && (player == null || WandResearchHelper.isResearchComplete(player, wr.getResearch()))) {
                br = true;
                break;
            }
        }
        return br && bc;
    }
    private static boolean checkMatch(ItemStack cap1, ItemStack cap2, ItemStack rod, Player player) {
        boolean bc = false;
        boolean br = false;
        if (!cap1.isEmpty() && !cap2.isEmpty() && !rod.isEmpty() && checkItemEquals(cap1, cap2)) {
            for (WandCap wc : WandCap.caps.values()) {
                if (matchesCapComponent(cap1, wc)
                        && (player == null || WandResearchHelper.isResearchComplete(player, wc.getResearch()))) {
                    bc = true;
                    break;
                }
            }
            for (WandRod wr : WandRod.rods.values()) {
                if (checkItemEquals(rod, wr.getItem())
                        && (player == null || WandResearchHelper.isResearchComplete(player, wr.getResearch()))) {
                    br = true;
                    break;
                }
            }
        }
        return br && bc;
    }
    private static boolean matchesCapComponent(ItemStack component, WandCap wc) {
        if (component.getItem() instanceof arcaneresearch.common.items.wands.ItemWandCap) {
            return wc.getTag().equals(arcaneresearch.common.items.wands.ItemWandCap.getCapTag(component));
        }
        return false;
    }
    private static boolean checkItemEquals(ItemStack target, ItemStack input) {
        if (target.isEmpty() || input.isEmpty()) {
            return false;
        }
        if (target.getItem() != input.getItem()) {
            return false;
        }
        try {
            return ArcaneresearchApiHelper.areItemStackTagsEqualForCrafting(input, target);
        } catch (Exception e) {
            return true;
        }
    }
}
