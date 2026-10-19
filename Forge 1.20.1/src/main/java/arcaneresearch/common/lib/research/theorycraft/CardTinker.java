package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModItems;
import java.util.Random;
public class CardTinker extends TheorycraftCard {
    static ItemStack[] options = new ItemStack[]{
            new ItemStack(ModItems.VIS_RESONATOR.get()),
            new ItemStack(ModItems.THAUMOMETER.get()),
            new ItemStack(Blocks.CRAFTING_TABLE),
            new ItemStack(Blocks.FURNACE),
            new ItemStack(Blocks.DISPENSER),
            new ItemStack(Blocks.DROPPER),
            new ItemStack(Blocks.OBSERVER),
            new ItemStack(Blocks.HOPPER),
            new ItemStack(Blocks.PISTON),
            new ItemStack(Blocks.STICKY_PISTON),
            new ItemStack(Blocks.DAYLIGHT_DETECTOR),
            new ItemStack(Blocks.REDSTONE_LAMP),
            new ItemStack(Blocks.NOTE_BLOCK)
    };
    ItemStack stack = ItemStack.EMPTY;
    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.put("stack", stack.save(new CompoundTag()));
        return nbt;
    }
    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        stack = ItemStack.of(nbt.getCompound("stack"));
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        stack = options[r.nextInt(options.length)].copy();
        return !stack.isEmpty();
    }
    @Override
    public int getInspirationCost() {
        return 1;
    }
    @Override
    public String getResearchCategory() {
        return "ARTIFICE";
    }
    private int getVal() {
        int q = 0;
        try {
            q = (int) (q + Math.sqrt(arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager
                    .getObjectTags(stack).visSize()));
        } catch (Exception ignored) {
        }
        return q;
    }
    @Override
    public String getLocalizedName() {
        return "card.tinker.name";
    }
    @Override
    public String getLocalizedText() {
        int a = getVal() * 2;
        int b = a + 10;
        return net.minecraft.network.chat.Component.translatable("card.tinker.text", a, b).getString();
    }
    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{stack};
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        int q = getVal() * 2;
        data.addTotal(getResearchCategory(), Mth.nextInt(player.getRandom(), q, q + 10));
        return true;
    }
}
