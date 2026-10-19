package arcaneresearch.proxies;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.client.gui.screens.*;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.lib.RefGui;
import arcaneresearch.common.menu.*;
import arcaneresearch.common.tiles.crafting.*;
import arcaneresearch.common.tiles.devices.TileHungryChest;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;
import arcaneresearch.common.tiles.devices.TileSpa;
import arcaneresearch.common.tiles.essentia.TileSmelter;

public class ProxyGUI {

    public static int researchBrowserId() {
        return RefGui.THAUMONOMICON;
    }

    private static <T> T tileAt(Level level, BlockPos pos, Class<T> type) {
        if (level == null || pos == null) {
            return null;
        }
        try {
            net.minecraft.world.level.block.entity.BlockEntity tile = level.getBlockEntity(pos);
            return type.isInstance(tile) ? type.cast(tile) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static <T extends Entity> T entityAt(Level level, int entityId, Class<T> type) {
        if (level == null || level.isClientSide()) {

            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(entityId);
                return type.isInstance(entity) ? type.cast(entity) : null;
            }
            return null;
        }
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(entityId);
            return type.isInstance(entity) ? type.cast(entity) : null;
        }
        return null;
    }

    private static BlockHitResult retrace(Player player) {
        try {
            double range = 5.0;
            net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getViewVector(1.0f);
            net.minecraft.world.phys.Vec3 end = eye.add(look.scale(range));
            return player.level().clip(new net.minecraft.world.level.ClipContext(eye, end,
                    net.minecraft.world.level.ClipContext.Block.OUTLINE,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        } catch (Exception e) {
            return null;
        }
    }

    private static ISealEntity findSeal(Player player, Level level, BlockPos fallback) {
        BlockHitResult ray = retrace(player);
        if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
            BlockPos target = ray.getBlockPos();
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
                try {
                    ISealEntity seal = SealHandler.getSealEntity(level.dimension(), new SealPos(target, side));
                    if (seal != null) {
                        return seal;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (fallback != null) {
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
                try {
                    ISealEntity seal = SealHandler.getSealEntity(level.dimension(), new SealPos(fallback, side));
                    if (seal != null) {
                        return seal;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public AbstractContainerMenu getServerGuiElement(int id, Player player, Level level, BlockPos pos, int entityId) {
        return getServerGuiElement(id, player.getInventory(), player, level, pos, entityId, player.containerMenu.containerId);
    }

    public AbstractContainerMenu getServerGuiElement(int id, Inventory playerInventory, Player player,
                                                     Level level, BlockPos pos, int entityId, int containerId) {
        return switch (id) {
            case 13 -> tileAt(level, pos, TileArcaneWorkbench.class) == null ? null
                    : new ArcaneWorkbenchMenu(containerId, playerInventory, tileAt(level, pos, TileArcaneWorkbench.class));
            case 10 -> tileAt(level, pos, TileResearchTable.class) == null ? null
                    : new ResearchTableMenu(containerId, playerInventory, tileAt(level, pos, TileResearchTable.class));
            case 9 -> tileAt(level, pos, TileSmelter.class) == null ? null
                    : new SmelterMenu(containerId, playerInventory, tileAt(level, pos, TileSmelter.class));
            case 1 -> entityAt(level, entityId, EntityPech.class) == null ? null
                    : new PechMenu(containerId, playerInventory, entityAt(level, entityId, EntityPech.class));
            case 16, 17 -> entityAt(level, entityId, EntityTurretCrossbow.class) == null ? null
                    : new TurretMenu(containerId, playerInventory, entityAt(level, entityId, EntityTurretCrossbow.class));
            case 3 -> tileAt(level, pos, TileThaumatorium.class) == null ? null
                    : new ThaumatoriumMenu(containerId, playerInventory, tileAt(level, pos, TileThaumatorium.class));
            case 14 -> entityAt(level, entityId, EntityArcaneBore.class) == null ? null
                    : new ArcaneBoreMenu(containerId, playerInventory, entityAt(level, entityId, EntityArcaneBore.class));
            case 4 -> new HandMirrorMenu(containerId, playerInventory, player.getUsedItemHand());
            case 5 -> new FocusPouchMenu(containerId, playerInventory, player.getUsedItemHand());
            case 6 -> tileAt(level, pos, TileSpa.class) == null ? null
                    : new SpaMenu(containerId, playerInventory, tileAt(level, pos, TileSpa.class));
            case 7 -> tileAt(level, pos, TileFocalManipulator.class) == null ? null
                    : new FocalManipulatorMenu(containerId, playerInventory, tileAt(level, pos, TileFocalManipulator.class));
            case 19 -> tileAt(level, pos, TileGolemBuilder.class) == null ? null
                    : new GolemBuilderMenu(containerId, playerInventory, tileAt(level, pos, TileGolemBuilder.class));
            case 21 -> tileAt(level, pos, TilePotionSprayer.class) == null ? null
                    : new PotionSprayerMenu(containerId, playerInventory, tileAt(level, pos, TilePotionSprayer.class));
            case 22 -> tileAt(level, pos, TileVoidSiphon.class) == null ? null
                    : new VoidSiphonMenu(containerId, playerInventory, tileAt(level, pos, TileVoidSiphon.class));
            case 11 -> tileAt(level, pos, TileHungryChest.class) == null ? null
                    : new HungryChestMenu(containerId, playerInventory, tileAt(level, pos, TileHungryChest.class));
            case 18 -> {
                ISealEntity seal = findSeal(player, level, pos);
                if (seal != null) {
                    Object container = seal.getSeal().returnContainer(level, player, pos, seal.getSealPos().face, seal);
                    if (container instanceof AbstractContainerMenu menu) {
                        yield menu;
                    }
                    yield new SealMenu(containerId, playerInventory, seal);
                }
                yield null;
            }
            case 20 -> {
                BlockHitResult ray = retrace(player);
                BlockPos target = ray != null && ray.getType() == HitResult.Type.BLOCK ? ray.getBlockPos() : pos;
                net.minecraft.core.Direction side = ray != null && ray.getType() == HitResult.Type.BLOCK ? ray.getDirection() : net.minecraft.core.Direction.UP;
                yield new LogisticsMenu(containerId, playerInventory, target, side);
            }
            default -> null;
        };
    }

    public Object getClientGuiElement(int id, Player player, Level level, BlockPos pos, int entityId) {

        return switch (id) {
            case 12 -> new ResearchBrowserScreen();
            default -> null;
        };
    }

    public Object getClientGuiElement(AbstractContainerMenu menu, Inventory playerInventory, Component title) {
        if (menu instanceof ArcaneWorkbenchMenu m) {
            return new ArcaneWorkbenchScreen(m, playerInventory, title);
        } else if (menu instanceof ResearchTableMenu m) {
            return new ResearchTableScreen(m, playerInventory, title);
        } else if (menu instanceof SmelterMenu m) {
            return new SmelterScreen(m, playerInventory, title);
        } else if (menu instanceof PechMenu m) {
            return new PechScreen(m, playerInventory, title);
        } else if (menu instanceof TurretMenu m) {
            return new TurretScreen(m, playerInventory, title);
        } else if (menu instanceof ThaumatoriumMenu m) {
            return new ThaumatoriumScreen(m, playerInventory, title);
        } else if (menu instanceof ArcaneBoreMenu m) {
            return new ArcaneBoreScreen(m, playerInventory, title);
        } else if (menu instanceof HandMirrorMenu m) {
            return new HandMirrorScreen(m, playerInventory, title);
        } else if (menu instanceof FocusPouchMenu m) {
            return new FocusPouchScreen(m, playerInventory, title);
        } else if (menu instanceof SpaMenu m) {
            return new SpaScreen(m, playerInventory, title);
        } else if (menu instanceof FocalManipulatorMenu m) {
            return new FocalManipulatorScreen(m, playerInventory, title);
        } else if (menu instanceof GolemBuilderMenu m) {
            return new GolemBuilderScreen(m, playerInventory, title);
        } else if (menu instanceof PotionSprayerMenu m) {
            return new PotionSprayerScreen(m, playerInventory, title);
        } else if (menu instanceof VoidSiphonMenu m) {
            return new VoidSiphonScreen(m, playerInventory, title);
        } else if (menu instanceof HungryChestMenu m) {
            return new HungryChestScreen(m, playerInventory, title);
        } else if (menu instanceof SealMenu m) {
            return new SealScreen(m, playerInventory, title);
        } else if (menu instanceof LogisticsMenu m) {
            return new LogisticsScreen(m, playerInventory, title);
        }
        return null;
    }
}
