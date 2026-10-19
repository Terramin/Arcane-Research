package arcaneresearch.common.items.tools;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.renderers.ScanPopupRenderer;
import arcaneresearch.common.items.ItemTC;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketAuraToClient;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.world.aura.AuraChunk;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModSounds;
public class ItemThaumometer extends ItemTC {
    private static final double SCAN_RANGE = 9.0;
    private static final double HIGHLIGHT_RANGE = 16.0;
    private static final int STUDY_DURATION = 25;
    private static final int STUDY_COMPLETE_AT = 5;
    private static final java.util.Map<java.util.UUID, String> CLIENT_KEY = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, String> SERVER_KEY = new java.util.HashMap<>();
    public ItemThaumometer() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return STUDY_DURATION;
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SCAN.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
            drawFX(level, player);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override
    public void onUseTick(Level tickLevel, LivingEntity entity, ItemStack stack, int count) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.getOffhandItem() == stack) {
            player.stopUsingItem();
            return;
        }
        java.util.Map<java.util.UUID, String> keys =
                tickLevel.isClientSide() ? CLIENT_KEY : SERVER_KEY;
        String key = steadyKey(tickLevel, player);
        if (count >= STUDY_DURATION) {
            if (key.isEmpty()) {
                player.stopUsingItem();
                return;
            }
            keys.put(player.getUUID(), key);
            return;
        }
        String held = keys.get(player.getUUID());
        if (held == null || !held.equals(key)) {
            keys.remove(player.getUUID());
            player.stopUsingItem();
            return;
        }
        if (count <= STUDY_COMPLETE_AT) {
            keys.remove(player.getUUID());
            if (!tickLevel.isClientSide()) {
                doScan(tickLevel, player);
            }
            player.stopUsingItem();
        }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            CLIENT_KEY.remove(player.getUUID());
            SERVER_KEY.remove(player.getUUID());
        }
    }
    private String steadyKey(Level level, Player player) {
        Entity target = getTargetEntity(level, player, SCAN_RANGE, 0.0f);
        if (target != null) {
            return "E:" + target.getId();
        }
        BlockHitResult hit = getTargetBlock(level, player, SCAN_RANGE);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            return "B:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        }
        return "";
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) return;
        boolean held = isSelected || slotId == 0;
        if (!held) return;
        if (!level.isClientSide() && entity.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            updateAura(stack, level, serverPlayer);
        }
        if (level.isClientSide() && entity.tickCount % 5 == 0) {
            highlightScannables(level, player);
        }
    }
    private void doScan(Level level, Player player) {
        Entity targetEntity = getTargetEntity(level, player, SCAN_RANGE, 0.0f);
        if (targetEntity != null) {
            ScanningManager.scanTheThing(player, targetEntity);
            return;
        }
        BlockHitResult blockHit = getTargetBlock(level, player, SCAN_RANGE);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            ScanningManager.scanTheThing(player, blockHit.getBlockPos());
            return;
        }
        ScanningManager.scanTheThing(player, (BlockPos) null);
    }
    private void updateAura(ItemStack stack, Level level, ServerPlayer player) {
        AuraChunk ac = AuraHandler.getAuraChunk(level.dimension(),
                player.blockPosition().getX() >> 4, player.blockPosition().getZ() >> 4);
        if (ac != null) {
            if ((ac.getFlux() > ac.getVis() || ac.getFlux() > ac.getBase() / 3)
                    && !ArcaneresearchCapabilities.knowsResearch(player, "FLUX")) {
                ResearchManager.startResearchWithPopup(player, "FLUX");
                player.displayClientMessage(
                        Component.translatable("research.FLUX.warn").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            PacketHandler.sendToPlayer(new PacketAuraToClient(ac), player);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void drawFX(Level level, Player player) {
        Entity target = getTargetEntity(level, player, SCAN_RANGE, 0.0f);
        if (target != null) {
            for (int a = 0; a < 10; ++a) {
                FXDispatcher.INSTANCE.blockRunes(target.getX() - 0.5, target.getY() + target.getEyeHeight() / 2.0f,
                        target.getZ() - 0.5, 0.3f + level.random.nextFloat() * 0.7f, 0.0f,
                        0.3f + level.random.nextFloat() * 0.7f, (int) (target.getBbHeight() * 15.0f), 0.03f);
            }
        } else {
            BlockHitResult mop = getTargetBlock(level, player, SCAN_RANGE);
            if (mop.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = mop.getBlockPos();
                for (int a = 0; a < 10; ++a) {
                    FXDispatcher.INSTANCE.blockRunes(pos.getX(), pos.getY() + 0.25, pos.getZ(),
                            0.3f + level.random.nextFloat() * 0.7f, 0.0f,
                            0.3f + level.random.nextFloat() * 0.7f, 15, 0.03f);
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void highlightScannables(Level level, Player player) {
        Entity target = getTargetEntity(level, player, HIGHLIGHT_RANGE, 5.0f);
        if (target != null && ScanningManager.isThingStillScannable(player, target)) {
            FXDispatcher.INSTANCE.scanHighlight(target);
        }
        ScanPopupRenderer.thaumTarget = target;
        BlockHitResult mop = getWildBlockHit(level, player);
        if (mop.getType() == HitResult.Type.BLOCK
                && ScanningManager.isThingStillScannable(player, mop.getBlockPos())) {
            FXDispatcher.INSTANCE.scanHighlight(mop.getBlockPos());
        }
        BlockHitResult precise = getTargetBlock(level, player, HIGHLIGHT_RANGE);
        if (precise.getType() == HitResult.Type.BLOCK) {
            ScanPopupRenderer.thaumBlockTarget = precise.getBlockPos();
        } else {
            ScanPopupRenderer.thaumBlockTarget = null;
        }
    }
    private BlockHitResult getWildBlockHit(Level level, Player player) {
        float pitch = player.getXRot() + level.random.nextInt(25) - level.random.nextInt(25);
        float yaw = player.getYRot() + level.random.nextInt(25) - level.random.nextInt(25);
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        Vec3 targetPos = eyePos.add(look.scale(HIGHLIGHT_RANGE));
        return level.clip(new ClipContext(eyePos, targetPos,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }
    private Entity getTargetEntity(Level level, Player player, double range, float padding) {
        return EntityUtils.getPointedEntity(level, player, 1.0, range, padding, true);
    }
    private BlockHitResult getTargetBlock(Level level, Player player, double range) {
        return EntityUtils.blockRay(level, player, range);
    }
}
