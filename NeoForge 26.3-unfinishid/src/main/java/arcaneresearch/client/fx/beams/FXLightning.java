package arcaneresearch.client.fx.beams;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// NEOFORGE_PORT: 1:1 logic from Forge client/fx/beams/FXLightning.java (fractal
// midpoint-displacement + branching, growth with age, two passes wide-glow +
// bright-core, distance-based width). REQUIRED changes: TextureSheetParticle
// removed -> SingleQuadParticle + BeamEngine.BeamEffect (same as FXBolt);
// immediate-mode render (Tesselator/BufferBuilder/RenderSystem/GameRenderer)
// removed -> emitQuads() submits world-space ribbon quads through BeamEngine
// (addVertex/setUv/setColor(ARGB); no endVertex; state via RenderType).
// Two passes (LARGE glow + SMALL core) use the multi-texture BeamEffect path
// (beamRenderTypes + typed emitQuads); pass 1 with type >= 5 keeps the old
// normal-blend look via normalBeam, otherwise additive like Forge blendFunc.
// The old camera-look miter vector (Camera#getLookVector) is Camera#forwardVector.
/**
 * Фрактальная молния, точный порт TC4 {@code FXLightningBolt} (+Common):
 * midpoint-displacement с ветвлением, рост по мере возраста, два прохода
 * (широкое свечение + яркое ядро), ширина от дистанции. Нодовый разряд
 * TC4 ({@code nodeBolt}, type 0) идёт через неё, а не через простой зигзаг.
 */
