package arcaneresearch.common.items.consumables;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.ItemEssentiaContainer;
import arcaneresearch.common.tiles.essentia.TileJar;
import arcaneresearch.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ItemPhial extends ItemEssentiaContainer {

    public static final int PHIAL_CAPACITY = 10;

    private final boolean filled;

    public ItemPhial(boolean filled) {
        super(new Properties().stacksTo(64), PHIAL_CAPACITY);
        this.filled = filled;
    }

    public static ItemPhial createEmpty() {
        return new ItemPhial(false);
    }

    public static ItemPhial createFilled() {
        return new ItemPhial(true);
    }

    public static ItemStack makeFilledPhial(Aspect aspect) {
        return makePhial(aspect, PHIAL_CAPACITY);
    }

    public static ItemStack makePhial(Aspect aspect, int amount) {
        if (aspect == null) {
            return ItemStack.EMPTY;
        }

        Item item = ModItems.PHIAL_FILLED.get();

        ItemStack stack = new ItemStack(item);
        if (!(stack.getItem() instanceof ItemPhial phial)) {
            return ItemStack.EMPTY;
        }

        phial.setAspects(stack, new AspectList().add(aspect, amount));
        return stack;
    }

    private static Aspect firstAspect(AspectList aspects) {
        if (aspects == null) {
            return null;
        }

        Aspect[] array = aspects.getAspects();
        if (array == null || array.length == 0) {
            return null;
        }

        return array[0];
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (filled) {
            AspectList aspects = getAspects(stack);
            Aspect aspect = firstAspect(aspects);
            if (aspect != null) {
                return Component.translatable("item.arcaneresearch.phial_filled", aspect.getName());
            }
        }
        return super.getName(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        InteractionHand hand = context.getHand();
        ItemStack heldStack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide();

        if (!filled) {
            BlockEntity te = level.getBlockEntity(pos);

            if (te instanceof arcaneresearch.common.tiles.essentia.TileAlembic alembic) {
                Aspect aspect = alembic.aspect;
                if (aspect != null && alembic.amount >= PHIAL_CAPACITY) {
                    if (clientSide) {
                        return InteractionResult.SUCCESS;
                    }

                    ItemStack filledPhial = makeFilledPhial(aspect);
                    if (filledPhial.isEmpty()) {
                        return InteractionResult.FAIL;
                    }

                    if (alembic.takeFromContainer(aspect, PHIAL_CAPACITY)) {
                        heldStack.shrink(1);

                        if (!player.getInventory().add(filledPhial)) {
                            level.addFreshEntity(new ItemEntity(
                                    level,
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    filledPhial
                            ));
                        }

                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                        return InteractionResult.CONSUME;
                    }
                }
            }

            if (te instanceof TileJar jar) {
                Aspect aspect = jar.getAspect();
                if (aspect != null && jar.getAmount() >= PHIAL_CAPACITY) {
                    if (clientSide) {
                        return InteractionResult.SUCCESS;
                    }

                    ItemStack filledPhial = makeFilledPhial(aspect);
                    if (filledPhial.isEmpty()) {
                        return InteractionResult.FAIL;
                    }

                    if (jar.takeFromContainer(aspect, PHIAL_CAPACITY)) {
                        heldStack.shrink(1);

                        if (!player.getInventory().add(filledPhial)) {
                            level.addFreshEntity(new ItemEntity(
                                    level,
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5,
                                    filledPhial
                            ));
                        }

                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            AspectList myAspects = getAspects(heldStack);
            Aspect aspect = firstAspect(myAspects);

            if (aspect != null) {
                BlockEntity te = level.getBlockEntity(pos);

                if (te instanceof TileJar jar
                        && jar.getAmount() <= TileJar.CAPACITY - PHIAL_CAPACITY
                        && jar.doesContainerAccept(aspect)) {
                    if (clientSide) {
                        return InteractionResult.SUCCESS;
                    }

                    Item emptyItem = ModItems.PHIAL_EMPTY.get();

                    int leftover = jar.addToContainer(aspect, PHIAL_CAPACITY);

                    if (leftover == 0) {
                        heldStack.shrink(1);

                        ItemStack emptyPhial = new ItemStack(emptyItem);
                        if (!player.getInventory().add(emptyPhial)) {
                            level.addFreshEntity(new ItemEntity(
                                    level,
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5,
                                    emptyPhial
                            ));
                        }

                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
                                      BlockPos pos, Player player) {
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public boolean isFilled() {
        return filled;
    }
}