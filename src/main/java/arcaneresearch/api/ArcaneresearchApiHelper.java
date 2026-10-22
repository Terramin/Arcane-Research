package arcaneresearch.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.registries.BuiltInRegistries; 
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.crafting.IngredientNBTTC;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ArcaneresearchApiHelper {

    public static final Attribute CHAMPION_MOD =
            (Attribute) new RangedAttribute("tc.mobmod", -2.0D, -2.0D, 100.0D).setSyncable(true);

    public static boolean areItemsEqual(ItemStack s1, ItemStack s2) {
        return ArcaneresearchInvHelper.areItemsEqual(s1, s2);
    }

    public static boolean containsMatch(boolean strict, ItemStack[] inputs, List<ItemStack> targets) {
        return ArcaneresearchInvHelper.containsMatch(strict, inputs, targets);
    }

    public static boolean areItemStacksEqualForCrafting(ItemStack stack0, Object in) {
        return ArcaneresearchInvHelper.areItemStacksEqualForCrafting(stack0, in);
    }

    public static boolean areItemStackTagsEqualForCrafting(ItemStack slotItem, ItemStack recipeItem) {
        return ArcaneresearchInvHelper.areItemStackTagsEqualForCrafting(slotItem, recipeItem);
    }

    
    public static ResourceHandler<ItemResource> getItemHandlerAt(Level level, BlockPos pos, Direction side) {
        return ArcaneresearchInvHelper.getItemHandlerAt(level, pos, side);
    }

    public static ResourceHandler<ItemResource> wrapInventory(net.minecraft.world.Container inventory, Direction side) {
        return ArcaneresearchInvHelper.wrapInventory(inventory, side);
    }

    public static boolean areItemStackTagsEqualRelaxed(ItemStack prime, ItemStack other) {
        return ArcaneresearchInvHelper.areItemStackTagsEqualRelaxed(prime, other);
    }

    public static boolean compareTagsRelaxed(net.minecraft.nbt.CompoundTag prime, net.minecraft.nbt.CompoundTag other) {
        return ArcaneresearchInvHelper.compareTagsRelaxed(prime, other);
    }

    public static BlockEntity getConnectableTile(Level level, BlockPos pos, Direction direction) {
        BlockEntity te = level.getBlockEntity(pos.relative(direction));
        if (te instanceof IEssentiaTransport transport && transport.isConnectable(direction.getOpposite())) {
            return te;
        }
        return null;
    }

    public static BlockEntity getConnectableTile(BlockGetter level, BlockPos pos, Direction direction) {
        if (level instanceof Level actual) {
            return getConnectableTile(actual, pos, direction);
        }
        BlockEntity te = level.getBlockEntity(pos.relative(direction));
        if (te instanceof IEssentiaTransport transport && transport.isConnectable(direction.getOpposite())) {
            return te;
        }
        return null;
    }

    
    
    
    
    
    
    
    
    
    public static Ingredient getIngredient(Object obj) {
        if (obj instanceof Ingredient ingredient) {
            return ingredient;
        }
        if (obj instanceof ItemStack stack) {
            
            
            
            
            
            if (!stack.isEmpty() && stack.isComponentsPatchEmpty()) {
                return Ingredient.of(stack.getItem());
            }
            return new Ingredient(new IngredientNBTTC(stack));
        }
        if (obj instanceof ItemStack[] stacks) {
            
            
            List<Item> items = new ArrayList<>();
            for (ItemStack s : stacks) {
                if (s != null && !s.isEmpty()) {
                    items.add(s.getItem());
                }
            }
            if (items.isEmpty()) {
                return null;
            }
            return Ingredient.of(items.stream());
        }
        if (obj instanceof net.minecraft.world.level.ItemLike itemLike) {
            return Ingredient.of(itemLike);
        }
        if (obj instanceof String name) {
            Identifier id = resolveTagId(name.trim());
            if (id != null) {
                var key = TagKey.create(Registries.ITEM, id);
                List<net.minecraft.core.Holder<Item>> holders = new ArrayList<>();
                BuiltInRegistries.ITEM.getTagOrEmpty(key).forEach(holders::add);
                
                
                if (holders.isEmpty()) {
                    return null;
                }
                return Ingredient.of(net.minecraft.core.HolderSet.direct(holders));
            }
            return null;
        }
        if (obj instanceof Collection<?> collection) {
            
            
            List<Item> items = new ArrayList<>();
            for (Object o : collection) {
                if (o instanceof ItemStack s && !s.isEmpty()) {
                    items.add(s.getItem());
                } else if (o instanceof net.minecraft.world.level.ItemLike il
                        && il.asItem() != net.minecraft.world.item.Items.AIR) {
                    items.add(il.asItem());
                }
            }
            if (items.isEmpty()) {
                return null;
            }
            return Ingredient.of(items.stream());
        }

        return null;
    }

    private static Identifier resolveTagId(String name) {
        if (name.isEmpty()) {
            return null;
        }
        try {
            if (name.contains(":")) {
                return Identifier.parse(name);
            }
            Identifier mapped = OreDictionaryEntries.ORE_TAGS.get(name);
            if (mapped != null) {
                return mapped;
            }
            return Identifier.fromNamespaceAndPath("c", name); 
        } catch (Exception e) {
            return null;
        }
    }

    public static List<ItemStack> getOresWithWildCards(String oreDict) {
        List<ItemStack> out = new ArrayList<>();
        String s = oreDict.trim();
        try {
            
            
            if (s.endsWith("*")) {
                String prefix = s.substring(0, s.length() - 1).toLowerCase(Locale.ROOT);
                BuiltInRegistries.ITEM.getTags()
                        .map(net.minecraft.core.HolderSet.Named::key)
                        .filter(key -> key.location().getPath().toLowerCase(Locale.ROOT).startsWith(prefix))
                        .forEach(key -> BuiltInRegistries.ITEM.getTagOrEmpty(key).forEach(holder -> out.add(new ItemStack(holder.value()))));
                return out;
            }
            Identifier id = resolveTagId(s);
            if (id == null) {
                return out;
            }
            BuiltInRegistries.ITEM.getTags()
                    .map(net.minecraft.core.HolderSet.Named::key)
                    .filter(key -> key.location().equals(id))
                    .forEach(key -> BuiltInRegistries.ITEM.getTagOrEmpty(key).forEach(holder -> out.add(new ItemStack(holder.value()))));
        } catch (Exception ignored) {

        }
        return out;
    }

    public static BlockHitResult rayTraceIgnoringSource(Level level, Vec3 v1, Vec3 v2,
                                                        boolean stopOnLiquid, boolean ignoreNoCollision, boolean returnLast) {
        if (Double.isNaN(v1.x) || Double.isNaN(v1.y) || Double.isNaN(v1.z)
                || Double.isNaN(v2.x) || Double.isNaN(v2.y) || Double.isNaN(v2.z)) {
            return null;
        }
        int i = Mth.floor(v2.x);
        int j = Mth.floor(v2.y);
        int k = Mth.floor(v2.z);
        int l = Mth.floor(v1.x);
        int i1 = Mth.floor(v1.y);
        int j1 = Mth.floor(v1.z);
        Vec3 cur = v1;
        BlockHitResult lastMiss = null;
        int steps = 200;
        while (steps-- >= 0) {
            if (Double.isNaN(cur.x) || Double.isNaN(cur.y) || Double.isNaN(cur.z)) {
                return null;
            }
            if (l == i && i1 == j && j1 == k) {
                continue;
            }
            boolean stepX = true;
            boolean stepY = true;
            boolean stepZ = true;
            double bx = 999.0D;
            double by = 999.0D;
            double bz = 999.0D;
            if (i > l) {
                bx = l + 1.0D;
            } else if (i < l) {
                bx = l + 0.0D;
            } else {
                stepX = false;
            }
            if (j > i1) {
                by = i1 + 1.0D;
            } else if (j < i1) {
                by = i1 + 0.0D;
            } else {
                stepY = false;
            }
            if (k > j1) {
                bz = j1 + 1.0D;
            } else if (k < j1) {
                bz = j1 + 0.0D;
            } else {
                stepZ = false;
            }
            double fx = 999.0D;
            double fy = 999.0D;
            double fz = 999.0D;
            double dx = v2.x - cur.x;
            double dy = v2.y - cur.y;
            double dz = v2.z - cur.z;
            if (stepX) {
                fx = (bx - cur.x) / dx;
            }
            if (stepY) {
                fy = (by - cur.y) / dy;
            }
            if (stepZ) {
                fz = (bz - cur.z) / dz;
            }
            if (fx == -0.0D) {
                fx = -1.0E-4D;
            }
            if (fy == -0.0D) {
                fy = -1.0E-4D;
            }
            if (fz == -0.0D) {
                fz = -1.0E-4D;
            }
            Direction facing;
            if (fx < fy && fx < fz) {
                facing = (i > l) ? Direction.WEST : Direction.EAST;
                cur = new Vec3(bx, cur.y + dy * fx, cur.z + dz * fx);
            } else if (fy < fz) {
                facing = (j > i1) ? Direction.DOWN : Direction.UP;
                cur = new Vec3(cur.x + dx * fy, by, cur.z + dz * fy);
            } else {
                facing = (k > j1) ? Direction.NORTH : Direction.SOUTH;
                cur = new Vec3(cur.x + dx * fz, cur.y + dy * fz, bz);
            }
            l = Mth.floor(cur.x) - ((facing == Direction.EAST) ? 1 : 0);
            i1 = Mth.floor(cur.y) - ((facing == Direction.UP) ? 1 : 0);
            j1 = Mth.floor(cur.z) - ((facing == Direction.SOUTH) ? 1 : 0);
            BlockPos bp = new BlockPos(l, i1, j1);
            BlockState state = level.getBlockState(bp);
            VoxelShape shape = state.getCollisionShape(level, bp);
            if (!ignoreNoCollision || !shape.isEmpty()) {

                if (stopOnLiquid || !state.liquid()) {
                    BlockHitResult hit = shape.clip(cur, v2, bp);
                    if (hit != null) {
                        return hit;
                    }
                    continue;
                }
                lastMiss = new BlockHitResult(cur, facing, bp, false);
            }
        }
        return returnLast ? lastMiss : null;
    }

    public static int setByteInInt(int data, byte b, int index) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(0, data);
        bb.put(index, b);
        return bb.getInt(0);
    }

    public static byte getByteInInt(int data, int index) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(0, data);
        return bb.get(index);
    }

    public static long setByteInLong(long data, byte b, int index) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(0, data);
        bb.put(index, b);
        return bb.getLong(0);
    }

    public static byte getByteInLong(long data, int index) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(0, data);
        return bb.get(index);
    }

    public static int setNibbleInInt(int data, int nibble, int nibbleIndex) {
        int shift = nibbleIndex * 4;
        return (data & ~(0xf << shift)) | (nibble << shift);
    }

    public static int getNibbleInInt(int data, int nibbleIndex) {
        return (data >> (nibbleIndex << 2)) & 0xF;
    }

    public static ItemStack makeCrystal(Aspect aspect, int stackSize) {
        if (aspect == null) return ItemStack.EMPTY;

        
        
        Item crystalItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("arcaneresearch", "crystal_essence"));

        if (crystalItem == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(crystalItem, stackSize);
        if (crystalItem instanceof IEssentiaContainerItem container) {
            container.setAspects(stack, new AspectList().add(aspect, 1));
        }
        return stack;
    }

    public static ItemStack makeCrystal(Aspect aspect) {
        return makeCrystal(aspect, 1);
    }

    public static Object getNBTDataFromId(net.minecraft.nbt.CompoundTag nbt, byte id, String key) {
        
        
        return switch (id) {
            case 1 -> nbt.getByteOr(key, (byte) 0);
            case 2 -> (short) nbt.getShortOr(key, (short) 0);
            case 3 -> nbt.getIntOr(key, 0);
            case 4 -> nbt.getLongOr(key, 0L);
            case 5 -> nbt.getFloatOr(key, 0F);
            case 6 -> nbt.getDoubleOr(key, 0D);
            case 7 -> nbt.getByteArray(key).orElse(new byte[0]);
            case 8 -> nbt.getStringOr(key, "");
            case 9 -> nbt.getListOrEmpty(key);
            case 10 -> nbt.getCompoundOrEmpty(key);
            case 11 -> nbt.getIntArray(key).orElse(new int[0]);
            default -> null;
        };
    }
}
