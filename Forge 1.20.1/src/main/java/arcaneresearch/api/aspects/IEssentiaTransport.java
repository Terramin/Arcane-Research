package arcaneresearch.api.aspects;
import net.minecraft.core.Direction;
public interface IEssentiaTransport {
    boolean isConnectable(Direction direction);
    boolean canInputFrom(Direction direction);
    boolean canOutputTo(Direction direction);
    void setSuction(Aspect aspect, int amount);
    Aspect getSuctionType(Direction direction);
    int getSuctionAmount(Direction direction);
    int takeEssentia(Aspect aspect, int amount, Direction direction);
    int addEssentia(Aspect aspect, int amount, Direction direction);
    Aspect getEssentiaType(Direction direction);
    int getEssentiaAmount(Direction direction);
    int getMinimumSuction();
    default boolean isBlocked() {
        return false;
    }
}
