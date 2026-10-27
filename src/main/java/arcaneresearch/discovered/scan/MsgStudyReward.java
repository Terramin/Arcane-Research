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
    private static final int MAX_ENTRIES = 1024;
    private static final int MAX_KEY_LEN = 64;

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

    public void encode(FriendlyByteBuf buf) {
        if (buf == null) {
            return;
        }
        int n = Math.min(tags.size(), amounts.size());
        buf.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            String tag = tags.get(i);
            Integer amount = amounts.get(i);
            if (tag == null || amount == null) {
                continue;
            }
            buf.writeUtf(tag, MAX_KEY_LEN);
            buf.writeVarInt(amount);
        }
    }

    public MsgStudyReward(FriendlyByteBuf buf) {
        if (buf == null) {
            return;
        }
        int n = buf.readVarInt();
        if (n < 0 || n > MAX_ENTRIES) {
            n = 0;
        }
        for (int i = 0; i < n; i++) {
            String tag = buf.readUtf(MAX_KEY_LEN);
            int amount = buf.readVarInt();
            tags.add(tag);
            amounts.add(amount);
        }
    }

    public static void handle(MsgStudyReward msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx == null ? null : ctx.get();
        if (context == null) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                return;
            }
            notifyClient(msg);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void notifyClient(MsgStudyReward msg) {
        if (msg == null) {
            return;
        }
        int n = Math.min(msg.tags.size(), msg.amounts.size());
        AspectList granted = new AspectList();
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