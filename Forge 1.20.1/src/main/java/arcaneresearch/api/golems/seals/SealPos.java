package arcaneresearch.api.golems.seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.Objects;
public class SealPos {
    public BlockPos pos;
    public Direction face;
    public SealPos(BlockPos pos, Direction face) {
        this.pos = pos;
        this.face = face;
    }
    @Override
    public int hashCode() {
        byte faceOrdinal = (byte) (face.ordinal() + 1);
        int i = 31 * faceOrdinal + pos.getX();
        i = 31 * i + pos.getY();
        i = 31 * i + pos.getZ();
        return i;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SealPos other)) {
            return false;
        }
        return Objects.equals(pos, other.pos) && face == other.face;
    }
}
