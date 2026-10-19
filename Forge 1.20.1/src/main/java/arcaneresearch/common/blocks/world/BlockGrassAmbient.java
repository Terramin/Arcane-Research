package arcaneresearch.common.blocks.world;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
public class BlockGrassAmbient extends GrassBlock {
    public BlockGrassAmbient() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(0.6f)
                .randomTicks()
                .sound(SoundType.GRASS));
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int skyLight = level.getBrightness(LightLayer.SKY, pos.above());
        float celestialAngle = level.getSunAngle(1.0f);
        float adjustedAngle = celestialAngle;
        float targetAngle = (celestialAngle < (float) Math.PI) ? 0.0f : (float) (Math.PI * 2);
        adjustedAngle += (targetAngle - celestialAngle) * 0.2f;
        int adjustedLight = Math.round(skyLight * Mth.cos(adjustedAngle));
        adjustedLight = Mth.clamp(adjustedLight, 0, 15);
        if (4 + adjustedLight * 2 < 1 + random.nextInt(13)) {
            int x = Mth.randomBetweenInclusive(random, -8, 8);
            int z = Mth.randomBetweenInclusive(random, -8, 8);
            BlockPos searchPos = pos.offset(x, 5, z);
            for (int q = 0; q < 10 && searchPos.getY() > 50; q++) {
                BlockState targetState = level.getBlockState(searchPos);
                if (targetState.is(Blocks.GRASS_BLOCK) || targetState.getBlock() instanceof GrassBlock) {
                    break;
                }
                searchPos = searchPos.below();
            }
            BlockState foundState = level.getBlockState(searchPos);
            if (foundState.is(Blocks.GRASS_BLOCK) || foundState.getBlock() instanceof GrassBlock) {
                FXDispatcher.INSTANCE.drawWispyMotesOnBlock(searchPos.above(), 400, -0.01f);
            }
        }
    }
}
