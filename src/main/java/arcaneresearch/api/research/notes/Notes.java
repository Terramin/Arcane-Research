package arcaneresearch.api.research.notes;

import net.minecraft.network.chat.Component;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;

public final class Notes {
    private Notes() {
    }

    public static Component researchName(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null) {
            return entry.getLocalizedName();
        }
        return Component.translatable("tempnote." + key + ".name");
    }

    public static Component researchPopup(String key) {
        if (key == null) {
            return null;
        }
        String langKey = "tempnote." + key + ".popup";
        String resolved = Component.translatable(langKey).getString();
        if (resolved.isEmpty() || resolved.equals(langKey)) {
            return null;
        }
        return Component.translatable(langKey);
    }

    public static boolean requiresHex(String key) {
        return NoteData.requiresHex(key);
    }

    public static Component researchText(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null && entry.getStages() != null) {
            java.util.List<Component> parts = new java.util.ArrayList<>();
            for (arcaneresearch.api.research.ResearchStage stage : entry.getStages()) {
                if (stage != null && stage.getText() != null && !stage.getText().isEmpty()) {
                    parts.add(Component.translatable(stage.getText()));
                }
            }
            if (!parts.isEmpty()) {
                net.minecraft.network.chat.MutableComponent out = Component.empty();
                for (int i = 0; i < parts.size(); i++) {
                    if (i > 0) {
                        out.append(" ");
                    }
                    out.append(parts.get(i));
                }
                return out;
            }
        }
        return Component.translatable("tempnote." + key + ".text");
    }
}
