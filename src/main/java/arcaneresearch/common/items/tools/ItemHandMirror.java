package arcaneresearch.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.common.menu.HandMirrorMenu;
import arcaneresearch.common.tiles.devices.TileMirror;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;

import java.util.function.Consumer;

public class ItemHandMirror extends Item {

    public ItemHandMirror(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level level) {
        
        if (!ArcaneresearchInvHelper.hasCustomTag(mirror)) {
            return false;
        }

        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(mirror);
        if (tag == null || !tag.contains("linkX")) {
            return false;
        }

        int lx = tag.getIntOr("linkX", 0);
        int ly = tag.getIntOr("linkY", 0);
        int lz = tag.getIntOr("linkZ", 0);
        String dimKey = tag.getStringOr("linkDim", "");

        ServerLevel targetLevel = null;
        if (level.getServer() != null) {
            for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                if (serverLevel.dimension().identifier().toString().equals(dimKey)) {
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

            
            mirror.remove(DataComponents.CUSTOM_DATA);
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
            
            player.swing(context.getHand(), SwingAnimation.DEFAULT, false);
            return InteractionResult.SUCCESS;
        }

        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileMirror) {
            
            CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            tag.putInt("linkX", pos.getX());
            tag.putInt("linkY", pos.getY());
            tag.putInt("linkZ", pos.getZ());
            tag.putString("linkDim", level.dimension().identifier().toString());
            ArcaneresearchInvHelper.setCustomTag(stack, tag);

            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0f, 2.0f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked"));
            player.inventoryMenu.broadcastChanges();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && ArcaneresearchInvHelper.hasCustomTag(stack)) {
            CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
            if (tag != null && tag.contains("linkX")) {
                int lx = tag.getIntOr("linkX", 0);
                int ly = tag.getIntOr("linkY", 0);
                int lz = tag.getIntOr("linkZ", 0);
                String dimKey = tag.getStringOr("linkDim", "");

                ServerLevel targetLevel = null;
                if (level.getServer() != null) {
                    for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                        if (serverLevel.dimension().identifier().toString().equals(dimKey)) {
                            targetLevel = serverLevel;
                            break;
                        }
                    }
                }

                if (targetLevel == null) {
                    return InteractionResult.PASS;
                }

                BlockPos targetPos = new BlockPos(lx, ly, lz);
                BlockEntity te = targetLevel.getBlockEntity(targetPos);

                if (te == null || !targetLevel.getBlockState(targetPos).is(ModBlocks.MIRROR_ITEM.get())) {

                    
                    stack.remove(DataComponents.CUSTOM_DATA);
                    player.playSound(ModSounds.ZAP.get(), 1.0f, 0.8f);
                    player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
                    return InteractionResult.PASS;
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    final InteractionHand usedHand = hand;
                    
                    
                    
                    serverPlayer.openMenu(new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("container.arcaneresearch.hand_mirror");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player menuPlayer) {
                            return new HandMirrorMenu(containerId, playerInventory, usedHand);
                        }
                    }, buf -> {
                        buf.writeEnum(usedHand);
                    });
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {

        return ArcaneresearchInvHelper.hasCustomTag(stack);
    }

    
    

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("linkX")) {
            int lx = tag.getIntOr("linkX", 0);
            int ly = tag.getIntOr("linkY", 0);
            int lz = tag.getIntOr("linkZ", 0);
            String dim = tag.getStringOr("linkDim", "");

            tooltip.accept(Component.literal(
                            Component.translatable("tc.handmirrorlinkedto").getString()
                                    + " " + lx + "," + ly + "," + lz + " in " + dim)
                    .withStyle(style -> style.withColor(0x808080)));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}