package arcaneresearch.common.items.tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.devices.TileCondenser;
public class ItemResonator extends Item {
    public ItemResonator() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }
    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag();
    }
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile == null || !(tile instanceof IEssentiaTransport)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            if (player != null) {
                player.swing(hand);
            }
            return InteractionResult.SUCCESS;
        }
        if (player == null) {
            return InteractionResult.FAIL;
        }
        IEssentiaTransport et = (IEssentiaTransport) tile;
        if (!(tile instanceof arcaneresearch.common.tiles.essentia.TileTubeBuffer) && et.getEssentiaType(side) != null) {
            player.displayClientMessage(Component.translatable("tc.resonator1",
                    String.valueOf(et.getEssentiaAmount(side)), et.getEssentiaType(side).getName()), false);
        } else if (tile instanceof arcaneresearch.common.tiles.essentia.TileTubeBuffer buffer
                && buffer.getAspects().size() > 0) {
            for (Aspect aspect : buffer.getAspects().getAspectsSortedByName()) {
                player.displayClientMessage(Component.translatable("tc.resonator1",
                        String.valueOf(buffer.getAspects().getAmount(aspect)), aspect.getName()), false);
            }
        }
        String suctionName = Component.translatable("tc.resonator3").getString();
        if (et.getSuctionType(side) != null) {
            suctionName = et.getSuctionType(side).getName();
        }
        player.displayClientMessage(Component.translatable("tc.resonator2",
                String.valueOf(et.getSuctionAmount(side)), suctionName), false);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS, 0.5f, 1.9f + level.random.nextFloat() * 0.1f);
        if (tile instanceof TileCondenser condenser) {
            player.displayClientMessage(Component.translatable("tc.condenser1",
                    String.valueOf(condenser.cost)), false);
            int seconds = condenser.interval / 20;
            player.displayClientMessage(Component.translatable("tc.condenser2",
                    String.valueOf(condenser.interval), String.valueOf(seconds)), false);
        }
        return InteractionResult.SUCCESS;
    }
}
