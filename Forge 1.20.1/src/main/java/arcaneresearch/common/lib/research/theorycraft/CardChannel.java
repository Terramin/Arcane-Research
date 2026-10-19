package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.items.consumables.ItemPhial;
import java.util.Random;
public class CardChannel extends TheorycraftCard {
    Aspect aspect;
    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        if (aspect != null) {
            nbt.putString("aspect", aspect.getTag());
        }
        return nbt;
    }
    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        aspect = Aspect.getAspect(nbt.getString("aspect"));
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        int num = r.nextInt(Aspect.getCompoundAspects().size());
        aspect = Aspect.getCompoundAspects().get(num);
        return true;
    }
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "INFUSION";
    }
    @Override
    public String getLocalizedName() {
        String name = aspect != null ? aspect.getName() : "";
        return Component.translatable("card.channel.name",
                net.minecraft.ChatFormatting.DARK_BLUE + name
                        + net.minecraft.ChatFormatting.RESET + "" + net.minecraft.ChatFormatting.BOLD).getString();
    }
    @Override
    public String getLocalizedText() {
        String name = aspect != null ? aspect.getName() : "";
        return Component.translatable("card.channel.text",
                net.minecraft.ChatFormatting.BOLD + name + net.minecraft.ChatFormatting.RESET).getString();
    }
    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{ItemPhial.makeFilledPhial(aspect)};
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 25);
        return true;
    }
}
