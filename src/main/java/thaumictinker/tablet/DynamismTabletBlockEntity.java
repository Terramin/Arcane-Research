package thaumictinker.tablet;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.eventbus.api.Event;

import arcaneresearch.Arcaneresearch;

 




 
public class DynamismTabletBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SWING_SPEED = 3;
    public static final int MAX_DEGREE = 45;

    private ItemStack stack = ItemStack.EMPTY;
    private boolean leftClick = true;
    private boolean redstone;
    private int swingProgress;
    private int swingMod;
    private double ticksExisted;
    private boolean breaking;
    private boolean progressShown;
    private int initialDamage;
    private int currentDamage;
    private int durabilityRemaining;
    private List<Entity> detectedEntities = List.of();
    private @Nullable TabletFakePlayer player;

    public DynamismTabletBlockEntity(BlockPos pos, BlockState state) {
        super(TabletFeature.TABLET_ENTITY, pos, state);
    }

    public boolean isLeftClick() {
        return leftClick;
    }

    public boolean isRedstone() {
        return redstone;
    }

    public void setModes(boolean leftClick, boolean redstone) {
        if (this.leftClick != leftClick || this.redstone != redstone) {
            this.leftClick = leftClick;
            this.redstone = redstone;
            sync();
        }
    }

    public int swingProgress() {
        return swingProgress;
    }

    public int swingMod() {
        return swingMod;
    }

    public double ticksExisted() {
        return ticksExisted;
    }

    public boolean isBreaking() {
        return breaking;
    }

    public ItemStack heldStack() {
        return stack;
    }

    public Direction facing() {
        return getBlockState().getValue(DynamismTabletBlock.FACING);
    }

    public BlockPos targetPos() {
        return worldPosition.relative(facing());
    }

    public void tick(Level tickLevel) {
        if (tickLevel instanceof ServerLevel level) {
            serverTick(level);
        } else {
            clientTick();
        }
    }

    private void clientTick() {
        ticksExisted++;
        advanceSwing(false, null);
    }

    private boolean advanceSwing(boolean hit, @Nullable ServerLevel level) {
        if (stack.isEmpty()) {
            swingMod = 0;
            swingProgress = 0;
            return false;
        }
        if (swingProgress >= MAX_DEGREE && hit && level != null) {
            swingHit(level);
        }
        swingMod = swingProgress <= 0 ? 0 : swingProgress >= MAX_DEGREE ? -SWING_SPEED : swingMod;
        swingProgress += swingMod;
        if (swingProgress < 0) {
            swingProgress = 0;
        }
        return true;
    }

    public void serverTick(ServerLevel level) {
        ticksExisted++;
        if (!advanceSwing(true, level) && breaking) {
            stopBreaking(level);
        }
        boolean detect = detect(level);
        if (!detect) {
            stopBreaking(level);
        }
        if (detect && breaking) {
            continueBreaking(level);
        }
        if ((!redstone || breaking) && detect && swingProgress == 0 && !stack.isEmpty()) {
            initiateSwing();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
        }
    }

    public void initiateSwing() {
        swingMod = SWING_SPEED;
        swingProgress = 1;
    }

    public void trigger(ServerLevel level) {
        findEntities(level, targetPos());
        initiateSwing();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
    }

    @Override
    public boolean triggerEvent(int type, int data) {
        if (type == 0) {
            if (level != null && level.isClientSide()) {
                initiateSwing();
            }
            return true;
        }
        return super.triggerEvent(type, data);
    }

    public boolean detect(ServerLevel level) {
        BlockPos target = targetPos();
        findEntities(level, target);
        return !level.getBlockState(target).isAir() || !detectedEntities.isEmpty();
    }

    public void findEntities(ServerLevel level, BlockPos target) {
        detectedEntities = level.getEntitiesOfClass(Entity.class, new AABB(target));
    }

    private TabletFakePlayer prepare(ServerLevel level) {
        if (player == null) {
            player = new TabletFakePlayer(level);
        }
        player.align(worldPosition, facing(), stack);
        return player;
    }

    private void commit(ServerLevel level, TabletFakePlayer fakePlayer) {
        ItemStack now = fakePlayer.release(level, worldPosition);
        ItemStack replacement = now.isEmpty() ? ItemStack.EMPTY : now;
        if (!ItemStack.isSameItemSameTags(replacement, stack)) {
            stack = replacement;
            sync();
        } else {
            setChanged();
        }
    }

    public void swingHit(ServerLevel level) {
        BlockPos target = targetPos();
        BlockState state = level.getBlockState(target);
        TabletFakePlayer fakePlayer = prepare(level);
        boolean done = false;
        try {
            if (leftClick) {
                Entity entity = randomEntity(level);
                if (entity != null) {
                    attack(fakePlayer, entity);
                    done = true;
                } else if (!breaking && !state.isAir() && state.getDestroySpeed(level, target) >= 0.0F) {
                    breaking = true;
                    startBreaking(level, fakePlayer, state);
                    done = true;
                }
            } else {
                done = rightClick(level, fakePlayer, target, state);
            }
        } finally {
            if (done) {
                commit(level, fakePlayer);
            } else {
                fakePlayer.release(level, worldPosition);
            }
            setChanged();
        }
    }

    @Nullable
    private Entity randomEntity(ServerLevel level) {
        return detectedEntities.isEmpty() ? null : detectedEntities.get(level.getRandom().nextInt(detectedEntities.size()));
    }

    private void attack(TabletFakePlayer fakePlayer, Entity entity) {
        Multimap<Attribute, AttributeModifier> modifiers =
                fakePlayer.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND);
        fakePlayer.getAttributes().addTransientAttributeModifiers(modifiers);
        try {
            fakePlayer.attack(entity);
        } finally {
            fakePlayer.getAttributes().removeAttributeModifiers(modifiers);
        }
    }

    private boolean rightClick(ServerLevel level, TabletFakePlayer fakePlayer, BlockPos target, BlockState state) {
        Direction side = facing().getOpposite();
        BlockPos clicked = target;
        if (state.isAir()) {
            clicked = target.below();
            side = Direction.UP;
        }
        try {
            Entity entity = randomEntity(level);
            ItemStack held = fakePlayer.getMainHandItem();
            boolean done = entity instanceof Mob mob
                    && (held.interactLivingEntity(fakePlayer, mob, InteractionHand.MAIN_HAND).consumesAction()
                    || !(mob instanceof Animal)
                    || mob.interact(fakePlayer, InteractionHand.MAIN_HAND).consumesAction());
            if (!done) {
                BlockHitResult hit = new BlockHitResult(Vec3.atLowerCornerOf(clicked), side, clicked, false);
                InteractionResult result = fakePlayer.gameMode.useItemOn(fakePlayer, level, held,
                        InteractionHand.MAIN_HAND, hit);
                if (!result.consumesAction()) {
                    fakePlayer.gameMode.useItem(fakePlayer, level, fakePlayer.getMainHandItem(), InteractionHand.MAIN_HAND);
                }
            }
            return true;
        } catch (Exception exception) {
            reportError(level, exception);
            return true;
        }
    }

    private void reportError(ServerLevel level, Exception exception) {
        Arcaneresearch.LOGGER.error("Tool Dynamism Tablet at {} failed", worldPosition, exception);
        AABB area = new AABB(worldPosition.getX() - 8, worldPosition.getY() - 8, worldPosition.getZ() - 8,
                worldPosition.getX() + 8, worldPosition.getY() + 8, worldPosition.getZ() + 8);
        for (Player nearby : level.getEntitiesOfClass(Player.class, area)) {
            nearby.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.animationTablet.error")
                    .withStyle(ChatFormatting.RED));
            nearby.sendSystemMessage(Component.literal(String.valueOf(exception.getMessage()))
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
    }

    private int breakerId() {
        return -1 - (worldPosition.hashCode() & Integer.MAX_VALUE);
    }

    private void showProgress(ServerLevel level, int progress) {
        level.destroyBlockProgress(breakerId(), targetPos(), progress);
        progressShown = progress >= 0;
    }

    public void stopBreaking(ServerLevel level) {
        boolean wasBreaking = breaking;
        breaking = false;
        if (wasBreaking || progressShown) {
            showProgress(level, -1);
        }
    }

    private void startBreaking(ServerLevel level, TabletFakePlayer fakePlayer, BlockState state) {
        BlockPos target = targetPos();
        Direction side = facing().getOpposite();
        PlayerInteractEventCompat event = PlayerInteractEventCompat.fire(fakePlayer, target, side);
        if (event.canceled) {
            stopBreaking(level);
            return;
        }
        initialDamage = currentDamage;
        if (event.useBlock) {
            state.attack(level, target, fakePlayer);
        }
        float progress = state.getDestroyProgress(fakePlayer, level, target);
        if (!event.useItem) {
            stopBreaking(level);
            return;
        }
        if (progress >= 1.0F) {
            tryHarvest(level, fakePlayer, target);
            stopBreaking(level);
        } else {
            int stage = (int) (progress * 10.0F);
            showProgress(level, stage);
            durabilityRemaining = stage;
        }
    }

    private void continueBreaking(ServerLevel level) {
        currentDamage++;
        int elapsed = currentDamage - initialDamage;
        BlockPos target = targetPos();
        BlockState state = level.getBlockState(target);
        if (state.isAir()) {
            stopBreaking(level);
            return;
        }
        TabletFakePlayer fakePlayer = prepare(level);
        try {
            float progress = state.getDestroyProgress(fakePlayer, level, target) * elapsed;
            int stage = (int) (progress * 10.0F);
            if (stage != durabilityRemaining) {
                showProgress(level, stage);
                durabilityRemaining = stage;
            }
            if (progress >= 1.0F) {
                tryHarvest(level, fakePlayer, target);
                stopBreaking(level);
            }
        } finally {
            commit(level, fakePlayer);
        }
    }

    public boolean tryHarvest(ServerLevel level, TabletFakePlayer fakePlayer, BlockPos target) {
        return fakePlayer.gameMode.destroyBlock(target);
    }

      
    private static final class PlayerInteractEventCompat {
        final boolean canceled;
        final boolean useBlock;
        final boolean useItem;

        private PlayerInteractEventCompat(boolean canceled, boolean useBlock, boolean useItem) {
            this.canceled = canceled;
            this.useBlock = useBlock;
            this.useItem = useItem;
        }

        static PlayerInteractEventCompat fire(Player player, BlockPos pos, Direction face) {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event =
                    ForgeHooks.onLeftClickBlock(player, pos, face, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
            boolean useBlock = !event.isCanceled() && event.getUseBlock() != Event.Result.DENY;
            boolean useItem = !event.isCanceled() && event.getUseItem() != Event.Result.DENY;
            return new PlayerInteractEventCompat(event.isCanceled(), useBlock, useItem);
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.split(amount);
        sync();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack;
        stack = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack newStack) {
        if (slot == 0) {
            stack = newStack;
            newStack.setCount(Math.min(newStack.getCount(), getMaxStackSize()));
            sync();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        
        
        return slot == 0 && !stack.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        stack = ItemStack.EMPTY;
    }

      
    public void dropContents() {
        if (level instanceof ServerLevel serverLevel) {
            stopBreaking(serverLevel);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                stack = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        player = null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumictinker.dynamism_tablet");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DynamismTabletMenu(containerId, inventory, this);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        leftClick = tag.getBoolean("leftClick");
        redstone = tag.getBoolean("redstone");
        CompoundTag items = tag.getCompound("Items");
        if (items.contains("0")) {
            stack = ItemStack.of(items.getCompound("0"));
        } else {
            stack = ItemStack.EMPTY;
        }
        swingProgress = tag.getInt("progress");
        swingMod = tag.getInt("mod");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("leftClick", leftClick);
        tag.putBoolean("redstone", redstone);
        CompoundTag items = new CompoundTag();
        if (!stack.isEmpty()) {
            items.put("0", stack.save(new CompoundTag()));
        }
        tag.put("Items", items);
        tag.putInt("progress", swingProgress);
        tag.putInt("mod", swingMod);
    }
}
