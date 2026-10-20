package arcaneresearch.discovered.table;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.init.ModSounds;

/**
 * Client -&gt; Server: поставить (tag) или стереть ("") аспект на гексе.
 * Порт {@code PacketAspectPlaceToServer} (там были q/r байтами и XYZ тайла;
 * здесь XYZ нет — записка берётся из главной руки, привязка к тайлу стола —
 * вопрос отдельно).
 *
 * NEOFORGE_PORT: SimpleChannel(HexTableNetwork) -&gt; CustomPacketPayload
 * (ручной StreamCodec: q/r int, тег строкой). ItemStack.hurt(int,
 * RandomSource, ServerPlayer) удалён -&gt; ручной износ инструментов
 * (setDamageValue + shrink при пределе — то же наблюдаемое состояние).
 * Payload id "arcaneresearch:hex_place_aspect".
 */
public class MsgPlaceAspect implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgPlaceAspect> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_place_aspect"));

    public static final StreamCodec<ByteBuf, MsgPlaceAspect> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgPlaceAspect packet) {
            buf.writeInt(packet.q);
            buf.writeInt(packet.r);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.aspectTag);
        }

        @Override
        public MsgPlaceAspect decode(ByteBuf buf) {
            return new MsgPlaceAspect(buf.readInt(), buf.readInt(),
                    ByteBufCodecs.STRING_UTF8.decode(buf));
        }
    };

    private final int q;
    private final int r;
    private final String aspectTag;

    public MsgPlaceAspect(int q, int r, String aspectTag) {
        this.q = q;
        this.r = r;
        this.aspectTag = aspectTag != null ? aspectTag : "";
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgPlaceAspect msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            // Источник записки — только слот открытого меню (игры из руки нет).
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
            // Порядок стадий строгий: поле трогаем только при stage-1
            // (мини-игра стола 1) на 100%. Чисто hex-записки без проектного
            // слоя (демо) — без гейта.
            if (!stageOneComplete(source)) {
                int pct = stageOnePercent(source);
                player.sendSystemMessage(Component.literal(
                        "Finish Knowledge first (now " + pct + "%)"), true);
                return;
            }
            // Паритет consumeInkFromTable(check): без годных чернил ход запрещён.
            ItemStack tools = menu.getSlot(0).getItem();
            if (!HexResearchTableMenu.isInkStack(tools)
                    || tools.getDamageValue() >= tools.getMaxDamage()) {
                return;
            }
            if (msg.aspectTag.isEmpty()) {
                // Стирание: ячейка чистится, возврата в пул НЕТ (паритет:
                // рефанд только с перками), чернила тратятся всегда.
                Aspect removed = HexResearchGame.eraseAspect(note, msg.q, msg.r);
                if (removed != null) {
                    damageInk(player, menu);
                }
            } else {
                Aspect aspect;
                try {
                    aspect = Aspect.getAspect(msg.aspectTag);
                } catch (Exception e) {
                    return;
                }
                // Списание: кошелёк, затем бонус тайла (паритет placeAspect).
                // Занятость — до списания, иначе брали бы плату за отказ.
                try {
                    String cellKey = new HexUtils.Hex(msg.q, msg.r).toString();
                    HexResearchNoteData.HexEntry peek = note.hexEntries.get(cellKey);
                    if (peek == null || peek.type != 0 || !note.hexes.containsKey(cellKey)) {
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                if (!HexEconomy.takeAspect(player, menu, aspect)) {
                    player.sendSystemMessage(
                            Component.literal("No research points: " + aspect.getName()), true);
                    return;
                }
                if (HexResearchGame.placeAspect(note, msg.q, msg.r, aspect)
                        != HexResearchGame.PlaceResult.OK) {
                    return;
                }
                damageInk(player, menu);
                // Победа: флаг hex в проектной записке + ровно одна стадия, если
                // текущая — hex. have/need НЕ трогаем: stage-1 проходит своей
                // мини-игрой, иначе она пропускается (бывший баг).
                if (HexResearchGame.checkCompletion(note, a -> HexResearchGame.isKnown(player, a))) {
                    try {
                        arcaneresearch.discovered.note.DiscoveredNoteData discovered =
                                arcaneresearch.discovered.note.DiscoveredNoteData.read(source);
                        if (discovered.key != null && discovered.key.equals(note.key)) {
                            discovered.stage2 = true;
                            arcaneresearch.discovered.note.DiscoveredNoteData.write(source, discovered);
                        }
                    } catch (Exception ignored) {
                    }
                    // Победа стадию исследования НЕ двигает: завершение —
                    // только сдачей открытия (tryCompleteFromNote), как в проекте.
                    player.level().playSound(null, player.blockPosition(),
                            ModSounds.LEARN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            HexResearchNoteData.updateData(source, note);
            menu.broadcastChanges();
            PacketHandler.sendToPlayer(new MsgSyncNote(note), player);
            HexEconomy.push(player);
        });
    }

    /**
     * Stage-1 (стол 1) завершена на 100%: прогресс have/need проектной
     * записки. Без проектного ключа (демо) — всегда true.
     */
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

    /**
     * Порча инструментов писца (слот 0) после хода — паритет
     * consumeInkFromTable(doit). Вызывается только при наличии чернил
     * (проверка выше уже запретила ход без них).
     */
    private static void damageInk(ServerPlayer player, HexResearchTableMenu menu) {
        if (menu == null) {
            return;
        }
        try {
            ItemStack tools = menu.getSlot(0).getItem();
            if (HexResearchTableMenu.isInkStack(tools)) {
                // NEOFORGE_PORT (26.3): ItemStack.hurt(int, RandomSource, ServerPlayer)
                // removed. Replicate its body: isDamageableItem gate, Unbreaking
                // (EnchantmentHelper.processDurabilityChange), durability criterion,
                // setDamageValue. hurtAndBreak is not used: it skips creative players,
                // which the reference hurt() did not.
                if (tools.isDamageableItem()) {
                    net.minecraft.server.level.ServerLevel serverLevel = player.level();
                    int amount = net.minecraft.world.item.enchantment.EnchantmentHelper
                            .processDurabilityChange(serverLevel, tools, 1);
                    if (amount > 0) {
                        int newDamage = tools.getDamageValue() + amount;
                        net.minecraft.advancements.triggers.CriteriaTriggers.ITEM_DURABILITY_CHANGED
                                .trigger(player, tools, newDamage);
                        tools.setDamageValue(newDamage);
                    }
                }
                if (tools.getDamageValue() >= tools.getMaxDamage()) {
                    tools.shrink(1);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
