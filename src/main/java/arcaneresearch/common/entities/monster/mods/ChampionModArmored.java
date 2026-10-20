package arcaneresearch.common.entities.monster.mods;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
public class ChampionModArmored implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {
        if (!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            float reduced = amount * 19.0f;
            amount = reduced / 25.0f;
        }
        return amount;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void showFX(LivingEntity champion) {
        if (champion.level().random.nextInt(4) != 0) {
            return;
        }
        float w = champion.level().random.nextFloat() * champion.getBbWidth();
        float d = champion.level().random.nextFloat() * champion.getBbWidth();
        float h = champion.level().random.nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.9f, 0.9f, 0.9f + champion.level().random.nextFloat() * 0.1f,
                0.7f, false, 448, 9, 1, 5 + champion.level().random.nextInt(4), 0,
                0.6f + champion.level().random.nextFloat() * 0.2f, 0.0f, 0);
    }
}
