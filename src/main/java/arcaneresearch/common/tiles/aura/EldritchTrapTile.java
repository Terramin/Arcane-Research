package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXZap;
import arcaneresearch.common.tiles.TileArcaneresearch;

import java.util.List;

/**
 * Тайл ловушки, порт TC4 TileEldritchTrap: каждые 10-35 тиков бьёт
 * ближайшего игрока в радиусе 3 (2 урона магией + 50% временный варп 1-2)
 * с разрядом-молнией. Лежит в arcaneresearch.arcaneresearch.common.tiles.aura.
 */
public class EldritchTrapTile extends TileArcaneresearch {
    private int count = 20;

    public EldritchTrapTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchTrapTile tile) {
        tile.serverTick();
    }

    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (this.count-- > 0) {
            return;
        }
        this.count = 10 + level.getRandom().nextInt(25);
        try {
            Vec3 center = new Vec3(this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D);
            List<ServerPlayer> players = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(ServerPlayer.class), new AABB(this.worldPosition).inflate(3.0D, 3.0D, 3.0D), e -> true);
            ServerPlayer target = null;
            double best = Double.MAX_VALUE;
            for (ServerPlayer player : players) {
                double dist = player.distanceToSqr(center);
                if (dist < best) {
                    best = dist;
                    target = player;
                }
            }
            if (target == null) {
                return;
            }
            DamageSource magic = target.damageSources().magic();
            target.hurt(magic, 2.0F);
            if (level.getRandom().nextBoolean()) {
                try {
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(target,
                            1 + level.getRandom().nextInt(2), IPlayerWarp.EnumWarpType.TEMPORARY);
                } catch (Exception ignored) {
                }
            }
            Vec3 eyes = new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
            PacketFXZap zap = new PacketFXZap(center, eyes, 0xFFFFFF, 0.5F);
            // NEOFORGE_PORT: Forge PacketDistributor.TargetPoint(x,y,z,range,dim) removed ->
            //   PacketDistributor.sendToPlayersNear(level, except, x, y, z, range, payload).
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                try {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(
                            serverLevel, null, center.x, center.y, center.z, 32.0D, zap);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}
