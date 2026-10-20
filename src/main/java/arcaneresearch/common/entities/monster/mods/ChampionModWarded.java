package arcaneresearch.common.entities.monster.mods;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
public class ChampionModWarded implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            amount *= 0.25f;
        }
        return amount;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void showFX(LivingEntity champion) {
        if (champion.level().random.nextBoolean()) {
            return;
        }
        float w = champion.level().random.nextFloat() * champion.getBbWidth();
        float d = champion.level().random.nextFloat() * champion.getBbWidth();
        float h = champion.level().random.nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.5f + champion.level().random.nextFloat() * 0.1f,
                0.5f + champion.level().random.nextFloat() * 0.1f, 0.5f + champion.level().random.nextFloat() * 0.1f,
                0.6f, true, 69, 4, 1, 4 + champion.level().random.nextInt(4), 0,
                0.8f + champion.level().random.nextFloat() * 0.3f, 0.0f, 0);
    }
}
