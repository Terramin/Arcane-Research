package arcaneresearch.common.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

    /**
     * Длительность изучения (тики удержания ПКМ). Порт
     * {@code ItemThaumometer.func_77626_a} (TC 4.2.3.5): 25 тиков,
     * срабатывание при остатке &lt;= 5, т.е. ~20 тиков на одной цели.
     */
    private static final int STUDY_DURATION = 25;
    private static final int STUDY_COMPLETE_AT = 5;

    /** Захваченная цель на время удержания, отдельно на сторону (SP: общий класслоадер). */
    private static final java.util.Map<java.util.UUID, String> CLIENT_KEY = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, String> SERVER_KEY = new java.util.HashMap<>();

    public ItemThaumometer(net.minecraft.world.item.Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        // NEOFORGE_PORT: getUseDuration now takes the using entity.
        return STUDY_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // NEOFORGE_PORT: UseAnim -> ItemUseAnimation.
        return ItemUseAnimation.NONE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // NEOFORGE_PORT: InteractionResultHolder removed -> InteractionResult.
        ItemStack stack = player.getItemInHand(hand);
        // Во второй руке прибор не работает: ни скана, ни использования.
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {

            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SCAN.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
            drawFX(level, player);
        }
        // Мгновенного скана больше нет: эффект — только после удержания
        // (см. onUsingTick). Проектное дополнение temp-изучению.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    /**
     * Порт {@code ItemThaumometer.onUsingTick} (TC 4.2.3.5): цель
     * захватывается на первом тике ({@code startScan}), смена цели срывает
     * изучение, при остатке {@code <= 5} — однократный скан сервера.
     */
    @Override
    public void onUseTick(Level tickLevel, LivingEntity entity, ItemStack stack, int count) {
        if (!(entity instanceof Player player)) {
            return;
        }
        // Страховка: изучение из второй руки (перекладка на ходу) — стопаем.
        if (player.getOffhandItem() == stack) {
            player.stopUsingItem();
            return;
        }
        java.util.Map<java.util.UUID, String> keys =
                tickLevel.isClientSide() ? CLIENT_KEY : SERVER_KEY;
        String key = steadyKey(tickLevel, player);
        if (count >= STUDY_DURATION) {
            // Первый тик: захват цели. Пустота — сразу отмена.
            if (key.isEmpty()) {
                player.stopUsingItem();
                return;
            }
            keys.put(player.getUUID(), key);
            return;
        }
        String held = keys.get(player.getUUID());
        if (held == null || !held.equals(key)) {
            // Паритет startScan=null: цель сменилась — изучение сорвано.
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
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // NEOFORGE_PORT: void -> boolean in 26.x (true = захват цели сброшен).
        if (entity instanceof Player player) {
            CLIENT_KEY.remove(player.getUUID());
            SERVER_KEY.remove(player.getUUID());
            return true;
        }
        return false;
    }

    /** Ключ стабильности цели (сущность важнее блока). */
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
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) return;

        // NEOFORGE_PORT: Forge passed (isSelected, slotId) and treated slotId == 0 as held;
        // the 26.x hook passes an EquipmentSlot instead, so "held" = the stack is in a hand.
        boolean held = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

        if (!held) return;

        if (entity.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            updateAura(stack, level, serverPlayer);
        }

        // NEOFORGE_PORT: Forge also ran highlightScannables client-side here every
        // 5 ticks, but Item.inventoryTick is server-only in 26.x. The client phase
        // drives it instead (ClientEvents.onClientTickPre -> clientTick below),
        // same held check + same 5-tick gate as Forge.
    }

    // NEOFORGE_PORT: client-phase driver for the Forge inventoryTick client branch.
    // Called every client tick; runs highlightScannables when a thaumometer is held
    // and player.tickCount % 5 == 0 (Forge 1:1). Minecraft access stays in client
    // classes (ClientEvents); this method itself only takes level/player.
    // NEOFORGE_PORT: parameter is Level, not ClientLevel: 26.x no longer strips @OnlyIn
    // members at runtime, so a client-only type in a common class signature makes the
    // dedicated server fail to link this class (NoClassDefFoundError: ClientLevel from
    // ModItems.<clinit>). The body only uses Level methods; the caller (ClientEvents,
    // client-only) passes mc.level.
    public static void clientTick(Level level, Player player) {
        if (player.tickCount % 5 != 0) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (main.getItem() instanceof ItemThaumometer || off.getItem() instanceof ItemThaumometer) {
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
                // NEOFORGE_PORT: Player.displayClientMessage REMOVED -> sendOverlayMessage (action bar).
                player.sendOverlayMessage(
                        Component.translatable("research.FLUX.warn").withStyle(ChatFormatting.DARK_PURPLE));
            }
            // NEOFORGE_PORT: PacketAuraToClient ported + registered (1:1 ctor).
            PacketHandler.sendToPlayer(new PacketAuraToClient(ac), player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawFX(Level level, Player player) {
        Entity target = getTargetEntity(level, player, SCAN_RANGE, 0.0f);
        if (target != null) {
            for (int a = 0; a < 10; ++a) {
                FXDispatcher.INSTANCE.blockRunes(target.getX() - 0.5, target.getY() + target.getEyeHeight() / 2.0f,
                        target.getZ() - 0.5, 0.3f + level.getRandom().nextFloat() * 0.7f, 0.0f,
                        0.3f + level.getRandom().nextFloat() * 0.7f, (int) (target.getBbHeight() * 15.0f), 0.03f);
            }
        } else {
            BlockHitResult mop = getTargetBlock(level, player, SCAN_RANGE);
            if (mop.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = mop.getBlockPos();
                for (int a = 0; a < 10; ++a) {
                    FXDispatcher.INSTANCE.blockRunes(pos.getX(), pos.getY() + 0.25, pos.getZ(),
                            0.3f + level.getRandom().nextFloat() * 0.7f, 0.0f,
                            0.3f + level.getRandom().nextFloat() * 0.7f, 15, 0.03f);
                }
            }
        }
    }

    // NEOFORGE_PORT: static so the client tick driver (clientTick above, called
    // from ClientEvents) can reach it; body is Forge 1:1 (no instance state used).
    @OnlyIn(Dist.CLIENT)
    public static void highlightScannables(Level level, Player player) {
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

    private static BlockHitResult getWildBlockHit(Level level, Player player) {
        float pitch = player.getXRot() + level.getRandom().nextInt(25) - level.getRandom().nextInt(25);
        float yaw = player.getYRot() + level.getRandom().nextInt(25) - level.getRandom().nextInt(25);
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        Vec3 targetPos = eyePos.add(look.scale(HIGHLIGHT_RANGE));
        return level.clip(new ClipContext(eyePos, targetPos,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }

    private static Entity getTargetEntity(Level level, Player player, double range, float padding) {
        return EntityUtils.getPointedEntity(level, player, 1.0, range, padding, true);
    }

    private static BlockHitResult getTargetBlock(Level level, Player player, double range) {
        return EntityUtils.blockRay(level, player, range);
    }
}
