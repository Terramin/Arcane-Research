package arcaneresearch.discovered.table;

import java.util.function.Supplier;

import arcaneresearch.discovered.note.DiscoveredNoteData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class MsgEnsureHex {
    public MsgEnsureHex() {
    }

    public MsgEnsureHex(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof HexResearchTableMenu menu)) {
                return;
            }
            ItemStack stack = menu.getSlot(1).getItem();
            if (stack.isEmpty() || !HexResearchTableMenu.isNoteStack(stack)
                    || HexResearchNoteData.getData(stack) != null) {
                return;
            }
            DiscoveredNoteData data;
            try {
                data = DiscoveredNoteData.read(stack);
            } catch (Exception e) {
                return;
            }
            String key = data.key;
            if (key == null || key.isEmpty()) {
                return;
            }
            if (!MsgPlaceAspect.stageOneComplete(stack)) {
                int pct;
                try {
                    pct = (int) (data.getTotalProgress() * 100.0F);
                } catch (Exception e) {
                    pct = 0;
                }
                player.displayClientMessage(Component.literal(
                        "Finish Knowledge first (now " + pct + "%)"), true);
                return;
            }
            HexResearchNoteData note;
            try {
                note = HexResearchGame.createNote(
                        key, HexStages.targetsFor(key), HexStages.colorFor(key),
                        new java.util.Random(player.getRandom().nextLong()));
            } catch (Exception e) {
                return;
            }
            HexResearchNoteData.updateData(stack, note);
            menu.broadcastChanges();
            HexTableNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgSyncNote(note));
        });
        context.setPacketHandled(true);
    }
}