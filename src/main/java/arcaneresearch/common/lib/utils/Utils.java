package arcaneresearch.common.lib.utils;

import arcaneresearch.api.internal.WeightedRandomLoot;
import arcaneresearch.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {

    public static final String[] COLOR_NAMES = {
            "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray",
            "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
    };
    public static final int[] COLORS = {
            0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
            0xABABAB, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B
    };

    public static final Map<Item, ItemStack> specialMiningResult = new HashMap<>();
    public static final Map<Item, Float> specialMiningChance = new HashMap<>();

    public static boolean isChunkLoaded(Level level, int x, int z) {
        if (level == null) return false;
        LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        return chunk != null;
    }

    public static boolean isChunkLoaded(Level level, BlockPos pos) {
        if (pos == null) return false;
        return isChunkLoaded(level, pos.getX(), pos.getZ());
    }

    public static boolean useBonemealAtLoc(Level level, Player player, BlockPos pos) {
        if (level == null || pos == null) return false;
        ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
        return BoneMealItem.applyBonemeal(bonemeal, level, pos, player);
    }

    public static boolean hasColor(byte[] colors) {
        if (colors == null) return false;
        for (byte col : colors) {
            if (col >= 0) return true;
        }
        return false;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile == null || destFile == null) {
            throw new IOException("source/destination file is null");
        }
        if (!destFile.exists()) {
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Cannot create parent dir: " + parent);
            }
            if (!destFile.createNewFile()) {
                throw new IOException("Cannot create: " + destFile);
            }
        }
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0L, source.size());
        }
    }

    public static void addSpecialMiningResult(ItemStack in, ItemStack out, float chance) {
        if (in == null || in.isEmpty() || out == null || out.isEmpty()) return;
        specialMiningResult.put(in.getItem(), out.copy());
        specialMiningChance.put(in.getItem(), chance);
    }

    public static ItemStack findSpecialMiningResult(ItemStack is, float chance, RandomSource rand) {
        if (is == null || is.isEmpty() || rand == null) return ItemStack.EMPTY;

        ItemStack dropped = is.copy();
        Item key = is.getItem();
        ItemStack special = specialMiningResult.get(key);
        Float specialChance = specialMiningChance.get(key);
        if (special == null || specialChance == null) return dropped;

        if (rand.nextFloat() <= chance * specialChance) {
            ItemStack result = special.copy();
            result.setCount(result.getCount() * is.getCount());
            return result;
        }
        return dropped;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int setNibble(int data, int nibble, int nibbleIndex) {
        int shift = nibbleIndex * 4;
        return (data & ~(15 << shift)) | nibble << shift;
    }

    public static int getNibble(int data, int nibbleIndex) {
        return (data >> (nibbleIndex << 2)) & 0xF;
    }

    public static boolean getBit(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    public static int setBit(int value, int bit) {
        return value | (1 << bit);
    }

    public static int clearBit(int value, int bit) {
        return value & ~(1 << bit);
    }

    public static int toggleBit(int value, int bit) {
        return value ^ (1 << bit);
    }

    public static byte pack(boolean... vals) {
        if (vals == null) return 0;
        if (vals.length > 8) {
            throw new IllegalArgumentException("pack() accepts at most 8 bits, got " + vals.length);
        }
        byte result = 0;
        for (boolean bit : vals) {
            result = (byte) (result << 1 | (bit ? 1 : 0));
        }
        return result;
    }

    public static boolean[] unpack(byte val) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = ((val >> (7 - i)) & 0x1) == 1;
        }
        return result;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public static int byteArrayToInt(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            throw new IllegalArgumentException("byteArrayToInt requires >= 4 bytes, got "
                    + (bytes == null ? "null" : bytes.length));
        }
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte[] shortToByteArray(short value) {
        return new byte[]{
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public static short byteArrayToShort(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            throw new IllegalArgumentException("byteArrayToShort requires >= 2 bytes, got "
                    + (bytes == null ? "null" : bytes.length));
        }
        return (short) ((bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF));
    }

    public static boolean isLyingInCone(double[] x, double[] t, double[] b, float aperture) {
        if (x == null || t == null || b == null) return false;
        if (x.length < 3 || t.length < 3 || b.length < 3) return false;

        double halfAperture = aperture / 2.0f;
        double[] apexToXVect = dif(t, x);
        double[] axisVect = dif(t, b);

        double axMagn = magn(apexToXVect);
        double axisMagn = magn(axisVect);
        if (axMagn < 1.0E-9D || axisMagn < 1.0E-9D) return false;

        double dot = dotProd(apexToXVect, axisVect);
        double cos = dot / axMagn / axisMagn;
        if (cos <= Math.cos(halfAperture)) return false;

        return dot / axisMagn < axisMagn;
    }

    public static double dotProd(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static double[] dif(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    public static double magn(double[] a) {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }

    public static Vec3 calculateVelocity(Vec3 from, Vec3 to, double heightGain, double gravity) {
        if (from == null || to == null || gravity <= 0.0D) return Vec3.ZERO;

        double endGain = to.y - from.y;
        double horizSq = distanceSquared2d(from, to);
        if (horizSq < 1.0E-9D) return Vec3.ZERO;

        double horizDist = Math.sqrt(horizSq);
        double maxGain = Math.max(heightGain, endGain + heightGain);
        if (maxGain <= 0.0D) return Vec3.ZERO;

        double a = -horizDist * horizDist / (4.0 * maxGain);
        if (a == 0.0D) return Vec3.ZERO;

        double c = -endGain;
        double discriminant = horizDist * horizDist - 4.0 * a * c;
        if (discriminant < 0.0D) return Vec3.ZERO;

        double denom = 2.0 * a;
        if (denom == 0.0D) return Vec3.ZERO;

        double slope = -horizDist / denom - Math.sqrt(discriminant) / denom;
        if (slope == 0.0D || Double.isNaN(slope) || Double.isInfinite(slope)) return Vec3.ZERO;

        double vy = Math.sqrt(maxGain * gravity);
        double vh = vy / slope;
        if (Double.isNaN(vh) || Double.isInfinite(vh)) return Vec3.ZERO;

        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double mag = Math.sqrt(dx * dx + dz * dz);
        if (mag < 1.0E-9D) return new Vec3(0.0D, vy, 0.0D);

        return new Vec3(vh * (dx / mag), vy, vh * (dz / mag));
    }

    public static double distanceSquared2d(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return dx * dx + dz * dz;
    }

    public static double distanceSquared3d(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static final int LOOT_MAX_DEPTH = 8;

    public static ItemStack generateLoot(int rarity, RandomSource rand) {
        if (rand == null) return ItemStack.EMPTY;

        ItemStack result = ItemStack.EMPTY;
        for (int depth = 0; depth < LOOT_MAX_DEPTH; depth++) {
            if (rarity > 0 && rand.nextFloat() < 0.025f * rarity) {
                result = generateGear(rarity, rand);
                if (!result.isEmpty()) break;
            } else {
                WeightedRandomLoot loot = switch (rarity) {
                    case 1 -> WeightedRandomLoot.getRandomItem(
                            WeightedRandomLoot.lootBagUncommon, pickRandom(rand));
                    case 2 -> WeightedRandomLoot.getRandomItem(
                            WeightedRandomLoot.lootBagRare, pickRandom(rand));
                    default -> WeightedRandomLoot.getRandomItem(
                            WeightedRandomLoot.lootBagCommon, pickRandom(rand));
                };
                if (loot != null) {
                    result = loot.item.copy();
                }
                break;
            }
        }

        if (result.is(Items.BOOK)) {
            result = EnchantmentHelper.enchantItem(rand.fork(), result,
                    (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), false);
        }
        return result;
    }

    private static Random pickRandom(RandomSource rand) {
        return rand.fork().nextLong() > 0L ? new Random(rand.nextLong()) : new Random();
    }

    private static ItemStack generateGear(int rarity, RandomSource rand) {
        int quality = rand.nextInt(2);
        if (rand.nextFloat() < 0.2f) quality++;
        if (rand.nextFloat() < 0.15f) quality++;
        if (rand.nextFloat() < 0.1f) quality++;
        if (rand.nextFloat() < 0.095f) quality++;
        if (rand.nextFloat() < 0.095f) quality++;

        Item item = getGearItemForSlot(rand.nextInt(5), quality);
        if (item == null) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(item);
        if (result.isDamageableItem()) {
            result.setDamageValue(rand.nextInt(1 + result.getMaxDamage() / 6));
        }
        if (rand.nextInt(4) < rarity) {
            result = EnchantmentHelper.enchantItem(rand.fork(), result,
                    (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), false);
        }
        return result;
    }

    private static Item getGearItemForSlot(int slot, int quality) {
        return switch (slot) {
            case 4 -> switch (quality) {
                case 0 -> Items.LEATHER_HELMET;
                case 1 -> Items.GOLDEN_HELMET;
                case 2 -> Items.CHAINMAIL_HELMET;
                case 3 -> Items.IRON_HELMET;
                case 4 -> ModItems.THAUMIUM_HELM.get();
                case 5 -> Items.DIAMOND_HELMET;
                case 6 -> ModItems.VOID_HELM.get();
                default -> null;
            };
            case 3 -> switch (quality) {
                case 0 -> Items.LEATHER_CHESTPLATE;
                case 1 -> Items.GOLDEN_CHESTPLATE;
                case 2 -> Items.CHAINMAIL_CHESTPLATE;
                case 3 -> Items.IRON_CHESTPLATE;
                case 4 -> ModItems.THAUMIUM_CHEST.get();
                case 5 -> Items.DIAMOND_CHESTPLATE;
                case 6 -> ModItems.VOID_CHEST.get();
                default -> null;
            };
            case 2 -> switch (quality) {
                case 0 -> Items.LEATHER_LEGGINGS;
                case 1 -> Items.GOLDEN_LEGGINGS;
                case 2 -> Items.CHAINMAIL_LEGGINGS;
                case 3 -> Items.IRON_LEGGINGS;
                case 4 -> ModItems.THAUMIUM_LEGS.get();
                case 5 -> Items.DIAMOND_LEGGINGS;
                case 6 -> ModItems.VOID_LEGS.get();
                default -> null;
            };
            case 1 -> switch (quality) {
                case 0 -> Items.LEATHER_BOOTS;
                case 1 -> Items.GOLDEN_BOOTS;
                case 2 -> Items.CHAINMAIL_BOOTS;
                case 3 -> Items.IRON_BOOTS;
                case 4 -> ModItems.THAUMIUM_BOOTS.get();
                case 5 -> Items.DIAMOND_BOOTS;
                case 6 -> ModItems.VOID_BOOTS.get();
                default -> null;
            };
            case 0 -> switch (quality) {
                case 0 -> Items.IRON_AXE;
                case 1 -> Items.IRON_SWORD;
                case 2 -> Items.GOLDEN_AXE;
                case 3 -> Items.GOLDEN_SWORD;
                case 4 -> ModItems.THAUMIUM_SWORD.get();
                case 5 -> Items.DIAMOND_SWORD;
                case 6 -> ModItems.VOID_SWORD.get();
                default -> null;
            };
            default -> null;
        };
    }

    public static Vec3 rotateAsBlock(Vec3 vec, Direction side) {
        return rotate(vec.subtract(0.5, 0.5, 0.5), side).add(0.5, 0.5, 0.5);
    }

    public static Vec3 rotateAsBlockRev(Vec3 vec, Direction side) {
        return revRotate(vec.subtract(0.5, 0.5, 0.5), side).add(0.5, 0.5, 0.5);
    }

    public static Vec3 rotate(Vec3 vec, Direction side) {
        if (side == null) return vec;
        return switch (side) {
            case DOWN -> new Vec3(vec.x, -vec.y, -vec.z);
            case UP -> new Vec3(vec.x, vec.y, vec.z);
            case NORTH -> new Vec3(vec.x, vec.z, -vec.y);
            case SOUTH -> new Vec3(vec.x, -vec.z, vec.y);
            case WEST -> new Vec3(-vec.y, vec.x, vec.z);
            case EAST -> new Vec3(vec.y, -vec.x, vec.z);
        };
    }

    public static Vec3 revRotate(Vec3 vec, Direction side) {
        if (side == null) return vec;
        return switch (side) {
            case DOWN -> new Vec3(vec.x, -vec.y, -vec.z);
            case UP -> new Vec3(vec.x, vec.y, vec.z);
            case NORTH -> new Vec3(vec.x, -vec.z, vec.y);
            case SOUTH -> new Vec3(vec.x, vec.z, -vec.y);
            case WEST -> new Vec3(vec.y, -vec.x, vec.z);
            case EAST -> new Vec3(-vec.y, vec.x, vec.z);
        };
    }

    public static Vec3 rotateAroundX(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x, vec.y * cos + vec.z * sin, vec.z * cos - vec.y * sin);
    }

    public static Vec3 rotateAroundY(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    public static Vec3 rotateAroundZ(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.y * sin, vec.y * cos - vec.x * sin, vec.z);
    }

    public static BlockHitResult rayTrace(Level level, Entity entity, boolean useLiquids) {
        if (level == null || entity == null) return null;
        double range = 5.0;
        if (entity instanceof ServerPlayer serverPlayer) {
            range = serverPlayer.getBlockReach();
        }
        return rayTrace(level, entity, useLiquids, range);
    }

    public static BlockHitResult rayTrace(Level level, Entity entity, boolean useLiquids, double range) {
        if (level == null || entity == null) return null;
        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, fluidMode, entity));
    }

    public static BlockHitResult rayTrace(Level level, Entity entity, Vec3 lookVec, boolean useLiquids, double range) {
        if (level == null || entity == null || lookVec == null) return null;
        Vec3 eyePos = entity.getEyePosition();
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, fluidMode, entity));
    }

    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        if (clazz == null || fieldName == null) {
            throw new NoSuchFieldException("class/field is null");
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) throw e;
            return getField(superClass, fieldName);
        }
    }

    public static boolean isWoodLog(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LOGS);
    }

    public static boolean isOreBlock(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        return state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES);
    }
}