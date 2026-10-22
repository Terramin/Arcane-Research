package arcaneresearch.lib.lighting;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.lib.colour.ColourRGBA;
import arcaneresearch.lib.render.CCRenderState;
import arcaneresearch.lib.vec.BlockCoord;

public class LightMatrix implements CCRenderState.IVertexOperation {
    public static int operationIndex;
    public static int[][] ssamplem;
    public static int[][] qsamplem;
    public static float[] sideao;

    static {
        operationIndex = CCRenderState.registerOperation();
        ssamplem = new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8}, {18, 19, 20, 21, 22, 23, 24, 25, 26}, {0, 9, 18, 1, 10, 19, 2, 11, 20}, {6, 15, 24, 7, 16, 25, 8, 17, 26}, {0, 3, 6, 9, 12, 15, 18, 21, 24}, {2, 5, 8, 11, 14, 17, 20, 23, 26}, {9, 10, 11, 12, 13, 14, 15, 16, 17}, {9, 10, 11, 12, 13, 14, 15, 16, 17}, {3, 12, 21, 4, 13, 22, 5, 14, 23}, {3, 12, 21, 4, 13, 22, 5, 14, 23}, {1, 4, 7, 10, 13, 16, 19, 22, 25}, {1, 4, 7, 10, 13, 16, 19, 22, 25}, {13, 13, 13, 13, 13, 13, 13, 13, 13}};
        qsamplem = new int[][]{{0, 1, 3, 4}, {5, 1, 2, 4}, {6, 7, 3, 4}, {5, 7, 8, 4}};
        sideao = new float[]{0.5f, 1.0f, 0.8f, 0.8f, 0.6f, 0.6f, 0.5f, 1.0f, 0.8f, 0.8f, 0.6f, 0.6f, 1.0f};
    }

    public int computed;
    public float[][] ao;
    public int[][] brightness;
    
    public BlockAndLightGetter access;
    public BlockCoord pos;
    private int sampled;
    private float[] aSamples;
    private int[] bSamples;

    public LightMatrix() {
        computed = 0;
        ao = new float[13][4];
        brightness = new int[13][4];
        pos = new BlockCoord();
        sampled = 0;
        aSamples = new float[27];
        bSamples = new int[27];
    }

    public static float interpAO(float a, float b, float c, float d) {
        return (a + b + c + d) / 4.0f;
    }

    public static int interpBrightness(int a, int b, int c, int d) {
        if (a == 0) {
            a = d;
        }
        if (b == 0) {
            b = d;
        }
        if (c == 0) {
            c = d;
        }
        return a + b + c + d >> 2 & 0xFF00FF;
    }

    
    public void locate(BlockAndLightGetter level, BlockPos pos) {
        access = level;
        this.pos.set(pos);
        computed = 0;
        sampled = 0;
    }

    public void sample(int i) {
        if ((sampled & 1 << i) == 0x0) {
            int x = pos.x + i % 3 - 1;
            int y = pos.y + i / 9 - 1;
            int z = pos.z + i / 3 % 3 - 1;
            BlockPos p = new BlockPos(x, y, z);
            BlockState b = access.getBlockState(p);

            int sky = access.getBrightness(LightLayer.SKY, p);
            int block = Math.max(access.getBrightness(LightLayer.BLOCK, p),
                    b.getLightEmission(access, p)); 
            bSamples[i] = sky << 20 | block << 4;
            aSamples[i] = b.getShadeBrightness(access, p);
        }
    }

    public int[] brightness(int side) {
        sideSample(side);
        return brightness[side];
    }

    public float[] ao(int side) {
        sideSample(side);
        return ao[side];
    }

    public void sideSample(int side) {
        if ((computed & 1 << side) == 0x0) {
            int[] ssample = LightMatrix.ssamplem[side];
            for (int q = 0; q < 4; ++q) {
                int[] qsample = LightMatrix.qsamplem[q];
                if (Minecraft.getInstance().options.ambientOcclusion().get()) {
                    interp(side, q, ssample[qsample[0]], ssample[qsample[1]], ssample[qsample[2]], ssample[qsample[3]]);
                } else {
                    interp(side, q, ssample[4], ssample[4], ssample[4], ssample[4]);
                }
            }
            computed |= 1 << side;
        }
    }

    private void interp(int s, int q, int a, int b, int c, int d) {
        sample(a);
        sample(b);
        sample(c);
        sample(d);
        ao[s][q] = interpAO(aSamples[a], aSamples[b], aSamples[c], aSamples[d]) * LightMatrix.sideao[s];
        brightness[s][q] = interpBrightness(bSamples[a], bSamples[b], bSamples[c], bSamples[d]);
    }

    @Override
    public boolean load() {
        CCRenderState.pipeline.addDependency(CCRenderState.colourAttrib);
        CCRenderState.pipeline.addDependency(CCRenderState.lightCoordAttrib);
        return true;
    }

    @Override
    public void operate() {
        LC lc = CCRenderState.lc;
        float[] a = ao(lc.side);
        float f = a[0] * lc.fa + a[1] * lc.fb + a[2] * lc.fc + a[3] * lc.fd;
        int[] b = brightness(lc.side);
        CCRenderState.setColour(ColourRGBA.multiplyC(CCRenderState.colour, f));
        CCRenderState.setBrightness((int) (b[0] * lc.fa + b[1] * lc.fb + b[2] * lc.fc + b[3] * lc.fd) & 0xFF00FF);
    }

    @Override
    public int operationID() {
        return LightMatrix.operationIndex;
    }
}
