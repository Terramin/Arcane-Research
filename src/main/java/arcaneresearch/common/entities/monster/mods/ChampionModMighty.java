package arcaneresearch.common.entities.monster.mods;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
public class ChampionModMighty implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {
        return amount * 1.5f;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void showFX(LivingEntity champion) {
        if (champion.level().random.nextFloat() > 0.3f) {
            return;
        }
        float w = champion.level().random.nextFloat() * champion.getBbWidth();
        float d = champion.level().random.nextFloat() * champion.getBbWidth();
        float h = champion.level().random.nextFloat() * champion.getBbHeight();
        int p = 704 + champion.level().random.nextInt(4) * 3;
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.8f + champion.level().random.nextFloat() * 0.2f,
                0.8f + champion.level().random.nextFloat() * 0.2f, 0.8f + champion.level().random.nextFloat() * 0.2f,
                0.7f, false, p, 3, 1, 4 + champion.level().random.nextInt(3), 0,
                1.0f + champion.level().random.nextFloat() * 0.3f, 0.0f, 0);
    }
}
