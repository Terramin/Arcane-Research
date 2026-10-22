package arcaneresearch.discovered.scan;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.network.PacketHandler;

 









 
public class MsgStudyComplete implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgStudyComplete> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_study_complete"));

    public static final StreamCodec<ByteBuf, MsgStudyComplete> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgStudyComplete packet) {
            buf.writeByte(packet.kind);
            if (packet.kind == 1 || packet.kind == 3) {
                buf.writeLong(packet.pos);
            } else if (packet.kind == 2) {
                buf.writeInt(packet.entityId);
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.entityType == null ? "" : packet.entityType);
            }
        }

        @Override
        public MsgStudyComplete decode(ByteBuf buf) {
            MsgStudyComplete out = new MsgStudyComplete();
            out.kind = buf.readByte();
            if (out.kind == 1 || out.kind == 3) {
                out.pos = buf.readLong();
            } else if (out.kind == 2) {
                out.entityId = buf.readInt();
                out.entityType = ByteBufCodecs.STRING_UTF8.decode(buf);
            }
            return out;
        }
    };

    private byte kind;
    private long pos;
    private int entityId;
    private String entityType;

    public MsgStudyComplete() {
    }

    public MsgStudyComplete(ScanTargets.Target target) {
        if (target.kind == ScanTargets.Kind.BLOCK) {
            this.kind = 1;
            this.pos = target.pos.asLong();
        } else if (target.kind == ScanTargets.Kind.NODE) {
            this.kind = 3;
            this.pos = target.pos.asLong();
        } else if (target.kind == ScanTargets.Kind.ENTITY) {
            this.kind = 2;
            this.entityId = target.entity.getId();
            this.entityType = target.entity.getType().getDescriptionId();
        } else {
            this.kind = 0;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgStudyComplete msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            ScanTargets.Target target = validate(player, msg);
            if (target == null) {
                return;
            }
            Level level = player.level();
            String key = ScanTargets.scannedKey(level, target, player);
            if (ScanWallet.hasScanned(player, key)) {
                
                return;
            }
            AspectList aspects = ScanTargets.aspectsFor(level, target, player);
            if (aspects == null || aspects.size() <= 0) {
                player.sendSystemMessage(Component.literal("Unknown object"), true);
                return;
            }
            if (!ScanTargets.validScan(player, aspects)) {
                
                player.sendSystemMessage(
                        Component.literal("Unknown object: undiscovered components"), true);
                return;
            }
            
            
            AspectList granted = new AspectList();
            try {
                for (Aspect aspect : aspects.getAspects()) {
                    if (aspect == null) {
                        continue;
                    }
                    int amount = aspects.getAmount(aspect);
                    if (!ScanTargets.known(player, aspect)) {
                        try {
                            ArcaneresearchApi.internalMethods.progressResearch(
                                    player, "!" + aspect.getTag());
                        } catch (Exception ignored) {
                        }
                        amount += 2;
                    }
                    if (amount > 0) {
                        ScanWallet.add(player, aspect, amount);
                        granted.add(aspect, amount);
                    }
                }
            } catch (Exception ignored) {
            }
            ScanWallet.markScanned(player, key);
            if (granted.size() > 0) {
                PacketHandler.sendToPlayer(new MsgStudyReward(granted), player);
            }
            MsgWalletRequest.push(player);
        });
    }

     


 
    private static ScanTargets.Target validate(ServerPlayer player, MsgStudyComplete msg) {
        try {
            Level level = player.level();
            
            
            if (msg.kind == 1 || msg.kind == 3) {
                BlockPos blockPos = BlockPos.of(msg.pos);
                if (player.getEyePosition().distanceTo(
                        net.minecraft.world.phys.Vec3.atCenterOf(blockPos)) > ScanTargets.RANGE + 1.0D) {
                    return null;
                }
                BlockHitResult hit = ScanTargets.blockRay(level, player, ScanTargets.RANGE);
                if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(blockPos)) {
                    if (msg.kind == 3) {
                        
                        
                        try {
                            if (level.getBlockEntity(blockPos)
                                    instanceof arcaneresearch.common.lib.aura.INode) {
                                return new ScanTargets.Target(
                                        ScanTargets.Kind.NODE, blockPos, null, "");
                            }
                        } catch (Exception ignored) {
                        }
                        return null;
                    }
                    return new ScanTargets.Target(
                            ScanTargets.Kind.BLOCK, blockPos, null, "");
                }
                return null;
            }
            if (msg.kind == 2) {
                Entity entity = level.getEntity(msg.entityId);
                if (entity == null || !entity.isAlive()
                        || !entity.getType().getDescriptionId().equals(msg.entityType)) {
                    return null;
                }
                if (player.getEyePosition().distanceTo(entity.position())
                        > ScanTargets.RANGE + entity.getBbHeight() + 1.0D) {
                    return null;
                }
                return new ScanTargets.Target(
                        ScanTargets.Kind.ENTITY, null, entity, "");
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
