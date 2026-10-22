package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashSet;
import java.util.Set;

public class CropUtils {

    private static final Set<String> standardCrops = new HashSet<>();

    private static final Set<String> clickableCrops = new HashSet<>();

    private static final Set<String> stackedCrops = new HashSet<>();

    private static final Set<String> lampBlacklist = new HashSet<>();

    public static void addStandardCrop(ItemStack stack, int grownAge) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) return;
        addStandardCrop(block, grownAge);
    }

    public static void addStandardCrop(Block block, int grownAge) {
        String blockId = getBlockId(block);
        if (blockId == null) return;

        if (grownAge == 32767) {

            for (int a = 0; a < 16; a++) {
                standardCrops.add(blockId + ":" + a);
            }
        } else {
            standardCrops.add(blockId + ":" + grownAge);
        }

        if (block instanceof CropBlock && grownAge != 7) {
            standardCrops.add(blockId + ":7");
        }
    }

    public static void addClickableCrop(ItemStack stack, int grownAge) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) return;

        String blockId = getBlockId(block);
        if (blockId == null) return;

        if (grownAge == 32767) {
            for (int a = 0; a < 16; a++) {
                clickableCrops.add(blockId + ":" + a);
            }
        } else {
            clickableCrops.add(blockId + ":" + grownAge);
        }

        if (block instanceof CropBlock && grownAge != 7) {
            clickableCrops.add(blockId + ":7");
        }
    }

    public static void addStackedCrop(ItemStack stack, int grownAge) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) return;
        addStackedCrop(block, grownAge);
    }

    public static void addStackedCrop(Block block, int grownAge) {
        String blockId = getBlockId(block);
        if (blockId == null) return;

        if (grownAge == 32767) {
            for (int a = 0; a < 16; a++) {
                stackedCrops.add(blockId + ":" + a);
            }
        } else {
            stackedCrops.add(blockId + ":" + grownAge);
        }

        if (block instanceof CropBlock && grownAge != 7) {
            stackedCrops.add(blockId + ":7");
        }
    }

    public static void blacklistLamp(ItemStack stack, int age) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) return;

        String blockId = getBlockId(block);
        if (blockId == null) return;

        if (age == 32767) {
            for (int a = 0; a < 16; a++) {
                lampBlacklist.add(blockId + ":" + a);
            }
        } else {
            lampBlacklist.add(blockId + ":" + age);
        }
    }

    public static boolean isGrownCrop(Level level, BlockPos pos) {
        if (level.isEmptyBlock(pos)) return false;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String blockId = getBlockId(block);

        if (blockId == null) return false;

        int age = getBlockAge(state);
        String key = blockId + ":" + age;

        if (standardCrops.contains(key) || clickableCrops.contains(key)) {
            return true;
        }

        if (stackedCrops.contains(key)) {
            Block below = level.getBlockState(pos.below()).getBlock();
            if (below == block) {
                return true;
            }
        }

        if (block instanceof BonemealableBlock bonemealable) {

            if (!(block instanceof StemBlock)) {
                
                
                if (!bonemealable.isValidBonemealTarget(level, pos, state,
                        net.minecraft.world.level.block.BonemealSource.INTERACTION)) {
                    return true;
                }
            }
        }

        if (block instanceof CropBlock cropBlock) {
            if (cropBlock.isMaxAge(state)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isClickableCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String blockId = getBlockId(block);

        if (blockId == null) return false;

        int age = getBlockAge(state);
        return clickableCrops.contains(blockId + ":" + age);
    }

    public static boolean isStackedCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String blockId = getBlockId(block);

        if (blockId == null) return false;

        int age = getBlockAge(state);
        return stackedCrops.contains(blockId + ":" + age);
    }

    public static boolean doesLampGrow(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String blockId = getBlockId(block);

        if (blockId == null) return true;

        int age = getBlockAge(state);
        return !lampBlacklist.contains(blockId + ":" + age);
    }

    private static String getBlockId(Block block) {
        net.minecraft.resources.Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        return key != null ? key.toString() : null;
    }

    private static int getBlockAge(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.getAge(state);
        }

        for (var prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase("age")) {
                Object value = state.getValue(prop);
                if (value instanceof Integer intVal) {
                    return intVal;
                }
            }
        }
        return 0;
    }

    public static void init() {

        addStackedCrop(Blocks.SUGAR_CANE, 0);
        addStackedCrop(Blocks.CACTUS, 0);
        addStackedCrop(Blocks.BAMBOO, 32767);
        addStackedCrop(Blocks.KELP_PLANT, 32767);

    }
}
