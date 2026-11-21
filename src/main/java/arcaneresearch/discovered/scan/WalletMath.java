package arcaneresearch.discovered.scan;

/** Pure amount arithmetic used by the hex scan wallet. */
public final class WalletMath {

    private WalletMath() {
    }

    /** Non-positive deposits are ignored. */
    public static int add(int current, int amount) {
        return amount <= 0 ? current : current + amount;
    }

    /** Never take more than is available, and never take on non-positive requests. */
    public static int take(int available, int amount) {
        return amount <= 0 ? 0 : Math.min(available, amount);
    }

    public static int remaining(int available, int taken) {
        return available - taken;
    }
}
