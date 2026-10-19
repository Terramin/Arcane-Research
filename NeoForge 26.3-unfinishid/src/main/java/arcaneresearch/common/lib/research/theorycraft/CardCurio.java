package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardCurio extends TheorycraftCard {

    private ItemStack curio = ItemStack.EMPTY;

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.put("stack", ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, curio).result().orElseGet(net.minecraft.nbt.CompoundTag::new));
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        curio = ItemStack.OPTIONAL_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt.getCompoundOrEmpty("stack")).result().orElse(ItemStack.EMPTY);
    }

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getLocalizedName() {
        return "card.curio.name";
    }

    @Override
    public String getLocalizedText() {
        return "card.curio.text";
    }

    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{curio};
    }

    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true};
    }

    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        List<ItemStack> curios = new ArrayList<>();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof arcaneresearch.common.items.curios.ItemCurio) {
                ItemStack c = stack.copy();
                c.setCount(1);
                curios.add(c);
            }
        }

        if (!curios.isEmpty()) {
            curio = curios.get(r.nextInt(curios.size()));
        }

        return !curio.isEmpty();
    }

    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal("BASICS", 5);

        String[] categories = ResearchCategories.researchCategories.keySet().toArray(new String[0]);
        if (categories.length > 0) {
            data.addTotal(categories[player.getRandom().nextInt(categories.length)], 5);
        }

        String type = curio.getItem() instanceof arcaneresearch.common.items.curios.ItemCurio itemCurio
                ? itemCurio.getVariant() : "";
        switch (type) {
            case "arcane" -> data.addTotal("AUROMANCY", Mth.nextInt(player.getRandom(), 25, 35));
            case "preserved" -> data.addTotal("ALCHEMY", Mth.nextInt(player.getRandom(), 25, 35));
            case "ancient" -> data.addTotal("GOLEMANCY", Mth.nextInt(player.getRandom(), 25, 35));
            case "eldritch" -> data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), 25, 35));
            case "knowledge" -> data.addTotal("INFUSION", Mth.nextInt(player.getRandom(), 25, 35));
            case "twisted" -> data.addTotal("ARTIFICE", Mth.nextInt(player.getRandom(), 25, 35));
            case "rites" -> {
                data.addTotal("ELDRITCH", Mth.nextInt(player.getRandom(), 15, 20));
                data.addTotal("AUROMANCY", Mth.nextInt(player.getRandom(), 10, 15));
            }
            default -> data.addTotal("BASICS", Mth.nextInt(player.getRandom(), 25, 35));
        }

        if (player.getRandom().nextBoolean()) data.bonusDraws++;
        if (player.getRandom().nextBoolean()) data.bonusDraws++;

        return true;
    }
}
