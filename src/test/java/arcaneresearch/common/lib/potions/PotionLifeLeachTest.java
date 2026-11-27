package arcaneresearch.common.lib.potions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Похищение жизни: лечение по уровню эффекта")
class PotionLifeLeachTest {

    @Test
    void oneHealthPointPerLevel() {
        assertEquals(1.0f, PotionLifeLeach.healFor(0));
        assertEquals(2.0f, PotionLifeLeach.healFor(1));
        assertEquals(5.0f, PotionLifeLeach.healFor(4));
    }

    @Test
    void healingIsNotCapped() {
        assertEquals(21.0f, PotionLifeLeach.healFor(20));
        assertEquals(101.0f, PotionLifeLeach.healFor(100));
    }

    @Test
    void negativeAmplifierTreatedAsFirstLevel() {
        assertEquals(1.0f, PotionLifeLeach.healFor(-1));
    }
}
