package arcaneresearch.common.lib.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.api.research.*;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketKnowledgeGain;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResearchManager {

    private static final Set<String> warnedItems = new HashSet<>();
    private static final Map<String, String> LEGACY_ITEM_MAPPINGS = new HashMap<>();
    public static ConcurrentHashMap<String, Boolean> syncList = new ConcurrentHashMap<>();
    public static boolean noFlags = false;
    public static LinkedHashSet<Integer> craftingReferences = new LinkedHashSet<>();

    static {

        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:ingot", "arcaneresearch:thaumium_ingot");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:plate", "arcaneresearch:plate_brass");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:nugget", "arcaneresearch:brass_nugget");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:metal", "arcaneresearch:thaumium_ingot");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:mind", "arcaneresearch:brain_clockwork");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:turret", "arcaneresearch:turret_placer_basic");

        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_aer", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_aqua", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_ignis", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_terra", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_ordo", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crystal_perditio", "arcaneresearch:crystal_essence");

        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:vis_crystal", "arcaneresearch:crystal_essence");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:amulet_vis", "arcaneresearch:amulet_vis_crafted");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:seal", "arcaneresearch:blank_seal");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:focus_basic", "arcaneresearch:focus_blank");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:focus_greater", "arcaneresearch:focus_advanced");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:biothaumic_mind", "arcaneresearch:brain_biothaumic");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:clockwork_mind", "arcaneresearch:brain_clockwork");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:crimson_rites", "arcaneresearch:thaumonomicon");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:module", "arcaneresearch:golem_module_vision");

        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:sapling_greatwood", "arcaneresearch:greatwood_sapling");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:sapling_silverwood", "arcaneresearch:silverwood_sapling");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:log_greatwood", "arcaneresearch:greatwood_log");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:log_silverwood", "arcaneresearch:silverwood_log");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:tube", "arcaneresearch:tube_normal");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:smelter_basic", "arcaneresearch:smelter");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:arcane_lamp", "arcaneresearch:lamp_arcane");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:mirror", "arcaneresearch:mirror_item");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:paving_barrier", "arcaneresearch:paving_stone_barrier");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:paving_travel", "arcaneresearch:paving_stone_travel");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:stone_ancient", "arcaneresearch:ancient_stone");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:stone_eldritch_tile", "arcaneresearch:eldritch_stone_tile");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:arcane_bore", "arcaneresearch:turret_placer_bore");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:turret_crossbow", "arcaneresearch:turret_placer_basic");
        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:turret_crossbow_advanced", "arcaneresearch:turret_placer_advanced");

        LEGACY_ITEM_MAPPINGS.put("arcaneresearch:enchanted_placeholder", "minecraft:enchanted_book");

        LEGACY_ITEM_MAPPINGS.put("minecraft:dye", "minecraft:white_dye");
        LEGACY_ITEM_MAPPINGS.put("minecraft:hardened_clay", "minecraft:terracotta");
    }

    public static boolean addKnowledge(Player player, IPlayerKnowledge.EnumKnowledgeType type,
                                       ResearchCategory category, int amount) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return false;

        String catKey = category != null ? category.key : null;
        if (!type.hasFields()) {
            catKey = null;
        }

        // NEOFORGE_PORT: bus post(T):T (was boolean); cancellable check via isCanceled().
        if (NeoForge.EVENT_BUS.post(new ResearchEvent.Knowledge(player, type, category, amount)).isCanceled()) {
            return false;
        }

        int before = knowledge.getKnowledge(type, catKey);
        knowledge.addKnowledge(type, catKey, amount);
        int gained = knowledge.getKnowledge(type, catKey) - before;

        if (amount > 0 && player instanceof ServerPlayer serverPlayer) {
            for (int a = 0; a < gained; a++) {
                PacketHandler.sendToPlayer(new PacketKnowledgeGain(type, catKey), serverPlayer);
            }
        }

        syncList.put(player.getName().getString(), true);
        return true;
    }

    public static boolean completeResearch(Player player, String researchKey) {
        return completeResearch(player, researchKey, true);
    }

    public static boolean completeResearch(Player player, String researchKey, boolean sync) {
        boolean success = false;
        while (progressResearch(player, researchKey, sync)) {
            success = true;
        }
        return success;
    }

    public static boolean startResearchWithPopup(Player player, String researchKey) {
        boolean success = progressResearch(player, researchKey, true);
        if (success) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                knowledge.setResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.POPUP);
                knowledge.setResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
            }
        }
        return success;
    }

    public static boolean progressResearch(Player player, String researchKey) {
        return progressResearch(player, researchKey, true);
    }

    public static boolean progressResearch(Player player, String researchKey, boolean sync) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return false;

        if (knowledge.isResearchComplete(researchKey) || !doesPlayerHaveRequisites(player, researchKey)) {
            return false;
        }

        if (NeoForge.EVENT_BUS.post(new ResearchEvent.Research(player, researchKey)).isCanceled()) {
            return false;
        }

        if (!knowledge.isResearchKnown(researchKey)) {
            knowledge.addResearch(researchKey);
        }

        ResearchEntry entry = ResearchCategories.getResearch(researchKey);
        if (entry == null) {

            if (sync) {
                syncList.put(player.getName().getString(), true);
                player.giveExperienceLevels(5);
            }
            return true;
        }
        if (entry != null) {
            boolean showPopups = true;

            if (entry.getStages() != null && entry.getStages().length > 0) {
                int currentStage = knowledge.getResearchStage(researchKey);
                ResearchStage stage = null;

                if (currentStage > 0) {
                    currentStage = Math.min(currentStage, entry.getStages().length);
                    stage = entry.getStages()[currentStage - 1];
                }

                if (entry.getStages().length == 1 && currentStage == 0 && isStageAutoComplete(entry.getStages()[0])) {
                    currentStage++;
                } else if (entry.getStages().length > 1 && entry.getStages().length <= currentStage + 1
                        && currentStage < entry.getStages().length && isStageAutoComplete(entry.getStages()[currentStage])) {
                    currentStage++;
                }

                knowledge.setResearchStage(researchKey, Math.min(entry.getStages().length + 1, currentStage + 1));
                showPopups = (currentStage >= entry.getStages().length);

                int warp = 0;
                if (stage != null) {
                    warp = stage.getWarp();
                }
                if (showPopups && currentStage > 0) {
                    currentStage = Math.min(currentStage, entry.getStages().length);
                    stage = entry.getStages()[currentStage - 1];
                }
                if (stage != null) {
                    warp += stage.getWarp();
                    if (warp > 0 && !ModConfig.wussMode && !player.level().isClientSide()) {

                        if (warp > 1) {
                            int w2 = warp / 2;
                            if (warp - w2 > 0) {
                                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp - w2, IPlayerWarp.EnumWarpType.PERMANENT);
                            }
                            if (w2 > 0) {
                                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, w2, IPlayerWarp.EnumWarpType.NORMAL);
                            }
                        } else {
                            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, warp, IPlayerWarp.EnumWarpType.PERMANENT);
                        }
                    }
                }
            }

            if (showPopups) {
                if (sync) {
                    knowledge.setResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.POPUP);
                    if (!noFlags) {
                        knowledge.setResearchFlag(researchKey, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
                    } else {
                        noFlags = false;
                    }

                    if (entry.getRewardItem() != null) {
                        for (ItemStack reward : entry.getRewardItem()) {
                            if (!player.getInventory().add(reward.copy())) {
                                // NEOFORGE_PORT: spawnAtLocation(ItemStack, float) removed ->
                                // spawnAtLocation(ServerLevel, ItemStack, float).
                                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                    player.spawnAtLocation(serverLevel, reward.copy(), 1.0f);
                                }
                            }
                        }
                    }

                    if (entry.getRewardKnow() != null) {
                        for (ResearchStage.Knowledge rk : entry.getRewardKnow()) {
                            addKnowledge(player, rk.type, rk.category, rk.type.getProgression() * rk.amount);
                        }
                    }
                }

                checkAddendumUnlocks(player, knowledge, researchKey);
            }
        }

        if (entry != null && entry.getSiblings() != null) {
            for (String sibling : entry.getSiblings()) {
                if (!knowledge.isResearchComplete(sibling) && doesPlayerHaveRequisites(player, sibling)) {
                    completeResearch(player, sibling, sync);
                }
            }
        }

        if (sync) {
            syncList.put(player.getName().getString(), true);
            if (entry != null) {
                player.giveExperienceLevels(5);
            }
        }

        return true;
    }

    private static boolean isStageAutoComplete(ResearchStage stage) {
        return stage.getCraft() == null && stage.getObtain() == null
                && stage.getKnow() == null && stage.getResearch() == null
                && stage.getDiscovery() == null;
    }

    private static void checkAddendumUnlocks(Player player, IPlayerKnowledge knowledge, String completedResearch) {
        for (String catKey : ResearchCategories.researchCategories.keySet()) {
            ResearchCategory category = ResearchCategories.getResearchCategory(catKey);
            if (category == null) continue;

            for (ResearchEntry entry : category.research.values()) {
                if (entry == null || entry.getAddenda() == null || !knowledge.isResearchComplete(entry.getKey())) {
                    continue;
                }

                for (ResearchAddendum addendum : entry.getAddenda()) {
                    if (addendum.getResearch() != null && Arrays.asList(addendum.getResearch()).contains(completedResearch)) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.addaddendum", entry.getLocalizedName()));
                        knowledge.setResearchFlag(entry.getKey(), IPlayerKnowledge.EnumResearchFlag.PAGE);
                        break;
                    }
                }
            }
        }
    }

    public static boolean doesPlayerHaveRequisites(Player player, String key) {
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry == null) return true;

        String[] parents = entry.getParentsStripped();
        return parents == null || ArcaneresearchCapabilities.knowsResearchStrict(player, parents);
    }

    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        for (Aspect aspect : Aspect.aspects.values()) {
            if (aspect.getComponents() != null) {
                Aspect[] components = aspect.getComponents();
                if ((components[0] == aspect1 && components[1] == aspect2) ||
                        (components[0] == aspect2 && components[1] == aspect1)) {
                    return aspect;
                }
            }
        }
        return null;
    }

    public static void parseAllResearch() {
        int totalEntries = 0;

        for (Identifier loc : CommonInternals.jsonLocs.values()) {
            String path = "/assets/" + loc.getNamespace() + "/" + loc.getPath();
            if (!path.endsWith(".json")) {
                path += ".json";
            }

            try (InputStream stream = ResearchManager.class.getResourceAsStream(path)) {
                if (stream != null) {
                    InputStreamReader reader = new InputStreamReader(stream);
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray entries = obj.getAsJsonArray("entries");

                    int count = 0;
                    for (JsonElement element : entries) {
                        try {
                            JsonObject entryObj = element.getAsJsonObject();
                            ResearchEntry entry = parseResearchJson(entryObj);
                            addResearchToCategory(entry);
                            count++;
                        } catch (Exception e) {
                            Arcaneresearch.LOGGER.warn("Invalid research entry in {}: {}", loc, e.getMessage());
                        }
                    }

                    Arcaneresearch.LOGGER.info("Loaded {} research entries from {}", count, loc);
                    totalEntries += count;
                } else {
                    Arcaneresearch.LOGGER.warn("Research file not found: {}", path);
                }
            } catch (Exception e) {
                Arcaneresearch.LOGGER.warn("Failed to parse research file {}: {}", loc, e.getMessage());
            }
        }

        Arcaneresearch.LOGGER.info("Total research entries loaded: {}", totalEntries);
    }

    private static ResearchEntry parseResearchJson(JsonObject obj) throws Exception {
        ResearchEntry entry = new ResearchEntry();

        entry.setKey(obj.get("key").getAsString());
        entry.setName(obj.get("name").getAsString());
        entry.setCategory(obj.get("category").getAsString());

        if (obj.has("icons")) {
            String[] iconStrings = arrayJsonToString(obj.getAsJsonArray("icons"));
            if (iconStrings != null && iconStrings.length > 0) {
                Object[] icons = new Object[iconStrings.length];
                for (int i = 0; i < iconStrings.length; i++) {
                    ItemStack stack = parseJSONtoItemStack(iconStrings[i]);
                    if (!stack.isEmpty()) {
                        icons[i] = stack;
                    } else if (iconStrings[i].startsWith("focus")) {
                        icons[i] = iconStrings[i];
                    } else {

                        icons[i] = Identifier.parse(
                                iconStrings[i].replace("textures/items/", "textures/item/"));
                    }
                }
                entry.setIcons(icons);
            }
        }

        if (obj.has("location")) {
            Integer[] loc = arrayJsonToInt(obj.getAsJsonArray("location"));
            if (loc != null && loc.length == 2) {
                entry.setDisplayColumn(loc[0]);
                entry.setDisplayRow(loc[1]);
            }
        }

        if (obj.has("parents")) {
            entry.setParents(arrayJsonToString(obj.getAsJsonArray("parents")));
        }
        if (obj.has("siblings")) {
            entry.setSiblings(arrayJsonToString(obj.getAsJsonArray("siblings")));
        }

        if (obj.has("meta")) {
            String[] metaStrings = arrayJsonToString(obj.getAsJsonArray("meta"));
            if (metaStrings != null) {
                List<ResearchEntry.EnumResearchMeta> metas = new ArrayList<>();
                for (String s : metaStrings) {
                    try {
                        metas.add(ResearchEntry.EnumResearchMeta.valueOf(s.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        Arcaneresearch.LOGGER.warn("Unknown research meta: {}", s);
                    }
                }
                entry.setMeta(metas.toArray(new ResearchEntry.EnumResearchMeta[0]));
            }
        }

        if (obj.has("reward_item")) {
            entry.setRewardItem(parseJsonItemList(arrayJsonToString(obj.getAsJsonArray("reward_item"))));
        }
        if (obj.has("reward_knowledge")) {
            String[] knowledgeStrings = arrayJsonToString(obj.getAsJsonArray("reward_knowledge"));
            if (knowledgeStrings != null) {
                List<ResearchStage.Knowledge> knowledge = new ArrayList<>();
                for (String s : knowledgeStrings) {
                    ResearchStage.Knowledge k = ResearchStage.Knowledge.parse(s);
                    if (k != null) knowledge.add(k);
                }
                if (!knowledge.isEmpty()) {
                    entry.setRewardKnow(knowledge.toArray(new ResearchStage.Knowledge[0]));
                }
            }
        }

        if (obj.has("stages")) {
            JsonArray stagesJson = obj.getAsJsonArray("stages");
            List<ResearchStage> stages = new ArrayList<>();
            for (JsonElement stageElement : stagesJson) {
                stages.add(parseStageJson(stageElement.getAsJsonObject(), entry.getKey()));
            }
            if (!stages.isEmpty()) {
                entry.setStages(stages.toArray(new ResearchStage[0]));
            }
        }

        if (obj.has("addenda")) {
            JsonArray addendaJson = obj.getAsJsonArray("addenda");
            List<ResearchAddendum> addenda = new ArrayList<>();
            for (JsonElement addElement : addendaJson) {
                addenda.add(parseAddendumJson(addElement.getAsJsonObject()));
            }
            if (!addenda.isEmpty()) {
                entry.setAddenda(addenda.toArray(new ResearchAddendum[0]));
            }
        }

        return entry;
    }

    private static ResearchStage parseStageJson(JsonObject obj, String entryKey) {
        ResearchStage stage = new ResearchStage();

        // text опционален ( бывает stage только с рецептами): без проверки
        // падало NPE и выкидывало ВСЁ исследование из загрузки как invalid.
        if (obj.has("text") && !obj.get("text").isJsonNull()) {
            stage.setText(obj.get("text").getAsString());
        }

        if (obj.has("recipes")) {
            stage.setRecipes(arrayJsonToResourceLocations(obj.getAsJsonArray("recipes")));
        }

        if (obj.has("required_item")) {
            stage.setObtain(parseJsonOreList(arrayJsonToString(obj.getAsJsonArray("required_item"))));
        }

        if (obj.has("required_craft")) {
            String[] craftStrings = arrayJsonToString(obj.getAsJsonArray("required_craft"));
            Object[] craftItems = parseJsonOreList(craftStrings);
            stage.setCraft(craftItems);

            if (craftItems != null) {
                int[] refs = new int[craftItems.length];
                for (int i = 0; i < craftItems.length; i++) {
                    int code;
                    if (craftItems[i] instanceof ItemStack stack) {
                        code = createItemStackHash(stack);
                    } else {
                        code = ("tag:" + craftItems[i]).hashCode();
                    }
                    craftingReferences.add(code);
                    refs[i] = code;
                }
                stage.setCraftReference(refs);
            }
        }

        if (obj.has("required_knowledge")) {
            String[] knowStrings = arrayJsonToString(obj.getAsJsonArray("required_knowledge"));
            if (knowStrings != null) {
                List<ResearchStage.Knowledge> knowledge = new ArrayList<>();
                for (String s : knowStrings) {
                    ResearchStage.Knowledge k = ResearchStage.Knowledge.parse(s);
                    if (k != null) knowledge.add(k);
                }
                if (!knowledge.isEmpty()) {
                    stage.setKnow(knowledge.toArray(new ResearchStage.Knowledge[0]));
                }
            }
        }

        if (obj.has("required_research")) {
            String[] researchStrings = arrayJsonToString(obj.getAsJsonArray("required_research"));
            if (researchStrings != null) {
                String[] keys = new String[researchStrings.length];
                String[] icons = new String[researchStrings.length];
                for (int i = 0; i < researchStrings.length; i++) {
                    String[] parts = researchStrings[i].split(";");
                    keys[i] = parts[0];
                    icons[i] = parts.length > 1 ? parts[1] : null;
                }
                stage.setResearch(keys);
                stage.setResearchIcon(icons);
            }
        }

        if (obj.has("warp")) {
            stage.setWarp(obj.get("warp").getAsInt());
        }

        // NEOFORGE_PORT (discovered/*, Forge 1:1): структура-книга + required_discovery.
        if (obj.has("structure")) {
            String[] structStrings = arrayJsonToString(obj.getAsJsonArray("structure"));
            if (structStrings != null && structStrings.length > 0) {
                stage.setStructure(structStrings);
            }
        }

        if (obj.has("required_discovery")) {
            // Элемент-строка ("#KEY") — ключ открытия; элемент-булево — флаг
            // hex-стадии (вторая, мини-игра). false/отсутствие — только первая.
            // Объектная форма {"keys": [...], "hex": ...} тоже поддерживается.
            com.google.gson.JsonArray discArray = null;
            try {
                if (obj.get("required_discovery").isJsonArray()) {
                    discArray = obj.getAsJsonArray("required_discovery");
                } else if (obj.get("required_discovery").isJsonObject()) {
                    com.google.gson.JsonObject discObj = obj.getAsJsonObject("required_discovery");
                    if (discObj.has("keys") && discObj.get("keys").isJsonArray()) {
                        discArray = discObj.getAsJsonArray("keys");
                    }
                    if (discObj.has("hex") && !discObj.get("hex").isJsonNull()) {
                        try {
                            stage.setHex(discObj.get("hex").getAsBoolean());
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            if (discArray != null) {
                java.util.ArrayList<String> keys = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement el : discArray) {
                    if (el == null || el.isJsonNull()) {
                        continue;
                    }
                    try {
                        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                            stage.setHex(el.getAsJsonPrimitive().getAsBoolean());
                            continue;
                        }
                        String raw = el.getAsString();
                        if (raw.equalsIgnoreCase("hex:true")) {
                            stage.setHex(true);
                            continue;
                        }
                        if (raw.equalsIgnoreCase("hex:false")) {
                            stage.setHex(false);
                            continue;
                        }
                        String s = raw.trim();
                        keys.add(s.startsWith("#") ? s.substring(1) : s);
                    } catch (Exception ignored) {
                    }
                }
                if (!keys.isEmpty()) {
                    stage.setDiscovery(keys.toArray(new String[0]));
                }
            }
        }

        return stage;
    }

    private static ResearchAddendum parseAddendumJson(JsonObject obj) {
        ResearchAddendum addendum = new ResearchAddendum();

        addendum.setText(obj.get("text").getAsString());

        if (obj.has("recipes")) {
            addendum.setRecipes(arrayJsonToResourceLocations(obj.getAsJsonArray("recipes")));
        }

        if (obj.has("required_research")) {
            addendum.setResearch(arrayJsonToString(obj.getAsJsonArray("required_research")));
        }

        return addendum;
    }

    private static void addResearchToCategory(ResearchEntry entry) {
        ResearchCategory category = ResearchCategories.getResearchCategory(entry.getCategory());
        if (category == null) {
            Arcaneresearch.LOGGER.warn("Cannot add research {} - category {} not found",
                    entry.getKey(), entry.getCategory());
            return;
        }

        if (category.research.containsKey(entry.getKey())) {
            Arcaneresearch.LOGGER.warn("Research {} already exists in category {}",
                    entry.getKey(), entry.getCategory());
            return;
        }

        for (ResearchEntry existing : category.research.values()) {
            if (existing.getDisplayColumn() == entry.getDisplayColumn() &&
                    existing.getDisplayRow() == entry.getDisplayRow()) {
                Arcaneresearch.LOGGER.warn("Research {} overlaps with {} at position [{},{}]",
                        entry.getKey(), existing.getKey(), entry.getDisplayColumn(), entry.getDisplayRow());
                return;
            }
        }

        category.research.put(entry.getKey(), entry);

        if (entry.getDisplayColumn() < category.minDisplayColumn) {
            category.minDisplayColumn = entry.getDisplayColumn();
        }
        if (entry.getDisplayRow() < category.minDisplayRow) {
            category.minDisplayRow = entry.getDisplayRow();
        }
        if (entry.getDisplayColumn() > category.maxDisplayColumn) {
            category.maxDisplayColumn = entry.getDisplayColumn();
        }
        if (entry.getDisplayRow() > category.maxDisplayRow) {
            category.maxDisplayRow = entry.getDisplayRow();
        }
    }

    private static String[] arrayJsonToString(JsonArray array) {
        if (array == null || array.isEmpty()) return null;
        List<String> list = new ArrayList<>();
        for (JsonElement e : array) {
            list.add(e.getAsString());
        }
        return list.toArray(new String[0]);
    }

    private static Integer[] arrayJsonToInt(JsonArray array) {
        if (array == null || array.isEmpty()) return null;
        List<Integer> list = new ArrayList<>();
        for (JsonElement e : array) {
            list.add(e.getAsInt());
        }
        return list.toArray(new Integer[0]);
    }

    private static Identifier[] arrayJsonToResourceLocations(JsonArray array) {
        if (array == null || array.isEmpty()) return null;
        List<Identifier> list = new ArrayList<>();
        for (JsonElement e : array) {
            list.add(Identifier.parse(e.getAsString()));
        }
        return list.toArray(new Identifier[0]);
    }

    private static ItemStack[] parseJsonItemList(String[] strings) {
        if (strings == null || strings.length == 0) return null;
        List<ItemStack> items = new ArrayList<>();
        for (String s : strings) {
            ItemStack stack = parseJSONtoItemStack(s.replace("'", "\""));
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        return items.isEmpty() ? null : items.toArray(new ItemStack[0]);
    }

    private static Object[] parseJsonOreList(String[] strings) {
        if (strings == null || strings.length == 0) return null;
        List<Object> items = new ArrayList<>();
        for (String s : strings) {
            s = s.replace("'", "\"");
            if (s.startsWith("tag:") || s.startsWith("oredict:")) {

                String tagName = s.contains(":") ? s.substring(s.indexOf(":") + 1) : s;
                items.add(tagName);
            } else {
                ItemStack stack = parseJSONtoItemStack(s);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        return items.isEmpty() ? null : items.toArray();
    }

    public static ItemStack parseJSONtoItemStack(String entry) {
        if (entry == null || entry.isEmpty()) return ItemStack.EMPTY;

        entry = entry.replace("'", "\"");

        String[] split = entry.split(";");
        String name = split[0].trim();
        int count = 1;
        String nbt = null;

        for (int i = 1; i < split.length; i++) {
            String segment = split[i].trim();
            if (segment.startsWith("{")) {

                StringBuilder nbtBuilder = new StringBuilder();
                for (int j = i; j < split.length; j++) {
                    if (j > i) nbtBuilder.append(";");
                    nbtBuilder.append(split[j]);
                }
                nbt = nbtBuilder.toString();
                break;
            }
            try {
                int value = Integer.parseInt(segment);

                if (count == 1) {
                    count = Math.max(1, value);
                }

            } catch (NumberFormatException ignored) {
            }
        }

        name = name.toLowerCase();

        try {
            Identifier itemId = Identifier.parse(name);
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {

                String mappedName = LEGACY_ITEM_MAPPINGS.get(name);
                if (mappedName != null) {
                    itemId = Identifier.parse(mappedName);
                    item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId);
                }
            }

            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                ItemStack stack = new ItemStack(item, count);
                if (nbt != null && !nbt.isEmpty()) {
                    try {
                        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same parsed NBT).
                        // TagParser.parseTag -> parseCompoundFully in 26.x (checked exception
                        // already covered by the surrounding catch).
                        arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(
                                stack, TagParser.parseCompoundFully(nbt));
                    } catch (Exception nbtEx) {
                        Arcaneresearch.LOGGER.debug("Failed to parse NBT for {}: {}", entry, nbtEx.getMessage());
                    }
                }
                return stack;
            } else {

                if (!warnedItems.contains(name)) {
                    warnedItems.add(name);
                    Arcaneresearch.LOGGER.debug("Unknown item in research: {}", name);
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("Failed to parse item stack: {}", entry, e);
        }

        return ItemStack.EMPTY;
    }

    public static int createItemStackHash(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy.toString().hashCode();
    }
}
