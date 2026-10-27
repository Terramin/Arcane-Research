package arcaneresearch.common.items.wands;

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
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.wands.WandAuraHelper;
import arcaneresearch.common.lib.wands.WandRegistry;
import arcaneresearch.proxies.Proxies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ItemWand extends Item implements IWand, IArchitect, IArchitectExtended {
    private final DecimalFormat formatter = new DecimalFormat("#######.#");

    public ItemWand(Properties props) {

        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    private static ItemStack decodeStack(CompoundTag parent) {
        if (parent == null || !parent.contains("focus")) {
            return ItemStack.EMPTY;
        }
        Tag inner = parent.get("focus");
        if (inner == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.CODEC.parse(arcaneresearch.common.lib.utils.RegistryNbt.ops(), inner).result().orElse(ItemStack.EMPTY);
    }

    private static void encodeStack(CompoundTag parent, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            parent.remove("focus");
            return;
        }
        ItemStack.CODEC.encodeStart(arcaneresearch.common.lib.utils.RegistryNbt.ops(), stack.copy()).result().ifPresent(encoded -> parent.put("focus", encoded));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
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
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root == null || !root.contains(aspect.getTag())) {
            return 0;
        }
        return root.getIntOr(aspect.getTag(), 0);
    }

    @Override
    public AspectList getAllVis(ItemStack stack) {
        AspectList out = new AspectList();

        if (stack == null || stack.isEmpty()) {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                out.merge(aspect, 0);
            }
            return out;
        }

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
            String key = aspect.getTag();
            if (root != null && root.contains(key)) {
                out.merge(aspect, root.getIntOr(key, 0));
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
        int maxVis = getMaxVisInternal(stack);
        for (Aspect aspect : cur.getAspects()) {
            int current = cur.getAmount(aspect);
            if (current < maxVis) {
                int room = Math.max(1, (maxVis - current) / 100);
                out.add(aspect, room);
            }
        }
        return out;
    }

    private void storeVisInternal(ItemStack stack, Aspect aspect, int amount) {

        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        root.putInt(aspect.getTag(), amount);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, Aspect aspect, int amount, boolean crafting) {
        amount *= 100;
        amount = (int) (amount * getConsumptionModifier(stack, player, aspect, crafting));

        int current = getVisInternal(stack, aspect);
        if (current >= amount) {
            storeVisInternal(stack, aspect, current - amount);
            return true;
        }
        return false;
    }

    @SuppressWarnings("resource")
    private static boolean isClientSide(Player player) {
        return player != null && player.level().isClientSide();
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

            if (amount <= 0) {
                continue;
            }

            if (getVisInternal(stack, aspect) < amount) {
                return false;
            }
            needed.add(aspect, amount);
        }
        if (!simulate && !isClientSide(player)) {
            for (Aspect aspect : needed.getAspects()) {
                int current = getVisInternal(stack, aspect);
                storeVisInternal(stack, aspect, current - needed.getAmount(aspect));
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
            List<Aspect> specialAspects = cap.getSpecialCostModifierAspects();
            if (specialAspects != null && specialAspects.contains(aspect)) {
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

        int efficiency = EnumInfusionEnchantment.getInfusionEnchantmentLevel(
                stack, EnumInfusionEnchantment.MAGIC_EFFICIENCY);
        if (efficiency > 0) {
            modifier -= efficiency / 10.0F;
        }

        return Math.max(modifier, 0.1F);
    }

    /**
     * Returns all aspects currently stored in the wand.
     * Возвращает все аспекты, в данный момент хранящиеся в жезле.
     */
    //TODO Что-то нужно с этим сделать
    @SuppressWarnings("unused")
    public AspectList getAspectsInChargeable(ItemStack stack) {
        return getAllVis(stack);
    }

    //TODO Допилить потом исправление поведения и обработку выхода
    public Aspect handleRecharge(Level level, ItemStack stack, BlockPos pos, Player player, int amount) {
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
    public ItemFocusBasic getFocus(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("focus")) {
            ItemStack focusStack = decodeStack(root);
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
            return decodeStack(root);
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
            encodeStack(root, focus);
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
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof Player player) {
            WandRod rod = getRod(stack);
            if (rod != null && rod.getOnUpdate() != null) {
                rod.getOnUpdate().onUpdate(stack, player);
            }
            if (player.tickCount % 5 == 0) {


                net.minecraft.world.entity.player.Inventory inv = player.getInventory();
                int slotId = -1;
                if (slot == EquipmentSlot.MAINHAND) {
                    slotId = inv.getSelectedSlot();
                } else if (slot == null) {
                    java.util.List<ItemStack> main = inv.getNonEquipmentItems();
                    for (int i = 0; i < main.size(); i++) {
                        if (main.get(i) == stack) {
                            slotId = i;
                            break;
                        }
                    }
                } else if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    slotId = slot.getIndex();
                } else if (slot == EquipmentSlot.OFFHAND) {
                    slotId = 0;
                }
                boolean selected = slotId >= 0 && slotId == inv.getSelectedSlot();
                int rate = slotId >= 0 ? WandManager.getBaseChargeRate(player, selected, slotId) : 0;
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
    public @NonNull Component getName(@NonNull ItemStack stack) {

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
    public @NonNull InteractionResult onItemUseFirst(@NonNull ItemStack stack, UseOnContext ctx) {
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
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof arcaneresearch.common.tiles.aura.AuraNodeTile
                    || be instanceof arcaneresearch.common.tiles.aura.TileJarNode) {
                setObjectInUse(stack, pos.getX(), pos.getY(), pos.getZ());
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
        }

        ItemFocusBasic focus = getFocus(stack);
        if (focus == null) {
            return super.use(level, player, hand);
        }

        ItemStack focusStack = getFocusStack(stack);

        if (!focus.isVisCostPerTick(focusStack) && !WandManager.isOnCooldown(player)) {
            AspectList cost = focus.getVisCost(stack);
            if (consumeAllVis(stack, player, cost, true, false)) {
                WandManager.setCooldown(player, focus.getActivationCooldown(focusStack));
                if (focus.onFocusActivation(stack, level, player, hit, 0)) {
                    consumeAllVis(stack, player, cost, level.isClientSide(), false);
                    arcaneresearch.helper.ClientRunHelper.runOnClient(
                            () -> arcaneresearch.client.renderers.item.WandUseAnimation.noteCast(hand));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (focus.isVisCostPerTick(focusStack) && !WandManager.isOnCooldown(player)) {
            player.startUsingItem(hand);
            WandManager.setCooldown(player, -1);
            return InteractionResult.CONSUME;
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity entity, @NonNull ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }

        IWandable target = getObjectInUse(stack, level);
        if (target != null) {
            target.onUsingWandTick(stack, player, remainingTicks);
            return;
        }

        ItemFocusBasic focus = getFocus(stack);
        if (focus == null || WandManager.isOnCooldown(player)) {
            return;
        }

        AspectList cost = focus.getVisCost(stack);
        if (!consumeAllVis(stack, player, cost, true, false)) {
            player.stopUsingItem();
            return;
        }

        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
        if (focus.onFocusActivation(stack, level, player, hit, remainingTicks)) {
            consumeAllVis(stack, player, cost, level.isClientSide(), false);
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
            BlockPos pos = new BlockPos(root.getIntOr("IIUX", 0),
                    root.getIntOr("IIUY", 0), root.getIntOr("IIUZ", 0));
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof IWandable wandable) {
                return wandable;
            }
        }
        return null;
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        IWandable target = getObjectInUse(stack, level);
        if (target != null) {
            target.onWandStoppedUsing(stack, level, player, timeLeft);
        }
        clearObjectInUse(stack);
        return true;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {

        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NonNull LivingEntity entity) {


        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NonNull ItemStack oldStack, @NonNull ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean onEntitySwing(@NonNull ItemStack stack, @NonNull LivingEntity entity, @NonNull InteractionHand hand) {

        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && !WandManager.isOnCooldown(entity)) {
            WandManager.setCooldown(entity, getFocus(stack).getActivationCooldown(focusStack));
            return focusStack.getItem().onEntitySwing(stack, entity, hand);
        }
        return false;
    }


    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
            return focus.onFocusBlockStartBreak(stack, pos, player);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(@NonNull ItemStack stack, @NonNull BlockState state) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null) {
            ItemStack focusStack = getFocusStack(stack);
            if (!focusStack.isEmpty()) {
                return focusStack.getItem().getDestroySpeed(focusStack, state);
            }
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(@NonNull ItemStack stack, @NonNull BlockState state) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null) {
            return getFocusStack(stack).isCorrectToolForDrops(state);
        }
        return false;
    }

    //TODO Потенциальные проблемы с постоянной аллокацией памяти. Нужна практическая оценка
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
            HitResult result = extended.getArchitectMOP(stack, level, entity);
            return result instanceof BlockHitResult block ? block : null;
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


    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        boolean shift = Proxies.get().isShiftKeyDown();
        Level tooltipLevel = event.getEntity() != null ? event.getEntity().level() : null;
        ItemStack focusStack = getFocusStack(stack);
        ItemFocusBasic focus = focusStack.getItem() instanceof ItemFocusBasic b ? b : null;

        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null) {
            String line = "";
            int total = 0;
            int count = 0;
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                String aspectTag = aspect.getTag();
                if (!root.contains(aspectTag)) {
                    continue;
                }

                String amount = String.valueOf(getVis(stack, aspect));
                float mod = getConsumptionModifier(stack, null, aspect, false);
                String consumption = formatter.format(mod * 100.0F);
                count++;
                total += (int) (mod * 100.0F);

                String extra = "";
                if (focus != null) {
                    float cost = focus.getVisCost(focusStack).getAmount(aspect) * mod;
                    if (cost > 0.0F) {
                        extra = ", " + formatter.format(cost) + " "
                                + Component.translatable(focus.isVisCostPerTick(focusStack)
                                ? "item.Focus.cost2" : "item.Focus.cost1").getString();
                    }
                }

                if (shift) {
                    tooltip.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + " x " + amount
                            + ", (" + consumption + "% "
                            + Component.translatable("tc.vis.cost").getString() + ")" + extra));
                } else {
                    line = line.isEmpty() ? "" : line + " | ";
                    line += "§" + aspect.getChatcolor() + amount + "§r";
                }
            }
            if (!shift && count > 0) {
                tooltip.add(Component.literal(line));
                total /= count;
                tooltip.add(Component.literal(" (" + total + "% "
                        + Component.translatable("tc.vis.costavg").getString() + ")"));
            }
        }
        WandCap cap = getCap(stack);
        if (cap != null) {
            tooltip.add(Component.literal(ChatFormatting.GOLD + Component.translatable("item.capacity.text").getString()
                    + " " + getMaxVis(stack) + "§r"));

            if (cap.getRechargeMultiplier() != 1.0F) {
                tooltip.add(Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.rechargemult.text").getString() + " x"
                        + cap.getRechargeMultiplier() + "§r"));
            }
            if (cap.getChargeBonus() > 0) {
                tooltip.add(Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.chargebonus.text").getString() + " +"
                        + cap.getChargeBonus() + "§r"));
            }
        }
        if (getFocus(stack) != null) {
            tooltip.add(Component.literal(ChatFormatting.BOLD.toString() + ChatFormatting.ITALIC
                    + ChatFormatting.GREEN + focusStack.getHoverName().getString()));
            if (shift && focus != null) {
                List<Component> focusInfo = new ArrayList<>();
                focus.addFocusInformation(focusStack, tooltipLevel, focusInfo, event.getFlags());
                tooltip.addAll(focusInfo);
            }
        }
    }
}
