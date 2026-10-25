package arcaneresearch.common.items.casters.foci;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.misc.BlockHole;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
public class FocusEffectRift extends FocusEffect {
    public static boolean createHole(Level world, BlockPos pos, Direction side, byte count, int max) {
        BlockState bs = world.getBlockState(pos);
        if (!world.isClientSide() && world.getBlockEntity(pos) == null
                && !BlockUtils.isPortableHoleBlackListed(bs)
                && !bs.is(Blocks.BEDROCK) && !bs.is(ModBlocks.HOLE.get())
                && (bs.isAir() || !bs.canBeReplaced())
                && bs.getFluidState().isEmpty()
                && bs.getDestroySpeed(world, pos) != -1.0f) {
            return BlockHole.createHole(world, pos, bs, (short) max, count, side);
        }
        return false;
    }
    @Override
    public String getResearch() {
        return "FOCUSRIFT";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.RIFT";
    }
    @Override
    public Aspect getAspect() {
        return Aspect.ELDRITCH;
    }
    @Override
    public int getComplexity() {
        return 3 + getSettingValue("duration") / 2 + getSettingValue("depth") / 4;
    }
    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (getPackage() == null || getPackage().world == null) {
            return false;
        }
        if (target.getType() != HitResult.Type.BLOCK || !(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        Level world = getPackage().world;
        if (world.dimension().location().getPath().equals("outer_lands")) {
            BlockPos failPos = blockHit.getBlockPos();
            world.playSound(null, failPos, ModSounds.WAND_FAIL.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            return false;
        }
        float maxDist = getSettingValue("depth") * finalPower;
        int dur = 20 * getSettingValue("duration");
        Direction hitSide = blockHit.getDirection();
        int distance = 0;
        BlockPos pos = blockHit.getBlockPos();
        for (distance = 0; distance < maxDist; distance++) {
            BlockState bi = world.getBlockState(pos);
            if (BlockUtils.isPortableHoleBlackListed(bi) || bi.is(Blocks.BEDROCK)
                    || bi.is(ModBlocks.HOLE.get()) || bi.isAir()) {
                break;
            }
            if (bi.getDestroySpeed(world, pos) == -1.0f) {
                break;
            }
            pos = pos.relative(hitSide.getOpposite());
        }
        createHole(world, blockHit.getBlockPos(), hitSide, (byte) (distance + 1), dur);
        return true;
    }
    @Override
    public NodeSetting[] createSettings() {
        int[] depth = {8, 16, 24, 32};
        String[] depthDesc = {"8", "16", "24", "32"};
        return new NodeSetting[]{
                new NodeSetting("depth", "focus.rift.depth",
                        new NodeSetting.NodeSettingIntList(depth, depthDesc)),
                new NodeSetting("duration", "focus.common.duration",
                        new NodeSetting.NodeSettingIntRange(2, 10))
        };
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        FXDispatcher.GenPart part = new FXDispatcher.GenPart();
        part.age = 16 + level.random.nextInt(16);
        part.partStart = 384 + level.random.nextInt(16);
        part.partNum = 1;
        part.partInc = 1;
        part.slowDown = 0.75;
        part.alpha = new float[]{1.0f, 0.0f};
        part.scale = new float[]{(float) (0.7 + level.random.nextGaussian() * 0.3)};
        part.redStart = 0.25f;
        part.greenStart = 0.25f;
        part.blueStart = 1.0f;
        part.loop = false;
        part.layer = 0;
        part.delay = 0;
        part.randX = 0.01;
        part.randY = 0.01;
        part.randZ = 0.01;
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, part);
    }
    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                    0.2f, 0.7f);
        }
    }
}
