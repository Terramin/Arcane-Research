package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockItemTags;
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
import arcaneresearch.api.internal.WeightedRandomLoot;
import arcaneresearch.init.ModItems;

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
    public static Map<Item, ItemStack> specialMiningResult = new HashMap<>();
    public static Map<Item, Float> specialMiningChance = new HashMap<>();

    public static boolean isChunkLoaded(Level level, int x, int z) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        return chunk != null;
    }

    public static boolean isChunkLoaded(Level level, BlockPos pos) {
        return isChunkLoaded(level, pos.getX(), pos.getZ());
    }

    public static boolean useBonemealAtLoc(Level level, Player player, BlockPos pos) {
        ItemStack bonemeal = new ItemStack(Items.BONE_MEAL);
        return BoneMealItem.applyBonemeal(bonemeal, level, pos, player);
    }

    public static boolean hasColor(byte[] colors) {
        for (byte col : colors) {
            if (col >= 0) {
                return true;
            }
        }
        return false;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0L, source.size());
        }
    }

    public static void addSpecialMiningResult(ItemStack in, ItemStack out, float chance) {
        specialMiningResult.put(in.getItem(), out);
        specialMiningChance.put(in.getItem(), chance);
    }

    public static ItemStack findSpecialMiningResult(ItemStack is, float chance, RandomSource rand) {
        ItemStack dropped = is.copy();
        float r = rand.nextFloat();
        Item key = is.getItem();

        if (specialMiningResult.containsKey(key) && r <= chance * specialMiningChance.get(key)) {
            dropped = specialMiningResult.get(key).copy();
            dropped.setCount(dropped.getCount() * is.getCount());
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
        byte result = 0;
        for (boolean bit : vals) {
            result = (byte) (result << 1 | (bit ? 1 : 0));
        }
        return result;
    }

    public static boolean[] unpack(byte val) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; ++i) {
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
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte[] shortToByteArray(short value) {
        return new byte[]{
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public static short byteArrayToShort(byte[] bytes) {
        return (short) ((bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF));
    }

    public static boolean isLyingInCone(double[] x, double[] t, double[] b, float aperture) {
        double halfAperture = aperture / 2.0f;
        double[] apexToXVect = dif(t, x);
        double[] axisVect = dif(t, b);

        boolean isInInfiniteCone = dotProd(apexToXVect, axisVect) / magn(apexToXVect) / magn(axisVect) > Math.cos(halfAperture);
        if (!isInInfiniteCone) {
            return false;
        }

        return dotProd(apexToXVect, axisVect) / magn(axisVect) < magn(axisVect);
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
        double endGain = to.y - from.y;
        double horizDist = Math.sqrt(distanceSquared2d(from, to));
        double maxGain = Math.max(heightGain, endGain + heightGain);

        double a = -horizDist * horizDist / (4.0 * maxGain);
        double b = horizDist;
        double c = -endGain;

        double slope = -b / (2.0 * a) - Math.sqrt(b * b - 4.0 * a * c) / (2.0 * a);
        double vy = Math.sqrt(maxGain * gravity);
        double vh = vy / slope;

        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double mag = Math.sqrt(dx * dx + dz * dz);

        double dirx = dx / mag;
        double dirz = dz / mag;

        return new Vec3(vh * dirx, vy, vh * dirz);
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

    // NEOFORGE_PORT: EnchantmentHelper.enchantItem(RandomSource, ItemStack, int, boolean)
    // removed in 26.x -> overload with RegistryAccess + Optional<HolderSet> (empty = all
    // enchantments; old `false` treasure filter has no equivalent and is dropped).
    // RegistryAccess threaded from callers (both have Level).
    public static ItemStack generateLoot(int rarity, RandomSource rand, net.minecraft.core.RegistryAccess registries) {
        ItemStack result = ItemStack.EMPTY;

        if (rarity > 0 && rand.nextFloat() < 0.025f * rarity) {
            result = generateGear(rarity, rand, registries);
            if (result.isEmpty()) {
                result = generateLoot(rarity, rand, registries);
            }
        } else {

            WeightedRandomLoot loot = switch (rarity) {
                case 1 ->
                        WeightedRandomLoot.getRandomItem(WeightedRandomLoot.lootBagUncommon, rand.fork().nextLong() > 0 ? new Random(rand.nextLong()) : new Random());
                case 2 ->
                        WeightedRandomLoot.getRandomItem(WeightedRandomLoot.lootBagRare, rand.fork().nextLong() > 0 ? new Random(rand.nextLong()) : new Random());
                default ->
                        WeightedRandomLoot.getRandomItem(WeightedRandomLoot.lootBagCommon, rand.fork().nextLong() > 0 ? new Random(rand.nextLong()) : new Random());
            };

            if (loot != null) {
                result = loot.item.copy();
            }
        }

        if (result.is(Items.BOOK)) {
            result = EnchantmentHelper.enchantItem(rand.fork(), result,
                    (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), registries,
                    java.util.Optional.empty());
        }

        return result;
    }

    private static ItemStack generateGear(int rarity, RandomSource rand, net.minecraft.core.RegistryAccess registries) {
        int quality = rand.nextInt(2);
        if (rand.nextFloat() < 0.2f) quality++;
        if (rand.nextFloat() < 0.15f) quality++;
        if (rand.nextFloat() < 0.1f) quality++;
        if (rand.nextFloat() < 0.095f) quality++;
        if (rand.nextFloat() < 0.095f) quality++;

        Item item = getGearItemForSlot(rand.nextInt(5), quality);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(item);

        if (result.isDamageableItem()) {
            result.setDamageValue(rand.nextInt(1 + result.getMaxDamage() / 6));
        }

        if (rand.nextInt(4) < rarity) {
            result = EnchantmentHelper.enchantItem(rand.fork(), result,
                    (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), registries,
                    java.util.Optional.empty());
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
        double range = 5.0;
        if (entity instanceof ServerPlayer serverPlayer) {
            // NEOFORGE_PORT: ServerPlayer.getBlockReach removed -> vanilla
            // BLOCK_INTERACTION_RANGE attribute (same value).
            range = serverPlayer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
        }
        return rayTrace(level, entity, useLiquids, range);
    }

    public static BlockHitResult rayTrace(Level level, Entity entity, boolean useLiquids, double range) {
        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, fluidMode, entity));
    }

    public static BlockHitResult rayTrace(Level level, Entity entity, Vec3 lookVec, boolean useLiquids, double range) {
        Vec3 eyePos = entity.getEyePosition();
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        ClipContext.Fluid fluidMode = useLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, fluidMode, entity));
    }

    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return getField(superClass, fieldName);
        }
    }

    public static boolean isWoodLog(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LOGS);
    }

    public static boolean isOreBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        return state.is(BlockTags.GOLD_ORES) ||
                state.is(BlockTags.IRON_ORES) ||
                state.is(BlockTags.COPPER_ORES) ||
                state.is(BlockItemTags.COAL_ORES.block()) ||
                state.is(BlockItemTags.REDSTONE_ORES.block()) ||
                state.is(BlockItemTags.LAPIS_ORES.block()) ||
                state.is(BlockItemTags.DIAMOND_ORES.block()) ||
                state.is(BlockItemTags.EMERALD_ORES.block());
    }
}
