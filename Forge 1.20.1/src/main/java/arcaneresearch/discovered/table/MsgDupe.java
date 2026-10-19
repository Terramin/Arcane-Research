package arcaneresearch.discovered.table;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.table.DiscoveredTableResearch;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
public class MsgDupe {
    public MsgDupe() {
    }
    public MsgDupe(FriendlyByteBuf buf) {
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
            ItemStack source = menu.getSlot(1).getItem();
            if (!HexResearchTableMenu.isNoteStack(source)) {
                return;
            }
            HexResearchNoteData note = HexResearchNoteData.getData(source);
            if (note == null || !note.complete) {
                return;
            }
            ArrayList<Aspect> targets = new ArrayList<>();
            for (HexEntry entry : note.hexEntries.values()) {
                if (entry != null && entry.type == 1 && entry.aspect != null
                        && !targets.contains(entry.aspect)) {
                    targets.add(entry.aspect);
                }
            }
            AspectList required = null;
            try {
                required = DiscoveredTableResearch.requiredAspects(note.key);
            } catch (Exception ignored) {
            }
            for (Aspect target : targets) {
                int amount = 1;
                try {
                    if (required != null && required.getAmount(target) > 0) {
                        amount = required.getAmount(target);
                    }
                } catch (Exception ignored) {
                }
                if (ScanWallet.getAmount(player, target) < amount + note.copies) {
                    player.displayClientMessage(Component.literal("Not enough aspects to copy"), true);
                    return;
                }
            }
            Inventory inv = player.getInventory();
            if (!inv.contains(new ItemStack(Items.PAPER))) {
                player.displayClientMessage(Component.literal("Need paper to copy"), true);
                return;
            }
            if (!inv.contains(new ItemStack(Items.INK_SAC))) {
                player.displayClientMessage(Component.literal("Need ink sac to copy"), true);
                return;
            }
            if (!consumeOne(inv, Items.PAPER) || !consumeOne(inv, Items.INK_SAC)) {
                return;
            }
            for (Aspect target : targets) {
                int amount = 1;
                try {
                    if (required != null && required.getAmount(target) > 0) {
                        amount = required.getAmount(target);
                    }
                } catch (Exception ignored) {
                }
                ScanWallet.remove(player, target, amount + note.copies);
            }
            note.copies++;
            ItemStack copy = source.copy();
            HexResearchNoteData.updateData(copy, note);
            inv.placeItemBackInInventory(copy);
            if (!copy.isEmpty()) {
                player.drop(copy, false);
            }
            HexResearchNoteData.updateData(source, note);
            menu.broadcastChanges();
            HexTableNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgSyncNote(note));
            HexEconomy.push(player);
        });
        ctx.get().setPacketHandled(true);
    }
    private static boolean consumeOne(Inventory inv, net.minecraft.world.item.Item item) {
        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
