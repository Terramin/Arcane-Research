package arcaneresearch.common.lib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge.EnumKnowledgeType;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.IPlayerWarp.EnumWarpType;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.api.internal.IInternalMethodHandler;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.common.golems.seals.ItemSealPlacer;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXPollute;
import arcaneresearch.common.lib.network.playerdata.PacketWarpMessage;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.common.world.aura.AuraHandler;
public class InternalMethodHandler implements IInternalMethodHandler {
    @Override
    public boolean addKnowledge(Player player, EnumKnowledgeType type, ResearchCategory category, int amount) {
        if (amount == 0 || player.level().isClientSide()) {
            return false;
        }
        return ResearchManager.addKnowledge(player, type, category, amount);
    }
    @Override
    public boolean progressResearch(Player player, String researchKey) {
        if (researchKey == null || player.level().isClientSide()) {
            return false;
        }
        return ResearchManager.progressResearch(player, researchKey);
    }
    @Override
    public boolean completeResearch(Player player, String researchKey) {
        if (researchKey == null || player.level().isClientSide()) {
            return false;
        }
        return ResearchManager.completeResearch(player, researchKey);
    }
    @Override
    public boolean doesPlayerHaveRequisites(Player player, String researchKey) {
        return ResearchManager.doesPlayerHaveRequisites(player, researchKey);
    }
    @Override
    public void addWarpToPlayer(Player player, int amount, EnumWarpType type) {
        if (amount == 0 || player.level().isClientSide()) {
            return;
        }
        IPlayerWarp pw = ArcaneresearchCapabilities.getWarp(player);
        if (pw == null) {
            return;
        }
        int cur = pw.get(type);
        if (amount < 0 && cur + amount < 0) {
            amount = -cur;
        }
        pw.add(type, amount);
        if (player instanceof ServerPlayer serverPlayer) {
            byte typeId = switch (type) {
                case PERMANENT -> (byte) 0;
                case NORMAL -> (byte) 1;
                case TEMPORARY -> (byte) 2;
            };
            PacketHandler.sendToPlayer(new PacketWarpMessage(player, typeId, amount), serverPlayer);
        }
        if (amount > 0) {
            int totalWarp = pw.get(EnumWarpType.TEMPORARY) + pw.get(EnumWarpType.PERMANENT) + pw.get(EnumWarpType.NORMAL);
            pw.setCounter(totalWarp);
        }
        if (type != EnumWarpType.TEMPORARY
                && ArcaneresearchCapabilities.knowsResearchStrict(player, "FIRSTSTEPS")
                && !ArcaneresearchCapabilities.knowsResearchStrict(player, "WARP")) {
            completeResearch(player, "WARP");
            player.displayClientMessage(Component.translatable("research.WARP.warn"), true);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            pw.sync(serverPlayer);
        }
    }
    @Override
    public int getActualWarp(Player player) {
        IPlayerWarp wc = ArcaneresearchCapabilities.getWarp(player);
        if (wc == null) {
            return 0;
        }
        return wc.get(EnumWarpType.NORMAL) + wc.get(EnumWarpType.PERMANENT);
    }
    @Override
    public AspectList getObjectAspects(ItemStack is) {
        return ArcaneresearchCraftingManager.getObjectTags(is);
    }
    @Override
    public AspectList generateTags(ItemStack is) {
        return ArcaneresearchCraftingManager.generateTags(is);
    }
    @Override
    public float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainVis(level, pos, amount, simulate);
    }
    @Override
    public float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainFlux(level, pos, amount, simulate);
    }
    @Override
    public void addVis(Level level, BlockPos pos, float amount) {
        AuraHandler.addVis(level, pos, amount);
    }
    @Override
    public void addFlux(Level level, BlockPos pos, float amount, boolean showEffect) {
        if (level.isClientSide()) {
            return;
        }
        AuraHandler.addFlux(level, pos, amount);
        if (showEffect && amount > 0.0f) {
            if (level instanceof ServerLevel serverLevel) {
                PacketHandler.sendToAllTrackingChunk(
                        new PacketFXPollute(pos, amount),
                        serverLevel,
                        pos
                );
            }
        }
    }
    @Override
    public float getTotalAura(Level level, BlockPos pos) {
        return AuraHandler.getTotalAura(level, pos);
    }
    @Override
    public float getVis(Level level, BlockPos pos) {
        return AuraHandler.getVis(level, pos);
    }
    @Override
    public float getFlux(Level level, BlockPos pos) {
        return AuraHandler.getFlux(level, pos);
    }
    @Override
    public int getAuraBase(Level level, BlockPos pos) {
        return AuraHandler.getAuraBase(level, pos);
    }
    @Override
    public boolean shouldPreserveAura(Level level, Player player, BlockPos pos) {
        return AuraHandler.shouldPreserveAura(level, player, pos);
    }
    @Override
    public void registerSeal(ISeal seal) {
        SealHandler.registerSeal(seal);
    }
    @Override
    public ISeal getSeal(String key) {
        return SealHandler.getSeal(key);
    }
    @Override
    public ISealEntity getSealEntity(String dimension, SealPos pos) {
        ResourceKey<Level> dimKey = parseDimensionKey(dimension);
        if (dimKey == null) {
            return null;
        }
        return SealHandler.getSealEntity(dimKey, pos);
    }
    @Override
    public void addGolemTask(String dimension, Task task) {
        ResourceKey<Level> dimKey = parseDimensionKey(dimension);
        if (dimKey == null) {
            return;
        }
        TaskHandler.addTask(dimKey, task);
    }
    @Override
    public ItemStack getSealStack(String key) {
        return ItemSealPlacer.getSealStack(key);
    }
    private ResourceKey<Level> parseDimensionKey(String dimension) {
        if (dimension == null || dimension.isEmpty()) {
            return null;
        }
        try {
            ResourceLocation loc = new ResourceLocation(dimension);
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, loc);
        } catch (Exception e) {
            return null;
        }
    }
    @SuppressWarnings("unused")
    private ServerLevel getServerLevel(String dimension) {
        ResourceKey<Level> dimKey = parseDimensionKey(dimension);
        if (dimKey == null) {
            return null;
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        return server.getLevel(dimKey);
    }
}
