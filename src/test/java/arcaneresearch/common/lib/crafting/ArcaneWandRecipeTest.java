package arcaneresearch.common.lib.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Жезлы: стоимость по правилам Thaumcraft 4")
class ArcaneWandRecipeTest {

    @Test
    @DisplayName("стоимость Aspects равна произведению цены наконечника и стержня")
    void crystalCostIsCapTimesRod() {
        assertEquals(9, ArcaneWandRecipe.wandCrystalCost(3, 3));
        assertEquals(54, ArcaneWandRecipe.wandCrystalCost(6, 9));
        assertEquals(64, ArcaneWandRecipe.wandCrystalCost(32, 32));
    }

    @Test
    @DisplayName("скипетр стоит в 1.5 раза дороже обычного жезла")
    void sceptreCostIsOneAndAHalfTimesTheProduct() {
        assertEquals(1, ArcaneWandRecipe.sceptreCrystalCost(1, 1));
        assertEquals(13, ArcaneWandRecipe.sceptreCrystalCost(3, 3));
        assertEquals(64, ArcaneWandRecipe.sceptreCrystalCost(6, 9));
        assertEquals(64, ArcaneWandRecipe.sceptreCrystalCost(32, 32));
    }
}
