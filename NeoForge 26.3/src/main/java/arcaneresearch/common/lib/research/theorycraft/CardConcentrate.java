package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

import java.util.Random;

public class CardConcentrate extends TheorycraftCard {

    private Aspect aspect;

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putString("aspect", aspect != null ? aspect.getTag() : "");
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        aspect = Aspect.getAspect(nbt.getStringOr("aspect", ""));
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        if (Aspect.getCompoundAspects().isEmpty()) {
            aspect = Aspect.ORDER;
        } else {
            int num = r.nextInt(Aspect.getCompoundAspects().size());
            aspect = Aspect.getCompoundAspects().get(num);
        }
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
        return "card.concentrate.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.concentrate.text";
    }

    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{ArcaneresearchApiHelper.makeCrystal(aspect)};
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), 15);
        data.bonusDraws++;
        if (player.getRandom().nextFloat() < 0.33f) {
            data.addInspiration(1);
        }
        return true;
    }
}
