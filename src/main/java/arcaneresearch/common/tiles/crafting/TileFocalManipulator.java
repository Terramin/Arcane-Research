package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.casters.FocusModSplit;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TileFocalManipulator extends TileArcaneresearchInventory {
    public float vis = 0.0f;
    public HashMap<Integer, FocusElementNode> data = new HashMap<>();
    public String focusName = "";
    public boolean doGather = false;
    public float visCost = 0.0f;
    public int xpCost = 0;
    public AspectList crystalsSync = new AspectList();
    public boolean doGuiReset = false;

    private int ticks = 0;
    private AspectList crystals = new AspectList();
    private AABB renderBox;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public TileFocalManipulator(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state, 1);
        syncedSlots = new int[]{0};
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileFocalManipulator tile) {
        if (level == null) return;

        tile.ticks++;
        boolean complete = false;

        if (tile.ticks % 20 == 0) {
            ItemStack focus = tile.getItem(0);

            if (tile.vis > 0.0f) {
                if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus)) {
                    complete = true;
                    tile.vis = 0.0f;
                    level.playSound(null, pos, ModSounds.WAND_FAIL.get(), SoundSource.BLOCKS, 0.33f, 1.0f);
                }
            }

            if (!complete && tile.vis > 0.0f) {
                float amt = tile.spendAura(Math.min(20.0f, tile.vis));

                if (amt > 0.0f) {
                    level.blockEvent(pos, state.getBlock(), 5, 1);
                    tile.vis -= amt;
                    tile.syncTile(false);
                    tile.setChanged();
                }

                if (tile.vis <= 0.0f) {
                    focus = tile.getItem(0);

                    if (!focus.isEmpty() && focus.getItem() instanceof ItemFocus) {
                        complete = true;
                        tile.endCraft();
                    }
                }
            }
        }

        if (complete) {
            tile.vis = 0.0f;
            tile.syncTile(false);
            tile.setChanged();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileFocalManipulator tile) {
        if (level == null) return;

        tile.ticks++;

        if (tile.vis > 0.0f) {
            FXDispatcher.INSTANCE.drawGenericParticles(
                    pos.getX() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.3f,
                    pos.getY() + 1.4 + (level.random.nextFloat() - level.random.nextFloat()) * 0.3f,
                    pos.getZ() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.3f,
                    0.0, 0.0, 0.0, 0.5f + level.random.nextFloat() * 0.4f,
                    1.0f - level.random.nextFloat() * 0.4f, 1.0f - level.random.nextFloat() * 0.4f,
                    0.8f, false, 448, 9, 1, 6 + level.random.nextInt(5), 0,
                    0.3f + level.random.nextFloat() * 0.3f, 0.0f, 0);
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putFloat("vis", vis);
        tag.putString("focusName", focusName);
        crystalsSync.writeToNBT(tag, "crystals");

        ListTag nodeList = new ListTag();

        for (FocusElementNode node : data.values()) {
            nodeList.add(node.serialize());
        }

        tag.put("nodes", nodeList);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        vis = tag.getFloat("vis");
        focusName = tag.getString("focusName");

        crystalsSync = new AspectList();
        crystalsSync.readFromNBT(tag, "crystals");

        ListTag nodeList = tag.getList("nodes", Tag.TAG_COMPOUND);
        data.clear();

        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag nodeTag = nodeList.getCompound(i);
            FocusElementNode node = new FocusElementNode();
            node.deserialize(nodeTag);
            data.put(node.id, node);
        }
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        ItemStack prev = getItem(slot);
        super.setItem(slot, stack);

        if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, prev)) return;

        if (level != null && level.isClientSide) {
            data.clear();
            doGuiReset = true;
        } else {
            vis = 0.0f;
            crystalsSync = new AspectList();
            setChanged();
            syncTile(false);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof ItemFocus;
    }

    public float spendAura(float amount) {
        if (level == null) return 0.0f;

        if (level.getBlockState(worldPosition.above()).is(ModBlocks.ARCANE_WORKBENCH_CHARGER.get())) {
            float remaining = amount;
            float perChunk = amount / 9.0f;

            outer:
            for (int xx = -1; xx <= 1; ++xx) {
                for (int zz = -1; zz <= 1; ++zz) {
                    float toDrain = Math.min(perChunk, remaining);

                    mutablePos.set(
                            worldPosition.getX() + xx * 16,
                            worldPosition.getY(),
                            worldPosition.getZ() + zz * 16
                    );

                    remaining -= AuraHandler.drainVis(level, mutablePos, toDrain, false);

                    if (remaining <= 0.0f) break outer;
                }
            }

            return amount - remaining;
        }

        return AuraHandler.drainVis(level, worldPosition, amount, false);
    }

    private FocusPackage generateFocus() {
        if (data == null || data.isEmpty()) return null;

        FocusPackage core = new FocusPackage();
        int totalComplexity = 0;
        HashMap<String, Integer> compCount = new HashMap<>();

        for (FocusElementNode node : data.values()) {
            if (node.node == null) continue;

            int count = compCount.getOrDefault(node.node.getKey(), 0) + 1;
            node.complexityMultiplier = 0.5f * (count + 1);
            compCount.put(node.node.getKey(), count);
            totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);
        }

        core.setComplexity(totalComplexity);

        FocusElementNode root = data.get(0);

        if (root != null) {
            traverseChildren(core, root);
        }

        return core;
    }

    private void traverseChildren(FocusPackage currentPackage, FocusElementNode currentNode) {
        if (currentPackage == null || currentNode == null || currentNode.node == null) return;

        currentPackage.addNode(currentNode.node);

        if (currentNode.children == null || currentNode.children.length == 0) return;

        if (currentNode.children.length == 1) {
            traverseChildren(currentPackage, data.get(currentNode.children[0]));
            return;
        }

        if (currentNode.node instanceof FocusModSplit splitNode) {
            splitNode.getSplitPackages().clear();

            for (int childId : currentNode.children) {
                FocusPackage splitPackage = new FocusPackage();
                traverseChildren(splitPackage, data.get(childId));
                splitNode.getSplitPackages().add(splitPackage);
            }
        }
    }

    public void endCraft() {
        if (level == null) return;

        vis = 0.0f;

        ItemStack focus = getItem(0);

        if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus)) return;

        FocusPackage core = generateFocus();

        if (core == null) return;

        level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        if (focus.hasTag()) {
            assert focus.getTag() != null;
            focus.getTag().remove("color");
        }

        if (!focusName.isEmpty()) {
            focus.setHoverName(net.minecraft.network.chat.Component.literal(focusName));
        }

        ItemFocus.setPackage(focus, core);
        setItem(0, focus);

        crystalsSync = new AspectList();
        data.clear();
        syncTile(false);
        setChanged();
    }

    public boolean startCraft(int id, Player player) {
        if (level == null || level.isClientSide || player == null) return false;
        if (data == null || data.isEmpty() || vis > 0.0f) return false;

        ItemStack focus = getItem(0);

        if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus focusItem)) {
            return false;
        }

        int maxComplexity = focusItem.getMaxComplexity();
        int totalComplexity = 0;

        crystals = new AspectList();
        HashMap<String, Integer> compCount = new HashMap<>();

        for (FocusElementNode node : data.values()) {
            if (node.node == null) return false;
            if (!ArcaneresearchCapabilities.knowsResearchStrict(player, node.node.getResearch())) return false;

            int count = compCount.getOrDefault(node.node.getKey(), 0) + 1;
            node.complexityMultiplier = 0.5f * (count + 1);
            compCount.put(node.node.getKey(), count);
            totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);

            if (node.node.getAspect() != null) {
                crystals.add(node.node.getAspect(), 1);
            }
        }

        vis = (float) (totalComplexity * 10 + maxComplexity / 5);
        xpCost = (int) Math.max(1, Math.round(Math.sqrt(totalComplexity)));

        boolean creative = player.getAbilities().instabuild;

        if (!creative && player.experienceLevel < xpCost) {
            vis = 0.0f;
            return false;
        }

        if (!creative) {
            player.giveExperienceLevels(-xpCost);
        }

        Aspect[] crystalAspects = crystals.getAspects();

        if (crystalAspects.length == 0) {
            vis = 0.0f;
            return false;
        }

        ItemStack[] components = new ItemStack[crystalAspects.length];

        for (int i = 0; i < crystalAspects.length; i++) {
            Aspect aspect = crystalAspects[i];
            components[i] = ArcaneresearchApiHelper.makeCrystal(aspect, crystals.getAmount(aspect));
        }

        if (!creative) {
            for (ItemStack component : components) {
                if (!arcaneresearch.common.lib.utils.InventoryUtils.isPlayerCarryingAmount(player, component, false)) {
                    vis = 0.0f;
                    return false;
                }
            }

            for (ItemStack component : components) {
                arcaneresearch.common.lib.utils.InventoryUtils.consumePlayerItem(player, component, true, false);
            }
        }

        crystalsSync = crystals.copy();
        setChanged();
        syncTile(false);

        level.playSound(null, worldPosition, ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            doGuiReset = true;
        }

        if (id == 5) {
            if (level != null && level.isClientSide) {
                spawnVisSparkle(param);
            }
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnVisSparkle(int param) {
        if (level == null) return;

        FXDispatcher.INSTANCE.visSparkle(
                worldPosition.getX() + level.random.nextInt(3) - level.random.nextInt(3),
                worldPosition.getY() + level.random.nextInt(3),
                worldPosition.getZ() + level.random.nextInt(3) - level.random.nextInt(3),
                worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), param);
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1
            );
        }

        return renderBox;
    }
}