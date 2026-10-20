package arcaneresearch.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;

// NEOFORGE_PORT: 1:1 from Forge .../playerdata/PacketSyncWarp.java (PLAY_TO_CLIENT).
// Same transport adaptation as PacketSyncKnowledge (see its javadoc).
// Payload id "arcaneresearch:sync_warp".
public class PacketSyncWarp implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncWarp> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "sync_warp"));

    public static final StreamCodec<ByteBuf, PacketSyncWarp> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(PacketSyncWarp::new, msg -> msg.data);

    private CompoundTag data;

    public PacketSyncWarp() {
        this.data = new CompoundTag();
    }

    private PacketSyncWarp(CompoundTag data) {
        this.data = data;
    }

    public PacketSyncWarp(Player player) {
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null) {
            this.data = warp.serializeNBT();
        } else {
            this.data = new CompoundTag();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncWarp msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
    }

    private static void handleOnClient(PacketSyncWarp msg, Player player) {
        if (player == null) return;

        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null && msg.data != null) {
            warp.deserializeNBT(msg.data);
        }
    }
}