package arcaneresearch.common.golems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
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
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.menu.LogisticsMenu;
import arcaneresearch.common.menu.SealMenu;
import arcaneresearch.init.ModSounds;

public class ItemGolemBell extends Item implements ISealDisplayer {

    public ItemGolemBell(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    public static void openSealGui(Player player, ISealEntity sealEntity) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        BlockPos sealBlockPos = sealEntity.getSealPos().pos;
        Direction sealFace = sealEntity.getSealPos().face;
        serverPlayer.openMenu( new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("item.arcaneresearch.seal.blank");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
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
        serverPlayer.openMenu( new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.arcaneresearch.logistics");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                return new LogisticsMenu(containerId, inventory, pos, side, deliverToPlayer);
            }
        }, buf -> {
            buf.writeBlockPos(pos);
            buf.writeByte((byte) side.ordinal());
            buf.writeBoolean(deliverToPlayer);
        });
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
        if (!level.isClientSide()) {
            ISealEntity seal = SealHandler.getSealEntity(level.dimension(),
                    new SealPos(context.getClickedPos(), context.getClickedFace()));
            if (seal != null) {
                if (player.isShiftKeyDown()) {
                    SealHandler.removeSealEntity(level, seal.getSealPos(), false);
                    level.playSound(null, context.getClickedPos(), ModSounds.ZAP.get(),
                            SoundSource.BLOCKS, 0.5f, 1.0f);
                } else {
                    openSealGui(player, seal);
                }
                return InteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()
                    && ArcaneresearchCapabilities.isResearchKnown(player, "GOLEMLOGISTICS")) {
                openLogisticsGui(player, context.getClickedPos(), context.getClickedFace());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        player.swing(context.getHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);

        if (!level.isClientSide()) {
            ISealEntity sealEntity = SealHandler.getSealEntity(level.dimension(), new SealPos(pos, side));

            if (sealEntity != null) {
                if (player.isShiftKeyDown()) {
                    SealHandler.removeSealEntity(level, sealEntity.getSealPos(), false);
                    level.playSound(null, pos, ModSounds.ZAP.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
                } else {
                    openSealGui(player, sealEntity);
                }
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()
                    && ArcaneresearchCapabilities.isResearchKnown(player, "GOLEMLOGISTICS")) {
                openLogisticsGui(player, pos, side);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    // NEOFORGE_PORT: Item.use now returns plain InteractionResult (holder removed);
    // SUCCESS preserves the old success(stack) semantics (both sides, swing already done above).
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);

        if (!level.isClientSide()) {
            ISealEntity sealEntity = getSealFromRaytrace(player);

            if (sealEntity != null) {
                if (player.isShiftKeyDown()) {
                    SealHandler.removeSealEntity(level, sealEntity.getSealPos(), false);
                    level.playSound(null, sealEntity.getSealPos().pos, ModSounds.ZAP.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
                } else {
                    openSealGui(player, sealEntity);
                }
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()
                    && ArcaneresearchCapabilities.isResearchKnown(player, "GOLEMLOGISTICS")) {
                openLogisticsGui(player, player.blockPosition(), Direction.UP, true);
                return InteractionResult.SUCCESS;
            }
        } else {
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f, 1.0f + level.getRandom().nextFloat() * 0.1f);
        }

        return super.use(level, player, hand);
    }

    private ISealEntity getSealFromRaytrace(Player player) {
        float pitch = player.getXRot();
        float yaw = player.getYRot();

        Vec3 eyePos = player.getEyePosition();

        float cosYaw = Mth.cos(-yaw * Mth.DEG_TO_RAD - Mth.PI);
        float sinYaw = Mth.sin(-yaw * Mth.DEG_TO_RAD - Mth.PI);
        float cosPitch = -Mth.cos(-pitch * Mth.DEG_TO_RAD);
        float sinPitch = Mth.sin(-pitch * Mth.DEG_TO_RAD);

        float lookX = sinYaw * cosPitch;
        float lookY = sinPitch;
        float lookZ = cosYaw * cosPitch;

        double range = 5.0;
        Vec3 lookVec = new Vec3(lookX * range, lookY * range, lookZ * range);
        Vec3 endPos = eyePos.add(lookVec);

        Vec3 step = lookVec.scale(0.1);
        Vec3 checkPos = eyePos;

        for (int i = 0; i < (int) (range * 10); i++) {
            BlockHitResult hitResult = player.level().clip(new ClipContext(
                    eyePos, checkPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                ISealEntity sealEntity = SealHandler.getSealEntity(
                        player.level().dimension(),
                        new SealPos(hitResult.getBlockPos(), hitResult.getDirection()));

                if (sealEntity != null) {
                    return sealEntity;
                }
            }

            checkPos = checkPos.add(step);
        }

        return null;
    }
}
