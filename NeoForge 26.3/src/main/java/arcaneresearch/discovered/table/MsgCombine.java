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

/**
 * Client -&gt; Server: соединить два аспекта.
 * Порт {@code PacketAspectCombinationToServer}: гейт — кошелёк/бонус
 * по каждому аспекту (без перков и ab-флагов клиента), списание ДО проверки
 * результата (плата за попытку), успех — {@code checkAndSync} (открытие +
 * пул 1/3) и анимация награды.
 *
 * NEOFORGE_PORT: SimpleChannel(HexTableNetwork) -&gt; CustomPacketPayload
 * (ручной StreamCodec: два тега строками). Награда — сообщениями
 * scan-канала (на table-канале не зарегистрирована — было
 * "invalid message" и тишина).
 * Payload id "arcaneresearch:hex_combine".
 */
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
            // Пул — хранилище игрока (кошелёк/бонус), записка не нужна.
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
            // Соединение — только с годными инструментами (гейт, без порчи).
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
                player.sendSystemMessage(Component.literal("Need scribing tools"), true);
                return;
            }
            // Списание каждого прошедшего гейт (кошелёк, иначе бонус тайла).
            // Частичная оплата возможна — как в оригинале (без рефанда).
            boolean paid1 = HexEconomy.takeAspect(player, menu, a1);
            boolean paid2 = HexEconomy.takeAspect(player, menu, a2);
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
                // Плата за попытку остаётся списанной (паритет).
                player.sendSystemMessage(Component.literal("No combination"), true);
                HexEconomy.push(player);
                return;
            }
            // Порт checkAndSyncAspectKnowledge(combo, 1): открытие + пул
            // (1 знаю, 3 новое) + анимация награды.
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
            // Награда — сообщениями scan-канала (на table-канале не
            // зарегистрирована — было "invalid message" и тишина).
            PacketHandler.sendToPlayer(new MsgStudyReward(granted), player);
            HexEconomy.push(player);
        });
    }
}
