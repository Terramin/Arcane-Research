package arcaneresearch.common.lib.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.items.armor.ItemBootsTraveller;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class PlayerEvents {

     




 
    static final Map<Integer, Integer> runicCharge = new HashMap<>();
    static final Map<Integer, Integer[]> runicInfo = new HashMap<>();
    private static final Map<Integer, Integer> lastCharge = new HashMap<>();
    private static final Map<Integer, Long> nextCycle = new HashMap<>();
      
    static final Map<String, Long> upgradeCooldown = new HashMap<>();
    static boolean runicDirty = true;
      
    static int rechargeDelay = 0;

    @SubscribeEvent
    public static void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
        try {

            IPlayerKnowledge oldKnowledge = ArcaneresearchCapabilities.getKnowledge(event.getOriginal());
            IPlayerKnowledge newKnowledge = ArcaneresearchCapabilities.getKnowledge(event.getEntity());
            if (oldKnowledge != null && newKnowledge != null) {
                CompoundTag nbtKnowledge = oldKnowledge.serializeNBT();
                newKnowledge.deserializeNBT(nbtKnowledge);
            }

            IPlayerWarp oldWarp = ArcaneresearchCapabilities.getWarp(event.getOriginal());
            IPlayerWarp newWarp = ArcaneresearchCapabilities.getWarp(event.getEntity());
            if (oldWarp != null && newWarp != null) {
                CompoundTag nbtWarp = oldWarp.serializeNBT();
                newWarp.deserializeNBT(nbtWarp);
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("Could not clone player [{}] knowledge when respawning/changing dimensions",
                    event.getOriginal().getName().getString(), e);
        }
    }

    @SubscribeEvent
    public static void playerJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);

            if (knowledge != null) {
                knowledge.sync(player);
            }
            if (warp != null) {
                warp.sync(player);
            }

            Arcaneresearch.LOGGER.debug("Synced Arcaneresearch data for player: {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    
    
    public static void livingTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {

            handleMisc(player);
            if (!player.level().isClientSide()) {

                handleRunicArmor(player);

                if (player.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
                    String playerName = player.getName().getString();
                    if (arcaneresearch.common.lib.research.ResearchManager.syncList.remove(playerName) != null) {
                        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
                        if (knowledge != null) {
                            knowledge.sync(serverPlayer);
                        }
                    }
                }

                if (player.tickCount % 200 == 0) {
                    ConfigResearch.checkPeriodicResearch(player);
                }

                if (player.tickCount % 2000 == 0
                        && !arcaneresearch.common.config.ModConfig.wussMode
                        && !player.hasEffect(arcaneresearch.init.ModEffects.WARP_WARD)) {
                    WarpEvents.checkWarpEvent(player);
                }

                if (player.tickCount % 20 == 0) {
                    WarpEvents.checkDeathGaze(player);
                }

                
                
                if (player.tickCount % 20 == 0 && player instanceof ServerPlayer shaderPlayer) {
                    arcaneresearch.client.lib.events.TCShaderHandler.tickServer(shaderPlayer);
                }
            }
        }
    }

    private static void handleMisc(Player player) {
        if (player.tickCount % 20 == 0 && !player.isSpectator()
                && !player.getAbilities().instabuild && player.getAbilities().flying
                && player.level().dimension().identifier().getPath().equals("outer_lands")) {
            player.getAbilities().flying = false;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.onUpdateAbilities();
            }
            player.sendOverlayMessage(Component.translatable("tc.break.fly")
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void playerJumps(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.getItem() instanceof ItemBootsTraveller
                && RechargeHelper.getCharge(boots) > 0) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + 0.275D, motion.z);
        }
    }

    public static boolean isRunicShieldActive(Player player) {
        Integer id = player.getId();
        return runicInfo.containsKey(id) && runicCharge.getOrDefault(id, 0) > 0;
    }

    private static void sendRunicCharge(Player player, int charge, int max) {
        if (player instanceof ServerPlayer sp) {
            arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.playerdata.PacketRunicCharge(
                            player.getId(), charge, max),
                    sp);
        }
    }

    private static void handleRunicArmor(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        Integer id = player.getId();

        if (runicDirty || player.tickCount % 40 == 0) {
            int max = 0;
            int charged = 0;
            int kinetic = 0;
            int healing = 0;
            int emergency = 0;
            runicDirty = false;

            
            for (net.minecraft.world.entity.EquipmentSlot armorSlot : new net.minecraft.world.entity.EquipmentSlot[]{
                    net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                    net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
                ItemStack armor = player.getItemBySlot(armorSlot);
                max += getRunicCharge(armor);
            }
            final int[] curiosMax = {0};
            arcaneresearch.common.lib.compat.CuriosCompat.forEachCuriosItem(player,
                    stack -> curiosMax[0] += getRunicCharge(stack));
            max += curiosMax[0];
            
            int[] procs = countRunicProcs(player);
            charged = procs[0];
            kinetic = procs[1];
            healing = procs[2];
            emergency = procs[3];

            if (max > 0) {
                runicInfo.put(id, new Integer[]{max, charged, kinetic, healing, emergency});
                if (runicCharge.containsKey(id)) {
                    int charge = runicCharge.get(id);
                    if (charge > max) {
                        runicCharge.put(id, max);
                        sendRunicCharge(player, max, max);
                    }
                }
            } else {
                runicInfo.remove(id);
                runicCharge.put(id, 0);
                sendRunicCharge(player, 0, 0);
            }
        }

        if (rechargeDelay > 0) {
            rechargeDelay--;
        } else if (runicInfo.containsKey(id)) {
            lastCharge.putIfAbsent(id, -1);
            runicCharge.putIfAbsent(id, 0);
            nextCycle.putIfAbsent(id, 0L);

            long time = System.currentTimeMillis();
            int charge = runicCharge.get(id);
            if (charge > runicInfo.get(id)[0]) {
                charge = runicInfo.get(id)[0];
            } else if (charge < runicInfo.get(id)[0] && nextCycle.get(id) < time
                    && arcaneresearch.common.items.wands.WandManager.consumeVisFromInventory(player,
                    new arcaneresearch.api.aspects.AspectList()
                            .add(arcaneresearch.api.aspects.Aspect.AIR,
                                    arcaneresearch.common.config.ModConfig.shieldCost)
                            .add(arcaneresearch.api.aspects.Aspect.EARTH,
                                    arcaneresearch.common.config.ModConfig.shieldCost))) {
                long interval = arcaneresearch.common.config.ModConfig.shieldRecharge
                        - (long) runicInfo.get(id)[1] * 500L;
                nextCycle.put(id, time + interval);
                charge++;
                runicCharge.put(id, charge);
            }
            if (lastCharge.get(id) != charge) {
                sendRunicCharge(player, charge, runicInfo.get(id)[0]);
                lastCharge.put(id, charge);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return;

        boolean hasPickedCrystal = knowledge.isResearchKnown("!gotcrystal");
        boolean hasDream = knowledge.isResearchKnown("!gotdream");

        if (hasPickedCrystal && !hasDream) {
            giveDreamJournal(serverPlayer);
            Arcaneresearch.LOGGER.info("Player {} received the Arcaneresearch dream", player.getName().getString());
        }
    }

    private static void giveDreamJournal(ServerPlayer player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return;
        }
        knowledge.addResearch("!gotdream");
        knowledge.sync(player);

        ItemStack book = new ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK);
        
        
        
        
        java.util.List<net.minecraft.server.network.Filterable<Component>> pages = new java.util.ArrayList<>();
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.1").getString())));
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.2").getString())));
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.3").getString())));
        String title = Component.translatable("book.start.title").getString();
        if (title.length() > net.minecraft.world.item.component.WrittenBookContent.TITLE_MAX_LENGTH) {
            
            title = title.substring(0, net.minecraft.world.item.component.WrittenBookContent.TITLE_MAX_LENGTH);
        }
        book.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,
                new net.minecraft.world.item.component.WrittenBookContent(
                        net.minecraft.server.network.Filterable.passThrough(title),
                        player.getName().getString(), 3, pages, false));
        if (!player.getInventory().add(book)) {
            player.drop(book, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        player.sendSystemMessage(Component.literal("§5" + Component.translatable("got.dream").getString()));
    }

      
    private static Component bookPage(String raw) {
        try {
            return net.minecraft.network.chat.ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(raw))
                    .getOrThrow();
        } catch (Exception e) {
            return Component.literal(raw);
        }
    }

    @SubscribeEvent
    
    
    
    public static void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }

        ItemStack stack = event.getOriginalStack();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();

        boolean isCrystal = false;

        if (item instanceof ItemCrystalEssence) {
            isCrystal = true;
        } else if (item == ModBlocks.CRYSTAL_AIR.get().asItem() ||
                item == ModBlocks.CRYSTAL_FIRE.get().asItem() ||
                item == ModBlocks.CRYSTAL_WATER.get().asItem() ||
                item == ModBlocks.CRYSTAL_EARTH.get().asItem() ||
                item == ModBlocks.CRYSTAL_ORDER.get().asItem() ||
                item == ModBlocks.CRYSTAL_ENTROPY.get().asItem() ||
                item == ModBlocks.CRYSTAL_FLUX.get().asItem()) {
            isCrystal = true;
        }

        if (isCrystal) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null && !knowledge.isResearchKnown("!gotcrystal")) {

                knowledge.addResearch("!gotcrystal");

                if (player instanceof ServerPlayer serverPlayer) {
                    knowledge.sync(serverPlayer);
                }

                player.sendOverlayMessage(Component.translatable("tc.crystal.pickup"));

                if (arcaneresearch.common.config.ModConfig.noSleep
                        && !knowledge.isResearchKnown("!gotdream")
                        && player instanceof ServerPlayer serverPlayer2) {
                    giveDreamJournal(serverPlayer2);
                }

                Arcaneresearch.LOGGER.info("Player {} picked up first crystal - sleep to receive dream!",
                        player.getName().getString());
            }
        }

        if (item == ModItems.THAUMONOMICON.get()) {
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null && !knowledge.isResearchKnown("!gotthaumonomicon")) {
                knowledge.addResearch("!gotthaumonomicon");

                if (player instanceof ServerPlayer serverPlayer) {
                    knowledge.sync(serverPlayer);
                }
                Arcaneresearch.LOGGER.info("Player {} picked up Thaumonomicon - research unlocked!",
                        player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void droppedItem(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        CompoundTag itemData = itemEntity.getPersistentData();
        itemData.putString("thrower", event.getPlayer().getName().getString());
    }

    @SubscribeEvent
    public static void pickupXP(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        net.minecraft.world.entity.ExperienceOrb orb = event.getOrb();
        if (orb == null || orb.getValue() <= 1) {
            return;
        }
        if (!arcaneresearch.common.items.baubles.ItemCuriosityBand.isWearingBand(player)) {
            return;
        }
        int d = orb.getValue() / 2;
        
        
        
        orb.setValue(orb.getValue() - d);
        float r = player.getRandom().nextFloat();
        String[] cats = arcaneresearch.api.research.ResearchCategories.researchCategories.keySet()
                .toArray(new String[0]);
        if (cats.length == 0) {
            return;
        }
        String cat = cats[player.getRandom().nextInt(cats.length)];
        if (r < 0.05 * d) {
            arcaneresearch.api.ArcaneresearchApi.internalMethods.addKnowledge(player,
                    IPlayerKnowledge.EnumKnowledgeType.THEORY,
                    arcaneresearch.api.research.ResearchCategories.getResearchCategory(cat), 1);
        } else if (r < 0.2 * d) {
            arcaneresearch.api.ArcaneresearchApi.internalMethods.addKnowledge(player,
                    IPlayerKnowledge.EnumKnowledgeType.OBSERVATION,
                    arcaneresearch.api.research.ResearchCategories.getResearchCategory(cat), 1);
        }
    }

    @SubscribeEvent
    public static void finishedUsingItem(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof Player player)) {
            return;
        }
        var effectHolder = arcaneresearch.init.ModEffects.UNNATURAL_HUNGER;
        if (effectHolder == null || !player.hasEffect(effectHolder)) {
            return;
        }
        var eaten = event.getItem();
        boolean isRemedy = eaten.is(net.minecraft.world.item.Items.ROTTEN_FLESH)
                || (ModItems.ZOMBIE_BRAIN != null && eaten.is(ModItems.ZOMBIE_BRAIN.get()));
        if (!isRemedy) {

            player.sendSystemMessage(Component.translatable("warp.text.hunger.1"));
            return;
        }
        var active = player.getEffect(effectHolder);
        if (active == null) {
            return;
        }
        int amp = active.getAmplifier() - 1;
        int duration = active.getDuration() - 600;
        player.removeEffect(effectHolder); 
        if (duration > 0 && amp >= 0) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    effectHolder, duration, amp, true, false));
        }
        player.sendSystemMessage(Component.translatable("warp.text.hunger.2"));
    }

     


 
    private static int[] countRunicProcs(Player player) {
        int[] out = new int[4];
        try {
            arcaneresearch.common.lib.compat.CuriosCompat.forEachCuriosItem(player, stack -> {
                if (!stack.isEmpty()
                        && stack.getItem() instanceof arcaneresearch.common.items.baubles.ItemRunicBauble bauble) {
                    switch (bauble.getProc(stack)) {
                        case CHARGED -> out[0]++;
                        case KINETIC -> out[1]++;
                        case HEALING -> out[2]++;
                        case EMERGENCY -> out[3]++;
                        default -> {
                        }
                    }
                }
            });
        } catch (Exception ignored) {
        }
        return out;
    }

    public static void markForSync(Player player) {
        arcaneresearch.common.lib.research.ResearchManager.syncList.put(player.getName().getString(), true);
    }

    public static int getRunicCharge(net.minecraft.world.item.ItemStack stack) {
        
        if (stack.isEmpty() || !(stack.getItem() instanceof arcaneresearch.api.items.IRunicArmor armor)) {
            return 0;
        }
        int base = armor.getRunicCharge(stack);
        CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("TC.RUNIC")) {
            base += tag.getByteOr("TC.RUNIC", (byte) 0);
        }
        return base;
    }

    public static int getFinalWarp(net.minecraft.world.item.ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = 0;

        if (stack.getItem() instanceof arcaneresearch.api.items.IWarpingGear warpingGear) {
            warp += warpingGear.getWarp(stack, player);
        }

        CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null) {
            if (tag != null && tag.contains("TC.WARP")) {
                warp += tag.getByteOr("TC.WARP", (byte) 0);
            }
        }

        return warp;
    }

    public static int getFinalDiscount(net.minecraft.world.item.ItemStack stack, Player player) {
        if (stack.isEmpty() || !(stack.getItem() instanceof arcaneresearch.api.items.IVisDiscountGear gear)) {
            return 0;
        }
        return gear.getVisDiscount(stack, player);
    }
}
