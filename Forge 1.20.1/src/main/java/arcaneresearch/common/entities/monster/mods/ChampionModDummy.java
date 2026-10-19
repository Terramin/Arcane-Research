package arcaneresearch.common.entities.monster.mods;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
public class ChampionModDummy implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {
        return amount;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void showFX(LivingEntity champion) {
    }
}
