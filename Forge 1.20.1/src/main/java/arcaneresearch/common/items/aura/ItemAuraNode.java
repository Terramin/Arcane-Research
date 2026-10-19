package arcaneresearch.common.items.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player,
                                                 ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        try {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AuraNodeTile node && stack.hasTag()) {
                NodeStackData.applyTo(node, stack.getTag());
                node.syncTile(true);
                return true;
            }
        } catch (Exception ignored) {
        }
        return result;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        NodeStackData.appendTooltip(stack, tooltip);
    }
    @Override
    public AspectList getAspects(ItemStack itemstack) {
        return NodeStackData.readAspects(itemstack);
    }
    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        try {
            aspects.writeToNBT(itemstack.getOrCreateTag(), "Aspects");
        } catch (Exception ignored) {
        }
    }
    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(
            java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private arcaneresearch.client.renderers.item.NodeItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new arcaneresearch.client.renderers.item.NodeItemRenderer(false);
                }
                return renderer;
            }
        });
    }
}
