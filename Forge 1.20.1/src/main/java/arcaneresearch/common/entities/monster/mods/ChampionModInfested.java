package arcaneresearch.common.entities.monster.mods;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModSounds;
public class ChampionModInfested implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {
        if (!champion.level().isClientSide && champion.getHealth() - amount <= 0) {
            champion.playSound(ModSounds.GORE.get(), 0.5f, 1.0f);
            int count = 2 + champion.level().random.nextInt(3);
            for (int i = 0; i < count; i++) {
                Silverfish silverfish = EntityType.SILVERFISH.create(champion.level());
                if (silverfish != null) {
                    double offsetX = (champion.level().random.nextDouble() - 0.5) * champion.getBbWidth();
                    double offsetZ = (champion.level().random.nextDouble() - 0.5) * champion.getBbWidth();
                    silverfish.setPos(champion.getX() + offsetX, champion.getY(), champion.getZ() + offsetZ);
                    champion.level().addFreshEntity(silverfish);
                }
            }
        }
        return amount;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void showFX(LivingEntity champion) {
        if (champion.level().random.nextBoolean()) {
            FXDispatcher.INSTANCE.slimeJumpFX(champion, 0);
        }
    }
}
