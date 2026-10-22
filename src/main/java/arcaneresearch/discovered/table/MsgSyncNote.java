package arcaneresearch.discovered.table;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.client.HexResearchTableScreen;
import arcaneresearch.helper.ClientRunHelper;

 








 
public class MsgSyncNote implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgSyncNote> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_sync_note"));

    public static final StreamCodec<ByteBuf, MsgSyncNote> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, p -> p.tag,
            MsgSyncNote::new);

    private final CompoundTag tag;

    public MsgSyncNote(HexResearchNoteData note) {
        CompoundTag nbt = new CompoundTag();
        if (note != null) {
            note.writeToNBT(nbt);
        }
        this.tag = nbt;
    }

    private MsgSyncNote(CompoundTag tag) {
        this.tag = tag != null ? tag : new CompoundTag();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgSyncNote msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRunHelper.runOnClient(
                () -> handleClient(msg)));
    }

    private static void handleClient(MsgSyncNote msg) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        CompoundTag tag = msg.tag;
        if (mc.player == null || tag == null) {
            return;
        }
        
        
        try {
            if (mc.player.containerMenu instanceof HexResearchTableMenu menu) {
                ItemStack stack = menu.getSlot(1).getItem();
                if (!stack.isEmpty()) {
                    HexResearchNoteData data = new HexResearchNoteData();
                    data.readFromNBT(tag);
                    HexResearchNoteData.updateData(stack, data);
                }
            }
        } catch (Exception ignored) {
        }
        if (mc.gui.screen() instanceof HexResearchTableScreen screen) {
            screen.applySync(tag);
        }
    }
}
