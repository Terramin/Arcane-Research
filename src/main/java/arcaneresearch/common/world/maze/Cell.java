package arcaneresearch.common.world.maze;

public class Cell {
    public boolean north;
    public boolean south;
    public boolean east;
    public byte feature = 0;
    public boolean west;
    public boolean above;
    public boolean below = false;

    public Cell() {
    }

    public Cell(short data) {
        unpack(data);
    }

    private void unpack(short data) {
        this.north = (data & 1) != 0;
        this.south = (data & 2) != 0;
        this.east  = (data & 4) != 0;
        this.west  = (data & 8) != 0;
        this.above = (data & 16) != 0;
        this.below = (data & 32) != 0;
        this.feature = (byte) (data >> 8);
    }

    public short pack() {
        int out = (this.north ? 1 : 0)
                | (this.south ? 2 : 0)
                | (this.east  ? 4 : 0)
                | (this.west  ? 8 : 0)
                | (this.above ? 16 : 0)
                | (this.below ? 32 : 0)
                | ((this.feature & 0xFF) << 8);
        return (short) out;
    }
}