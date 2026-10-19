package arcaneresearch.common.items.casters;

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
import net.minecraft.world.entity.LivingEntity; // NEOFORGE_PORT: needed by getUseDuration(ItemStack,LivingEntity)
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
// NEOFORGE_PORT (26.x): Forge net.minecraftforge.api.distmarker.* -> net.neoforged.api.distmarker.*.
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.casters.*;
import arcaneresearch.api.items.IArchitect;
// NEOFORGE_PORT: CasterItemRenderer (SpecialModelRenderer rework) deferred.
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.world.aura.AuraHandler;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class ItemCaster extends Item implements ICaster, IArchitect {

    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#######.#");

    private final int auraArea;

    public ItemCaster(Item.Properties props, int area) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
        this.auraArea = area;
    }

    public static ItemCaster createBasic(Item.Properties props) {
        return new ItemCaster(props, 0);
    }

    // NEOFORGE_PORT: ItemStack.save(CompoundTag)/ItemStack.of(CompoundTag) removed in 26.x ->
    // ItemStack.CODEC via NbtOps (same "focus"/"picked" keys; inner shape is codec form).
    private static ItemStack decodeStack(CompoundTag parent, String key) {
        if (parent == null || !parent.contains(key)) {
            return null;
        }
        Tag inner = parent.get(key);
        if (inner == null) {
            return null;
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
    public float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
        float modifier = 1.0f;
        if (player != null) {

            modifier -= CasterManager.getTotalVisDiscount(player);
        }
        return Math.max(modifier, 0.1f);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
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
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        switch (auraArea) {
            case 1 -> {
                float chunk = amount / 5.0f;
                while (amount > 0.0f) {
                    float take = Math.min(chunk, amount);
                    amount -= AuraHandler.drainVis(level, pos, take, false);
                    if (amount <= 0.0f) break;
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        take = Math.min(chunk, amount);
                        amount -= AuraHandler.drainVis(level, pos.relative(face, 16), take, false);
                        if (amount <= 0.0f) break;
                    }
                }
                return amount <= 0.0f;
            }
            case 2 -> {
                float chunk = amount / 9.0f;
                while (amount > 0.0f) {
                    for (int xx = -1; xx <= 1; xx++) {
                        for (int zz = -1; zz <= 1; zz++) {
                            float take = Math.min(chunk, amount);
                            amount -= AuraHandler.drainVis(level, pos.offset(xx * 16, 0, zz * 16), take, false);
                            if (amount <= 0.0f) break;
                        }
                        if (amount <= 0.0f) break;
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
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("focus")) {
            ItemStack focus = decodeStack(root, "focus");
            if (focus != null && !focus.isEmpty()) {
                return focus;
            }
        }
        return null;
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (stack.isEmpty()) {
            return;
        }
        // NEOFORGE_PORT: COPY + write-back via ArcaneresearchInvHelper (getCustomTag returns a copy).
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        encodeStack(root, "focus", focus);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root == null || !root.contains("picked")) {
            return ItemStack.EMPTY;
        }

        ItemStack focusStack = getFocusStack(stack);
        if (focusStack != null && !focusStack.isEmpty()) {
            FocusPackage fp = ItemFocus.getPackage(focusStack);
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IFocusBlockPicker) {
                        ItemStack picked = decodeStack(root, "picked");
                        return picked != null ? picked : ItemStack.EMPTY;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void storePickedBlock(ItemStack stack, ItemStack pickedBlock) {
        CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        encodeStack(root, "picked", pickedBlock);
        ArcaneresearchInvHelper.setCustomTag(stack, root);
    }

    @Override
    public net.minecraft.world.phys.BlockHitResult getArchitectMOP(ItemStack stack, Level level,
                                                                    net.minecraft.world.entity.LivingEntity player) {
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack != null && !focusStack.isEmpty()
                && getFocus(stack) != null) {
            FocusPackage fp = ItemFocus.getPackage(focusStack);
            if (fp != null && FocusEngine.doesPackageContainElement(fp, "arcaneresearch.PLAN")) {
                IFocusElement plan = FocusEngine.getElement("arcaneresearch.PLAN");
                if (plan instanceof IArchitect architect) {
                    return architect.getArchitectMOP(focusStack, level, player);
                }
            }
        }
        return null;
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public java.util.ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level,
                                                             BlockPos pos, net.minecraft.core.Direction side, Player player) {
        if (getFocus(stack) != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IArchitect architect) {
                        return architect.getArchitectBlocks(stack, level, pos, side, player);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player,
                             net.minecraft.core.Direction side, IArchitect.EnumAxis axis) {
        if (getFocus(stack) != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IArchitect architect) {
                        return architect.showAxis(stack, level, player, side, axis);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
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
                    ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        ItemStack fb = getFocusStack(stack);
        if (fb != null && !fb.isEmpty() && player != null) {
            FocusPackage core = ItemFocus.getPackage(fb);
            if (core != null) {
                for (IFocusElement fe : core.nodes) {
                    if (fe instanceof IFocusBlockPicker && player.isCrouching()
                            && level.getBlockEntity(pos) == null) {
                        if (!level.isClientSide()) {
                            ItemStack picked = new ItemStack(state.getBlock());
                            try {
                                if (!state.is(net.minecraft.world.level.block.Blocks.AIR)) {
                                    ItemStack silk = BlockUtils.getSilkTouchDrop(state, level, pos);
                                    if (silk != null && !silk.isEmpty()) {
                                        picked = silk.copy();
                                    }
                                }
                            } catch (Exception ex) {

                            }
                            storePickedBlock(stack, picked);
                            return InteractionResult.SUCCESS;
                        }
                        // NEOFORGE_PORT: swing(hand) -> swing(hand, SwingAnimation.DEFAULT, false).
                        player.swing(hand, SwingAnimation.DEFAULT, false);
                        return InteractionResult.PASS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // pass/success/fail holders -> InteractionResult.PASS/SUCCESS/FAIL.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(stack);

        if (focusStack == null || focusStack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!(focusStack.getItem() instanceof ItemFocus focus)) {
            return InteractionResult.PASS;
        }

        if (CasterManager.isOnCooldown(player)) {
            return InteractionResult.PASS;
        }
        CasterManager.setCooldown(player, focus.getActivationTime(focusStack));

        if (player.isCrouching()) {
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core != null) {
                for (IFocusElement fe : core.nodes) {
                    if (fe instanceof IFocusBlockPicker) {
                        return InteractionResult.PASS;
                    }
                }
            }
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        float visCost = focus.getVisCost(focusStack);

        if (!consumeVis(stack, player, visCost, false, false)) {

            return InteractionResult.FAIL;
        }

        FocusPackage focusPackage = ItemFocus.getPackage(focusStack);
        if (focusPackage != null) {
            FocusEngine.castFocusPackage(player, focusPackage);
        }

        player.swing(hand, SwingAnimation.DEFAULT, false);

        int cooldown = focus.getActivationTime(focusStack);
        player.getCooldowns().addCooldown(stack, cooldown);

        return InteractionResult.SUCCESS;
    }

    // NEOFORGE_PORT: inventoryTick is server-only in 26.x (ServerLevel + EquipmentSlot, no
    // isSelected/slotId); "held" mirrors the Forge check (main hand or offhand holds this stack).
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {

        boolean held = entity instanceof Player player
                && (player.getMainHandItem() == stack || player.getOffhandItem() == stack);
        if (held && entity.tickCount % 10 == 0
                && entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            float vis = 0.0f;
            float flux = 0.0f;
            short base = 0;
            BlockPos origin = serverPlayer.blockPosition();
            switch (auraArea) {
                default -> {

                    var chunk = arcaneresearch.common.world.aura.AuraHandler.getAuraChunk(
                            level.dimension(), origin.getX() >> 4, origin.getZ() >> 4);
                    if (chunk != null) {
                        vis = chunk.getVis();
                        flux = chunk.getFlux();
                        base = chunk.getBase();
                    }
                }
                case 1 -> {

                    var center = arcaneresearch.common.world.aura.AuraHandler.getAuraChunk(
                            level.dimension(), origin.getX() >> 4, origin.getZ() >> 4);
                    if (center == null) break;
                    vis = center.getVis();
                    flux = center.getFlux();
                    base = center.getBase();
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        var neighbour = arcaneresearch.common.world.aura.AuraHandler.getAuraChunk(
                                level.dimension(),
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
                            var chunk = arcaneresearch.common.world.aura.AuraHandler.getAuraChunk(
                                    level.dimension(),
                                    (origin.getX() >> 4) + xx,
                                    (origin.getZ() >> 4) + zz);
                            if (chunk != null) {
                                vis += chunk.getVis();
                                flux += chunk.getFlux();
                                base += chunk.getBase();
                            }
                        }
                    }
                }
            }
            // NEOFORGE_PORT: Forge PacketHandler.sendToPlayer(PacketAuraToClient) 1:1
            // (payload ported + registered; helper preserved).
            arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.misc.PacketAuraToClient(base, vis, flux), serverPlayer);
        }

    }

    // NEOFORGE_PORT: getUseDuration now takes the using entity.
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    // NEOFORGE_PORT: UseAnim -> ItemUseAnimation.
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        if (oldStack.getItem() == this && newStack.getItem() == this) {
            ItemStack oldFocus = getFocusStack(oldStack);
            ItemStack newFocus = getFocusStack(newStack);

            if (oldFocus == null && newFocus == null) return false;
            if (oldFocus == null || newFocus == null) return true;

            if (oldFocus.getItem() instanceof ItemFocus oldF && newFocus.getItem() instanceof ItemFocus newF) {
                String oldSort = oldF.getSortingHelper(oldFocus);
                String newSort = newF.getSortingHelper(newFocus);
                if (oldSort != null && newSort != null) {
                    return !oldSort.equals(newSort);
                }
            }
            // NEOFORGE_PORT: isSameItemSameTags removed -> isSameItemSameComponents.
            return !ItemStack.isSameItemSameComponents(oldFocus, newFocus);
        }
        return oldStack.getItem() != newStack.getItem();
    }

    // NEOFORGE_PORT: appendHoverText 5-arg (TooltipContext, TooltipDisplay, Consumer<Component>);
    // Level for focus info comes from context.level() (nullable, as ItemFocus allows).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        ItemStack focusStack = getFocusStack(stack);

        if (focusStack != null && !focusStack.isEmpty() && focusStack.getItem() instanceof ItemFocus focus) {

            float visCost = focus.getVisCost(focusStack);
            if (visCost > 0) {
                tooltip.accept(Component.translatable("tc.vis.cost")
                        .append(" ")
                        .append(Component.literal(VIS_FORMAT.format(visCost)))
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
            }

            tooltip.accept(Component.empty());
            tooltip.accept(focusStack.getHoverName()
                    .copy()
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));

            // NEOFORGE_PORT: addFocusInformation still takes List<Component> -> buffer then accept.
            java.util.List<Component> focusInfo = new java.util.ArrayList<>();
            focus.addFocusInformation(focusStack, context.level(), focusInfo, flag);
            for (Component lineComponent : focusInfo) {
                tooltip.accept(lineComponent);
            }
        } else {
            tooltip.accept(Component.translatable("item.arcaneresearch.caster.no_focus")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // NEOFORGE_PORT: intentionally a no-op. Forge wired CasterItemRenderer
        // (BEWLR) here via registerClientItemExtensions; in 26.x the same
        // render (base gauntlet mesh + focus overlay per display context) is
        // data-driven: assets/arcaneresearch/items/caster_basic.json
        // (minecraft:composite + arcaneresearch:caster_focus special layer,
        // see CasterFocusSpecialRenderer + SpecialItemModels).
    }
}
