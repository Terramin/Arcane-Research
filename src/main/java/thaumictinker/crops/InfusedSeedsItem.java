package thaumictinker.crops;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import thaumictinker.registry.TtRegistry;

 



 
public class InfusedSeedsItem extends BlockItem implements TtRegistry.TabVariants {

    public static final String ASPECT_TAG = "SeedAspect";
    public static final String TENDENCIES_TAG = "SeedTendencies";

    public InfusedSeedsItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    public static Aspect getAspect(ItemStack stack) {
        String raw = stack.hasTag() ? stack.getTag().getString(ASPECT_TAG) : "";
        return raw.isEmpty() ? null : Aspect.getAspect(raw);
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        stack.getOrCreateTag().putString(ASPECT_TAG, aspect.getTag());
        
        stack.getOrCreateTag().putInt("CustomModelData", modelIndex(aspect));
    }

      
    private static int modelIndex(Aspect aspect) {
        if (aspect == Aspect.AIR) {
            return 1;
        }
        if (aspect == Aspect.WATER) {
            return 2;
        }
        if (aspect == Aspect.FIRE) {
            return 3;
        }
        if (aspect == Aspect.ORDER) {
            return 4;
        }
        if (aspect == Aspect.ENTROPY) {
            return 5;
        }
        if (aspect == Aspect.EARTH) {
            return 6;
        }
        return 7;
    }

    public static AspectList getTendencies(ItemStack stack) {
        AspectList tendencies = new AspectList();
        if (stack.hasTag()) {
            tendencies.readFromNBT(stack.getTag(), TENDENCIES_TAG);
        }
        return tendencies;
    }

    public static void setTendencies(ItemStack stack, AspectList tendencies) {
        if (stack.hasTag()) {
            stack.getTag().remove(TENDENCIES_TAG);
        }
        tendencies.writeToNBT(stack.getOrCreateTag(), TENDENCIES_TAG);
    }

    public static ItemStack stackFor(Aspect aspect) {
        ItemStack stack = new ItemStack(CropsFeature.INFUSED_SEEDS);
        setAspect(stack, aspect);
        setTendencies(stack, new AspectList());
        return stack;
    }

    @Override
    public void addVariants(CreativeModeTab.Output output) {
        for (Aspect primal : Aspect.getPrimalAspects()) {
            output.accept(stackFor(primal));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect != null) {
            tooltip.add(Component.translatable(aspect.getLocalizedDescription()));
        }
        AspectList tendencies = getTendencies(stack);
        for (Aspect tag : tendencies.getAspects()) {
            tooltip.add(Component.literal(tag.getName() + ": " + tendencies.getAmount(tag))
                    .withStyle(s -> s.withColor(net.minecraft.network.chat.TextColor.fromRgb(tag.getColor()))));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos above = pos.above();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!(level.getBlockState(pos).getBlock() instanceof FarmBlock) || !level.isInWorldBounds(above)) {
            return InteractionResult.PASS;
        }
        if (player != null && (!player.mayUseItemAt(pos, Direction.UP, stack) || !player.mayUseItemAt(above, Direction.UP, stack))) {
            return InteractionResult.PASS;
        }
        if (!level.getBlockState(above).canBeReplaced()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Aspect aspect = getAspect(stack);
        plant(level, pos, aspect != null ? aspect : Aspect.AIR, getTendencies(stack));
        level.playSound(null, above, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static void plant(Level level, BlockPos farmland, Aspect aspect, AspectList tendencies) {
        if (!(level.getBlockState(farmland).getBlock() instanceof InfusedFarmlandBlock)) {
            level.setBlockAndUpdate(farmland, CropsFeature.INFUSED_FARMLAND.defaultBlockState());
        }
        BlockState crop = CropsFeature.INFUSED_GRAIN.stateFor(aspect);
        BlockPos above = farmland.above();
        level.setBlockAndUpdate(above, crop);
        if (level.getBlockEntity(above) instanceof InfusedGrainBlockEntity grain) {
            grain.setup(aspect, tendencies);
        }
    }
}
