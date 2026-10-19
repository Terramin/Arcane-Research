package arcaneresearch.common.entities.construct;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBoreDig;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class EntityArcaneBore extends EntityOwnedConstruct {
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.BOOLEAN);
    private static final HashMap<Integer, ArrayList<ItemStack>> drops = new HashMap<>();
    private static final float EYE_HEIGHT = 0.8125f;
    public int spiral = 0;
    public float currentRadius = 0.0f;
    public boolean clientDigging = false;
    private BlockPos digTarget = null;
    private BlockPos digTargetPrev = null;
    private float digCost = 0.25f;
    private int digDelay = 0;
    private int digDelayMax = 0;
    private int breakCounter = 0;
    private float radInc = 1.0f;
    private float charge = 0.0f;
    private long soundDelay = 0L;
    public EntityArcaneBore(EntityType<? extends EntityArcaneBore> type, Level level) {
        super(type, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return EntityOwnedConstruct.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FACING, Direction.DOWN);
        this.entityData.define(ACTIVE, false);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Charge", charge);
        tag.putByte("Facing", (byte) getFacing().ordinal());
        tag.putBoolean("Active", isActive());
        tag.putInt("Spiral", spiral);
        tag.putFloat("Radius", currentRadius);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        charge = tag.getFloat("Charge");
        setFacing(Direction.values()[tag.getByte("Facing") % Direction.values().length]);
        setActive(tag.getBoolean("Active"));
        spiral = tag.getInt("Spiral");
        currentRadius = tag.getFloat("Radius");
    }
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (tickCount % 50 == 0) {
                heal(1.0f);
            }
            if (tickCount % 10 == 0 && charge < 10.0f) {
                rechargeVis();
            }
            int k = net.minecraft.util.Mth.floor(getX());
            int l = net.minecraft.util.Mth.floor(getY());
            int i1 = net.minecraft.util.Mth.floor(getZ());
            if (net.minecraft.world.level.block.BaseRailBlock.isRail(level(), new BlockPos(k, l - 1, i1))) {
                l--;
            }
            net.minecraft.core.BlockPos railPos = new BlockPos(k, l, i1);
            net.minecraft.world.level.block.state.BlockState railState = level().getBlockState(railPos);
            if (net.minecraft.world.level.block.BaseRailBlock.isRail(railState)) {
                if (railState.is(arcaneresearch.init.ModBlocks.ACTIVATOR_RAIL.get())) {
                    setActive(!railState.getValue(
                            net.minecraft.world.level.block.PoweredRailBlock.POWERED));
                }
            } else if (!isPassenger()) {
                setActive(level().hasNeighborSignal(blockPosition().below()));
            }
            if (validInventory()) {
            }
        }
        if (!isActive()) {
            digTarget = null;
        }
        if (digTarget != null && charge >= digCost && !level().isClientSide) {
            if (digDelay-- <= 0 && dig()) {
                charge -= digCost;
                if (soundDelay < System.currentTimeMillis()) {
                    soundDelay = System.currentTimeMillis() + 1200L + random.nextInt(100);
                    if (ModSounds.RUMBLE != null) {
                        playSound(ModSounds.RUMBLE.get(), 0.25f, 0.9f + random.nextFloat() * 0.2f);
                    }
                }
            }
        }
        if (!level().isClientSide && digTarget == null && isActive() && validInventory()) {
            findNextBlockToDig();
            if (digTarget != null) {
                level().broadcastEntityEvent(this, (byte) 16);
                PacketHandler.sendToAllTrackingChunk(
                        new PacketFXBoreDig(digTarget, this, digDelayMax),
                        (ServerLevel) level(), digTarget);
            } else {
                level().broadcastEntityEvent(this, (byte) 17);
            }
        }
    }
    public boolean validInventory() {
        ItemStack held = getMainHandItem();
        if (held.isEmpty()) return false;
        boolean isPickaxe = held.getItem() instanceof PickaxeItem ||
                held.getItem().canPerformAction(held, net.minecraftforge.common.ToolActions.PICKAXE_DIG);
        if (!isPickaxe) return false;
        return held.getDamageValue() + 1 < held.getMaxDamage();
    }
    public int getDigRadius() {
        int r = 0;
        ItemStack held = getMainHandItem();
        if (!held.isEmpty() && (held.getItem() instanceof PickaxeItem ||
                held.getItem().canPerformAction(held, net.minecraftforge.common.ToolActions.PICKAXE_DIG))) {
            r = held.getItem().getEnchantmentValue() / 3;
            r += EnumInfusionEnchantment.getInfusionEnchantmentLevel(held, EnumInfusionEnchantment.DESTRUCTIVE) * 2;
        }
        return (r <= 1) ? 2 : r;
    }
    public int getDigDepth() {
        int r = getDigRadius() * 8;
        ItemStack held = getMainHandItem();
        if (!held.isEmpty()) {
            r += EnumInfusionEnchantment.getInfusionEnchantmentLevel(held, EnumInfusionEnchantment.BURROWING) * 16;
        }
        return r;
    }
    public int getFortune() {
        int r = 0;
        if (validInventory()) {
            r = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, getMainHandItem());
            int r2 = EnumInfusionEnchantment.getInfusionEnchantmentLevel(getMainHandItem(), EnumInfusionEnchantment.SOUNDING);
            r = Math.max(r, r2);
        }
        return r;
    }
    public int getDigSpeed(BlockState blockState) {
        if (!validInventory()) return 0;
        ItemStack held = getMainHandItem();
        int speed = (int) (held.getDestroySpeed(blockState) / 2.0f);
        speed += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, held);
        return speed;
    }
    public boolean hasSilkTouch() {
        if (!validInventory()) return false;
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, getMainHandItem()) > 0;
    }
    private boolean canSilkTouch(BlockPos pos, BlockState state) {
        return hasSilkTouch() && !state.hasBlockEntity()
                && state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL;
    }
    public int getRefining() {
        if (!validInventory()) return 0;
        return EnumInfusionEnchantment.getInfusionEnchantmentLevel(getMainHandItem(), EnumInfusionEnchantment.REFINING);
    }
    private boolean dig() {
        if (digTarget == null || level().isEmptyBlock(digTarget)) {
            digTarget = null;
            return false;
        }
        BlockState digBs = level().getBlockState(digTarget);
        if (digBs.isAir()) {
            digTarget = null;
            return false;
        }
        boolean b = false;
        if (level() instanceof ServerLevel serverLevel) {
            int fortune = getFortune();
            boolean silktouch = canSilkTouch(digTarget, digBs);
            if (silktouch) {
                fortune = 0;
            }
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
            fakePlayer.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            ItemStack held = getMainHandItem().copy();
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, held);
            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(
                    serverLevel, fakePlayer.gameMode.getGameModeForPlayer(), fakePlayer, digTarget);
            if (exp == -1) {
                digTarget = null;
                return false;
            }
            ItemStack effectiveTool = held;
            int existingFortune = held.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            if (fortune > existingFortune) {
                effectiveTool = held.copy();
                java.util.Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchMap =
                        EnchantmentHelper.getEnchantments(held);
                enchMap.put(Enchantments.BLOCK_FORTUNE, fortune);
                EnchantmentHelper.setEnchantments(enchMap, effectiveTool);
            }
            net.minecraft.world.level.block.entity.BlockEntity blockEntity =
                    level().getBlockEntity(digTarget);
            List<ItemStack> items = net.minecraft.world.level.block.Block.getDrops(
                    digBs, serverLevel, digTarget, blockEntity, fakePlayer, effectiveTool);
            serverLevel.levelEvent(null, 2001, digTarget,
                    net.minecraft.world.level.block.Block.getId(digBs));
            serverLevel.destroyBlock(digTarget, false);
            if (exp > 0) {
                digBs.getBlock().popExperience(serverLevel, digTarget, exp);
            }
            List<ItemEntity> targets = level().getEntitiesOfClass(ItemEntity.class,
                    new AABB(digTarget).inflate(1.5));
            for (ItemEntity item : targets) {
                items.add(item.getItem().copy());
                item.discard();
            }
            int refining = getRefining();
            for (ItemStack is : items) {
                ItemStack dropped = is.copy();
                if (!silktouch && refining > 0) {
                    dropped = arcaneresearch.common.lib.utils.Utils.findSpecialMiningResult(
                            is, (refining + 1) * 0.125f, level().random);
                }
                if (dropped != null && !dropped.isEmpty()) {
                    boolean inserted = false;
                    for (Direction f : Direction.values()) {
                        BlockPos p = blockPosition().relative(f);
                        if (arcaneresearch.api.ArcaneresearchInvHelper.getItemHandlerAt(level(), p, f) != null) {
                            arcaneresearch.common.lib.utils.InventoryUtils.ejectStackAt(
                                    level(), blockPosition(), f, dropped);
                            inserted = true;
                            break;
                        }
                    }
                    if (inserted) {
                        continue;
                    }
                    arcaneresearch.common.lib.utils.InventoryUtils.ejectStackAt(
                            level(), blockPosition(), getFacing().getOpposite(), dropped);
                }
            }
            breakCounter++;
            if (breakCounter >= 50) {
                breakCounter -= 50;
                ItemStack heldStack = getMainHandItem();
                heldStack.hurtAndBreak(1, this, (e) -> {
                });
            }
            b = true;
        }
        digTarget = null;
        return b;
    }
    private void findNextBlockToDig() {
        if (digTargetPrev == null || distanceToSqr(Vec3.atCenterOf(digTargetPrev)) > (getDigRadius() + 1) * (getDigRadius() + 1)) {
            digTargetPrev = blockPosition();
        }
        if (radInc == 0.0f) {
            radInc = 1.0f;
        }
        int digRadius = getDigRadius();
        int digDepth = getDigDepth();
        BlockPos end = digTargetPrev.relative(getFacing(), digDepth);
        Vec3 start = Vec3.atCenterOf(digTargetPrev);
        Vec3 endVec = Vec3.atCenterOf(end);
        BlockHitResult hit = level().clip(new ClipContext(
                start, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            Direction facing = getFacing();
            Vec3 digger = new Vec3(getX() + facing.getStepX(),
                    getY() + EYE_HEIGHT + facing.getStepY(),
                    getZ() + facing.getStepZ());
            hit = level().clip(new ClipContext(
                    digger, Vec3.atCenterOf(hit.getBlockPos()),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hit.getBlockPos();
                BlockState state = level().getBlockState(hitPos);
                if (state.getDestroySpeed(level(), hitPos) > -1.0f
                        && !state.getCollisionShape(level(), hitPos).isEmpty()) {
                    digDelay = Math.max(10 - getDigSpeed(state),
                            (int) (state.getDestroySpeed(level(), hitPos) * 2.0f) - getDigSpeed(state) * 2);
                    if (digDelay < 1) {
                        digDelay = 1;
                    }
                    digDelayMax = digDelay;
                    if (!hitPos.equals(blockPosition()) && !hitPos.equals(blockPosition().below())) {
                        digTarget = hitPos;
                        return;
                    }
                }
            }
        }
        updateSpiralTarget(digRadius);
    }
    private void updateSpiralTarget(int digRadius) {
        int x = digTargetPrev.getX();
        int y = digTargetPrev.getY();
        int z = digTargetPrev.getZ();
        Direction facing = getFacing();
        int guard = 0;
        while (x == digTargetPrev.getX() && z == digTargetPrev.getZ() && y == digTargetPrev.getY()
                && guard++ < 512) {
            if (Math.abs(currentRadius) > digRadius) {
                currentRadius = digRadius;
            }
            spiral += (int) (3.0f + Math.max(0.0f, (10.0f - Math.abs(currentRadius)) * 2.0f));
            if (spiral >= 360) {
                spiral -= 360;
                currentRadius += radInc;
                if (currentRadius > digRadius || currentRadius < -digRadius) {
                    currentRadius = 0.0f;
                }
            }
            Vec3 vsource = new Vec3(net.minecraft.util.Mth.floor(getX()) + 0.5 + facing.getStepX(),
                    getY() + facing.getStepY() + EYE_HEIGHT,
                    net.minecraft.util.Mth.floor(getZ()) + 0.5 + facing.getStepZ());
            Vec3 vtar = new Vec3(0.0, currentRadius, 0.0);
            vtar = arcaneresearch.common.lib.utils.Utils.rotateAroundZ(vtar, spiral / 180.0f * (float) Math.PI);
            vtar = arcaneresearch.common.lib.utils.Utils.rotateAroundY(vtar, 1.5707964f * facing.getStepX());
            vtar = arcaneresearch.common.lib.utils.Utils.rotateAroundX(vtar, 1.5707964f * facing.getStepY());
            Vec3 vres = vsource.add(vtar.x, vtar.y, vtar.z);
            x = net.minecraft.util.Mth.floor(vres.x);
            y = net.minecraft.util.Mth.floor(vres.y);
            z = net.minecraft.util.Mth.floor(vres.z);
        }
        digTargetPrev = new BlockPos(x, y, z);
    }
    private void rechargeVis() {
        float drained = AuraHandler.drainVis(level(), blockPosition(), 10.0f, false);
        charge += drained;
    }
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && isOwner(player) && isAlive()) {
            if (player.isShiftKeyDown()) {
                if (ModSounds.ZAP != null) {
                    playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                }
                if (!getMainHandItem().isEmpty()) {
                    spawnAtLocation(getMainHandItem(), 0.5f);
                }
                spawnAtLocation(new ItemStack(ModItems.TURRET_PLACER_BORE.get()), 0.5f);
                discard();
                return InteractionResult.SUCCESS;
            } else {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                            new net.minecraft.world.MenuProvider() {
                                @Override
                                public net.minecraft.network.chat.Component getDisplayName() {
                                    return EntityArcaneBore.this.getDisplayName();
                                }
                                @Override
                                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                                        int containerId,
                                        net.minecraft.world.entity.player.Inventory inventory,
                                        Player p) {
                                    return new arcaneresearch.common.menu.ArcaneBoreMenu(
                                            containerId, inventory, EntityArcaneBore.this);
                                }
                            }, buf -> buf.writeInt(getId()));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity living && isOwner(living)) {
            Direction dir = Direction.orderedByNearest(this)[0];
            if (dir != Direction.DOWN) {
                setFacing(dir);
            }
            return false;
        }
        return super.hurt(source, amount);
    }
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide) {
            if (!getMainHandItem().isEmpty()) {
                spawnAtLocation(getMainHandItem(), 0.5f);
            }
        }
    }
    @Override
    public void move(MoverType type, Vec3 motion) {
        super.move(type, new Vec3(motion.x / 5.0, motion.y, motion.z / 5.0));
    }
    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    public boolean isPickable() {
        return true;
    }
    public Direction getFacing() {
        return entityData.get(FACING);
    }
    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
    }
    public boolean isActive() {
        return entityData.get(ACTIVE);
    }
    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }
    public float getCharge() {
        return charge;
    }
    public void setCharge(float charge) {
        this.charge = charge;
    }
    public BlockPos getDigTarget() {
        return digTarget;
    }
    public boolean isClientDigging() {
        return clientDigging;
    }
    public boolean hasValidInventory() {
        return validInventory();
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            clientDigging = true;
        } else if (id == 17) {
            clientDigging = false;
        } else {
            super.handleEntityEvent(id);
        }
    }
    @Override
    public Team getTeam() {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }
    @Override
    protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.8125f;
    }
}
