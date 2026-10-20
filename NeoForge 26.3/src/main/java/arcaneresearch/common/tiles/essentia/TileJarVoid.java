package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.init.ModBlockEntities;

public class TileJarVoid extends TileJar {

    public boolean voiding = false;
    private int voidingTimer = 0;

    public TileJarVoid(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileJarVoid(BlockPos pos, BlockState state) {
        this(ModBlockEntities.JAR_VOID.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileJarVoid tile) {

        if (++tile.tickCount % 5 == 0) {
            tile.fillFromAbove();
        }

        if (tile.voiding) {
            tile.voidingTimer++;
            if (tile.voidingTimer > 10) {
                tile.voiding = false;
                tile.voidingTimer = 0;
                tile.markDirtyAndSync();
            }
        }
    }

    @Override
    public int addToContainer(Aspect tag, int amt) {

        boolean up = amount < CAPACITY;
        if (amt == 0) return amt;

        if (tag == aspect || amount == 0) {
            aspect = tag;
            amount += amt;
            amt = 0;
            if (amount > CAPACITY) {
                if (level != null && level.getRandom().nextInt(250) == 0) {
                    AuraHelper.polluteAura(level, worldPosition, 1.0F, true);
                }
                amount = CAPACITY;
            }
        }
        if (up) {
            markDirtyAndSync();
        }
        return amt;
    }

    @Override
    public int getSuctionAmount(Direction face) {

        if (aspectFilter != null && amount < CAPACITY) {
            return 48;
        }
        return 32;
    }

    @Override
    public int getMinimumSuction() {

        return (aspectFilter != null) ? 48 : 32;
    }

    public boolean isVoiding() {
        return voiding;
    }
}
