package arcaneresearch.common.items.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeStackData;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.aura.AuraNodeTile;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAuraNode extends BlockItem implements IEssentiaContainerItem {
    public ItemAuraNode(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack createStack(NodeType type, NodeModifier modifier, AspectList aspects) {
        try {
            return NodeStackData.create(AuraBlocks.AURA_NODE_ITEM.get(), type, modifier, aspects);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack createStack(NodeType type, NodeModifier modifier,
                                        AspectList aspects, AspectList base) {
        try {
            return NodeStackData.create(AuraBlocks.AURA_NODE_ITEM.get(), type, modifier, aspects, base);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    
    
    
    
    
    
    
    @Override
    public void onBlockPlacedBy(BlockPlaceContext context, Level level, BlockPos pos, BlockState state,
                                @Nullable Player placer, ItemStack stack) {
        try {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AuraNodeTile node
                    && arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(stack)) {
                NodeStackData.applyTo(node, arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack));

                node.syncTile(true);
            }
        } catch (Exception ignored) {
        }
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        java.util.List<Component> extra = new java.util.ArrayList<>();
        NodeStackData.appendTooltip(stack, extra);
        extra.forEach(tooltip);
    }

    @Override
    public AspectList getAspects(ItemStack itemstack) {
        return NodeStackData.readAspects(itemstack);
    }

    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        try {
            
            CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> aspects.writeToNBT(tag, "Aspects"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    
    
    
    
}
