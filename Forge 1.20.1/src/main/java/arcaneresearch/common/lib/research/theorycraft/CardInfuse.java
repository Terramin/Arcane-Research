package arcaneresearch.common.lib.research.theorycraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import java.util.Random;
public class CardInfuse extends TheorycraftCard {
    private static final ItemStack[] OPTIONS = {
            new ItemStack(arcaneresearch.init.ModItems.ALUMENTUM.get()),
            new ItemStack(arcaneresearch.init.ModItems.AMBER.get()),
            new ItemStack(arcaneresearch.init.ModItems.ZOMBIE_BRAIN.get()),
            new ItemStack(arcaneresearch.init.ModItems.ENCHANTED_FABRIC.get()),
            new ItemStack(arcaneresearch.init.ModItems.SALIS_MUNDUS.get()),
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(arcaneresearch.init.ModItems.THAUMIUM_INGOT.get()),
            new ItemStack(arcaneresearch.init.ModItems.QUICKSILVER.get()),
            new ItemStack(Items.GOLD_INGOT),
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.BLAZE_ROD),
            new ItemStack(Items.LEATHER),
            new ItemStack(Items.ARROW),
            new ItemStack(Items.EGG),
            new ItemStack(Items.FEATHER),
            new ItemStack(Items.GLOWSTONE_DUST),
            new ItemStack(Items.REDSTONE),
            new ItemStack(Items.GHAST_TEAR),
            new ItemStack(Items.GUNPOWDER),
            new ItemStack(Items.BOW),
            new ItemStack(Items.QUARTZ),
            new ItemStack(Items.APPLE)
    };
    private Aspect aspect;
    private ItemStack stack = ItemStack.EMPTY;
    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putString("aspect", aspect != null ? aspect.getTag() : "");
        nbt.put("stack", stack.save(new CompoundTag()));
        return nbt;
    }
    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        aspect = Aspect.getAspect(nbt.getString("aspect"));
        stack = ItemStack.of(nbt.getCompound("stack"));
    }
    @Override
    public boolean initialize(Player player, ResearchTableData data) {
        Random r = new Random(getSeed());
        if (Aspect.getCompoundAspects().isEmpty()) {
            aspect = Aspect.MAGIC;
        } else {
            int num = r.nextInt(Aspect.getCompoundAspects().size());
            aspect = Aspect.getCompoundAspects().get(num);
        }
        stack = OPTIONS[r.nextInt(OPTIONS.length)].copy();
        return aspect != null && !stack.isEmpty();
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
        return "card.infuse.name";
    }
    @Override
    public String getLocalizedText() {
        String aspectName = aspect != null ? aspect.getName() : "";
        return net.minecraft.network.chat.Component.translatable("card.infuse.text",
                net.minecraft.ChatFormatting.BOLD + aspectName + net.minecraft.ChatFormatting.RESET,
                stack.getHoverName().getString(), getValue()).getString();
    }
    private int getValue() {
        int q = 10;
        try {
            q = (int) (q + Math.sqrt(arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager
                    .getObjectTags(stack).visSize()) * 1.5);
        } catch (Exception ignored) {
        }
        return q;
    }
    @Override
    public ItemStack[] getRequiredItems() {
        return new ItemStack[]{stack,
                arcaneresearch.common.items.consumables.ItemPhial.makeFilledPhial(aspect)};
    }
    @Override
    public boolean[] getRequiredItemsConsumed() {
        return new boolean[]{true, true};
    }
    @Override
    public boolean activate(Player player, ResearchTableData data) {
        data.addTotal(getResearchCategory(), getValue());
        return true;
    }
}
