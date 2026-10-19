package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.tiles.TileArcaneresearch;

/**
 * TC4 TileEldritchCap.func_145833_n = 9216 (96^2) — в 26.x этого
 * метода у BlockEntity больше нет: дистанция рендера определяется
 * дистанцией чанков + getRenderBoundingBox (frustum-куллинг) и
 * shouldRenderOffScreen у BER. Пьедестал целиком в своём блоке,
 * так что виден на всю ванильную рендер-дистанцию чанков.
 */

/**
 * Тайл крышки, порт TC4 TileEldritchCap: маркер без тика
 * (дальность рендера как в оригинале). Лежит в arcaneresearch.arcaneresearch.common.tiles.aura.
 */
public class EldritchCapTile extends TileArcaneresearch {
    public EldritchCapTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    public AABB getRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 2, 2));
    }
}
