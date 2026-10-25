package arcaneresearch.common.golems.seals;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
public class SealGuardAdvanced extends SealGuard implements ISealConfigToggles {
    private ResourceLocation iconAdvanced;
    public SealGuardAdvanced() {
        super();
        iconAdvanced = new ResourceLocation(Arcaneresearch.MODID, "item/seals/seal_guard_advanced");
    }
    @Override
    public String getKey() {
        return "arcaneresearch:guard_advanced";
    }
    @Override
    public ResourceLocation getSealIcon() {
        return iconAdvanced;
    }
    @Override
    public int[] getGuiCategories() {
        return new int[]{2, 3, 0, 4};
    }
    @Override
    public SealToggle[] getToggles() {
        return props;
    }
    @Override
    public void setToggle(int index, boolean value) {
        if (index >= 0 && index < props.length) {
            props[index].setValue(value);
        }
    }
    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.SMART};
    }
}
