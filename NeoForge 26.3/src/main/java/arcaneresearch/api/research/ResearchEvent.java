package arcaneresearch.api.research;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent; // NEOFORGE_PORT: was  (bus 8.x uses the interface)
import net.neoforged.bus.api.Event;
import arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;


public class ResearchEvent extends Event implements ICancellableEvent {

    private final Player player;

    public ResearchEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    
    public static class Knowledge extends ResearchEvent implements ICancellableEvent {
        private final EnumKnowledgeType type;
        private final ResearchCategory category;
        private final int amount;

        public Knowledge(Player player, EnumKnowledgeType type, ResearchCategory category, int amount) {
            super(player);
            this.type = type;
            this.category = category;
            this.amount = amount;
        }

        public EnumKnowledgeType getType() {
            return type;
        }

        public ResearchCategory getCategory() {
            return category;
        }

        public int getAmount() {
            return amount;
        }
    }

    
    public static class Research extends ResearchEvent implements ICancellableEvent {
        private final String researchKey;

        public Research(Player player, String researchKey) {
            super(player);
            this.researchKey = researchKey;
        }

        public String getResearchKey() {
            return researchKey;
        }
    }
}
