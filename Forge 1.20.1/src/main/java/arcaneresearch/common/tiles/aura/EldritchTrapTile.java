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
        if (level == null || level.isClientSide) {
            return;
        }
        if (this.count-- > 0) {
            return;
        }
        this.count = 10 + level.random.nextInt(25);
        try {
            Vec3 center = new Vec3(this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(this.worldPosition).inflate(3.0D, 3.0D, 3.0D));
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
            if (level.random.nextBoolean()) {
                try {
                    ArcaneresearchApi.internalMethods.addWarpToPlayer(target,
                            1 + level.random.nextInt(2), IPlayerWarp.EnumWarpType.TEMPORARY);
                } catch (Exception ignored) {
                }
            }
            Vec3 eyes = new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
            PacketFXZap zap = new PacketFXZap(center, eyes, 0xFFFFFF, 0.5F);
            try {
                PacketHandler.sendToNear(zap,
                        new net.minecraftforge.network.PacketDistributor.TargetPoint(
                                center.x, center.y, center.z, 32.0D, level.dimension()));
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }
}
