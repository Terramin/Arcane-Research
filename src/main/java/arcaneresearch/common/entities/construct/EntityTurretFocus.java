package arcaneresearch.common.entities.construct;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.items.wands.ItemWand;
import arcaneresearch.common.items.wands.WandItems;
import arcaneresearch.common.lib.wands.WandAuraHelper;
import arcaneresearch.common.lib.wands.WandRegistry;
public class EntityTurretFocus extends EntityOwnedConstruct {
    private static final EntityDataAccessor<ItemStack> DATA_WAND = SynchedEntityData.defineId(EntityTurretFocus.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Byte> DATA_FACING = SynchedEntityData.defineId(EntityTurretFocus.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_TARGETS = SynchedEntityData.defineId(EntityTurretFocus.class, EntityDataSerializers.BYTE);
    protected AspectList vis = new AspectList();
    protected int maxVis = 100;
    protected int attackTimer = 10;
    boolean attackedLastTick;
    int attackCount;
    public EntityTurretFocus(EntityType<? extends EntityTurretFocus> type, Level level) {
        super(type, level);
        this.attackedLastTick = false;
        this.attackCount = 0;
        setTargetMob(true);
    }
    public EntityTurretFocus(EntityType<? extends EntityTurretFocus> type, Level level, BlockPos pos, Direction face) {
        this(type, level);
        this.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        setFacing(face);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WAND, ItemStack.EMPTY);
        this.entityData.define(DATA_FACING, (byte) Direction.UP.ordinal());
        this.entityData.define(DATA_TARGETS, (byte) 0);
    }
    @Override
    public void tick() {
        super.tick();
        doUpdateStuff();
        if (!this.level().isClientSide) {
            if (this.tickCount % 4 == 0) {
                rechargeVis();
            }
            if (this.tickCount % 40 == 0) {
                this.heal(1.0F);
            }
            if (this.attackTimer > 0) {
                this.attackTimer--;
            }
            boolean wasAttacking = this.attackedLastTick;
            this.attackedLastTick = false;
            if (this.getTarget() != null && this.attackTimer <= 0) {
                attackEntityWithFocus();
            }
            if (wasAttacking && !this.attackedLastTick) {
                this.attackCount = 0;
            }
        }
    }
    protected void doUpdateStuff() {
        if (this.getTarget() instanceof EntityTurretFocus && !getTargetFriendly()) {
            this.setTarget(null);
        }
    }
    protected void attackEntityWithFocus() {
    }
    protected boolean consumeVis(AspectList aspects, boolean doit) {
        if (this.level().isClientSide) {
            return true;
        }
        if (aspects == null || aspects.size() == 0) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (this.vis.getAmount(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        if (doit) {
            for (Aspect aspect : aspects.getAspects()) {
                this.vis.remove(aspect, aspects.getAmount(aspect));
            }
        }
        return true;
    }
    protected void rechargeVis() {
        if (this.level().isClientSide) {
            return;
        }
        Aspect lowest = null;
        int lowestAmt = Integer.MAX_VALUE;
        for (Aspect primal : Aspect.getPrimalAspects()) {
            int amt = this.vis.getAmount(primal);
            if (amt < this.maxVis && amt < lowestAmt) {
                lowestAmt = amt;
                lowest = primal;
            }
        }
        if (lowest == null) {
            return;
        }
        int room = Math.min(5, this.maxVis - lowestAmt);
        int drained = WandAuraHelper.drainAuraAvailable(this.level(), this.blockPosition(), lowest, room);
        if (drained > 0) {
            this.vis.add(lowest, drained);
        }
    }
    public Direction getFacing() {
        int ord = this.entityData.get(DATA_FACING) & 0xFF;
        Direction[] values = Direction.values();
        return ord < values.length ? values[ord] : Direction.UP;
    }
    public void setFacing(Direction face) {
        this.entityData.set(DATA_FACING, (byte) face.ordinal());
    }
    public ItemStack getWand() {
        return this.entityData.get(DATA_WAND);
    }
    public void setWand(ItemStack stack) {
        this.entityData.set(DATA_WAND, stack == null ? ItemStack.EMPTY : stack);
    }
    public void updateFocus() {
        try {
            WandRegistry.initWands();
            ItemStack wand = WandItems.createWandStack();
            if (!(wand.getItem() instanceof ItemWand ws)) {
                return;
            }
            if (WandRegistry.WAND_CAP_GOLD != null) {
                ws.setCap(wand, WandRegistry.WAND_CAP_GOLD);
            }
            if (WandRegistry.WAND_ROD_GREATWOOD != null) {
                ws.setRod(wand, WandRegistry.WAND_ROD_GREATWOOD);
            }
            ItemStack held = this.getMainHandItem();
            if (!held.isEmpty()) {
                ws.setFocus(wand, held);
                if (held.getItem() instanceof ItemFocusBasic focus
                        && this.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                    this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(focus.getTurretRange(held));
                }
            }
            setWand(wand);
        } catch (Exception e) {
        }
    }
    protected void dropFocus() {
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty()) {
            this.spawnAtLocation(held, 0.0F);
        }
    }
    private boolean getBit(int bit) {
        return (this.entityData.get(DATA_TARGETS) & (1 << bit)) != 0;
    }
    private void setBit(int bit, boolean value) {
        byte b = this.entityData.get(DATA_TARGETS);
        if (value) {
            b = (byte) (b | (1 << bit));
        } else {
            b = (byte) (b & ~(1 << bit));
        }
        this.entityData.set(DATA_TARGETS, b);
        this.setTarget(null);
    }
    public boolean canAttackClass(Class<?> clazz) {
        if (net.minecraft.world.entity.animal.Animal.class.isAssignableFrom(clazz)
                && !net.minecraft.world.entity.monster.Enemy.class.isAssignableFrom(clazz) && getTargetAnimal()) {
            return true;
        }
        if (net.minecraft.world.entity.monster.Enemy.class.isAssignableFrom(clazz) && getTargetMob()) {
            return true;
        }
        if (Player.class.isAssignableFrom(clazz) && getTargetPlayer()) {
            return true;
        }
        return false;
    }
    public boolean getTargetAnimal() {
        return getBit(0);
    }
    public void setTargetAnimal(boolean v) {
        setBit(0, v);
    }
    public boolean getTargetMob() {
        return getBit(1);
    }
    public void setTargetMob(boolean v) {
        setBit(1, v);
    }
    public boolean getTargetPlayer() {
        return getBit(2);
    }
    public void setTargetPlayer(boolean v) {
        setBit(2, v);
    }
    public boolean getTargetFriendly() {
        return getBit(3);
    }
    public void setTargetFriendly(boolean v) {
        setBit(3, v);
    }
    @Override
    public void push(double x, double y, double z) {
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.setYRot((float) (this.getYRot() + this.random.nextGaussian() * 45.0D));
        return super.hurt(source, amount);
    }
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        return super.mobInteract(player, hand);
    }
    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide) {
            dropFocus();
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.vis.writeToNBT(tag);
        tag.putByte("face", (byte) getFacing().ordinal());
        tag.putByte("targets", this.entityData.get(DATA_TARGETS));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.vis.readFromNBT(tag);
        Direction[] values = Direction.values();
        int ord = tag.getByte("face") & 0xFF;
        setFacing(ord < values.length ? values[ord] : Direction.UP);
        this.entityData.set(DATA_TARGETS, tag.getByte("targets"));
        updateFocus();
    }
}
