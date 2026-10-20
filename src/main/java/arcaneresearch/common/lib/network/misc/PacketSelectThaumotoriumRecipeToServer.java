package arcaneresearch.common.lib.network.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import java.util.function.Supplier;
public class PacketSelectThaumotoriumRecipeToServer {
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
    public static void encode(PacketSelectThaumotoriumRecipeToServer packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.pos);
        buf.writeInt(packet.recipeHash);
    }
    public static PacketSelectThaumotoriumRecipeToServer decode(FriendlyByteBuf buf) {
        return new PacketSelectThaumotoriumRecipeToServer(buf.readLong(), buf.readInt());
    }
    public static void handle(PacketSelectThaumotoriumRecipeToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
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
        ctx.get().setPacketHandled(true);
    }
}
