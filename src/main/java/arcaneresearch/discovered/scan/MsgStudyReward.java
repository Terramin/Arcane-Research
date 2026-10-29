package arcaneresearch.discovered.scan;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import org.jspecify.annotations.NonNull;

public class MsgStudyReward implements CustomPacketPayload {

    private static final int MAX_ENTRIES = 1024;
    private static final int MAX_KEY_LEN = 64;

    public static final CustomPacketPayload.Type<MsgStudyReward> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_study_reward"));

    public static final StreamCodec<ByteBuf, MsgStudyReward> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull MsgStudyReward packet) {
            int n = Math.min(packet.tags.size(), packet.amounts.size());
            List<Integer> validIndexes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String tag = packet.tags.get(i);
                Integer amount = packet.amounts.get(i);
                if (tag != null && amount != null && amount > 0) {
                    validIndexes.add(i);
                }
            }
            ByteBufCodecs.VAR_INT.encode(buf, validIndexes.size());
            for (int idx : validIndexes) {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.tags.get(idx));
                ByteBufCodecs.VAR_INT.encode(buf, packet.amounts.get(idx));
            }
        }

        @Override
        public @NonNull MsgStudyReward decode(@NonNull ByteBuf buf) {
            MsgStudyReward out = new MsgStudyReward();
            int n = ByteBufCodecs.VAR_INT.decode(buf);
            if (n < 0 || n > MAX_ENTRIES) {
                n = 0;
            }
            for (int i = 0; i < n; i++) {
                String tag = ByteBufCodecs.STRING_UTF8.decode(buf);
                int amount = ByteBufCodecs.VAR_INT.decode(buf);
                if (amount > 0) {
                    out.tags.add(tag);
                    out.amounts.add(amount);
                }
            }
            return out;
        }
    };

    private final List<String> tags = new ArrayList<>();
    private final List<Integer> amounts = new ArrayList<>();

    public MsgStudyReward() {
    }

    public MsgStudyReward(AspectList granted) {
        if (granted == null) {
            return;
        }
        Aspect[] aspects = granted.getAspects();
        if (aspects == null) {
            return;
        }
        for (Aspect aspect : aspects) {
            if (aspect == null) {
                continue;
            }
            String tag = aspect.getTag();
            if (tag == null) {
                continue;
            }
            int amount = granted.getAmount(aspect);
            if (amount > 0) {
                tags.add(tag);
                amounts.add(amount);
            }
        }
    }

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgStudyReward msg, IPayloadContext ctx) {
        if (ctx == null) {
            return;
        }
        ctx.enqueueWork(() -> notifyClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void notifyClient(MsgStudyReward msg) {
        if (msg == null) {
            return;
        }
        AspectList granted = new AspectList();
        int n = Math.min(msg.tags.size(), msg.amounts.size());
        for (int i = 0; i < n; i++) {
            String tag = msg.tags.get(i);
            Integer amount = msg.amounts.get(i);
            if (tag == null || amount == null || amount <= 0) {
                continue;
            }
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect != null) {
                granted.add(aspect, amount);
            }
        }
        if (!granted.aspects.isEmpty()) {
            ScanClient.notifyReward(granted);
        }
    }
}