package arcaneresearch.discovered.table;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.MsgStudyReward;
import arcaneresearch.discovered.scan.ScanWallet;

 











 
public class MsgCombine implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgCombine> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_combine"));

    public static final StreamCodec<ByteBuf, MsgCombine> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgCombine packet) {
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.tag1);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.tag2);
        }

        @Override
        public MsgCombine decode(ByteBuf buf) {
            return new MsgCombine(ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf));
        }
    };

    private final String tag1;
    private final String tag2;

    public MsgCombine(String tag1, String tag2) {
        this.tag1 = tag1 != null ? tag1 : "";
        this.tag2 = tag2 != null ? tag2 : "";
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgCombine msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            
            Aspect a1;
            Aspect a2;
            try {
                a1 = Aspect.getAspect(msg.tag1);
                a2 = Aspect.getAspect(msg.tag2);
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
            if (tools.isEmpty()
                    || !HexResearchTableMenu.isInkStack(tools)
                    || tools.getDamageValue() >= tools.getMaxDamage()) {
                player.sendSystemMessage(Component.literal("Need scribing tools"), true);
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
                player.sendSystemMessage(Component.literal("No research points"), true);
                HexEconomy.push(player);
                return;
            }
            Aspect result = null;
            try {
                result = AspectHelper.getCombinationResult(a1, a2);
            } catch (Exception ignored) {
            }
            if (result == null) {
                
                player.sendSystemMessage(Component.literal("No combination"), true);
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
                PacketHandler.sendToPlayer(new MsgStudyReward(granted), player);
            } catch (Exception ignored) {
            }
            HexEconomy.push(player);
        });
    }
}
