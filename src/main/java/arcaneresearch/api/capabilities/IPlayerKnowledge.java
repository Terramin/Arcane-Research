package arcaneresearch.api.capabilities;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;
public interface IPlayerKnowledge extends INBTSerializable<CompoundTag> {
    void clear();
    EnumResearchStatus getResearchStatus(@Nonnull String research);
    boolean isResearchComplete(String research);
    boolean isResearchKnown(String research);
    enum EnumResearchStatus {
        UNKNOWN, COMPLETE, IN_PROGRESS
    }
    int getResearchStage(@Nonnull String research);
    boolean addResearch(@Nonnull String research);
    boolean setResearchStage(@Nonnull String research, int stage);
    boolean removeResearch(@Nonnull String research);
    @Nonnull
    Set<String> getResearchList();
    boolean setResearchFlag(@Nonnull String research, @Nonnull EnumResearchFlag flag);
    boolean clearResearchFlag(@Nonnull String research, @Nonnull EnumResearchFlag flag);
    boolean hasResearchFlag(@Nonnull String research, @Nonnull EnumResearchFlag flag);
    boolean addPresentedDiscovery(@Nonnull String key);
    boolean hasPresentedDiscovery(@Nonnull String key);
    boolean consumePresentedDiscovery(@Nonnull String key);
    void clearPresentedDiscoveries();
    boolean addKnowledge(@Nonnull EnumKnowledgeType type, String category, int amount);
    default boolean addKnowledge(@Nonnull EnumKnowledgeType type, arcaneresearch.api.research.ResearchCategory category, int amount) {
        return addKnowledge(type, category != null ? category.key : null, amount);
    }
    int getKnowledge(@Nonnull EnumKnowledgeType type, String category);
    default int getKnowledge(@Nonnull EnumKnowledgeType type, arcaneresearch.api.research.ResearchCategory category) {
        return getKnowledge(type, category != null ? category.key : null);
    }
    int getKnowledgeRaw(@Nonnull EnumKnowledgeType type, String category);
    default int getKnowledgeRaw(@Nonnull EnumKnowledgeType type, arcaneresearch.api.research.ResearchCategory category) {
        return getKnowledgeRaw(type, category != null ? category.key : null);
    }
    void sync(ServerPlayer player);
    enum EnumKnowledgeType {
        THEORY(32, true, "T"),
        OBSERVATION(16, true, "O");
        private final short progression;
        private final boolean hasFields;
        private final String abbr;
        EnumKnowledgeType(int progression, boolean hasFields, String abbr) {
            this.progression = (short) progression;
            this.hasFields = hasFields;
            this.abbr = abbr;
        }
        public int getProgression() {
            return progression;
        }
        public boolean hasFields() {
            return hasFields;
        }
        public String getAbbreviation() {
            return abbr;
        }
    }
    enum EnumResearchFlag {
        PAGE,
        RESEARCH,
        POPUP,
        DISCOVERY
    }
}
