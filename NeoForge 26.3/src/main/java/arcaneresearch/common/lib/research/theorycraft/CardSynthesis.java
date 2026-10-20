package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

import java.util.Random;

public class CardSynthesis extends TheorycraftCard {

    Aspect aspect1;
    Aspect aspect2;
    Aspect aspect3;

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        if (aspect1 != null) nbt.putString("aspect1", aspect1.getTag());
        if (aspect2 != null) nbt.putString("aspect2", aspect2.getTag());
        if (aspect3 != null) nbt.putString("aspect3", aspect3.getTag());
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        aspect1 = Aspect.getAspect(nbt.getStringOr("aspect1", ""));
        aspect2 = Aspect.getAspect(nbt.getStringOr("aspect2", ""));
        aspect3 = Aspect.getAspect(nbt.getStringOr("aspect3", ""));
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        int num = r.nextInt(Aspect.getCompoundAspects().size());
        aspect3 = Aspect.getCompoundAspects().get(num);
        aspect1 = aspect3.getComponents()[0];
        aspect2 = aspect3.getComponents()[1];
        return true;
    }

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getResearchCategory() {
        return "ALCHEMY";
    }

    @Override
    public String getLocalizedName() {
        return "card.synthesis.name";
    }

    @Override
    public String getLocalizedText() {
        String a1 = aspect1 != null ? aspect1.getName() : "";
        String a2 = aspect2 != null ? aspect2.getName() : "";
        return Component.translatable("card.synthesis.text",
                net.minecraft.ChatFormatting.BOLD + a1 + net.minecraft.ChatFormatting.RESET,
                net.minecraft.ChatFormatting.BOLD + a2 + net.minecraft.ChatFormatting.RESET).getString();
    }

    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{
                ArcaneresearchApiHelper.makeCrystal(aspect1),
                ArcaneresearchApiHelper.makeCrystal(aspect2)
        };
    }

    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true, true};
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {

        ItemStack res = ArcaneresearchApiHelper.makeCrystal(aspect3);
        data.addTotal(getResearchCategory(), 40);
        if (player.getRandom().nextFloat() < 0.33f) data.addInspiration(1);
        if (!res.isEmpty() && !player.getInventory().add(res)) {
            player.drop(res, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        return true;
    }
}
