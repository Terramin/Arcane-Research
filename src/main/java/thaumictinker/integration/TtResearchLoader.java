package thaumictinker.integration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;

import thaumictinker.TtCore;

 








 
public final class TtResearchLoader {

    private static final String RESEARCH_PATH = "/assets/thaumictinker/research/tt_research.json";

    private TtResearchLoader() {
    }

    public static void load() {
        JsonObject root;
        try (InputStream stream = TtResearchLoader.class.getResourceAsStream(RESEARCH_PATH)) {
            if (stream == null) {
                TtCore.LOGGER.warn("Thaumic Tinkerer research file not found: {}", RESEARCH_PATH);
                return;
            }
            root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception exception) {
            TtCore.LOGGER.warn("Failed to read Thaumic Tinkerer research file", exception);
            return;
        }

        JsonArray entries = root.getAsJsonArray("entries");
        if (entries == null) {
            TtCore.LOGGER.warn("Thaumic Tinkerer research file has no 'entries' array");
            return;
        }

        int loaded = 0;
        for (JsonElement element : entries) {
            try {
                if (addEntry(element.getAsJsonObject())) {
                    loaded++;
                }
            } catch (Exception exception) {
                TtCore.LOGGER.warn("Invalid Thaumic Tinkerer research entry: {}", exception.getMessage());
            }
        }
        TtCore.LOGGER.info("Loaded {} Thaumic Tinkerer research entries", loaded);
    }

    private static boolean addEntry(JsonObject json) {
        ResearchEntry entry = new ResearchEntry();
        entry.setKey(json.get("key").getAsString());
        entry.setName(json.get("name").getAsString());
        entry.setCategory(json.get("category").getAsString());

        if (json.has("icons")) {
            entry.setIcons(readIcons(json.getAsJsonArray("icons")));
        }
        if (json.has("location")) {
            JsonArray location = json.getAsJsonArray("location");
            entry.setDisplayColumn(location.get(0).getAsInt());
            entry.setDisplayRow(location.get(1).getAsInt());
        }
        if (json.has("parents")) {
            entry.setParents(readStrings(json.getAsJsonArray("parents")));
        }
        if (json.has("siblings")) {
            entry.setSiblings(readStrings(json.getAsJsonArray("siblings")));
        }
        if (json.has("meta")) {
            entry.setMeta(readMeta(json.getAsJsonArray("meta")));
        }
        if (json.has("stages")) {
            entry.setStages(readStages(json.getAsJsonArray("stages")));
        }

        ResearchCategory category = ResearchCategories.getResearchCategory(entry.getCategory());
        if (category == null) {
            TtCore.LOGGER.warn("Research category {} is not registered for entry {}",
                    entry.getCategory(), entry.getKey());
            return false;
        }
        if (category.research.containsKey(entry.getKey())) {
            return false;
        }

        category.research.put(entry.getKey(), entry);
        category.minDisplayColumn = Math.min(category.minDisplayColumn, entry.getDisplayColumn());
        category.minDisplayRow = Math.min(category.minDisplayRow, entry.getDisplayRow());
        category.maxDisplayColumn = Math.max(category.maxDisplayColumn, entry.getDisplayColumn());
        category.maxDisplayRow = Math.max(category.maxDisplayRow, entry.getDisplayRow());
        return true;
    }

    private static Object[] readIcons(JsonArray array) {
        Object[] icons = new Object[array.size()];
        for (int index = 0; index < array.size(); index++) {
            String value = array.get(index).getAsString();
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id != null) {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) {
                    icons[index] = new ItemStack(item);
                    continue;
                }
            }
            icons[index] = id != null ? id : new ResourceLocation("minecraft", "missingno");
        }
        return icons;
    }

    private static ResearchStage[] readStages(JsonArray array) {
        List<ResearchStage> stages = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            ResearchStage stage = new ResearchStage();
            stage.setText(json.get("text").getAsString());

            if (json.has("recipes")) {
                JsonArray recipes = json.getAsJsonArray("recipes");
                ResourceLocation[] ids = new ResourceLocation[recipes.size()];
                for (int index = 0; index < recipes.size(); index++) {
                    ids[index] = new ResourceLocation(recipes.get(index).getAsString());
                }
                stage.setRecipes(ids);
            }
            if (json.has("required_research")) {
                stage.setResearch(readStrings(json.getAsJsonArray("required_research")));
            }
            if (json.has("required_knowledge")) {
                JsonArray knowledge = json.getAsJsonArray("required_knowledge");
                List<ResearchStage.Knowledge> parsed = new ArrayList<>();
                for (JsonElement value : knowledge) {
                    ResearchStage.Knowledge knowledgeEntry = ResearchStage.Knowledge.parse(value.getAsString());
                    if (knowledgeEntry != null) {
                        parsed.add(knowledgeEntry);
                    }
                }
                stage.setKnow(parsed.toArray(ResearchStage.Knowledge[]::new));
            }
            if (json.has("warp")) {
                stage.setWarp(json.get("warp").getAsInt());
            }
            stages.add(stage);
        }
        return stages.toArray(ResearchStage[]::new);
    }

    private static String[] readStrings(JsonArray array) {
        String[] values = new String[array.size()];
        for (int index = 0; index < array.size(); index++) {
            values[index] = array.get(index).getAsString();
        }
        return values;
    }

    private static ResearchEntry.EnumResearchMeta[] readMeta(JsonArray array) {
        List<ResearchEntry.EnumResearchMeta> meta = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            try {
                meta.add(ResearchEntry.EnumResearchMeta.valueOf(element.getAsString()));
            } catch (IllegalArgumentException exception) {
                TtCore.LOGGER.warn("Unknown research meta flag: {}", element.getAsString());
            }
        }
        return meta.toArray(ResearchEntry.EnumResearchMeta[]::new);
    }
}
