package arcaneresearch.common.items.resources;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Full port of Thaumcraft ItemResource metadata 15 (Primal Charm). While it
 * is carried it may drop a primal essence crystal, reveal primal-focus lore,
 * and occasionally whisper a hint in its tooltip.
 */
public class ItemPrimalCharm extends Item {

    private static final Aspect[] PRIMALS = {
            Aspect.AIR, Aspect.EARTH, Aspect.FIRE,
            Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY
    };
    private static final String CLUE = "!LEGACYFOCUSPRIMAL";

    public ItemPrimalCharm() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity,
                              int slot, boolean selected) {
        if (level.isClientSide) {
            return;
        }
        // The original cleared the stale "blurb" tag on every inventory tick.
        if (stack.hasTag() && stack.getTag().contains("blurb")) {
            stack.getTag().remove("blurb");
        }
        int roll = level.getRandom().nextInt(20000);
        if (roll < 20) {
            Aspect aspect = PRIMALS[level.getRandom().nextInt(PRIMALS.length)];
            ItemStack essence = ItemCrystalEssence.createStack(ModItems.CRYSTAL_ESSENCE.get(), aspect, 1);
            ItemEntity orb = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), essence);
            orb.setDeltaMovement(level.getRandom().nextGaussian() * 0.05,
                    level.getRandom().nextFloat() * 0.2,
                    level.getRandom().nextGaussian() * 0.05);
            level.addFreshEntity(orb);
        } else if (roll == 42 && entity instanceof Player player && !hasClue(player)) {
            player.displayClientMessage(
                    Component.translatable("tc.primalcharm.trigger")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            ArcaneresearchApi.internalMethods.completeResearch(player, CLUE);
        }
    }

    private static boolean hasClue(Player player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player).orElse(null);
        return knowledge != null && (knowledge.isResearchComplete(CLUE)
                || knowledge.isResearchComplete("LEGACYFOCUSPRIMAL"));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        long time = level != null ? level.getGameTime() / 120L : 0L;
        Random random = new Random(stack.hashCode() + time);
        if (random.nextInt(200) < 25) {
            tooltip.add(Component.translatable("tc.primalcharm." + random.nextInt(5))
                    .withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
