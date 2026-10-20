package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModItems;

public class CardCelestial extends TheorycraftCard {

    private int noteType1;
    private int noteType2;
    private String category = "BASICS";

    private static net.minecraft.world.item.Item noteItem(int md) {
        return switch (md) {
            case 0 -> ModItems.CELESTIAL_NOTES_SUN.get();
            case 1 -> ModItems.CELESTIAL_NOTES_STARS_1.get();
            case 2 -> ModItems.CELESTIAL_NOTES_STARS_2.get();
            case 3 -> ModItems.CELESTIAL_NOTES_STARS_3.get();
            case 4 -> ModItems.CELESTIAL_NOTES_STARS_4.get();
            case 5 -> ModItems.CELESTIAL_NOTES_MOON_1.get();
            case 6 -> ModItems.CELESTIAL_NOTES_MOON_2.get();
            case 7 -> ModItems.CELESTIAL_NOTES_MOON_3.get();
            case 8 -> ModItems.CELESTIAL_NOTES_MOON_4.get();
            case 9 -> ModItems.CELESTIAL_NOTES_MOON_5.get();
            case 10 -> ModItems.CELESTIAL_NOTES_MOON_6.get();
            case 11 -> ModItems.CELESTIAL_NOTES_MOON_7.get();
            default -> ModItems.CELESTIAL_NOTES_MOON_8.get();
        };
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putInt("md1", noteType1);
        nbt.putInt("md2", noteType2);
        nbt.putString("cat", category);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        noteType1 = nbt.getIntOr("md1", 0);
        noteType2 = nbt.getIntOr("md2", 0);
        category = nbt.getStringOr("cat", "");
    }

    @Override
    public String getResearchCategory() {
        return category;
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        if (data.categoryTotals.isEmpty() || !ArcaneresearchCapabilities.knowsResearch(player, "CELESTIALSCANNING")) {
            return false;
        }

        RandomSource r = RandomSource.create(getSeed());
        noteType1 = Mth.nextInt(r, 0, 12);
        noteType2 = noteType1;
        while (noteType1 == noteType2) {
            noteType2 = Mth.nextInt(r, 0, 12);
        }

        int highestValue = 0;
        String highestKey = "BASICS";
        for (String cat : data.categoryTotals.keySet()) {
            int value = data.getTotal(cat);
            if (value > highestValue) {
                highestValue = value;
                highestKey = cat;
            }
        }
        category = highestKey;

        return category != null;
    }

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getLocalizedName() {
        return "card.celestial.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.celestial.text";
    }

    @Override
    public ItemStack[] getRequiredItems() {

        return new ItemStack[]{
                new ItemStack(noteItem(noteType1)),
                new ItemStack(noteItem(noteType2))
        };
    }

    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true, true};
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), Mth.nextInt(player.getRandom(), 25, 50));

        boolean sun = noteType1 == 0 || noteType2 == 0;
        boolean moon = noteType1 > 4 || noteType2 > 4;
        boolean stars = (noteType1 > 0 && noteType1 < 5) || (noteType2 > 0 && noteType2 < 5);

        if (stars) {
            int amt = Mth.nextInt(player.getRandom(), 0, 5);
            data.addTotal("ELDRITCH", amt * 2);
            ArcaneresearchApi.internalMethods.addWarpToPlayer(player, amt, IPlayerWarp.EnumWarpType.TEMPORARY);
        }
        if (sun) {
            data.penaltyStart++;
        }
        if (moon) {
            data.bonusDraws++;
        }

        return true;
    }
}
