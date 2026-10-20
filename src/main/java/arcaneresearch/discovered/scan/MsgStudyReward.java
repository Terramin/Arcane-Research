package arcaneresearch.discovered.scan;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
public class MsgStudyReward {
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
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            buf.writeUtf(tags.get(i), 64);
            buf.writeVarInt(amounts.get(i));
        }
    }
    public MsgStudyReward(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            tags.add(buf.readUtf(64));
            amounts.add(buf.readVarInt());
        }
    }
    public static void handle(MsgStudyReward msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            notifyClient(msg);
        });
        ctx.get().setPacketHandled(true);
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
