package arcaneresearch.common.items.wands;

import net.minecraft.nbt.NbtOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.*;
import arcaneresearch.common.lib.wands.WandAuraHelper;
import arcaneresearch.common.lib.wands.WandRegistry;
import arcaneresearch.proxies.Proxies;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemWand extends Item implements IWand, IArchitect, IArchitectExtended {
    private final DecimalFormat formatter = new DecimalFormat("#######.#");

    public ItemWand(Properties props) {
        // NEOFORGE_PORT: getRarity(stack) override removed in 26.x -> rarity in Properties.
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    // NEOFORGE_PORT: тот же хук, что в Forge (WandUseAnimation.HAND_EXTENSION);
    // без @Override (см. BlockHungryChestItem: initializeClient убран из Item в 26.x).

    // NEOFORGE_PORT: ItemStack.save(CompoundTag)/ItemStack.of(CompoundTag) removed in 26.x ->
    // ItemStack.CODEC via NbtOps (same "focus" key; inner shape is codec form).
    private static ItemStack decodeStack(CompoundTag parent, String key) {
        if (parent == null || !parent.contains(key)) {
            return ItemStack.EMPTY;
        }
        Tag inner = parent.get(key);
        if (inner == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.CODEC.parse(NbtOps.INSTANCE, inner).result().orElse(ItemStack.EMPTY);
    }

    private static void encodeStack(CompoundTag parent, String key, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            parent.remove(key);
            return;
        }
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack.copy()).result().ifPresent(encoded -> {
            parent.put(key, encoded);
        });
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxVis(ItemStack stack) {
        WandRod rod = getRod(stack);
        if (rod == null) {
            return 0;
        }
        return (int) (rod.getCapacity() * (isSceptre(stack) ? 1.5D : 1.0D));
    }

    private int getMaxVisInternal(ItemStack stack) {
        return getMaxVis(stack) * 100;
    }

    @Override
    public int getVis(ItemStack stack, Aspect aspect) {
        return getVisInternal(stack, aspect) / 100;
    }

    private int getVisInternal(ItemStack stack, Aspect aspect) {
        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same per-aspect int keys).
        if (stack.isEmpty() || aspect == null) {
            return 0;
        }
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root == null || !root.contains(aspect.getTag())) {
            return 0;
        }
        return root.getIntOr(aspect.getTag(), 0);
    }

    @Override
    public AspectList getAllVis(ItemStack stack) {
        AspectList out = new AspectList();
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (root != null && root.contains(aspect.getTag())) {
                out.merge(aspect, root.getIntOr(aspect.getTag(), 0) / 100);
            } else {
                out.merge(aspect, 0);
            }
        }
        return out;
    }

    private AspectList getAllVisInternal(ItemStack stack) {
        AspectList out = new AspectList();
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (root != null && root.contains(aspect.getTag())) {
                out.merge(aspect, root.getIntOr(aspect.getTag(), 0));
            } else {
                out.merge(aspect, 0);
            }
        }
        return out;
    }

    @Override
    public AspectList getAspectsWithRoom(ItemStack stack) {
        AspectList out = new AspectList();
        AspectList cur = getAllVisInternal(stack);
        for (Aspect aspect : cur.getAspects()) {
            if (cur.getAmount(aspect) < getMaxVisInternal(stack)) {
                int room = Math.max(1, (getMaxVisInternal(stack) - cur.getAmount(aspect)) / 100);
                out.add(aspect, room);
            }
        }
        return out;
    }

    private void storeVisInternal(ItemStack stack, Aspect aspect, int amount) {
        // NEOFORGE_PORT: COPY + write-back via ArcaneresearchInvHelper (getCustomTag returns a copy).
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.putInt(aspect.getTag(), amount);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, Aspect aspect, int amount, boolean crafting) {
        amount *= 100;
        amount = (int) (amount * getConsumptionModifier(stack, player, aspect, crafting));
        if (getVisInternal(stack, aspect) >= amount) {
            storeVisInternal(stack, aspect, getVisInternal(stack, aspect) - amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeAllVis(ItemStack stack, Player player, AspectList cost, boolean simulate, boolean crafting) {
        if (cost == null || cost.size() == 0) {
            return false;
        }
        AspectList needed = new AspectList();
        for (Aspect aspect : cost.getAspects()) {
            int amount = cost.getAmount(aspect) * 100;
            amount = (int) (amount * getConsumptionModifier(stack, player, aspect, crafting));
            if (getVisInternal(stack, aspect) < amount) {
                return false;
            }
            needed.add(aspect, amount);
        }
        if (!simulate && (player == null || !player.level().isClientSide())) {
            for (Aspect aspect : needed.getAspects()) {
                storeVisInternal(stack, aspect, getVisInternal(stack, aspect) - needed.getAmount(aspect));
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
        return (int) Math.floor(leftover / 100.0D);
    }

    @Override
    public float getConsumptionModifier(ItemStack stack, Player player, Aspect aspect, boolean crafting) {
        float modifier = 1.0F;
        WandCap cap = getCap(stack);
        if (cap != null) {
            if (cap.getSpecialCostModifierAspects() != null
                    && cap.getSpecialCostModifierAspects().contains(aspect)) {
                modifier = cap.getSpecialCostModifier();
            } else {
                modifier = cap.getBaseCostModifier();
            }
        }
        if (player != null) {
            modifier -= WandManager.getTotalVisDiscount(player, aspect);
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
        int efficiency = arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment
                .getInfusionEnchantmentLevel(stack,
                        arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment.MAGIC_EFFICIENCY);
        if (efficiency > 0) {
            modifier -= efficiency / 10.0F;
        }
        return Math.max(modifier, 0.1F);
    }

    public AspectList getAspectsInChargable(ItemStack stack) {
        return getAllVis(stack);
    }

    public Aspect handleRecharge(Level level, ItemStack stack, BlockPos pos, Player player, int amount) {
        WandCap cap = getCap(stack);
        if (cap != null) {
            amount += cap.getChargeBonus();
            if (cap.getRechargeMultiplier() != 1.0F) {
                amount = Math.max(1, Math.round(amount * cap.getRechargeMultiplier()));
            }
        }
        AspectList room = getAspectsWithRoom(stack);
        for (Aspect aspect : room.getAspectsSortedByAmount()) {
            if (aspect != null && (player == null || !WandAuraHelper.shouldPreserveAura(level, player, pos, aspect))) {
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
        }
        return null;
    }

    @Override
    public ItemFocusBasic getFocus(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("focus")) {
            ItemStack focusStack = decodeStack(root, "focus");
            if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocusBasic basic) {
                return basic;
            }
        }
        return null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("focus")) {
            return decodeStack(root, "focus");
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        if (focus == null || focus.isEmpty()) {
            root.remove("focus");
        } else {
            encodeStack(root, "focus", focus);
        }
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @Override
    public WandRod getRod(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("rod")) {
            WandRod rod = WandRod.getRod(root.getStringOr("rod", ""));
            if (rod != null) {
                return rod;
            }
        }
        return WandRegistry.WAND_ROD_WOOD;
    }

    @Override
    public boolean isStaff(ItemStack stack) {
        WandRod rod = getRod(stack);
        return rod != null && rod.isStaff();
    }

    @Override
    public boolean isSceptre(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        return root != null && root.contains("sceptre");
    }

    /** Паритет TC4 (NBT-флаг "sceptre"): превратить жезл в скипетр. */
    public static void setSceptre(ItemStack stack) {
        if (!stack.isEmpty()) {
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            root.putBoolean("sceptre", true);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    @Override
    public void setRod(ItemStack stack, WandRod rod) {
        if (!stack.isEmpty() && rod != null) {
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            root.putString("rod", rod.getTag());
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    @Override
    public WandCap getCap(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("cap")) {
            WandCap cap = WandCap.caps.get(root.getStringOr("cap", ""));
            if (cap != null) {
                return cap;
            }
        }
        // NEOFORGE_PORT: registry may not be inited yet when data-driven
        // callers (recipes, archaeology) query a fresh wand; init is idempotent.
        WandItems.initWands();
        return WandRegistry.WAND_CAP_IRON;
    }

    @Override
    public void setCap(ItemStack stack, WandCap cap) {
        if (!stack.isEmpty() && cap != null) {
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            root.putString("cap", cap.getTag());
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof Player player) {
            WandRod rod = getRod(stack);
            if (rod != null && rod.getOnUpdate() != null) {
                rod.getOnUpdate().onUpdate(stack, player);
            }
            if (player.tickCount % 5 == 0) {
                // NEOFORGE_PORT: 26.x passes EquipmentSlot (no slot index / selected flag).
                // MAINHAND ticks only for the held (= selected) stack, so outcomes match
                // Forge for main hand (incl. hotbar research bonus) and offhand exactly.
                // NOTE: unselected-hotbar NODETAPPER bonus (slot<9) has no 26.x tick source
                // (slot index is not provided) and no longer applies.
                boolean selected = slot == EquipmentSlot.MAINHAND;
                int slotId = selected ? player.getInventory().getSelectedSlot()
                        : net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND;
                int rate = WandManager.getBaseChargeRate(player, selected, slotId);
                if (rate > 0) {
                    handleRecharge(level, stack, player.blockPosition(), player, rate);
                }
            }
        }
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
        if (getFocus(stack) == null) {
            return 0;
        }
        return getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.treasure);
    }

    @Override
    public int getFocusFrugal(ItemStack stack) {
        if (getFocus(stack) == null) {
            return 0;
        }
        return getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.frugal);
    }

    @Override
    public int getFocusEnlarge(ItemStack stack) {
        if (getFocus(stack) == null) {
            return 0;
        }
        return getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.enlarge);
    }

    @Override
    public int getFocusExtend(ItemStack stack) {
        if (getFocus(stack) == null) {
            return 0;
        }
        return getFocus(stack).getUpgradeLevel(getFocusStack(stack), FocusUpgradeType.extend);
    }

    public float getChargeLevel(ItemStack stack) {
        return -1.0F;
    }

    @Override
    public Component getName(ItemStack stack) {
        // NEOFORGE_PORT: getDescriptionId(stack) is final in 26.x -> getName override (same body).
        try {
            WandCap cap = getCap(stack);
            WandRod rod = getRod(stack);
            if (cap == null || rod == null) {
                return super.getName(stack);
            }
            String name = Component.translatable("item.Wand.name").getString();
            name = name.replace("%CAP", Component.translatable("item.Wand." + cap.getTag() + ".cap").getString());
            String rodTag = rod.getTag();
            if (rodTag.contains("_staff")) {
                rodTag = rodTag.substring(0, rodTag.indexOf("_staff"));
            }
            name = name.replace("%ROD", Component.translatable("item.Wand." + rodTag + ".rod").getString());
            String obj = isStaff(stack) ? Component.translatable("item.Wand.staff.obj").getString()
                    : (isSceptre(stack) ? Component.translatable("item.Wand.sceptre.obj").getString()
                    : Component.translatable("item.Wand.wand.obj").getString());
            return Component.literal(name.replace("%OBJ", obj));
        } catch (Exception e) {
            return super.getName(stack);
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IWandable wandable
                && wandable.onWandRightClick(level, stack, player, pos, ctx.getClickedFace())) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof IWandable wandable
                && wandable.onWandRightClick(level, stack, player, pos, ctx.getClickedFace())) {
            return InteractionResult.SUCCESS;
        }
        if (WandTriggerRegistry.hasTrigger(state)
                && WandTriggerRegistry.performTrigger(level, stack, player, pos, ctx.getClickedFace(), state)) {
            return InteractionResult.SUCCESS;
        }
        if (state.getBlock() instanceof arcaneresearch.api.casters.IInteractWithCaster casterBlock
                && casterBlock.onCasterRightClick(level, stack, player, pos, ctx.getClickedFace(), ctx.getHand())) {
            return InteractionResult.SUCCESS;
        }
        if (tile instanceof arcaneresearch.api.casters.IInteractWithCaster casterTile
                && casterTile.onCasterRightClick(level, stack, player, pos, ctx.getClickedFace(), ctx.getHand())) {
            return InteractionResult.SUCCESS;
        }
        if (arcaneresearch.api.casters.CasterTriggerRegistry.hasTrigger(state)
                && arcaneresearch.api.casters.CasterTriggerRegistry.performTrigger(level, stack, player, pos,
                        ctx.getClickedFace(), state)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // NEOFORGE_PORT: InteractionResultHolder removed in 26.x -> InteractionResult.
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockEntity be = level.getBlockEntity(hit.getBlockPos());
            if (be instanceof arcaneresearch.common.tiles.aura.AuraNodeTile
                    || be instanceof arcaneresearch.common.tiles.aura.TileJarNode) {
                setObjectInUse(stack, hit.getBlockPos().getX(), hit.getBlockPos().getY(),
                        hit.getBlockPos().getZ());
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
        }
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null && !focus.isVisCostPerTick(getFocusStack(stack))
                && !WandManager.isOnCooldown(player)
                && consumeAllVis(stack, player, focus.getVisCost(stack), true, false)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
            if (focus.onFocusActivation(stack, level, player, hit, 0)) {
                consumeAllVis(stack, player, focus.getVisCost(stack), level.isClientSide(), false);
            }
            return InteractionResult.SUCCESS;
        }
        if (focus != null && focus.isVisCostPerTick(getFocusStack(stack)) && !WandManager.isOnCooldown(player)) {
            player.startUsingItem(hand);
            WandManager.setCooldown(player, -1);
            return InteractionResult.CONSUME;
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }
        IWandable target = getObjectInUse(stack, level);
        if (target != null) {
            target.onUsingWandTick(stack, player, remainingTicks);
            return;
        }
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            if (consumeAllVis(stack, player, focus.getVisCost(stack), true, false)) {
                BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
                WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
                if (focus.onFocusActivation(stack, level, player, hit, remainingTicks)) {
                    consumeAllVis(stack, player, focus.getVisCost(stack), level.isClientSide(), false);
                }
            } else {
                player.stopUsingItem();
            }
        }
    }

    public void setObjectInUse(ItemStack stack, int x, int y, int z) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.putInt("IIUX", x);
        root.putInt("IIUY", y);
        root.putInt("IIUZ", z);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    public void clearObjectInUse(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.remove("IIUX");
        root.remove("IIUY");
        root.remove("IIUZ");
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    public IWandable getObjectInUse(ItemStack stack, Level level) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("IIUX")) {
            BlockEntity tile = level.getBlockEntity(new BlockPos(root.getIntOr("IIUX", 0),
                    root.getIntOr("IIUY", 0), root.getIntOr("IIUZ", 0)));
            if (tile instanceof IWandable wandable) {
                return wandable;
            }
        }
        return null;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // NEOFORGE_PORT: void -> boolean in 26.x (verified signature via javap on Item);
        // body 1:1, true when the stop-use was handled.
        if (entity instanceof Player player) {
            IWandable target = getObjectInUse(stack, level);
            if (target != null) {
                target.onWandStoppedUsing(stack, level, player, timeLeft);
            }
            clearObjectInUse(stack);
            return true;
        }
        return false;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // NEOFORGE_PORT: UseAnim -> ItemUseAnimation.
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // NEOFORGE_PORT: getUseDuration now takes the using entity; 72000 as in Forge
        // (bow-like "infinite" for the game, but float-exact; MAX_VALUE breaks
        // BOW use-animation math with its float step of 256).
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        // NEOFORGE_PORT: IItemExtension hand-aware variant; base Item returns false.
        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && !WandManager.isOnCooldown(entity)) {
            WandManager.setCooldown(entity, getFocus(stack).getActivationCooldown(focusStack));
            return focusStack.getItem().onEntitySwing(stack, entity, hand);
        }
        return false;
    }

    // NEOFORGE_PORT: no @Override (no such method on Item in 26.x; wired via
    // ToolEvents.playerLeftClickBlock, same body as Forge).
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
            return focus.onFocusBlockStartBreak(stack, pos, player);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null) {
            return getFocusStack(stack).getItem().getDestroySpeed(getFocusStack(stack), state);
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null) {
            return getFocusStack(stack).isCorrectToolForDrops(state);
        }
        return false;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side,
                                                  Player player) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus instanceof IArchitect architect) {
            return architect.getArchitectBlocks(stack, level, pos, side, player);
        }
        return new ArrayList<>();
    }

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity entity) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus instanceof IArchitectExtended extended) {
            return (BlockHitResult) extended.getArchitectMOP(stack, level, entity);
        }
        if (focus instanceof IArchitect architect) {
            return architect.getArchitectMOP(stack, level, entity);
        }
        return null;
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus instanceof IArchitectExtended extended) {
            return extended.useBlockHighlight(stack);
        }
        if (focus instanceof IArchitect architect) {
            return architect.useBlockHighlight(stack);
        }
        return true;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus instanceof IArchitect architect
                && focus.isUpgradedWith(getFocusStack(stack), FocusUpgradeType.architect)) {
            return architect.showAxis(stack, level, player, side, axis);
        }
        return false;
    }

    // Level for focus info comes from context.level() (nullable, as ItemFocusBasic allows).
    // Shift state via sided proxy (server returns false; tooltips only render on client).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        boolean shift = Proxies.get().isShiftKeyDown();
        Level tooltipLevel = context.level();
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null) {
            String line = "";
            int total = 0;
            int count = 0;
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (root.contains(aspect.getTag())) {
                    String amount = String.valueOf(getVis(stack, aspect));
                    float mod = getConsumptionModifier(stack, null, aspect, false);
                    String consumption = formatter.format(mod * 100.0F);
                    count++;
                    total += (int) (mod * 100.0F);
                    String extra = "";
                    ItemStack focusStack = getFocusStack(stack);
                    if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocusBasic basic) {
                        float cost = basic.getVisCost(focusStack).getAmount(aspect) * mod;
                        if (cost > 0.0F) {
                            extra = ", " + formatter.format(cost) + " "
                                    + Component.translatable(basic.isVisCostPerTick(focusStack)
                                    ? "item.Focus.cost2" : "item.Focus.cost1").getString();
                        }
                    }
                    if (shift) {
                        tooltip.accept(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + " x " + amount
                                + ", (" + consumption + "% "
                                + Component.translatable("tc.vis.cost").getString() + ")" + extra));
                    } else {
                        line = line.isEmpty() ? "" : line + " | ";
                        line += "§" + aspect.getChatcolor() + amount + "§r";
                    }
                }
            }
            if (!shift && count > 0) {
                tooltip.accept(Component.literal(line));
                total /= count;
                tooltip.accept(Component.literal(" (" + total + "% "
                        + Component.translatable("tc.vis.costavg").getString() + ")"));
            }
        }
        WandCap cap = getCap(stack);
        if (cap != null) {
            tooltip.accept(Component.literal(ChatFormatting.GOLD + Component.translatable("item.capacity.text").getString()
                    + " " + getMaxVis(stack) + "§r"));
            // NEOFORGE_PORT: Forge insert order was capacity, rechargemult, chargebonus
            // (second add(insert+1) lands before the first); appends here keep that order.
            if (cap.getRechargeMultiplier() != 1.0F) {
                tooltip.accept(Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.rechargemult.text").getString() + " x"
                        + cap.getRechargeMultiplier() + "§r"));
            }
            if (cap.getChargeBonus() > 0) {
                tooltip.accept(Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.chargebonus.text").getString() + " +"
                        + cap.getChargeBonus() + "§r"));
            }
        }
        if (getFocus(stack) != null) {
            ItemStack focusStack = getFocusStack(stack);
            tooltip.accept(Component.literal(ChatFormatting.BOLD.toString() + ChatFormatting.ITALIC
                    + ChatFormatting.GREEN + focusStack.getHoverName().getString()));
            if (shift && focusStack.getItem() instanceof ItemFocusBasic basic) {
                // NEOFORGE_PORT: addFocusInformation still takes List<Component> -> buffer then accept.
                List<Component> focusInfo = new ArrayList<>();
                basic.addFocusInformation(focusStack, tooltipLevel, focusInfo, flag);
                for (Component lineComponent : focusInfo) {
                    tooltip.accept(lineComponent);
                }
            }
        }
    }
}
