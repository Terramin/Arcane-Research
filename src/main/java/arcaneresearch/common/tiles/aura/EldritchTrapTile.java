package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXZap;
import arcaneresearch.common.tiles.TileArcaneresearch;

import java.util.List;

public class EldritchTrapTile extends TileArcaneresearch {
    private static final double TRAP_RADIUS = 3.0D;
    private static final double FX_RADIUS = 32.0D;

    private int count = 20;

    public EldritchTrapTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchTrapTile tile) {
        tile.serverTick();
    }

    protected void serverTick() {
        Level level = getLevel();

        if (level == null || level.isClientSide) return;

        if (this.count > 0) {
            this.count--;
            return;
        }

        this.count = 10 + level.random.nextInt(25);

        double cx = this.worldPosition.getX() + 0.5D;
        double cy = this.worldPosition.getY() + 0.5D;
        double cz = this.worldPosition.getZ() + 0.5D;

        AABB box = new AABB(this.worldPosition).inflate(TRAP_RADIUS);

        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);

        if (players.isEmpty()) return;

        ServerPlayer target = null;
        double best = Double.MAX_VALUE;

        for (ServerPlayer player : players) {
            double dist = player.distanceToSqr(cx, cy, cz);

            if (dist < best) {
                best = dist;
                target = player;
            }
        }

        if (target == null) return;

        DamageSource magic = target.damageSources().magic();

        target.hurt(magic, 2.0F);

        if (level.random.nextBoolean()) {
            try {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(
                        target,
                        1 + level.random.nextInt(2),
                        IPlayerWarp.EnumWarpType.TEMPORARY
                );
            } catch (Exception ignored) {
            }
        }

        Vec3 center = new Vec3(cx, cy, cz);
        Vec3 eyes = new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());

        PacketFXZap zap = new PacketFXZap(center, eyes, 0xFFFFFF, 0.5F);

        try {
            PacketHandler.sendToNear(
                    zap,
                    new PacketDistributor.TargetPoint(
                            cx, cy, cz, FX_RADIUS, level.dimension()
                    )
            );
        } catch (Exception ignored) {
        }
    }
}