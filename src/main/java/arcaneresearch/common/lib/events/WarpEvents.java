package arcaneresearch.common.lib.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.entities.monster.EntityMindSpider;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketMiscEvent;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModEntities;

import java.util.List;

public class WarpEvents {

    public static void checkWarpEvent(Player player) {
        if (player.level().isClientSide()) return;

        IPlayerWarp wc = ArcaneresearchCapabilities.getWarp(player);
        if (wc == null) return;

        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, -1, IPlayerWarp.EnumWarpType.TEMPORARY);

        int tw = wc.get(IPlayerWarp.EnumWarpType.TEMPORARY);
        int nw = wc.get(IPlayerWarp.EnumWarpType.NORMAL);
        int pw = wc.get(IPlayerWarp.EnumWarpType.PERMANENT);
        int warp = tw + nw + pw;
        int actualwarp = pw + nw;
        int gearWarp = getWarpFromGear(player);
        warp += gearWarp;

        int warpCounter = wc.getCounter();
        int r = player.level().getRandom().nextInt(100);

        if (warpCounter > 0 && warp > 0 && r <= Math.sqrt(warpCounter)) {
            warp = Math.min(100, (warp + warp + warpCounter) / 3);
            warpCounter -= (int) Math.max(5.0, Math.sqrt(warpCounter) * 2.0 - gearWarp * 2);
            wc.setCounter(warpCounter);

            int eff = player.level().getRandom().nextInt(warp) + gearWarp;

            net.minecraft.world.item.ItemStack helm = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            if (helm.getItem() instanceof arcaneresearch.common.items.armor.ItemFortressArmor
                    && arcaneresearch.common.items.armor.ItemFortressArmor.hasMask(helm)
                    && arcaneresearch.common.items.armor.ItemFortressArmor.getMaskType(helm) == 0) {
                eff -= 2 + player.level().getRandom().nextInt(4);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandler.sendToPlayer(new PacketMiscEvent(PacketMiscEvent.WARP_EVENT), serverPlayer);
            }

            if (eff > 0) {
                triggerWarpEffect(player, eff, warp, nw);
            }

            if (actualwarp > 10 && !ArcaneresearchCapabilities.knowsResearch(player, "BATHSALTS")
                    && !ArcaneresearchCapabilities.knowsResearch(player, "!BATHSALTS")) {
                player.sendOverlayMessage(getWarpText(8));
                ArcaneresearchApi.internalMethods.completeResearch(player, "!BATHSALTS");
            }
            if (actualwarp > 25 && !ArcaneresearchCapabilities.knowsResearch(player, "ELDRITCHMINOR")) {
                ArcaneresearchApi.internalMethods.completeResearch(player, "ELDRITCHMINOR");
            }
            if (actualwarp > 50 && !ArcaneresearchCapabilities.knowsResearch(player, "ELDRITCHMAJOR")) {
                ArcaneresearchApi.internalMethods.completeResearch(player, "ELDRITCHMAJOR");
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            wc.sync(serverPlayer);
        }
    }

