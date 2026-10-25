package arcaneresearch.common.items.baubles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.wands.IWand;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import javax.annotation.Nullable;
import java.util.List;
public class ItemAmuletVis extends Item implements ICurioItem, IRunicArmor {
    private final boolean isCrafted;
    public ItemAmuletVis(boolean crafted) {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(crafted ? Rarity.RARE : Rarity.UNCOMMON));
        this.isCrafted = crafted;
    }
    public static ItemAmuletVis createFound() {
        return new ItemAmuletVis(false);
    }
    public static ItemAmuletVis createCrafted() {
        return new ItemAmuletVis(true);
    }
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && !player.level().isClientSide()) {
            tickWorn(player, stack);
        }
    }
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("necklace");
    }
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
    public void tickWorn(Player player, ItemStack stack) {
        if (player.tickCount % 5 != 0) {
            return;
        }
        try {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && held.getItem() instanceof IWand wand) {
                AspectList room = wand.getAspectsWithRoom(held);
                if (room != null) {
                    for (Aspect aspect : room.getAspects()) {
                        if (aspect == null || getVis(stack, aspect) <= 0) {
                            continue;
                        }
                        int wandVis = held.hasTag() ? held.getTag().getInt(aspect.getTag()) : 0;
                        int amt = Math.min(5, wand.getMaxVis(held) * 100 - wandVis);
                        amt = Math.min(amt, getVis(stack, aspect));
                        if (amt > 0) {
                            storeVis(stack, aspect, getVis(stack, aspect) - amt);
                            held.getOrCreateTag().putInt(aspect.getTag(), wandVis + amt);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arcaneresearch.amulet_vis.text")
                .withStyle(ChatFormatting.AQUA));
        if (isCrafted) {
            tooltip.add(Component.translatable("item.arcaneresearch.amulet_vis.crafted")
                    .withStyle(ChatFormatting.GRAY));
        }
        try {
            java.text.DecimalFormat fmt = new java.text.DecimalFormat("#######.##");
            tooltip.add(Component.translatable("item.capacity.text")
                    .append(": " + fmt.format(getMaxVis(stack) / 100.0F))
                    .withStyle(ChatFormatting.GOLD));
            if (stack.hasTag()) {
                for (Aspect aspect : Aspect.getPrimalAspects()) {
                    if (aspect != null && stack.getTag().contains(aspect.getTag())) {
                        String amount = fmt.format(stack.getTag().getInt(aspect.getTag()) / 100.0F);
                        tooltip.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName()
                                        + "§r x " + amount));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
    public int getMaxVis(ItemStack stack) {
        return isCrafted ? 25000 : 2500;
    }
    public int getVis(ItemStack stack, Aspect aspect) {
        if (aspect != null && stack.hasTag() && stack.getTag().contains(aspect.getTag())) {
            return stack.getTag().getInt(aspect.getTag());
        }
        return 0;
    }
    public void storeVis(ItemStack stack, Aspect aspect, int amount) {
        if (aspect != null && !stack.isEmpty()) {
            stack.getOrCreateTag().putInt(aspect.getTag(), Math.max(0, amount));
        }
    }
    public AspectList getAllVis(ItemStack stack) {
        AspectList out = new AspectList();
        try {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (aspect == null) {
                    continue;
                }
                out.merge(aspect, stack.hasTag() ? stack.getTag().getInt(aspect.getTag()) : 0);
            }
        } catch (Exception ignored) {
        }
        return out;
    }
    public AspectList getAspectsWithRoom(ItemStack stack) {
        AspectList out = new AspectList();
        try {
            AspectList cur = getAllVis(stack);
            for (Aspect aspect : cur.getAspects()) {
                if (aspect != null && cur.getAmount(aspect) < getMaxVis(stack)) {
                    out.add(aspect, 1);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
    public boolean consumeAllVis(ItemStack stack, Player player, AspectList cost,
                                 boolean doit, boolean crafting) {
        if (cost == null || cost.size() == 0) {
            return false;
        }
        try {
            for (Aspect aspect : cost.getAspects()) {
                if (aspect == null) {
                    continue;
                }
                if (getVis(stack, aspect) < cost.getAmount(aspect) * 100) {
                    return false;
                }
            }
            if (doit) {
                for (Aspect aspect : cost.getAspects()) {
                    if (aspect == null) {
                        continue;
                    }
                    storeVis(stack, aspect, getVis(stack, aspect) - cost.getAmount(aspect) * 100);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public int addVis(ItemStack stack, Aspect aspect, int amount, boolean simulate) {
        if (aspect == null || !aspect.isPrimal()) {
            return amount;
        }
        int storeAmount = getVis(stack, aspect) + amount * 100;
        int leftover = Math.max(storeAmount - getMaxVis(stack), 0);
        if (!simulate) {
            storeVis(stack, aspect, Math.min(storeAmount, getMaxVis(stack)));
        }
        return leftover / 100;
    }
}
