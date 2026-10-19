package arcaneresearch.client.fx.beams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class FXLightning extends TextureSheetParticle {
    private static final ResourceLocation LARGE =
            new ResourceLocation("arcaneresearch", "textures/misc/p_large.png");
    private static final ResourceLocation SMALL =
            new ResourceLocation("arcaneresearch", "textures/misc/p_small.png");
    private static final float[][] OUTER = {
            {0.6F, 0.3F, 0.6F}, {0.6F, 0.6F, 0.1F}, {0.1F, 0.1F, 0.6F}, {0.1F, 1.0F, 0.1F},
            {0.6F, 0.1F, 0.1F}, {0.6F, 0.2F, 0.6F}, {0.75F, 1.0F, 1.0F},
    };
    private static final float[][] CORE = {
            {1.0F, 0.6F, 1.0F}, {1.0F, 1.0F, 0.1F}, {0.1F, 0.1F, 1.0F}, {0.1F, 0.6F, 0.1F},
            {1.0F, 0.1F, 0.1F}, {0.0F, 0.0F, 0.0F}, {0.75F, 1.0F, 1.0F},
    };
    private static final class V {
        double x, y, z;
        V(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        V copy() {
            return new V(x, y, z);
        }
        V add(V o) {
            x += o.x;
            y += o.y;
            z += o.z;
            return this;
        }
        V sub(V o) {
            x -= o.x;
            y -= o.y;
            z -= o.z;
            return this;
        }
        V scale(double s) {
            x *= s;
            y *= s;
            z *= s;
            return this;
        }
        double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }
        V normalize() {
            double l = length();
            if (l > 1.0E-9D) {
                scale(1.0D / l);
            }
            return this;
        }
        double dot(V o) {
            return x * o.x + y * o.y + z * o.z;
        }
        static V cross(V a, V b) {
            return new V(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        }
        static double anglePreNorm(V a, V b) {
            double d = Math.max(-1.0D, Math.min(1.0D, a.dot(b)));
            return Math.acos(d);
        }
        static V perpendicular(V v) {
            double ax = Math.abs(v.x), ay = Math.abs(v.y);
            V ref = (ax < 0.9D || ay < 0.9D)
                    ? (ax < ay ? new V(1, 0, 0) : new V(0, 1, 0))
                    : new V(0, 0, 1);
            return cross(v, ref).normalize();
        }
        V rotate(double deg, V axis) {
            double rad = Math.toRadians(deg);
            double c = Math.cos(rad);
            double s = Math.sin(rad);
            V k = axis.copy().normalize();
            double d = dot(k);
            double nx = x * c + (k.y * z - k.z * y) * s + k.x * d * (1.0D - c);
            double ny = y * c + (k.z * x - k.x * z) * s + k.y * d * (1.0D - c);
            double nz = z * c + (k.x * y - k.y * x) * s + k.z * d * (1.0D - c);
            x = nx;
            y = ny;
            z = nz;
            return this;
        }
    }
    private static final class BoltPoint {
        final V point;
        final V basepoint;
        final V offsetvec;
        BoltPoint(V basepoint, V offsetvec) {
            this.basepoint = basepoint;
            this.offsetvec = offsetvec;
            this.point = basepoint.copy().add(offsetvec);
        }
    }
    private static final class Segment {
        final BoltPoint startpoint;
        final BoltPoint endpoint;
        V diff;
        Segment prev;
        Segment next;
        V prevdiff;
        V nextdiff;
        float sinprev = 1.0F;
        float sinnext = 1.0F;
        final float light;
        final int segmentno;
        final int splitno;
        Segment(BoltPoint start, BoltPoint end, float light, int segmentno, int splitno) {
            this.startpoint = start;
            this.endpoint = end;
            this.light = light;
            this.segmentno = segmentno;
            this.splitno = splitno;
            calcDiff();
        }
        void calcDiff() {
            this.diff = endpoint.point.copy().sub(startpoint.point);
        }
        void calcEndDiffs() {
            if (prev != null) {
                V prevnorm = prev.diff.copy().normalize();
                V thisnorm = diff.copy().normalize();
                prevdiff = thisnorm.add(prevnorm).normalize();
                sinprev = (float) Math.sin(V.anglePreNorm(thisnorm, prevnorm.scale(-1.0D)) / 2.0D);
            } else {
                prevdiff = diff.copy().normalize();
                sinprev = 1.0F;
            }
            if (next != null) {
                V nextnorm = next.diff.copy().normalize();
                V thisnorm = diff.copy().normalize();
                nextdiff = thisnorm.add(nextnorm).normalize();
                sinnext = (float) Math.sin(V.anglePreNorm(thisnorm, nextnorm.scale(-1.0D)) / 2.0D);
            } else {
                nextdiff = diff.copy().normalize();
                sinnext = 1.0F;
            }
        }
    }
    private final List<Segment> segments = new ArrayList<>();
    private final Map<Integer, Integer> splitparents = new HashMap<>();
    private final Random rand;
    private final float length;
    private int numsegments0 = 1;
    private int numpsplits = 0;
    private boolean finalized = false;
    private final int maxAge;
    private int boltAge;
    private int type = 0;
    public FXLightning(ClientLevel level, double x1, double y1, double z1,
                       double x2, double y2, double z2,
                       long seed, int duration, float multi) {
        super(level, (x1 + x2) / 2.0D, (y1 + y2) / 2.0D, (z1 + z2) / 2.0D, 0, 0, 0);
        this.rand = new Random(seed);
        this.setSize(0.02f, 0.02f);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.segments.add(new Segment(
                new BoltPoint(new V(x1, y1, z1), new V(0, 0, 0)),
                new BoltPoint(new V(x2, y2, z2), new V(0, 0, 0)), 1.0F, 0, 0));
        V d = new V(x2 - x1, y2 - y1, z2 - z1);
        this.length = (float) d.length();
        this.multiplier = multi;
        this.maxAge = duration + rand.nextInt(Math.max(1, duration)) - duration / 2;
        this.boltAge = -(int) (this.length * 3.0F);
        this.lifetime = Math.max(1, maxAge);
    }
    private float multiplier = 1.0F;
    public void setType(int type) {
        this.type = Math.max(0, Math.min(6, type));
    }
    public void defaultFractal() {
        fractal(2, length * multiplier / 8.0F, 0.7F, 0.1F, 45.0F);
        fractal(2, length * multiplier / 12.0F, 0.5F, 0.1F, 50.0F);
        fractal(2, length * multiplier / 17.0F, 0.5F, 0.1F, 55.0F);
        fractal(2, length * multiplier / 23.0F, 0.5F, 0.1F, 60.0F);
        fractal(2, length * multiplier / 30.0F, 0.0F, 0.0F, 0.0F);
        fractal(2, length * multiplier / 34.0F, 0.0F, 0.0F, 0.0F);
        fractal(2, length * multiplier / 40.0F, 0.0F, 0.0F, 0.0F);
    }
    public void fractal(int splits, float amount, float splitchance, float splitlength, float splitangle) {
        if (finalized) {
            return;
        }
        List<Segment> old = new ArrayList<>(segments);
        segments.clear();
        Segment prev = null;
        for (Segment segment : old) {
            prev = segment.prev;
            V sub = segment.diff.copy().scale(1.0F / splits);
            BoltPoint[] pts = new BoltPoint[splits + 1];
            V startpoint = segment.startpoint.point;
            pts[0] = segment.startpoint;
            pts[splits] = segment.endpoint;
            int i;
            for (i = 1; i < splits; i++) {
                V off = V.perpendicular(segment.diff)
                        .rotate(rand.nextFloat() * 360.0F, segment.diff);
                off.scale((rand.nextFloat() - 0.5F) * amount);
                V base = startpoint.copy().add(sub.copy().scale(i));
                pts[i] = new BoltPoint(base, off);
            }
            for (i = 0; i < splits; i++) {
                Segment next = new Segment(pts[i], pts[i + 1],
                        segment.light, segment.segmentno * splits + i, segment.splitno);
                next.prev = prev;
                if (prev != null) {
                    prev.next = next;
                }
                if (i != 0 && rand.nextFloat() < splitchance) {
                    V splitrot = V.cross(new V(1, 0, 0), next.diff)
                            .rotate(rand.nextFloat() * 360.0F, next.diff);
                    V diff = next.diff.copy()
                            .rotate((rand.nextFloat() * 0.66F + 0.33F) * splitangle, splitrot)
                            .scale(splitlength);
                    numpsplits++;
                    splitparents.put(numpsplits, next.splitno);
                    Segment split = new Segment(pts[i],
                            new BoltPoint(pts[i + 1].basepoint,
                                    pts[i + 1].offsetvec.copy().add(diff)),
                            segment.light / 2.0F, next.segmentno, numpsplits);
                    split.prev = prev;
                    segments.add(split);
                }
                prev = next;
                segments.add(next);
            }
            if (segment.next != null) {
                segment.next.prev = prev;
            }
        }
        numsegments0 *= splits;
    }
    public void finalizeBolt() {
        if (finalized) {
            return;
        }
        finalized = true;
        List<Segment> ordered = new ArrayList<>(segments);
        ordered.sort(Comparator.comparingInt((Segment s) -> s.splitno)
                .thenComparingInt(s -> s.segmentno));
        Map<Integer, Integer> lastactive = new HashMap<>();
        int lastsplitcalc = 0;
        int lastactiveseg = 0;
        for (Segment segment : ordered) {
            if (segment.splitno > lastsplitcalc) {
                lastactive.put(lastsplitcalc, lastactiveseg);
                lastsplitcalc = segment.splitno;
                lastactiveseg = lastactive.get(splitparents.get(segment.splitno));
            }
            lastactiveseg = segment.segmentno;
        }
        lastactive.put(lastsplitcalc, lastactiveseg);
        lastsplitcalc = 0;
        lastactiveseg = lastactive.get(0);
        List<Segment> kept = new ArrayList<>();
        for (Segment segment : ordered) {
            if (lastsplitcalc != segment.splitno) {
                lastsplitcalc = segment.splitno;
                lastactiveseg = lastactive.get(segment.splitno);
            }
            if (segment.segmentno > lastactiveseg) {
                continue;
            }
            segment.calcEndDiffs();
            kept.add(segment);
        }
        segments.clear();
        segments.addAll(kept);
        segments.sort((a, b) -> Float.compare(b.light, a.light));
    }
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.boltAge++;
        if (this.boltAge > this.maxAge) {
            this.remove();
        }
        if (this.age++ >= this.lifetime + 4) {
            this.remove();
        }
    }
    @Override
    public void render(@NotNull VertexConsumer ignored,
                       @NotNull Camera camera, float partialTicks) {
        if (segments.isEmpty() || !finalized) {
            return;
        }
        float boltage = boltAge >= 0 ? (float) boltAge / (float) Math.max(1, maxAge) : 0.0F;
        Vec3 camPos = camera.getPosition();
        org.joml.Vector3f look = camera.getLookVector();
        float renderlength = (boltAge + partialTicks + length * 3.0F) / (length * 3.0F) * numsegments0;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        for (int pass = 0; pass < 2; pass++) {
            float[] col = (pass == 0 ? OUTER : CORE)[Math.min(6, type)];
            boolean normalBlend = type >= 5;
            float alphaBase = pass == 0 ? (1.0F - boltage) * 0.4F : 1.0F - boltage * 0.5F;
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, pass == 0 ? LARGE : SMALL);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, normalBlend ? 771 : 1);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Segment seg : segments) {
                if (seg.segmentno > renderlength) {
                    continue;
                }
                V start = seg.startpoint.point;
                V end = seg.endpoint.point;
                double dx = end.x - camPos.x;
                double dy = end.y - camPos.y;
                double dz = end.z - camPos.z;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                float width = 0.03F * (float) (dist / 5.0D + 1.0D) * (1.0F + seg.light) * 0.5F;
                V d1 = V.cross(new V(look.x, look.y, look.z), seg.prevdiff)
                        .scale(width / Math.max(1.0E-4F, seg.sinprev));
                V d2 = V.cross(new V(look.x, look.y, look.z), seg.nextdiff)
                        .scale(width / Math.max(1.0E-4F, seg.sinnext));
                float a = alphaBase * seg.light;
                float sx = (float) (start.x - camPos.x);
                float sy = (float) (start.y - camPos.y);
                float sz = (float) (start.z - camPos.z);
                float ex = (float) (end.x - camPos.x);
                float ey = (float) (end.y - camPos.y);
                float ez = (float) (end.z - camPos.z);
                builder.vertex(ex - (float) d2.x, ey - (float) d2.y, ez - (float) d2.z)
                        .uv(0.5F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                builder.vertex(sx - (float) d1.x, sy - (float) d1.y, sz - (float) d1.z)
                        .uv(0.5F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                builder.vertex(sx + (float) d1.x, sy + (float) d1.y, sz + (float) d1.z)
                        .uv(0.5F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                builder.vertex(ex + (float) d2.x, ey + (float) d2.y, ez + (float) d2.z)
                        .uv(0.5F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                if (seg.next == null) {
                    float rx = (float) (end.x + seg.diff.x / Math.max(1.0E-6D,
                            Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.x);
                    float ry = (float) (end.y + seg.diff.y / Math.max(1.0E-6D,
                            Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.y);
                    float rz = (float) (end.z + seg.diff.z / Math.max(1.0E-6D,
                            Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.z);
                    builder.vertex(rx - (float) d2.x, ry - (float) d2.y, rz - (float) d2.z)
                            .uv(0.0F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(ex - (float) d2.x, ey - (float) d2.y, ez - (float) d2.z)
                            .uv(0.5F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(ex + (float) d2.x, ey + (float) d2.y, ez + (float) d2.z)
                            .uv(0.5F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(rx + (float) d2.x, ry + (float) d2.y, rz + (float) d2.z)
                            .uv(0.0F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                }
                if (seg.prev == null) {
                    double dl = Math.max(1.0E-6D,
                            Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z));
                    float rx = (float) (start.x - seg.diff.x / dl * width - camPos.x);
                    float ry = (float) (start.y - seg.diff.y / dl * width - camPos.y);
                    float rz = (float) (start.z - seg.diff.z / dl * width - camPos.z);
                    builder.vertex(sx - (float) d1.x, sy - (float) d1.y, sz - (float) d1.z)
                            .uv(0.5F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(rx - (float) d1.x, ry - (float) d1.y, rz - (float) d1.z)
                            .uv(0.0F, 0.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(rx + (float) d1.x, ry + (float) d1.y, rz + (float) d1.z)
                            .uv(0.0F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                    builder.vertex(sx + (float) d1.x, sy + (float) d1.y, sz + (float) d1.z)
                            .uv(0.5F, 1.0F).color(col[0], col[1], col[2], a).endVertex();
                }
            }
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(770, 771);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        }
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
    @Override
    public boolean shouldCull() {
        return false;
    }
}
