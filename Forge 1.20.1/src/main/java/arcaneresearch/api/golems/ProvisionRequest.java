package arcaneresearch.api.golems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
public class ProvisionRequest {
    private ISealEntity seal;
    private Entity entity;
    private BlockPos pos;
    private Direction side;
    private ItemStack stack;
    private int id;
    private int ui = 0;
    private Task linkedTask;
    private boolean invalid;
    private long timeout;
    ProvisionRequest(ISealEntity seal, ItemStack stack) {
        this.seal = seal;
        this.stack = stack.copy();
        String s = seal.getSealPos().pos.toString() + seal.getSealPos().face.name() + stack.toString();
        if (stack.hasTag()) s += stack.getTag().toString();
        id = s.hashCode();
        timeout = System.currentTimeMillis() + 10000;
    }
    ProvisionRequest(BlockPos pos, Direction side, ItemStack stack) {
        this.pos = pos;
        this.side = side;
        this.stack = stack.copy();
        String s = pos.toString() + side.name() + stack.toString();
        if (stack.hasTag()) s += stack.getTag().toString();
        id = s.hashCode();
        timeout = System.currentTimeMillis() + 10000;
    }
    ProvisionRequest(Entity entity, ItemStack stack) {
        this.entity = entity;
        this.stack = stack.copy();
        String s = entity.getId() + stack.toString();
        if (stack.hasTag()) s += stack.getTag().toString();
        id = s.hashCode();
        timeout = System.currentTimeMillis() + 10000;
    }
    public long getTimeout() {
        return timeout;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setUI(int ui) {
        this.ui = ui;
    }
    public ISealEntity getSeal() {
        return seal;
    }
    public Entity getEntity() {
        return entity;
    }
    public ItemStack getStack() {
        return stack;
    }
    public BlockPos getPos() {
        return pos;
    }
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
    public Direction getSide() {
        return side;
    }
    public void setSide(Direction side) {
        this.side = side;
    }
    public Task getLinkedTask() {
        return linkedTask;
    }
    public void setLinkedTask(Task linkedTask) {
        this.linkedTask = linkedTask;
        timeout = System.currentTimeMillis() + 120000;
    }
    public boolean isInvalid() {
        return invalid;
    }
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProvisionRequest pr)) {
            return false;
        }
        return id == pr.id && ui == pr.ui;
    }
    @Override
    public int hashCode() {
        return 31 * id + ui;
    }
    private boolean isItemStackEqual(ItemStack first, ItemStack other) {
        if (first.getCount() != other.getCount()) return false;
        if (first.getItem() != other.getItem()) return false;
        if (first.getDamageValue() != other.getDamageValue()) return false;
        if (first.getTag() == null && other.getTag() != null) return false;
        return first.getTag() == null || first.getTag().equals(other.getTag());
    }
}
