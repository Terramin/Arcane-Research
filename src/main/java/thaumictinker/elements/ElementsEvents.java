package thaumictinker.elements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.net.TtNet;

 



 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementsEvents {

    private static final int AIR_DURATION = 20;
    private static final int FIRE_DURATION = 6000;
    private static final int PULSE_PERIOD = 5;
    private static final int FIRE_SECONDS = 6;
    private static final double FX_RANGE = 64.0;

    private static final Map<Entity, Long> AIR_HITS = new HashMap<>();
    private static final Map<Entity, Long> FIRE_HITS = new HashMap<>();

    private ElementsEvents() {
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingAttackEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player) {
            hit(player, event.getEntity());
        }
    }

    public static void hit(Player player, LivingEntity target) {
        Level level = player.level();
        if (level.isClientSide) {
            return;
        }
        if (player.hasEffect(ElementsFeature.AIR_IMBUED)) {
            AIR_HITS.put(target, level.getGameTime());
        }
        if (player.hasEffect(ElementsFeature.FIRE_IMBUED)) {
            FIRE_HITS.put(target, level.getGameTime());
        }
        if (player.hasEffect(ElementsFeature.EARTH_IMBUED)) {
            buildWall(level, player, target);
        }
    }

    public static void buildWall(Level level, Player player, LivingEntity target) {
        boolean alongX = Math.abs(target.getZ() - player.getZ()) < Math.abs(target.getX() - player.getX());
        int centerX = Mth.floor((target.getX() + player.getX()) / 2);
        int centerY = Mth.floor(player.getY() + 2);
        int centerZ = Mth.floor((target.getZ() + player.getZ()) / 2);
        BlockState forcefield = ElementsFeature.FORCEFIELD.defaultBlockState();
        for (int vertical = -2; vertical < 3; vertical++) {
            for (int horizontal = -2; horizontal < 3; horizontal++) {
                BlockPos pos = alongX
                        ? new BlockPos(centerX, centerY + vertical, centerZ + horizontal)
                        : new BlockPos(centerX + horizontal, centerY + vertical, centerZ);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlockAndUpdate(pos, forcefield);
                    TtNet.sendToNear(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, FX_RANGE,
                            TtNet.elementFx(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0.0F));
                }
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !event.player.hasEffect(ElementsFeature.WATER_IMBUED)) {
            return;
        }
        solidifyLava(event.player.level(), event.player);
    }

    public static void solidifyLava(Level level, Player player) {
        for (int x = Mth.floor(player.getX() - 2); x < player.getX() + 2; x++) {
            for (int y = Mth.floor(player.getY() - 2); y < player.getY() + 2; y++) {
                for (int z = Mth.floor(player.getZ() - 2); z < player.getZ() + 2; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                        TtNet.sendToNear(level, x + 0.5, y + 0.5, z + 0.5, FX_RANGE,
                                TtNet.elementFx(x + 0.5, y + 0.5, z + 0.5, 1, 1.2F));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        pulseAir();
        pulseFire();
    }

    private static void pulseAir() {
        Iterator<Map.Entry<Entity, Long>> iterator = AIR_HITS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Long> entry = iterator.next();
            Entity target = entry.getKey();
            long time = target.level().getGameTime();
            if (target.isAlive() && time % PULSE_PERIOD == 0) {
                RandomSource random = target.level().getRandom();
                target.setDeltaMovement(random.nextFloat() - 0.5, random.nextFloat(), random.nextFloat() - 0.5);
                target.hasImpulse = true;
                TtNet.sendToNear(target.level(), target.getX(), target.getY(), target.getZ(), FX_RANGE,
                        TtNet.elementFx(target.getX(), target.getY(), target.getZ(), 1, 0.5F));
            }
            if (target.isRemoved() || time > entry.getValue() + AIR_DURATION) {
                iterator.remove();
            }
        }
    }

    private static void pulseFire() {
        Iterator<Map.Entry<Entity, Long>> iterator = FIRE_HITS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Long> entry = iterator.next();
            Entity target = entry.getKey();
            long time = target.level().getGameTime();
            if (target.isAlive() && time % PULSE_PERIOD == 0) {
                target.setSecondsOnFire(FIRE_SECONDS);
                TtNet.sendToNear(target.level(), target.getX(), target.getY(), target.getZ(), FX_RANGE,
                        TtNet.elementFx(target.getX(), target.getY(), target.getZ(), 2, 0.0F));
            }
            if (target.isRemoved() || time > entry.getValue() + FIRE_DURATION) {
                iterator.remove();
            }
        }
    }

    public static boolean isAirHit(Entity entity) {
        return AIR_HITS.containsKey(entity);
    }

    public static boolean isFireHit(Entity entity) {
        return FIRE_HITS.containsKey(entity);
    }

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        FireMaps.reset();
        ElementsFeature.ORDER_TRANSFORMATION.reset();
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        AIR_HITS.clear();
        FIRE_HITS.clear();
    }
}
