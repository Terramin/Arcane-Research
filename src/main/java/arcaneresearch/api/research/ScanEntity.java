package arcaneresearch.api.research;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.ArcaneresearchApi;
public class ScanEntity implements IScanThing {
    private final String research;
    private final Class<?> entityClass;
    private final boolean inheritedClasses;
    private final ArcaneresearchApi.EntityTagsNBT[] nbtData;
    public ScanEntity(String research, Class<?> entityClass, boolean inheritedClasses) {
        this(research, entityClass, inheritedClasses, (ArcaneresearchApi.EntityTagsNBT[]) null);
    }
    public ScanEntity(String research, Class<?> entityClass, boolean inheritedClasses,
            ArcaneresearchApi.EntityTagsNBT... nbt) {
        this.research = research;
        this.entityClass = entityClass;
        this.inheritedClasses = inheritedClasses;
        this.nbtData = nbt;
    }
    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;
        if ((!inheritedClasses && entityClass == obj.getClass())
                || (inheritedClasses && entityClass.isInstance(obj))) {
            if (nbtData != null && nbtData.length > 0 && obj instanceof Entity entity) {
                net.minecraft.nbt.CompoundTag tc = new net.minecraft.nbt.CompoundTag();
                entity.saveWithoutId(tc);
                for (ArcaneresearchApi.EntityTagsNBT nbt : nbtData) {
                    if (!tc.contains(nbt.name) || !arcaneresearch.api.ArcaneresearchApiHelper.getNBTDataFromId(
                            tc, tc.getTagType(nbt.name), nbt.name).equals(nbt.value)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
