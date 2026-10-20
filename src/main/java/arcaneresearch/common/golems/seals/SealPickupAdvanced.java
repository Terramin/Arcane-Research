package arcaneresearch.common.golems.seals;

import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.seals.ISealConfigToggles;

public class SealPickupAdvanced extends SealPickup implements ISealConfigToggles {

    private Identifier iconAdvanced;

    public SealPickupAdvanced() {
        super();
        iconAdvanced = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_pickup_advanced");
    }

    @Override
    public String getKey() {
        return "arcaneresearch:pickup_advanced";
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public Identifier getSealIcon() {
        return iconAdvanced;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{2, 1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.SMART};
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
}
