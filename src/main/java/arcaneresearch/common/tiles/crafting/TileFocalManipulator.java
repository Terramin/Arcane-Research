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
import arcaneresearch.api.casters.FocusNode;
import arcaneresearch.api.casters.FocusPackage;
import arcaneresearch.api.casters.IFocusElement;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.wands.FocusManager;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileFocalManipulator extends TileArcaneresearchInventory {
    public AspectList vis = new AspectList();
    public HashMap<Integer, FocusElementNode> data = new HashMap<>();

    public String focusName = "";
    public boolean doGather = false;
    public AspectList visCost = new AspectList();
    public int xpCost = 0;
    public AspectList crystalsSync = new AspectList();
    public boolean doGuiReset = false;

    private UUID crafter;
    private int ticks = 0;
    private AspectList crystals = new AspectList();
    private AABB renderBox;

    public TileFocalManipulator(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state, 1);
        syncedSlots = new int[]{0};
    }

    public boolean hasVisRemaining() {
        return vis != null && vis.visSize() > 0;
    }

    public void clearVis() {
        vis = new AspectList();
        visCost = new AspectList();
        crafter = null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileFocalManipulator tile) {
        if (level == null) return;

        tile.ticks++;
        boolean complete = false;

        if (tile.ticks % 20 == 0) {
            ItemStack focus = tile.getItem(0);

            if (tile.hasVisRemaining()) {
                if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus)) {
                    complete = true;
                    tile.clearVis();
                    level.playSound(null, pos, ModSounds.WAND_FAIL.get(), SoundSource.BLOCKS, 0.33f, 1.0f);
                }
            }

            if (!complete && tile.hasVisRemaining()) {
                Player player = tile.crafter != null ? level.getPlayerByUUID(tile.crafter) : null;
                if (player != null) {
                    for (Aspect aspect : tile.vis.getAspects()) {
                        int remaining = tile.vis.getAmount(aspect);
                        if (aspect == null || remaining <= 0) {
                            continue;
                        }
                        int portion = Math.min(remaining, 5);
                        AspectList payment = new AspectList().add(aspect, portion);
                        if (FocusManager.consumeVisFromInventory(player, payment, false)) {
                            tile.vis.reduce(aspect, portion);
                            level.blockEvent(pos, state.getBlock(), 5, 1);
                        }
                    }
                    tile.syncTile(false);
                    tile.setChanged();
                }

                if (!tile.hasVisRemaining()) {
                    focus = tile.getItem(0);
                    if (!focus.isEmpty() && focus.getItem() instanceof ItemFocus) {
                        complete = true;
                        tile.endCraft();
                    }
                }
            }
        }

        if (complete) {
            tile.clearVis();
            tile.syncTile(false);
            tile.setChanged();
        }
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileFocalManipulator tile) {
        tile.ticks++;

        if (tile.hasVisRemaining()) {
            FXDispatcher.INSTANCE.drawGenericParticles(
                    pos.getX() + 0.5 + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f,
                    pos.getY() + 1.4 + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f,
                    pos.getZ() + 0.5 + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f,
                    0.0, 0.0, 0.0, 0.5f + level.getRandom().nextFloat() * 0.4f,
                    1.0f - level.getRandom().nextFloat() * 0.4f, 1.0f - level.getRandom().nextFloat() * 0.4f,
                    0.8f, false, 448, 9, 1, 6 + level.getRandom().nextInt(5), 0,
                    0.3f + level.getRandom().nextFloat() * 0.3f, 0.0f, 0);
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        vis.writeToNBT(tag, "vis");
        visCost.writeToNBT(tag, "visCost");
        if (crafter != null) {
            tag.putString("crafter", crafter.toString());
        }
        tag.putString("focusName", focusName);
        crystalsSync.writeToNBT(tag, "crystals");

        java.util.List<CompoundTag> nodeEntries = new java.util.ArrayList<>();
        for (FocusElementNode node : data.values()) {
            nodeEntries.add(node.serialize());
        }
        writeTagList(tag, "nodes", nodeEntries);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        vis = new AspectList();
        vis.readFromNBT(tag, "vis");
        visCost = new AspectList();
        visCost.readFromNBT(tag, "visCost");
        String crafterStr = tag.getStringOr("crafter", "");
        crafter = crafterStr.isEmpty() ? null : java.util.UUID.fromString(crafterStr);
        focusName = tag.getStringOr("focusName", "");
        crystalsSync = new AspectList();
        crystalsSync.readFromNBT(tag, "crystals");

        data.clear();
        for (CompoundTag nodeTag : readTagList(tag, "nodes")) {
            FocusElementNode node = new FocusElementNode();
            node.deserialize(nodeTag);
            data.put(node.id, node);
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack prev = getItem(slot);
        super.setItem(slot, stack);

        if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, prev)) {
            if (level != null && level.isClientSide()) {
                data.clear();
                doGuiReset = true;
            } else {
                clearVis();
                crystalsSync = new AspectList();
                setChanged();
                syncTile(false);
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof ItemFocus;
    }

    private FocusPackage generateFocus() {
        if (data == null || data.isEmpty()) {
            return null;
        }

        FocusPackage core = new FocusPackage();
        int totalComplexity = 0;
        HashMap<String, Integer> compCount = new HashMap<>();

        for (FocusElementNode node : data.values()) {
            if (node.node != null) {
                int count = compCount.getOrDefault(node.node.getKey(), 0) + 1;
                node.complexityMultiplier = 0.5f * (count + 1);
                compCount.put(node.node.getKey(), count);
                totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);
            }
        }

        core.setComplexity(totalComplexity);

        FocusElementNode root = data.get(0);
        if (root != null) {
            traverseChildren(core, root);
        }

        return core;
    }

    private void traverseChildren(FocusPackage currentPackage, FocusElementNode currentNode) {
        if (currentPackage == null || currentNode == null) {
            return;
        }

        currentPackage.addNode(currentNode.node);

        if (currentNode.children == null || currentNode.children.length == 0) {
            return;
        }

        if (currentNode.children.length == 1) {

            traverseChildren(currentPackage, data.get(currentNode.children[0]));
        } else {

            if (currentNode.node instanceof FocusModSplit splitNode) {
                splitNode.getSplitPackages().clear();
                for (int childId : currentNode.children) {
                    FocusPackage splitPackage = new FocusPackage();
                    traverseChildren(splitPackage, data.get(childId));
                    splitNode.getSplitPackages().add(splitPackage);
                }
            }
        }
    }

    public void endCraft() {
        if (level == null) return;

        vis = new AspectList();

        ItemStack focus = getItem(0);
        if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus)) return;

        FocusPackage core = generateFocus();
        if (core == null) return;

        level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, focus, t -> t.remove("color"));

        focus.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal(focusName));
        ItemFocus.setPackage(focus, core);

        setItem(0, focus);
        crystalsSync = new AspectList();
        data.clear();
        syncTile(false);
        setChanged();
    }

    public AspectList getPreviewCastCost() {
        ItemStack focus = getItem(0);
        if (focus.isEmpty() || !(focus.getItem() instanceof ItemFocus focusItem)) {
            return new AspectList();
        }
        FocusPackage core = generateFocus();
        if (core == null) {
            return new AspectList();
        }
        ItemStack preview = focus.copy();
        ItemFocus.setPackage(preview, core);

        float cost = focusItem.getVisCost(preview);
        AspectList out = new AspectList();
        List<Aspect> aspects = new ArrayList<>();
        for (IFocusElement element : core.nodes) {
            if (element instanceof FocusNode node && node.getAspect() != null && !aspects.contains(node.getAspect())) {
                aspects.add(node.getAspect());
            }
        }
        if (aspects.isEmpty()) return out;
        int perAspect = Math.max(1, (int) Math.ceil(cost / aspects.size()));
        for (Aspect aspect : aspects) {
            out.add(aspect, perAspect);
        }
        return out;
    }

    public AspectList getPreviewCost() {
        AspectList cast = getPreviewCastCost();
        AspectList cost = new AspectList();
        for (Aspect aspect : cast.getAspects()) {
            cost.add(aspect, Math.max(5, cast.getAmount(aspect) * 10));
        }
        return cost;
    }

    public boolean startCraft(int id, Player player) {
        if (level == null || level.isClientSide() || player == null) return false;
        if (data == null || data.isEmpty() || hasVisRemaining()) return false;

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

        AspectList cost = getPreviewCost();
        if (cost.size() == 0) {
            clearVis();
            return false;
        }
        boolean creative = player.getAbilities().instabuild;

        if (!creative && !FocusManager.consumeVisFromInventory(player, cost, true)) {
            clearVis();
            return false;
        }
        vis = cost.copy();
        visCost = cost.copy();
        crafter = player.getUUID();

        xpCost = (int) Math.max(1, Math.round(Math.sqrt(totalComplexity)));

        if (!creative && player.experienceLevel < xpCost) {
            clearVis();
            return false;
        }

        if (!creative) {
            player.giveExperienceLevels(-xpCost);
        }

        Aspect[] crystalAspects = crystals.getAspects();
        if (crystalAspects.length == 0) {
            clearVis();
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
                    clearVis();
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

            if (level != null && level.isClientSide()) {
                spawnVisSparkle(param);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }
    private void spawnVisSparkle(int param) {
        FXDispatcher.INSTANCE.visSparkle(
                worldPosition.getX() + level.getRandom().nextInt(3) - level.getRandom().nextInt(3),
                worldPosition.getY() + level.getRandom().nextInt(3),
                worldPosition.getZ() + level.getRandom().nextInt(3) - level.getRandom().nextInt(3),
                worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), param);
    }

    
    
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1);
    }
}
