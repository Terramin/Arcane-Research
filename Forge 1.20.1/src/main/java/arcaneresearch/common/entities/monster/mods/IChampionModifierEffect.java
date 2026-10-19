package arcaneresearch.common.entities.monster.mods;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
public interface IChampionModifierEffect {
    float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount);
    @OnlyIn(Dist.CLIENT)
    void showFX(LivingEntity champion);
    @OnlyIn(Dist.CLIENT)
    default void preRender(LivingEntity champion) {
    }
}
