package arcaneresearch.api.crafting;

@Deprecated

public interface IStabilizable {

    @Deprecated
    void addStability();

    @Deprecated
    EnumStability getStability();

    @Deprecated
    public enum EnumStability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE
    }
}
