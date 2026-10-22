package arcaneresearch.proxies;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.CropUtils;
import arcaneresearch.common.lib.utils.Utils;
import arcaneresearch.common.world.biomes.BiomeHandler;

import java.util.HashMap;
import java.util.Map;





public class CommonProxy implements IProxy {

    public static final Map<Identifier, Integer> championModWhitelist = new HashMap<>();

    private static ItemStack parseCountedStack(String id, String meta) {
        try {
            Identifier rl = Identifier.tryParse(id);
            if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(rl));
            }
            Item item = BuiltInRegistries.ITEM.byId(Integer.parseInt(id));
            if (item != null) {
                return new ItemStack(item);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public Level getClientLevel() {
        return null;
    }

    @Override
    public boolean getSingleplayer() {
        return false;
    }

    @Override
    public Level getLevel(ResourceKey<Level> dimension) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getLevel(dimension);
    }

    @Override
    public boolean isShiftKeyDown() {
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
                            if (t.length == 5) {
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
                            try {
                                if (t.length == 3) {
                                    BiomeHandler.addDimBlacklist(Identifier.fromNamespaceAndPath(t[0], t[1]), Integer.parseInt(t[2]));
                                } else if (t.length == 2) {
                                    BiomeHandler.addDimBlacklist(Identifier.withDefaultNamespace(t[0]), Integer.parseInt(t[1]));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    case "biomeBlacklist" -> {
                        if (value instanceof String s) {
                            String[] t = s.split(":");
                            try {
                                if (t.length == 3) {
                                    BiomeHandler.addBiomeBlacklist(Identifier.fromNamespaceAndPath(t[0], t[1]), Integer.parseInt(t[2]));
                                } else if (t.length == 2) {
                                    BiomeHandler.addBiomeBlacklist(Identifier.withDefaultNamespace(t[0]), Integer.parseInt(t[1]));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    case "championWhiteList" -> {
                        if (value instanceof String s) {
                            try {
                                String[] t = s.split(":");
                                if (t.length == 3) {
                                    Identifier id = Identifier.fromNamespaceAndPath(t[0], t[1]);
                                    int lvl = Integer.parseInt(t[2]);
                                    if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)
                                            && BuiltInRegistries.ENTITY_TYPE.getValue(id) instanceof EntityType) {
                                        championModWhitelist.put(id, lvl);
                                    }
                                } else if (t.length == 2) {
                                    Identifier id = Identifier.withDefaultNamespace(t[0]);
                                    int lvl = Integer.parseInt(t[1]);
                                    if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
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
}
