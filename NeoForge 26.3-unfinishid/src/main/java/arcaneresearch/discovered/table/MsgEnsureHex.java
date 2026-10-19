package arcaneresearch.discovered.table;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.note.DiscoveredNoteData;

/**
 * Client -&gt; Server: сгенерировать hex-поле для проектной записки в слоте.
 * Ключ берётся из {@code DiscoveredNoteData} записки, цели — из
 * {@code HexStages}. Ответ — {@code MsgSyncNote}.
 *
 * NEOFORGE_PORT: SimpleChannel(HexTableNetwork) -&gt; CustomPacketPayload
 * (пустое тело); player.getRandom() жив.
 * Payload id "arcaneresearch:hex_ensure_hex".
 */
public class MsgEnsureHex implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgEnsureHex> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_ensure_hex"));

    public static final StreamCodec<ByteBuf, MsgEnsureHex> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgEnsureHex packet) {
        }

        @Override
        public MsgEnsureHex decode(ByteBuf buf) {
            return new MsgEnsureHex();
        }
    };

    public MsgEnsureHex() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgEnsureHex msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            HexResearchTableMenu menu =
                    player.containerMenu instanceof HexResearchTableMenu m ? m : null;
            if (menu == null) {
                return;
            }
            ItemStack stack = menu.getSlot(1).getItem();
            if (!HexResearchTableMenu.isNoteStack(stack)
                    || HexResearchNoteData.getData(stack) != null) {
                return;
            }
            String key;
            try {
                key = DiscoveredNoteData.read(stack).key;
            } catch (Exception e) {
                return;
            }
            if (key == null || key.isEmpty()) {
                return;
            }
            // Мини-игра не запускается без stage-1 на 100% (строгий порядок).
            if (!MsgPlaceAspect.stageOneComplete(stack)) {
                int pct;
                try {
                    pct = (int) (DiscoveredNoteData.read(stack).getTotalProgress() * 100.0F);
                } catch (Exception e) {
                    pct = 0;
                }
                player.sendSystemMessage(Component.literal(
                        "Finish Knowledge first (now " + pct + "%)"), true);
                return;
            }
            HexResearchNoteData note = HexResearchGame.createNote(
                    key, HexStages.targetsFor(key), HexStages.colorFor(key),
                    new java.util.Random(player.getRandom().nextLong()));
            HexResearchNoteData.updateData(stack, note);
            menu.broadcastChanges();
            PacketHandler.sendToPlayer(new MsgSyncNote(note), player);
        });
    }
}
