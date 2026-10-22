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

 







 
public class MsgStudyReward implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgStudyReward> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_study_reward"));

    public static final StreamCodec<ByteBuf, MsgStudyReward> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgStudyReward packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.tags.size());
            for (int i = 0; i < packet.tags.size(); i++) {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.tags.get(i));
                ByteBufCodecs.VAR_INT.encode(buf, packet.amounts.get(i));
            }
        }

        @Override
        public MsgStudyReward decode(ByteBuf buf) {
            MsgStudyReward out = new MsgStudyReward();
            int n = ByteBufCodecs.VAR_INT.decode(buf);
            for (int i = 0; i < n; i++) {
                out.tags.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                out.amounts.add(ByteBufCodecs.VAR_INT.decode(buf));
            }
            return out;
        }
    };

    private final List<String> tags = new ArrayList<>();
    private final List<Integer> amounts = new ArrayList<>();

    public MsgStudyReward() {
    }

    public MsgStudyReward(AspectList granted) {
        for (Aspect aspect : granted.getAspects()) {
            if (aspect == null) {
                continue;
            }
            tags.add(aspect.getTag());
            amounts.add(granted.getAmount(aspect));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgStudyReward msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> notifyClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void notifyClient(MsgStudyReward msg) {
        AspectList granted = new AspectList();
        for (int i = 0; i < msg.tags.size(); i++) {
            Aspect aspect = Aspect.getAspect(msg.tags.get(i));
            if (aspect != null && msg.amounts.get(i) > 0) {
                granted.add(aspect, msg.amounts.get(i));
            }
        }
        if (granted.size() > 0) {
            ScanClient.notifyReward(granted);
        }
    }
}
