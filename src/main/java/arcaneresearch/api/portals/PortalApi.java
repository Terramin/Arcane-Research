package arcaneresearch.api.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import arcaneresearch.api.items.IPortalDestination;
import arcaneresearch.init.ModEntities;

import java.util.ArrayList;
import java.util.List;

public final class PortalApi {

    public static final String TAG_X = "Pos_X";
    public static final String TAG_Y = "Pos_Y";
    public static final String TAG_Z = "Pos_Z";
    public static final String TAG_DIM = "Dim";
    public static final String TAG_TIMER = "Timer";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";

    private PortalApi() {
    }

    public static boolean hasSavedPosition(ItemStack stack) {
        return stack != null && stack.getItem() instanceof IPortalDestination dest && dest.hasSavedPosition(stack);
    }

    @Nullable
    public static PortalDestination getSavedDestination(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IPortalDestination dest)) {
            return null;
        }
        return dest.getSavedDestination(stack);
    }

    public static boolean bindPosition(ItemStack stack, Level level, BlockPos pos) {
        if (stack == null || level == null || pos == null) {
            return false;
        }
        if (!(stack.getItem() instanceof IPortalDestination dest)) {
            return false;
        }
        return dest.bindPosition(stack, level, pos);
    }

    public static void writeDestination(CompoundTag tag, BlockPos pos, ResourceKey<Level> dimension,
                                        float width, float height) {
        if (tag == null || pos == null) {
            return;
        }
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIM, (dimension == null ? Level.OVERWORLD : dimension).identifier().toString());
        tag.putFloat(TAG_WIDTH, width);
        tag.putFloat(TAG_HEIGHT, height);
    }

    @Nullable
    public static PortalDestination readDestination(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Y) || !tag.contains(TAG_Z)) {
            return null;
        }
        BlockPos pos = new BlockPos(tag.getIntOr(TAG_X, 0), tag.getIntOr(TAG_Y, 0), tag.getIntOr(TAG_Z, 0));
        ResourceKey<Level> dimension = Level.OVERWORLD;
        if (tag.contains(TAG_DIM)) {
            Identifier parsed = parseResourceLocation(tag.getStringOr(TAG_DIM, ""));
            if (parsed != null) {
                dimension = ResourceKey.create(Registries.DIMENSION, parsed);
            }
        }
        float width = tag.contains(TAG_WIDTH) ? tag.getFloatOr(TAG_WIDTH, PortalDestination.DEFAULT_WIDTH) : PortalDestination.DEFAULT_WIDTH;
        float height = tag.contains(TAG_HEIGHT) ? tag.getFloatOr(TAG_HEIGHT, PortalDestination.DEFAULT_HEIGHT) : PortalDestination.DEFAULT_HEIGHT;
        return new PortalDestination(pos, dimension, width, height);
    }

    @Nullable
    public static Identifier parseResourceLocation(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        int sep = value.indexOf(':');
        if (sep >= 0) {
            return Identifier.fromNamespaceAndPath(value.substring(0, sep), value.substring(sep + 1));
        }
        return Identifier.fromNamespaceAndPath("minecraft", value);
    }

    @Nullable
    public static IPortal createPortal(Level level, BlockPos pos, float yaw, PortalDestination destination) {
        if (level == null || pos == null || destination == null || !destination.isValid()) {
            return null;
        }
        Entity entity = new arcaneresearch.common.entities.EntityPortal(ModEntities.PORTAL.get(), level);
        if (!(entity instanceof IPortal portal)) {
            return null;
        }
        entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        portal.setSize(destination.width(), destination.height());
        portal.setTeleportTarget(destination.pos());
        portal.setTeleportDimension(destination.dimension());
        level.addFreshEntity(entity);
        return portal;
    }

    public static List<IPortal> getPortals(Level level) {
        List<IPortal> portals = new ArrayList<>();
        if (level instanceof ServerLevel serverLevel) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof IPortal portal) {
                    portals.add(portal);
                }
            }
        }
        return portals;
    }

    @Nullable
    public static ServerLevel resolveTargetLevel(IPortal portal) {
        if (portal == null) {
            return null;
        }
        Level level = portal.level();
        MinecraftServer server = level == null ? null : level.getServer();
        if (server == null) {
            return null;
        }
        return server.getLevel(portal.getTeleportDimension());
    }

    public static BlockPos findSafeSpawn(ServerLevel level, BlockPos target) {
        BlockPos safe = target.above();
        for (int i = 0; i < 8; i++) {
            BlockPos above = safe.above();
            if (level.getBlockState(safe).isAir() && level.getBlockState(above).isAir()) {
                break;
            }
            safe = above;
        }
        return safe;
    }

    public static boolean teleport(ServerPlayer player, IPortal portal) {
        if (player == null || portal == null) {
            return false;
        }
        ServerLevel targetLevel = resolveTargetLevel(portal);
        if (targetLevel == null) {
            targetLevel = (ServerLevel) player.level();
        }
        BlockPos safe = findSafeSpawn(targetLevel, portal.getTeleportTarget());
        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.teleportTo(targetLevel, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D,
                java.util.Set.of(), player.getYRot(), player.getXRot(), true);
        player.setDeltaMovement(0, 0, 0);
        player.resetFallDistance();
        sendTeleportFx(player);
        return true;
    }

    public static void sendTeleportFx(ServerPlayer player) {
        if (player == null) {
            return;
        }
        arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                new arcaneresearch.common.lib.network.fx.PacketFXTeleport(), player);
    }
}
