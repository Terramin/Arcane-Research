package arcaneresearch.common.items.casters;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.api.casters.IFocusBlockPicker;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.FocusItem;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.client.renderers.item.CasterItemRenderer;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketAuraToClient;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.wands.WandAuraHelper;
import arcaneresearch.common.lib.wands.WandRegistry;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.common.items.wands.FocusManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemCaster extends Item implements IFocusHost, IArchitect {

    @Override
    public boolean isCaster() {
        return true;
    }

    private final DecimalFormat formatter = new DecimalFormat("#######.#");
    private final int auraArea;

    public ItemCaster(int area) {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
        this.auraArea = area;
    }

    public static ItemCaster createBasic() {
        return new ItemCaster(0);
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        WandRod rod = getRod(stack);
        if (rod == null) {
            return 0;
        }
        return (int) (rod.getCapacity() * (isSceptre(stack) ? 1.5F : 1.0F));
    }

    private int getMaxVisInternal(ItemStack stack) {
        return getMaxVis(stack) * 100;
    }

    @Override
    public int getVis(ItemStack stack, Aspect aspect) {
        return getVisInternal(stack, aspect) / 100;
    }

    private int getVisInternal(ItemStack stack, Aspect aspect) {
        if (stack.isEmpty() || aspect == null) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(aspect.getTag()) : 0;
    }

    @Override
    public AspectList getAllVis(ItemStack stack) {
        AspectList out = new AspectList();
        if (stack == null || stack.isEmpty()) {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                out.add(aspect, 0);
            }
            return out;
        }
        CompoundTag tag = stack.getTag();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            String key = aspect.getTag();
            out.add(aspect, tag != null && tag.contains(key) ? tag.getInt(key) / 100 : 0);
        }
        return out;
    }

    private AspectList getAllVisInternal(ItemStack stack) {
        AspectList out = new AspectList();
        CompoundTag tag = stack.getTag();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            String key = aspect.getTag();
            out.add(aspect, tag != null && tag.contains(key) ? tag.getInt(key) : 0);
        }
        return out;
    }

    @Override
    public AspectList getAspectsWithRoom(ItemStack stack) {
        AspectList out = new AspectList();
        AspectList cur = getAllVisInternal(stack);
        int maxVis = getMaxVisInternal(stack);
        for (Aspect aspect : cur.getAspects()) {
            int current = cur.getAmount(aspect);
            if (current < maxVis) {
                out.add(aspect, Math.max(1, (maxVis - current) / 100));
            }
        }
        return out;
    }

    private void storeVisInternal(ItemStack stack, Aspect aspect, int amount) {
        stack.getOrCreateTag().putInt(aspect.getTag(), amount);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, Aspect aspect, int amount, boolean simulate) {
        amount *= 100;
        amount = (int) (amount * getConsumptionModifier(stack, player, aspect, false));
        int current = getVisInternal(stack, aspect);
        if (current >= amount) {
            if (!simulate && !player.level().isClientSide) {
                storeVisInternal(stack, aspect, current - amount);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeAllVis(ItemStack stack, Player player, AspectList cost,
                                 boolean simulate, boolean crafting) {
        if (cost == null || cost.size() == 0) {
            return false;
        }
        AspectList needed = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            int amount = cost.getAmount(aspect) * 100;
            amount = (int) (amount * getConsumptionModifier(stack, player, aspect, crafting));
            if (amount <= 0) {
                continue;
            }
            if (getVisInternal(stack, aspect) < amount) {
                return false;
            }
            needed.add(aspect, amount);
        }
        if (!simulate && (player == null || !player.level().isClientSide)) {
            for (Aspect aspect : needed.getAspects()) {
                storeVisInternal(stack, aspect,
                        getVisInternal(stack, aspect) - needed.getAmount(aspect));
            }
        }
        return true;
    }

    @Override
    public int addVis(ItemStack stack, Aspect aspect, int amount, boolean simulate) {
        if (aspect == null || !aspect.isPrimal()) {
            return 0;
        }
        int stored = getVisInternal(stack, aspect) + amount * 100;
        int leftover = Math.max(stored - getMaxVisInternal(stack), 0);
        if (!simulate) {
            storeVisInternal(stack, aspect, Math.min(stored, getMaxVisInternal(stack)));
        }
        return leftover / 100;
    }

    @Override
    public float getConsumptionModifier(ItemStack stack, Player player,
                                        Aspect aspect, boolean crafting) {
        float modifier = 1.0F;
        WandCap cap = getCap(stack);
        if (cap != null) {
            List<Aspect> special = cap.getSpecialCostModifierAspects();
            if (special != null && special.contains(aspect)) {
                modifier = cap.getSpecialCostModifier();
            } else {
                modifier = cap.getBaseCostModifier();
            }
        }
        if (player != null) {
            modifier -= arcaneresearch.common.items.wands.FocusManager.getTotalVisDiscount(player, aspect);
        }
        if (getFocus(stack) != null && !crafting) {
            modifier -= getFocusFrugal(stack) / 10.0F;
            if (isStaff(stack)) {
                modifier -= 0.1F;
            }
        }
        if (isSceptre(stack)) {
            modifier -= 0.1F;
        }
        int efficiency = EnumInfusionEnchantment.getInfusionEnchantmentLevel(
                stack, EnumInfusionEnchantment.MAGIC_EFFICIENCY);
        modifier -= efficiency / 10.0F;
        return Math.max(0.1F, modifier);
    }


    @Override
    public FocusItem getFocus(ItemStack stack) {
        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && focusStack.getItem() instanceof FocusItem basic) {
            return basic;
        }
        return null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("focus")) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound("focus"));
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (focus == null || focus.isEmpty()) {
            stack.getOrCreateTag().remove("focus");
        } else {
            stack.getOrCreateTag().put("focus", focus.save(new CompoundTag()));
        }
    }


    @Override
    public WandRod getRod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("rod")) {
            WandRod rod = WandRod.getRod(tag.getString("rod"));
            if (rod != null) {
                return rod;
            }
        }
        return WandRegistry.WAND_ROD_WOOD;
    }

    @Override
    public void setRod(ItemStack stack, WandRod rod) {
        if (!stack.isEmpty() && rod != null) {
            stack.getOrCreateTag().putString("rod", rod.getTag());
        }
    }

    @Override
    public WandCap getCap(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("cap")) {
            WandCap cap = WandCap.caps.get(tag.getString("cap"));
            if (cap != null) {
                return cap;
            }
        }
        return WandRegistry.WAND_CAP_IRON;
    }

    @Override
    public void setCap(ItemStack stack, WandCap cap) {
        if (!stack.isEmpty() && cap != null) {
            stack.getOrCreateTag().putString("cap", cap.getTag());
        }
    }

    @Override
    public boolean isStaff(ItemStack stack) {
        WandRod rod = getRod(stack);
        return rod != null && rod.isStaff();
    }

    @Override
    public boolean isSceptre(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("sceptre");
    }

    @Override
    public int getFocusPotency(ItemStack stack) {
        if (getFocus(stack) == null) {
            return 0;
        }
        return getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.potency)
                + (getRod(stack) != null && getRod(stack).hasPotencyBonus() ? 1 : 0);
    }

    @Override
    public int getFocusTreasure(ItemStack stack) {
        return getFocus(stack) == null ? 0
                : getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.treasure);
    }

    @Override
    public int getFocusFrugal(ItemStack stack) {
        return getFocus(stack) == null ? 0
                : getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.frugal);
    }

    @Override
    public int getFocusEnlarge(ItemStack stack) {
        return getFocus(stack) == null ? 0
                : getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.enlarge);
    }

    @Override
    public int getFocusExtend(ItemStack stack) {
        return getFocus(stack) == null ? 0
                : getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.extend);
    }


    public Aspect handleRecharge(Level level, ItemStack stack, BlockPos pos,
                                 Player player, int amount) {
        WandCap cap = getCap(stack);
        if (cap != null) {
            amount += cap.getChargeBonus();
            if (cap.getRechargeMultiplier() != 1.0F) {
                amount = Math.max(1, Math.round(amount * cap.getRechargeMultiplier()));
            }
        }
        AspectList room = getAspectsWithRoom(stack);
        if (room == null) {
            return null;
        }
        for (Aspect aspect : room.getAspectsSortedByAmount()) {
            if (aspect == null) {
                continue;
            }
            if (player != null && WandAuraHelper.shouldPreserveAura(level, player, pos, aspect)) {
                continue;
            }
            int take = Math.min(amount, room.getAmount(aspect));
            int drained = WandAuraHelper.drainAuraAvailable(level, pos, aspect, take);
            if (drained > 0) {
                addVis(stack, aspect, drained, false);
                amount -= drained;
                if (amount <= 0) {
                    return aspect;
                }
            }
        }
        return null;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity,
                              int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        boolean held = isSelected || (entity instanceof Player player && player.getOffhandItem() == stack);

        if (!level.isClientSide && held && entity.tickCount % 5 == 0) {
            if (entity instanceof Player player) {
                WandRod rod = getRod(stack);
                if (rod != null && rod.getOnUpdate() != null) {
                    rod.getOnUpdate().onUpdate(stack, player);
                }
                int rate = arcaneresearch.common.items.wands.FocusManager.getBaseChargeRate(player, isSelected, slotId);
                if (rate > 0) {
                    handleRecharge(level, stack, player.blockPosition(), player, rate);
                }
            }
        }

        if (level.isClientSide() || !held || entity.tickCount % 10 != 0
                || !(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        float vis = 0.0f;
        float flux = 0.0f;
        short base = 0;
        BlockPos origin = serverPlayer.blockPosition();
        var dimension = level.dimension();

        switch (auraArea) {
            case 1 -> {
                var center = AuraHandler.getAuraChunk(dimension, origin.getX() >> 4, origin.getZ() >> 4);
                if (center == null) {
                    break;
                }
                vis = center.getVis();
                flux = center.getFlux();
                base = center.getBase();
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    var neighbour = AuraHandler.getAuraChunk(dimension,
                            (origin.getX() >> 4) + face.getStepX(),
                            (origin.getZ() >> 4) + face.getStepZ());
                    if (neighbour != null) {
                        vis += neighbour.getVis();
                        flux += neighbour.getFlux();
                        base += neighbour.getBase();
                    }
                }
            }
            case 2 -> {
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        var chunk = AuraHandler.getAuraChunk(dimension,
                                (origin.getX() >> 4) + xx, (origin.getZ() >> 4) + zz);
                        if (chunk != null) {
                            vis += chunk.getVis();
                            flux += chunk.getFlux();
                            base += chunk.getBase();
                        }
                    }
                }
            }
            default -> {
                var chunk = AuraHandler.getAuraChunk(dimension, origin.getX() >> 4, origin.getZ() >> 4);
                if (chunk != null) {
                    vis = chunk.getVis();
                    flux = chunk.getFlux();
                    base = chunk.getBase();
                }
            }
        }
        PacketHandler.sendToPlayer(new PacketAuraToClient(base, vis, flux), serverPlayer);
    }


    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("picked")) {
            return ItemStack.EMPTY;
        }
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return ItemStack.EMPTY;
        }
        for (var fe : fp.nodes) {
            if (fe instanceof IFocusBlockPicker) {
                return ItemStack.of(tag.getCompound("picked"));
            }
        }
        return ItemStack.EMPTY;
    }

    public void storePickedBlock(ItemStack stack, ItemStack pickedBlock) {
        if (stack == null || pickedBlock == null) {
            return;
        }
        stack.getOrCreateTag().put("picked", pickedBlock.save(new CompoundTag()));
    }

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity player) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack.isEmpty()) {
            return null;
        }
        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (!arcaneresearch.api.casters.FocusEngine.doesPackageContainElement(fp, "arcaneresearch.PLAN")) {
            return null;
        }
        var plan = arcaneresearch.api.casters.FocusEngine.getElement("arcaneresearch.PLAN");
        if (plan instanceof IArchitect architect) {
            return architect.getArchitectMOP(focusStack, level, player);
        }
        return null;
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level,
                                                   BlockPos pos, Direction side, Player player) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack.isEmpty()) {
            return null;
        }
        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return null;
        }
        for (var fe : fp.nodes) {
            if (fe instanceof IArchitect architect) {
                return architect.getArchitectBlocks(stack, level, pos, side, player);
            }
        }
        return null;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack.isEmpty()) {
            return false;
        }
        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return false;
        }
        for (var fe : fp.nodes) {
            if (fe instanceof IArchitect architect) {
                return architect.showAxis(stack, level, player, side, axis);
            }
        }
        return false;
    }


    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof arcaneresearch.api.casters.IInteractWithCaster casterBlock) {
            if (casterBlock.onCasterRightClick(level, stack, player, pos, side, hand)) {
                return InteractionResult.SUCCESS;
            }
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof arcaneresearch.api.casters.IInteractWithCaster casterTile) {
            if (casterTile.onCasterRightClick(level, stack, player, pos, side, hand)) {
                return InteractionResult.SUCCESS;
            }
        }
        if (arcaneresearch.api.casters.CasterTriggerRegistry.hasTrigger(state)) {
            return arcaneresearch.api.casters.CasterTriggerRegistry.performTrigger(
                    level, stack, player, pos, side, state)
                    ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        ItemStack fb = getFocusStack(stack);
        if (!fb.isEmpty() && player != null && tile == null) {
            FocusPackage core = ItemFocus.getPackage(fb);
            if (core != null && core.nodes != null) {
                for (var fe : core.nodes) {
                    if (!(fe instanceof IFocusBlockPicker) || !player.isCrouching()) {
                        continue;
                    }
                    if (!level.isClientSide) {
                        ItemStack picked = new ItemStack(state.getBlock());
                        try {
                            if (!state.is(Blocks.AIR)) {
                                ItemStack silk = BlockUtils.getSilkTouchDrop(state, level, pos);
                                if (silk != null && !silk.isEmpty()) {
                                    picked = silk.copy();
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        storePickedBlock(stack, picked);
                        return InteractionResult.SUCCESS;
                    }
                    player.swing(hand);
                    return InteractionResult.PASS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(stack);
        FocusItem focus = getFocus(stack);

        if (focus == null || focusStack.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        if (FocusManager.isOnCooldown(player)) {
            return InteractionResultHolder.pass(stack);
        }

        if (focus.isVisCostPerTick(focusStack)) {
            player.startUsingItem(hand);
            FocusManager.setCooldown(player, -1);
            return InteractionResultHolder.consume(stack);
        }

        AspectList cost = focus.getVisCost(focusStack);
        if (!consumeAllVis(stack, player, cost, true, false)) {
            return InteractionResultHolder.fail(stack);
        }

        FocusManager.setCooldown(player, focus.getActivationCooldown(focusStack));

        if (player.isCrouching()) {
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core != null && core.nodes != null) {
                for (var fe : core.nodes) {
                    if (fe instanceof IFocusBlockPicker) {
                        return InteractionResultHolder.pass(stack);
                    }
                }
            }
        }

        HitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        boolean activated = focus.onFocusActivation(stack, level, player, hit, 0);
        if (level.isClientSide()) {
            return activated ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
        }
        if (!activated) {
            return InteractionResultHolder.fail(stack);
        }
        consumeAllVis(stack, player, cost, false, false);

        player.swing(hand);
        player.getCooldowns().addCooldown(this, focus.getActivationCooldown(focusStack));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity,
                          @NotNull ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack focusStack = getFocusStack(stack);
        FocusItem focus = getFocus(stack);
        if (focus == null || FocusManager.isOnCooldown(player)) {
            return;
        }
        AspectList cost = focus.getVisCost(focusStack);
        if (!consumeAllVis(stack, player, cost, true, false)) {
            player.stopUsingItem();
            return;
        }
        HitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        FocusManager.setCooldown(player, focus.getActivationCooldown(focusStack));
        if (focus.onFocusActivation(stack, level, player, hit, remainingTicks)) {
            consumeAllVis(stack, player, cost, level.isClientSide, false);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level,
                             @NotNull LivingEntity entity, int timeLeft) {
        FocusManager.setCooldown(entity, 0);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        ItemStack focusStack = getFocusStack(stack);
        FocusItem focus = getFocus(stack);
        if (focus != null && !focusStack.isEmpty() && !FocusManager.isOnCooldown(entity)) {
            FocusManager.setCooldown(entity, focus.getActivationCooldown(focusStack));
            return focusStack.getItem().onEntitySwing(stack, entity);
        }
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        FocusItem focus = getFocus(stack);
        if (focus != null && !FocusManager.isOnCooldown(player)) {
            FocusManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
            return focus.onFocusBlockStartBreak(stack, pos, player);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        FocusItem focus = getFocus(stack);
        if (focus != null) {
            ItemStack focusStack = getFocusStack(stack);
            if (!focusStack.isEmpty()) {
                return focusStack.getItem().getDestroySpeed(focusStack, state);
            }
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        FocusItem focus = getFocus(stack);
        if (focus != null) {
            return getFocusStack(stack).isCorrectToolForDrops(state);
        }
        return false;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack == null || newStack == null) {
            return true;
        }
        if (oldStack.getItem() == this && newStack.getItem() == this) {
            ItemStack oldFocus = getFocusStack(oldStack);
            ItemStack newFocus = getFocusStack(newStack);
            if (oldFocus.isEmpty() && newFocus.isEmpty()) {
                return false;
            }
            if (oldFocus.isEmpty() || newFocus.isEmpty()) {
                return true;
            }
            String oldSort = oldFocus.getItem() instanceof FocusItem f ? f.getSortingHelper(oldFocus) : null;
            String newSort = newFocus.getItem() instanceof FocusItem f ? f.getSortingHelper(newFocus) : null;
            if (oldSort != null && newSort != null) {
                return !oldSort.equals(newSort);
            }
            return !ItemStack.isSameItemSameTags(oldFocus, newFocus);
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ItemStack focusStack = getFocusStack(stack);
        FocusItem focus = getFocus(stack);
        AspectList focusCost = focus != null ? focus.getVisCost(focusStack) : null;

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            StringBuilder line = new StringBuilder();
            int count = 0;
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (!tag.contains(aspect.getTag())) {
                    continue;
                }
                int amount = getVis(stack, aspect);
                float mod = getConsumptionModifier(stack, null, aspect, false);
                String extra = "";
                if (focus != null && focusCost != null) {
                    float cost = focusCost.getAmount(aspect) * mod;
                    if (cost > 0.0F) {
                        extra = ", " + formatter.format(cost) + " "
                                + Component.translatable(focus.isVisCostPerTick(focusStack)
                                ? "item.Focus.cost2" : "item.Focus.cost1").getString();
                    }
                }
                if (flag.isAdvanced()) {
                    tooltip.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName()
                            + " x " + amount + ", (" + formatter.format(mod * 100.0F) + "% "
                            + Component.translatable("tc.vis.cost").getString() + ")" + extra));
                } else {
                    line.append(line.isEmpty() ? "" : " | ");
                    line.append("§").append(aspect.getChatcolor()).append(amount).append("§r");
                }
                count++;
            }
            if (!flag.isAdvanced() && count > 0) {
                tooltip.add(Component.literal(line.toString()));
            }
        }

        WandCap cap = getCap(stack);
        if (cap != null) {
            tooltip.add(Component.literal(ChatFormatting.GOLD
                    + Component.translatable("item.capacity.text").getString()
                    + " " + getMaxVis(stack) + "§r"));
        }

        if (focus != null) {
            tooltip.add(Component.empty());
            tooltip.add(focusStack.getHoverName().copy()
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));
            focus.addFocusInformation(focusStack, level, tooltip, flag);
        } else {
            tooltip.add(Component.translatable("item.arcaneresearch.caster.no_focus")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final CasterItemRenderer renderer = new CasterItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}
