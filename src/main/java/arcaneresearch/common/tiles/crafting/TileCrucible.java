package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.transfer.fluid.FluidResource; 
import arcaneresearch.common.lib.fluid.ArcaneresearchFluidTank; 
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.EntitySpecialItem;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TileCrucible extends TileArcaneresearch implements IAspectContainer {

    public static final int MAX_ASPECTS = 500;
    public static final int TANK_CAPACITY = 1000;
    
    
    
    private final ArcaneresearchFluidTank tank = new ArcaneresearchFluidTank(TANK_CAPACITY,
            stack -> stack.getFluid() == Fluids.WATER);

    @javax.annotation.Nullable
    public net.neoforged.neoforge.transfer.ResourceHandler<FluidResource> getFluidHandler(
            net.minecraft.core.Direction side) {
        return tank;
    }
    public short heat = 0;
    public AspectList aspects = new AspectList();
    private int bellows = -1;
    private long tickCounter = -100;

    private int delay = 0;
    private int prevcolor = 0;
    private int prevx = 0;
    private int prevy = 0;

    public TileCrucible(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCrucible tile) {
        tile.tickCounter++;
        int prevHeat = tile.heat;

        if (tile.tank.getFluidAmount() > 0) {
            BlockState below = level.getBlockState(pos.below());
            if (tile.isHeatSource(below)) {
                if (tile.heat < 200) {
                    tile.heat++;
                    if (prevHeat < 151 && tile.heat >= 151) {
                        tile.markDirtyAndSync();
                    }
                }
            } else if (tile.heat > 0) {
                tile.heat--;
                if (tile.heat == 149) {
                    tile.markDirtyAndSync();
                }
            }
        } else if (tile.heat > 0) {
            tile.heat--;
        }

        if (tile.aspects.visSize() > MAX_ASPECTS) {
            tile.spillRandom();
        }

        if (tile.tickCounter >= 100) {
            tile.spillRandom();
            tile.tickCounter = 0;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCrucible tile) {
        if (tile.tank.getFluidAmount() > 0) {
            tile.drawEffects();
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putShort("Heat", heat);
        tank.writeToNBT(tag);
        aspects.writeToNBT(tag);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        heat = (short) tag.getShortOr("Heat", (short) 0);
        tank.readFromNBT(tag);
        aspects.readFromNBT(tag);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawEffects() {
        if (level == null || !level.isClientSide()) return;
        if (heat > 150) {
            FXDispatcher.INSTANCE.crucibleFroth(
                    worldPosition.getX() + 0.2f + level.getRandom().nextFloat() * 0.6f,
                    worldPosition.getY() + getFluidHeight(),
                    worldPosition.getZ() + 0.2f + level.getRandom().nextFloat() * 0.6f);
            if (aspects.visSize() > 500) {
                for (int a = 0; a < 2; ++a) {
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX(),
                            worldPosition.getY() + 1, worldPosition.getZ() + level.getRandom().nextFloat());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + 1,
                            worldPosition.getY() + 1, worldPosition.getZ() + level.getRandom().nextFloat());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + level.getRandom().nextFloat(),
                            worldPosition.getY() + 1, worldPosition.getZ());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + level.getRandom().nextFloat(),
                            worldPosition.getY() + 1, worldPosition.getZ() + 1);
                }
            }
        }
        if (level.getRandom().nextInt(6) == 0 && aspects.size() > 0) {
            Aspect[] arr = aspects.getAspects();
            int color = arr[level.getRandom().nextInt(arr.length)].getColor();
            int x = 5 + level.getRandom().nextInt(22);
            int y = 5 + level.getRandom().nextInt(22);
            delay = level.getRandom().nextInt(10);
            prevcolor = color;
            prevx = x;
            prevy = y;
            java.awt.Color c = new java.awt.Color(color);
            float r = c.getRed() / 255.0f;
            float g = c.getGreen() / 255.0f;
            float b = c.getBlue() / 255.0f;
            FXDispatcher.INSTANCE.crucibleBubble(worldPosition.getX() + x / 32.0f + 0.015625f,
                    worldPosition.getY() + 0.05f + getFluidHeight(),
                    worldPosition.getZ() + y / 32.0f + 0.015625f, r, g, b);
        }
    }

    private boolean isHeatSource(BlockState state) {

        return state.is(Blocks.LAVA) ||
                state.is(Blocks.FIRE) ||
                state.is(Blocks.SOUL_FIRE) ||
                state.is(Blocks.MAGMA_BLOCK) ||
                state.getBlock() instanceof arcaneresearch.common.blocks.misc.BlockNitor;
    }

    public void attemptSmelt(ItemEntity itemEntity) {
        if (level == null || level.isClientSide()) return;
        if (heat < 151 || tank.getFluidAmount() <= 0) return;

        ItemStack stack = itemEntity.getItem();

        String thrower = itemEntity.getOwner() != null ?
                itemEntity.getOwner().getName().getString() : "";

        ItemStack result = attemptSmelt(stack, thrower);
        if (result == null || result.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(result);
        }
    }

    public ItemStack attemptSmelt(ItemStack item, String username) {
        if (level == null) return item;

        boolean bubble = false;
        boolean craftDone = false;
        int stacksize = item.getCount();
        Player player = null;
        if (!username.isEmpty()) {
            player = level.players().stream()
                    .filter(p -> p.getName().getString().equals(username)).findFirst().orElse(null);
        }

        for (int a = 0; a < stacksize; ++a) {
            CrucibleRecipeType recipe = ArcaneresearchCraftingManager.findMatchingCrucibleRecipe(aspects, item, player, level);
            if (recipe != null && tank.getFluidAmount() > 0) {
                
                
                ItemStack out = recipe.assemble(new net.minecraft.world.item.crafting.RecipeInput() {
                    @Override
                    public ItemStack getItem(int index) {
                        return ItemStack.EMPTY;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }
                }).copy();
                if (player != null) {
                    net.neoforged.neoforge.event.EventHooks.firePlayerCraftingEvent(player, out,
                            new net.minecraft.world.SimpleContainer(item.copy()));
                }
                aspects = recipe.removeMatchingAspects(aspects);
                tank.drain(50, true);
                ejectItem(out);
                craftDone = true;
                --stacksize;
                tickCounter = -250;
            } else {
                AspectList itemAspects = getItemAspects(item);
                if (itemAspects != null && itemAspects.size() != 0) {
                    for (Aspect tag : itemAspects.getAspects()) {
                        aspects.add(tag, itemAspects.getAmount(tag));
                    }
                    bubble = true;
                    --stacksize;
                    tickCounter = -150;
                }
            }
        }

        if (bubble) {
            level.playSound(null, worldPosition, ModSounds.BUBBLE.get(),
                    SoundSource.BLOCKS, 0.2f, 1.0f + level.getRandom().nextFloat() * 0.4f);
            markDirtyAndSync();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 1);
        }
        if (craftDone) {
            markDirtyAndSync();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 99, 0);
        }
        setChanged();

        if (stacksize <= 0) {
            return null;
        }
        item.setCount(stacksize);
        return item;
    }

    private AspectList getItemAspects(ItemStack item) {
        return ArcaneresearchCraftingManager.getObjectTags(item);
    }

    public void ejectItem(ItemStack items) {
        if (level == null || level.isClientSide()) return;
        boolean first = true;
        while (items.getCount() > 0) {
            ItemStack spitout = items.copy();
            if (spitout.getCount() > spitout.getMaxStackSize()) {
                spitout.setCount(spitout.getMaxStackSize());
            }
            items.shrink(spitout.getCount());
            EntitySpecialItem entityitem = new EntitySpecialItem(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5,
                    spitout);
            entityitem.setDeltaMovement(
                    first ? 0.0 : (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.01f,
                    0.075,
                    first ? 0.0 : (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.01f);
            level.addFreshEntity(entityitem);
            first = false;
        }
    }

    public void spillRandom() {
        if (level == null || aspects.size() <= 0) return;

        Aspect[] aspectArray = aspects.getAspects();
        if (aspectArray.length > 0) {
            Aspect toSpill = aspectArray[level.getRandom().nextInt(aspectArray.length)];
            aspects.remove(toSpill, 1);

            float pollution = (toSpill == Aspect.FLUX) ? 1.0f : 0.25f;
            AuraHelper.polluteAura(level, worldPosition, pollution, true);
        }
        markDirtyAndSync();
    }

    public void spillAll() {
        if (level == null) return;

        int total = aspects.visSize();
        if (tank.getFluidAmount() > 0 || total > 0) {
            tank.drain(TANK_CAPACITY, true);
            AuraHelper.polluteAura(level, worldPosition, total * 0.25f, true);

            int flux = aspects.getAmount(Aspect.FLUX);
            if (flux > 0) {
                AuraHelper.polluteAura(level, worldPosition, flux * 0.75f, false);
            }

            aspects = new AspectList();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 5);
            markDirtyAndSync();
        }
    }

    public void spillRemnants() {
        spillAll();
    }

    
    
    
    
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        spillAll();
        super.preRemoveSideEffects(pos, oldState);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (level != null && level.isClientSide() && clientTriggerEvent(id, param)) {
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean clientTriggerEvent(int id, int param) {
        if (id == 99) {
            FXDispatcher.INSTANCE.drawBamf(worldPosition.getX() + 0.5, worldPosition.getY() + 1.25f,
                    worldPosition.getZ() + 0.5, true, true, Direction.UP);
            level.playLocalSound(worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f,
                    worldPosition.getZ() + 0.5f, ModSounds.SPILL.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            return true;
        }
        if (id == 1) {
            FXDispatcher.INSTANCE.drawBamf(worldPosition.above(), true, true, Direction.UP);
            return true;
        }
        if (id == 2) {
            for (int q = 0; q < 10; ++q) {
                FXDispatcher.INSTANCE.crucibleBoil(worldPosition, this, param);
            }
            return true;
        }
        return false;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {

    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    public ArcaneresearchFluidTank getTank() {
        return tank;
    }

    public float getFluidHeight() {
        float base = 0.3f + 0.5f * (tank.getFluidAmount() / (float) TANK_CAPACITY);
        float out = base + aspects.visSize() / (float) MAX_ASPECTS * (1.0f - base);
        if (out > 1.0f) {
            out = 1.001f;
        }
        if (out == 1.0f) {
            out = 0.9999f;
        }
        return out;
    }

    
    
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1);
    }

    public boolean isHeated() {
        return heat >= 151;
    }
}
