package arcaneresearch.common.lib.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.IDustTrigger;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.common.lib.events.ServerEvents;
import arcaneresearch.common.lib.events.ToolEvents;
import arcaneresearch.common.lib.utils.BlockUtils;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
public class DustTriggerMultiblock implements IDustTrigger {
    private final Part[][][] blueprint;
    private final String research;
    private final int ySize;
    private final int xSize;
    private final int zSize;
    public DustTriggerMultiblock(@Nullable String research, Part[][][] blueprint) {
        this.blueprint = blueprint;
        this.research = research;
        this.ySize = blueprint.length;
        this.xSize = blueprint[0].length;
        this.zSize = blueprint[0][0].length;
    }
    @Override
    public Placement getValidFace(Level level, Player player, BlockPos pos, Direction face) {
        if (research != null && !ArcaneresearchCapabilities.knowsResearch(player, research)) {
            return null;
        }
        for (int yy = -ySize; yy <= 0; ++yy) {
            for (int xx = -xSize; xx <= 0; ++xx) {
                for (int zz = -zSize; zz <= 0; ++zz) {
                    BlockPos p2 = pos.offset(xx, yy, zz);
                    Direction f = fitMultiblock(level, p2);
                    if (f != null) {
                        return new Placement(xx, yy, zz, f);
                    }
                }
            }
        }
        return null;
    }
    @Nullable
    private Direction fitMultiblock(Level level, BlockPos pos) {
        Direction[] horizontals = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction face : horizontals) {
            if (checkRotation(level, pos, face)) {
                return face;
            }
        }
        return null;
    }
    private boolean checkRotation(Level level, BlockPos pos, Direction face) {
        for (int y = 0; y < ySize; ++y) {
            Matrix matrix = new Matrix(blueprint[y]);
            matrix.Rotate90DegRight(getRotationCount(face));
            for (int x = 0; x < matrix.getRows(); ++x) {
                for (int z = 0; z < matrix.getCols(); ++z) {
                    Part part = matrix.getMatrix()[x][z];
                    if (part != null) {
                        BlockPos checkPos = pos.offset(x, -y + (ySize - 1), z);
                        BlockState worldState = level.getBlockState(checkPos);
                        if (!matchesSource(part.getSource(), worldState)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    private boolean matchesSource(Object source, BlockState worldState) {
        if (source == null) {
            return true;
        }
        if (source instanceof Block block) {
            return worldState.is(block);
        }
        if (source instanceof BlockState state) {
            return worldState.equals(state);
        }
        if (source instanceof MapColor mapColor) {
            return worldState.getMapColor(null, null) == mapColor;
        }
        if (source instanceof ItemStack stack) {
            Block blockFromItem = Block.byItem(stack.getItem());
            return worldState.is(blockFromItem);
        }
        return false;
    }
    private int getRotationCount(Direction face) {
        return 3 - face.get2DDataValue();
    }
    @Override
    public List<BlockPos> sparkle(Level level, Player player, BlockPos pos, Placement placement) {
        BlockPos p2 = pos.offset(placement.xOffset, placement.yOffset, placement.zOffset);
        ArrayList<BlockPos> list = new ArrayList<>();
        for (int y = 0; y < ySize; ++y) {
            Matrix matrix = new Matrix(blueprint[y]);
            matrix.Rotate90DegRight(getRotationCount(placement.facing));
            for (int x = 0; x < matrix.getRows(); ++x) {
                for (int z = 0; z < matrix.getCols(); ++z) {
                    Part part = matrix.getMatrix()[x][z];
                    if (part != null) {
                        BlockPos p3 = p2.offset(x, -y + (ySize - 1), z);
                        if (part.getSource() != null && BlockUtils.isBlockExposed(level, p3)) {
                            list.add(p3);
                        }
                    }
                }
            }
        }
        return list;
    }
    @Override
    public void execute(Level level, Player player, BlockPos pos, Placement placement, Direction side) {
        if (level.isClientSide()) {
            return;
        }
        ItemStack craftResult = ItemStack.EMPTY;
        try {
            craftResult = new ItemStack(arcaneresearch.init.ModBlocks.INFERNAL_FURNACE.get());
        } catch (Exception ignored) {
        }
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, craftResult, null));
        BlockPos p2 = pos.offset(placement.xOffset, placement.yOffset, placement.zOffset);
        for (int y = 0; y < ySize; ++y) {
            Matrix matrix = new Matrix(blueprint[y]);
            matrix.Rotate90DegRight(getRotationCount(placement.facing));
            for (int x = 0; x < matrix.getRows(); ++x) {
                for (int z = 0; z < matrix.getCols(); ++z) {
                    Part part = matrix.getMatrix()[x][z];
                    if (part == null || part.getTarget() == null) {
                        continue;
                    }
                    BlockPos p3 = p2.offset(x, -y + (ySize - 1), z);
                    final BlockPos finalPos = p3;
                    ItemStack targetStack = getTargetStack(part, placement.facing, side, player);
                    final BlockState targetState = getTargetState(part, placement.facing, side, player);
                    BlockState sourceState = level.getBlockState(p3);
                    ToolEvents.addBlockedBlock(level, p3);
                    ServerEvents.addRunnableServer(level, () -> {
                        ServerEvents.addSwapper(level, finalPos, sourceState, targetStack, targetState,
                                false, 0, player, true, false, -9999,
                                false, false, 0, ServerEvents.DEFAULT_PREDICATE, 0.0f);
                        ToolEvents.clearBlockedBlock(level, finalPos);
                    }, part.getPriority());
                }
            }
        }
    }
    private ItemStack getTargetStack(Part part, Direction placementFacing, Direction clickSide, Player player) {
        Object target = part.getTarget();
        if (target instanceof String s) {
            if ("AIR".equals(s)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        if (target instanceof Block block) {
            return new ItemStack(block);
        }
        if (target instanceof ItemStack stack) {
            return stack.copy();
        }
        if (target instanceof BlockState state) {
            return new ItemStack(state.getBlock());
        }
        return ItemStack.EMPTY;
    }
    private BlockState getTargetState(Part part, Direction placementFacing, Direction clickSide, Player player) {
        Object target = part.getTarget();
        if (target instanceof BlockState exact) {
            return exact;
        }
        Block block = null;
        if (target instanceof Block b) {
            block = b;
        } else if (target instanceof ItemStack stack) {
            block = Block.byItem(stack.getItem());
            if (block == net.minecraft.world.level.block.Blocks.AIR) {
                return null;
            }
        } else {
            return null;
        }
        BlockState targetState = block.defaultBlockState();
        DirectionProperty facingProp = null;
        if (targetState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facingProp = HorizontalDirectionalBlock.FACING;
        } else if (targetState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
            facingProp = net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
        } else {
            return null;
        }
        Direction facing;
        if (part.getApplyPlayerFacing()) {
            facing = clickSide.getAxis().isHorizontal() ? clickSide : player.getDirection().getOpposite();
        } else if (part.isOpp()) {
            facing = placementFacing.getOpposite();
        } else {
            facing = placementFacing;
        }
        if (facingProp == HorizontalDirectionalBlock.FACING && !facing.getAxis().isHorizontal()) {
            facing = placementFacing;
        }
        try {
            return targetState.setValue(facingProp, facing);
        } catch (Exception e) {
            return null;
        }
    }
}