@OnlyIn(Dist.CLIENT)
public class FXLightning extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier LARGE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/p_large.png");
    private static final Identifier SMALL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/p_small.png");

    /** Цвета проходов по типам TC4: {outer}, {core}. */
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

        /** Угол между (считаются уже нормированными). */
        static double anglePreNorm(V a, V b) {
            double d = Math.max(-1.0D, Math.min(1.0D, a.dot(b)));
            return Math.acos(d);
        }

        /** Любой перпендикуляр. */
        static V perpendicular(V v) {
            double ax = Math.abs(v.x), ay = Math.abs(v.y);
            V ref = (ax < 0.9D || ay < 0.9D)
                    ? (ax < ay ? new V(1, 0, 0) : new V(0, 1, 0))
                    : new V(0, 0, 1);
            return cross(v, ref).normalize();
        }

        /** Поворот вектора на угол (градусы) вокруг оси (Родриг). */
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
        super(level, (x1 + x2) / 2.0D, (y1 + y2) / 2.0D, (z1 + z2) / 2.0D, 0, 0, 0, null);
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

    /** TC4 defaultFractal дословно. */
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

    /** TC4 finalizeBolt дословно (обрезка ветвей + сортировка по свету). */
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

    // NEOFORGE_PORT: engine must NOT submit this (BeamEngine does); NO_RENDER skips grouping.
    @Override
    public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState state,
                        Camera camera, float partialTicks) {
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.NO_RENDER;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    private RenderType largeType() {
        // NEOFORGE_PORT (26.3): Forge used blendFunc(770, type >= 5 ? 771 : 1) for BOTH passes.
        return type >= 5 ? BeamEngine.normalBeam(LARGE) : BeamEngine.additiveBeam(LARGE);
    }

    private RenderType smallType() {
        boolean normalBlend = type >= 5;
        return normalBlend ? BeamEngine.normalBeam(SMALL) : BeamEngine.additiveBeam(SMALL);
    }

    @Override
    public RenderType beamRenderType() {
        return largeType();
    }

    @Override
    public java.util.List<RenderType> beamRenderTypes() {
        return java.util.List.of(largeType(), smallType());
    }

    @Override
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        emitQuads(consumer, camera, partialTicks, largeType());
        emitQuads(consumer, camera, partialTicks, smallType());
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks, RenderType renderType) {
        if (segments.isEmpty() || !finalized) {
            return;
        }
        int pass = renderType == smallType() ? 1 : 0;
        float boltage = boltAge >= 0 ? (float) boltAge / (float) Math.max(1, maxAge) : 0.0F;
        net.minecraft.world.phys.Vec3 camPos = camera.position();
        float renderlength = (boltAge + partialTicks + length * 3.0F) / (length * 3.0F) * numsegments0;

        float[] col = (pass == 0 ? OUTER : CORE)[Math.min(6, type)];
        // TC4: ma существует, но нигде не применяется (только type 6
        // его меняет) — для точности не применяем и здесь.
        float alphaBase = pass == 0 ? (1.0F - boltage) * 0.4F : 1.0F - boltage * 0.5F;

        // NEOFORGE_PORT (26.3): Camera#getLookVector -> Camera#forwardVector (same vector).
        org.joml.Vector3fc fwd = camera.forwardVector();
        V look = new V(fwd.x(), fwd.y(), fwd.z());

        for (Segment seg : segments) {
            if (seg.segmentno > renderlength) {
                continue;
            }
            V start = seg.startpoint.point;
            V end = seg.endpoint.point;
            double dx = end.x - camPos.x();
            double dy = end.y - camPos.y();
            double dz = end.z - camPos.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float width = 0.03F * (float) (dist / 5.0D + 1.0D) * (1.0F + seg.light) * 0.5F;
            // Майтер-стыки дословно TC4 (cross с направлением взгляда).
            V d1 = V.cross(look, seg.prevdiff)
                    .scale(width / Math.max(1.0E-4F, seg.sinprev));
            V d2 = V.cross(look, seg.nextdiff)
                    .scale(width / Math.max(1.0E-4F, seg.sinnext));
            float a = alphaBase * seg.light;
            int argb = ARGB.color((int) (a * 255.0F),
                    (int) (col[0] * 255.0F), (int) (col[1] * 255.0F), (int) (col[2] * 255.0F));
            float sx = (float) (start.x - camPos.x());
            float sy = (float) (start.y - camPos.y());
            float sz = (float) (start.z - camPos.z());
            float ex = (float) (end.x - camPos.x());
            float ey = (float) (end.y - camPos.y());
            float ez = (float) (end.z - camPos.z());
            quad(consumer,
                    ex - (float) d2.x, ey - (float) d2.y, ez - (float) d2.z, 0.5F, 0.0F, argb,
                    sx - (float) d1.x, sy - (float) d1.y, sz - (float) d1.z, 0.5F, 0.0F, argb,
                    sx + (float) d1.x, sy + (float) d1.y, sz + (float) d1.z, 0.5F, 1.0F, argb,
                    ex + (float) d2.x, ey + (float) d2.y, ez + (float) d2.z, 0.5F, 1.0F, argb);
            if (seg.next == null) {
                float rx = (float) (end.x + seg.diff.x / Math.max(1.0E-6D,
                        Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.x());
                float ry = (float) (end.y + seg.diff.y / Math.max(1.0E-6D,
                        Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.y());
                float rz = (float) (end.z + seg.diff.z / Math.max(1.0E-6D,
                        Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z)) * width - camPos.z());
                quad(consumer,
                        rx - (float) d2.x, ry - (float) d2.y, rz - (float) d2.z, 0.0F, 0.0F, argb,
                        ex - (float) d2.x, ey - (float) d2.y, ez - (float) d2.z, 0.5F, 0.0F, argb,
                        ex + (float) d2.x, ey + (float) d2.y, ez + (float) d2.z, 0.5F, 1.0F, argb,
                        rx + (float) d2.x, ry + (float) d2.y, rz + (float) d2.z, 0.0F, 1.0F, argb);
            }
            if (seg.prev == null) {
                double dl = Math.max(1.0E-6D,
                        Math.sqrt(seg.diff.x * seg.diff.x + seg.diff.y * seg.diff.y + seg.diff.z * seg.diff.z));
                float rx = (float) (start.x - seg.diff.x / dl * width - camPos.x());
                float ry = (float) (start.y - seg.diff.y / dl * width - camPos.y());
                float rz = (float) (start.z - seg.diff.z / dl * width - camPos.z());
                quad(consumer,
                        sx - (float) d1.x, sy - (float) d1.y, sz - (float) d1.z, 0.5F, 0.0F, argb,
                        rx - (float) d1.x, ry - (float) d1.y, rz - (float) d1.z, 0.0F, 0.0F, argb,
                        rx + (float) d1.x, ry + (float) d1.y, rz + (float) d1.z, 0.0F, 1.0F, argb,
                        sx + (float) d1.x, sy + (float) d1.y, sz + (float) d1.z, 0.5F, 1.0F, argb);
            }
        }
    }

    private static void quad(VertexConsumer consumer,
            double x0, double y0, double z0, float u0, float v0, int argb,
            double x1, double y1, double z1, float u1, float v1, int argb1,
            double x2, double y2, double z2, float u2, float v2, int argb2,
            double x3, double y3, double z3, float u3, float v3, int argb3) {
        // NEOFORGE_PORT (crash fix): ENTITY pipelines require the full vertex
        // (cf. AuraNodeRenderHelper: overlay + fullbright + normal); pos+uv+color
        // alone dies in world submits with "Missing elements in vertex".
        // Beams are unlit glow -> fullbright; ribbons face the camera, Y-up normal
        // matches the other billboard emitters.
        consumer.addVertex((float) x0, (float) y0, (float) z0).setUv(u0, v0).setColor(argb)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x1, (float) y1, (float) z1).setUv(u1, v1).setColor(argb1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x2, (float) y2, (float) z2).setUv(u2, v2).setColor(argb2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x3, (float) y3, (float) z3).setUv(u3, v3).setColor(argb3)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
    }

    // NEOFORGE_PORT: Particle.shouldCull убран вместе с движком частиц;
    // BeamEngine не отсекает (тот же no-cull, что Forge disableCull в FXLightning).
    public boolean shouldCull() {
        return false;
    }
}
