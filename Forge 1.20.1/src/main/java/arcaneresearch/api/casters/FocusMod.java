package arcaneresearch.api.casters;
import arcaneresearch.api.aspects.Aspect;
public abstract class FocusMod extends FocusNode {
    @Override
    public EnumUnitType getType() {
        return EnumUnitType.MOD;
    }
    public abstract boolean execute();
    @Override
    public Aspect getAspect() {
        return null;
    }
    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return null;
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return null;
    }
}
