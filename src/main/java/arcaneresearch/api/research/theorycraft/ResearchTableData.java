package arcaneresearch.api.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchEntry.EnumResearchMeta;
import javax.annotation.Nullable;
import java.util.*;
public class ResearchTableData {
    public BlockEntity table;
    public String player;
    public int inspiration;
    public int inspirationStart;
    public int bonusDraws;
    public int placedCards;
    public int aidsChosen;
    public int penaltyStart;
    public List<Long> savedCards = new ArrayList<>();
    public List<String> aidCards = new ArrayList<>();
    public TreeMap<String, Integer> categoryTotals = new TreeMap<>();
    public List<String> categoriesBlocked = new ArrayList<>();
    public List<CardChoice> cardChoices = new ArrayList<>();
    public CardChoice lastDraw;
    public static class CardChoice {
        public TheorycraftCard card;
        public String key;
        public boolean fromAid;
        public boolean selected;
        public CardChoice(String key, TheorycraftCard card, boolean aid, boolean selected) {
            this.key = key;
            this.card = card;
            this.fromAid = aid;
            this.selected = selected;
        }
        @Override
        public String toString() {
            return "key:" + key +
                    " card:" + card.getSeed() +
                    " fromAid:" + fromAid +
                    " selected:" + selected;
        }
    }
    public ResearchTableData(BlockEntity tileResearchTable) {
        table = tileResearchTable;
    }
    public ResearchTableData(Player player, BlockEntity tileResearchTable) {
        this.player = player.getName().getString();
        table = tileResearchTable;
    }
    public boolean isComplete() {
        return inspiration <= 0;
    }
    public boolean hasTotal(String cat) {
        return categoryTotals.containsKey(cat);
    }
    public int getTotal(String cat) {
        return categoryTotals.getOrDefault(cat, 0);
    }
    public void addTotal(String cat, int amt) {
        int current = categoryTotals.getOrDefault(cat, 0);
        current += amt;
        if (current <= 0) {
            categoryTotals.remove(cat);
        } else {
            categoryTotals.put(cat, current);
        }
    }
    public void addInspiration(int amt) {
        inspiration += amt;
        if (inspiration > inspirationStart) inspiration = inspirationStart;
    }
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("player", player != null ? player : "");
        nbt.putInt("inspiration", inspiration);
        nbt.putInt("inspirationStart", inspirationStart);
        nbt.putInt("placedCards", placedCards);
        nbt.putInt("bonusDraws", bonusDraws);
        nbt.putInt("aidsChosen", aidsChosen);
        nbt.putInt("penaltyStart", penaltyStart);
        ListTag savedTag = new ListTag();
        for (Long card : savedCards) {
            CompoundTag gt = new CompoundTag();
            gt.putLong("card", card);
            savedTag.add(gt);
        }
        nbt.put("savedCards", savedTag);
        ListTag categoriesBlockedTag = new ListTag();
        for (String category : categoriesBlocked) {
            CompoundTag gt = new CompoundTag();
            gt.putString("category", category);
            categoriesBlockedTag.add(gt);
        }
        nbt.put("categoriesBlocked", categoriesBlockedTag);
        ListTag categoryTotalsTag = new ListTag();
        for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
            CompoundTag gt = new CompoundTag();
            gt.putString("category", entry.getKey());
            gt.putInt("total", entry.getValue());
            categoryTotalsTag.add(gt);
        }
        nbt.put("categoryTotals", categoryTotalsTag);
        ListTag aidCardsTag = new ListTag();
        for (String mc : aidCards) {
            CompoundTag gt = new CompoundTag();
            gt.putString("aidCard", mc);
            aidCardsTag.add(gt);
        }
        nbt.put("aidCards", aidCardsTag);
        ListTag cardChoicesTag = new ListTag();
        for (CardChoice mc : cardChoices) {
            CompoundTag gt = serializeCardChoice(mc);
            cardChoicesTag.add(gt);
        }
        nbt.put("cardChoices", cardChoicesTag);
        if (lastDraw != null) {
            nbt.put("lastDraw", serializeCardChoice(lastDraw));
        }
        return nbt;
    }
    public CompoundTag serializeCardChoice(CardChoice mc) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("cardChoice", mc.key);
        nbt.putBoolean("aid", mc.fromAid);
        nbt.putBoolean("select", mc.selected);
        try {
            nbt.put("cardNBT", mc.card.serialize());
        } catch (Exception e) {
        }
        return nbt;
    }
    public void deserialize(CompoundTag nbt) {
        if (nbt == null) return;
        inspiration = nbt.getInt("inspiration");
        inspirationStart = nbt.getInt("inspirationStart");
        placedCards = nbt.getInt("placedCards");
        bonusDraws = nbt.getInt("bonusDraws");
        aidsChosen = nbt.getInt("aidsChosen");
        penaltyStart = nbt.getInt("penaltyStart");
        player = nbt.getString("player");
        ListTag savedTag = nbt.getList("savedCards", Tag.TAG_COMPOUND);
        savedCards = new ArrayList<>();
        for (int x = 0; x < savedTag.size(); x++) {
            CompoundTag nbtdata = savedTag.getCompound(x);
            savedCards.add(nbtdata.getLong("card"));
        }
        ListTag categoriesBlockedTag = nbt.getList("categoriesBlocked", Tag.TAG_COMPOUND);
        categoriesBlocked = new ArrayList<>();
        for (int x = 0; x < categoriesBlockedTag.size(); x++) {
            CompoundTag nbtdata = categoriesBlockedTag.getCompound(x);
            categoriesBlocked.add(nbtdata.getString("category"));
        }
        ListTag categoryTotalsTag = nbt.getList("categoryTotals", Tag.TAG_COMPOUND);
        categoryTotals = new TreeMap<>();
        for (int x = 0; x < categoryTotalsTag.size(); x++) {
            CompoundTag nbtdata = categoryTotalsTag.getCompound(x);
            categoryTotals.put(nbtdata.getString("category"), nbtdata.getInt("total"));
        }
        ListTag aidCardsTag = nbt.getList("aidCards", Tag.TAG_COMPOUND);
        aidCards = new ArrayList<>();
        for (int x = 0; x < aidCardsTag.size(); x++) {
            CompoundTag nbtdata = aidCardsTag.getCompound(x);
            aidCards.add(nbtdata.getString("aidCard"));
        }
        ListTag cardChoicesTag = nbt.getList("cardChoices", Tag.TAG_COMPOUND);
        cardChoices = new ArrayList<>();
        for (int x = 0; x < cardChoicesTag.size(); x++) {
            CompoundTag nbtdata = cardChoicesTag.getCompound(x);
            CardChoice cc = deserializeCardChoice(nbtdata);
            if (cc != null) cardChoices.add(cc);
        }
        lastDraw = deserializeCardChoice(nbt.getCompound("lastDraw"));
    }
    @Nullable
    public CardChoice deserializeCardChoice(CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) return null;
        String key = nbt.getString("cardChoice");
        if (key.isEmpty()) return null;
        TheorycraftCard tc = generateCardWithNBT(key, nbt.getCompound("cardNBT"));
        if (tc == null) return null;
        return new CardChoice(key, tc, nbt.getBoolean("aid"), nbt.getBoolean("select"));
    }
    private boolean isCategoryBlocked(String cat) {
        return categoriesBlocked.contains(cat);
    }
    public void drawCards(int draw, Player pe) {
        if (draw == 3) {
            if (bonusDraws > 0) {
                bonusDraws--;
            } else {
                draw = 2;
            }
        }
        cardChoices.clear();
        player = pe.getName().getString();
        List<String> availCats = getAvailableCategories(pe);
        List<String> drawnCards = new ArrayList<>();
        boolean aidDrawn = false;
        int failsafe = 0;
        while (draw > 0 && failsafe < 10000) {
            failsafe++;
            if (!aidDrawn && !aidCards.isEmpty() && pe.getRandom().nextFloat() <= 0.25f) {
                int idx = pe.getRandom().nextInt(aidCards.size());
                String key = aidCards.get(idx);
                TheorycraftCard card = generateCard(key, -1, pe);
                if (card == null || card.getInspirationCost() > inspiration ||
                    isCategoryBlocked(card.getResearchCategory())) continue;
                if (drawnCards.contains(key)) continue;
                drawnCards.add(key);
                cardChoices.add(new CardChoice(key, card, true, false));
                aidCards.remove(idx);
            } else {
                try {
                    String[] cardKeys = TheorycraftManager.cards.keySet().toArray(new String[0]);
                    if (cardKeys.length == 0) break;
                    int idx = pe.getRandom().nextInt(cardKeys.length);
                    TheorycraftCard card = generateCard(cardKeys[idx], -1, pe);
                    if (card == null || card.isAidOnly() || card.getInspirationCost() > inspiration) continue;
                    if (card.getResearchCategory() != null) {
                        boolean found = false;
                        for (String cn : availCats) {
                            if (cn.equals(card.getResearchCategory())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) continue;
                    }
                    if (drawnCards.contains(cardKeys[idx])) continue;
                    drawnCards.add(cardKeys[idx]);
                    cardChoices.add(new CardChoice(cardKeys[idx], card, false, false));
                } catch (Exception e) {
                    continue;
                }
            }
            draw--;
        }
    }
    @Nullable
    private TheorycraftCard generateCard(String key, long seed, Player pe) {
        if (key == null) return null;
        Class<? extends TheorycraftCard> tcc = TheorycraftManager.cards.get(key);
        if (tcc == null) return null;
        TheorycraftCard tc = null;
        try {
            tc = tcc.getDeclaredConstructor().newInstance();
            if (seed < 0) {
                if (pe != null) {
                    tc.setSeed(pe.getRandom().nextLong());
                } else {
                    tc.setSeed(System.nanoTime());
                }
            } else {
                tc.setSeed(seed);
            }
            if (pe != null && !tc.initialize(pe, this)) return null;
        } catch (Exception e) {
        }
        return tc;
    }
    @Nullable
    private TheorycraftCard generateCardWithNBT(String key, CompoundTag nbt) {
        if (key == null) return null;
        Class<? extends TheorycraftCard> tcc = TheorycraftManager.cards.get(key);
        if (tcc == null) return null;
        TheorycraftCard tc = null;
        try {
            tc = tcc.getDeclaredConstructor().newInstance();
            tc.deserialize(nbt);
        } catch (Exception e) {
        }
        return tc;
    }
    public void initialize(Player player, Set<String> aids) {
        inspirationStart = getAvailableInspiration(player);
        inspiration = inspirationStart - aids.size();
        for (String muk : aids) {
            ITheorycraftAid mu = TheorycraftManager.aids.get(muk);
            if (mu != null) {
                for (Class<? extends TheorycraftCard> clazz : mu.getCards()) {
                    aidCards.add(clazz.getName());
                }
            }
        }
    }
    public List<String> getAvailableCategories(Player player) {
        List<String> cats = new ArrayList<>();
        for (String rck : ResearchCategories.researchCategories.keySet()) {
            ResearchCategory rc = ResearchCategories.getResearchCategory(rck);
            if (rc == null || isCategoryBlocked(rck)) continue;
            if (rc.researchKey == null || ArcaneresearchCapabilities.knowsResearchStrict(player, rc.researchKey)) {
                cats.add(rck);
            }
        }
        return cats;
    }
    public static int getAvailableInspiration(Player player) {
        float tot = 5;
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return 5;
        for (String s : knowledge.getResearchList()) {
            if (ArcaneresearchCapabilities.knowsResearchStrict(player, s)) {
                ResearchEntry re = ResearchCategories.getResearch(s);
                if (re == null) continue;
                if (re.hasMeta(EnumResearchMeta.SPIKY)) {
                    tot += 0.5f;
                }
                if (re.hasMeta(EnumResearchMeta.HIDDEN)) {
                    tot += 0.1f;
                }
            }
        }
        return Math.min(15, Math.round(tot));
    }
    @Nullable
    public Level getLevel() {
        return table != null ? table.getLevel() : null;
    }
}
