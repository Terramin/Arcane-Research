package arcaneresearch.proxies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.common.config.ConfigAspects;
import arcaneresearch.common.config.ConfigMultiblocks;
import arcaneresearch.common.config.ConfigRecipes;
import arcaneresearch.common.config.ConfigResearch;
import arcaneresearch.common.lib.BehaviorDispenseAlumetum;
import arcaneresearch.common.lib.InternalMethodHandler;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.CropUtils;
import arcaneresearch.common.lib.utils.Utils;
import arcaneresearch.common.world.biomes.BiomeHandler;
import java.util.HashMap;
import java.util.Map;
public class CommonProxy implements IProxy {
    public static final Map<ResourceLocation, Integer> championModWhitelist = new HashMap<>();
    protected final ProxyGUI proxyGUI;
    public CommonProxy() {
        proxyGUI = new ProxyGUI();
    }
    private static ItemStack parseCountedStack(String id, String meta) {
        try {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null && ForgeRegistries.ITEMS.containsKey(rl)) {
                return new ItemStack(ForgeRegistries.ITEMS.getValue(rl));
            }
            int numeric = Integer.parseInt(id);
            for (net.minecraft.world.item.Item item : ForgeRegistries.ITEMS.getValues()) {
                if (net.minecraft.world.item.Item.getId(item) == numeric) {
                    return new ItemStack(item);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    @Override
    public void preInit() {
        if (ArcaneresearchApi.internalMethods == null) {
            ArcaneresearchApi.internalMethods = new InternalMethodHandler();
        }
        BehaviorDispenseAlumetum.register();
    }
    @Override
    public void init() {
        ConfigResearch.init();
        arcaneresearch.common.config.ModConfig.modCompatibility();
        arcaneresearch.common.config.ConfigEntities.initPechTrades();
        arcaneresearch.common.config.ConfigBlocks.initMisc();
        arcaneresearch.common.config.ConfigItems.initMisc();
        arcaneresearch.api.items.ItemsTC.init();
        arcaneresearch.common.config.ModConfig.postInitLoot();
        arcaneresearch.common.config.ConfigEntities.postInitEntitySpawns();
        ConfigRecipes.registerSmeltingBonuses();
        ConfigMultiblocks.init();
        ConfigRecipes.registerFakeRecipes();
        ArcaneresearchCraftingManager.registerDynamicRecipes();
    }
    @Override
    public void postInit() {
        ConfigAspects.init();
        arcaneresearch.common.config.ModConfig.postInitMisc();
        ConfigResearch.postInit();
    }
    public Object getClientGuiElement(int id, net.minecraft.world.entity.player.Player player, Level level,
                                      net.minecraft.core.BlockPos pos, int entityId) {
        return proxyGUI.getClientGuiElement(id, player, level, pos, entityId);
    }
    public Object getServerGuiElement(int id, net.minecraft.world.entity.player.Player player, Level level,
                                      net.minecraft.core.BlockPos pos, int entityId) {
        return proxyGUI.getServerGuiElement(id, player, level, pos, entityId);
    }
    @Override
    public boolean isShiftKeyDown() {
        return false;
    }
    @Override
    public Level getClientLevel() {
        return null;
    }
    @Override
    public Level getLevel(ResourceKey<Level> dimension) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getLevel(dimension);
    }
    @Override
    public boolean getSingleplayer() {
        return false;
    }
    @Override
    public void checkInterModComs(InterModProcessEvent event) {
        event.getIMCStream().forEach(msg -> {
            try {
                Object value = msg.messageSupplier().get();
                switch (msg.method()) {
                    case "portableHoleBlacklist" -> {
                        if (value instanceof String s) {
                            BlockUtils.portableHoleBlackList.add(s);
                        }
                    }
                    case "harvestStandardCrop" -> {
                        if (value instanceof ItemStack stack) {
                            CropUtils.addStandardCrop(stack, stack.getDamageValue());
                        }
                    }
                    case "harvestClickableCrop" -> {
                        if (value instanceof ItemStack stack) {
                            CropUtils.addClickableCrop(stack, stack.getDamageValue());
                        }
                    }
                    case "harvestStackedCrop" -> {
                        if (value instanceof ItemStack stack) {
                            CropUtils.addStackedCrop(stack, stack.getDamageValue());
                        }
                    }
                    case "nativeCluster" -> {
                        if (value instanceof String s) {
                            String[] t = s.split(",");
                            if (t != null && t.length == 5) {
                                try {
                                    ItemStack ore = parseCountedStack(t[0].trim(), t[1].trim());
                                    ItemStack cluster = parseCountedStack(t[2].trim(), t[3].trim());
                                    if (ore != null && cluster != null) {
                                        Utils.addSpecialMiningResult(ore, cluster, Float.parseFloat(t[4].trim()));
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                    case "lampBlacklist" -> {
                        if (value instanceof ItemStack stack) {
                            CropUtils.blacklistLamp(stack, stack.getDamageValue());
                        }
                    }
                    case "dimensionBlacklist" -> {
                        if (value instanceof String s) {
                            String[] t = s.split(":");
                            if (t != null && t.length == 3) {
                                try {
                                    BiomeHandler.addDimBlacklist(new ResourceLocation(t[0], t[1]), Integer.parseInt(t[2]));
                                } catch (Exception ignored) {
                                }
                            } else if (t != null && t.length == 2) {
                                try {
                                    BiomeHandler.addDimBlacklist(new ResourceLocation(t[0]), Integer.parseInt(t[1]));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                    case "biomeBlacklist" -> {
                        if (value instanceof String s) {
                            String[] t = s.split(":");
                            if (t != null && t.length == 3) {
                                try {
                                    BiomeHandler.addBiomeBlacklist(new ResourceLocation(t[0], t[1]), Integer.parseInt(t[2]));
                                } catch (Exception ignored) {
                                }
                            } else if (t != null && t.length == 2) {
                                try {
                                    BiomeHandler.addBiomeBlacklist(new ResourceLocation(t[0]), Integer.parseInt(t[1]));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                    case "championWhiteList" -> {
                        if (value instanceof String s) {
                            try {
                                String[] t = s.split(":");
                                if (t != null && t.length == 3) {
                                    ResourceLocation id = new ResourceLocation(t[0], t[1]);
                                    int lvl = Integer.parseInt(t[2]);
                                    if (ForgeRegistries.ENTITY_TYPES.containsKey(id)
                                            && ForgeRegistries.ENTITY_TYPES.getValue(id) instanceof EntityType) {
                                        championModWhitelist.put(id, lvl);
                                    }
                                } else if (t != null && t.length == 2) {
                                    ResourceLocation id = new ResourceLocation(t[0]);
                                    int lvl = Integer.parseInt(t[1]);
                                    if (ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
                                        championModWhitelist.put(id, lvl);
                                    }
                                }
                            } catch (Exception e) {
                                Arcaneresearch.LOGGER.error("Failed to Whitelist [{}] with [ championWhiteList ] message.", s);
                            }
                        }
                    }
                    default -> {
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }
    public void registerPackets() {
        PacketHandler.init();
    }
}
