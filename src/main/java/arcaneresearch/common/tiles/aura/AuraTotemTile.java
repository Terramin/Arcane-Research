package arcaneresearch.common.tiles.aura;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.menu.AuraTotemMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AuraTotemTile extends TileArcaneresearchInventory implements MenuProvider {
    public static final String TOTEM_ID = "arcaneresearch:aura_totem";
    private static final Block TOTEM_BLOCK =
            ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(TOTEM_ID));

    public byte type = -1;
    public int time = 0;
    public int maxTime = 500;
    protected int count = 0;
    protected int zoneOuter = -1;
    protected int zoneInner = -1;
    protected int zonePure = -1;
    public int cd;

    public AuraTotemTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state, 1);
        this.cd = 10;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AuraTotemTile tile) {
        tile.serverTick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AuraTotemTile tile) {
        tile.clientTick();
    }

    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        this.count++;
        if (this.zoneOuter < 0) checkPoles();
        if (this.count % this.cd != 0) return;

        BlockState state = getBlockState();

        if (this.time > 0) {
            this.time--;
            performMagnet();
            return;
        }

        if (this.type >= 0) {
            setChanged();
            level.blockEvent(this.worldPosition, state.getBlock(), 2, -1);
        }
        this.type = -1;

        ItemStack fuel = getItem(0);
        if (fuel.isEmpty() || !fuel.is(ModItems.CRYSTAL_ESSENCE.get())) return;

        Aspect aspect = ItemCrystalEssence.getAspect(fuel);
        if (aspect == null) return;

        byte mode = aspectToMode(aspect);
        if (mode < 0) return;

        this.type = mode;
        removeItem(0, 1);
        this.time = this.maxTime;
        setChanged();
        level.blockEvent(this.worldPosition, state.getBlock(), 2, this.type);
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) return;

        this.count++;
        if (this.zoneOuter < 0) checkPoles();
        if (this.type >= 0 && this.count % 20 == 0) drawEffect();
    }

    protected void performMagnet() {
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawEffect() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) return;

        int span = this.zoneOuter + this.zoneInner + 1;
        int drop = span > 0 ? level.random.nextInt(span) : 0;

        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() - drop;
        double z = this.worldPosition.getZ() + 0.5D;
        FXDispatcher.INSTANCE.drawWispyMotes(x, y, z, 0.0D, 0.03D, 0.0D, 40, 0.0F);
    }

    public static byte aspectToMode(Aspect aspect) {
        if (aspect == null) return -1;
        if (aspect == Aspect.AIR) return 1;
        if (aspect == Aspect.FIRE) return 2;
        if (aspect == Aspect.WATER) return 3;
        if (aspect == Aspect.EARTH) return 4;
        if (aspect == Aspect.ORDER) return 5;
        if (aspect == Aspect.ENTROPY) return 6;
        if (aspect == Aspect.FLUX) return 7;
        return -1;
    }

    public Aspect getAspect() {
        return switch (this.type) {
            case 1 -> Aspect.AIR;
            case 2 -> Aspect.FIRE;
            case 3 -> Aspect.WATER;
            case 4 -> Aspect.EARTH;
            case 5 -> Aspect.ORDER;
            case 6 -> Aspect.ENTROPY;
            case 7 -> Aspect.FLUX;
            default -> null;
        };
    }

    public void checkPoles() {
        Level level = getLevel();
        if (level == null) return;

        this.zoneOuter = 0;
        this.zoneInner = 0;
        this.zonePure = 0;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                this.worldPosition.getX(),
                this.worldPosition.getY() - 1,
                this.worldPosition.getZ());

        int scanned = 0;
        while (scanned < 5) {
            BlockState state = level.getBlockState(cursor);
            if (state.getBlock() != TOTEM_BLOCK) break;

            int variant = poleVariant(state);
            if (variant < 0) break;

            switch (variant) {
                case 0 -> this.zoneOuter++;
                case 1 -> this.zoneInner++;
                case 2 -> this.zonePure++;
            }

            scanned++;
            cursor.move(Direction.DOWN);
        }

        this.cd = Math.max(1, 10 - this.zoneInner + this.zoneOuter);
    }

    static boolean isTotemBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() == TOTEM_BLOCK;
    }

    static int poleVariant(BlockState state) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            if (entry.getKey().getName().equals("type") && entry.getValue() instanceof Enum<?> e) {
                return switch (e.name()) {
                    case "POLE_OUTER" -> 0;
                    case "POLE_INNER" -> 1;
                    case "POLE_PURE"  -> 2;
                    default -> -1;
                };
            }
        }
        return -1;
    }

    public int getTimeScaled(int scale) {
        return this.maxTime <= 0 ? 0 : this.time * scale / this.maxTime;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.arcaneresearch.aura_totem");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new AuraTotemMenu(windowId, playerInventory, this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.CRYSTAL_ESSENCE.get());
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        Level level = getLevel();

        if (id == 1) {
            if (level != null && level.isClientSide) checkPoles();
            return true;
        }

        if (id == 2) {
            if (level != null && level.isClientSide) {
                this.type = (byte) param;
                BlockState state = getBlockState();
                level.sendBlockUpdated(this.worldPosition, state, state, 3);
            }
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByte("type", this.type);
        tag.putInt("time", this.time);
        tag.putInt("maxTime", this.maxTime);
        tag.putInt("zoneOuter", this.zoneOuter);
        tag.putInt("zoneInner", this.zoneInner);
        tag.putInt("zonePure", this.zonePure);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        this.type = tag.getByte("type");
        this.time = tag.getInt("time");
        this.maxTime = Math.max(1, tag.getInt("maxTime"));
        this.zoneOuter = tag.getInt("zoneOuter");
        this.zoneInner = tag.getInt("zoneInner");
        this.zonePure = tag.getInt("zonePure");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        writeTotem(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        readTotem(tag);
    }

    protected void writeTotem(CompoundTag tag) {
        tag.putByte("type", this.type);
        tag.putInt("time", this.time);
        tag.putInt("maxTime", this.maxTime);
        tag.putInt("count", this.count);
        tag.putInt("zoneOuter", this.zoneOuter);
        tag.putInt("zoneInner", this.zoneInner);
        tag.putInt("zonePure", this.zonePure);
        tag.putInt("cd", this.cd);
    }

    protected void readTotem(CompoundTag tag) {
        this.type = tag.getByte("type");
        this.time = tag.getInt("time");
        this.maxTime = Math.max(1, tag.getInt("maxTime"));
        this.count = tag.getInt("count");
        this.zoneOuter = tag.getInt("zoneOuter");
        this.zoneInner = tag.getInt("zoneInner");
        this.zonePure = tag.getInt("zonePure");
        this.cd = tag.contains("cd") ? Math.max(1, tag.getInt("cd")) : 10;
    }
}