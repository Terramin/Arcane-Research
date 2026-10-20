package arcaneresearch.discovered.table;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.discovered.note.DiscoveredNoteData;
public class MsgEnsureHex {
    public MsgEnsureHex() {
    }
    public MsgEnsureHex(FriendlyByteBuf buf) {
    }
    public void encode(FriendlyByteBuf buf) {
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
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
            if (!MsgPlaceAspect.stageOneComplete(stack)) {
                int pct;
                try {
                    pct = (int) (DiscoveredNoteData.read(stack).getTotalProgress() * 100.0F);
                } catch (Exception e) {
                    pct = 0;
                }
                player.displayClientMessage(Component.literal(
                        "Finish Knowledge first (now " + pct + "%)"), true);
                return;
            }
            HexResearchNoteData note = HexResearchGame.createNote(
                    key, HexStages.targetsFor(key), HexStages.colorFor(key),
                    new java.util.Random(player.getRandom().nextLong()));
            HexResearchNoteData.updateData(stack, note);
            menu.broadcastChanges();
            HexTableNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgSyncNote(note));
        });
        ctx.get().setPacketHandled(true);
    }
}
