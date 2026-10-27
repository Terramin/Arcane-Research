package arcaneresearch.discovered.table;

import java.util.function.Supplier;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.scan.MsgStudyReward;
import arcaneresearch.discovered.scan.ScanNetwork;
import arcaneresearch.discovered.scan.ScanWallet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class MsgCombine {
    private final String tag1;
    private final String tag2;

    public MsgCombine(String tag1, String tag2) {
        this.tag1 = tag1 != null ? tag1 : "";
        this.tag2 = tag2 != null ? tag2 : "";
    }

    public MsgCombine(FriendlyByteBuf buf) {
        this.tag1 = buf.readUtf();
        this.tag2 = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(tag1);
        buf.writeUtf(tag2);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Aspect a1;
            Aspect a2;
            try {
                a1 = Aspect.getAspect(tag1);
                a2 = Aspect.getAspect(tag2);
            } catch (Exception e) {
                return;
            }
            if (a1 == null || a2 == null) {
                return;
            }

            if (!(player.containerMenu instanceof HexResearchTableMenu menu)) {
                return;
            }

            ItemStack tools;
            try {
                tools = menu.getSlot(0).getItem();
            } catch (Exception e) {
                return;
            }
            if (tools.isEmpty()
                    || !HexResearchTableMenu.isInkStack(tools)
                    || tools.getDamageValue() >= tools.getMaxDamage()) {
                player.displayClientMessage(Component.literal("Need scribing tools"), true);
                return;
            }

            boolean paid1;
            boolean paid2;
            try {
                paid1 = HexEconomy.takeAspect(player, menu, a1);
                paid2 = HexEconomy.takeAspect(player, menu, a2);
            } catch (Exception e) {
                HexEconomy.push(player);
                return;
            }
            if (!paid1 || !paid2) {
                player.displayClientMessage(Component.literal("No research points"), true);
                HexEconomy.push(player);
                return;
            }

            Aspect result = null;
            try {
                result = AspectHelper.getCombinationResult(a1, a2);
            } catch (Exception ignored) {
            }
            if (result == null) {
                player.displayClientMessage(Component.literal("No combination"), true);
                HexEconomy.push(player);
                return;
            }

            boolean known;
            try {
                known = HexResearchGame.isKnown(player, result);
            } catch (Exception e) {
                HexEconomy.push(player);
                return;
            }
            if (!known) {
                try {
                    ArcaneresearchApi.internalMethods.progressResearch(player, "!" + result.getTag());
                } catch (Exception ignored) {
                }
            }

            int amount = known ? 1 : 3;
            ScanWallet.add(player, result, amount);
            AspectList granted = new AspectList();
            granted.add(result, amount);
            try {
                ScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new MsgStudyReward(granted));
            } catch (Exception ignored) {
            }
            HexEconomy.push(player);
        });
        context.setPacketHandled(true);
    }
}