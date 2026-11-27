package arcaneresearch.ingametest;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.potions.PotionLifeLeach;
import arcaneresearch.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** In-game checks for the Life Leech potion effect. */
@GameTestHolder(Arcaneresearch.MODID)
public final class LifestealSimulationTest {

    static {
        StructureUtils.testStructuresDir = "ingametest";
    }

    private LifestealSimulationTest() {
    }

    /**
     * Verifies that hurting a mob while affected actually heals the player, with the
     * amount coming from the effect level rather than the 5 HP enchantment cap.
     */
    @GameTest(template = "default_3x3x3", timeoutTicks = 200)
    @PrefixGameTestTemplate(false)
    public static void lifeLeechHealsOnAttack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer();
        player.setHealth(player.getMaxHealth() - 10.0f);
        player.addEffect(new MobEffectInstance(ModEffects.LIFE_LEACH.get(), 200, 2));

        float healthBefore = player.getHealth();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
        zombie.hurt(level.damageSources().playerAttack(player), 5.0f);

        float expected = healthBefore + PotionLifeLeach.healFor(2);
        if (Math.abs(player.getHealth() - expected) > 0.01f) {
            helper.fail("Life Leech: expected " + expected + " HP, got " + player.getHealth());
            return;
        }
        helper.succeed();
    }
}
