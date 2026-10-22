package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModSounds;

public class ChampionModInfested implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (!champion.level().isClientSide() && champion.getHealth() - amount <= 0) {

            champion.playSound(ModSounds.GORE.get(), 0.5f, 1.0f);
            int count = 2 + champion.level().getRandom().nextInt(3);
            
            
            net.minecraft.world.entity.EntityType<?> silverfishType =
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "silverfish"));
            for (int i = 0; i < count; i++) {
                net.minecraft.world.entity.Entity created = silverfishType == null ? null
                        : silverfishType.create(champion.level(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                if (created instanceof Silverfish silverfish) {
                    double offsetX = (champion.level().getRandom().nextDouble() - 0.5) * champion.getBbWidth();
                    double offsetZ = (champion.level().getRandom().nextDouble() - 0.5) * champion.getBbWidth();
                    silverfish.setPos(champion.getX() + offsetX, champion.getY(), champion.getZ() + offsetZ);
                    champion.level().addFreshEntity(silverfish);
                }
            }
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity champion) {

        if (champion.level().getRandom().nextBoolean()) {
            FXDispatcher.INSTANCE.slimeJumpFX(champion, 0);
        }
    }
}