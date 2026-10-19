package arcaneresearch.common.tiles.aura;
import arcaneresearch.common.menu.AuraTotemMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.Map;
public class AuraTotemTile extends TileArcaneresearchInventory implements MenuProvider {
    public static final String TOTEM_ID = "arcaneresearch:aura_totem";
    public byte type = -1;
    public int time = 0;
    public int maxTime = 500;
    protected int count = 0;
    protected int zoneOuter = -1;
    protected int zoneInner = -1;
    protected int zonePure = -1;
    public int cd = 10;
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
        if (level == null || level.isClientSide) {
            return;
        }
        this.count++;
        if (this.zoneOuter < 0) {
            checkPoles();
        }
        if (this.count % Math.max(1, this.cd) == 0) {
            if (this.time > 0) {
                this.time--;
                performMagnet();
            } else {
                if (this.type >= 0) {
                    setChanged();
                    level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
                    level.blockEvent(this.worldPosition, getBlockState().getBlock(), 2, -1);
                }
                this.type = -1;
                ItemStack fuel = getItem(0);
                if (!fuel.isEmpty() && fuel.is(ModItems.CRYSTAL_ESSENCE.get())) {
                    Aspect aspect = ItemCrystalEssence.getAspect(fuel);
                    this.type = aspectToMode(aspect);
                    removeItem(0, 1);
                    this.time = this.maxTime;
                    setChanged();
                    level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
                    level.blockEvent(this.worldPosition, getBlockState().getBlock(), 2, this.type);
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        this.count++;
        if (this.zoneOuter < 0) {
            checkPoles();
        }
        if (this.type >= 0 && this.count % 20 == 0) {
            drawEffect();
        }
    }
    protected void performMagnet() {
    }
    @OnlyIn(Dist.CLIENT)
    protected void drawEffect() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() - level.random.nextInt(this.zoneOuter + this.zoneInner + 1);
        double z = this.worldPosition.getZ() + 0.5D;
        arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawWispyMotes(
                x, y, z, 0.0D, 0.03D, 0.0D, 40, 0.0F);
    }
    public static byte aspectToMode(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        if (aspect == Aspect.AIR) {
            return 1;
        }
        if (aspect == Aspect.FIRE) {
            return 2;
        }
        if (aspect == Aspect.WATER) {
            return 3;
        }
        if (aspect == Aspect.EARTH) {
            return 4;
        }
        if (aspect == Aspect.ORDER) {
            return 5;
        }
        if (aspect == Aspect.ENTROPY) {
            return 6;
        }
        if (aspect == Aspect.FLUX) {
            return 7;
        }
        return 0;
    }
    public Aspect getAspect() {
        switch (this.type) {
            case 1:
                return Aspect.AIR;
            case 2:
                return Aspect.FIRE;
            case 3:
                return Aspect.WATER;
            case 4:
                return Aspect.EARTH;
            case 5:
                return Aspect.ORDER;
            case 6:
                return Aspect.ENTROPY;
            case 7:
                return Aspect.FLUX;
            default:
                return null;
        }
    }
    public void checkPoles() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        this.zoneOuter = 0;
        this.zoneInner = 0;
        this.zonePure = 0;
        BlockPos pos = this.worldPosition.below();
        int scanned = 0;
        while (isTotemBlock(level, pos) && scanned < 5) {
            String variant = poleVariant(level, pos);
            if ("POLE_OUTER".equals(variant)) {
                this.zoneOuter++;
                scanned++;
            }
            if ("POLE_INNER".equals(variant)) {
                this.zoneInner++;
                scanned++;
            }
            if ("POLE_PURE".equals(variant)) {
                this.zonePure++;
                scanned++;
            }
            pos = pos.below();
        }
        this.cd = 10 - this.zoneInner + this.zoneOuter;
    }
    static boolean isTotemBlock(Level level, BlockPos pos) {
        net.minecraft.world.level.block.Block block = level.getBlockState(pos).getBlock();
        net.minecraft.resources.ResourceLocation id =
                net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        return id != null && TOTEM_ID.equals(id.toString());
    }
    static String poleVariant(Level level, BlockPos pos) {
        for (Map.Entry<Property<?>, Comparable<?>> entry
                : level.getBlockState(pos).getValues().entrySet()) {
            if (entry.getKey().getName().equals("type") && entry.getValue() instanceof Enum) {
                return ((Enum<?>) entry.getValue()).name();
            }
        }
        return "";
    }
    public int getTimeScaled(int scale) {
        return this.maxTime <= 0 ? 0 : this.time * scale / this.maxTime;
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.arcaneresearch.aura_totem");
    }
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
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
            if (level != null && level.isClientSide) {
                checkPoles();
            }
            return true;
        }
        if (id == 2) {
            if (level != null && level.isClientSide) {
                this.type = (byte) param;
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        writeTotem(tag);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        readTotem(tag);
    }
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeTotem(tag);
    }
    @Override
    public void load(CompoundTag tag) {
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
        this.maxTime = tag.getInt("maxTime");
        this.count = tag.getInt("count");
        this.zoneOuter = tag.getInt("zoneOuter");
        this.zoneInner = tag.getInt("zoneInner");
        this.zonePure = tag.getInt("zonePure");
        this.cd = tag.contains("cd") ? tag.getInt("cd") : 10;
    }
}
