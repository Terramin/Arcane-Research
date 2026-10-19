package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.init.ModItems;
import java.util.Random;
public class CardMindOverMatter extends TheorycraftCard {
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
            new ItemStack(Blocks.NOTE_BLOCK),
            new ItemStack(Items.COMPASS),
            new ItemStack(Items.CLOCK),
            new ItemStack(Items.MAP),
            new ItemStack(Items.SPYGLASS),
            new ItemStack(Items.RECOVERY_COMPASS)
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
    @Override
    public String getLocalizedName() {
        return "card.mindmatter.name";
    }
    @Override
    public String getLocalizedText() {
        return net.minecraft.network.chat.Component.translatable("card.mindmatter.text", getVal()).getString();
    }
    private int getVal() {
        int q = 10;
        try {
            q = (int) (q + Math.sqrt(arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager
                    .getObjectTags(stack).visSize()));
        } catch (Exception ignored) {
        }
        return q;
    }
    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{stack};
    }
    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true};
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), getVal());
        return true;
    }
}
