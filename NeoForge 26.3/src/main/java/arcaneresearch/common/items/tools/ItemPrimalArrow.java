package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.aspects.Aspect;

/**
 * Первородная стрела, порт TC4 {@code ItemPrimalArrow}: 6 видов
 * (в 1.7.10 — метадата 0-5, здесь 6 отдельных предметов).
 */
public class ItemPrimalArrow extends Item {

    public static final int AIR = 0;
    public static final int FIRE = 1;
    public static final int WATER = 2;
    public static final int EARTH = 3;
    public static final int ORDER = 4;
    public static final int ENTROPY = 5;

    private final int type;

    public ItemPrimalArrow(Item.Properties props, int type) {
        // NEOFORGE_PORT: registerItem passes props (same plain properties as Forge).
        super(props);
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public static Aspect aspectFor(int type) {
        return switch (type) {
            case FIRE -> Aspect.FIRE;
            case WATER -> Aspect.WATER;
            case EARTH -> Aspect.EARTH;
            case ORDER -> Aspect.ORDER;
            case ENTROPY -> Aspect.ENTROPY;
            default -> Aspect.AIR;
        };
    }
}
