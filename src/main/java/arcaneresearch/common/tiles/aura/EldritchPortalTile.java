package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModSounds;
import arcaneresearch.common.world.maze.MazeFeature;
import arcaneresearch.common.world.maze.TeleporterArcaneresearch;

import java.util.List;

 






 
public class EldritchPortalTile extends TileArcaneresearch {
    public int opencount = -1;
    private int count = 0;

    public EldritchPortalTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchPortalTile tile) {
        tile.serverTick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EldritchPortalTile tile) {
        tile.clientTick();
    }

    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }
        this.count++;
        if (this.count % 250 == 0 || this.count == 1) {
            level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D, ModSounds.EVIL_PORTAL.get(), SoundSource.BLOCKS,
                    1.0F, 1.0F);
        }
        if (this.opencount < 30) {
            this.opencount++;
        }
    }

    protected void serverTick() {
        
        
        Level level = getLevel();
        if (level == null || level.isClientSide() || this.count++ % 5 != 0) {
            return;
        }
        AABB box = new AABB(this.worldPosition).inflate(0.5D, 1.0D, 0.5D);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);
        for (ServerPlayer player : players) {
            if (player instanceof net.neoforged.neoforge.common.util.FakePlayer) {
                continue;
            }
            if (player.isSpectator()) {
                continue;
            }
            if (player.getVehicle() != null || !player.getPassengers().isEmpty()) {
                continue;
            }
            if (player.getPortalCooldown() > 0) {
                player.setPortalCooldown(100);
                continue;
            }
            if (player.level().dimension().equals(MazeFeature.DIMENSION)) {
                
                
                net.minecraft.server.MinecraftServer server = level.getServer();
                if (server == null) {
                    continue;
                }
                net.minecraft.server.level.ServerLevel dest = null;
                try {
                    
                    
                    ServerPlayer.RespawnConfig cfg = player.getRespawnConfig();
                    net.minecraft.resources.ResourceKey<Level> respawnDim =
                            cfg != null && cfg.respawnData() != null
                                    && cfg.respawnData().globalPos() != null
                                    ? cfg.respawnData().globalPos().dimension() : null;
                    if (respawnDim != null) {
                        dest = server.getLevel(respawnDim);
                    }
                } catch (Exception ignored) {
                }
                if (dest == null || dest.dimension().equals(MazeFeature.DIMENSION)) {
                    dest = server.overworld();
                }
                if (dest == null) {
                    continue;
                }
                player.setPortalCooldown(100);
                TeleporterArcaneresearch.teleportPlayer(player, dest);
            } else {
                net.minecraft.server.MinecraftServer server = level.getServer();
                if (server == null
                        || server.getLevel(MazeFeature.DIMENSION) == null) {
                    continue;
                }
                player.setPortalCooldown(100);
                TeleporterArcaneresearch.teleportPlayer(player,
                        server.getLevel(MazeFeature.DIMENSION));
                if (!ArcaneresearchCapabilities.knowsResearch(player, "ENTEROUTER")) {
                    ResearchManager.startResearchWithPopup(player, "ENTEROUTER");
                }
            }
        }
    }

    public AABB getRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 2, 2));
    }
}
