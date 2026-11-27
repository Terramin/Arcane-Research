package arcaneresearch.common.lib.potions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EN: Unit checks for Life Leech healing scaling without the enchantment cap.
 * RU: Юнит-проверки шкалы лечения «Похищения жизни» без лимита зачарований.
 */
@DisplayName("Похищение жизни: лечение по уровню эффекта")
class PotionLifeLeachTest {

    /**
     * EN: Each effect level adds one health point.
     * RU: Каждый уровень эффекта добавляет одно очко здоровья.
     */
    @Test
    void oneHealthPointPerLevel() {
        assertEquals(1.0f, PotionLifeLeach.healFor(0));
        assertEquals(2.0f, PotionLifeLeach.healFor(1));
        assertEquals(5.0f, PotionLifeLeach.healFor(4));
    }

    /**
     * EN: High amplifiers are not truncated to the enchantment's 5 HP limit.
     * RU: Высокие уровни не обрезаются лимитом зачарований в 5 HP.
     */
    @Test
    void healingIsNotCapped() {
        assertEquals(21.0f, PotionLifeLeach.healFor(20));
        assertEquals(101.0f, PotionLifeLeach.healFor(100));
    }

    /**
     * EN: A negative amplifier is treated as the first effect level.
     * RU: Отрицательный уровень считается первым уровнем эффекта.
     */
    @Test
    void negativeAmplifierTreatedAsFirstLevel() {
        assertEquals(1.0f, PotionLifeLeach.healFor(-1));
    }
}
