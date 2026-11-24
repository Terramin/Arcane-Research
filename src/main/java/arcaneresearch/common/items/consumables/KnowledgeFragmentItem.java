package arcaneresearch.common.items.consumables;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.scan.MsgStudyReward;
import arcaneresearch.discovered.scan.MsgWalletRequest;
import arcaneresearch.discovered.scan.ScanNetwork;
import arcaneresearch.discovered.scan.ScanWallet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Port of Thaumcraft 4.2.3.5 ItemResource metadata 9 ("knowledge fragment").
 * Consuming one grants 1-2 points of each of the six primal aspects, matching
 * the original aspect-pool reward. The 1.20.1 port stores those points in the
 * scan wallet used by the discovery table instead of the removed aspect pool.
 */
public class KnowledgeFragmentItem extends Item {

    private static final Aspect[] PRIMALS = {
            Aspect.AIR, Aspect.EARTH, Aspect.FIRE,
            Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY
    };

    public KnowledgeFragmentItem() {
        super(new Item.Properties());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            AspectList granted = new AspectList();
            for (Aspect aspect : PRIMALS) {
                int amount = 1 + player.getRandom().nextInt(2);
                ScanWallet.add(player, aspect, amount);
                granted.add(aspect, amount);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                MsgWalletRequest.push(serverPlayer);
                ScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new MsgStudyReward(granted));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tc.research.add").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
