package arcaneresearch.api.golems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.api.golems.tasks.Task;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
public class GolemHelper {
    public static final HashMap<ResourceKey<Level>, ArrayList<ProvisionRequest>> provisionRequests = new HashMap<>();
    private static final int LIST_LIMIT = 1000;
    static final int LISTLIMIT = 1000;
    public static void registerSeal(ISeal seal) {
        ArcaneresearchApi.internalMethods.registerSeal(seal);
    }
    @Nullable
    public static ISeal getSeal(String key) {
        return ArcaneresearchApi.internalMethods.getSeal(key);
    }
    public static ItemStack getSealStack(String key) {
        return ArcaneresearchApi.internalMethods.getSealStack(key);
    }
    @Nullable
    public static ISealEntity getSealEntity(ResourceKey<Level> dimension, SealPos pos) {
        return ArcaneresearchApi.internalMethods.getSealEntity(dimension.location().toString(), pos);
    }
    public static void addGolemTask(ResourceKey<Level> dimension, Task task) {
        ArcaneresearchApi.internalMethods.addGolemTask(dimension.location().toString(), task);
    }
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(dim, k -> new ArrayList<>());
        ProvisionRequest pr = new ProvisionRequest(seal, stack.copy());
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LIST_LIMIT) list.remove(0);
    }
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(dim, k -> new ArrayList<>());
        ProvisionRequest pr = new ProvisionRequest(pos, side, stack.copy());
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LIST_LIMIT) list.remove(0);
    }
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(dim, k -> new ArrayList<>());
        ProvisionRequest pr = new ProvisionRequest(entity, stack.copy());
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LIST_LIMIT) list.remove(0);
    }
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int ui) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(dim, k -> new ArrayList<>());
        ProvisionRequest pr = new ProvisionRequest(pos, side, stack.copy());
        pr.setUI(ui);
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LIST_LIMIT) list.remove(0);
    }
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int ui) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(dim, k -> new ArrayList<>());
        ProvisionRequest pr = new ProvisionRequest(entity, stack.copy());
        pr.setUI(ui);
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LIST_LIMIT) list.remove(0);
    }
    public static BlockPos getPosInArea(ISealEntity seal, int count) {
        Direction face = seal.getSealPos().face;
        BlockPos area = seal.getArea();
        int xx = 1 + (area.getX() - 1) * (face.getStepX() == 0 ? 2 : 1);
        int yy = 1 + (area.getY() - 1) * (face.getStepY() == 0 ? 2 : 1);
        int zz = 1 + (area.getZ() - 1) * (face.getStepZ() == 0 ? 2 : 1);
        int qx = face.getStepX() != 0 ? face.getStepX() : 1;
        int qy = face.getStepY() != 0 ? face.getStepY() : 1;
        int qz = face.getStepZ() != 0 ? face.getStepZ() : 1;
        int y = qy * count / zz / xx % yy + face.getStepY();
        int x = qx * count / zz % xx + face.getStepX();
        int z = qz * count % zz + face.getStepZ();
        return seal.getSealPos().pos.offset(
                x - (face.getStepX() == 0 ? xx / 2 : 0),
                y - (face.getStepY() == 0 ? yy / 2 : 0),
                z - (face.getStepZ() == 0 ? zz / 2 : 0));
    }
    public static AABB getBoundsForArea(ISealEntity seal) {
        SealPos sealPos = seal.getSealPos();
        BlockPos pos = sealPos.pos;
        Direction face = sealPos.face;
        BlockPos area = seal.getArea();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                       pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
                .move(face.getStepX(), face.getStepY(), face.getStepZ())
                .expandTowards(
                        face.getStepX() != 0 ? (area.getX() - 1) * face.getStepX() : 0,
                        face.getStepY() != 0 ? (area.getY() - 1) * face.getStepY() : 0,
                        face.getStepZ() != 0 ? (area.getZ() - 1) * face.getStepZ() : 0)
                .inflate(
                        face.getStepX() == 0 ? area.getX() - 1 : 0,
                        face.getStepY() == 0 ? area.getY() - 1 : 0,
                        face.getStepZ() == 0 ? area.getZ() - 1 : 0);
    }
}
