package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketSelectThaumotoriumRecipeToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSelectThaumotoriumRecipeToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "select_thaumotorium_recipe_to_server"));

    public static final StreamCodec<ByteBuf, PacketSelectThaumotoriumRecipeToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketSelectThaumotoriumRecipeToServer packet) {
            buf.writeLong(packet.pos);
            buf.writeInt(packet.recipeHash);
        }

        @Override
        public PacketSelectThaumotoriumRecipeToServer decode(ByteBuf buf) {
            return new PacketSelectThaumotoriumRecipeToServer(buf.readLong(), buf.readInt());
        }
    };

    private final long pos;
    private final int recipeHash;

    public PacketSelectThaumotoriumRecipeToServer(Player player, BlockPos pos, int recipeHash) {
        this.pos = pos.asLong();
        this.recipeHash = recipeHash;
    }

    private PacketSelectThaumotoriumRecipeToServer(long pos, int recipeHash) {
        this.pos = pos;
        this.recipeHash = recipeHash;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSelectThaumotoriumRecipeToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;
            Level level = player.level();
            BlockPos bp = BlockPos.of(packet.pos);
            if (level == null || bp == null) return;
            BlockEntity be = level.getBlockEntity(bp);
            if (!(be instanceof TileThaumatorium thaumatorium)) return;
            boolean flag = false;
            int i = 0;

            for (int idx = 0; idx < thaumatorium.recipeHash.size(); idx++) {
                if (packet.recipeHash == thaumatorium.recipeHash.get(idx)) {
                    thaumatorium.recipeEssentia.remove(idx);
                    thaumatorium.recipePlayer.remove(idx);
                    thaumatorium.recipeHash.remove(idx);
                    thaumatorium.currentCraft = -1;
                    flag = true;
                    break;
                }
                i++;
            }
            if (!flag && thaumatorium.recipeHash.size() < thaumatorium.maxRecipes) {
                for (CrucibleRecipe cr : thaumatorium.recipes) {
                    if (cr.hash == packet.recipeHash) {
                        thaumatorium.recipeEssentia.add(cr.getAspects().copy());
                        thaumatorium.recipePlayer.add(player.getName().getString());
                        thaumatorium.recipeHash.add(cr.hash);
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) {
                thaumatorium.setChanged();
                thaumatorium.syncTile(false);
            }
        });
    }
}
