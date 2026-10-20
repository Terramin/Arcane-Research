package arcaneresearch.api.research.theorycraft;

import java.util.HashMap;
import java.util.Map;

public class TheorycraftManager {

    public static final Map<String, ITheorycraftAid> aids = new HashMap<>();

    public static final Map<String, Class<? extends TheorycraftCard>> cards = new HashMap<>();

    public static void registerAid(ITheorycraftAid aid) {
        String key = aid.getClass().getName();
        if (!aids.containsKey(key)) {
            aids.put(key, aid);
        }
    }

    public static void registerCard(Class<? extends TheorycraftCard> cardClass) {
        String key = cardClass.getName();
        if (!cards.containsKey(key)) {
            cards.put(key, cardClass);
        }
    }

    public static Class<? extends TheorycraftCard> getCardClass(String key) {
        return cards.get(key);
    }

    public static ITheorycraftAid getAid(String key) {
        return aids.get(key);
    }

    public static TheorycraftCard createCard(String key) {
        Class<? extends TheorycraftCard> cardClass = cards.get(key);
        if (cardClass == null) return null;
        try {
            return cardClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
