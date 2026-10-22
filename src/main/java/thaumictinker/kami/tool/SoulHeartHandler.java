package thaumictinker.kami.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.net.TtNet;

 




 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class SoulHeartHandler {

    public static final int MAX_HP = 20;
    private static final String KEY = "thaumictinker_soul_hearts";

      
    private static int clientHP;

    private SoulHeartHandler() {
    }

    public static int getHP(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(KEY) ? data.getInt(KEY) : 0;
    }

    public static int getClientHP() {
        return clientHP;
    }

    public static void applyClientHearts(int hearts) {
        clientHP = Math.max(0, Math.min(MAX_HP, hearts));
    }

    public static void setHP(Player player, int hp) {
        int clamped = Math.max(0, Math.min(MAX_HP, hp));
        if (clamped != getHP(player)) {
            player.getPersistentData().putInt(KEY, clamped);
            if (player instanceof ServerPlayer serverPlayer) {
                TtNet.sendSoulHearts(serverPlayer, clamped);
            }
        }
    }

    public static void addHearts(Player player) {
        addHP(player, 1);
    }

    public static boolean addHP(Player player, int hp) {
        int current = getHP(player);
        if (current >= MAX_HP) {
            return false;
        }
        setHP(player, current + hp);
        return true;
    }

    public static int removeHP(Player player, int hp) {
        int remaining = getHP(player) - hp;
        setHP(player, remaining);
        return Math.max(0, -remaining);
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        float damage = event.getAmount();
        int absorbable = Math.min(getHP(player), (int) damage);
        if (damage <= 0.0F || absorbable <= 0) {
            return;
        }
        removeHP(player, absorbable);
        event.setAmount(damage - absorbable);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TtNet.sendSoulHearts(player, getHP(player));
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TtNet.sendSoulHearts(player, getHP(player));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TtNet.sendSoulHearts(player, getHP(player));
        }
    }

      
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().remove(KEY);
            TtNet.sendSoulHearts(player, 0);
        }
    }
}
