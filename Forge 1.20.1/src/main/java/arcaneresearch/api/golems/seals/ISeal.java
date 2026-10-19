package arcaneresearch.api.golems.seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.tasks.Task;
public interface ISeal {
    String getKey();
    boolean canPlaceAt(Level level, BlockPos pos, Direction side);
    void tickSeal(Level level, ISealEntity seal);
    void onTaskStarted(Level level, IGolemAPI golem, Task task);
    boolean onTaskCompletion(Level level, IGolemAPI golem, Task task);
    void onTaskSuspension(Level level, Task task);
    boolean canGolemPerformTask(IGolemAPI golem, Task task);
    void readCustomNBT(CompoundTag nbt);
    void writeCustomNBT(CompoundTag nbt);
    ResourceLocation getSealIcon();
    void onRemoval(Level level, BlockPos pos, Direction side);
    Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal);
    @OnlyIn(Dist.CLIENT)
    Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal);
    EnumGolemTrait[] getRequiredTags();
    EnumGolemTrait[] getForbiddenTags();
}
