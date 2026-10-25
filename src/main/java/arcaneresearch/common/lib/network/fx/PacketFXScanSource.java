package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.ParticleEngine;
import arcaneresearch.client.fx.particles.FXGeneric;
import arcaneresearch.common.lib.utils.Utils;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;
public class PacketFXScanSource {
    private static final int C_QUARTZ = 15064789;
    private static final int C_IRON = 14200723;
    private static final int C_LAPIS = 1328572;
    private static final int C_GOLD = 16576075;
    private static final int C_DIAMOND = 6155509;
    private static final int C_EMERALD = 1564002;
    private static final int C_REDSTONE = 16711680;
    private static final int C_COAL = 1052688;
    private static final int C_SILVER = 14342653;
    private static final int C_TIN = 15724539;
    private static final int C_COPPER = 16620629;
    private static final int C_AMBER = 16626469;
    private static final int C_CINNABAR = 10159368;
    private static final int C_DEFAULT = 12632256;
    private final long loc;
    private final int size;
    public PacketFXScanSource(BlockPos pos, int size) {
        this.loc = pos.asLong();
        this.size = size;
    }
    private PacketFXScanSource(long loc, int size) {
        this.loc = loc;
        this.size = size;
    }
    public static void encode(PacketFXScanSource packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.loc);
        buffer.writeByte(packet.size);
    }
    public static PacketFXScanSource decode(FriendlyByteBuf buffer) {
        long loc = buffer.readLong();
        int size = buffer.readByte();
        return new PacketFXScanSource(loc, size);
    }
    public static void handle(PacketFXScanSource packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXScanSource packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockPos center = BlockPos.of(packet.loc);
        startScan(level, center, packet.size);
    }
    @OnlyIn(Dist.CLIENT)
    private static void startScan(Level level, BlockPos pos, int r) {
        int range = 4 + r * 4;
        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int xx = -range; xx <= range; xx++) {
            for (int yy = -range; yy <= range; yy++) {
                for (int zz = -range; zz <= range; zz++) {
                    BlockPos p = pos.offset(xx, yy, zz);
                    if (Utils.isOreBlock(level, p)) {
                        positions.add(p);
                    }
                }
            }
        }
        while (!positions.isEmpty()) {
            BlockPos start = positions.get(0);
            ArrayList<BlockPos> group = new ArrayList<>();
            group.add(start);
            positions.remove(0);
            calcGroup(level, start, group, positions);
            if (!group.isEmpty()) {
                int color = getOreColor(level, start);
                double x = 0.0;
                double y = 0.0;
                double z = 0.0;
                for (BlockPos p : group) {
                    x += p.getX() + 0.5;
                    y += p.getY() + 0.5;
                    z += p.getZ() + 0.5;
                }
                x /= group.size();
                y /= group.size();
                z /= group.size();
                double dis = Math.sqrt(pos.distToCenterSqr(x, y, z));
                Color c = new Color(color);
                float rf = c.getRed() / 255f;
                float gf = c.getGreen() / 255f;
                float bf = c.getBlue() / 255f;
                float q = (rf + gf + bf) / 3f;
                if (level instanceof ClientLevel clientLevel) {
                    FXGeneric fb = new FXGeneric(clientLevel, x, y, z, 0.0, 0.0, 0.0);
                    fb.setMaxAge(44);
                    fb.setColor(rf, gf, bf);
                    fb.setAlphaKeyframes(0.0f, 1.0f, 0.8f, 0.0f);
                    fb.setParticles(240, 15, 1);
                    fb.setGridSize(16);
                    fb.setLoop(true);
                    fb.setScale(9.0f * 0.1f);
                    fb.setLayer(q < 0.25f ? 3 : 2);
                    fb.setRotationSpeed(0.0f);
                    ParticleEngine.addEffectWithDelay(clientLevel, fb, (int) (dis * 3.0));
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private static void calcGroup(Level level, BlockPos start, ArrayList<BlockPos> group, ArrayList<BlockPos> positions) {
        BlockState startState = level.getBlockState(start);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos neighbor = start.offset(x, y, z);
                    BlockState neighborState = level.getBlockState(neighbor);
                    if (neighborState.equals(startState) && positions.contains(neighbor)) {
                        positions.remove(neighbor);
                        group.add(neighbor);
                        if (positions.isEmpty()) {
                            return;
                        }
                        calcGroup(level, neighbor, group, positions);
                    }
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private static int getOreColor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return C_DEFAULT;
        }
        if (state.is(BlockTags.IRON_ORES)) {
            return C_IRON;
        }
        if (state.is(BlockTags.COAL_ORES)) {
            return C_COAL;
        }
        if (state.is(BlockTags.REDSTONE_ORES)) {
            return C_REDSTONE;
        }
        if (state.is(BlockTags.GOLD_ORES)) {
            return C_GOLD;
        }
        if (state.is(BlockTags.LAPIS_ORES)) {
            return C_LAPIS;
        }
        if (state.is(BlockTags.DIAMOND_ORES)) {
            return C_DIAMOND;
        }
        if (state.is(BlockTags.EMERALD_ORES)) {
            return C_EMERALD;
        }
        if (state.is(BlockTags.COPPER_ORES)) {
            return C_COPPER;
        }
        if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return C_QUARTZ;
        }
        String regName = state.getBlock().getDescriptionId().toLowerCase();
        if (regName.contains("silver")) return C_SILVER;
        if (regName.contains("tin")) return C_TIN;
        if (regName.contains("amber")) return C_AMBER;
        if (regName.contains("cinnabar")) return C_CINNABAR;
        if (regName.contains("quartz")) return C_QUARTZ;
        return C_DEFAULT;
    }
}
