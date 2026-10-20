package arcaneresearch.discovered.table;
import java.util.function.Supplier;
import arcaneresearch.discovered.scan.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
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
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
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
            HexResearchTableMenu menu = player.containerMenu instanceof HexResearchTableMenu m
                    ? m : null;
            if (menu == null) {
                return;
            }
            ItemStack tools;
            try {
                tools = menu.getSlot(0).getItem();
            } catch (Exception e) {
                return;
            }
            if (!HexResearchTableMenu.isInkStack(tools)
                    || tools.getDamageValue() >= tools.getMaxDamage()) {
                player.displayClientMessage(Component.literal("Need scribing tools"), true);
                return;
            }
            boolean paid1 = HexEconomy.takeAspect(player, menu, a1);
            boolean paid2 = HexEconomy.takeAspect(player, menu, a2);
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
            boolean known = HexResearchGame.isKnown(player, result);
            if (!known) {
                try {
                    ArcaneresearchApi.internalMethods.progressResearch(player, "!" + result.getTag());
                } catch (Exception ignored) {
                }
            }
            ScanWallet.add(player, result, known ? 1 : 3);
            AspectList granted = new AspectList();
            granted.add(result, known ? 1 : 3);
            ScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgStudyReward(granted));
            HexEconomy.push(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
