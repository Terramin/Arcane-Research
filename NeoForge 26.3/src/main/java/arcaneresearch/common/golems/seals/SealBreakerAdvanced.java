package arcaneresearch.common.golems.seals;

import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;

public class SealBreakerAdvanced extends SealBreaker {

    private Identifier iconAdvanced;

    public SealBreakerAdvanced() {
        super();
        iconAdvanced = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_breaker_advanced");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(false, "psilk", "golem.prop.silk")
        };
    }

    @Override
    public String getKey() {
        return "arcaneresearch:breaker_advanced";
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
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.BREAKER, EnumGolemTrait.SMART};
    }

    @Override
    public boolean isSilkTouch() {
        return super.isSilkTouch();
    }
}
