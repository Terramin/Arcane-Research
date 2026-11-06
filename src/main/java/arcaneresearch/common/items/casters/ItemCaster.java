package arcaneresearch.common.items.casters;

import arcaneresearch.api.casters.*;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.client.renderers.item.CasterItemRenderer;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketAuraToClient;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.world.aura.AuraHandler;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemCaster extends Item implements ICaster, IArchitect {

    private static final ThreadLocal<DecimalFormat> VIS_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#######.#"));

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
    public float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
        float modifier = 1.0f;
        if (player != null) {
            modifier -= CasterManager.getTotalVisDiscount(player);
        }
        return Math.max(modifier, 0.1f);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
        if (player == null) {
            return false;
        }

        amount *= getConsumptionModifier(stack, player, crafting);
        float available = getAuraPool(player);

        if (available < amount) {
            return false;
        }
        if (simulate) {
            return true;
        }
        return drainFromAura(player, amount);
    }

    private float getAuraPool(Player player) {
        if (player == null) {
            return 0.0f;
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();

        return switch (auraArea) {
            case 1 -> {
                float total = AuraHandler.getVis(level, pos);
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    total += AuraHandler.getVis(level, pos.relative(face, 16));
                }
                yield total;
            }
            case 2 -> {
                float total = 0.0f;
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        total += AuraHandler.getVis(level, pos.offset(xx * 16, 0, zz * 16));
                    }
                }
                yield total;
            }
            default -> AuraHandler.getVis(level, pos);
        };
    }

    private boolean drainFromAura(Player player, float amount) {
        if (player == null) {
            return false;
        }
        if (amount <= 0.0f) {
            return true;
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();

        switch (auraArea) {
            case 1 -> {
                while (amount > 0.0f) {
                    float before = amount;
                    float chunk = amount / 5.0f;

                    float take = Math.min(chunk, amount);
                    amount -= AuraHandler.drainVis(level, pos, take, false);

                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        if (amount <= 0.0f) {
                            break;
                        }
                        take = Math.min(chunk, amount);
                        amount -= AuraHandler.drainVis(level, pos.relative(face, 16), take, false);
                    }

                    if (amount >= before) {
                        break;
                    }
                }
                return amount <= 0.0f;
            }
            case 2 -> {
                while (amount > 0.0f) {
                    float before = amount;
                    float chunk = amount / 9.0f;

                    outer:
                    for (int xx = -1; xx <= 1; xx++) {
                        for (int zz = -1; zz <= 1; zz++) {
                            if (amount <= 0.0f) {
                                break outer;
                            }
                            float take = Math.min(chunk, amount);
                            amount -= AuraHandler.drainVis(level, pos.offset(xx * 16, 0, zz * 16), take, false);
                        }
                    }

                    if (amount >= before) {
                        break;
                    }
                }
                return amount <= 0.0f;
            }
            default -> {
                return AuraHandler.drainVis(level, pos, amount, false) >= amount;
            }
        }
    }

    @Override
    @Nullable
    public Item getFocus(ItemStack stack) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack != null && !focusStack.isEmpty()) {
            return focusStack.getItem();
        }
        return null;
    }

    @Override
    @Nullable
    public ItemStack getFocusStack(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return null;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("focus")) {
            return null;
        }

        return ItemStack.of(tag.getCompound("focus"));
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (stack == null) {
            return;
        }

        if (focus == null || focus.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove("focus");
            }
        } else {
            stack.getOrCreateTag().put("focus", focus.save(new CompoundTag()));
        }
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
        if (focusStack == null || focusStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return ItemStack.EMPTY;
        }

        for (IFocusElement fe : fp.nodes) {
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
        if (focusStack == null || focusStack.isEmpty()) {
            return null;
        }

        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null) {
            return null;
        }

        if (!FocusEngine.doesPackageContainElement(fp, "arcaneresearch.PLAN")) {
            return null;
        }

        IFocusElement plan = FocusEngine.getElement("arcaneresearch.PLAN");
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
        if (focusStack == null || focusStack.isEmpty()) {
            return null;
        }

        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return null;
        }

        for (IFocusElement fe : fp.nodes) {
            if (fe instanceof IArchitect architect) {
                return architect.getArchitectBlocks(stack, level, pos, side, player);
            }
        }

        return null;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack == null || focusStack.isEmpty()) {
            return false;
        }

        FocusPackage fp = ItemFocus.getPackage(focusStack);
        if (fp == null || fp.nodes == null) {
            return false;
        }

        for (IFocusElement fe : fp.nodes) {
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

        if (state.getBlock() instanceof IInteractWithCaster casterBlock) {
            if (casterBlock.onCasterRightClick(level, stack, player, pos, side, hand)) {
                return InteractionResult.PASS;
            }
        }

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof IInteractWithCaster casterTile) {
            if (casterTile.onCasterRightClick(level, stack, player, pos, side, hand)) {
                return InteractionResult.PASS;
            }
        }

        if (CasterTriggerRegistry.hasTrigger(state)) {
            return CasterTriggerRegistry.performTrigger(level, stack, player, pos, side, state)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        }

        ItemStack fb = getFocusStack(stack);
        if (fb != null && !fb.isEmpty() && player != null && tile == null) {
            FocusPackage core = ItemFocus.getPackage(fb);
            if (core != null && core.nodes != null) {
                for (IFocusElement fe : core.nodes) {
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(stack);

        if (focusStack == null || focusStack.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(focusStack.getItem() instanceof ItemFocus focus)) {
            return InteractionResultHolder.pass(stack);
        }
        if (CasterManager.isOnCooldown(player)) {
            return InteractionResultHolder.pass(stack);
        }

        CasterManager.setCooldown(player, focus.getActivationTime(focusStack));

        if (player.isCrouching()) {
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core != null && core.nodes != null) {
                for (IFocusElement fe : core.nodes) {
                    if (fe instanceof IFocusBlockPicker) {
                        return InteractionResultHolder.pass(stack);
                    }
                }
            }
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        float visCost = focus.getVisCost(focusStack);
        if (!consumeVis(stack, player, visCost, false, false)) {
            return InteractionResultHolder.fail(stack);
        }

        FocusPackage focusPackage = ItemFocus.getPackage(focusStack);
        if (focusPackage != null) {
            FocusEngine.castFocusPackage(player, focusPackage);
        }

        player.swing(hand);
        int cooldown = focus.getActivationTime(focusStack);
        player.getCooldowns().addCooldown(this, cooldown);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        boolean held = isSelected
                || (entity instanceof Player player && player.getOffhandItem() == stack);

        if (level.isClientSide() || !held || entity.tickCount % 10 != 0) {
            return;
        }
        if (!(entity instanceof ServerPlayer serverPlayer)) {
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
                    var neighbour = AuraHandler.getAuraChunk(
                            dimension,
                            (origin.getX() >> 4) + face.getStepX(),
                            (origin.getZ() >> 4) + face.getStepZ()
                    );
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
                        var chunk = AuraHandler.getAuraChunk(
                                dimension,
                                (origin.getX() >> 4) + xx,
                                (origin.getZ() >> 4) + zz
                        );
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

            if (oldFocus == null && newFocus == null) {
                return false;
            }
            if (oldFocus == null || newFocus == null) {
                return true;
            }

            if (oldFocus.getItem() instanceof ItemFocus oldF && newFocus.getItem() instanceof ItemFocus newF) {
                String oldSort = oldF.getSortingHelper(oldFocus);
                String newSort = newF.getSortingHelper(newFocus);
                if (oldSort != null && newSort != null) {
                    return !oldSort.equals(newSort);
                }
            }

            return !ItemStack.isSameItemSameTags(oldFocus, newFocus);
        }

        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ItemStack focusStack = getFocusStack(stack);

        if (focusStack != null && !focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {
            float visCost = focus.getVisCost(focusStack);
            if (visCost > 0) {
                tooltip.add(Component.translatable("tc.vis.cost")
                        .append(" ")
                        .append(Component.literal(VIS_FORMAT.get().format(visCost)))
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
            }
            tooltip.add(Component.empty());
            tooltip.add(focusStack.getHoverName()
                    .copy()
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