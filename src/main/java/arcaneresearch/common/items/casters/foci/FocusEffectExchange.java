package arcaneresearch.common.items.casters.foci;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.IFocusBlockPicker;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.casters.ItemCaster;
import arcaneresearch.common.lib.events.ServerEvents;
import javax.annotation.Nullable;
public class FocusEffectExchange extends FocusEffect implements IFocusBlockPicker {
    @Override
    public String getResearch() {
        return "FOCUSEXCHANGE";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.EXCHANGE";
    }
    @Override
    public Aspect getAspect() {
        return Aspect.EXCHANGE;
    }
    @Override
    public int getComplexity() {
        int fortuneValue = getSettingValue("fortune");
        int fortuneComplexity = (fortuneValue == 0) ? 0 : ((fortuneValue + 1) * 3);
        return 5 + getSettingValue("silk") * 4 + fortuneComplexity;
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
        Entity caster = getPackage().getCaster();
        if (!(caster instanceof ServerPlayer player)) {
            return false;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState oldState = world.getBlockState(pos);
        if (!canModifyBlock(player, world, pos, oldState)) {
            return false;
        }
        ItemStack casterStack = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof ItemCaster) {
            casterStack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof ItemCaster) {
            casterStack = player.getOffhandItem();
        }
        if (casterStack.isEmpty()) {
            return false;
        }
        boolean silk = getSettingValue("silk") > 0;
        int fortune = getSettingValue("fortune");
        ItemStack picked = ((ItemCaster) casterStack.getItem()).getPickedBlock(casterStack);
        if (!picked.isEmpty()) {
            ServerEvents.addSwapper(world, pos, oldState, picked, true, 0, player,
                    true, false, 8038177, true, silk, fortune,
                    ServerEvents.DEFAULT_PREDICATE,
                    0.25f + (silk ? 0.25f : 0.0f) + fortune * 0.1f);
        }
        return true;
    }
    private boolean canModifyBlock(ServerPlayer player, Level world, BlockPos pos, BlockState state) {
        if (state.getDestroySpeed(world, pos) < 0) {
            return false;
        }
        if (!world.mayInteract(player, pos)) {
            return false;
        }
        if (!player.mayBuild()) {
            return false;
        }
        return true;
    }
    @Override
    public NodeSetting[] createSettings() {
        int[] silk = {0, 1};
        String[] silkDesc = {"focus.common.no", "focus.common.yes"};
        int[] fortune = {0, 1, 2, 3, 4};
        String[] fortuneDesc = {"focus.common.no", "I", "II", "III", "IV"};
        return new NodeSetting[]{
                new NodeSetting("fortune", "focus.common.fortune",
                        new NodeSetting.NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk",
                        new NodeSetting.NodeSettingIntList(silk, silkDesc))
        };
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderParticleFX(Level level, double posX, double posY, double posZ,
                                 double motionX, double motionY, double motionZ) {
        FXDispatcher.GenPart part = new FXDispatcher.GenPart();
        part.age = 9;
        part.redStart = 0.25f + level.random.nextFloat() * 0.25f;
        part.greenStart = 0.25f + level.random.nextFloat() * 0.25f;
        part.blueStart = 0.25f + level.random.nextFloat() * 0.25f;
        part.alpha = new float[]{0.0f, 0.6f, 0.6f, 0.0f};
        part.grid = 64;
        part.partStart = 448;
        part.partNum = 9;
        part.partInc = 1;
        part.scale = new float[]{0.5f, 0.25f};
        part.loop = false;
        part.grav = (float) (level.random.nextGaussian() * 0.01);
        part.layer = 0;
        part.randX = 0.0025;
        part.randY = 0.0025;
        part.randZ = 0.0025;
        double jx = motionX + level.random.nextGaussian() * 0.01;
        double jy = motionY + level.random.nextGaussian() * 0.01;
        double jz = motionZ + level.random.nextGaussian() * 0.01;
        FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, jx, jy, jz, part);
    }
    @Override
    public void onCast(Entity caster) {
        if (caster != null && caster.level() != null) {
            caster.level().playSound(null, caster.blockPosition().above(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                    0.2f, 2.0f + (float) (caster.level().random.nextGaussian() * 0.05));
        }
    }
}
