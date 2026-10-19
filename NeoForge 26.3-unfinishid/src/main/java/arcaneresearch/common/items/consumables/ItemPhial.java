package arcaneresearch.common.items.consumables;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.ItemEssentiaContainer;
import arcaneresearch.common.tiles.essentia.TileJar;
import arcaneresearch.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPhial extends ItemEssentiaContainer {

    public static final int PHIAL_CAPACITY = 10;

    private final boolean filled;

    public ItemPhial(Item.Properties props, boolean filled) {
        super(props.stacksTo(64), PHIAL_CAPACITY);
        this.filled = filled;
    }

    public static ItemPhial createEmpty(Item.Properties props) {
        return new ItemPhial(props, false);
    }

    public static ItemPhial createFilled(Item.Properties props) {
        return new ItemPhial(props, true);
    }

    public static ItemStack makeFilledPhial(Aspect aspect) {
        return makePhial(aspect, PHIAL_CAPACITY);
    }

    public static ItemStack makePhial(Aspect aspect, int amount) {
        ItemStack stack = new ItemStack(ModItems.PHIAL_FILLED.get());
        ItemPhial phial = (ItemPhial) stack.getItem();
        phial.setAspects(stack, new AspectList().add(aspect, amount));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (filled) {
            AspectList aspects = getAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                Aspect aspect = aspects.getAspects()[0];
                return Component.translatable("item.arcaneresearch.phial_filled", aspect.getName());
            }
        }
        return super.getName(stack);
    }

    // NEOFORGE_PORT: Forge appendHoverText(ItemStack, Level, List, Flag) 1:1 logic kept
    // below (plain method); 26.x vanilla calls the 5-arg variant, which delegates to it
    // (same pattern as ItemFocus).
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (filled) {
            AspectList aspects = getAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                Aspect aspect = aspects.getAspects()[0];
                int amount = aspects.getAmount(aspect);
                tooltip.add(Component.literal(aspect.getName() + ": " + amount)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    // NEOFORGE_PORT: 26.x tooltip signature (TooltipContext/TooltipDisplay/Consumer).
    // Same content as Forge (aspect name + amount for filled phials).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        appendHoverText(stack, context.level(), lines, flag);
        for (Component line : lines) {
            tooltip.accept(line);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        if (player == null) return InteractionResult.PASS;
        ItemStack heldStack = player.getItemInHand(hand);

        if (!filled) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof arcaneresearch.common.tiles.essentia.TileAlembic alembic
                    && alembic.amount >= PHIAL_CAPACITY) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                ItemStack filledPhial = makeFilledPhial(alembic.aspect);
                if (alembic.takeFromContainer(alembic.aspect, PHIAL_CAPACITY)) {
                    heldStack.shrink(1);

                    if (!player.getInventory().add(filledPhial)) {
                        level.addFreshEntity(new ItemEntity(level,
                                player.getX(), player.getY(), player.getZ(), filledPhial));
                    }
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }
            if (te instanceof arcaneresearch.common.tiles.essentia.TileJar jar
                    && jar.getAmount() >= PHIAL_CAPACITY) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                Aspect asp = jar.getAspect();
                ItemStack filledPhial = makeFilledPhial(asp);
                if (jar.takeFromContainer(asp, PHIAL_CAPACITY)) {
                    heldStack.shrink(1);
                    if (!player.getInventory().add(filledPhial)) {
                        level.addFreshEntity(new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, filledPhial));
                    }
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }
        } else {
            AspectList myAspects = getAspects(heldStack);
            if (myAspects != null && myAspects.size() > 0) {
                Aspect aspect = myAspects.getAspects()[0];

                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof arcaneresearch.common.tiles.essentia.TileJar jar
                        && jar.getAmount() <= TileJar.CAPACITY - PHIAL_CAPACITY
                        && jar.doesContainerAccept(aspect)) {
                    if (level.isClientSide()) {
                        return InteractionResult.SUCCESS;
                    }

                    int leftover = jar.addToContainer(aspect, PHIAL_CAPACITY);
                    if (leftover == 0) {
                        heldStack.shrink(1);
                        ItemStack emptyPhial = new ItemStack(ModItems.PHIAL_EMPTY.get());

                        if (!player.getInventory().add(emptyPhial)) {
                            level.addFreshEntity(new ItemEntity(level,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, emptyPhial));
                        }

                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    // NEOFORGE_PORT: Forge verbatim (IItemExtension keeps the 4-arg form in 26.x,
    // verified in the NeoForge jar; same always-true behavior).
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
                                      BlockPos pos, Player player) {

        return true;
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // InteractionResultHolder.pass(stack) -> InteractionResult.PASS.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public boolean isFilled() {
        return filled;
    }
}
