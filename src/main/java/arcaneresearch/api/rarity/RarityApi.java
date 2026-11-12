package arcaneresearch.api.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

public class RarityApi {
    public static final CustomRarity COMMON = new CustomRarity("common", ChatFormatting.WHITE);
    public static final CustomRarity UNCOMMON = new CustomRarity("uncommon", ChatFormatting.GREEN);
    public static final CustomRarity MAGIC = new CustomRarity("magic", ChatFormatting.BLUE);
    public static final CustomRarity LEGACY = new CustomRarity("legacy", ChatFormatting.GOLD);


    public static class CustomRarity {
        private String TAG;
        private ChatFormatting Color;

        public CustomRarity(String tag, ChatFormatting color) {
            this.TAG = tag;
            this.Color = color;
        }

        public Rarity getMainRarity() {
            return Rarity.create(getTAG() + "_item", this.Color);
        }

        public String getTAG() {
            return this.TAG;
        }
    }
}
