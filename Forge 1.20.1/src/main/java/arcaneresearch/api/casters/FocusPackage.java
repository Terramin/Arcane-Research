package arcaneresearch.api.casters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
public class FocusPackage implements IFocusElement {
    @Override
    public String getResearch() {
        return null;
    }
    @Override
    public String getKey() {
        return "arcaneresearch.PACKAGE";
    }
    @Override
    public EnumUnitType getType() {
        return EnumUnitType.PACKAGE;
    }
    public List<IFocusElement> nodes = Collections.synchronizedList(new ArrayList<>());
    public Level world;
    private LivingEntity caster;
    private UUID casterUUID;
    public int index = 0;
    UUID uid;
    private int complexity = 0;
    private float power = 1.0f;
    public FocusPackage() {
    }
    public FocusPackage(LivingEntity caster) {
        super();
        world = caster.level();
        this.caster = caster;
        casterUUID = caster.getUUID();
    }
    public int getComplexity() {
        return complexity;
    }
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }
    public UUID getUniqueID() {
        return uid;
    }
    public void setUniqueID(UUID id) {
        uid = id;
    }
    public int getExecutionIndex() {
        return index;
    }
    public void setExecutionIndex(int idx) {
        index = idx;
    }
    public void addNode(IFocusElement e) {
        nodes.add(e);
    }
    public void addNode(FocusNode node) {
        nodes.add(node);
    }
    public UUID getCasterUUID() {
        if (caster != null) casterUUID = caster.getUUID();
        return casterUUID;
    }
    public void setCasterUUID(UUID casterUUID) {
        this.casterUUID = casterUUID;
    }
    public LivingEntity getCaster() {
        try {
            if (caster == null && world != null) {
                if (world instanceof ServerLevel serverLevel && getCasterUUID() != null) {
                    Entity entity = serverLevel.getEntity(getCasterUUID());
                    if (entity instanceof LivingEntity living) {
                        caster = living;
                    }
                }
                if (caster == null && getCasterUUID() != null) {
                    for (LivingEntity e : world.getEntitiesOfClass(LivingEntity.class,
                            new net.minecraft.world.phys.AABB(-30000000, -64, -30000000, 30000000, 320, 30000000),
                            LivingEntity::isAlive)) {
                        if (getCasterUUID().equals(e.getUUID())) {
                            caster = e;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return caster;
    }
    public void setCaster(LivingEntity caster) {
        this.caster = caster;
        if (caster != null) {
            this.casterUUID = caster.getUUID();
        }
    }
    public FocusEffect[] getFocusEffects() {
        return getFocusEffectsPackage(this);
    }
    public List<IFocusElement> getEffects() {
        List<IFocusElement> effects = new ArrayList<>();
        for (FocusEffect e : getFocusEffects()) {
            effects.add(e);
        }
        return effects;
    }
    private FocusEffect[] getFocusEffectsPackage(FocusPackage fp) {
        ArrayList<FocusEffect> out = new ArrayList<>();
        for (IFocusElement el : fp.nodes) {
            if (el instanceof FocusEffect) out.add((FocusEffect) el);
            else if (el instanceof FocusPackage) {
                for (FocusEffect fep : getFocusEffectsPackage((FocusPackage) el))
                    out.add(fep);
            } else if (el instanceof FocusModSplit split) {
                for (FocusPackage fsp : split.getSplitPackages())
                    for (FocusEffect fep : getFocusEffectsPackage(fsp))
                        out.add(fep);
            }
        }
        return out.toArray(new FocusEffect[]{});
    }
    public int getSortingHelper() {
        String s = "";
        for (IFocusElement k : nodes) {
            s += k.getKey();
            if (k instanceof FocusNode && ((FocusNode) k).getSettingList() != null)
                for (String ns : ((FocusNode) k).getSettingList()) {
                    s += "" + ((FocusNode) k).getSettingValue(ns);
                }
        }
        return s.hashCode();
    }
    public void deserialize(CompoundTag nbt) {
        if (nbt.contains("uidMost") && nbt.contains("uidLeast")) {
            uid = new UUID(nbt.getLong("uidMost"), nbt.getLong("uidLeast"));
        }
        index = nbt.getInt("index");
        if (nbt.contains("casterUUIDMost") && nbt.contains("casterUUIDLeast")) {
            setCasterUUID(new UUID(nbt.getLong("casterUUIDMost"), nbt.getLong("casterUUIDLeast")));
        }
        power = nbt.getFloat("power");
        complexity = nbt.getInt("complexity");
        ListTag nodelist = nbt.getList("nodes", Tag.TAG_COMPOUND);
        nodes.clear();
        for (int x = 0; x < nodelist.size(); x++) {
            CompoundTag nodenbt = nodelist.getCompound(x);
            EnumUnitType ut;
            try {
                ut = EnumUnitType.valueOf(nodenbt.getString("type"));
            } catch (Exception e) {
                continue;
            }
            if (ut != null) {
                if (ut == EnumUnitType.PACKAGE) {
                    FocusPackage fp = new FocusPackage();
                    fp.deserialize(nodenbt.getCompound("package"));
                    nodes.add(fp);
                    break;
                } else {
                    IFocusElement fn = FocusEngine.getElement(nodenbt.getString("key"));
                    if (fn != null) {
                        if (fn instanceof FocusNode) {
                            ((FocusNode) fn).initialize();
                            if (((FocusNode) fn).getSettingList() != null)
                                for (String ns : ((FocusNode) fn).getSettingList()) {
                                    if (nodenbt.contains("setting." + ns)) {
                                        ((FocusNode) fn).getSetting(ns).setValue(nodenbt.getInt("setting." + ns));
                                    }
                                }
                            if (fn instanceof FocusModSplit split) {
                                if (nodenbt.contains("packages")) {
                                    split.deserialize(nodenbt.getCompound("packages"));
                                }
                            }
                        }
                        addNode(fn);
                    }
                }
            }
        }
    }
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        if (uid != null) {
            nbt.putLong("uidMost", uid.getMostSignificantBits());
            nbt.putLong("uidLeast", uid.getLeastSignificantBits());
        }
        nbt.putInt("index", index);
        if (getCasterUUID() != null) {
            nbt.putLong("casterUUIDMost", getCasterUUID().getMostSignificantBits());
            nbt.putLong("casterUUIDLeast", getCasterUUID().getLeastSignificantBits());
        }
        nbt.putFloat("power", power);
        nbt.putInt("complexity", complexity);
        if (world != null) {
            nbt.putString("dim", world.dimension().location().toString());
        }
        ListTag nodelist = new ListTag();
        synchronized (nodes) {
            for (IFocusElement node : nodes) {
                if (node == null || node.getType() == null) continue;
                CompoundTag nodenbt = new CompoundTag();
                nodenbt.putString("type", node.getType().name());
                nodenbt.putString("key", node.getKey());
                if (node.getType() == EnumUnitType.PACKAGE) {
                    nodenbt.put("package", ((FocusPackage) node).serialize());
                    nodelist.add(nodenbt);
                    break;
                } else {
                    if (node instanceof FocusNode && ((FocusNode) node).getSettingList() != null)
                        for (String ns : ((FocusNode) node).getSettingList()) {
                            nodenbt.putInt("setting." + ns, ((FocusNode) node).getSettingValue(ns));
                        }
                    if (node instanceof FocusModSplit split) {
                        nodenbt.put("packages", split.serialize());
                    }
                    nodelist.add(nodenbt);
                }
            }
        }
        nbt.put("nodes", nodelist);
        return nbt;
    }
    public static FocusPackage createSimple(String effectKey) {
        FocusPackage pack = new FocusPackage();
        IFocusElement el = FocusEngine.getElement(effectKey);
        if (el != null) {
            pack.addNode(el);
            if (el instanceof FocusNode fn) {
                pack.setComplexity(fn.getComplexity());
            } else {
                pack.setComplexity(10);
            }
        } else {
            pack.setComplexity(10);
        }
        return pack;
    }
    public float getPower() {
        return power;
    }
    public void setPower(float power) {
        this.power = power;
    }
    public void multiplyPower(float pow) {
        power *= pow;
    }
    public FocusPackage copy(LivingEntity caster) {
        FocusPackage fp = new FocusPackage(caster);
        fp.deserialize(serialize());
        return fp;
    }
    public void initialize(LivingEntity caster) {
        world = caster.level();
        if (nodes.isEmpty()) return;
        IFocusElement node = nodes.get(0);
        if (node instanceof FocusMediumRoot root && root.supplyTargets() == null) {
            root.setupFromCaster(caster);
        }
    }
}
