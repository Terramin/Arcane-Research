package arcaneresearch.discovered.table;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModSounds;

public class MsgPlaceAspect {
    private final int q;
    private final int r;
    private final String aspectTag;

    public MsgPlaceAspect(int q, int r, String aspectTag) {
        this.q = q;
        this.r = r;
        this.aspectTag = aspectTag != null ? aspectTag : "";
    }

    public MsgPlaceAspect(FriendlyByteBuf buf) {
        this.q = buf.readInt();
        this.r = buf.readInt();
        this.aspectTag = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(q);
        buf.writeInt(r);
        buf.writeUtf(aspectTag);
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
            if (note == null) {
                return;
            }
            if (!stageOneComplete(source)) {
                int pct = stageOnePercent(source);
                player.displayClientMessage(Component.literal(
                        "Finish Knowledge first (now " + pct + "%)"), true);
                return;
            }
            ItemStack tools = menu.getSlot(0).getItem();
            if (!HexResearchTableMenu.isInkStack(tools)
                    || tools.getDamageValue() >= tools.getMaxDamage()) {
                return;
            }
            if (aspectTag.isEmpty()) {
                Aspect removed = HexResearchGame.eraseAspect(note, q, r);
                if (removed != null) {
                    damageInk(player, menu);
                }
            } else {
                Aspect aspect;
                try {
                    aspect = Aspect.getAspect(aspectTag);
                } catch (Exception e) {
                    return;
                }
                try {
                    String cellKey = new HexUtils.Hex(q, r).toString();
                    HexResearchNoteData.HexEntry peek = note.hexEntries.get(cellKey);
                    if (peek == null || peek.type != 0 || !note.hexes.containsKey(cellKey)) {
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                if (!HexEconomy.takeAspect(player, menu, aspect)) {
                    player.displayClientMessage(
                            Component.literal("No research points: " + aspect.getName()), true);
                    return;
                }
                if (HexResearchGame.placeAspect(note, q, r, aspect)
                        != HexResearchGame.PlaceResult.OK) {
                    return;
                }
                damageInk(player, menu);
                if (HexResearchGame.checkCompletion(note, a -> HexResearchGame.isKnown(player, a))) {
                    try {
                        arcaneresearch.discovered.note.DiscoveredNoteData discovered;
                        discovered = arcaneresearch.discovered.note.DiscoveredNoteData.read(source);
                        if (discovered.key != null && discovered.key.equals(note.key)) {
                            discovered.stage2 = true;
                            arcaneresearch.discovered.note.DiscoveredNoteData.write(source, discovered);
                        }
                    } catch (Exception ignored) {
                    }
                    player.level().playSound(null, player.blockPosition(),
                            ModSounds.LEARN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            HexResearchNoteData.updateData(source, note);
            menu.broadcastChanges();
            HexTableNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgSyncNote(note));
            HexEconomy.push(player);
        });
        ctx.get().setPacketHandled(true);
    }

    static boolean stageOneComplete(ItemStack stack) {
        try {
            arcaneresearch.discovered.note.DiscoveredNoteData data =
                    arcaneresearch.discovered.note.DiscoveredNoteData.read(stack);
            if (data.key == null || data.key.isEmpty()) {
                return true;
            }
            return data.getTotalProgress() >= 1.0F;
        } catch (Exception e) {
            return false;
        }
    }

    private static int stageOnePercent(ItemStack stack) {
        try {
            return (int) (arcaneresearch.discovered.note.DiscoveredNoteData.read(stack)
                    .getTotalProgress() * 100.0F);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void damageInk(ServerPlayer player, HexResearchTableMenu menu) {
        if (menu == null) {
            return;
        }
        try {
            ItemStack tools = menu.getSlot(0).getItem();
            if (HexResearchTableMenu.isInkStack(tools)) {
                tools.hurt(1, player.getRandom(), player);
                if (tools.getDamageValue() >= tools.getMaxDamage()) {
                    tools.shrink(1);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
