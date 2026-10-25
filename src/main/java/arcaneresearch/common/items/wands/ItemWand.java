package arcaneresearch.common.items.wands;

import arcaneresearch.client.renderers.item.WandUseAnimation;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.*;
import arcaneresearch.common.lib.wands.WandAuraHelper;
import arcaneresearch.common.lib.wands.WandRegistry;
import arcaneresearch.proxies.Proxies;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemWand extends Item implements IWand, IArchitect, IArchitectExtended {
    private final DecimalFormat formatter = new DecimalFormat("#######.#");

    public ItemWand() {
        super(new Properties().stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(WandUseAnimation.HAND_EXTENSION);
    }

    @SuppressWarnings("resource")
    private static boolean isClientSide(Player player) {
        return player != null && player.level().isClientSide;
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

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(aspect.getTag())) {
            return 0;
        }

        return tag.getInt(aspect.getTag());
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

        CompoundTag tag = stack.getTag();

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            if (tag != null && tag.contains(aspect.getTag())) {
                out.merge(aspect, tag.getInt(aspect.getTag()) / 100);
            } else {
                out.merge(aspect, 0);
            }
        }
        return out;
    }

    private AspectList getAllVisInternal(ItemStack stack) {
        AspectList out = new AspectList();
        CompoundTag tag = stack.getTag();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            String key = aspect.getTag();
            if (tag != null && tag.contains(key)) {
                out.merge(aspect, tag.getInt(key));
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
        stack.getOrCreateTag().putInt(aspect.getTag(), amount);
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
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("focus")) {
            return null;
        }
        ItemStack focusStack = ItemStack.of(tag.getCompound("focus"));
        if (!focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocusBasic basic) {
            return basic;
        }
        return null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("focus")) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound("focus"));
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (stack.isEmpty()) {
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
    public boolean isStaff(ItemStack stack) {
        WandRod rod = getRod(stack);
        return rod != null && rod.isStaff();
    }

    @Override
    public boolean isSceptre(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("sceptre");
    }

    public static void setSceptre(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putBoolean("sceptre", true);
        }
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
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            WandRod rod = getRod(stack);
            if (rod != null && rod.getOnUpdate() != null) {
                rod.getOnUpdate().onUpdate(stack, player);
            }
            if (player.tickCount % 5 == 0) {
                int rate = WandManager.getBaseChargeRate(player, selected, slot);
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

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        try {
            WandCap cap = getCap(stack);
            WandRod rod = getRod(stack);
            if (cap == null || rod == null) {
                return super.getDescriptionId(stack);
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
            return name.replace("%OBJ", obj);
        } catch (Exception e) {
            return super.getDescriptionId(stack);
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof arcaneresearch.common.tiles.aura.AuraNodeTile
                    || be instanceof arcaneresearch.common.tiles.aura.TileJarNode) {
                setObjectInUse(stack, pos.getX(), pos.getY(), pos.getZ());
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
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
                    consumeAllVis(stack, player, cost, level.isClientSide, false);
                    arcaneresearch.helper.ClientRunHelper.runOnClient(
                            () -> arcaneresearch.client.renderers.item.WandUseAnimation.noteCast(hand));
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }

        if (focus.isVisCostPerTick(focusStack) && !WandManager.isOnCooldown(player)) {
            player.startUsingItem(hand);
            WandManager.setCooldown(player, -1);
            return InteractionResultHolder.consume(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingTicks) {
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
            consumeAllVis(stack, player, cost, level.isClientSide, false);
        }
    }

    public void setObjectInUse(ItemStack stack, int x, int y, int z) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("IIUX", x);
        tag.putInt("IIUY", y);
        tag.putInt("IIUZ", z);
    }

    public void clearObjectInUse(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("IIUX");
        tag.remove("IIUY");
        tag.remove("IIUZ");
    }

    public IWandable getObjectInUse(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("IIUX")) {
            BlockPos pos = new BlockPos(tag.getInt("IIUX"), tag.getInt("IIUY"), tag.getInt("IIUZ"));
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof IWandable wandable) {
                return wandable;
            }
        }
        return null;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        IWandable target = getObjectInUse(stack, level);
        if (target != null) {
            target.onWandStoppedUsing(stack, level, player, timeLeft);
        }
        clearObjectInUse(stack);
    }

    //Нужен для некоторых взаимодействий без кастомной анимации
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && !WandManager.isOnCooldown(entity)) {
            WandManager.setCooldown(entity, getFocus(stack).getActivationCooldown(focusStack));
            return focusStack.getItem().onEntitySwing(stack, entity);
        }
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        ItemFocusBasic focus = getFocus(stack);
        if (focus != null && !WandManager.isOnCooldown(player)) {
            WandManager.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
            return focus.onFocusBlockStartBreak(stack, pos, player);
        }
        return false;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
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
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        boolean shift = false;
        try {
            shift = Proxies.get().isShiftKeyDown();
        } catch (Exception ignored) {
        }

        // getFocusStack — дорогая NBT-операция, кешируем один раз на весь метод
        ItemStack focusStack = getFocusStack(stack);
        ItemFocusBasic focus = focusStack.getItem() instanceof ItemFocusBasic b ? b : null;

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            String line = "";
            int total = 0;
            int count = 0;

            for (Aspect aspect : Aspect.getPrimalAspects()) {
                String aspectTag = aspect.getTag();
                if (!tag.contains(aspectTag)) {
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

        int insert = tooltip.size();
        WandCap cap = getCap(stack);
        if (cap != null) {
            tooltip.add(insert, Component.literal(ChatFormatting.GOLD + Component.translatable("item.capacity.text").getString()
                    + " " + getMaxVis(stack) + "§r"));
            if (cap.getChargeBonus() > 0) {
                tooltip.add(insert + 1, Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.chargebonus.text").getString() + " +"
                        + cap.getChargeBonus() + "§r"));
            }
            if (cap.getRechargeMultiplier() != 1.0F) {
                tooltip.add(insert + 1, Component.literal(ChatFormatting.AQUA
                        + Component.translatable("item.rechargemult.text").getString() + " x"
                        + cap.getRechargeMultiplier() + "§r"));
            }
        }

        if (focus != null) {
            tooltip.add(Component.literal(ChatFormatting.BOLD.toString() + ChatFormatting.ITALIC
                    + ChatFormatting.GREEN + focusStack.getHoverName().getString()));
            if (shift) {
                focus.addFocusInformation(focusStack, level, tooltip, flag);
            }
        }
    }
}