    private static void triggerWarpEffect(Player player, int eff, int warp, int nw) {
        Level level = player.level();

        if (eff <= 4) {

            if (!arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.nostress) {
                level.playSound(null, player.blockPosition(), SoundEvents.CREEPER_PRIMED,
                        SoundSource.AMBIENT, 1.0f, 0.5f);
            }
        } else if (eff <= 8) {

            if (!arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.nostress) {
                double rx = player.getX() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 10.0f;
                double ry = player.getY() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 10.0f;
                double rz = player.getZ() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 10.0f;
                level.playSound(null, rx, ry, rz, SoundEvents.GENERIC_EXPLODE,
                        SoundSource.AMBIENT, 4.0f, (1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f) * 0.7f);
            }
        } else if (eff <= 12) {

            player.sendOverlayMessage(getWarpText(11));
        } else if (eff <= 16) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.VIS_EXHAUST, 5000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(1));
        } else if (eff <= 20) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.THAUMARHIA, Math.min(32000, 10 * warp), 0, true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(15));
        } else if (eff <= 24) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, 5000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(2));
        } else if (eff <= 28) {

            player.sendOverlayMessage(getWarpText(12));
        } else if (eff <= 32) {

            spawnMist(player, warp, 1);
        } else if (eff <= 36) {

            try {
                player.addEffect(new MobEffectInstance(ModEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0, true, true));
            } catch (Exception ignored) {
            }
        } else if (eff <= 40) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.SUN_SCORNED, 5000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(5));
        } else if (eff <= 44) {

            try {
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 1200, Math.min(3, warp / 15), true, true));
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(9));
        } else if (eff <= 48) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.INFECTIOUS_VIS_EXHAUST, 6000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(1));
        } else if (eff <= 52) {

            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true, true));
            player.sendOverlayMessage(getWarpText(10));
        } else if (eff <= 56) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.DEATH_GAZE, 6000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(4));
        } else if (eff <= 60) {

            suddenlySpiders(player, warp, false);
        } else if (eff <= 64) {

            player.sendOverlayMessage(getWarpText(13));
        } else if (eff <= 68) {

            spawnMist(player, warp, warp / 30);
        } else if (eff <= 72) {

            try {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, true, true));
            } catch (Exception ignored) {
            }
        } else if (eff == 76) {

            if (nw > 0) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, -1, IPlayerWarp.EnumWarpType.NORMAL);
            }
            player.sendOverlayMessage(getWarpText(14));
        } else if (eff <= 80) {

            MobEffectInstance pe = new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, 6000, Math.min(3, warp / 15), true, true);
            try {
                player.addEffect(pe);
            } catch (Exception ignored) {
            }
            player.sendOverlayMessage(getWarpText(2));
        } else if (eff <= 88) {

            spawnPortal(player);
        } else if (eff <= 92) {

            suddenlySpiders(player, warp, true);
        } else {

            spawnMist(player, warp, warp / 15);
        }
    }

    private static void spawnMist(Player player, int warp, int guardian) {

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new PacketMiscEvent(PacketMiscEvent.MIST_EVENT), serverPlayer);
        }

        if (guardian > 0) {
            guardian = Math.min(8, guardian);
            for (int a = 0; a < guardian; ++a) {
                spawnGuardian(player);
            }
        }
        player.sendOverlayMessage(getWarpText(6));
    }

    private static void spawnPortal(Player player) {
        Level level = player.level();
        EntityCultistPortalLesser portal = new EntityCultistPortalLesser(ModEntities.CULTIST_PORTAL_LESSER.get(), level);

        int i = Mth.floor(player.getX());
        int j = Mth.floor(player.getY());
        int k = Mth.floor(player.getZ());

        for (int l = 0; l < 50; ++l) {
            int i2 = i + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
            int j2 = j + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
            int k2 = k + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);

            BlockPos pos = new BlockPos(i2, j2, k2);
            portal.setPos(i2 + 0.5, j2 + 1.0, k2 + 0.5);

            if (level.getBlockState(pos.below()).isSolidRender()
                    && level.noCollision(portal)
                    && !level.containsAnyLiquid(portal.getBoundingBox())) {
                portal.finalizeSpawn((net.minecraft.server.level.ServerLevel) level, ((net.minecraft.world.level.ServerLevelAccessor) level).getCurrentDifficultyAt(pos), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, null);
                level.addFreshEntity(portal);
                player.sendOverlayMessage(getWarpText(16));
                break;
            }
        }
    }

    private static void spawnGuardian(Player player) {
        Level level = player.level();
        EntityEldritchGuardian guardian = new EntityEldritchGuardian(ModEntities.ELDRITCH_GUARDIAN.get(), level);

        int i = Mth.floor(player.getX());
        int j = Mth.floor(player.getY());
        int k = Mth.floor(player.getZ());

        for (int l = 0; l < 50; ++l) {
            int i2 = i + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
            int j2 = j + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
            int k2 = k + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);

            BlockPos pos = new BlockPos(i2, j2, k2);

            if (level.getBlockState(pos.below()).isSolidRender()) {
                guardian.setPos(i2, j2, k2);

                if (level.noCollision(guardian)
                        && !level.containsAnyLiquid(guardian.getBoundingBox())) {
                    guardian.setTarget(player);
                    level.addFreshEntity(guardian);
                    break;
                }
            }
        }
    }

    private static void suddenlySpiders(Player player, int warp, boolean real) {
        Level level = player.level();
        int spawns = Math.min(50, warp);

        for (int a = 0; a < spawns; ++a) {
            EntityMindSpider spider = new EntityMindSpider(ModEntities.MIND_SPIDER.get(), level);

            int i = Mth.floor(player.getX());
            int j = Mth.floor(player.getY());
            int k = Mth.floor(player.getZ());
            boolean success = false;

            for (int l = 0; l < 50; ++l) {
                int i2 = i + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
                int j2 = j + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);
                int k2 = k + Mth.nextInt(level.getRandom(), 7, 24) * Mth.nextInt(level.getRandom(), -1, 1);

                BlockPos pos = new BlockPos(i2, j2, k2);

                if (level.getBlockState(pos.below()).isSolidRender()) {
                    spider.setPos(i2, j2, k2);

                    if (level.noCollision(spider)
                            && !level.containsAnyLiquid(spider.getBoundingBox())) {
                        success = true;
                        break;
                    }
                }
            }

            if (success) {
                spider.setTarget(player);
                if (!real) {
                    spider.setViewer(player.getName().getString());
                    spider.setHarmless(true);
                }
                level.addFreshEntity(spider);
            }
        }
        player.sendOverlayMessage(getWarpText(7));
    }

    public static void checkDeathGaze(Player player) {
        if (player.level().isClientSide()) return;

        MobEffectInstance effect = player.getEffect(ModEffects.DEATH_GAZE);
        if (effect == null) return;

        int amplifier = effect.getAmplifier();
        int range = Math.min(8 + amplifier * 3, 24);

        Level level = player.level();
        AABB searchBox = player.getBoundingBox().inflate(range, range, range);
        List<Entity> entities = level.getEntities(player, searchBox);

        Vec3 playerLook = player.getLookAngle();
        Vec3 playerEyes = player.getEyePosition();

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!entity.isAlive()) continue;
            if (living.hasEffect(MobEffects.WITHER)) continue;

            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(playerEyes).normalize();
            double dot = playerLook.dot(toEntity);
            if (dot < 0.75) continue;

            if (entity instanceof Player targetPlayer) {
                if (level instanceof net.minecraft.server.level.ServerLevel pvpLevel && !pvpLevel.isPvpAllowed()) {
                    continue;
                }
            }

            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
            
            
            
            living.setLastHurtByPlayer(player, living.tickCount);

            if (living instanceof PathfinderMob mob) {
                mob.setTarget(player);
            }
        }
    }

    private static int getWarpFromGear(Player player) {
        int w = PlayerEvents.getFinalWarp(player.getMainHandItem(), player);

        for (net.minecraft.world.entity.EquipmentSlot armorSlot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(armorSlot);
            w += PlayerEvents.getFinalWarp(armor, player);
        }

        final int[] curiosWarp = {0};
        arcaneresearch.common.lib.compat.CuriosCompat.forEachCuriosItem(player,
                stack -> curiosWarp[0] += PlayerEvents.getFinalWarp(stack, player));
        w += curiosWarp[0];

        return w;
    }

    private static net.minecraft.network.chat.Component getWarpText(int index) {
        if (index >= 1 && index <= 16) {
            return net.minecraft.network.chat.Component.literal("§5§o")
                    .append(net.minecraft.network.chat.Component.translatable("warp.text." + index));
        }
        return net.minecraft.network.chat.Component.literal("§5§oThe warp consumes you...");
    }
}
