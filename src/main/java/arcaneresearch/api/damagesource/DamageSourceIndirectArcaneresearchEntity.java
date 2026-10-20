package arcaneresearch.api.damagesource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
public final class DamageSourceIndirectArcaneresearchEntity {
    private DamageSourceIndirectArcaneresearchEntity() {
    }
    public static DamageSource create(Level level, ResourceKey<DamageType> type, Entity source, Entity attacker) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type),
                source,
                attacker
        );
    }
    public static DamageSource createMagic(Level level, Entity source, Entity attacker) {
        return level.damageSources().indirectMagic(source, attacker);
    }
    public static DamageSource createUnblockable(Level level, Entity source, Entity attacker) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC),
                source,
                attacker
        );
    }
}
