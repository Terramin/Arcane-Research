package arcaneresearch.common.world.aura;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import arcaneresearch.common.lib.utils.PosXY;
import java.lang.ref.WeakReference;
public class AuraChunk {
    private static final float MAX_VALUE = 32766.0f;
    private ChunkPos loc;
    private short base;
    private float vis;
    private float flux;
    private WeakReference<LevelChunk> chunkRef;
    public AuraChunk(ChunkPos loc) {
        this.loc = loc;
    }
    public AuraChunk(LevelChunk chunk, short base, float vis, float flux) {
        if (chunk != null) {
            this.loc = chunk.getPos();
            this.chunkRef = new WeakReference<>(chunk);
        }
        this.base = base;
        this.vis = vis;
        this.flux = flux;
    }
    public boolean isModified() {
        return chunkRef != null && chunkRef.get() != null && chunkRef.get().isUnsaved();
    }
    public short getBase() {
        return base;
    }
    public void setBase(short base) {
        this.base = base;
    }
    public float getVis() {
        return vis;
    }
    public void setVis(float vis) {
        this.vis = Math.min(MAX_VALUE, Math.max(0.0f, vis));
    }
    public float getFlux() {
        return flux;
    }
    public void setFlux(float flux) {
        this.flux = Math.min(MAX_VALUE, Math.max(0.0f, flux));
    }
    public ChunkPos getLoc() {
        return loc;
    }
    public void setLoc(ChunkPos loc) {
        this.loc = loc;
    }
    public LevelChunk getChunk() {
        return chunkRef != null ? chunkRef.get() : null;
    }
    public PosXY getLocXY() {
        return loc != null ? new PosXY(loc.x, loc.z) : null;
    }
}
