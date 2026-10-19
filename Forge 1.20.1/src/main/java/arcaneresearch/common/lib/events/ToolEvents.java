package arcaneresearch.common.lib.events;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.entities.EntityFollowingItem;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXScanSource;
import arcaneresearch.common.lib.network.fx.PacketFXSlash;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToolEvents {
    public static final HashMap<String, ArrayList<BlockPos>> blockedBlocks = new HashMap<>();
    private static final HashMap<Integer, Direction> lastFaceClicked = new HashMap<>();
    private static final Map<Integer, Long> collectorBreaks = new HashMap<>();
    private static final Map<Integer, BlockPos> collectorBreakPos = new HashMap<>();
    private static boolean blockDestructiveRecursion = false;
    @SubscribeEvent
    public static void playerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        InteractionHand hand = player.getUsedItemHand();
        if (hand == null) hand = InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> enchantments = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        if (enchantments.contains(EnumInfusionEnchantment.ARCING) && event.getTarget().isAlive()) {
            int rank = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.ARCING);
            List<Entity> targets = player.level().getEntitiesOfClass(Entity.class,
                    event.getTarget().getBoundingBox().inflate(1.5 + rank, 1.0 + rank / 2.0, 1.5 + rank),
                    e -> e != player && e != event.getTarget());
            int count = 0;
            if (targets.size() > 0) {
                for (Entity target : targets) {
                    if (target.isRemoved() || !target.isAlive()) continue;
                    if (EntityUtils.isFriendly(player, target)) continue;
                    if (!(target instanceof Mob)) continue;
                    if (target instanceof Player otherPlayer && otherPlayer.getName().equals(player.getName()))
                        continue;
                    if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        float damage = (float) (attackDamage * 0.5f);
                        DamageSource damageSource = player.damageSources().playerAttack(player);
                        if (livingTarget.hurt(damageSource, damage)) {
                            float yawRad = player.getYRot() * ((float) Math.PI / 180f);
                            livingTarget.push(
                                    -Mth.sin(yawRad) * 0.5f,
                                    0.1,
                                    Mth.cos(yawRad) * 0.5f
                            );
                            ++count;
                            if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
                                PacketHandler.sendToAllTracking(new PacketFXSlash(player, livingTarget), player);
                            }
                        }
                    }
                    if (count >= rank) break;
                }
                if (count > 0 && !player.level().isClientSide) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.WIND.get(), SoundSource.PLAYERS,
                            1.0f, 0.9f + player.level().random.nextFloat() * 0.2f);
                }
            }
        }
    }
    @SubscribeEvent
    public static void playerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getEntity() == null) return;
        Player player = event.getEntity();
        InteractionHand usedHand = player.getUsedItemHand();
        final InteractionHand hand = (usedHand != null) ? usedHand : InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> enchantments = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        if (enchantments.contains(EnumInfusionEnchantment.SOUNDING) && player.isShiftKeyDown()) {
            heldItem.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(hand));
            BlockPos pos = event.getPos();
            event.getLevel().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    ModSounds.WAND_FAIL.get(), SoundSource.BLOCKS,
                    0.2f, 0.2f + event.getLevel().random.nextFloat() * 0.2f);
            int level = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.SOUNDING);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandler.sendToPlayer(new PacketFXScanSource(pos, level), serverPlayer);
            }
        }
    }
    @SubscribeEvent
    public static void playerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() != null && event.getFace() != null) {
            lastFaceClicked.put(event.getEntity().getId(), event.getFace());
        }
    }
    public static void addBlockedBlock(Level level, BlockPos pos) {
        String dimKey = level.dimension().location().toString();
        blockedBlocks.computeIfAbsent(dimKey, k -> new ArrayList<>());
        ArrayList<BlockPos> list = blockedBlocks.get(dimKey);
        if (!list.contains(pos)) {
            list.add(pos);
        }
    }
    public static void clearBlockedBlock(Level level, BlockPos pos) {
        String dimKey = level.dimension().location().toString();
        blockedBlocks.computeIfAbsent(dimKey, k -> new ArrayList<>());
        blockedBlocks.get(dimKey).remove(pos);
    }
    @SubscribeEvent
    public static void breakBlockEvent(BlockEvent.BreakEvent event) {
        try {
        if (!(event.getLevel() instanceof Level level)) return;
        String dimKey = level.dimension().location().toString();
        if (blockedBlocks.containsKey(dimKey)) {
            ArrayList<BlockPos> list = blockedBlocks.get(dimKey);
            if (list != null && list.contains(event.getPos())) {
                event.setCanceled(true);
                return;
            }
        }
        if (!level.isClientSide) {
            for (Direction face : Direction.values()) {
                arcaneresearch.common.golems.seals.SealHandler.removeSealEntity(
                        level,
                        new arcaneresearch.api.golems.seals.SealPos(event.getPos(), face),
                        false);
            }
        }
        if (level.isClientSide || event.getPlayer() == null) return;
        Player player = event.getPlayer();
        InteractionHand usedHand = player.getUsedItemHand();
        final InteractionHand hand = (usedHand != null) ? usedHand : InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> enchantments = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        if (enchantments.contains(EnumInfusionEnchantment.BURROWING) && !player.isShiftKeyDown()) {
            if (isToolEffective(level, event.getPos(), heldItem) && isValidBurrowBlock(level, event.getPos())) {
                event.setCanceled(true);
                if (!player.getName().getString().equals("FakeArcaneresearchBore")) {
                    heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                breakFurthestBlock(level, event.getPos(), event.getState(), player);
            }
        }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("Error in breakBlockEvent at {}", event.getPos(), e);
        }
    }
    private static boolean isValidBurrowBlock(Level level, BlockPos pos) {
        return BlockUtils.isLog(level, pos) || BlockUtils.isOre(level, pos);
    }
    private static void breakFurthestBlock(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos furthest = findFurthestConnectedBlock(level, pos, state, 32);
        if (furthest != null && !furthest.equals(pos)) {
            BlockState furthestState = level.getBlockState(furthest);
            if (level instanceof ServerLevel serverLevel) {
                Block.dropResources(furthestState, level, furthest, level.getBlockEntity(furthest), player, player.getMainHandItem());
            }
            level.destroyBlock(furthest, false, player);
        } else {
            if (level instanceof ServerLevel serverLevel) {
                Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
            }
            level.destroyBlock(pos, false, player);
        }
    }
    private static BlockPos findFurthestConnectedBlock(Level level, BlockPos start, BlockState targetState, int maxDistance) {
        Block targetBlock = targetState.getBlock();
        BlockPos furthest = start;
        double maxDistSq = 0;
        ArrayList<BlockPos> visited = new ArrayList<>();
        ArrayList<BlockPos> toVisit = new ArrayList<>();
        toVisit.add(start);
        while (!toVisit.isEmpty() && visited.size() < 512) {
            BlockPos current = toVisit.remove(0);
            if (visited.contains(current)) continue;
            visited.add(current);
            double distSq = current.distSqr(start);
            if (distSq > maxDistance * maxDistance) continue;
            BlockState currentState = level.getBlockState(current);
            if (currentState.getBlock() != targetBlock) continue;
            if (distSq > maxDistSq) {
                maxDistSq = distSq;
                furthest = current;
            }
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && !toVisit.contains(neighbor)) {
                    toVisit.add(neighbor);
                }
            }
        }
        return furthest;
    }
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        try {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();
        InteractionHand usedHand2 = player.getUsedItemHand();
        final InteractionHand hand = (usedHand2 != null) ? usedHand2 : InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> enchantments = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        if (enchantments.isEmpty()) return;
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!isToolEffective(level, pos, heldItem)) return;
        if (enchantments.contains(EnumInfusionEnchantment.COLLECTOR) && !player.isShiftKeyDown()) {
            noteCollectorBreak(player, pos);
        }
        if (!blockDestructiveRecursion && enchantments.contains(EnumInfusionEnchantment.DESTRUCTIVE) && !player.isShiftKeyDown()) {
            blockDestructiveRecursion = true;
            try {
            Direction face = lastFaceClicked.get(player.getId());
            if (face == null) {
                face = Direction.orderedByNearest(player)[0];
            }
            for (int aa = -1; aa <= 1; ++aa) {
                for (int bb = -1; bb <= 1; ++bb) {
                    if (aa == 0 && bb == 0) continue;
                    int xx = 0, yy = 0, zz = 0;
                    if (face.getAxis() == Direction.Axis.Y) {
                        xx = aa;
                        zz = bb;
                    } else if (face.getAxis() == Direction.Axis.Z) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }
                    BlockPos targetPos = pos.offset(xx, yy, zz);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.getDestroySpeed(level, targetPos) >= 0.0f &&
                            isToolEffective(level, targetPos, heldItem)) {
                        if (!player.getName().getString().equals("FakeArcaneresearchBore")) {
                            heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                        }
                        level.destroyBlock(targetPos, true, player);
                    }
                }
            }
            } finally {
                blockDestructiveRecursion = false;
            }
        }
        if (enchantments.contains(EnumInfusionEnchantment.LAMPLIGHT) && !player.isShiftKeyDown()) {
            level.getServer().execute(() -> {
                if (level.isEmptyBlock(pos) && level.getRawBrightness(pos, 0) < 10) {
                    if (ModBlocks.EFFECT_GLIMMER != null && ModBlocks.EFFECT_GLIMMER.get() != null) {
                        level.setBlock(pos, ModBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            });
        }
        } catch (Exception e) {
            blockDestructiveRecursion = false;
            Arcaneresearch.LOGGER.error("Error in onBlockBreak at {}", event.getPos(), e);
        }
    }
    @SubscribeEvent
    public static void onLootTableLoad(net.minecraftforge.event.LootTableLoadEvent event) {
        float chance = nuggetChance(event.getName());
        if (chance <= 0 || ModItems.RAREEARTH_NUGGET == null) {
            return;
        }
        event.getTable().addPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                .add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(
                        ModItems.RAREEARTH_NUGGET.get()))
                .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1.0f))
                .when(net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert(
                        net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches(
                                net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                                        .hasEnchantment(new net.minecraft.advancements.critereon.EnchantmentPredicate(
                                                net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH,
                                                net.minecraft.advancements.critereon.MinMaxBounds.Ints.atLeast(1))))))
                .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
                        .randomChance(chance))
                .build());
    }
    private static float nuggetChance(net.minecraft.resources.ResourceLocation table) {
        if (table == null) {
            return -1.0f;
        }
        String path = table.getPath();
        if (!path.startsWith("blocks/")) {
            return -1.0f;
        }
        String name = path.substring("blocks/".length());
        return switch (name) {
            case "clay" -> 0.05f;
            case "coal_ore", "deepslate_coal_ore" -> 0.075f;
            case "diamond_ore", "deepslate_diamond_ore",
                 "redstone_ore", "deepslate_redstone_ore",
                 "lapis_ore", "deepslate_lapis_ore",
                 "nether_quartz_ore" -> 0.01f;
            case "emerald_ore", "deepslate_emerald_ore" -> 0.001f;
            case "amber_ore", "deepslate_amber_ore",
                 "quartz_ore", "deepslate_quartz_ore" -> 0.05f;
            default -> -1.0f;
        };
    }
    public static void noteCollectorBreak(Player player, BlockPos pos) {
        collectorBreaks.put(player.getId(), player.level().getGameTime());
        collectorBreakPos.put(player.getId(), pos.immutable());
    }
    @SubscribeEvent
    public static void onItemSpawn(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (event.getLevel().isClientSide()) return;
        if (collectorBreaks.isEmpty()) return;
        long now = event.getLevel().getGameTime();
        java.util.Iterator<Map.Entry<Integer, Long>> it = collectorBreaks.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (now - entry.getValue() > 8) {
                it.remove();
                collectorBreakPos.remove(entry.getKey());
                continue;
            }
            BlockPos pos = collectorBreakPos.get(entry.getKey());
            if (pos != null && item.blockPosition().distSqr(pos) <= 6.25
                    && event.getLevel() instanceof ServerLevel serverLevel
                    && serverLevel.getEntity(entry.getKey()) instanceof Player player) {
                ItemStack stack = item.getItem().copy();
                EntityFollowingItem following = new EntityFollowingItem(
                        serverLevel, item.getX(), item.getY(), item.getZ(), stack, player, 10);
                following.setDeltaMovement(item.getDeltaMovement());
                following.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(following);
                item.discard();
                return;
            }
        }
    }
    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        if (event.getSource() == null || event.getSource().getEntity() == null) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        InteractionHand usedHand = player.getUsedItemHand();
        final InteractionHand hand = (usedHand != null) ? usedHand : InteractionHand.MAIN_HAND;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> enchantments = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        if (enchantments.isEmpty()) return;
        Level level = event.getEntity().level();
        if (enchantments.contains(EnumInfusionEnchantment.COLLECTOR)) {
            List<ItemEntity> newDrops = new ArrayList<>();
            for (ItemEntity ei : event.getDrops()) {
                ItemStack stack = ei.getItem().copy();
                EntityFollowingItem followingItem = new EntityFollowingItem(
                        level, ei.getX(), ei.getY(), ei.getZ(), stack, player, 10);
                followingItem.setDeltaMovement(ei.getDeltaMovement());
                followingItem.setDefaultPickUpDelay();
                ei.discard();
                newDrops.add(followingItem);
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        }
        if (enchantments.contains(EnumInfusionEnchantment.ESSENCE)) {
            AspectList aspects = AspectHelper.getEntityAspects(event.getEntity());
            if (aspects != null && aspects.size() > 0) {
                AspectList workingAspects = aspects.copy();
                int enchantLevel = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.ESSENCE);
                int b = (level.random.nextInt(5) < enchantLevel) ? 0 : 99;
                Aspect[] aspectArray = workingAspects.getAspects();
                while (b < enchantLevel && aspectArray != null && aspectArray.length > 0) {
                    Aspect aspect = aspectArray[level.random.nextInt(aspectArray.length)];
                    if (workingAspects.getAmount(aspect) > 0) {
                        workingAspects.remove(aspect, 1);
                        ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect);
                        if (!crystal.isEmpty()) {
                            ItemEntity crystalEntity;
                            if (enchantments.contains(EnumInfusionEnchantment.COLLECTOR)) {
                                crystalEntity = new EntityFollowingItem(
                                        level,
                                        event.getEntity().getX(),
                                        event.getEntity().getY() + event.getEntity().getEyeHeight(),
                                        event.getEntity().getZ(),
                                        crystal, player, 10);
                            } else {
                                crystalEntity = new ItemEntity(
                                        level,
                                        event.getEntity().getX(),
                                        event.getEntity().getY() + event.getEntity().getEyeHeight(),
                                        event.getEntity().getZ(),
                                        crystal);
                            }
                            event.getDrops().add(crystalEntity);
                        }
                        ++b;
                    }
                    aspectArray = workingAspects.getAspects();
                    if (level.random.nextInt(enchantLevel) == 0) {
                        b += 1 + level.random.nextInt(2);
                    }
                }
            }
        }
    }
    private static boolean isToolEffective(Level level, BlockPos pos, ItemStack tool) {
        if (tool.isEmpty()) return false;
        BlockState state = level.getBlockState(pos);
        if (tool.getItem() instanceof DiggerItem digger) {
            return digger.isCorrectToolForDrops(tool, state) ||
                    tool.getDestroySpeed(state) > 1.0f;
        }
        return tool.getDestroySpeed(state) > 1.0f;
    }
}
