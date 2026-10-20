package arcaneresearch.common.lib.network;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
public class FakeNetHandlerPlayServer extends ServerGamePacketListenerImpl {
    public FakeNetHandlerPlayServer(MinecraftServer server, Connection connection, ServerPlayer player) {
        super(server, connection, player);
    }
    @Override
    public void tick() {
    }
    @Override
    public void send(net.minecraft.network.protocol.Packet<?> packet) {
    }
}
