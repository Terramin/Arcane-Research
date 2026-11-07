package arcaneresearch.helper;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class OreColorAnalyzerHelper {

    private static final int BUCKETS = 16 * 16 * 16;
    private static final int MIN_ALPHA = 128;
    private static final float MIN_FRACTION = 0.02f;
    private static final int MIN_PIXELS_ABS = 2;

    private static final Map<BlockState, Integer> CACHE = new HashMap<>();

    private OreColorAnalyzerHelper() {}

    public static int getGeneratedOreColor(BlockState state, Level level, BlockPos pos) {
        Integer cached = CACHE.get(state);
        if (cached != null) return cached;

        int color = computeColor(state);
        CACHE.put(state, color);
        return color;
    }

    public static void invalidateCache() {
        CACHE.clear();
    }

    private static int computeColor(BlockState state) {
        TextureAtlasSprite sprite = getSprite(state);
        if (sprite == null) return fallbackColor(state);

        NativeImage image = getNativeImage(sprite);
        if (image == null) return fallbackColor(state);

        return findInclusionColor(image);
    }

    @Nullable
    private static TextureAtlasSprite getSprite(BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.getBlockRenderer() == null) return null;

        BlockModelShaper shaper = mc.getBlockRenderer().getBlockModelShaper();
        TextureAtlasSprite sprite = shaper.getParticleIcon(state);
        if (sprite == null) return null;

        ResourceLocation id = getSpriteId(sprite);
        if (id != null && id.getPath().contains("missingno")) return null;
        return sprite;
    }

    @Nullable
    private static ResourceLocation getSpriteId(TextureAtlasSprite sprite) {
        // 1.20.x — имя прямо на спрайте
        try {
            Method m = TextureAtlasSprite.class.getMethod("getName");
            Object r = m.invoke(sprite);
            if (r instanceof ResourceLocation id) return id;
        } catch (Throwable ignored) { }
        try {
            Object contents = sprite.contents();
            if (contents != null) {
                for (String name : new String[]{"getName", "name", "getId"}) {
                    try {
                        Method m = SpriteContents.class.getMethod(name);
                        Object r = m.invoke(contents);
                        if (r instanceof ResourceLocation id) return id;
                    } catch (Throwable ignored) { }
                }
            }
        } catch (Throwable ignored) { }

        return null;
    }

    @Nullable
    private static NativeImage getNativeImage(TextureAtlasSprite sprite) {
        SpriteContents contents;
        try {
            contents = sprite.contents();
        } catch (Throwable t) {
            return null;
        }
        if (contents == null) return null;

        try {
            Method m = SpriteContents.class.getMethod("getOriginalImage");
            Object r = m.invoke(contents);
            if (r instanceof NativeImage img) return img;
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable ignored) {
        }

        try {
            Field f = SpriteContents.class.getDeclaredField("image");
            f.setAccessible(true);
            Object r = f.get(contents);
            if (r instanceof NativeImage img) return img;
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static int findInclusionColor(NativeImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        if (w <= 0 || h <= 0) return 0xFFFFFF;

        int[] pixels = new int[w * h];
        int count = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = image.getPixelRGBA(x, y);
                int a = (rgba >>> 24) & 0xFF;
                if (a < MIN_ALPHA) continue;
                pixels[count++] = rgba & 0xFFFFFF;
            }
        }
        if (count == 0) return 0xFFFFFF;

        int[]  bucketCount = new int[BUCKETS];
        long[] bucketSumR  = new long[BUCKETS];
        long[] bucketSumG  = new long[BUCKETS];
        long[] bucketSumB  = new long[BUCKETS];

        for (int i = 0; i < count; i++) {
            int p = pixels[i];
            int r = (p >>> 16) & 0xFF;
            int g = (p >>> 8)  & 0xFF;
            int b =  p         & 0xFF;
            int key = ((r >> 4) << 8) | ((g >> 4) << 4) | (b >> 4);
            bucketCount[key]++;
            bucketSumR[key] += r;
            bucketSumG[key] += g;
            bucketSumB[key] += b;
        }

        int domKey = -1;
        int domCount = 0;
        for (int k = 0; k < BUCKETS; k++) {
            if (bucketCount[k] > domCount) {
                domCount = bucketCount[k];
                domKey   = k;
            }
        }
        if (domKey < 0 || domCount == 0) return 0xFFFFFF;

        int domR = (int) (bucketSumR[domKey] / domCount);
        int domG = (int) (bucketSumG[domKey] / domCount);
        int domB = (int) (bucketSumB[domKey] / domCount);

        int minCount = Math.max(MIN_PIXELS_ABS, (int) (count * MIN_FRACTION));
        double bestScore = -1.0;
        int bestKey = -1;

        for (int k = 0; k < BUCKETS; k++) {
            int c = bucketCount[k];
            if (c < minCount) continue;
            if (k == domKey)  continue;

            int r = (int) (bucketSumR[k] / c);
            int g = (int) (bucketSumG[k] / c);
            int b = (int) (bucketSumB[k] / c);

            double dist = redmeanDistance(r, g, b, domR, domG, domB);
            double score = dist * Math.sqrt((double) c / count);

            if (score > bestScore) {
                bestScore = score;
                bestKey   = k;
            }
        }

        if (bestKey < 0) {
            return (domR << 16) | (domG << 8) | domB;
        }
        int c = bucketCount[bestKey];
        int r = (int) (bucketSumR[bestKey] / c);
        int g = (int) (bucketSumG[bestKey] / c);
        int b = (int) (bucketSumB[bestKey] / c);
        return (r << 16) | (g << 8) | b;
    }

    private static double redmeanDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        double rmean = (r1 + r2) / 2.0;
        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;
        return Math.sqrt(
                (2.0 + rmean / 256.0) * dr * dr
                        + 4.0 * dg * dg
                        + (2.0 + (255.0 - rmean) / 256.0) * db * db
        );
    }

    private static int fallbackColor(BlockState state) {
        int hash = state.getBlock().getDescriptionId().hashCode();
        int r = (hash       ) & 0xFF;
        int g = (hash >>>  8) & 0xFF;
        int b = (hash >>> 16) & 0xFF;
        r = (r + 128) / 2;
        g = (g + 128) / 2;
        b = (b + 128) / 2;
        return (r << 16) | (g << 8) | b;
    }
}