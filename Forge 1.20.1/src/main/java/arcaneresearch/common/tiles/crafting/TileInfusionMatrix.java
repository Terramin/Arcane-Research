package arcaneresearch.common.tiles.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IAspectSource;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.IInfusionStabiliser;
import arcaneresearch.api.crafting.IInfusionStabiliserExt;
import arcaneresearch.api.items.IGogglesDisplayExtended;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.blocks.basic.BlockPillarTC;
import arcaneresearch.common.blocks.devices.BlockPedestal;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.events.EssentiaHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockArc;
import arcaneresearch.common.lib.network.fx.PacketFXInfusionSource;
import arcaneresearch.common.lib.network.fx.PacketFXInfusionStream;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.lib.utils.StreamPathHelper;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.tiles.devices.TileStabilizer;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import java.text.DecimalFormat;
import java.util.*;
public class TileInfusionMatrix extends TileArcaneresearch implements IAspectContainer, IGogglesDisplayExtended, arcaneresearch.api.casters.IInteractWithCaster {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#######.##");
    private final List<PendingEssentia> pendingEssentia = new ArrayList<>();
    public boolean active = false;
    public boolean crafting = false;
    public boolean checkSurroundings = true;
    public int stabilityCap = 25;
    public float stability = 0.0f;
    public float stabilityReplenish = 0.0f;
    public float costMult = 1.0f;
    public int craftCount = 0;
    public float startUp = 0.0f;
    public HashMap<String, SourceFX> sourceFX = new HashMap<>();
    private AspectList recipeEssentia = new AspectList();
    private List<ItemStack> recipeIngredients = new ArrayList<>();
    private ItemStack recipeInput = ItemStack.EMPTY;
    private Object recipeOutput = null;
    private String recipeOutputLabel = null;
    private int recipeInstability = 0;
    private int recipeXP = 0;
    private int recipeType = 0;
    private String recipePlayer = null;
    private int count = 0;
    private int cycleTime = 20;
    private int countDelay = 10;
    private int itemCount = 0;
    private int dangerCount = 0;
    private int nextStreamId = 1;
    private List<BlockPos> pedestals = new ArrayList<>();
    private List<BlockPos> problemBlocks = new ArrayList<>();
    private HashMap<Block, Integer> tempBlockCount = new HashMap<>();
    public TileInfusionMatrix(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileInfusionMatrix(BlockPos pos, BlockState state) {
        this(ModBlockEntities.INFUSION_MATRIX.get(), pos, state);
    }
    private static Vec3 jarLid(BlockPos jarPos) {
        return new Vec3(jarPos.getX() + 0.5, jarPos.getY() + 1.0, jarPos.getZ() + 0.5);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileInfusionMatrix tile) {
        tile.count++;
        if (tile.checkSurroundings) {
            tile.checkSurroundings = false;
            tile.scanSurroundings();
        }
        if (tile.count % (tile.crafting ? 20 : 100) == 0 && !tile.validLocation()) {
            tile.active = false;
            tile.setChanged();
            tile.syncTile(false);
            return;
        }
        if (tile.active && !tile.crafting && tile.stability < tile.stabilityCap && tile.count % Math.max(5, tile.countDelay) == 0) {
            tile.stability += Math.max(0.1f, tile.stabilityReplenish);
            if (tile.stability > tile.stabilityCap) {
                tile.stability = tile.stabilityCap;
            }
            tile.setChanged();
            tile.syncTile(false);
        }
        if (tile.active && tile.crafting && tile.count % tile.countDelay == 0) {
            tile.craftCycle();
            tile.setChanged();
        }
        if (tile.crafting && !tile.pendingEssentia.isEmpty()) {
            tile.settleEssentiaArrivals();
            tile.replanStreams();
        }
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileInfusionMatrix tile) {
        tile.doEffects();
    }
    private static boolean pathsEqual(List<Vec3> a, List<Vec3> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) return false;
        }
        return true;
    }
    private EnumStability getStability() {
        if (stability > stabilityCap / 2) return EnumStability.VERY_STABLE;
        if (stability >= 0.0f) return EnumStability.STABLE;
        if (stability > -25.0f) return EnumStability.UNSTABLE;
        return EnumStability.VERY_UNSTABLE;
    }
    private float getModFromCurrentStability() {
        return switch (getStability()) {
            case VERY_STABLE -> 5.0f;
            case STABLE -> 6.0f;
            case UNSTABLE -> 7.0f;
            case VERY_UNSTABLE -> 8.0f;
        };
    }
    private float getLossPerCycle() {
        return recipeInstability / getModFromCurrentStability();
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("Active", active);
        tag.putBoolean("Crafting", crafting);
        tag.putFloat("Stability", stability);
        tag.putInt("RecipeInstability", recipeInstability);
        recipeEssentia.writeToNBT(tag);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        active = tag.getBoolean("Active");
        crafting = tag.getBoolean("Crafting");
        stability = tag.getFloat("Stability");
        recipeInstability = tag.getInt("RecipeInstability");
        recipeEssentia.readFromNBT(tag);
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
            ListTag ingredientList = new ListTag();
            for (ItemStack stack : recipeIngredients) {
                if (!stack.isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    stack.save(itemTag);
                    ingredientList.add(itemTag);
                }
            }
            tag.put("RecipeIngredients", ingredientList);
        }
        if (!recipeInput.isEmpty()) {
            tag.put("RecipeInput", recipeInput.save(new CompoundTag()));
        }
        if (recipeOutput != null && recipeOutput instanceof ItemStack outStack) {
            tag.putString("RecipeOutputType", "@");
            tag.put("RecipeOutput", outStack.save(new CompoundTag()));
        } else if (recipeOutput != null && recipeOutput instanceof net.minecraft.nbt.Tag outTag
                && recipeOutputLabel != null) {
            tag.putString("RecipeOutputType", recipeOutputLabel);
            tag.put("RecipeOutput", outTag.copy());
        } else if (recipeOutputLabel != null) {
            tag.putString("RecipeOutputType", recipeOutputLabel);
        }
        if (recipePlayer != null) {
            tag.putString("RecipePlayer", recipePlayer);
        }
        tag.putInt("RecipeType", recipeType);
        tag.putInt("RecipeXP", recipeXP);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        recipeIngredients = new ArrayList<>();
        if (tag.contains("RecipeIngredients")) {
            ListTag ingredientList = tag.getList("RecipeIngredients", 10);
            for (int i = 0; i < ingredientList.size(); i++) {
                recipeIngredients.add(ItemStack.of(ingredientList.getCompound(i)));
            }
        }
        if (tag.contains("RecipeInput")) {
            recipeInput = ItemStack.of(tag.getCompound("RecipeInput"));
        }
        String outputType = tag.getString("RecipeOutputType");
        if (outputType.equals("@") && tag.contains("RecipeOutput")) {
            recipeOutput = ItemStack.of(tag.getCompound("RecipeOutput"));
            recipeOutputLabel = null;
        } else if (!outputType.isEmpty() && tag.contains("RecipeOutput")) {
            recipeOutputLabel = outputType;
            recipeOutput = tag.get("RecipeOutput").copy();
        } else if (!outputType.isEmpty()) {
            recipeOutputLabel = outputType;
        }
        recipePlayer = tag.getString("RecipePlayer");
        if (recipePlayer.isEmpty()) {
            recipePlayer = null;
        }
        recipeType = tag.getInt("RecipeType");
        recipeXP = tag.getInt("RecipeXP");
    }
    @OnlyIn(Dist.CLIENT)
    private void doEffects() {
        if (crafting) {
            if (craftCount == 0) {
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        arcaneresearch.init.ModSounds.INFUSER_START.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            } else if (craftCount % 65 == 0) {
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        arcaneresearch.init.ModSounds.INFUSER.get(), SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            craftCount++;
            FXDispatcher.INSTANCE.blockRunes(worldPosition.getX(), worldPosition.getY() - 2, worldPosition.getZ(),
                    0.5f + level.random.nextFloat() * 0.2f, 0.1f, 0.7f + level.random.nextFloat() * 0.3f, 25, -0.03f);
        } else if (craftCount > 0) {
            craftCount -= 2;
            if (craftCount < 0) craftCount = 0;
            if (craftCount > 50) craftCount = 50;
        }
        if (active && startUp < 1.0f) {
            startUp += Math.max(startUp / 10.0f, 0.001f);
            if (startUp > 0.999f) startUp = 1.0f;
        }
        if (!active && startUp > 0.0f) {
            startUp -= startUp / 10.0f;
            if (startUp < 0.001f) startUp = 0.0f;
        }
        for (String fxk : sourceFX.keySet().toArray(new String[0])) {
            SourceFX fx = sourceFX.get(fxk);
            if (fx.ticks <= 0) {
                sourceFX.remove(fxk);
            } else {
                fx.ticks--;
                sourceFX.put(fxk, fx);
            }
        }
        if (level.isClientSide) {
            spawnSourceFX();
        }
        if (crafting && stability < 0.0f && level.random.nextInt(250) <= Math.abs(stability)) {
            FXDispatcher.INSTANCE.spark(worldPosition.getX() + level.random.nextFloat(),
                    worldPosition.getY() + level.random.nextFloat(), worldPosition.getZ() + level.random.nextFloat(),
                    3.0f + level.random.nextFloat() * 2.0f, 0.7f + level.random.nextFloat() * 0.1f, 0.1f,
                    0.65f + level.random.nextFloat() * 0.1f, 0.8f);
        }
        if (active && !problemBlocks.isEmpty() && level.random.nextInt(25) == 0) {
            BlockPos p = problemBlocks.get(level.random.nextInt(problemBlocks.size()));
            FXDispatcher.INSTANCE.spark(p.getX() + level.random.nextFloat(), p.getY() + level.random.nextFloat(),
                    p.getZ() + level.random.nextFloat(), 2.0f + level.random.nextFloat(),
                    0.7f + level.random.nextFloat() * 0.1f, 0.1f, 0.65f + level.random.nextFloat() * 0.1f, 0.8f);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnSourceFX() {
        if (level == null) {
            return;
        }
        for (String fxk : sourceFX.keySet().toArray(new String[0])) {
            SourceFX fx = sourceFX.get(fxk);
            if (fx == null || fx.ticks <= 0) {
                continue;
            }
            if (fx.loc.equals(worldPosition)) {
                net.minecraft.world.entity.Entity target = level.getEntity(fx.color);
                if (target != null) {
                    for (int a = 0; a < 4; ++a) {
                        FXDispatcher.INSTANCE.drawInfusionParticles4(
                                target.getX() + (level.random.nextFloat() - level.random.nextFloat()) * target.getBbWidth(),
                                target.getBoundingBox().minY + level.random.nextFloat() * target.getBbHeight(),
                                target.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * target.getBbWidth(),
                                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                    }
                }
            } else {
                BlockEntity tile = level.getBlockEntity(fx.loc);
                if (tile instanceof TilePedestal pedestal) {
                    ItemStack stack = pedestal.getItem(0);
                    if (stack != null && !stack.isEmpty()) {
                        if (level.random.nextInt(3) == 0) {
                            FXDispatcher.INSTANCE.drawInfusionParticles3(
                                    fx.loc.getX() + level.random.nextFloat(),
                                    fx.loc.getY() + level.random.nextFloat() + 1.0f,
                                    fx.loc.getZ() + level.random.nextFloat(),
                                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                        } else if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                            for (int a2 = 0; a2 < 4; ++a2) {
                                FXDispatcher.INSTANCE.drawInfusionParticles2(
                                        fx.loc.getX() + level.random.nextFloat(),
                                        fx.loc.getY() + level.random.nextFloat() + 1.0f,
                                        fx.loc.getZ() + level.random.nextFloat(),
                                        worldPosition, blockItem.getBlock().defaultBlockState(),
                                        stack.getDamageValue());
                            }
                        } else {
                            for (int a2 = 0; a2 < 4; ++a2) {
                                FXDispatcher.INSTANCE.drawInfusionParticles1(
                                        fx.loc.getX() + 0.4f + level.random.nextFloat() * 0.2f,
                                        fx.loc.getY() + 1.23f + level.random.nextFloat() * 0.2f,
                                        fx.loc.getZ() + 0.4f + level.random.nextFloat() * 0.2f,
                                        worldPosition, stack);
                            }
                        }
                    }
                } else {
                    fx.ticks = 0;
                }
            }
        }
    }
    public boolean validLocation() {
        if (level == null) return false;
        BlockState below2 = level.getBlockState(worldPosition.below(2));
        if (!(below2.getBlock() instanceof BlockPedestal)) {
            return false;
        }
        BlockPos[] pillarPositions = {
                worldPosition.offset(-1, -2, -1),
                worldPosition.offset(1, -2, -1),
                worldPosition.offset(1, -2, 1),
                worldPosition.offset(-1, -2, 1)
        };
        for (BlockPos pillarPos : pillarPositions) {
            BlockState pillarState = level.getBlockState(pillarPos);
            if (!(pillarState.getBlock() instanceof BlockPillarTC)) {
                return false;
            }
        }
        return true;
    }
    public void startCrafting(Player player) {
        if (!validLocation()) {
            active = false;
            syncTile(false);
            return;
        }
        scanSurroundings();
        BlockEntity centerTE = level.getBlockEntity(worldPosition.below(2));
        recipeInput = ItemStack.EMPTY;
        if (centerTE instanceof TilePedestal pedestal) {
            ItemStack centerItem = pedestal.getItem(0);
            if (!centerItem.isEmpty()) {
                recipeInput = centerItem.copy();
            }
        }
        if (recipeInput.isEmpty()) return;
        List<ItemStack> components = new ArrayList<>();
        for (BlockPos pedestalPos : pedestals) {
            BlockEntity te = level.getBlockEntity(pedestalPos);
            if (te instanceof TilePedestal pedestal) {
                ItemStack stack = pedestal.getItem(0);
                if (!stack.isEmpty()) {
                    components.add(stack.copy());
                }
            }
        }
        if (components.isEmpty()) return;
        InfusionRecipeType recipe = ArcaneresearchCraftingManager.findMatchingInfusionRecipe(
                components, recipeInput, player, level);
        if (costMult < 0.5f) costMult = 0.5f;
        if (recipe != null) {
            recipeType = 0;
            recipeIngredients = new ArrayList<>(components);
            if (recipe instanceof arcaneresearch.common.lib.crafting.LegacyInfusionRecipeAdapter legacyAdapter) {
                Object legacyOut = legacyAdapter.getLegacyOutput(player, recipeInput, components);
                if (legacyOut instanceof Object[] pair && pair.length >= 2 && pair[0] instanceof String label) {
                    recipeOutputLabel = label;
                    recipeOutput = pair[1];
                } else {
                    recipeOutputLabel = null;
                    recipeOutput = legacyOut;
                }
            } else {
                recipeOutputLabel = null;
                recipeOutput = recipe.getRecipeOutput(player, recipeInput, components);
            }
            recipeInstability = recipe.getInstability(player, recipeInput, components);
            AspectList al = recipe.getAspects(player, recipeInput, components);
            AspectList al2 = new AspectList();
            for (Aspect as : al.getAspects()) {
                int adjusted = (int) (al.getAmount(as) * costMult);
                if (adjusted > 0) {
                    al2.add(as, adjusted);
                }
            }
            recipeEssentia = al2;
            recipePlayer = player.getStringUUID();
            crafting = true;
            level.playSound(null, worldPosition, arcaneresearch.init.ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            syncTile(false);
            setChanged();
        }
    }
    private int countInFlight(Aspect aspect) {
        int n = 0;
        for (PendingEssentia p : pendingEssentia) {
            if (p.aspect == aspect) n++;
        }
        return n;
    }
    private void settleEssentiaArrivals() {
        if (pendingEssentia.isEmpty()) return;
        Iterator<PendingEssentia> it = pendingEssentia.iterator();
        while (it.hasNext()) {
            PendingEssentia p = it.next();
            if (++p.elapsed < p.duration) continue;
            it.remove();
            creditPending(p);
        }
    }
    private void creditPending(PendingEssentia p) {
        boolean ok = false;
        BlockEntity src = level.getBlockEntity(p.source);
        if (src instanceof IAspectSource as && !as.isBlocked()) {
            ok = as.takeFromContainer(p.aspect, 1);
        }
        if (ok) {
            recipeEssentia.reduce(p.aspect, 1);
        } else {
            stability -= 0.25f;
        }
        syncTile(false);
        setChanged();
    }
    private void sendStreamPacket(PendingEssentia p) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketHandler.sendToAllTrackingChunk(
                    new PacketFXInfusionStream(p.streamId, p.source, worldPosition,
                            p.aspect.getColor(), p.duration, p.startTick,
                            level.getGameTime(), p.path),
                    serverLevel, worldPosition);
        }
    }
    private void killStream(PendingEssentia p) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketHandler.sendToAllTrackingChunk(
                    new PacketFXInfusionStream(p.streamId, p.source, worldPosition,
                            0, 0, p.startTick, level.getGameTime(), new ArrayList<>()),
                    serverLevel, worldPosition);
        }
    }
    private void replanStreams() {
        if (pendingEssentia.isEmpty() || level.getGameTime() % 20 != 0) return;
        Vec3 matrix = Vec3.atCenterOf(worldPosition);
        Iterator<PendingEssentia> it = pendingEssentia.iterator();
        while (it.hasNext()) {
            PendingEssentia p = it.next();
            BlockEntity src = level.getBlockEntity(p.source);
            if (!(src instanceof IAspectSource as) || as.isBlocked()) {
                it.remove();
                killStream(p);
                stability -= 0.25f;
                syncTile(false);
                setChanged();
                continue;
            }
            List<Vec3> fresh = StreamPathHelper.findPath(level, jarLid(p.source), matrix);
            if (pathsEqual(fresh, p.path)) continue;
            StreamPathHelper.Blockage blockage = StreamPathHelper.firstBlockedCell(level, p.path);
            if (blockage == null) {
                adoptPath(p, fresh);
                if (p.elapsed >= p.duration) {
                    it.remove();
                    creditPending(p);
                }
                continue;
            }
            double head = StreamPathHelper.headDistance(p.pathLen, p.duration, p.elapsed);
            if (head > blockage.dist() + 0.5) {
                it.remove();
                creditPending(p);
                killStream(p);
                continue;
            }
            adoptPath(p, fresh);
        }
    }
    private void adoptPath(PendingEssentia p, List<Vec3> fresh) {
        p.path = fresh;
        p.pathLen = StreamPathHelper.pathLength(fresh);
        p.duration = Math.max(1, (int) (p.pathLen * 21.0));
        sendStreamPacket(p);
    }
    private void craftCycle() {
        if (level == null) {
            cancelCrafting();
            return;
        }
        boolean valid = false;
        float ff = level.random.nextFloat() * getLossPerCycle();
        stability -= ff;
        stability += stabilityReplenish;
        stability = Math.max(-100, Math.min(stabilityCap, stability));
        BlockEntity centerTE = level.getBlockEntity(worldPosition.below(2));
        if (centerTE instanceof TilePedestal pedestal && !pedestal.getItem(0).isEmpty()) {
            ItemStack i2 = pedestal.getItem(0).copy();
            if (recipeInput.getDamageValue() == 32767) {
                i2.setDamageValue(32767);
            }
            if (ArcaneresearchInvHelper.areItemStacksEqualForCrafting(i2, recipeInput)) {
                valid = true;
            }
        }
        if (!valid || (stability < 0.0f && level.random.nextInt(1500) <= Math.abs(stability))) {
            triggerInstabilityEvent();
            stability += 5.0f + level.random.nextFloat() * 5.0f;
            grantInstabilityResearch();
            if (valid) return;
        }
        if (!valid) {
            cancelCrafting();
            return;
        }
        if (countDelay < 1) countDelay = 1;
        if (recipeEssentia.visSize() > 0) {
            Aspect[] all = recipeEssentia.getAspectsSortedByName();
            for (int k = 0; k < all.length; k++) {
                Aspect aspect = all[k];
                int na = recipeEssentia.getAmount(aspect);
                if (na <= 0) continue;
                if (countInFlight(aspect) >= na) continue;
                BlockPos srcPos = EssentiaHandler.findEssentiaSourcePos(this, aspect, null, 12, false);
                if (srcPos != null) {
                    List<Vec3> path = StreamPathHelper.findPath(level,
                            jarLid(srcPos), Vec3.atCenterOf(worldPosition));
                    double pathLen = StreamPathHelper.pathLength(path);
                    int travel = Math.max(1, (int) (pathLen * 21.0));
                    PendingEssentia pending = new PendingEssentia(aspect, srcPos,
                            nextStreamId++, level.getGameTime(), path, pathLen, travel);
                    pendingEssentia.add(pending);
                    sendStreamPacket(pending);
                    return;
                }
                stability -= 0.25f;
                syncTile(false);
                setChanged();
            }
            checkSurroundings = true;
            return;
        }
        if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
            for (int a = 0; a < recipeIngredients.size(); a++) {
                for (BlockPos cc : pedestals) {
                    BlockEntity te = level.getBlockEntity(cc);
                    if (te instanceof TilePedestal ped && !ped.getItem(0).isEmpty()) {
                        if (ArcaneresearchInvHelper.areItemStacksEqualForCrafting(ped.getItem(0), recipeIngredients.get(a))) {
                            if (itemCount == 0) {
                                itemCount = 10;
                                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                    PacketHandler.sendToAllTrackingChunk(
                                            new PacketFXInfusionSource(worldPosition, cc, 0),
                                            serverLevel, worldPosition);
                                }
                            } else if (itemCount-- <= 1) {
                                ItemStack container = ped.getItem(0).getItem().getCraftingRemainingItem(ped.getItem(0));
                                ped.setItem(0, container == null || container.isEmpty() ? ItemStack.EMPTY : container.copy());
                                te.setChanged();
                                ped.syncTile(false);
                                recipeIngredients.remove(a);
                                setChanged();
                            }
                            return;
                        }
                    }
                }
                Aspect[] ingEss = recipeEssentia.getAspects();
                if (ingEss != null && ingEss.length > 0 && level.random.nextInt(1 + a) == 0) {
                    Aspect as = ingEss[level.random.nextInt(ingEss.length)];
                    recipeEssentia.add(as, 1);
                    stability -= 0.25f;
                    syncTile(false);
                    setChanged();
                }
            }
            return;
        }
        crafting = false;
        craftingFinish();
        recipeOutput = null;
        syncTile(false);
        setChanged();
    }
    private void cancelCrafting() {
        crafting = false;
        recipeEssentia = new AspectList();
        recipeInstability = 0;
        for (PendingEssentia p : pendingEssentia) {
            killStream(p);
        }
        pendingEssentia.clear();
        level.playSound(null, worldPosition, arcaneresearch.init.ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 1.0f, 0.6f);
        syncTile(false);
    }
    private void craftingFinish() {
        BlockEntity te = level.getBlockEntity(worldPosition.below(2));
        if (te instanceof TilePedestal pedestal) {
            float dmg = 1.0f;
            if (recipeOutput instanceof ItemStack outStack) {
                ItemStack qs = outStack.copy();
                ItemStack inputStack = pedestal.getItem(0);
                if (inputStack.isDamageableItem() && inputStack.isDamaged()) {
                    dmg = inputStack.getDamageValue() / (float) inputStack.getMaxDamage();
                    if (qs.isDamageableItem() && !qs.isDamaged()) {
                        qs.setDamageValue((int) (qs.getMaxDamage() * dmg));
                    }
                }
                pedestal.setItemFromInfusion(qs);
            } else if (recipeOutput instanceof net.minecraft.nbt.Tag tag && recipeOutputLabel != null) {
                ItemStack temp = pedestal.getItem(0);
                if (!temp.isEmpty()) {
                    temp.getOrCreateTag().put(recipeOutputLabel, tag.copy());
                    syncTile(false);
                    te.setChanged();
                }
            } else if (recipeOutput instanceof Enchantment ench) {
                ItemStack temp = pedestal.getItem(0);
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(temp);
                int currentLevel = enchantments.getOrDefault(ench, 0);
                enchantments.put(ench, currentLevel + 1);
                EnchantmentHelper.setEnchantments(enchantments, temp);
                syncTile(false);
                te.setChanged();
            }
            if (recipePlayer != null && !recipePlayer.isEmpty()) {
                Player p = null;
                try {
                    p = level.getPlayerByUUID(UUID.fromString(recipePlayer));
                } catch (IllegalArgumentException ignored) {
                }
                if (p == null) {
                    for (Player pl : level.players()) {
                        if (pl.getName().getString().equals(recipePlayer)) {
                            p = pl;
                            break;
                        }
                    }
                }
                if (p != null) {
                    try {
                        MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(p, pedestal.getItem(0), null));
                    } catch (Exception ignored) {
                    }
                }
            }
            recipeEssentia = new AspectList();
            recipeInstability = 0;
            syncTile(false);
            setChanged();
            level.blockEvent(worldPosition.below(2), level.getBlockState(worldPosition.below(2)).getBlock(), 12, 0);
            level.playSound(null, worldPosition, arcaneresearch.init.ModSounds.WAND.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }
    private void triggerInstabilityEvent() {
        int event = level.random.nextInt(24);
        switch (event) {
            case 0, 1, 2, 3 -> instabilityEjectItem(0);
            case 4, 5, 6 -> instabilityWarp();
            case 7, 8, 9 -> instabilityZap(false);
            case 10, 11 -> instabilityZap(true);
            case 12, 13 -> instabilityEjectItem(1);
            case 14, 15 -> instabilityEjectItem(2);
            case 16 -> instabilityEjectItem(3);
            case 17 -> instabilityEjectItem(4);
            case 18, 19 -> instabilityHarm(false);
            case 20, 21 -> instabilityEjectItem(5);
            case 22 -> instabilityHarm(true);
            case 23 -> level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, 1.5f + level.random.nextFloat(), false, Level.ExplosionInteraction.BLOCK);
        }
    }
    private void instabilityZap(boolean all) {
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(worldPosition).inflate(10.0));
        for (LivingEntity target : targets) {
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                PacketHandler.sendToAllTrackingChunk(
                        new PacketFXBlockArc(worldPosition, target, 0.3f - level.random.nextFloat() * 0.1f,
                                0.0f, 0.3f - level.random.nextFloat() * 0.1f),
                        serverLevel, worldPosition);
            }
            target.hurt(level.damageSources().magic(), 4 + level.random.nextInt(4));
            if (!all) break;
        }
    }
    private void instabilityHarm(boolean all) {
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(worldPosition).inflate(10.0));
        for (LivingEntity target : targets) {
            if (level.random.nextBoolean()) {
                if (ModEffects.FLUX_TAINT != null) {
                    target.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT.get(), 120, 0, false, true));
                }
            } else {
                if (ModEffects.VIS_EXHAUST != null) {
                    MobEffectInstance pe = new MobEffectInstance(ModEffects.VIS_EXHAUST.get(), 2400, 0, true, true);
                    pe.getCurativeItems().clear();
                    target.addEffect(pe);
                }
            }
            if (!all) break;
        }
    }
    private void instabilityWarp() {
        List<Player> targets = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(10.0));
        if (!targets.isEmpty()) {
            Player target = targets.get(level.random.nextInt(targets.size()));
            if (level.random.nextFloat() < 0.25f) {
                arcaneresearch.api.ArcaneresearchApi.internalMethods.addWarpToPlayer(target, 1, IPlayerWarp.EnumWarpType.NORMAL);
            } else {
                arcaneresearch.api.ArcaneresearchApi.internalMethods.addWarpToPlayer(target, 2 + level.random.nextInt(4), IPlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
    }
    private void instabilityEjectItem(int type) {
        for (int retries = 0; retries < 25 && !pedestals.isEmpty(); retries++) {
            BlockPos cc = pedestals.get(level.random.nextInt(pedestals.size()));
            BlockEntity te = level.getBlockEntity(cc);
            if (te instanceof TilePedestal ped && !ped.getItem(0).isEmpty()) {
                BlockPos stabPos = ped.findInstabilityMitigator();
                if (stabPos != null) {
                    BlockEntity stabilizerTe = level.getBlockEntity(stabPos);
                    if (stabilizerTe instanceof TileStabilizer stabilizer
                            && stabilizer.mitigate(5 + level.random.nextInt(6))) {
                        level.blockEvent(cc, level.getBlockState(cc).getBlock(), 5, 0);
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            PacketHandler.sendToAllTrackingChunk(
                                    new PacketFXBlockArc(worldPosition, cc.above(),
                                            0.3f - level.random.nextFloat() * 0.1f, 0.0f,
                                            0.3f - level.random.nextFloat() * 0.1f),
                                    serverLevel, worldPosition);
                            PacketHandler.sendToAllTrackingChunk(
                                    new PacketFXBlockArc(cc.above(), stabPos,
                                            0.3f - level.random.nextFloat() * 0.1f, 0.0f,
                                            0.3f - level.random.nextFloat() * 0.1f),
                                    serverLevel, cc);
                        }
                        return;
                    }
                }
                if (type <= 3 || type == 5) {
                    InventoryUtils.dropItems(level, cc);
                } else {
                    ped.setItem(0, ItemStack.EMPTY);
                }
                te.setChanged();
                ped.syncTile(false);
                if (type == 1 || type == 3) {
                    if (ModBlocks.FLUX_GOO != null) {
                        level.setBlockAndUpdate(cc.above(), ModBlocks.FLUX_GOO.get().defaultBlockState());
                    }
                    level.playSound(null, cc, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.3f, 1.0f);
                } else if (type == 2 || type == 4) {
                    AuraHelper.polluteAura(level, cc, 5 + level.random.nextInt(5), true);
                } else if (type == 5) {
                    level.explode(null, cc.getX() + 0.5f, cc.getY() + 0.5f, cc.getZ() + 0.5f,
                            1.0f, false, Level.ExplosionInteraction.BLOCK);
                }
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    PacketHandler.sendToAllTrackingChunk(
                            new PacketFXBlockArc(worldPosition, cc.above(), 0.3f - level.random.nextFloat() * 0.1f,
                                    0.0f, 0.3f - level.random.nextFloat() * 0.1f),
                            serverLevel, worldPosition);
                }
                level.blockEvent(cc, level.getBlockState(cc).getBlock(), 11, 0);
                return;
            }
        }
    }
    private void grantInstabilityResearch() {
        List<Player> targets = level.getEntitiesOfClass(Player.class,
                new AABB(worldPosition).inflate(10.0));
        for (Player player : targets) {
            if (!ArcaneresearchCapabilities.knowsResearch(player, "!INSTABILITY")) {
                ResearchManager.completeResearch(player, "!INSTABILITY");
                player.displayClientMessage(
                        Component.translatable("got.instability").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE),
                        true);
            }
        }
    }
    private void scanSurroundings() {
        Set<Long> stuff = new HashSet<>();
        pedestals.clear();
        tempBlockCount.clear();
        problemBlocks.clear();
        cycleTime = 10;
        stabilityReplenish = 0.0f;
        costMult = 1.0f;
        try {
            for (int xx = -8; xx <= 8; xx++) {
                for (int zz = -8; zz <= 8; zz++) {
                    for (int yy = -3; yy <= 7; yy++) {
                        if (xx == 0 && zz == 0) continue;
                        BlockPos bp = worldPosition.offset(xx, -yy, zz);
                        Block bi = level.getBlockState(bp).getBlock();
                        if (bi instanceof BlockPedestal) {
                            pedestals.add(bp);
                        }
                        try {
                            if (bi instanceof IInfusionStabiliser stabiliser) {
                                if (stabiliser.canStabaliseInfusion(level, bp)) {
                                    stuff.add(bp.asLong());
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        if (bi == Blocks.SKELETON_SKULL || bi == Blocks.WITHER_SKELETON_SKULL ||
                                bi == Blocks.ZOMBIE_HEAD || bi == Blocks.CREEPER_HEAD || bi == Blocks.PLAYER_HEAD ||
                                bi == Blocks.DRAGON_HEAD ||
                                bi == Blocks.CANDLE || bi == Blocks.CANDLE_CAKE ||
                                bi == Blocks.WHITE_CANDLE || bi == Blocks.ORANGE_CANDLE || bi == Blocks.MAGENTA_CANDLE ||
                                bi == Blocks.LIGHT_BLUE_CANDLE || bi == Blocks.YELLOW_CANDLE || bi == Blocks.LIME_CANDLE ||
                                bi == Blocks.PINK_CANDLE || bi == Blocks.GRAY_CANDLE || bi == Blocks.LIGHT_GRAY_CANDLE ||
                                bi == Blocks.CYAN_CANDLE || bi == Blocks.PURPLE_CANDLE || bi == Blocks.BLUE_CANDLE ||
                                bi == Blocks.BROWN_CANDLE || bi == Blocks.GREEN_CANDLE || bi == Blocks.RED_CANDLE ||
                                bi == Blocks.BLACK_CANDLE) {
                            stuff.add(bp.asLong());
                        }
                    }
                }
            }
            while (!stuff.isEmpty()) {
                Long[] posArray = stuff.toArray(new Long[0]);
                if (posArray.length == 0 || posArray[0] == null) break;
                long lp = posArray[0];
                BlockPos c1 = BlockPos.of(lp);
                int x2 = worldPosition.getX() - c1.getX();
                int z2 = worldPosition.getZ() - c1.getZ();
                int x3 = worldPosition.getX() + x2;
                int z3 = worldPosition.getZ() + z2;
                BlockPos c2 = new BlockPos(x3, c1.getY(), z3);
                try {
                    Block sb1 = level.getBlockState(c1).getBlock();
                    Block sb2 = level.getBlockState(c2).getBlock();
                    float amt1 = getStabilizationAmount(sb1, c1);
                    float amt2 = getStabilizationAmount(sb2, c2);
                    boolean hasCustomPenalty = false;
                    float customPenalty = 0;
                    if (sb1 instanceof IInfusionStabiliserExt ext1) {
                        if (ext1.hasSymmetryPenalty(level, c1, c2)) {
                            hasCustomPenalty = true;
                            customPenalty = ext1.getSymmetryPenalty(level, c1);
                        }
                    }
                    if (sb1 == sb2 && amt1 == amt2 && !hasCustomPenalty) {
                        stabilityReplenish += calcDiminishingReturns(sb1, amt1);
                    } else if (hasCustomPenalty) {
                        stabilityReplenish -= customPenalty;
                        problemBlocks.add(c1);
                    } else {
                        stabilityReplenish -= Math.max(amt1, amt2);
                        problemBlocks.add(c1);
                    }
                    stuff.remove(c2.asLong());
                } catch (Exception ignored) {
                }
                stuff.remove(lp);
            }
            BlockPos[] pillars = {
                    worldPosition.offset(-1, -2, -1),
                    worldPosition.offset(1, -2, -1),
                    worldPosition.offset(1, -2, 1),
                    worldPosition.offset(-1, -2, 1)
            };
            boolean allAncient = true;
            boolean allEldritch = true;
            for (BlockPos pillarPos : pillars) {
                Block pillar = level.getBlockState(pillarPos).getBlock();
                if (!pillar.equals(ModBlocks.ANCIENT_PILLAR.get())) allAncient = false;
                if (!pillar.equals(ModBlocks.ELDRITCH_PILLAR.get())) allEldritch = false;
            }
            if (allAncient) {
                cycleTime--;
                costMult -= 0.1f;
                stabilityReplenish -= 0.1f;
            }
            if (allEldritch) {
                cycleTime -= 3;
                costMult += 0.05f;
                stabilityReplenish += 0.2f;
            }
            int[] xm = {-1, 1, 1, -1};
            int[] zm = {-1, -1, 1, 1};
            for (int a = 0; a < 4; ++a) {
                Block b = level.getBlockState(worldPosition.offset(xm[a], -3, zm[a])).getBlock();
                if (b.equals(ModBlocks.MATRIX_SPEED.get())) {
                    --cycleTime;
                    costMult += 0.01f;
                }
                if (b.equals(ModBlocks.MATRIX_COST.get())) {
                    ++cycleTime;
                    costMult -= 0.02f;
                }
            }
            for (BlockPos cc : pedestals) {
                Block bb = level.getBlockState(cc).getBlock();
                if (bb.equals(ModBlocks.PEDESTAL_ELDRITCH.get())) costMult += 0.0025f;
                if (bb.equals(ModBlocks.PEDESTAL_ANCIENT.get())) costMult -= 0.01f;
            }
            countDelay = cycleTime / 2;
        } catch (Exception e) {
        }
    }
    private float getStabilizationAmount(Block block, BlockPos pos) {
        if (block instanceof IInfusionStabiliserExt ext) {
            return ext.getStabilizationAmount(level, pos);
        }
        return 0.1f;
    }
    private float calcDiminishingReturns(Block b, float base) {
        float bb = base;
        int c = tempBlockCount.getOrDefault(b, 0);
        if (c > 0) {
            bb *= (float) Math.pow(0.75, c);
        }
        tempBlockCount.put(b, c + 1);
        return bb;
    }
    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player,
                                      BlockPos pos, Direction side, InteractionHand hand) {
        if (level.isClientSide && active && !crafting) {
            checkSurroundings = true;
        }
        if (!level.isClientSide && active && !crafting) {
            startCrafting(player);
            return true;
        }
        if (!level.isClientSide && !active && validLocation()) {
            level.playSound(null, pos, arcaneresearch.init.ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            active = true;
            syncTile(false);
            setChanged();
            return true;
        }
        return false;
    }
    @Override
    public AspectList getAspects() {
        return recipeEssentia;
    }
    @Override
    public void setAspects(AspectList aspects) {
    }
    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }
    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }
    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }
    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }
    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }
    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }
    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }
    @Override
    public String[] getIGogglesText() {
        return getGogglesText();
    }
    @Override
    public net.minecraft.world.phys.Vec3 getIGogglesTextOffset() {
        return new net.minecraft.world.phys.Vec3(0, 1.5, 0);
    }
    public String[] getGogglesText() {
        float lpc = getLossPerCycle();
        if (lpc != 0.0f) {
            return new String[]{
                    "stability." + getStability().name(),
                    FORMATTER.format(stabilityReplenish) + " stability.gain",
                    "stability.range " + FORMATTER.format(lpc) + " stability.loss"
            };
        }
        return new String[]{
                "stability." + getStability().name(),
                FORMATTER.format(stabilityReplenish) + " stability.gain"
        };
    }
    public AABB getCustomRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 0.1, worldPosition.getY() - 0.1, worldPosition.getZ() - 0.1,
                worldPosition.getX() + 1.1, worldPosition.getY() + 1.1, worldPosition.getZ() + 1.1
        );
    }
    private enum EnumStability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE
    }
    private static class PendingEssentia {
        final Aspect aspect;
        final BlockPos source;
        final int streamId;
        final long startTick;
        List<Vec3> path;
        double pathLen;
        int duration;
        int elapsed;
        PendingEssentia(Aspect aspect, BlockPos source, int streamId, long startTick,
                        List<Vec3> path, double pathLen, int duration) {
            this.aspect = aspect;
            this.source = source;
            this.streamId = streamId;
            this.startTick = startTick;
            this.path = path;
            this.pathLen = pathLen;
            this.duration = duration;
            this.elapsed = 0;
        }
    }
    public static class SourceFX {
        public BlockPos loc;
        public int ticks;
        public int color;
        public int entity;
        public SourceFX(BlockPos loc, int ticks, int color) {
            this.loc = loc;
            this.ticks = ticks;
            this.color = color;
        }
    }
}
