package arcaneresearch.common.entities.monster.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import arcaneresearch.Arcaneresearch;










public class ChampionModifier {

    public static final ChampionModifier[] MODS;
    public static final AttributeModifier ATTRIBUTE_MOD_NONE;
    public static final AttributeModifier ATTRIBUTE_MINUS_ONE;

    static {

        MODS = new ChampionModifier[]{
                new ChampionModifier(0, "bold", -1, new ChampionModBold()),
                new ChampionModifier(1, "spine", 2, new ChampionModSpined()),
                new ChampionModifier(2, "armor", 2, new ChampionModArmored()),
                new ChampionModifier(3, "mighty", -1, new ChampionModMighty()),
                new ChampionModifier(4, "grim", 1, new ChampionModGrim()),
                new ChampionModifier(5, "warded", 0, new ChampionModWarded()),
                new ChampionModifier(6, "warp", 1, new ChampionModWarp()),
                new ChampionModifier(7, "undying", 0, new ChampionModUndying()),
                new ChampionModifier(8, "fiery", 1, new ChampionModFire()),
                new ChampionModifier(9, "sickly", 1, new ChampionModSickly()),
                new ChampionModifier(10, "venomous", 1, new ChampionModPoison()),
                new ChampionModifier(11, "vampiric", 1, new ChampionModVampire()),
                new ChampionModifier(12, "infested", 2, new ChampionModInfested()),
                new ChampionModifier(13, "tainted", 0, new ChampionModTainted())
        };

        ATTRIBUTE_MOD_NONE = new AttributeModifier(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "champion/normal"),
                1.0, AttributeModifier.Operation.ADD_VALUE);
        ATTRIBUTE_MINUS_ONE = new AttributeModifier(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "champion/minus1"),
                -1.0, AttributeModifier.Operation.ADD_VALUE);
    }

    public final int id;
    public final int type;
    public final IChampionModifierEffect effect;
    public final AttributeModifier attributeMod;
    private final String name;

    public ChampionModifier(int id, String name, int type, IChampionModifierEffect effect) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.effect = effect;
        this.attributeMod = new AttributeModifier(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "champion/" + name),
                id + 2, AttributeModifier.Operation.ADD_VALUE);
    }

    public static ChampionModifier getById(int id) {
        if (id >= 0 && id < MODS.length) {
            return MODS[id];
        }
        return null;
    }

    public static ChampionModifier getRandomModifier(java.util.Random random) {
        return MODS[random.nextInt(MODS.length)];
    }

    public static int getChampionId(LivingEntity entity) {
        if (entity == null) return -1;
        
        return entity.getPersistentData().getIntOr("arcaneresearch_champion", 0) - 1;
    }

    public static void setChampionId(LivingEntity entity, int id) {
        if (entity == null) return;
        entity.getPersistentData().putInt("arcaneresearch_champion", id + 1);
    }

    public String getModNameLocalized() {
        return Component.translatable("champion.mod." + name).getString();
    }

    public String getName() {
        return name;
    }
}
