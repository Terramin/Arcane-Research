package arcaneresearch.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.api.distmarker.Dist;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GLEEmitter {

    public static final int GL_QUAD_STRIP = 5;

    private final PoseStack poseStack;
    private final VertexConsumer builder;
    private final int packedLight;

    private final Deque<double[]> matrixStack = new ArrayDeque<>();

    private float colorR = 1.0F;
    private float colorG = 1.0F;
    private float colorB = 1.0F;
    private float colorA = 1.0F;
    private double normalX;
    private double normalY;
    private double normalZ;
    private double texU;
    private double texV;

    private int beginMode = -1;
    private final List<double[]> stripVerts = new ArrayList<>();

    private static final ThreadLocal<GLEEmitter> ACTIVE = new ThreadLocal<>();
    private static final ThreadLocal<Deque<GLEEmitter>> ACTIVE_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    public GLEEmitter(PoseStack poseStack, VertexConsumer builder, int packedLight) {
        this.poseStack = poseStack;
        this.builder = builder;
        this.packedLight = packedLight;
        this.normalX = 0.0D;
        this.normalY = 0.0D;
        this.normalZ = 1.0D;
        matrixStack.push(identity());
    }

    public static void setActive(GLEEmitter emitter) {
        ACTIVE.set(emitter);
    }

    public static void clearActive(GLEEmitter emitter) {
        if (ACTIVE.get() == emitter) {
            ACTIVE.remove();
        }
    }

    public static void pushActive(GLEEmitter emitter) {
        ACTIVE_STACK.get().push(emitter);
        ACTIVE.set(emitter);
    }

    public static void popActive() {
        Deque<GLEEmitter> stack = ACTIVE_STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            ACTIVE.remove();
        } else {
            ACTIVE.set(stack.peek());
        }
    }

    public static void multActive(double[][] rowMajor) {
        GLEEmitter emitter = ACTIVE.get();
        if (emitter != null && rowMajor != null) {
            emitter.glMultMatrix(rowMajor);
        }
    }

    public boolean isLightingEnabled() {
        return true;
    }

    public void glPushMatrix() {
        double[] top = matrixStack.peek();
        double[] copy = new double[16];
        System.arraycopy(top, 0, copy, 0, 16);
        matrixStack.push(copy);
    }

    public void glPopMatrix() {
        if (matrixStack.size() > 1) {
            matrixStack.pop();
        }
    }

    public void glMultMatrix(double[][] rowMajor) {
        double[] top = matrixStack.peek();
        double[] result = new double[16];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                double sum = 0.0D;
                for (int k = 0; k < 4; k++) {
                    sum += top[r * 4 + k] * rowMajor[c][k];
                }
                result[r * 4 + c] = sum;
            }
        }
        matrixStack.pop();
        matrixStack.push(result);
    }

    public void glBegin(int mode) {
        beginMode = mode;
        stripVerts.clear();
    }

    public void glEnd() {
        if (beginMode == GL_QUAD_STRIP) {
            int n = stripVerts.size();
            for (int i = 0; i + 3 < n; i += 2) {

                emit(stripVerts.get(i));
                emit(stripVerts.get(i + 1));
                emit(stripVerts.get(i + 3));
                emit(stripVerts.get(i + 2));
            }
        } else {
            for (int i = 0; i + 2 < stripVerts.size(); i += 3) {
                emit(stripVerts.get(i));
                emit(stripVerts.get(i + 1));
                emit(stripVerts.get(i + 2));
            }
        }
        stripVerts.clear();
        beginMode = -1;
    }

    public void glColor4f(float r, float g, float b, float a) {
        colorR = r;
        colorG = g;
        colorB = b;
        colorA = a;
    }

    public void glColor3d(double r, double g, double b) {
        colorR = (float) r;
        colorG = (float) g;
        colorB = (float) b;
        colorA = 1.0F;
    }

    public void glNormal3d(double x, double y, double z) {
        normalX = x;
        normalY = y;
        normalZ = z;
    }

    public void glTexCoord2d(double u, double v) {
        texU = u;
        texV = v;
    }

    public void glVertex3d(double x, double y, double z) {
        stripVerts.add(new double[]{x, y, z, normalX, normalY, normalZ,
                colorR, colorG, colorB, colorA, texU, texV});
    }

    public void glCapFan(double[][] loop, int count) {
        if (count < 3) {
            return;
        }
        double cx = 0.0D;
        double cy = 0.0D;
        double cz = 0.0D;
        for (int i = 0; i < count; i++) {
            cx += loop[i][0];
            cy += loop[i][1];
            cz += loop[i][2];
        }
        cx /= count;
        cy /= count;
        cz /= count;
        for (int i = 0; i < count; i++) {
            int j = (i + 1) % count;
            emitDirect(cx, cy, cz);
            emitDirect(loop[i][0], loop[i][1], loop[i][2]);
            emitDirect(loop[j][0], loop[j][1], loop[j][2]);
        }
    }

    private void emitDirect(double x, double y, double z) {
        emit(new double[]{x, y, z, normalX, normalY, normalZ,
                colorR, colorG, colorB, colorA, texU, texV});
    }

    private void emit(double[] v) {
        double[] top = matrixStack.peek();
        double x = top[0] * v[0] + top[1] * v[1] + top[2] * v[2] + top[3];
        double y = top[4] * v[0] + top[5] * v[1] + top[6] * v[2] + top[7];
        double z = top[8] * v[0] + top[9] * v[1] + top[10] * v[2] + top[11];
        double nx = top[0] * v[3] + top[1] * v[4] + top[2] * v[5];
        double ny = top[4] * v[3] + top[5] * v[4] + top[6] * v[5];
        double nz = top[8] * v[3] + top[9] * v[4] + top[10] * v[5];
        double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nl > 1.0E-9D) {
            nx /= nl;
            ny /= nl;
            nz /= nl;
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        Vector4f pos = new Vector4f((float) x, (float) y, (float) z, 1.0F);
        matrix.transform(pos);
        Vector3f nrm = new Vector3f((float) nx, (float) ny, (float) nz);
        normalMatrix.transform(nrm);
        nrm.normalize();

        
        
        
        builder.addVertex(pos.x(), pos.y(), pos.z())
                .setColor((float) v[6], (float) v[7], (float) v[8], (float) v[9])
                .setUv((float) v[10], (float) v[11])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(nrm.x(), nrm.y(), nrm.z());
    }

    private static double[] identity() {
        double[] m = new double[16];
        m[0] = 1.0D;
        m[5] = 1.0D;
        m[10] = 1.0D;
        m[15] = 1.0D;
        return m;
    }
}