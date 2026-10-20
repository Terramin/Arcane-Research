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
import arcaneresearch.common.tiles.aura.TileJarNode;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJarNode extends BlockItem implements IEssentiaContainerItem {
    public ItemJarNode(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack createStack(NodeType type, NodeModifier modifier, AspectList aspects) {
        try {
            return NodeStackData.create(AuraBlocks.JAR_NODE_ITEM.get(), type, modifier, aspects);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack createStack(NodeType type, NodeModifier modifier,
                                        AspectList aspects, AspectList base) {
        try {
            return NodeStackData.create(AuraBlocks.JAR_NODE_ITEM.get(), type, modifier, aspects, base);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack createStack(TileJarNode jar) {
        try {
            return NodeStackData.create(AuraBlocks.JAR_NODE_ITEM.get(),
                    jar.getNodeType(), jar.getNodeModifier(),
                    jar.getAspects().copy(), jar.getAspectsBase().copy(), jar.getId());
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    // NEOFORGE_PORT: BlockItem.updateCustomBlockEntityTag is now a static helper
    // (Level, Player, pos, stack); custom post-place tile init moves to the NeoForge
    // BlockItem.onBlockPlacedBy callback.
    // NEOFORGE_PORT (26.3): it must NOT run after super.place(): place() ends with
    // itemStack.consume(1, player), which empties a 1-count survival stack, and an empty
    // stack reports no CUSTOM_DATA -> the node data was dropped. onBlockPlacedBy runs on
    // both sides before consume, i.e. at the same point as Forge updateCustomBlockEntityTag.
    @Override
    public void onBlockPlacedBy(BlockPlaceContext context, Level level, BlockPos pos, BlockState state,
                                @Nullable Player placer, ItemStack stack) {
        try {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileJarNode jar
                    && arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(stack)) {
                NodeStackData.applyTo(jar, arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack));

                jar.syncTile(true);
            }
        } catch (Exception ignored) {
        }
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (Level+List) -> (TooltipContext, TooltipDisplay, Consumer<Component>).
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
            // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
            CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> aspects.writeToNBT(tag, "Aspects"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    // NEOFORGE_PORT (26.3): Item.initializeClient(Consumer<IClientItemExtensions>) was removed
    // in 26.x (the old no-op kept a client-only type in this common class). The Forge BEWLR
    // is done via client/renderers/item/NodeSpecialRenderer (arcaneresearch:node), registered
    // in SpecialItemModels and wired in assets/arcaneresearch/items/{aura_node,jar_node}.json.
}
