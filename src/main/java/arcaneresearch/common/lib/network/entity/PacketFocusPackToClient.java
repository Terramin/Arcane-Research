package arcaneresearch.common.lib.network.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.common.entities.projectile.EntityFocusCloud;
import arcaneresearch.common.entities.projectile.EntityFocusMine;
import arcaneresearch.common.entities.projectile.EntityFocusProjectile;











public class PacketFocusPackToClient implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFocusPackToClient> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_pack_to_client"));

    public static final StreamCodec<ByteBuf, PacketFocusPackToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.entityId,
            ByteBufCodecs.COMPOUND_TAG, p -> p.nbt,
            PacketFocusPackToClient::new);

    private int entityId;
    private CompoundTag nbt;

    public PacketFocusPackToClient(int entityId, CompoundTag nbt) {
        this.entityId = entityId;
        this.nbt = nbt != null ? nbt : new CompoundTag();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusPackToClient msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
    }

    private static void handleOnClient(PacketFocusPackToClient msg, Player player) {
        if (player == null) return;
        Level world = player.level();
        if (world == null) return;
        if (msg.nbt == null || msg.nbt.isEmpty()) return;
        Entity entity = world.getEntity(msg.entityId);
        if (entity == null) return;
        try {
            FocusPackage pack = new FocusPackage();
            pack.deserialize(msg.nbt);
            pack.world = world;
            if (entity instanceof EntityFocusProjectile projectile) {
                projectile.setFocusPackage(pack);
            } else if (entity instanceof EntityFocusCloud cloud) {
                cloud.setFocusPackage(pack);
            } else if (entity instanceof EntityFocusMine mine) {
                mine.setFocusPackage(pack);
            }
        } catch (Exception ignored) {
        }
    }
}
