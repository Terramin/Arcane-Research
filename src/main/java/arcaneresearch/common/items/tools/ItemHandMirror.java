package arcaneresearch.common.items.tools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.common.menu.HandMirrorMenu;
import arcaneresearch.common.tiles.devices.TileMirror;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class ItemHandMirror extends Item {
    public ItemHandMirror() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }
    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
        if (!mirror.hasTag()) {
            return false;
        }
        CompoundTag tag = mirror.getTag();
        if (tag == null || !tag.contains("linkX")) {
            return false;
        }
        int lx = tag.getInt("linkX");
        int ly = tag.getInt("linkY");
        int lz = tag.getInt("linkZ");
        String dimKey = tag.getString("linkDim");
        ServerLevel targetLevel = null;
        if (level.getServer() != null) {
            for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                if (serverLevel.dimension().location().toString().equals(dimKey)) {
                    targetLevel = serverLevel;
                    break;
                }
            }
        }
        if (targetLevel == null) {
            return false;
        }
        BlockPos targetPos = new BlockPos(lx, ly, lz);
        BlockEntity te = targetLevel.getBlockEntity(targetPos);
        if (te == null || !(te instanceof TileMirror tileMirror)) {
            mirror.setTag(null);
            player.playSound(ModSounds.ZAP.get(), 1.0f, 0.8f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
            return false;
        }
        if (tileMirror.transportDirect(items)) {
            items.setCount(0);
            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.1f, 1.0f);
            return true;
        }
        return false;
    }
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(pos).is(ModBlocks.MIRROR_ITEM.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileMirror) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("linkX", pos.getX());
            tag.putInt("linkY", pos.getY());
            tag.putInt("linkZ", pos.getZ());
            tag.putString("linkDim", level.dimension().location().toString());
            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0f, 2.0f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked"));
            player.inventoryMenu.broadcastChanges();
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("linkX")) {
                int lx = tag.getInt("linkX");
                int ly = tag.getInt("linkY");
                int lz = tag.getInt("linkZ");
                String dimKey = tag.getString("linkDim");
                ServerLevel targetLevel = null;
                if (level.getServer() != null) {
                    for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                        if (serverLevel.dimension().location().toString().equals(dimKey)) {
                            targetLevel = serverLevel;
                            break;
                        }
                    }
                }
                if (targetLevel == null) {
                    return InteractionResultHolder.pass(stack);
                }
                BlockPos targetPos = new BlockPos(lx, ly, lz);
                BlockEntity te = targetLevel.getBlockEntity(targetPos);
                if (te == null || !targetLevel.getBlockState(targetPos).is(ModBlocks.MIRROR_ITEM.get())) {
                    stack.setTag(null);
                    player.playSound(ModSounds.ZAP.get(), 1.0f, 0.8f);
                    player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
                    return InteractionResultHolder.pass(stack);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    final InteractionHand usedHand = hand;
                    NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("container.arcaneresearch.hand_mirror");
                        }
                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player menuPlayer) {
                            return new HandMirrorMenu(containerId, playerInventory, usedHand);
                        }
                    }, (FriendlyByteBuf buf) -> {
                        buf.writeEnum(usedHand);
                    });
                    return InteractionResultHolder.success(stack);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag();
    }
    @Override
    @Nullable
    public CompoundTag getShareTag(ItemStack stack) {
        return stack.getTag();
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("linkX")) {
                int lx = tag.getInt("linkX");
                int ly = tag.getInt("linkY");
                int lz = tag.getInt("linkZ");
                String dim = tag.getString("linkDim");
                tooltip.add(Component.literal(
                                Component.translatable("tc.handmirrorlinkedto").getString()
                                        + " " + lx + "," + ly + "," + lz + " in " + dim)
                        .withStyle(style -> style.withColor(0x808080)));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
