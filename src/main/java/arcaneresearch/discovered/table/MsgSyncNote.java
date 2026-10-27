package arcaneresearch.discovered.table;

import arcaneresearch.helper.ClientRunHelper;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;
import arcaneresearch.discovered.client.HexResearchTableScreen;

public class MsgSyncNote {
    private final CompoundTag tag;

    public MsgSyncNote(HexResearchNoteData note) {
        CompoundTag nbt = new CompoundTag();
        if (note != null) {
            note.writeToNBT(nbt);
        }
        this.tag = nbt;
    }

    public MsgSyncNote(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public static void handle(MsgSyncNote msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientRunHelper.runOnClient(
                () -> handleClient(msg)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(MsgSyncNote msg) {
        Minecraft mc = Minecraft.getInstance();
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
        if (mc.screen instanceof HexResearchTableScreen screen) {
            screen.applySync(tag);
        }
    }
}
