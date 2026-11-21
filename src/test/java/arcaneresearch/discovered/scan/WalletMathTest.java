package arcaneresearch.discovered.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Сканирование: арифметика кошелька аспектов")
class WalletMathTest {

    @Test
    void depositsIgnoreNonPositiveAmounts() {
        assertEquals(5, WalletMath.add(5, 0));
        assertEquals(5, WalletMath.add(5, -3));
        assertEquals(9, WalletMath.add(5, 4));
    }

    @Test
    void withdrawalsAreClampedToTheAvailableAmount() {
        assertEquals(0, WalletMath.take(10, 0));
        assertEquals(0, WalletMath.take(10, -2));
        assertEquals(4, WalletMath.take(10, 4));
        assertEquals(10, WalletMath.take(10, 25));
        assertEquals(0, WalletMath.take(0, 5));
    }

    @Test
    void remainingNeverGoesNegative() {
        assertEquals(6, WalletMath.remaining(10, 4));
        assertEquals(0, WalletMath.remaining(10, 10));
    }
}
