package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModSounds;
import arcaneresearch.common.world.maze.MazeFeature;
import arcaneresearch.common.world.maze.TeleporterArcaneresearch;

import java.util.List;

public class EldritchPortalTile extends TileArcaneresearch {
    private static final int PORTAL_COOLDOWN = 100;

    public int opencount = -1;
    private int count = 0;

    private AABB renderBox;

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
        if (level == null || !level.isClientSide) return;

        this.count++;

        if (this.count % 250 == 0 || this.count == 1) {
            level.playSound(null,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    ModSounds.EVIL_PORTAL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (this.opencount < 30) {
            this.opencount++;
        }
    }

    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        int tick = this.count++;
        if (tick % 5 != 0) return;

        AABB box = new AABB(this.worldPosition).inflate(0.5D, 1.0D, 0.5D);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);

        if (players.isEmpty()) return;

        for (ServerPlayer player : players) {
            if (player instanceof FakePlayer) continue;
            if (player.isSpectator()) continue;
            if (player.getVehicle() != null || !player.getPassengers().isEmpty()) continue;

            MinecraftServer server = player.getServer();
            if (server == null) continue;

            if (player.getPortalCooldown() > 0) {
                player.setPortalCooldown(PORTAL_COOLDOWN);
                continue;
            }

            if (player.level().dimension().equals(MazeFeature.DIMENSION)) {
                ServerLevel dest;

                ResourceKey<Level> respawnDim = player.getRespawnDimension();
                dest = server.getLevel(respawnDim);

                if (dest == null || dest.dimension().equals(MazeFeature.DIMENSION)) {
                    dest = server.overworld();
                }

                player.setPortalCooldown(PORTAL_COOLDOWN);
                player.changeDimension(dest, new TeleporterArcaneresearch());
            } else {
                ServerLevel mazeLevel = server.getLevel(MazeFeature.DIMENSION);
                if (mazeLevel == null) continue;

                player.setPortalCooldown(PORTAL_COOLDOWN);
                player.changeDimension(mazeLevel, new TeleporterArcaneresearch());

                if (!ArcaneresearchCapabilities.knowsResearch(player, "ENTEROUTER")) {
                    ResearchManager.startResearchWithPopup(player, "ENTEROUTER");
                }
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    this.worldPosition.offset(-1, -1, -1),
                    this.worldPosition.offset(2, 2, 2)
            );
        }

        return renderBox;
    }
}