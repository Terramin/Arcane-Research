package arcaneresearch.helper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
public final class RegsHelper {
    private RegsHelper() {
    }
    public static ResourceLocation itemKey(Item item) {
        try {
            return ForgeRegistries.ITEMS.getKey(item);
        } catch (Exception e) {
            return null;
        }
    }
    public static ResourceLocation blockKey(Block block) {
        try {
            return ForgeRegistries.BLOCKS.getKey(block);
        } catch (Exception e) {
            return null;
        }
    }
    public static ResourceLocation entityKey(EntityType<?> type) {
        try {
            return ForgeRegistries.ENTITY_TYPES.getKey(type);
        } catch (Exception e) {
            return null;
        }
    }
}
