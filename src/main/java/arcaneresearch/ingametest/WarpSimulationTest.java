package arcaneresearch.ingametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.utils.WarpRules;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModItems;

/** In-game checks for the warp rules that were aligned with 1.12.2. */
@GameTestHolder(Arcaneresearch.MODID)
public final class WarpSimulationTest {

    static {
        StructureUtils.testStructuresDir = "ingametest";
    }

    private WarpSimulationTest() {
    }

    /**
     * Checks the counter math and that warp effects cannot be cured by milk.
     *
     * @param helper game-test helper
     */
    @GameTest(template = "default_3x3x3", timeoutTicks = 20000)
    @PrefixGameTestTemplate(false)
    public static void warpRules(GameTestHelper helper) {
        if (WarpRules.nextCounter(50, 0) != 35 || WarpRules.nextCounter(20, 0) != 11
                || WarpRules.nextCounter(50, 10) != 45) {
            helper.fail("warp counter drain does not match 1.12.2");
            return;
        }

        MobEffectInstance hunger = WarpRules.applyWarpCuratives(
                new MobEffectInstance(ModEffects.UNNATURAL_HUNGER.get(), 100, 0), true);
        List<ItemStack> cures = hunger.getCurativeItems();
        boolean bread = cures.stream().anyMatch(stack -> stack.is(net.minecraft.world.item.Items.BREAD));
        boolean brain = cures.stream().anyMatch(stack -> stack.is(ModItems.ZOMBIE_BRAIN.get()));
        if (cures.size() != 2 || !bread || !brain) {
            helper.fail("Unnatural Hunger cures must be bread and zombie brain only: " + cures);
            return;
        }

        MobEffectInstance gaze = WarpRules.applyWarpCuratives(
                new MobEffectInstance(ModEffects.DEATH_GAZE.get(), 100, 0), false);
        if (!gaze.getCurativeItems().isEmpty()) {
            helper.fail("Death Gaze must have no curative items");
            return;
        }
        helper.succeed();
    }
}
