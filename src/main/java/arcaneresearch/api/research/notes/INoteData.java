package arcaneresearch.api.research.notes;

import net.minecraft.network.chat.Component;

/**
 * Read-only contract of a discovered research note's data.
 *
 * <p>Implementations are expected to be immutable snapshots read from an
 * {@link net.minecraft.world.item.ItemStack}; use {@link NoteData#read} to obtain one.</p>
 */
public interface INoteData {

    String getKey();

    arcaneresearch.api.aspects.Aspect[] getTags();

    int[] getNeed();

    int[] getHave();

    java.util.Map<String, Byte> getFailed();

    boolean isStage1();

    boolean isStage2();

    boolean isRevealed();

    float getTotalProgress();

    boolean isComplete();

    boolean requiresHex();

    float getTagProgress(arcaneresearch.api.aspects.Aspect tag);

    byte getFailed(String aspectTag);

    void addFailed(String aspectTag, byte amount);
}
