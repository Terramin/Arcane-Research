package arcaneresearch.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemTCBase extends Item {

    protected String BASE_NAME;
    protected String[] VARIANTS;
    protected int[] VARIANTS_META;
    private boolean hasSubtypes;

    public ItemTCBase(Properties properties, String name, String... variants) {
        super(properties);
        BASE_NAME = name;
        if (variants.length == 0) {
            VARIANTS = new String[]{name};
        } else {
            VARIANTS = variants;
        }
        VARIANTS_META = new int[VARIANTS.length];
        for (int m = 0; m < VARIANTS.length; m++) {
            VARIANTS_META[m] = m;
        }
        hasSubtypes = VARIANTS.length > 1;
    }

    public ItemTCBase(String name, String... variants) {
        this(new Properties(), name, variants);
    }

    public ItemTCBase(Properties properties) {
        super(properties);
        BASE_NAME = "item";
        VARIANTS = new String[]{"item"};
        VARIANTS_META = new int[]{0};
        hasSubtypes = false;
    }

    public String getUnlocalizedName(ItemStack stack) {
        
        if (hasSubtypes && stack.getDamageValue() < VARIANTS.length && !VARIANTS[stack.getDamageValue()].equals(BASE_NAME)) {
            return String.format(super.getDescriptionId() + ".%s", VARIANTS[stack.getDamageValue()]);
        }
        return super.getDescriptionId();
    }

    public String[] getVariantNames() {
        return VARIANTS;
    }

    public int[] getVariantMeta() {
        return VARIANTS_META;
    }
}
