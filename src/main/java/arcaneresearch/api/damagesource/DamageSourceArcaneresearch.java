package arcaneresearch.api.damagesource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;
public class DamageSourceArcaneresearch {
    public static final ResourceKey<DamageType> TAINT = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(Arcaneresearch.MODID, "taint"));
    public static final ResourceKey<DamageType> TENTACLE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(Arcaneresearch.MODID, "tentacle"));
    public static final ResourceKey<DamageType> SWARM = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(Arcaneresearch.MODID, "swarm"));
    public static final ResourceKey<DamageType> DISSOLVE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(Arcaneresearch.MODID, "dissolve"));
    public static DamageSource createTaint(Level level) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TAINT)
        );
    }
    public static DamageSource causeTentacleDamage(LivingEntity attacker) {
        return new DamageSource(
                attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TENTACLE),
                attacker
        );
    }
    public static DamageSource causeSwarmDamage(LivingEntity attacker) {
        return new DamageSource(
                attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SWARM),
                attacker
        );
    }
    public static DamageSource createDissolve(Level level) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DISSOLVE)
        );
    }
    public static DamageSource createMagic(Level level, Entity source, Entity attacker) {
        return level.damageSources().indirectMagic(source, attacker);
    }
}
