package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileInfernalFurnace extends TileArcaneresearchInventory implements net.minecraft.world.WorldlyContainer {

    public int furnaceCookTime = 0;
    public int furnaceMaxCookTime = 0;
    public int speedyTime = 0;
    public int facingX = -5;
    public int facingZ = -5;

    public TileInfernalFurnace(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFERNAL_FURNACE.get(), pos, state, 32);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileInfernalFurnace tile) {
        if (tile.facingX == -5) {
            tile.setFacing(state);
        }

        boolean wasCooked = false;

        if (tile.furnaceCookTime > 0) {
            tile.furnaceCookTime--;
            wasCooked = true;
        }

        if (tile.furnaceMaxCookTime <= 0) {
            tile.furnaceMaxCookTime = tile.calcCookTime();
        }

        if (tile.furnaceCookTime > tile.furnaceMaxCookTime) {
            tile.furnaceCookTime = tile.furnaceMaxCookTime;
        }

        if (tile.furnaceCookTime <= 0 && wasCooked) {
            for (int slot = 0; slot < tile.getContainerSize(); slot++) {
                ItemStack inputStack = tile.getItem(slot);
                if (!inputStack.isEmpty()) {
                    ItemStack result = tile.getSmeltingResult(inputStack);
                    if (!result.isEmpty()) {
                        if (tile.speedyTime > 0) {
                            tile.speedyTime--;
                        }
                        tile.ejectItem(result.copy(), inputStack);
                        level.blockEvent(pos, state.getBlock(), 3, 0);

                        if (level.getRandom().nextInt(20) == 0) {
                            Direction facing = tile.getFacing(state);
                            if (facing != null) {
                                AuraHelper.polluteAura(level, pos.relative(facing.getOpposite()), 1.0f, true);
                            }
                        }

                        tile.removeItem(slot, 1);
                        break;
                    }

                    tile.setItem(slot, ItemStack.EMPTY);
                }
            }
        }

        if (tile.speedyTime <= 0) {
            tile.speedyTime = (int) AuraHelper.drainVis(level, pos, 20.0f, false);
        }

        if (tile.furnaceCookTime == 0 && !wasCooked) {
            for (int slot = 0; slot < tile.getContainerSize(); slot++) {
                if (tile.canSmelt(tile.getItem(slot))) {
                    tile.furnaceMaxCookTime = tile.calcCookTime();
                    tile.furnaceCookTime = tile.furnaceMaxCookTime;
                    break;
                }
            }
        }
    }

    static TagKey<Item> oreDictToTag(String oreDict) {
        if (oreDict == null || !oreDict.startsWith("ore")) return null;
        String name = oreDict.substring(3).toLowerCase();
        return net.minecraft.tags.ItemTags.create(Identifier.fromNamespaceAndPath("c", "ores/" + name));
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putShort("CookTime", (short) furnaceCookTime);
        tag.putShort("SpeedyTime", (short) speedyTime);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        furnaceCookTime = tag.getShortOr("CookTime", (short) 0);
        speedyTime = tag.getShortOr("SpeedyTime", (short) 0);
    }

    private ItemStack getSmeltingResult(ItemStack input) {
        if (input.isEmpty() || level == null) return ItemStack.EMPTY;

        
        
        
        RecipeManager recipeManager = level.getServer().getRecipeManager();
        var recipe = recipeManager.getRecipeFor(
                RecipeType.SMELTING,
                new net.minecraft.world.item.crafting.SingleRecipeInput(input),
                level
        );

        return recipe.map(r -> r.value().assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(input)).copy()).orElse(ItemStack.EMPTY);
    }

    private boolean canSmelt(ItemStack stack) {
        return !stack.isEmpty() && !getSmeltingResult(stack).isEmpty();
    }

    private float getSmeltingExperience(ItemStack result) {
        if (level == null) return 0;

        
        
        RecipeManager recipeManager = level.getServer().getRecipeManager();
        for (var holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof SmeltingRecipe smelting) {
                if (ItemStack.isSameItem(smelting.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(result)), result)) {
                    return smelting.experience();
                }
            }
        }
        return 0;
    }

    private int getBellows() {
        if (level == null) return 0;

        int bellows = 0;
        for (Direction dir : Direction.values()) {
            if (dir != Direction.UP) {
                BlockPos bellowsPos = worldPosition.relative(dir, 2);
                BlockEntity tile = level.getBlockEntity(bellowsPos);

                if (tile instanceof TileBellows bellowsTile) {
                    BlockState bellowsState = level.getBlockState(bellowsPos);
                    Direction bellowsFacing = bellowsTile.getFacing(bellowsState);
                    if (bellowsFacing == dir.getOpposite() && level.getBestNeighborSignal(bellowsPos) == 0) {
                        bellows++;
                    }
                }
            }
        }
        return Math.min(4, bellows);
    }

    private int calcCookTime() {
        int b = getBellows();
        if (b > 0) {
            b *= 20 - (b - 1);
        }
        return Math.max(10, ((speedyTime > 0) ? 80 : 140) - b);
    }

    public ItemStack addItemsToInventory(ItemStack items) {
        if (level == null || items.isEmpty()) return items;
        if (canSmelt(items)) {
            items = ArcaneresearchInvHelper.insertStackAt(level, worldPosition, Direction.UP, items, false);
        } else {
            destroyItem();
            items = ItemStack.EMPTY;
        }
        return items;
    }

    private void destroyItem() {
        if (level == null) return;
        level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3f,
                2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f);

        double lx = worldPosition.getX() + level.getRandom().nextFloat();
        double ly = worldPosition.getY() + 1.0;
        double lz = worldPosition.getZ() + level.getRandom().nextFloat();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LAVA, lx, ly, lz, 1, 0, 0, 0, 0);
        } else {
            level.addParticle(ParticleTypes.LAVA, lx, ly, lz, 0, 0, 0);
        }
    }

    public void ejectItem(ItemStack items, ItemStack furnaceItemStack) {
        if (items.isEmpty() || level == null) return;

        List<ItemStack> ejecti = new ArrayList<>();
        ejecti.add(items.copy());

        int bellows = getBellows() + 1;
        for (int a = 0; a < bellows; ++a) {
            ItemStack[] boni = getSmeltingBonus(furnaceItemStack);
            if (boni != null) {
                for (ItemStack bonus : boni) {
                    if (!bonus.isEmpty() && bonus.getCount() > 0) {
                        ejecti.add(bonus);
                    }
                }
            }
        }

        Direction facing = getFacing(level.getBlockState(worldPosition));
        if (facing == null) facing = Direction.NORTH;
        Direction ejectDir = facing.getOpposite();
        for (ItemStack outItem : ejecti) {
            if (outItem.isEmpty()) continue;
            InventoryUtils.ejectStackAt(level, worldPosition, ejectDir, outItem);
        }

        spawnExperience(items);
    }

    private void spawnExperience(ItemStack result) {
        if (level == null) return;

        float xpf = getSmeltingExperience(result);
        int count = result.getCount();

        if (xpf == 0) {
            count = 0;
        } else if (xpf < 1.0f) {
            int xpAmount = Mth.floor(count * xpf);
            if (xpAmount < Mth.ceil(count * xpf) && level.getRandom().nextFloat() < count * xpf - xpAmount) {
                xpAmount++;
            }
            count = xpAmount;
        }

        Direction facing = getFacing(level.getBlockState(worldPosition));
        if (facing == null) facing = Direction.NORTH;

        float lx = worldPosition.getX() + 0.5f + facing.getOpposite().getStepX() * 1.2f;
        float lz = worldPosition.getZ() + 0.5f + facing.getOpposite().getStepZ() * 1.2f;

        while (count > 0) {
            int xpValue = ExperienceOrb.getExperienceValue(count);
            count -= xpValue;

            ExperienceOrb xp = new ExperienceOrb(level, lx, worldPosition.getY() + 0.4, lz, xpValue);
            float mx = facing.getOpposite().getStepX() == 0 ?
                    (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.025f :
                    facing.getOpposite().getStepX() * 0.13f;
            float mz = facing.getOpposite().getStepZ() == 0 ?
                    (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.025f :
                    facing.getOpposite().getStepZ() * 0.13f;
            xp.setDeltaMovement(mx, 0, mz);
            level.addFreshEntity(xp);
        }
    }

    private ItemStack[] getSmeltingBonus(ItemStack in) {
        if (in.isEmpty() || level == null) return new ItemStack[0];
        ArrayList<ItemStack> out = new ArrayList<>();
        for (ArcaneresearchApi.SmeltBonus bonus : CommonInternals.smeltingBonus) {
            if (bonus.in instanceof ItemStack bonusIn) {
                boolean itemMatch = in.is(bonusIn.getItem());

                boolean nbtWildcard = !bonusIn.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                boolean nbtMatch = nbtWildcard || ItemStack.isSameItemSameComponents(in, bonusIn);
                if (!itemMatch || !nbtMatch || level.getRandom().nextFloat() > bonus.chance) {
                    continue;
                }
                ItemStack is = bonus.out.copy();
                if (is.getCount() < 1) is.setCount(1);
                out.add(is);
            } else if (bonus.in instanceof String oreDict) {
                TagKey<Item> tag = oreDictToTag(oreDict);
                if (tag != null && in.is(tag)) {
                    if (level.getRandom().nextFloat() <= bonus.chance) {
                        ItemStack is2 = bonus.out.copy();
                        if (is2.getCount() < 1) is2.setCount(1);
                        out.add(is2);
                    }
                }
            }
        }
        return out.toArray(new ItemStack[0]);
    }

    private void setFacing(BlockState state) {
        Direction facing = getFacing(state);
        if (facing != null) {
            Direction opposite = facing.getOpposite();
            facingX = opposite.getStepX();
            facingZ = opposite.getStepZ();
        } else {
            facingX = 0;
            facingZ = -1;
        }
    }

    private Direction getFacing(BlockState state) {

        if (state.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)) {
            return state.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 3) {
            if (level != null && level.isClientSide()) {
                spawnLavaFX();
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnLavaFX() {
        for (int a = 0; a < 5; ++a) {
            FXDispatcher.INSTANCE.furnaceLavaFx(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), facingX, facingZ);
            level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + level.getRandom().nextFloat() * 0.1f,
                    0.9f + level.getRandom().nextFloat() * 0.15f, false);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            int[] slots = new int[getContainerSize()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
            return slots;
        }
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    
    
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 1.3, worldPosition.getY() - 1.3, worldPosition.getZ() - 1.3,
                worldPosition.getX() + 2.3, worldPosition.getY() + 2.3, worldPosition.getZ() + 2.3
        );
    }
}
