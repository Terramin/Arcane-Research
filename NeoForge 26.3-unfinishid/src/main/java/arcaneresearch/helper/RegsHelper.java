package arcaneresearch.helper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Единая точка обратных поисков в реестрах (порт из Forge 1.20.1:
 * {@code ForgeRegistries} -&gt; {@code BuiltInRegistries}).
 */
public final class RegsHelper {

    private RegsHelper() {
    }

    /** Ключ предмета ({@code null}, если не зарегистрирован). */
    public static Identifier itemKey(Item item) {
        try {
            return BuiltInRegistries.ITEM.getKey(item);
        } catch (Exception e) {
            return null;
        }
    }

    /** Ключ блока ({@code null}, если не зарегистрирован). */
    public static Identifier blockKey(Block block) {
        try {
            return BuiltInRegistries.BLOCK.getKey(block);
        } catch (Exception e) {
            return null;
        }
    }

    /** Ключ типа сущности ({@code null}, если не зарегистрирован). */
    public static Identifier entityKey(EntityType<?> type) {
        try {
            return BuiltInRegistries.ENTITY_TYPE.getKey(type);
        } catch (Exception e) {
            return null;
        }
    }
}
