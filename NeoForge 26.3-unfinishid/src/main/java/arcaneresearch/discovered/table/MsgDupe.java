package arcaneresearch.discovered.table;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.table.DiscoveredTableResearch;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;

/**
 * Client -&gt; Server: скопировать завершённую записку.
 * Порт кнопки 5 контейнера ({@code duplicate}): цена — из КОШЕЛЬКА игрока
 * по каждому аспекту (требуемое количество + copies), бумага + чернильный
 * мешок (оба проверяются ДО списания). Копия — полный клон предмета
 * (hex-слой и проектный слой), счётчик copies растёт на обеих.
 *
 * NEOFORGE_PORT: SimpleChannel(HexTableNetwork) -&gt; CustomPacketPayload
 * (пустое тело); player.drop — 3-аргументный с Prediction.SERVER_ONLY.
 * Payload id "arcaneresearch:hex_dupe".
 */
public class MsgDupe implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgDupe> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_dupe"));

    public static final StreamCodec<ByteBuf, MsgDupe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgDupe packet) {
        }

        @Override
        public MsgDupe decode(ByteBuf buf) {
            return new MsgDupe();
        }
    };

    public MsgDupe() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgDupe msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
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
            // Цена копии — как rr.tags: требуемое количество + copies,
            // из кошелька игрока (паритет duplicate).
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
                    player.sendSystemMessage(Component.literal("Not enough aspects to copy"), true);
                    return;
                }
            }
            Inventory inv = player.getInventory();
            // Оба ресурса проверяются до списания (иначе терялась бы бумага).
            if (!inv.contains(new ItemStack(Items.PAPER))) {
                player.sendSystemMessage(Component.literal("Need paper to copy"), true);
                return;
            }
            if (!inv.contains(new ItemStack(Items.INK_SAC))) {
                player.sendSystemMessage(Component.literal("Need ink sac to copy"), true);
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
            // Полный клон: проектный слой (DiscoveredNote) копируется вместе
            // с предметом, hex-слой перезаписывается со счётчиком.
            ItemStack copy = source.copy();
            HexResearchNoteData.updateData(copy, note);
            // NEOFORGE_PORT: placeItemBackInInventory now needs Prediction (server-side).
            inv.placeItemBackInInventory(copy, net.minecraft.util.Prediction.SERVER_ONLY);
            if (!copy.isEmpty()) {
                player.drop(copy, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            HexResearchNoteData.updateData(source, note);
            menu.broadcastChanges();
            PacketHandler.sendToPlayer(new MsgSyncNote(note), player);
            HexEconomy.push(player);
        });
    }

    private static boolean consumeOne(Inventory inv, net.minecraft.world.item.Item item) {
        // NEOFORGE_PORT: Inventory.items now private -> slot iteration (same stacks).
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
