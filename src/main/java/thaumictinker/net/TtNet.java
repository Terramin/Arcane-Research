package thaumictinker.net;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import thaumictinker.enchanting.EnchanterMenu;

 



 
public final class TtNet {

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("thaumictinker", "main"),
            () -> "1",
            version -> true,
            version -> true);

    private static boolean registered;

    private TtNet() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        int index = 0;
        CHANNEL.registerMessage(index++, FociEffectPayload.class, FociEffectPayload::encode, FociEffectPayload::decode,
                (BiConsumer<FociEffectPayload, Supplier<NetworkEvent.Context>>) TtNet::handle);
        CHANNEL.registerMessage(index++, EnchanterEditPayload.class, EnchanterEditPayload::encode, EnchanterEditPayload::decode,
                (BiConsumer<EnchanterEditPayload, Supplier<NetworkEvent.Context>>) TtNet::handleEnchanterEdit);
        CHANNEL.registerMessage(index++, EnchanterStartPayload.class, EnchanterStartPayload::encode, EnchanterStartPayload::decode,
                (BiConsumer<EnchanterStartPayload, Supplier<NetworkEvent.Context>>) TtNet::handleEnchanterStart);
        CHANNEL.registerMessage(index++, CropFxPayload.class, CropFxPayload::encode, CropFxPayload::decode,
                (BiConsumer<CropFxPayload, Supplier<NetworkEvent.Context>>) TtNet::handleCropFx);
        CHANNEL.registerMessage(++index, ElementsFxPayload.class, ElementsFxPayload::encode, ElementsFxPayload::decode,
                (BiConsumer<ElementsFxPayload, Supplier<NetworkEvent.Context>>) TtNet::handleElementFx);
        CHANNEL.registerMessage(++index, MobMagnetButtonPayload.class, MobMagnetButtonPayload::encode,
                MobMagnetButtonPayload::decode, (BiConsumer<MobMagnetButtonPayload, Supplier<NetworkEvent.Context>>) TtNet::handleMobMagnetButton);
        CHANNEL.registerMessage(++index, TabletButtonPayload.class, TabletButtonPayload::encode,
                TabletButtonPayload::decode, (BiConsumer<TabletButtonPayload, Supplier<NetworkEvent.Context>>) TtNet::handleTabletButton);
        CHANNEL.registerMessage(++index, WarpGateButtonPayload.class, WarpGateButtonPayload::encode,
                WarpGateButtonPayload::decode, (BiConsumer<WarpGateButtonPayload, Supplier<NetworkEvent.Context>>) TtNet::handleWarpGateButton);
        CHANNEL.registerMessage(++index, WarpGateTeleportPayload.class, WarpGateTeleportPayload::encode,
                WarpGateTeleportPayload::decode, (BiConsumer<WarpGateTeleportPayload, Supplier<NetworkEvent.Context>>) TtNet::handleWarpGateTeleport);
        CHANNEL.registerMessage(++index, ShadowbeamPayload.class, ShadowbeamPayload::encode,
                ShadowbeamPayload::decode, (BiConsumer<ShadowbeamPayload, Supplier<NetworkEvent.Context>>) TtNet::handleShadowbeam);
        CHANNEL.registerMessage(++index, ToggleArmorPayload.class, ToggleArmorPayload::encode,
                ToggleArmorPayload::decode, (BiConsumer<ToggleArmorPayload, Supplier<NetworkEvent.Context>>) TtNet::handleToggleArmor);
        CHANNEL.registerMessage(++index, ArmorStatusPayload.class, ArmorStatusPayload::encode,
                ArmorStatusPayload::decode, (BiConsumer<ArmorStatusPayload, Supplier<NetworkEvent.Context>>) TtNet::handleArmorStatus);
        CHANNEL.registerMessage(++index, SoulHeartsPayload.class, SoulHeartsPayload::encode,
                SoulHeartsPayload::decode, (BiConsumer<SoulHeartsPayload, Supplier<NetworkEvent.Context>>) TtNet::handleSoulHearts);
    }

      
    public static final class ToggleArmorPayload {
        public final boolean enabled;

        public ToggleArmorPayload(boolean enabled) {
            this.enabled = enabled;
        }

        public static void encode(ToggleArmorPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBoolean(payload.enabled);
        }

        public static ToggleArmorPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new ToggleArmorPayload(buf.readBoolean());
        }
    }

      
    public static final class ArmorStatusPayload {
        public final boolean enabled;

        public ArmorStatusPayload(boolean enabled) {
            this.enabled = enabled;
        }

        public static void encode(ArmorStatusPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBoolean(payload.enabled);
        }

        public static ArmorStatusPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new ArmorStatusPayload(buf.readBoolean());
        }
    }

      
    public static final class SoulHeartsPayload {
        public final int hearts;

        public SoulHeartsPayload(int hearts) {
            this.hearts = hearts;
        }

        public static void encode(SoulHeartsPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeVarInt(payload.hearts);
        }

        public static SoulHeartsPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new SoulHeartsPayload(buf.readVarInt());
        }
    }

    public static void sendSoulHearts(ServerPlayer player, int hearts) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SoulHeartsPayload(hearts));
    }

    public static void sendToggleArmor(boolean enabled) {
        CHANNEL.sendToServer(new ToggleArmorPayload(enabled));
    }

    private static void handleToggleArmor(ToggleArmorPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (!(sender instanceof ServerPlayer serverPlayer)
                    || !thaumictinker.awakenedarmor.AwakenedArmor.wearsAny(serverPlayer)) {
                return;
            }
            if (thaumictinker.awakenedarmor.AwakenedArmor.isEnabled(serverPlayer) == payload.enabled) {
                return;
            }
            thaumictinker.awakenedarmor.AwakenedArmor.setEnabled(serverPlayer, payload.enabled);
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ArmorStatusPayload(payload.enabled));
        });
        ctx.setPacketHandled(true);
    }

    private static void handleArmorStatus(ArmorStatusPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }
        ctx.enqueueWork(() -> ClientEffects.armorStatus(payload.enabled));
        ctx.setPacketHandled(true);
    }

    private static void handleSoulHearts(SoulHeartsPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }
        ctx.enqueueWork(() -> thaumictinker.kami.tool.SoulHeartHandler.applyClientHearts(payload.hearts));
        ctx.setPacketHandled(true);
    }

      
    public static final class CropFxPayload {
        public final net.minecraft.core.BlockPos from;
        public final net.minecraft.core.BlockPos to;
        public final int color;

        public CropFxPayload(net.minecraft.core.BlockPos from, net.minecraft.core.BlockPos to, int color) {
            this.from = from;
            this.to = to;
            this.color = color;
        }

        public static void encode(CropFxPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBlockPos(payload.from);
            buf.writeBlockPos(payload.to);
            buf.writeInt(payload.color);
        }

        public static CropFxPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new CropFxPayload(buf.readBlockPos(), buf.readBlockPos(), buf.readInt());
        }
    }

    public static CropFxPayload cropFx(net.minecraft.core.BlockPos from, net.minecraft.core.BlockPos to, int color) {
        return new CropFxPayload(from, to, color);
    }

      
    public static final class ElementsFxPayload {
        public static final int SPARKLE = 0;
        public static final int BURST = 1;
        public static final int WISPS = 2;

        public final int kind;
        public final double x;
        public final double y;
        public final double z;
        public final float size;

        public ElementsFxPayload(int kind, double x, double y, double z, float size) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        public static void encode(ElementsFxPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeByte(payload.kind);
            buf.writeDouble(payload.x);
            buf.writeDouble(payload.y);
            buf.writeDouble(payload.z);
            buf.writeFloat(payload.size);
        }

        public static ElementsFxPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new ElementsFxPayload(buf.readByte(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat());
        }
    }

    public static ElementsFxPayload elementFx(double x, double y, double z, int kind, float size) {
        return new ElementsFxPayload(kind, x, y, z, size);
    }

    public static void sendToNear(net.minecraft.world.level.Level level, double x, double y, double z,
                                  double range, ElementsFxPayload payload) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                    x, y, z, range, serverLevel.dimension())), payload);
        }
    }

      
    public static final class MobMagnetButtonPayload {
        public final boolean adult;

        public MobMagnetButtonPayload(boolean adult) {
            this.adult = adult;
        }

        public static void encode(MobMagnetButtonPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBoolean(payload.adult);
        }

        public static MobMagnetButtonPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new MobMagnetButtonPayload(buf.readBoolean());
        }
    }

    public static void sendMobMagnetButton(boolean adult) {
        CHANNEL.sendToServer(new MobMagnetButtonPayload(adult));
    }

    private static void handleMobMagnetButton(MobMagnetButtonPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender != null && sender.containerMenu instanceof thaumictinker.attractor.CorporealAttractorMenu menu) {
                menu.setAdult(payload.adult);
            }
        });
        ctx.setPacketHandled(true);
    }

      
    public static final class TabletButtonPayload {
        public final boolean leftClick;
        public final boolean redstone;

        public TabletButtonPayload(boolean leftClick, boolean redstone) {
            this.leftClick = leftClick;
            this.redstone = redstone;
        }

        public static void encode(TabletButtonPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBoolean(payload.leftClick);
            buf.writeBoolean(payload.redstone);
        }

        public static TabletButtonPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new TabletButtonPayload(buf.readBoolean(), buf.readBoolean());
        }
    }

    public static void sendTabletButton(boolean leftClick, boolean redstone) {
        CHANNEL.sendToServer(new TabletButtonPayload(leftClick, redstone));
    }

    private static void handleTabletButton(TabletButtonPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender != null && sender.containerMenu instanceof thaumictinker.tablet.DynamismTabletMenu menu) {
                menu.setModes(payload.leftClick, payload.redstone);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleElementFx(ElementsFxPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }
        ctx.enqueueWork(() -> ClientEffects.elementFx(payload));
        ctx.setPacketHandled(true);
    }

    public static void sendToTrackingChunk(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
                                           CropFxPayload payload) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.level.chunk.LevelChunk chunk =
                    serverLevel.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), payload);
        }
    }

    private static void handleCropFx(CropFxPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }
        ctx.enqueueWork(() -> ClientEffects.cropFx(payload));
        ctx.setPacketHandled(true);
    }

      
    public static final class EnchanterEditPayload {
        public final int bx;
        public final int by;
        public final int bz;
        public final String enchantment;
        public final int level;

        public EnchanterEditPayload(int bx, int by, int bz, String enchantment, int level) {
            this.bx = bx;
            this.by = by;
            this.bz = bz;
            this.enchantment = enchantment;
            this.level = level;
        }

        public static void encode(EnchanterEditPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeInt(payload.bx);
            buf.writeInt(payload.by);
            buf.writeInt(payload.bz);
            buf.writeUtf(payload.enchantment);
            buf.writeInt(payload.level);
        }

        public static EnchanterEditPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new EnchanterEditPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(), buf.readInt());
        }
    }

      
    public static final class EnchanterStartPayload {
        public final int bx;
        public final int by;
        public final int bz;

        public EnchanterStartPayload(int bx, int by, int bz) {
            this.bx = bx;
            this.by = by;
            this.bz = bz;
        }

        public static void encode(EnchanterStartPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeInt(payload.bx);
            buf.writeInt(payload.by);
            buf.writeInt(payload.bz);
        }

        public static EnchanterStartPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new EnchanterStartPayload(buf.readInt(), buf.readInt(), buf.readInt());
        }
    }

    private static void handleEnchanterEdit(EnchanterEditPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(payload.bx, payload.by, payload.bz);
            EnchanterMenu menu = sender.containerMenu instanceof EnchanterMenu enchanterMenu
                    && enchanterMenu.pos().equals(pos)
                    && enchanterMenu.stillValid(sender)
                    && enchanterMenu.enchanter() != null
                    ? enchanterMenu
                    : null;
            net.minecraft.world.item.enchantment.Enchantment enchantment = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS
                    .getValue(thaumictinker.TtCore.id(payload.enchantment));
            if (menu != null && enchantment != null) {
                menu.enchanter().applyEdit(sender, enchantment, payload.level);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleEnchanterStart(EnchanterStartPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            if (sender.containerMenu instanceof EnchanterMenu menu && menu.stillValid(sender) && menu.enchanter() != null) {
                menu.enchanter().startWorking();
            }
        });
        ctx.setPacketHandled(true);
    }

      
    public static final class FociEffectPayload {
        public final int kind;
        public final int playerId;
        public final double px;
        public final double py;
        public final double pz;
        public final int bx;
        public final int by;
        public final int bz;

        public FociEffectPayload(int kind, int playerId, double px, double py, double pz, int bx, int by, int bz) {
            this.kind = kind;
            this.playerId = playerId;
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.bx = bx;
            this.by = by;
            this.bz = bz;
        }

        public static void encode(FociEffectPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeInt(payload.kind);
            buf.writeInt(payload.playerId);
            buf.writeDouble(payload.px);
            buf.writeDouble(payload.py);
            buf.writeDouble(payload.pz);
            buf.writeInt(payload.bx);
            buf.writeInt(payload.by);
            buf.writeInt(payload.bz);
        }

        public static FociEffectPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new FociEffectPayload(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(), buf.readInt(), buf.readInt());
        }
    }

    public static void sendToTrackingAndSelf(Entity entity, FociEffectPayload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }

      
    public static void sendEnchanterEdit(net.minecraft.core.BlockPos pos, String enchantment, int level) {
        CHANNEL.sendToServer(new EnchanterEditPayload(pos.getX(), pos.getY(), pos.getZ(), enchantment, level));
    }

      
    public static void sendEnchanterStart(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new EnchanterStartPayload(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static void handle(FociEffectPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() != NetworkDirection.PLAY_TO_SERVER) {
                ClientEffects.handle(payload);
            }
        });
        ctx.setPacketHandled(true);
    }

      
    public static final class WarpGateButtonPayload {
        public final boolean locked;

        public WarpGateButtonPayload(boolean locked) {
            this.locked = locked;
        }

        public static void encode(WarpGateButtonPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBoolean(payload.locked);
        }

        public static WarpGateButtonPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new WarpGateButtonPayload(buf.readBoolean());
        }
    }

    public static void sendWarpGateButton(boolean locked) {
        CHANNEL.sendToServer(new WarpGateButtonPayload(locked));
    }

    private static void handleWarpGateButton(WarpGateButtonPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender != null && sender.containerMenu
                    instanceof thaumictinker.kami.gateway.WarpGateMenu menu) {
                menu.setLocked(payload.locked);
            }
        });
        ctx.setPacketHandled(true);
    }

      
    public static final class WarpGateTeleportPayload {
        public final net.minecraft.core.BlockPos gate;
        public final int index;

        public WarpGateTeleportPayload(net.minecraft.core.BlockPos gate, int index) {
            this.gate = gate;
            this.index = index;
        }

        public static void encode(WarpGateTeleportPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeBlockPos(payload.gate);
            buf.writeVarInt(payload.index);
        }

        public static WarpGateTeleportPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            return new WarpGateTeleportPayload(buf.readBlockPos(), buf.readVarInt());
        }
    }

    public static void sendWarpGateTeleport(net.minecraft.core.BlockPos gate, int index) {
        CHANNEL.sendToServer(new WarpGateTeleportPayload(gate, index));
    }

    private static void handleWarpGateTeleport(WarpGateTeleportPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (!(sender instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
                return;
            }
            net.minecraft.world.level.Level level = serverPlayer.level();
            if (!level.isLoaded(payload.gate)) {
                return;
            }
            if (level.getBlockEntity(payload.gate)
                    instanceof thaumictinker.kami.gateway.WarpGateBlockEntity gate) {
                gate.teleportPlayer(serverPlayer, payload.index);
            }
        });
        ctx.setPacketHandled(true);
    }

      
    public static final class ShadowbeamPayload {
        public static final int MAX_POINTS = thaumictinker.kami.foci.ShadowbeamFocus.Beam.MAX_POINTS;

        public final List<Vec3f> path;

          
        public record Vec3f(double x, double y, double z) {
        }

        public ShadowbeamPayload(List<Vec3f> path) {
            this.path = path;
        }

        public static void encode(ShadowbeamPayload payload, net.minecraft.network.FriendlyByteBuf buf) {
            buf.writeVarInt(payload.path.size());
            for (Vec3f point : payload.path) {
                buf.writeDouble(point.x());
                buf.writeDouble(point.y());
                buf.writeDouble(point.z());
            }
        }

        public static ShadowbeamPayload decode(net.minecraft.network.FriendlyByteBuf buf) {
            int count = Math.min(buf.readVarInt(), MAX_POINTS);
            List<Vec3f> path = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                path.add(new Vec3f(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            }
            return new ShadowbeamPayload(path);
        }
    }

    public static void sendShadowbeam(net.minecraft.server.level.ServerLevel level, double x, double y, double z,
                                      double range, List<Vec3> path) {
        List<ShadowbeamPayload.Vec3f> points = new ArrayList<>();
        for (Vec3 point : path) {
            if (points.size() >= ShadowbeamPayload.MAX_POINTS) {
                break;
            }
            points.add(new ShadowbeamPayload.Vec3f(point.x, point.y, point.z));
        }
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                x, y, z, range, level.dimension())), new ShadowbeamPayload(points));
    }

    private static void handleShadowbeam(ShadowbeamPayload payload, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }
        ctx.enqueueWork(() -> ClientEffects.shadowbeam(payload));
        ctx.setPacketHandled(true);
    }
}
