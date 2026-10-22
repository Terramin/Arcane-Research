package thaumictinker.kami.trinkets;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

 




 
public class ProtoclayItem extends net.minecraft.world.item.Item {

    public static final String PICK = "pick";
    public static final String SHOVEL = "shovel";
    public static final String AXE = "axe";
    public static final String SWORD = "sword";

    public ProtoclayItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static String advancedToolType(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"thaumictinker".equals(key.getNamespace())) {
            return null;
        }
        return switch (key.getPath()) {
            case "awakened_ichorium_pickaxe" -> PICK;
            case "awakened_ichorium_shovel" -> SHOVEL;
            case "awakened_ichorium_axe" -> AXE;
            case "awakened_ichorium_sword" -> SWORD;
            default -> null;
        };
    }

    public static String typeFor(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return PICK;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return SHOVEL;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)) {
            return AXE;
        }
        return "";
    }

    public static boolean carriesProtoclay(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < BlockStorage.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() instanceof ProtoclayItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean swapTool(Player player, BlockState state) {
        return swapTool(player, state, ProtoclayItem::advancedToolType);
    }

    public static boolean swapTool(Player player, BlockState state,
                                   java.util.function.Function<ItemStack, String> toolTypes) {
        if (!carriesProtoclay(player)) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int selected = inventory.selected;
        ItemStack current = inventory.getItem(selected);
        String currentType = toolTypes.apply(current);
        if (currentType == null || currentType.equals(SWORD)) {
            return false;
        }
        String typeToFind = typeFor(state);
        if (typeToFind.isEmpty() || currentType.equals(typeToFind)) {
            return false;
        }
        for (int slot = 0; slot < BlockStorage.INVENTORY_SIZE; slot++) {
            if (slot == selected) {
                continue;
            }
            ItemStack candidate = inventory.getItem(slot);
            if (typeToFind.equals(toolTypes.apply(candidate))) {
                inventory.setItem(selected, candidate);
                inventory.setItem(slot, current);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }
}
