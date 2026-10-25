package arcaneresearch.common.lib.events;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;
import arcaneresearch.common.entities.monster.boss.EntityArcaneresearchBoss;
import arcaneresearch.common.entities.monster.mods.ChampionModTainted;
import arcaneresearch.common.entities.monster.mods.ChampionModifier;
import arcaneresearch.common.items.armor.ItemBootsTraveller;
import arcaneresearch.common.items.armor.ItemFortressArmor;
import arcaneresearch.common.items.baubles.ItemCharmUndying;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXShield;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModItems;
import arcaneresearch.proxies.CommonProxy;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvents {
    private static final TagKey<Biome> SPOOKY_TAG =
            TagKey.create(Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "is_spooky"));
    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (itemEntity == null) return;
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;
        if (ModItems.BATH_SALTS != null && stack.getItem() == ModItems.BATH_SALTS.get()) {
            BlockPos pos = itemEntity.blockPosition();
            BlockState state = itemEntity.level().getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()
                    && ModBlocks.PURIFYING_FLUID != null && ModBlocks.PURIFYING_FLUID.get() != null) {
                itemEntity.level().setBlock(pos, ModBlocks.PURIFYING_FLUID.get().defaultBlockState(), 3);
            }
        }
    }
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living == null || living.level().isClientSide) return;
        int t = ChampionModifier.getChampionId(living);
        try {
            if (t >= 0 && t < ChampionModifier.MODS.length && ChampionModifier.MODS[t].type == 0) {
                ChampionModifier.MODS[t].effect.performEffect(living, null, null, 0.0f);
            }
        } catch (Exception e) {
        }
    }
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        Entity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (entity instanceof Player player) {
            handlePlayerHurt(event);
        }
        if (entity instanceof Player fallPlayer && !entity.level().isClientSide
                && source.is(DamageTypeTags.IS_FALL)) {
            if (fallPlayer.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemBootsTraveller) {
                float f = Math.max(0.0F, event.getAmount() / 2.0F - 1.0F);
                if (f < 1.0F) {
                    event.setCanceled(true);
                    event.setAmount(0.0F);
                } else {
                    event.setAmount(f);
                }
            }
            if (!event.isCanceled()
                    && CuriosCompat.isEquipped(fallPlayer, ModItems.CLOUD_RING.get())) {
                float f = Math.max(0.0F, event.getAmount() / 3.0F - 2.0F);
                if (f < 1.0F) {
                    event.setCanceled(true);
                    event.setAmount(0.0F);
                } else if (f < event.getAmount()) {
                    event.setAmount(f);
                }
                if (event.getAmount() < 1.0F) {
                    event.setCanceled(true);
                    event.setAmount(0.0F);
                }
            }
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Player attackingPlayer) {
            handlePlayerAttack(attackingPlayer, event.getEntity(), event.getAmount());
        }
        if (entity instanceof LivingEntity dying && !(dying instanceof Player)
                && !dying.level().isClientSide && dying.isAlive()
                && dying.getHealth() < 2.0f
                && !dying.isInvertedHealAndHarm()
                && !(dying instanceof EntityOwnedConstruct)
                && !(dying instanceof ITaintedMob)
                && ChampionModifier.getChampionId(dying) < 0
                && ModEffects.FLUX_TAINT != null
                && dying.hasEffect(ModEffects.FLUX_TAINT.get())
                && dying.level().random.nextBoolean()) {
            ChampionModifier.setChampionId(dying, 13);
            dying.heal(25.0f);
            if (dying instanceof Mob mob) {
                ChampionModTainted.resetAI(mob);
            }
            return;
        }
        if (entity instanceof Monster mob && !mob.level().isClientSide) {
            int t = ChampionModifier.getChampionId(mob);
            boolean isEldritch = mob.getType().getDescriptionId().contains("eldritch");
            if ((t >= 0 && t < ChampionModifier.MODS.length) || isEldritch) {
                if ((t == 5 || isEldritch) && mob.getAbsorptionAmount() > 0.0f) {
                    int target2 = source.getEntity() != null ? source.getEntity().getId() : -1;
                    PacketHandler.sendToAllTracking(new PacketFXShield(mob.getId(), target2), mob);
                } else if (t >= 0 && t < ChampionModifier.MODS.length
                        && ChampionModifier.MODS[t].type == 2 && source.getEntity() instanceof LivingEntity attacker2) {
                    event.setAmount(ChampionModifier.MODS[t].effect.performEffect(mob, attacker2, source, event.getAmount()));
                }
            }
            if (event.getAmount() > 0.0f && source.getEntity() instanceof Monster attackerMob) {
                int at = ChampionModifier.getChampionId(attackerMob);
                if (at >= 0 && at < ChampionModifier.MODS.length && ChampionModifier.MODS[at].type == 1
                        && entity instanceof LivingEntity victim) {
                    event.setAmount(ChampionModifier.MODS[at].effect.performEffect(attackerMob, victim, source, event.getAmount()));
                }
            }
        }
    }
    private static void handlePlayerHurt(LivingHurtEvent event) {
        Player player = (Player) event.getEntity();
        if (player.level().isClientSide) return;
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            if (ArcaneresearchCapabilities.knowsResearchStrict(player, "BASEAUROMANCY@2") &&
                    !ArcaneresearchCapabilities.knowsResearch(player, "f_onfire")) {
                IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
                if (knowledge != null) {
                    knowledge.addResearch("f_onfire");
                    if (player instanceof ServerPlayer serverPlayer) {
                        knowledge.sync(serverPlayer);
                        serverPlayer.displayClientMessage(
                                net.minecraft.network.chat.Component.translatable("got.onfire")
                                        .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE), true);
                    }
                }
            }
        }
        Entity directSource = source.getDirectEntity();
        if (directSource != null && ArcaneresearchCapabilities.knowsResearchStrict(player, "FOCUSPROJECTILE@2")) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                boolean discovered = false;
                if (directSource instanceof AbstractArrow &&
                        !ArcaneresearchCapabilities.knowsResearch(player, "f_arrow")) {
                    knowledge.addResearch("f_arrow");
                    discovered = true;
                }
                if (directSource instanceof Fireball &&
                        !ArcaneresearchCapabilities.knowsResearch(player, "f_fireball")) {
                    knowledge.addResearch("f_fireball");
                    discovered = true;
                }
                if (directSource instanceof net.minecraft.world.entity.projectile.LlamaSpit &&
                        !ArcaneresearchCapabilities.knowsResearch(player, "f_spit")) {
                    knowledge.addResearch("f_spit");
                    discovered = true;
                }
                if (discovered && player instanceof ServerPlayer sp) {
                    knowledge.sync(sp);
                    sp.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("got.projectile")
                                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE), true);
                }
            }
        }
        Entity trueSource = source.getEntity();
        if (trueSource instanceof LivingEntity attacker) {
            ItemStack helm = player.getInventory().getArmor(3);
            if (!helm.isEmpty() && helm.getItem() instanceof ItemFortressArmor
                    && ItemFortressArmor.hasMask(helm) && ItemFortressArmor.getMaskType(helm) == 1
                    && player.level().random.nextFloat() < amount / 10.0f) {
                try {
                    attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.WITHER, 80));
                } catch (Exception ex) {
                }
            }
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)
                || source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)
                || source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
            return;
        }
        Integer pid = player.getId();
        if (!PlayerEvents.runicInfo.containsKey(pid) || PlayerEvents.runicCharge.getOrDefault(pid, 0) <= 0) {
            return;
        }
        long time = System.currentTimeMillis();
        int target = -1;
        if (source.getEntity() != null) {
            target = source.getEntity().getId();
        }
        PacketHandler.sendToAllTracking(new PacketFXShield(player.getId(), target), player);
        int charge = PlayerEvents.runicCharge.get(pid);
        if (charge > amount) {
            charge = (int) (charge - amount);
            event.setAmount(0.0F);
        } else {
            event.setAmount(amount - charge);
            charge = 0;
        }
        Integer[] info = PlayerEvents.runicInfo.get(pid);
        String key = pid + ":2";
        if (charge <= 0 && info[2] > 0 && (!PlayerEvents.upgradeCooldown.containsKey(key)
                || PlayerEvents.upgradeCooldown.get(key) < time)) {
            PlayerEvents.upgradeCooldown.put(key, time + 20000L);
            player.level().explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0D,
                    player.getZ(), 1.5F + info[2] * 0.5F, false,
                    net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }
        key = pid + ":3";
        if (charge <= 0 && info[3] > 0 && (!PlayerEvents.upgradeCooldown.containsKey(key)
                || PlayerEvents.upgradeCooldown.get(key) < time)) {
            PlayerEvents.upgradeCooldown.put(key, time + 20000L);
            try {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.REGENERATION, 240, info[3]));
            } catch (Exception ignored) {
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    arcaneresearch.init.ModSounds.RUNIC_SHIELD_EFFECT.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        key = pid + ":4";
        if (charge <= 0 && info[4] > 0 && (!PlayerEvents.upgradeCooldown.containsKey(key)
                || PlayerEvents.upgradeCooldown.get(key) < time)) {
            PlayerEvents.upgradeCooldown.put(key, time + 60000L);
            int t = 8 * info[4];
            charge = Math.min(info[0], t);
            PlayerEvents.runicDirty = true;
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    arcaneresearch.init.ModSounds.RUNIC_SHIELD_CHARGE.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (charge <= 0) {
            PlayerEvents.rechargeDelay =
                    Math.max(1, arcaneresearch.common.config.ModConfig.shieldWait / 50);
        }
        PlayerEvents.runicCharge.put(pid, charge);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.playerdata.PacketRunicCharge(
                            pid, charge, info[0]),
                    sp);
        }
    }
    private static void handlePlayerAttack(Player player, LivingEntity target, float damage) {
        ItemStack helm = player.getInventory().getArmor(3);
        if (!helm.isEmpty() && helm.getItem() instanceof ItemFortressArmor
                && ItemFortressArmor.hasMask(helm) && ItemFortressArmor.getMaskType(helm) == 2
                && player.level().random.nextFloat() < damage / 12.0f) {
            player.heal(1.0f);
        }
    }
    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        if (player != null && player.getName().getString().startsWith("FakeArcaneresearch")) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        DamageSource source = event.getSource();
        boolean fakePlayer = source.getEntity() instanceof FakePlayer;
        if (entity instanceof Zombie && !(entity.getType().getDescriptionId().contains("brainy"))) {
            if (event.isRecentlyHit() && entity.level().random.nextInt(10) - event.getLootingLevel() < 1) {
                if (ModItems.ZOMBIE_BRAIN != null) {
                    ItemEntity brainDrop = new ItemEntity(
                            entity.level(),
                            entity.getX(),
                            entity.getY() + entity.getEyeHeight(),
                            entity.getZ(),
                            new ItemStack(ModItems.ZOMBIE_BRAIN.get()));
                    event.getDrops().add(brainDrop);
                }
            }
        }
        if (source.getMsgId().equals("arcaneresearch.dissolve")) {
            AspectList aspects = AspectHelper.getEntityAspects(entity);
            if (aspects != null && aspects.size() > 0) {
                Aspect[] aspectArray = aspects.getAspects();
                int dropCount = entity.level().random.nextInt(1 + aspects.visSize() / 10) + 1;
                for (int i = 0; i < dropCount && aspectArray.length > 0; i++) {
                    Aspect aspect = aspectArray[entity.level().random.nextInt(aspectArray.length)];
                    ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect);
                    if (!crystal.isEmpty()) {
                        ItemEntity crystalDrop = new ItemEntity(
                                entity.level(),
                                entity.getX(),
                                entity.getY() + entity.getEyeHeight(),
                                entity.getZ(),
                                crystal);
                        event.getDrops().add(crystalDrop);
                    }
                }
            }
        }
        if (entity instanceof arcaneresearch.common.entities.monster.cult.EntityCultist
                && !fakePlayer && source.getEntity() instanceof Player p) {
            int c = !ArcaneresearchCapabilities.knowsResearch(p, "!CrimsonCultist@2") ? 4 : 20;
            if (ModItems.CURIO_RITES != null
                    && arcaneresearch.common.lib.utils.InventoryUtils.getPlayerSlotFor(
                    p, new ItemStack(ModItems.CURIO_RITES.get())) >= 0) {
                c = 50;
            }
            if (ModItems.CURIO_RITES != null && entity.level().random.nextInt(c) == 0) {
                event.getDrops().add(new ItemEntity(entity.level(),
                        entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(),
                        new ItemStack(ModItems.CURIO_RITES.get())));
            }
        }
        int champId = ChampionModifier.getChampionId(entity);
        if (champId >= 0 && champId != 13 && event.isRecentlyHit()
                && !(entity instanceof arcaneresearch.common.entities.monster.boss.EntityArcaneresearchBoss)
                && !(event.getSource().getEntity() instanceof FakePlayer)) {
            int lb = Math.min(2, (entity.level().random.nextInt(9) + event.getLootingLevel()) / 5);
            ItemStack bag = switch (lb) {
                case 2 ->
                        ModItems.LOOT_BAG_RARE != null ? new ItemStack(ModItems.LOOT_BAG_RARE.get()) : ItemStack.EMPTY;
                case 1 ->
                        ModItems.LOOT_BAG_UNCOMMON != null ? new ItemStack(ModItems.LOOT_BAG_UNCOMMON.get()) : ItemStack.EMPTY;
                default ->
                        ModItems.LOOT_BAG_COMMON != null ? new ItemStack(ModItems.LOOT_BAG_COMMON.get()) : ItemStack.EMPTY;
            };
            if (!bag.isEmpty()) {
                event.getDrops().add(new ItemEntity(entity.level(),
                        entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), bag));
            }
            int xp = 5 + entity.level().random.nextInt(3);
            while (xp > 0) {
                int split = net.minecraft.world.entity.ExperienceOrb.getExperienceValue(xp);
                xp -= split;
                entity.level().addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(
                        entity.level(), entity.getX(), entity.getY(), entity.getZ(), split));
            }
        }
    }
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack charm = ItemCharmUndying.findCharm(player);
        if (charm == null || charm.isEmpty()) return;
        ItemCharmUndying.applyUndyingEffect(player, charm);
        player.level().broadcastEntityEvent(player, (byte) 35);
        event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster mob)) return;
        if (mob instanceof EntityArcaneresearchBoss) return;
        if (ChampionModifier.getChampionId(mob) != -1) return;
        if (mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) == null
                || mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue() < 10.0) {
            ChampionModifier.setChampionId(mob, -2);
            return;
        }
        boolean championsAllowed = ModConfig.CONFIG_WORLD.allowChampionMobs;
        int c = mob.level().random.nextInt(100);
        if (mob.level().getDifficulty() == net.minecraft.world.Difficulty.EASY || !championsAllowed) {
            c += 2;
        }
        if (mob.level().getDifficulty() == net.minecraft.world.Difficulty.HARD) {
            c -= (championsAllowed ? 2 : 0);
        }
        var biome = mob.level().getBiome(mob.blockPosition());
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END) || biome.is(SPOOKY_TAG)) {
            c -= (championsAllowed ? 2 : 1);
        }
        int cc = 0;
        boolean whitelisted = false;
        for (var entry : CommonProxy.championModWhitelist.entrySet()) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
            if (type != null && type == mob.getType()) {
                whitelisted = true;
                if (!championsAllowed && !(mob instanceof EntityArcaneresearchBoss)) {
                    continue;
                }
                cc = Math.max(cc, entry.getValue() - 1);
            }
        }
        c -= cc;
        if (whitelisted && c <= 0) {
            EntityUtils.makeChampion(mob, false);
        } else {
            ChampionModifier.setChampionId(mob, -2);
        }
    }
    @SubscribeEvent
    public static void onEntityConstruct(EntityEvent.EntityConstructing event) {
    }
}
