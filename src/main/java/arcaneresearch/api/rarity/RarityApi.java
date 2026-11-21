package arcaneresearch.api.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

public class RarityApi {
    public static final CustomRarity COMMON = new CustomRarity("common", ChatFormatting.WHITE);
    public static final CustomRarity UNCOMMON = new CustomRarity("uncommon", ChatFormatting.GREEN);
    public static final CustomRarity MAGIC = new CustomRarity("magic", ChatFormatting.BLUE);
    public static final CustomRarity LEGACY = new CustomRarity("legacy", ChatFormatting.GOLD);

    public static class CustomRarity {
        private final String TAG;
        private final ChatFormatting Color;

        public CustomRarity(String tag, ChatFormatting color) {
            this.TAG = tag;
            this.Color = color;
        }

        /** EN: 26.x has a fixed Rarity enum; map custom tags onto the closest vanilla entry. RU: В 26.x Rarity — фиксированный enum. */
        public Rarity getMainRarity() {
            return switch (TAG) {
                case "uncommon" -> Rarity.UNCOMMON;
                case "magic" -> Rarity.RARE;
                case "legacy" -> Rarity.EPIC;
                default -> Rarity.COMMON;
            };
        }

        public String getTAG() {
            return this.TAG;
        }

        public ChatFormatting getColor() {
            return this.Color;
        }
    }
}
