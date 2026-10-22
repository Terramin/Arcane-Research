package thaumictinker.gear;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.lib.research.ResearchManager;

 




 
public class ShareTomeItem extends Item {

    private static final String DATA_TAG = "thaumictinker:share";

    public ShareTomeItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static ShareData data(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(DATA_TAG, CompoundTag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag tag = stack.getTag().getCompound(DATA_TAG);
        if (!tag.contains("player") || !tag.contains("research")) {
            return null;
        }
        List<String> research = new ArrayList<>();
        ListTag list = tag.getList("research", Tag.TAG_STRING);
        for (int index = 0; index < list.size(); index++) {
            research.add(list.getString(index));
        }
        return new ShareData(tag.getString("player"), research);
    }

    public static void setData(ItemStack stack, @Nullable ShareData data) {
        if (!stack.hasTag()) {
            if (data == null) {
                return;
            }
            stack.getOrCreateTag();
        }
        if (data == null) {
            stack.getTag().remove(DATA_TAG);
            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("player", data.player());
        ListTag list = new ListTag();
        for (String key : data.research()) {
            list.add(StringTag.valueOf(key));
        }
        tag.put("research", list);
        stack.getOrCreateTag().put(DATA_TAG, tag);
    }

    public static List<String> snapshot(Player player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(knowledge.getResearchList());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        ItemStack stack = player.getItemInHand(hand);
        ShareData data = data(stack);
        if (data == null) {
            setData(stack, new ShareData(player.getGameProfile().getName(), snapshot(player)));
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.shareTome.write"));
            return InteractionResultHolder.success(stack);
        }
        ServerPlayer owner = serverPlayer.level().getServer().getPlayerList().getPlayerByName(data.player());
        List<String> research = owner == null ? data.research() : snapshot(owner);
        for (String key : research) {
            ResearchManager.completeResearch(serverPlayer, key, false);
        }
        player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.shareTome.sync"));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ShareData data = data(stack);
        tooltip.add(data == null
                ? Component.translatable("ttinkerer.ttmisc.shareTome.noAssign")
                : Component.translatable("ttinkerer.ttmisc.shareTome.playerName", data.player()));
    }
}
