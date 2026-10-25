package arcaneresearch.api.aspects;
import java.io.Serializable;
import java.util.LinkedHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
public class AspectList implements Serializable {
    private static final long serialVersionUID = 1L;
    public LinkedHashMap<Aspect, Integer> aspects = new LinkedHashMap<>();
    public AspectList(ItemStack stack) {
        try {
            AspectList temp = AspectHelper.getObjectAspects(stack);
            if (temp != null) {
                for (Aspect tag : temp.getAspects()) {
                    add(tag, temp.getAmount(tag));
                }
            }
        } catch (Exception e) {
        }
    }
    public AspectList() {
    }
    public AspectList copy() {
        AspectList out = new AspectList();
        for (Aspect a : getAspects()) {
            out.add(a, getAmount(a));
        }
        return out;
    }
    public int size() {
        return aspects.size();
    }
    public int visSize() {
        int q = 0;
        for (Aspect as : aspects.keySet()) {
            q += getAmount(as);
        }
        return q;
    }
    public Aspect[] getAspects() {
        return aspects.keySet().toArray(new Aspect[0]);
    }
    public Aspect[] getAspectsSortedByName() {
        try {
            Aspect[] out = aspects.keySet().toArray(new Aspect[0]);
            boolean change;
            do {
                change = false;
                for (int a = 0; a < out.length - 1; a++) {
                    Aspect e1 = out[a];
                    Aspect e2 = out[a + 1];
                    if (e1 != null && e2 != null && e1.getTag().compareTo(e2.getTag()) > 0) {
                        out[a] = e2;
                        out[a + 1] = e1;
                        change = true;
                        break;
                    }
                }
            } while (change);
            return out;
        } catch (Exception e) {
            return getAspects();
        }
    }
    public Aspect[] getAspectsSortedByAmount() {
        try {
            Aspect[] out = aspects.keySet().toArray(new Aspect[0]);
            boolean change;
            do {
                change = false;
                for (int a = 0; a < out.length - 1; a++) {
                    int e1 = getAmount(out[a]);
                    int e2 = getAmount(out[a + 1]);
                    if (e1 > 0 && e2 > 0 && e2 > e1) {
                        Aspect ea = out[a];
                        Aspect eb = out[a + 1];
                        out[a] = eb;
                        out[a + 1] = ea;
                        change = true;
                        break;
                    }
                }
            } while (change);
            return out;
        } catch (Exception e) {
            return getAspects();
        }
    }
    public int getAmount(Aspect key) {
        return aspects.get(key) == null ? 0 : aspects.get(key);
    }
    public boolean reduce(Aspect key, int amount) {
        if (getAmount(key) >= amount) {
            int am = getAmount(key) - amount;
            aspects.put(key, am);
            return true;
        }
        return false;
    }
    public AspectList remove(Aspect key, int amount) {
        int am = getAmount(key) - amount;
        if (am <= 0) {
            aspects.remove(key);
        } else {
            aspects.put(key, am);
        }
        return this;
    }
    public AspectList remove(Aspect key) {
        aspects.remove(key);
        return this;
    }
    public AspectList add(Aspect aspect, int amount) {
        if (aspect == null) return this;
        if (aspects.containsKey(aspect)) {
            int oldamount = aspects.get(aspect);
            amount += oldamount;
        }
        aspects.put(aspect, amount);
        return this;
    }
    public AspectList merge(Aspect aspect, int amount) {
        if (aspects.containsKey(aspect)) {
            int oldamount = aspects.get(aspect);
            if (amount < oldamount) {
                amount = oldamount;
            }
        }
        aspects.put(aspect, amount);
        return this;
    }
    public AspectList add(AspectList in) {
        for (Aspect a : in.getAspects()) {
            add(a, in.getAmount(a));
        }
        return this;
    }
    public AspectList remove(AspectList in) {
        for (Aspect a : in.getAspects()) {
            remove(a, in.getAmount(a));
        }
        return this;
    }
    public AspectList merge(AspectList in) {
        for (Aspect a : in.getAspects()) {
            merge(a, in.getAmount(a));
        }
        return this;
    }
    public boolean contains(AspectList required) {
        for (Aspect a : required.getAspects()) {
            if (getAmount(a) < required.getAmount(a)) {
                return false;
            }
        }
        return true;
    }
    public boolean contains(Aspect aspect) {
        return aspects.containsKey(aspect) && aspects.get(aspect) > 0;
    }
    public void readFromNBT(CompoundTag nbt) {
        aspects.clear();
        ListTag tlist = nbt.getList("Aspects", Tag.TAG_COMPOUND);
        for (int j = 0; j < tlist.size(); j++) {
            CompoundTag rs = tlist.getCompound(j);
            if (rs.contains("key")) {
                Aspect aspect = Aspect.getAspect(rs.getString("key"));
                if (aspect != null) {
                    add(aspect, rs.getInt("amount"));
                }
            }
        }
    }
    public void readFromNBT(CompoundTag nbt, String label) {
        aspects.clear();
        ListTag tlist = nbt.getList(label, Tag.TAG_COMPOUND);
        for (int j = 0; j < tlist.size(); j++) {
            CompoundTag rs = tlist.getCompound(j);
            if (rs.contains("key")) {
                Aspect aspect = Aspect.getAspect(rs.getString("key"));
                if (aspect != null) {
                    add(aspect, rs.getInt("amount"));
                }
            }
        }
    }
    public void writeToNBT(CompoundTag nbt) {
        ListTag tlist = new ListTag();
        nbt.put("Aspects", tlist);
        for (Aspect aspect : getAspects()) {
            if (aspect != null) {
                CompoundTag f = new CompoundTag();
                f.putString("key", aspect.getTag());
                f.putInt("amount", getAmount(aspect));
                tlist.add(f);
            }
        }
    }
    public void writeToNBT(CompoundTag nbt, String label) {
        ListTag tlist = new ListTag();
        nbt.put(label, tlist);
        for (Aspect aspect : getAspects()) {
            if (aspect != null) {
                CompoundTag f = new CompoundTag();
                f.putString("key", aspect.getTag());
                f.putInt("amount", getAmount(aspect));
                tlist.add(f);
            }
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AspectList[");
        boolean first = true;
        for (Aspect a : getAspects()) {
            if (!first) sb.append(", ");
            sb.append(a.getTag()).append("=").append(getAmount(a));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
