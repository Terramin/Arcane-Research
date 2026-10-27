package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.function.Supplier;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class MsgDupe {
    public MsgDupe() {
    }

    public MsgDupe(FriendlyByteBuf buf) {
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
            ItemStack source = menu.getSlot(1).getItem();
            if (source.isEmpty() || !HexResearchTableMenu.isNoteStack(source)) {
                return;
            }
            HexResearchNoteData note;
            try {
                note = HexResearchNoteData.getData(source);
            } catch (Exception e) {
                return;
            }
            if (note == null || !note.complete || note.hexEntries == null) {
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
            if (note.key != null && !note.key.isEmpty()) {
                try {
                    required = DiscoveredTableResearch.requiredAspects(note.key);
                } catch (Exception ignored) {
                }
            }

            final int size = targets.size();
            final int[] costs = new int[size];
            for (int i = 0; i < size; i++) {
                int amount = 1;
                try {
                    if (required != null) {
                        int a = required.getAmount(targets.get(i));
                        if (a > 0) {
                            amount = a;
                        }
                    }
                } catch (Exception ignored) {
                }
                costs[i] = amount + note.copies;
            }

            for (int i = 0; i < size; i++) {
                try {
                    if (ScanWallet.getAmount(player, targets.get(i)) < costs[i]) {
                        player.displayClientMessage(
                                Component.literal("Not enough aspects to copy"), true);
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
            }

            Inventory inv = player.getInventory();
            if (!hasItem(inv, Items.PAPER)) {
                player.displayClientMessage(Component.literal("Need paper to copy"), true);
                return;
            }
            if (!hasItem(inv, Items.INK_SAC)) {
                player.displayClientMessage(Component.literal("Need ink sac to copy"), true);
                return;
            }
            if (!consumeOne(inv, Items.PAPER) || !consumeOne(inv, Items.INK_SAC)) {
                return;
            }

            for (int i = 0; i < size; i++) {
                try {
                    ScanWallet.remove(player, targets.get(i), costs[i]);
                } catch (Exception ignored) {
                }
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
        context.setPacketHandled(true);
    }

    private static boolean hasItem(Inventory inv, Item item) {
        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOne(Inventory inv, Item item) {
        for (ItemStack stack : inv.items) {
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}