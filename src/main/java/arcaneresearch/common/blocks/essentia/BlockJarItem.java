package arcaneresearch.common.blocks.essentia;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.common.tiles.essentia.TileAlembic;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.List;

public class BlockJarItem extends BlockItem implements IEssentiaContainerItem {

    public BlockJarItem(Block block, net.minecraft.world.item.Item.Properties properties) {
        super(block, properties);
    }

    public static float getFill(ItemStack stack) {
        double durability = getDurabilityForDisplay(stack);
        if (durability == 1.0) {
            return 0.0F;
        }
        if (durability >= 0.75) {
            return 1.0F;
        }
        if (durability >= 0.5) {
            return 2.0F;
        }
        if (durability >= 0.25) {
            return 3.0F;
        }
        return 4.0F;
    }

    public static double getDurabilityForDisplay(ItemStack stack) {
        AspectList aspects = getAspectsStatic(stack);
        return aspects == null ? 1.0 : 1.0 - aspects.visSize() / 250.0;
    }

    public static AspectList getAspectsStatic(ItemStack itemstack) {
        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same NBT shape).
        net.minecraft.nbt.CompoundTag tag =
                arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(itemstack);
        if (tag != null) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(tag);
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }

    public static Aspect getFilter(ItemStack itemstack) {
        net.minecraft.nbt.CompoundTag tag =
                arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(itemstack);
        if (tag != null) {
            return Aspect.getAspect(tag.getStringOr("AspectFilter", ""));
        }
        return null;
    }

    public static void setAspectsStatic(ItemStack itemstack, AspectList aspects) {
        // NEOFORGE_PORT (26.3): merge into the existing CUSTOM_DATA like the reference wrote into
        // the existing root tag (a fresh tag dropped "AspectFilter" when filling a labelled jar).
        net.minecraft.nbt.CompoundTag tag =
                arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(itemstack);
        aspects.writeToNBT(tag);
        arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(itemstack, tag);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getAspects(stack) != null;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) getDurabilityForDisplay(stack) * 13.0F);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null && level.getBlockState(context.getClickedPos()).is(ModBlocks.ALEMBIC.get())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            BlockEntity be = level.getBlockEntity(context.getClickedPos());
            if (be instanceof TileAlembic alembic && alembic.amount > 0) {
                ItemStack held = context.getItemInHand();
                if (getFilter(held) != null && getFilter(held) != alembic.aspect) {
                    return InteractionResult.FAIL;
                }
                AspectList heldAspects = getAspectsStatic(held);
                if (heldAspects != null && heldAspects.getAspects()[0] != alembic.aspect) {
                    return InteractionResult.FAIL;
                }
                int amt = alembic.amount;
                if (heldAspects != null && heldAspects.visSize() + amt > 250) {
                    amt = Math.abs(heldAspects.visSize() - 250);
                }
                if (amt <= 0) {
                    return InteractionResult.FAIL;
                }
                Aspect aspect = alembic.aspect;
                if (alembic.takeFromContainer(alembic.aspect, amt)) {
                    int base = heldAspects == null ? 0 : heldAspects.visSize();
                    if (held.getCount() > 1) {
                        ItemStack jarStack = held.copy();
                        setAspectsStatic(jarStack, new AspectList().add(aspect, base + amt));
                        held.shrink(1);
                        jarStack.setCount(1);
                        if (!player.getInventory().add(jarStack)) {
                            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), jarStack));
                        }
                    } else {
                        setAspectsStatic(held, new AspectList().add(aspect, base + amt));
                    }
                    level.playSound(null, context.getClickedPos(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.25f, 1.0f);
                    player.inventoryMenu.broadcastChanges();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        return super.useOn(context);
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (Level+List) -> (TooltipContext, TooltipDisplay, Consumer<Component>).
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        net.minecraft.nbt.CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("AspectFilter")) {
            Aspect filter = Aspect.getAspect(tag.getStringOr("AspectFilter", ""));
            if (filter != null) {
                tooltip.accept(Component.literal(filter.getName()).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    @Override
    public AspectList getAspects(ItemStack itemstack) {
        return getAspectsStatic(itemstack);
    }

    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        setAspectsStatic(itemstack, aspects);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
}
