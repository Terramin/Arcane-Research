package arcaneresearch.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.beams.FXArc;
import arcaneresearch.client.fx.beams.FXBeamBore;
import arcaneresearch.client.fx.beams.FXBeamWand;
import arcaneresearch.client.fx.beams.FXBolt;
import arcaneresearch.client.fx.other.*;
import arcaneresearch.client.fx.particles.*;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
import arcaneresearch.common.tiles.crafting.TileCrucible;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.awt.*;
import java.util.Random;

public class FXDispatcher {

    private static final java.util.ArrayDeque<Long> serverClockWindow = new java.util.ArrayDeque<>();
    private static final int CLOCK_WINDOW = 8;
    public static FXDispatcher INSTANCE = new FXDispatcher();

    /**
     * РЎСѓС‰РЅРѕСЃС‚СЊ вЂ” Р»РѕРєР°Р»СЊРЅС‹Р№ РёРіСЂРѕРє СЃ РІРёРґРѕРј РѕС‚ РїРµСЂРІРѕРіРѕ Р»РёС†Р°.
     * Р­С„С„РµРєС‚С‹ РІРѕРєСЂСѓРі Р»РёС†Р° (СЃРїРёСЂР°Р»СЊ РјРµС‡Р°) РІ СЌС‚РѕРј СЃР»СѓС‡Р°Рµ СЃР»РµРїСЏС‚ РєР°РјРµСЂСѓ,
     * РѕС‚ 3 Р»РёС†Р° Рё РґР»СЏ РЅР°Р±Р»СЋРґР°С‚РµР»РµР№ РІСЃС‘ РєР°Рє РѕР±С‹С‡РЅРѕ.
     */
    public static boolean isLocalFirstPerson(net.minecraft.world.entity.Entity entity) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        return mc.player != null && entity == mc.player
                && mc.options.getCameraType().isFirstPerson();
    }
    private final Random rand = new Random();
    private final java.util.Map<Integer, arcaneresearch.client.fx.other.FXTimedStream> infusionStreams =
            new java.util.HashMap<>();

    public static void sampleServerClock(long serverNow) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;
        long offset = serverNow - mc.level.getGameTime();
        serverClockWindow.addLast(offset);
        while (serverClockWindow.size() > CLOCK_WINDOW) {
            serverClockWindow.removeFirst();
        }
    }

    public static float serverAge(long startTick, float partialTicks) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return 0.0f;
        long offset = 0L;
        if (!serverClockWindow.isEmpty()) {
            offset = serverClockWindow.getFirst();
            for (long o : serverClockWindow) {
                if (o < offset) offset = o;
            }
        }
        return mc.level.getGameTime() + partialTicks + offset - startTick;
    }

    public Level getWorld() {
        return Minecraft.getInstance().level;
    }

    private ClientLevel getClientLevel() {
        return Minecraft.getInstance().level;
    }

    // NEOFORGE_PORT: BeamEngine.BeamEffect instances (custom geometry: beams, streams,
    // runes, planes) submit through BeamEngine; sprite particles use the vanilla engine.
    private void addParticle(net.minecraft.client.particle.Particle particle) {
        if (particle == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        int ps = mc.options.particles().get().ordinal();
        if (mc.getFps() < 30) {
            ++ps;
        }
        if (mc.level.getRandom().nextInt(3) < ps) {
            return;
        }
        // NEOFORGE_PORT (26.3): Forge gated beams/streams by particle setting too;
        // route to BeamEngine only after the same check.
        if (particle instanceof arcaneresearch.client.fx.BeamEngine.BeamEffect beam) {
            arcaneresearch.client.fx.BeamEngine.add(beam);
            return;
        }
        mc.particleEngine.add(particle);
    }

    private void addParticleDelayed(net.minecraft.client.particle.Particle particle, int delay) {
        if (delay <= 0) {
            addParticle(particle);
        } else {
            ParticleHandler.schedule(() -> addParticle(particle), delay);
        }
    }

    public void drawFireMote(float x, float y, float z, float vx, float vy, float vz,
                             float r, float g, float b, float alpha, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {

            boolean bb = rand.nextBoolean();
            FXFireMote particle = new FXFireMote(level, x, y, z, vx, vy, vz, r, g, b,
                    (bb ? (scale / 3.0f) : scale) * 0.1f, bb ? 1 : 0);
            particle.setAlpha(alpha);
            addParticle(particle);
        }
    }

    public void drawAlumentum(float x, float y, float z, float vx, float vy, float vz,
                              float r, float g, float b, float alpha, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {

            FXFireMote particle = new FXFireMote(level, x, y, z, vx, vy, vz, r, g, b, scale * 0.1f, 1);
            particle.setAlpha(alpha);
            addParticle(particle);
        }
    }

    public void drawTaintParticles(float x, float y, float z, float vx, float vy, float vz, float scale) {
        ClientLevel level = getClientLevel();
        if (level == null) {
            return;
        }
        FXGeneric fb = new FXGeneric(level, x, y, z, vx, vy, vz);
        fb.setMaxAge(80 + level.getRandom().nextInt(20));
        fb.setColor(0.4f + level.getRandom().nextFloat() * 0.2f,
                0.1f + level.getRandom().nextFloat() * 0.3f,
                0.5f + level.getRandom().nextFloat() * 0.2f);
        fb.setAlphaKeyframes(0.75f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(57 + level.getRandom().nextInt(3), 1, 1);
        fb.setScaleKeyframes(scale * 0.1f, scale * 0.025f);
        fb.setLayer(1);
        fb.setSlowDown(0.975);
        fb.setGravity(0.2f);
        fb.setRotationSpeedWithStart(level.getRandom().nextFloat(), level.getRandom().nextBoolean() ? -1.0f : 1.0f);
        addParticle(fb);
    }

    public void drawLightningFlash(double x, double y, double z, float r, float g, float b, float alpha, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
            particle.setMaxAge(5 + rand.nextInt(5));
            particle.setGridSize(16);
            particle.setColor(r, g, b);
            particle.setAlphaKeyframes(alpha, 0.0f);
            particle.setParticles(108 + rand.nextInt(4), 1, 1);

            particle.setScale(scale * 0.1f);
            particle.setLayer(0);
            particle.setRotationSpeedWithStart(rand.nextFloat(), 0.0f);
            addParticle(particle);
        }
    }

    public void spark(double x, double y, double z, float size, float r, float g, float b, float a) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
            particle.setMaxAge(5 + rand.nextInt(5));
            particle.setAlphaF(a);
            particle.setColor(r, g, b);
            particle.setGridSize(16);
            particle.setParticles(8 + rand.nextInt(3) * 16, 8, 1);

            particle.setScale(size * 0.1f);
            particle.setFlipped(rand.nextBoolean());
            addParticle(particle);
        }
    }

    public void sparkle(float x, float y, float z, float r, float g, float b) {
        if (rand.nextInt(6) < 4) {
            drawGenericParticles(x, y, z, 0.0, 0.0, 0.0, r, g, b, 0.9f, true,
                    320, 16, 1, 6 + rand.nextInt(4), 0, 0.6f + rand.nextFloat() * 0.2f, 0.0f, 0);
        }
    }

    public void slimyBubbleFX(float x, float y, float z, float scale, float r, float g, float b) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXSlimyBubble bubble = new FXSlimyBubble(level, x, y, z, scale, r, g, b);
            addParticle(bubble);
        }
    }

    public void drawGenericParticles(double x, double y, double z, double mx, double my, double mz,
                                     float r, float g, float b, float alpha, boolean loop, int start, int num, int inc,
                                     int age, int delay, float scale, float rot, int layer) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, mx, my, mz);
            particle.setColor(r, g, b);
            particle.setAlphaF(alpha);
            particle.setLoop(loop);
            particle.setParticles(start, num, inc);
            particle.setMaxAge(age);

            particle.setScale(scale * 0.1f);
            particle.setRotationSpeed(rot);
            particle.setLayer(layer);
            addParticleDelayed(particle, delay);
        }
    }

    public void drawGenericParticles16(double x, double y, double z, double x2, double y2, double z2,
                                       float r, float g, float b, float alpha, boolean loop, int start, int num, int inc,
                                       int age, int delay, float scale, float rot, int layer) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, x2, y2, z2);
            particle.setGridSize(16);
            particle.setColor(r, g, b);
            particle.setAlphaF(alpha);
            particle.setLoop(loop);
            particle.setParticles(start, num, inc);
            particle.setMaxAge(age);

            particle.setScale(scale * 0.1f);
            particle.setRotationSpeed(rot);
            particle.setLayer(layer);
            addParticleDelayed(particle, delay);
        }
    }

    public void drawGenericParticles(double x, double y, double z, double mx, double my, double mz, GenPart part) {
        ClientLevel level = getClientLevel();
        if (level == null || part == null) {
            return;
        }
        FXGeneric fb = new FXGeneric(level, x, y, z, mx, my, mz);
        fb.setMaxAge(part.age);
        fb.setColorRange(part.redStart, part.greenStart, part.blueStart,
                part.redEnd, part.greenEnd, part.blueEnd);
        fb.setAlphaKeyframes(part.alpha);
        fb.setLoop(part.loop);
        fb.setParticles(part.partStart, part.partNum, part.partInc);
        float[] scaledKeys = new float[part.scale.length];
        for (int si = 0; si < scaledKeys.length; si++) {
            scaledKeys[si] = part.scale[si] * 0.1f;
        }
        fb.setScaleKeyframes(scaledKeys);
        fb.setLayer(part.layer);
        fb.setRotationSpeedWithStart(part.rotstart, part.rot);
        fb.setSlowDown(part.slowDown);
        fb.setGravity(part.grav);
        fb.setGridSize(part.grid);
        fb.setRandomMovementScale((float) part.randX, (float) part.randY, (float) part.randZ);
        addParticleDelayed(fb, part.delay);
    }

    public void crucibleBubble(float x, float y, float z, float cr, float cg, float cb) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        FXGeneric fb = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(15 + level.getRandom().nextInt(10));

        fb.setScale((level.getRandom().nextFloat() * 0.3f + 0.3f) * 0.1f);
        fb.setColor(cr, cg, cb);
        fb.setRandomMovementScale(0.002f, 0.002f, 0.002f);
        fb.setGravity(-0.001f);
        fb.setParticle(64);
        fb.setFinalFrames(65, 66, 66);
        addParticle(fb);
    }

    public void crucibleBoil(BlockPos pos, TileCrucible tile, int j) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        for (int a = 0; a < 2; ++a) {
            FXGeneric fb = new FXGeneric(level,
                    pos.getX() + 0.2f + level.getRandom().nextFloat() * 0.6f,
                    pos.getY() + 0.1f + tile.getFluidHeight(),
                    pos.getZ() + 0.2f + level.getRandom().nextFloat() * 0.6f,
                    0.0, 0.002, 0.0);
            fb.setMaxAge((int) (7.0 + 8.0 / (Math.random() * 0.8 + 0.2)));
            fb.setScale((level.getRandom().nextFloat() * 0.3f + 0.2f) * 0.1f);
            if (tile.aspects.size() == 0) {
                fb.setColor(1.0f, 1.0f, 1.0f);
            } else {
                Aspect[] arr = tile.aspects.getAspects();
                Color color = new Color(arr[level.getRandom().nextInt(arr.length)].getColor());
                fb.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            }
            fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
            fb.setGravity(-0.025f * j);
            fb.setParticle(64);
            fb.setFinalFrames(65, 66);
            addParticle(fb);
        }
    }

    public void crucibleFroth(float x, float y, float z) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        FXGeneric fb = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(4 + level.getRandom().nextInt(3));
        fb.setScale((level.getRandom().nextFloat() * 0.2f + 0.2f) * 0.1f);
        fb.setColor(0.5f, 0.5f, 0.7f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(0.1f);
        fb.setParticle(64);
        fb.setFinalFrames(65, 66);
        addParticle(fb);
    }

    public void crucibleFrothDown(float x, float y, float z) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        FXGeneric fb = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(12 + level.getRandom().nextInt(12));
        fb.setScale((level.getRandom().nextFloat() * 0.2f + 0.4f) * 0.1f);
        fb.setColor(0.25f, 0.0f, 0.75f);
        fb.setAlphaF(0.8f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(0.05f);
        fb.setNoClip(false);
        fb.setParticle(73);
        fb.setFinalFrames(65, 66);
        fb.setLayer(1);
        addParticle(fb);
    }

    public void drawBamf(BlockPos p, boolean sound, boolean flair, Direction side) {
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, sound, flair, side);
    }

    public void drawBamf(BlockPos p, float r, float g, float b, boolean sound, boolean flair, Direction side) {
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, r, g, b, sound, flair, side);
    }

    public void drawBamf(BlockPos p, int color, boolean sound, boolean flair, Direction side) {
        Color c = new Color(color);
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, sound, flair, side);
    }

    public void drawBamf(double x, double y, double z, int color, boolean sound, boolean flair, Direction side) {
        Color c = new Color(color);
        drawBamf(x, y, z, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, sound, flair, side);
    }

    public void drawBamf(double x, double y, double z, boolean sound, boolean flair, Direction side) {
        drawBamf(x, y, z, 0.5f, 0.1f, 0.6f, sound, flair, side);
    }

    public void drawBamf(double x, double y, double z, float r, float g, float b, boolean sound, boolean flair, Direction side) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        if (sound) {
            level.playLocalSound(x, y, z, ModSounds.POOF.get(), SoundSource.BLOCKS,
                    0.4f, 1.0f + (float) rand.nextGaussian() * 0.05f, false);
        }

        for (int a = 0; a < 6 + rand.nextInt(3) + 2; ++a) {
            double vx = (0.05f + rand.nextFloat() * 0.05f) * (rand.nextBoolean() ? -1 : 1);
            double vy = (0.05f + rand.nextFloat() * 0.05f) * (rand.nextBoolean() ? -1 : 1);
            double vz = (0.05f + rand.nextFloat() * 0.05f) * (rand.nextBoolean() ? -1 : 1);
            if (side != null) {
                vx += side.getStepX() * 0.1f;
                vy += side.getStepY() * 0.1f;
                vz += side.getStepZ() * 0.1f;
            }
            FXGeneric fb2 = new FXGeneric(level, x + vx * 2.0, y + vy * 2.0, z + vz * 2.0, vx / 2.0, vy / 2.0, vz / 2.0);
            fb2.setMaxAge(20 + rand.nextInt(15));
            fb2.setColor(Mth.clamp(r * (1.0f + (float) rand.nextGaussian() * 0.1f), 0.0f, 1.0f),
                    Mth.clamp(g * (1.0f + (float) rand.nextGaussian() * 0.1f), 0.0f, 1.0f),
                    Mth.clamp(b * (1.0f + (float) rand.nextGaussian() * 0.1f), 0.0f, 1.0f));
            fb2.setAlphaKeyframes(1.0f, 0.1f);
            fb2.setGridSize(16);
            fb2.setParticles(123, 5, 1);
            fb2.setScaleKeyframes(0.3f, 0.4f + rand.nextFloat() * 0.3f);
            fb2.setLayer(1);
            fb2.setSlowDown(0.7);
            fb2.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(fb2);
        }
        if (flair) {
            for (int a = 0; a < 2 + rand.nextInt(3); ++a) {
                double vx = (0.025f + rand.nextFloat() * 0.025f) * (rand.nextBoolean() ? -1 : 1);
                double vy = (0.025f + rand.nextFloat() * 0.025f) * (rand.nextBoolean() ? -1 : 1);
                double vz = (0.025f + rand.nextFloat() * 0.025f) * (rand.nextBoolean() ? -1 : 1);
                drawWispyMotes(x + vx * 2.0, y + vy * 2.0, z + vz * 2.0, vx, vy, vz, 15 + rand.nextInt(10), -0.01f);
            }
            FXGeneric fb3 = new FXGeneric(level, x, y, z, 0.0, 0.0, 0.0);
            fb3.setMaxAge(10 + rand.nextInt(5));
            fb3.setColor(1.0f, 0.9f, 1.0f);
            fb3.setAlphaKeyframes(1.0f, 0.0f);
            fb3.setGridSize(16);
            fb3.setParticles(77, 1, 1);
            fb3.setScaleKeyframes(1.0f + rand.nextFloat() * 0.2f, 0.0f);
            fb3.setLayer(0);
            fb3.setRotationSpeedWithStart(rand.nextFloat(), (float) rand.nextGaussian());
            addParticle(fb3);
        }
        for (int a = 0; a < (flair ? 2 : 0) + rand.nextInt(3); ++a) {
            drawCurlyWisp(x, y, z, 0.0, 0.0, 0.0, 1.0f,
                    (0.9f + rand.nextFloat() * 0.1f + r) / 2.0f, (0.1f + g) / 2.0f,
                    (0.5f + rand.nextFloat() * 0.1f + b) / 2.0f, 0.75f, side, a, 0, 0);
        }
    }

    public void drawWispyMotesOnBlock(BlockPos pp, int age, float grav) {
        drawWispyMotes(pp.getX() + rand.nextFloat(), pp.getY(), pp.getZ() + rand.nextFloat(),
                0.0, 0.0, 0.0, age,
                0.4f + rand.nextFloat() * 0.6f, 0.6f + rand.nextFloat() * 0.4f, 0.6f + rand.nextFloat() * 0.4f,
                grav);
    }

    public void drawWispyMotes(double x, double y, double z, double vx, double vy, double vz, int age, float grav) {
        drawWispyMotes(x, y, z, vx, vy, vz, age,
                0.25f + rand.nextFloat() * 0.75f, 0.25f + rand.nextFloat() * 0.75f, 0.25f + rand.nextFloat() * 0.75f,
                grav);
    }

    public void drawWispyMotes(double x, double y, double z, double vx, double vy, double vz,
                               int age, float r, float g, float b, float grav) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric mote = new FXGeneric(level, x, y, z, vx, vy, vz);
            mote.setMaxAge((int) (age + (float) age / 2 * rand.nextFloat()));
            mote.setColor(r, g, b);
            mote.setAlphaKeyframes(0.0f, 0.6f, 0.6f, 0.0f);
            mote.setGridSize(64);
            mote.setParticles(512, 16, 1);

            mote.setScaleKeyframes(0.1f, 0.05f);
            mote.setLoop(true);
            mote.setWind(0.001, 0.001);
            mote.setGravity(grav);
            mote.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
            addParticle(mote);
        }
    }

    public void drawWispyMotesEntity(double x, double y, double z, Entity e, float r, float g, float b) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGenericP2E mote = new FXGenericP2E(level, x, y, z, e);
            mote.setColor(r, g, b);
            mote.setAlphaF(0.6f);
            mote.setParticles(512, 16, 1);
            mote.setLoop(true);
            mote.setWind(0.001, 0.001);
            mote.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
            addParticle(mote);
        }
    }

    public void scanHighlight(BlockPos p) {
        Level level = getWorld();
        if (level == null) return;

        VoxelShape shape = level.getBlockState(p).getShape(level, p);
        AABB bb = (shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds()).move(p);
        scanHighlight(bb);
    }

    public void scanHighlight(Entity e) {
        scanHighlight(e.getBoundingBox());
    }

    public void scanHighlight(AABB bb) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        int num = Mth.ceil((float) ((bb.getXsize() + bb.getYsize() + bb.getZsize()) / 3.0) * 2.0f);
        double ax = (bb.minX + bb.maxX) / 2.0;
        double ay = (bb.minY + bb.maxY) / 2.0;
        double az = (bb.minZ + bb.maxZ) / 2.0;
        for (Direction face : Direction.values()) {
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; ++a) {
                double x = mx + rand.nextGaussian() * (bb.maxX - bb.minX);
                double y = my + rand.nextGaussian() * (bb.maxY - bb.minY);
                double z = mz + rand.nextGaussian() * (bb.maxZ - bb.minZ);
                x = Mth.clamp(x, bb.minX - ax, bb.maxX - ax);
                y = Mth.clamp(y, bb.minY - ay, bb.maxY - ay);
                z = Mth.clamp(z, bb.minZ - az, bb.maxZ - az);
                float r = (16 + rand.nextInt(17)) / 255.0f;
                float g = (132 + rand.nextInt(34)) / 255.0f;
                float b = (223 + rand.nextInt(17)) / 255.0f;
                drawSimpleSparkle(rand, ax + x, ay + y, az + z, 0.0, 0.0, 0.0,
                        0.4f + (float) rand.nextGaussian() * 0.1f, r, g, b, rand.nextInt(10), 1.0f, 0.0f, 4);
            }
        }
    }

    public void drawBlockSparkles(BlockPos p, Vec3 start) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        VoxelShape shape = level.getBlockState(p).getShape(level, p);
        AABB bs = (shape.isEmpty()
                ? new AABB(0, 0, 0, 1, 1, 1)
                : shape.bounds()).inflate(0.1, 0.1, 0.1);
        int num = (int) (Math.max(bs.getXsize(), Math.max(bs.getYsize(), bs.getZsize())) * 20.0);
        for (Direction face : Direction.values()) {
            BlockState state = level.getBlockState(p.relative(face));
            if (!state.canOcclude() && !state.isFaceSturdy(level, p.relative(face), face.getOpposite())) {
                boolean rx = face.getStepX() == 0;
                boolean ry = face.getStepY() == 0;
                boolean rz = face.getStepZ() == 0;
                double mx = 0.5 + face.getStepX() * 0.51;
                double my = 0.5 + face.getStepY() * 0.51;
                double mz = 0.5 + face.getStepZ() * 0.51;
                for (int a = 0; a < num * 2; ++a) {
                    double x = mx;
                    double y = my;
                    double z = mz;
                    if (rx) {
                        x += rand.nextGaussian() * 0.6;
                    }
                    if (ry) {
                        y += rand.nextGaussian() * 0.6;
                    }
                    if (rz) {
                        z += rand.nextGaussian() * 0.6;
                    }
                    x = Mth.clamp(x, bs.minX, bs.maxX);
                    y = Mth.clamp(y, bs.minY, bs.maxY);
                    z = Mth.clamp(z, bs.minZ, bs.maxZ);
                    float r = 1.0f;
                    float g = (189 + rand.nextInt(67)) / 255.0f;
                    float b = (64 + rand.nextInt(192)) / 255.0f;
                    Vec3 v1 = new Vec3(p.getX() + x, p.getY() + y, p.getZ() + z);
                    double delay = rand.nextInt(5) + v1.distanceTo(start) * 16.0;
                    drawSimpleSparkle(rand, p.getX() + x, p.getY() + y, p.getZ() + z,
                            0.0, 0.0025, 0.0, 0.4f + (float) rand.nextGaussian() * 0.1f,
                            r, g, b, (int) delay, 1.0f, 0.01f, 16);
                }
            }
        }
    }

    public void drawSimpleSparkle(Random rand, double x, double y, double z, double x2, double y2, double z2,
                                  float scale, float r, float g, float b, int delay, float decay, float grav, int baseAge) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            boolean sp = rand.nextFloat() < 0.2;
            FXGeneric sparkle = new FXGeneric(level, x, y, z, x2, y2, z2);
            int age = baseAge * 4 + rand.nextInt(Math.max(baseAge, 1));
            sparkle.setMaxAge(age);
            sparkle.setColor(r, g, b);
            float[] alphas = new float[6 + rand.nextInt(Math.max(age / 3, 1))];
            for (int a = 1; a < alphas.length - 1; ++a) {
                alphas[a] = rand.nextFloat();
            }
            sparkle.setAlphaKeyframes(alphas);
            sparkle.setParticles(sp ? 320 : 512, 16, 1);
            sparkle.setLoop(true);
            sparkle.setGravity(grav);

            sparkle.setScaleKeyframes(scale * 0.1f, scale * 0.2f);
            sparkle.setLayer(0);
            sparkle.setSlowDown(decay);
            sparkle.setRandomMovementScale(0.0005f, 0.001f, 0.0005f);
            sparkle.setWind(0.0005, 0.0005);
            addParticleDelayed(sparkle, delay);
        }
    }

    public void drawLineSparkle(Random rand, double x, double y, double z, double x2, double y2, double z2,
                                float scale, float r, float g, float b, int delay, float decay, float grav, int baseAge) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            boolean sp = rand.nextFloat() < 0.2;
            FXGeneric sparkle = new FXGeneric(level, x, y, z, x2, y2, z2);
            int age = baseAge * 4 + rand.nextInt(Math.max(baseAge, 1));
            sparkle.setMaxAge(age);
            sparkle.setColor(r, g, b);
            sparkle.setAlphaKeyframes(0.0f, 1.0f, 0.0f);
            sparkle.setParticles(sp ? 320 : 512, 16, 1);
            sparkle.setLoop(true);
            sparkle.setGravity(grav);

            sparkle.setScaleKeyframes(scale * 0.1f, scale * 0.2f, scale * 0.1f);
            sparkle.setLayer(0);
            sparkle.setSlowDown(decay);
            sparkle.setRandomMovementScale(0.00005f, 0.0f, 0.00005f);
            addParticleDelayed(sparkle, delay);
        }
    }

    public void drawBlockMistParticles(BlockPos p, int c) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        VoxelShape mistShape = level.getBlockState(p).getShape(level, p);
        var bs = mistShape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : mistShape.bounds();
        Color color = new Color(c);
        for (int a = 0; a < 8; ++a) {
            double x = p.getX() + bs.minX + rand.nextFloat() * (bs.maxX - bs.minX);
            double y = p.getY() + bs.minY + rand.nextFloat() * (bs.maxY - bs.minY);
            double z = p.getZ() + bs.minZ + rand.nextFloat() * (bs.maxZ - bs.minZ);
            FXGeneric particle = new FXGeneric(level, x, y, z,
                    rand.nextGaussian() * 0.01, rand.nextFloat() * 0.075, rand.nextGaussian() * 0.01);
            particle.setMaxAge(50 + rand.nextInt(25));
            particle.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            particle.setAlphaKeyframes(0.0f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f);
            particle.setGridSize(16);
            particle.setParticles(56, 1, 1);

            particle.setScaleKeyframes(0.5f, 0.1f);
            particle.setLayer(0);
            particle.setSlowDown(1.0);
            particle.setGravity(0.1f);
            particle.setWind(0.001, 0.001);
            particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(particle);
        }
    }

    public void drawBlockMistParticlesFlat(BlockPos p, int c) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        Color color = new Color(c);
        for (int a = 0; a < 6; ++a) {
            double x = p.getX() + rand.nextFloat();
            double y = p.getY() + rand.nextFloat() * 0.125f;
            double z = p.getZ() + rand.nextFloat();
            FXGeneric particle = new FXGeneric(level, x, y, z,
                    (rand.nextFloat() - rand.nextFloat()) * 0.005, 0.005, (rand.nextFloat() - rand.nextFloat()) * 0.005);
            particle.setMaxAge(400 + rand.nextInt(100));
            particle.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            particle.setAlphaKeyframes(1.0f, 0.0f);
            particle.setGridSize(8);
            particle.setParticles(24, 1, 1);

            particle.setScaleKeyframes(0.2f, 0.5f);
            particle.setLayer(0);
            particle.setSlowDown(1.0);
            particle.setWind(0.001, 0.001);
            particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(particle);
        }
    }

    public void drawFocusCloudParticle(double x, double y, double z, double mx, double my, double mz, int c) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        Color color = new Color(c);
        FXGeneric particle = new FXGeneric(level, x, y, z, mx, my, mz);
        particle.setMaxAge(20 + rand.nextInt(10));
        particle.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        particle.setAlphaKeyframes(0.0f, 0.66f, 0.0f);
        particle.setGridSize(16);
        particle.setParticles(56 + rand.nextInt(4), 1, 1);

        particle.setScaleKeyframes(0.5f + rand.nextFloat() * 0.1f, 1.0f + rand.nextFloat() * 0.1f);
        particle.setLayer(0);
        particle.setSlowDown(0.99);
        particle.setWind(0.001, 0.001);
        particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -0.25f : 0.25f);
        addParticle(particle);
    }

    public void visSparkle(int x, int y, int z, int x2, int y2, int z2, int color) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            Color c = new Color(color);
            FXVisSparkle particle = new FXVisSparkle(level,
                    x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(),
                    x2 + 0.4 + rand.nextFloat() * 0.2f, y2 + 0.4 + rand.nextFloat() * 0.2f,
                    z2 + 0.4 + rand.nextFloat() * 0.2f);
            particle.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
            addParticle(particle);
        }
    }

    public void drawLevitatorParticles(double x, double y, double z, double x2, double y2, double z2) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, x2, y2, z2);
            particle.setMaxAge(200 + rand.nextInt(100));
            particle.setColor(0.5f, 0.5f, 0.2f);
            particle.setAlphaKeyframes(0.3f, 0.0f);
            particle.setGridSize(16);
            particle.setParticles(56, 1, 1);

            particle.setScaleKeyframes(0.2f, 0.5f);
            particle.setLayer(0);
            particle.setSlowDown(1.0);
            particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(particle);
        }
    }

    public void drawStabilizerParticles(double x, double y, double z, double x2, double y2, double z2, int life) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, x2, y2, z2);
            particle.setMaxAge(life + rand.nextInt(life));
            particle.setColor(0.5f, 0.2f, 0.5f);
            particle.setAlphaKeyframes(0.3f, 0.0f);
            particle.setGridSize(16);
            particle.setParticles(72 + rand.nextInt(4), 1, 1);

            particle.setScaleKeyframes(0.1f, 1.0f);
            particle.setLayer(0);
            particle.setSlowDown(1.01);
            particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(particle);
        }
    }

    public void drawGolemFlyParticles(double x, double y, double z, double x2, double y2, double z2) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        try {
            FXGeneric particle = new FXGeneric(level, x, y, z, x2, y2, z2);
            particle.setMaxAge(20 + rand.nextInt(5));
            particle.setAlphaKeyframes(0.3f, 0.0f);
            particle.setGridSize(16);
            particle.setParticles(56, 1, 1);

            particle.setScaleKeyframes(0.15f, 0.3f, 0.8f);
            particle.setLayer(0);
            particle.setSlowDown(1.0);
            particle.setWind(0.001, 0.001);
            particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
            addParticle(particle);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void drawPollutionParticles(BlockPos p) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        float x = p.getX() + 0.2f + rand.nextFloat() * 0.6f;
        float y = p.getY() + 0.2f + rand.nextFloat() * 0.6f;
        float z = p.getZ() + 0.2f + rand.nextFloat() * 0.6f;
        FXGeneric particle = new FXGeneric(level, x, y, z,
                (rand.nextFloat() - rand.nextFloat()) * 0.005, 0.02, (rand.nextFloat() - rand.nextFloat()) * 0.005);
        particle.setMaxAge(100 + rand.nextInt(60));
        particle.setColor(1.0f, 0.3f, 0.9f);
        particle.setAlphaKeyframes(0.5f, 0.0f);
        particle.setGridSize(16);
        particle.setParticles(56, 1, 1);

        particle.setScaleKeyframes(0.2f, 0.5f);
        particle.setLayer(1);
        particle.setSlowDown(1.0);
        particle.setWind(0.001, 0.001);
        particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? -1.0f : 1.0f);
        addParticle(particle);
    }

    public void infusionStreamFx(int streamId, BlockPos source, BlockPos target, int color,
                                 int duration, long startTick, long serverNow,
                                 java.util.List<net.minecraft.world.phys.Vec3> waypoints) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        sampleServerClock(serverNow);
        arcaneresearch.client.fx.other.FXTimedStream existing = infusionStreams.get(streamId);
        if (waypoints == null || waypoints.size() < 2 || duration < 1) {
            if (existing != null) {
                existing.remove();
                infusionStreams.remove(streamId);
            }
            return;
        }
        if (existing != null && existing.isAlive()) {
            existing.update(waypoints, duration, startTick);
            return;
        }
        if (infusionStreams.size() > 64) {
            infusionStreams.values().removeIf(tube -> !tube.isAlive());
        }
        arcaneresearch.client.fx.other.FXTimedStream stream =
                new arcaneresearch.client.fx.other.FXTimedStream(level, waypoints, color, duration, startTick);
        infusionStreams.put(streamId, stream);
        addParticle(stream);
    }

    public void essentiaTrailFx(BlockPos p1, BlockPos p2, int count, int color, float scale, int ext) {
        ClientLevel level = getClientLevel();
        if (level == null) return;
        FXEssentiaStream stream = new FXEssentiaStream(level,
                p1.getX() + 0.5D, p1.getY() + 0.5D, p1.getZ() + 0.5D,
                p2.getX() + 0.5D, p2.getY() + 0.5D, p2.getZ() + 0.5D,
                count, color, scale, ext, 0.0D);
        addParticle(stream);
    }

    public void essentiaDropFx(double x, double y, double z, float r, float g, float b, float alpha) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric drop = new FXGeneric(level, x, y, z,
                    rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005);
            drop.setMaxAge(20 + rand.nextInt(10));
            drop.setColor(r, g, b);
            drop.setAlphaF(alpha);
            drop.setLoop(false);
            drop.setParticles(25, 1, 1);

            drop.setScaleKeyframes(0.04f + rand.nextFloat() * 0.02f, 0.02f);
            drop.setLayer(1);
            drop.setGravity(0.01f);
            drop.setRotationSpeed(0.0f);
            addParticle(drop);
        }
    }

    public void drawVentParticles(double x, double y, double z, double x2, double y2, double z2, int color) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXVent vent = new FXVent(level, x, y, z, x2, y2, z2, color);
            vent.setAlpha(0.4f);
            addParticle(vent);
        }
    }

    public void drawVentParticles(double x, double y, double z, double x2, double y2, double z2, int color, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXVent vent = new FXVent(level, x, y, z, x2, y2, z2, color);
            vent.setAlpha(0.4f);
            vent.setScale(scale);
            addParticle(vent);
        }
    }

    public void drawVentParticles2(double x, double y, double z, double x2, double y2, double z2, int color, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXVent2 vent = new FXVent2(level, x, y, z, x2, y2, z2, color);
            vent.setTCAlpha(0.4f);
            vent.setScale(scale);
            addParticle(vent);
            if (rand.nextInt(6) < 2) {
                drawGenericParticles(x, y, z, x2 / 2.0, y2 / 2.0, z2 / 2.0, 1.0f, 0.7f, 0.2f, 0.9f,
                        true, 320, 16, 1, 10 + rand.nextInt(4), 0, 0.25f + rand.nextFloat() * 0.1f, 0.0f, 0);
            }
        }
    }

    public void jarSplashFx(double x, double y, double z) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric drop = new FXGeneric(level,
                    x + rand.nextGaussian() * 0.075, y, z + rand.nextGaussian() * 0.075,
                    rand.nextGaussian() * 0.015, 0.075f + rand.nextFloat() * 0.05f, rand.nextGaussian() * 0.015);
            drop.setMaxAge(20 + rand.nextInt(10));
            Color c = new Color(2650102);
            drop.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
            drop.setAlphaF(0.5f);
            drop.setLoop(false);
            drop.setParticles(73, 1, 1);

            drop.setScaleKeyframes(0.04f + rand.nextFloat() * 0.03f, 0.0f);
            drop.setLayer(1);
            drop.setGravity(0.3f);
            drop.setRotationSpeed(0.0f);
            addParticle(drop);
        }
    }

    public void waterTrailFx(BlockPos p1, BlockPos p2, int count, int color, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXEssentiaStream stream = new FXEssentiaStream(level,
                    p1.getX() + 0.5, p1.getY() + 0.66, p1.getZ() + 0.5,
                    p2.getX() + 0.5, p2.getY() + 0.5, p2.getZ() + 0.5,
                    count, color, scale, 0, 0.2);
            addParticle(stream);
        }
    }

    public void drawInfusionParticles1(double x, double y, double z, BlockPos pos, ItemStack stack) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBoreParticles particle = new FXBoreParticles(level, x, y, z,
                    pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5,
                    (float) rand.nextGaussian() * 0.03f, (float) rand.nextGaussian() * 0.03f,
                    (float) rand.nextGaussian() * 0.03f, stack).getObjectColor(pos);
            particle.setTCAlpha(0.3f);
            addParticle(particle);
        }
    }

    public void drawInfusionParticles2(double x, double y, double z, BlockPos pos, BlockState state, int md) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBoreParticles particle = new FXBoreParticles(level, x, y, z,
                    pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5, state, md).getObjectColor(pos);
            particle.setTCAlpha(0.3f);
            addParticle(particle);
        }
    }

    public void drawInfusionParticles3(double x, double y, double z, int x2, int y2, int z2) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBoreSparkle particle = new FXBoreSparkle(level, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
            particle.setColor(0.4f + rand.nextFloat() * 0.2f, 0.2f, 0.6f + rand.nextFloat() * 0.3f);
            addParticle(particle);
        }
    }

    public void drawInfusionParticles4(double x, double y, double z, int x2, int y2, int z2) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBoreSparkle particle = new FXBoreSparkle(level, x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
            particle.setColor(0.2f, 0.6f + rand.nextFloat() * 0.3f, 0.3f);
            addParticle(particle);
        }
    }

    public void arcLightning(double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, float h) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            if (h <= 0.0f) {
                h = 0.1f;
            }
            FXArc arc = new FXArc(level, x, y, z, tx, ty, tz, r, g, b, h);
            addParticle(arc);
        }
    }

    public void arcBolt(double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, float width) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBolt bolt = new FXBolt(level, x, y, z, tx, ty, tz, r, g, b, width);

            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                arcaneresearch.client.fx.BeamEngine.add(bolt);
            }
        }
    }

    /**
     * Дуга разряда ноды, порт TC4 {@code nodeBolt}: фрактальная молния
     * (seed с сервера, duration 10, multi 4.0, type 0), а не простой зигзаг.
     */
    public void nodeBolt(double x1, double y1, double z1, double x2, double y2, double z2,
                         long seed) {
        ClientLevel level = getClientLevel();
        if (level == null) {
            return;
        }
        arcaneresearch.client.fx.beams.FXLightning bolt =
                new arcaneresearch.client.fx.beams.FXLightning(
                        level, x1, y1, z1, x2, y2, z2, seed, 10, 4.0F);
        bolt.defaultFractal();
        bolt.setType(0);
        bolt.finalizeBolt();
        addParticle(bolt);
    }

    public Object beamCont(LivingEntity p, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, Object input, int impact) {
        ClientLevel level = getClientLevel();
        if (level == null) return null;

        FXBeamWand beam = null;
        Color c = new Color(color);

        if (input instanceof FXBeamWand existingBeam) {
            beam = existingBeam;
        }

        if (beam == null || !beam.isAlive()) {
            beam = new FXBeamWand(level, p, tx, ty, tz,
                    c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 8);
            beam.setType(type);
            beam.setEndMod(endmod);
            beam.setReverse(reverse);
            addParticle(beam);
        } else {

            beam.updateBeam(tx, ty, tz);
            beam.setEndMod(endmod);
            beam.impact = impact;
        }

        return beam;
    }

    public Object beamBore(double px, double py, double pz, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, Object input, int impact) {
        ClientLevel level = getClientLevel();
        if (level == null) return null;

        FXBeamBore beam = null;
        Color c = new Color(color);

        if (input instanceof FXBeamBore existingBeam) {
            beam = existingBeam;
        }

        if (beam == null || !beam.isAlive()) {
            beam = new FXBeamBore(level, px, py, pz, tx, ty, tz,
                    c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 8);
            beam.setType(type);
            beam.setEndMod(endmod);
            beam.setReverse(reverse);
            addParticle(beam);
        } else {

            beam.updateBeam(px, py, pz, tx, ty, tz);
            beam.setEndMod(endmod);
            beam.impact = impact;
        }

        return beam;
    }

    public void burst(double sx, double sy, double sz, float size) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, sx, sy, sz, 0.0, 0.0, 0.0);
            particle.setMaxAge(31);
            particle.setGridSize(16);
            particle.setParticles(208, 31, 1);

            particle.setScale(size * 0.1f);
            addParticle(particle);
        }
    }

    public void excavateFX(BlockPos pos, LivingEntity p, int progress) {

        ClientLevel level = getClientLevel();
        if (level != null && p != null && pos != null) {
            level.destroyBlockProgress(p.getId(), pos, progress);
        }
    }

    public void blockRunes(double x, double y, double z, float r, float g, float b, int dur, float grav) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBlockRunes runes = new FXBlockRunes(level, x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
            runes.setGravity(grav);
            addParticle(runes);
        }
    }

    public void blockRunes2(double x, double y, double z, float r, float g, float b, int dur, float grav) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBlockRunes runes = new FXBlockRunes(level, x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
            runes.setGravity(grav);
            runes.setScale((float) (0.5 + rand.nextGaussian() * 0.1));
            runes.setOffsetX(0.0);
            addParticle(runes);
        }
    }

    public void drawPedestalShield(BlockPos pos) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXShieldRunes shield = new FXShieldRunes(level,
                    pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, null, 8, 0.0F, 90.0F);
            addParticle(shield);
        }
    }

    public void blockWard(double x, double y, double z, Direction side, float r, float g, float b) {
        ClientLevel level = getClientLevel();
        if (level != null) {

            FXBlockWard ward = new FXBlockWard(level, x + 0.5, y + 0.5, z + 0.5, side, r, g, b);
            addParticle(ward);
        }
    }

    public void smokeSpiral(double x, double y, double z, float rad, int start, int miny, int color) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            Color c = new Color(color);
            FXSmokeSpiral spiral = new FXSmokeSpiral(level, x, y, z, rad, start, miny);
            spiral.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
            addParticle(spiral);
        }
    }

    public void drawCurlyWisp(double x, double y, double z, double vx, double vy, double vz,
                              float scale, float r, float g, float b, float a, Direction side, int seed, int layer, int delay) {
        ClientLevel level = getClientLevel();
        if (level == null) {
            return;
        }
        vx += (0.0025f + rand.nextFloat() * 0.005f) * (rand.nextBoolean() ? -1 : 1);
        vy += (0.0025f + rand.nextFloat() * 0.005f) * (rand.nextBoolean() ? -1 : 1);
        vz += (0.0025f + rand.nextFloat() * 0.005f) * (rand.nextBoolean() ? -1 : 1);
        if (side != null) {
            vx += side.getStepX() * 0.025f;
            vy += side.getStepY() * 0.025f;
            vz += side.getStepZ() * 0.025f;
        }
        FXGeneric particle = new FXGeneric(level, x + vx * 5.0, y + vy * 5.0, z + vz * 5.0, vx, vy, vz);
        if (seed > 0 && rand.nextBoolean()) {
            particle.setAngles(90.0f * (float) rand.nextGaussian(), 90.0f * (float) rand.nextGaussian());
        }
        particle.setMaxAge(25 + rand.nextInt(20 + 20 * seed));
        particle.setColorRange(r, g, b, 0.1f, 0.0f, 0.1f);
        particle.setAlphaKeyframes(a, 0.0f);
        particle.setGridSize(16);
        particle.setParticles(60 + rand.nextInt(4), 1, 1);

        particle.setScaleKeyframes(0.5f * scale, (1.0f + rand.nextFloat() * 0.4f) * scale);
        particle.setLayer(layer);
        particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? (-2.0f - rand.nextFloat() * 2.0f) : (2.0f + rand.nextFloat() * 2.0f));
        addParticleDelayed(particle, delay);
    }

    public void voidStreak(double x, double y, double z, double x2, double y2, double z2, int seed, float scale) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXVoidStream particle = new FXVoidStream(level, x, y, z, x2, y2, z2, seed, scale);
            addParticle(particle);
        }
    }

    public void furnaceLavaFx(int x, int y, int z, int facingX, int facingZ) {
        Level level = getWorld();
        if (level == null) return;
        float qx = (facingX == 0) ? ((rand.nextFloat() - rand.nextFloat()) * 0.5f) : (facingX * rand.nextFloat());
        float qz = (facingZ == 0) ? ((rand.nextFloat() - rand.nextFloat()) * 0.5f) : (facingZ * rand.nextFloat());
        level.addParticle(ParticleTypes.LAVA,
                x + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.3f + facingX * 1.0f,
                y + 0.3f,
                z + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.3f + facingZ * 1.0f,
                0.15f * qx, 0.2f * rand.nextFloat(), 0.15f * qz);
    }

    public void bottleTaintBreak(double x, double y, double z) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        ItemStack shard = new ItemStack(ModItems.BOTTLE_TAINT.get());
        for (int k = 0; k < 8; ++k) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, shard.getItem()), x, y, z,
                    rand.nextGaussian() * 0.15, rand.nextDouble() * 0.2, rand.nextGaussian() * 0.15);
        }
        level.playLocalSound(x, y, z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL,
                1.0f, rand.nextFloat() * 0.1f + 0.9f, false);
    }

    public void cultistSpawn(double x, double y, double z, double a, double b, double c) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, a, b, c);
            particle.setMaxAge(10 + rand.nextInt(10));
            particle.setColorRange(1.0f, 1.0f, 1.0f, 0.6f, 0.0f, 0.0f);
            particle.setAlphaF(0.8f);
            particle.setGridSize(16);
            particle.setParticles(160, 6, 1);

            particle.setScale(0.3f + rand.nextFloat() * 0.2f);
            particle.setLayer(1);
            addParticle(particle);
        }
    }

    public void pechsCurseTick(double posX, double posY, double posZ) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        FXGeneric particle = new FXGeneric(level, posX, posY, posZ, 0.0, 0.0, 0.0);
        particle.setAngles(90.0f * (float) rand.nextGaussian(), 90.0f * (float) rand.nextGaussian());
        particle.setMaxAge(50 + rand.nextInt(50));
        particle.setColorRange(0.9f, 0.1f, 0.5f, 0.1f + rand.nextFloat() * 0.1f, 0.0f, 0.5f + rand.nextFloat() * 0.1f);
        particle.setAlphaKeyframes(0.75f, 0.0f);
        particle.setGridSize(8);
        particle.setParticles(28 + rand.nextInt(4), 1, 1);

        particle.setScaleKeyframes(0.3f, 0.5f + rand.nextFloat() * 0.2f);
        particle.setLayer(0);
        particle.setRotationSpeedWithStart(rand.nextFloat(), rand.nextBoolean() ? (-3.0f - rand.nextFloat() * 3.0f) : (3.0f + rand.nextFloat() * 3.0f));
        addParticle(particle);
        drawWispyMotes(posX, posY, posZ, 0.0, 0.0, 0.0, 10 + rand.nextInt(10), -0.01f);
    }

    public void wispFXEG(double posX, double posY, double posZ, Entity target) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            for (int a = 0; a < 2; ++a) {
                FXWisp particle = new FXWisp(level, posX, posY, posZ, target);
                addParticle(particle);
            }
        }
    }

    public void drawWispParticles(double x, double y, double z, double x2, double y2, double z2, int color, int a) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, x2, y2, z2);
            particle.setMaxAge(10 + rand.nextInt(5));
            Color c = new Color(color);
            particle.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
            particle.setAlphaF(0.5f);
            particle.setLoop(true);
            particle.setGridSize(64);
            particle.setParticles(264, 8, 1);

            particle.setScaleKeyframes(0.1f + rand.nextFloat() * 0.025f, 0.005f);
            particle.setWind(0.00025, 0.00025);
            particle.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
            addParticleDelayed(particle, a);
        }
    }

    public void drawSlash(double x, double y, double z, double x2, double y2, double z2, int dur) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXPlane slash = new FXPlane(level, x, y, z, x2, y2, z2, dur);
            addParticle(slash);
        }
    }

    public void boreDigFx(int x, int y, int z, Entity e, BlockState bi, int md, int delay) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        float p = 50.0f;
        for (int a = 0; a < p / delay; a++) {
            if (rand.nextInt(4) == 0) {

                FXBoreSparkle sparkle = new FXBoreSparkle(level,
                        x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), e);
                addParticle(sparkle);
            } else {

                FXBoreParticles debris = new FXBoreParticles(level,
                        x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(),
                        e.getX(), e.getY(), e.getZ(), bi, md);
                debris.setTarget(e);
                addParticle(debris);
            }
        }
    }

    public void sonicBoom(double x, double y, double z, Entity source, int duration) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXSonic boom = new FXSonic(level, x, y, z, source, duration);
            addParticle(boom);
        }
    }

    public void boreTrailFx(BlockPos p1, Entity e, int count, int color, float scale, int ext) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXBoreStream stream = new FXBoreStream(level,
                    p1.getX() + 0.5, p1.getY() + 0.5, p1.getZ() + 0.5,
                    e, count, color, scale, ext, 0.0);
            addParticle(stream);
        }
    }

    public void splooshFX(Entity e) {
        Level level = getWorld();
        if (level != null) {
            level.addParticle(ParticleTypes.WITCH, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(),
                    rand.nextGaussian() * 0.1, rand.nextGaussian() * 0.1, rand.nextGaussian() * 0.1);
        }
    }

    public void taintsplosionFX(Entity e) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        for (int i = 0; i < 8; i++) {
            double px = e.getX() + rand.nextGaussian() * 0.3;
            double py = e.getY() + rand.nextFloat() * e.getBbHeight();
            double pz = e.getZ() + rand.nextGaussian() * 0.3;

            FXTaintParticle particle = new FXTaintParticle(level, px, py, pz,
                    rand.nextGaussian() * 0.15, rand.nextGaussian() * 0.15, rand.nextGaussian() * 0.15,
                    0.5f + rand.nextFloat() * 0.5f);
            addParticle(particle);
        }
    }

    public void tentacleAriseFX(Entity e) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        float h = e.getBbHeight();
        for (int j = 0; j < 2.0f * h; ++j) {
            float f = rand.nextFloat() * 3.1415927f * h;
            float f2 = rand.nextFloat() * 0.5f + 0.5f;
            float f3 = Mth.sin(f) * h * 0.25f * f2;
            float f4 = Mth.cos(f) * h * 0.25f * f2;
            FXBreakingFade fx = new FXBreakingFade(level,
                    e.getX() + f3, e.getY(), e.getZ() + f4, Items.SLIME_BALL);
            fx.setColor(0.4f, 0.0f, 0.4f, 0.4f, 0.0f, 0.4f);
            fx.setTCAlpha(0.5f);
            fx.setParticleMaxAge((int) (66.0f / (rand.nextFloat() * 0.9f + 0.1f)));
            addParticle(fx);
            if (!level.getBlockState(e.blockPosition().below()).isAir()) {
                f = rand.nextFloat() * 3.1415927f * h;
                f2 = rand.nextFloat() * 0.5f + 0.5f;
                f3 = Mth.sin(f) * h * 0.25f * f2;
                f4 = Mth.cos(f) * h * 0.25f * f2;
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK,
                                level.getBlockState(e.blockPosition().below())),
                        e.getX() + f3, e.getY(), e.getZ() + f4, 0.0, 0.0, 0.0);
            }
        }
    }

    public void slimeJumpFX(Entity e, int size) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        int count = 2 + size;
        for (int i = 0; i < count; i++) {
            double px = e.getX() + rand.nextGaussian() * 0.2 * size;
            double py = e.getY() + e.getBbHeight() / 2;
            double pz = e.getZ() + rand.nextGaussian() * 0.2 * size;

            FXTaintParticle particle = new FXTaintParticle(level, px, py, pz,
                    rand.nextGaussian() * 0.05, 0.05 + rand.nextFloat() * 0.05, rand.nextGaussian() * 0.05,
                    0.3f + rand.nextFloat() * 0.2f * size);
            addParticle(particle);
        }
    }

    public void taintLandFX(Entity e) {
        ClientLevel level = getClientLevel();
        if (level == null) return;

        for (int i = 0; i < 4; i++) {
            double angle = rand.nextFloat() * Math.PI * 2;
            double speed = 0.05 + rand.nextFloat() * 0.05;

            FXTaintParticle particle = new FXTaintParticle(level, e.getX(), e.getY() + 0.1, e.getZ(),
                    Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed,
                    0.3f + rand.nextFloat() * 0.2f);
            addParticle(particle);
        }
    }

    public FXSwarm swarmParticleFX(Entity targetedEntity, float speed, float turnSpeed, float pg) {
        ClientLevel level = getClientLevel();
        if (level != null) {

            double x = targetedEntity.getX() + (rand.nextFloat() - rand.nextFloat()) * 2.0f;
            double y = targetedEntity.getY() + (rand.nextFloat() - rand.nextFloat()) * 2.0f;
            double z = targetedEntity.getZ() + (rand.nextFloat() - rand.nextFloat()) * 2.0f;

            float r = 0.8f + rand.nextFloat() * 0.2f;
            float g = rand.nextFloat() * 0.4f;
            float b = 1.0f - rand.nextFloat() * 0.2f;

            FXSwarm swarm = new FXSwarm(level, x, y, z, targetedEntity, r, g, b, speed, turnSpeed, pg);
            addParticle(swarm);
            return swarm;
        }
        return null;
    }

    public void drawNitorCore(double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            FXGeneric particle = new FXGeneric(level, x, y, z, vx, vy, vz);
            particle.setMaxAge(10);
            particle.setColor(1.0f, 1.0f, 1.0f);
            particle.setAlphaF(1.0f);
            particle.setParticles(457, 1, 1);

            particle.setScaleKeyframes(0.1f, 0.1f + (float) rand.nextGaussian() * 0.01f, 0.1f);

            particle.setLayer(0);
            particle.setRandomMovementScale(0.0002f, 0.0002f, 0.0002f);
            addParticle(particle);
        }
    }

    public void drawNitorFlames(double x, double y, double z, double vx, double vy, double vz, int color, int delay) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            Color c = new Color(color);
            FXGeneric particle = new FXGeneric(level, x, y, z, vx, vy, vz);
            particle.setMaxAge(10 + rand.nextInt(5));
            particle.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
            particle.setAlphaF(0.66f);
            particle.setLoop(true);
            particle.setGridSize(64);
            particle.setParticles(264, 8, 1);

            particle.setScaleKeyframes(0.3f + rand.nextFloat() * 0.1f, 0.005f);
            particle.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
            addParticleDelayed(particle, delay);
        }
    }

    public void drawSimpleSparkleGui(net.minecraft.util.RandomSource rand, double x, double y, double x2, double y2,
                                     float scale, float r, float g, float b, int delay, float decay, float grav) {
        ClientLevel level = getClientLevel();
        if (level != null) {
            boolean sp = rand.nextFloat() < 0.2;
            FXGenericGui sparkle = new FXGenericGui(level, x, y, 0.0, x2, y2, 0.0);
            sparkle.setMaxAge(32 + rand.nextInt(8));
            sparkle.setColor(r, g, b);
            sparkle.setAlphaKeyframes(0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f);
            sparkle.setParticles(sp ? 320 : 512, 16, 1);
            sparkle.setLoop(true);
            sparkle.setGravity(grav);
            sparkle.setScaleKeyframes(scale, scale * 2.0f);
            sparkle.setNoClip(false);
            sparkle.setLayer(4);
            sparkle.setSlowDown(decay);
            sparkle.setRandomMovementScale(0.025f, 0.025f, 0.0f);
            addParticleDelayed(sparkle, delay);
        }
    }

    // NEOFORGE_PORT: side-split bridges. Called from common code behind
    // isClientSide()/CLIENTBOUND-flow guards. Signatures use only server-safe
    // types, so dedicated-server verification of the CALLERS never loads client
    // classes (the bodies below only ever link on the client).
    public void spawnResearchTableSparkle(Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        ClientLevel clientLevel = getClientLevel();
        if (clientLevel == null) return;
        double xx = random.nextGaussian() / 2.0;
        double zz = random.nextGaussian() / 2.0;
        double yy = 1.5 + random.nextFloat();
        int a = 40 + random.nextInt(20);
        FXGeneric fb = new FXGeneric(
                clientLevel,
                pos.getX() + 0.5 + xx, pos.getY() + yy, pos.getZ() + 0.5 + zz,
                -xx / a, -(yy - 0.85) / a, -zz / a);
        fb.setMaxAge(a);
        fb.setColor(0.5f + random.nextFloat() * 0.5f,
                0.5f + random.nextFloat() * 0.5f,
                0.5f + random.nextFloat() * 0.5f);
        fb.setAlphaKeyframes(0.0f, 0.25f, 0.5f, 0.75f, 0.0f);
        fb.setParticles(384 + random.nextInt(16), 1, 1);
        fb.setScaleKeyframes((0.8f + random.nextFloat() * 0.3f) * 0.1f, 0.3f * 0.1f);
        fb.setLayer(0);
        Minecraft.getInstance().particleEngine.add(fb);
    }

    public void drawShieldRunes(Level level, Entity entity, float yaw, float pitch) {
        if (!(level instanceof ClientLevel clientLevel)) return;
        FXShieldRunes fb = new FXShieldRunes(
                clientLevel, entity.getX(), entity.getY(), entity.getZ(), entity, 8, yaw, pitch);
        BeamEngine.add(fb);
    }

    public void markChunkSectionDirty(Level level, BlockPos pos) {
        if (level instanceof ClientLevel clientLevel) {
            clientLevel.setSectionDirtyWithNeighbors(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        }
    }

    public void drawScanPing(Level level, double x, double y, double z,
                             float rf, float gf, float bf, float q, double dis) {
        if (!(level instanceof ClientLevel clientLevel)) return;
        FXGeneric fb = new FXGeneric(clientLevel, x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(44);
        fb.setColor(rf, gf, bf);
        fb.setAlphaKeyframes(0.0f, 1.0f, 0.8f, 0.0f);
        fb.setParticles(240, 15, 1);
        fb.setGridSize(16);
        fb.setLoop(true);
        fb.setScale(9.0f * 0.1f);
        fb.setLayer(q < 0.25f ? 3 : 2);
        fb.setRotationSpeed(0.0f);
        ParticleEngine.addEffectWithDelay(clientLevel, fb, (int) (dis * 3.0));
    }

    public static class GenPart {        public int grid;
        public int age;
        public float redStart, greenStart, blueStart;
        public float redEnd, greenEnd, blueEnd;
        public float[] alpha;
        public float[] scale;
        public float rot;
        public float rotstart;
        public boolean loop;
        public int partStart, partNum, partInc;
        public int layer;
        public double slowDown;
        public float grav;
        public double randX, randY, randZ;
        public int delay;

        public GenPart() {
            grid = 64;
            age = 0;
            redStart = 1.0f;
            greenStart = 1.0f;
            blueStart = 1.0f;
            redEnd = 1.0f;
            greenEnd = 1.0f;
            blueEnd = 1.0f;
            alpha = new float[]{1.0f};
            scale = new float[]{1.0f};
            rotstart = 0.0f;
            loop = false;
            partStart = 0;
            partNum = 1;
            partInc = 1;
            layer = 0;
            slowDown = 0.9800000190734863;
            grav = 0.0f;
            randX = 0.0;
            randY = 0.0;
            randZ = 0.0;
            delay = 0;
        }
    }
}