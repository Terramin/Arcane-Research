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
public class CardReactions extends TheorycraftCard {
    Aspect aspect1;
    Aspect aspect2;
    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        if (aspect1 != null) nbt.putString("aspect1", aspect1.getTag());
        if (aspect2 != null) nbt.putString("aspect2", aspect2.getTag());
        return nbt;
    }
    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        aspect1 = Aspect.getAspect(nbt.getString("aspect1"));
        aspect2 = Aspect.getAspect(nbt.getString("aspect2"));
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        int num = r.nextInt(Aspect.getCompoundAspects().size());
        aspect1 = Aspect.getCompoundAspects().get(num);
        int num2 = num;
        while (num2 == num) {
            num2 = r.nextInt(Aspect.getCompoundAspects().size());
        }
        aspect2 = Aspect.getCompoundAspects().get(num2);
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
        return "card.reactions.name";
    }
    @Override
    public String getLocalizedText() {
        String a1 = aspect1 != null ? aspect1.getName() : "";
        String a2 = aspect2 != null ? aspect2.getName() : "";
        return Component.translatable("card.reactions.text",
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
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 25);
        if (player.getRandom().nextFloat() < 0.33f) data.addInspiration(1);
        return true;
    }
}
