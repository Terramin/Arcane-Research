package arcaneresearch.common.golems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.menu.LogisticsMenu;
import arcaneresearch.common.menu.SealMenu;
import arcaneresearch.init.ModSounds;
import org.jetbrains.annotations.NotNull;

public class ItemGolemBell extends Item implements ISealDisplayer {

    private static final double SEAL_RAYTRACE_RANGE = 5.0;
    private static final Direction[] HORIZONTAL_AND_VERTICAL = Direction.values();

    public ItemGolemBell() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static void openSealGui(Player player, ISealEntity sealEntity) {
        if (!(player instanceof ServerPlayer serverPlayer) || sealEntity == null) {
            return;
        }
        SealPos sealPos = sealEntity.getSealPos();
        if (sealPos == null) {
            return;
        }
        BlockPos sealBlockPos = sealPos.pos;
        Direction sealFace = sealPos.face;
        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("item.arcaneresearch.seal.blank");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player p) {
                return new SealMenu(containerId, inventory, sealEntity);
            }
        }, buf -> {
            buf.writeBlockPos(sealBlockPos);
            buf.writeByte((byte) sealFace.ordinal());
        });
    }

    public static void openLogisticsGui(Player player, BlockPos pos, Direction side) {
        openLogisticsGui(player, pos, side, false);
    }

    public static void openLogisticsGui(Player player, BlockPos pos, Direction side, boolean deliverToPlayer) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("gui.arcaneresearch.logistics");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player p) {
                return new LogisticsMenu(containerId, inventory, pos, side, deliverToPlayer);
            }
        }, buf -> {
            buf.writeBlockPos(pos);
            buf.writeByte((byte) side.ordinal());
            buf.writeBoolean(deliverToPlayer);
        });
    }

    private static InteractionResult handleSealClick(Player player, Level level, BlockPos pos, Direction side) {
        ISealEntity seal = SealHandler.getSealEntity(level.dimension(), new SealPos(pos, side));
        if (seal == null && player.isShiftKeyDown()) {
            seal = findLoneSealOnBlock(level, pos, side);
        }
        if (seal != null) {
            if (player.isShiftKeyDown()) {
                SealHandler.removeSealEntity(level, seal.getSealPos(), false);
                level.playSound(null, pos, ModSounds.ZAP.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            } else {
                openSealGui(player, seal);
            }
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()
                && ArcaneresearchCapabilities.isResearchKnown(player, "GOLEMLOGISTICS")) {
            openLogisticsGui(player, pos, side);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static ISealEntity findLoneSealOnBlock(Level level, BlockPos pos, Direction clickedFace) {
        ISealEntity found = null;
        for (Direction d : HORIZONTAL_AND_VERTICAL) {
            if (d == clickedFace) {
                continue;
            }
            ISealEntity s = SealHandler.getSealEntity(level.dimension(), new SealPos(pos, d));
            if (s != null) {
                if (found != null) {
                    return null;
                }
                found = s;
            }
        }
        return found;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand());
        if (!context.getLevel().isClientSide()) {
            handleSealClick(player, context.getLevel(), context.getClickedPos(), context.getClickedFace());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.swing(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        ISealEntity sealEntity = getSealFromRaytrace(player);
        if (sealEntity != null) {
            BlockPos pos = sealEntity.getSealPos().pos;
            Direction face = sealEntity.getSealPos().face;
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    0.6f, 1.0f + level.random.nextFloat() * 0.1f);
            handleSealClick(player, level, pos, face);
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()
                && ArcaneresearchCapabilities.isResearchKnown(player, "GOLEMLOGISTICS")) {
            openLogisticsGui(player, player.blockPosition(), Direction.UP, true);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private ISealEntity getSealFromRaytrace(Player player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 endPos = eyePos.add(player.getLookAngle().scale(SEAL_RAYTRACE_RANGE));
        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return SealHandler.getSealEntity(
                    level.dimension(),
                    new SealPos(hitResult.getBlockPos(), hitResult.getDirection()));
        }
        return null;
    }
}