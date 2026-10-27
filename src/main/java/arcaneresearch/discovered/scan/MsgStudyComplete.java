package arcaneresearch.discovered.scan;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

public class MsgStudyComplete {
    private byte kind;
    private BlockPos pos;
    private int entityId;
    private String entityType;

    public MsgStudyComplete() {
    }

    public MsgStudyComplete(ScanTargets.Target target) {
        if (target == null || target.kind == null) {
            this.kind = 0;
            return;
        }
        if (target.kind == ScanTargets.Kind.BLOCK && target.pos != null) {
            this.kind = 1;
            this.pos = target.pos;
        } else if (target.kind == ScanTargets.Kind.NODE && target.pos != null) {
            this.kind = 3;
            this.pos = target.pos;
        } else if (target.kind == ScanTargets.Kind.ENTITY && target.entity != null) {
            this.kind = 2;
            this.entityId = target.entity.getId();
            this.entityType = target.entity.getType().getDescriptionId();
        } else {
            this.kind = 0;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        if (buf == null) {
            return;
        }
        if ((kind == 1 || kind == 3) && pos == null) {
            kind = 0;
        }
        if (kind == 2 && entityType == null) {
            entityType = "";
        }
        buf.writeByte(kind);
        if (kind == 1 || kind == 3) {
            buf.writeBlockPos(pos);
        } else if (kind == 2) {
            buf.writeInt(entityId);
            buf.writeUtf(entityType, 128);
        }
    }

    public MsgStudyComplete(FriendlyByteBuf buf) {
        if (buf == null) {
            kind = 0;
            return;
        }
        kind = buf.readByte();
        if (kind == 1 || kind == 3) {
            pos = buf.readBlockPos();
        } else if (kind == 2) {
            entityId = buf.readInt();
            entityType = buf.readUtf(128);
        } else {
            kind = 0;
        }
    }

    public static void handle(MsgStudyComplete msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx == null ? null : ctx.get();
        if (context == null || msg == null) {
            return;
        }
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ScanTargets.Target target = validate(player, msg);
            if (target == null) {
                return;
            }
            Level level = player.serverLevel();
            String key = ScanTargets.scannedKey(level, target, player);
            if (key.isEmpty()) {
                player.displayClientMessage(Component.literal("Unknown object"), true);
                return;
            }
            if (ScanWallet.hasScanned(player, key)) {
                return;
            }
            AspectList aspects = ScanTargets.aspectsFor(level, target, player);
            if (aspects == null || aspects.aspects.isEmpty()) {
                player.displayClientMessage(Component.literal("Unknown object"), true);
                return;
            }
            if (!ScanTargets.validScan(player, aspects)) {
                player.displayClientMessage(
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
            if (!granted.aspects.isEmpty()) {
                try {
                    ScanNetwork.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                            new MsgStudyReward(granted));
                } catch (Exception ignored) {
                }
            }
            MsgWalletRequest.push(player);
        });
        context.setPacketHandled(true);
    }

    private static ScanTargets.Target validate(ServerPlayer player, MsgStudyComplete msg) {
        if (player == null || msg == null) {
            return null;
        }
        try {
            Level level = player.serverLevel();
            if (msg.kind == 1 && msg.pos != null) {
                if (player.getEyePosition().distanceTo(
                        net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > ScanTargets.RANGE + 1.0D) {
                    return null;
                }
                BlockHitResult hit = ScanTargets.blockRay(level, player, ScanTargets.RANGE);
                if (hit.getType() == HitResult.Type.BLOCK && msg.pos.equals(hit.getBlockPos())) {
                    return new ScanTargets.Target(
                            ScanTargets.Kind.BLOCK, msg.pos, null, "");
                }
                return null;
            }
            if (msg.kind == 3 && msg.pos != null) {
                if (player.getEyePosition().distanceTo(
                        net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > ScanTargets.RANGE + 1.0D) {
                    return null;
                }
                BlockHitResult hit = ScanTargets.blockRay(level, player, ScanTargets.RANGE);
                if (hit.getType() == HitResult.Type.BLOCK && msg.pos.equals(hit.getBlockPos())) {
                    try {
                        if (level.getBlockEntity(msg.pos)
                                instanceof arcaneresearch.common.lib.aura.INode) {
                            return new ScanTargets.Target(
                                    ScanTargets.Kind.NODE, msg.pos, null, "");
                        }
                    } catch (Exception ignored) {
                    }
                }
                return null;
            }
            if (msg.kind == 2) {
                Entity entity = level.getEntity(msg.entityId);
                if (entity == null || !entity.isAlive()) {
                    return null;
                }
                if (msg.entityType == null
                        || !msg.entityType.equals(entity.getType().getDescriptionId())) {
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