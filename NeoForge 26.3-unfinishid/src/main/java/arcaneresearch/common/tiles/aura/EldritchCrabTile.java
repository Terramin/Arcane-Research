package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason; // NEOFORGE_PORT: was EntitySpawnReason
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.entities.monster.EntityEldritchCrab;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Тайл крабового спавнера, порт TC4 TileEldritchCrabSpawner:
 * при игроке в 16 блоках и меньше 6 крабов в 32 — спавнит краба
 * в сторону facing. Частицы вентиляции пропущены (клиентский
 * прокси TC4), логика и NBT — дословно. Лежит в arcaneresearch.arcaneresearch.common.tiles.aura.
 */
public class EldritchCrabTile extends TileArcaneresearch {
    public int count = 150;
    private int ticks = 0;
    private byte facing = 0;
    /** Счётчик продувки вента TC4 (клиент, взводится событием блока 1). */
    private int venting = 0;

    public EldritchCrabTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchCrabTile tile) {
        tile.serverTick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EldritchCrabTile tile) {
        tile.clientTick();
    }

    /** TC4 receiveClientEvent(1): продувка вента 20 тиков. */
    public void onVentEvent() {
        this.venting = 20;
    }

    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        // На мирной сложности гнездо не спавнит (как ванильные спавнеры
        // мобов; живые крабы при этом исчезают сами — Monster despawn).
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return;
        }
        if (this.ticks == 0) {
            this.ticks = level.getRandom().nextInt(500);
        }
        this.ticks++;
        this.count--;
        if (this.count < 0) {
            this.count = 50 + level.getRandom().nextInt(50);
        } else {
            if (this.count == 15 && isActivated() && !maxEntitiesReached()) {
                level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, 0);
                level.playSound(null, this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
                        net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            if (this.count <= 0 && isActivated() && !maxEntitiesReached()) {
                this.count = 150 + level.getRandom().nextInt(100);
                spawnCrab();
                level.playSound(null, this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
                        ModSounds.GORE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }

    private boolean maxEntitiesReached() {
        Level level = getLevel();
        if (level == null) {
            return true;
        }
        AABB box = new AABB(this.worldPosition).inflate(32.0D, 32.0D, 32.0D);
        List<EntityEldritchCrab> crabs = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityEldritchCrab.class), box, e -> true);
        return crabs.size() > 5;
    }

    public boolean isActivated() {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        Vec3 center = new Vec3(this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D);
        for (ServerPlayer player : level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(ServerPlayer.class), new AABB(this.worldPosition).inflate(16.0D, 16.0D, 16.0D), e -> true)) {
            if (player.distanceToSqr(center) < 256.0D) {
                return true;
            }
        }
        return false;
    }

    private void spawnCrab() {
        Level level = getLevel();
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) {
            return;
        }
        // Мэппинг TC6 (0 север, 1 юг, 2 запад, 3 восток), не 2D-значения.
        Direction dir = switch (this.facing & 3) {
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            case 3 -> Direction.EAST;
            default -> Direction.NORTH;
        };
        EntityEldritchCrab crab = new EntityEldritchCrab(ModEntities.ELDRITCH_CRAB.get(), server);
        int x = this.worldPosition.getX() + dir.getStepX();
        int y = this.worldPosition.getY() + dir.getStepY();
        int z = this.worldPosition.getZ() + dir.getStepZ();
        crab.teleportTo(x + 0.5D, y + 0.5D, z + 0.5D);
        crab.setYRot(0.0F); crab.setXRot(0.0F);;
        try {
            crab.setHelm(false);
        } catch (Exception ignored) {
        }
        crab.setDeltaMovement(dir.getStepX() * 0.2F, dir.getStepY() * 0.2F, dir.getStepZ() * 0.2F);
        crab.finalizeSpawn(server, server.getCurrentDifficultyAt(new BlockPos(x, y, z)), EntitySpawnReason.EVENT, null);
        server.addFreshEntity(crab);
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }
        try {
            if (this.venting > 0) {
                this.venting--;
                for (int a = 0; a < 3; a++) {
                    drawVent();
                }
            } else if (level.getRandom().nextInt(20) == 0) {
                drawVent();
            }
        } catch (Exception ignored) {
        }
    }

    /** TC4 drawVent: дымок из решётки в сторону facing. */
    @OnlyIn(Dist.CLIENT)
    protected void drawVent() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        Direction dir = switch (this.facing & 3) {
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            case 3 -> Direction.EAST;
            default -> Direction.NORTH;
        };
        float fx = 0.15F - level.getRandom().nextFloat() * 0.3F;
        float fz = 0.15F - level.getRandom().nextFloat() * 0.3F;
        float fy = 0.15F - level.getRandom().nextFloat() * 0.3F;
        float fx2 = 0.1F - level.getRandom().nextFloat() * 0.2F;
        float fz2 = 0.1F - level.getRandom().nextFloat() * 0.2F;
        float fy2 = 0.1F - level.getRandom().nextFloat() * 0.2F;
        try {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawVentParticles(
                    this.worldPosition.getX() + 0.5F + fx + dir.getStepX() / 2.1F,
                    this.worldPosition.getY() + 0.5F + fy + dir.getStepY() / 2.1F,
                    this.worldPosition.getZ() + 0.5F + fz + dir.getStepZ() / 2.1F,
                    dir.getStepX() / 3.0F + fx2,
                    dir.getStepY() / 3.0F + fy2,
                    dir.getStepZ() / 3.0F + fz2,
                    10061994, 2.0F);
        } catch (Exception ignored) {
        }
    }

    public byte getFacing() {
        return this.facing;
    }

    public void setFacing(byte facing) {
        this.facing = facing;
        setChanged();
    }

    public void setFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                this.facing = 0;
                break;
            case SOUTH:
                this.facing = 1;
                break;
            case WEST:
                this.facing = 2;
                break;
            case EAST:
                this.facing = 3;
                break;
            default:
                this.facing = 0;
                break;
        }
        setChanged();
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putByte("facing", this.facing);
        tag.putInt("crabCount", this.count);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        this.facing = tag.getByteOr("facing", (byte) 0);
        this.count = tag.getIntOr("crabCount", 0);
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putByte("facing", this.facing);
        tag.putInt("crabCount", this.count);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.facing = tag.read("facing", com.mojang.serialization.Codec.BYTE).isPresent() ? tag.getByteOr("facing", (byte) 0) : 0;
        this.count = tag.getIntOr("crabCount", 150);
    }

    public AABB getRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 2, 2));
    }
}
