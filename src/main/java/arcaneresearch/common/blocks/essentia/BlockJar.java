package arcaneresearch.common.blocks.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.blocks.ILabelable;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.consumables.ItemLabel;
import arcaneresearch.common.tiles.essentia.TileJar;
import arcaneresearch.common.tiles.essentia.TileJarBrain;
import arcaneresearch.common.tiles.essentia.TileJarVoid;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockJar extends Block implements EntityBlock, ILabelable {

    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);
    private final JarType jarType;

    public BlockJar(BlockBehaviour.Properties props, JarType type) {
        super(props
                .mapColor(MapColor.NONE)
                .strength(0.3f, 20.0f)
                .sound(ModSounds.JAR_TYPE)
                .noOcclusion());
        this.jarType = type;
    }

    private static void orientJarFacing(TileJar jar, float yaw) {
        int l = Mth.floor(yaw * 4.0f / 360.0f + 0.5f) & 3;
        if (l == 0) jar.setFacing(2);
        else if (l == 1) jar.setFacing(5);
        else if (l == 2) jar.setFacing(3);
        else jar.setFacing(4);
    }

    private static Aspect getLabelAspect(ItemStack labelStack) {
        if (labelStack.getItem() instanceof ItemLabel label) {
            Aspect a = label.getAspect(labelStack);
            if (a != null) return a;
        }
        if (labelStack.getItem() instanceof IEssentiaContainerItem container) {
            AspectList aspects = container.getAspects(labelStack);
            if (aspects != null && aspects.size() > 0) {
                return aspects.getAspects()[0];
            }
        }
        return null;
    }

    public static BlockJar createNormal(BlockBehaviour.Properties props) {
        return new BlockJar(props, JarType.NORMAL);
    }

    public static BlockJar createVoid(BlockBehaviour.Properties props) {
        return new BlockJar(props, JarType.VOID);
    }

    public static BlockJar createBrain(BlockBehaviour.Properties props) {
        return new BlockJar(props, JarType.BRAIN);
    }

    public JarType getJarType() {
        return jarType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity te = level.getBlockEntity(pos);

        if (te instanceof TileJar jar) {
            AspectList aspects = BlockJarItem.getAspectsStatic(stack);
            if (aspects != null) {
                jar.setAspects(aspects);
            }
            net.minecraft.nbt.CompoundTag stackTag =
                    arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
            if (stackTag != null && stackTag.contains("AspectFilter")) {
                jar.setAspectFilter(Aspect.getAspect(stackTag.getStringOr("AspectFilter", "")));
            }
            jar.markDirtyAndSync();
            level.sendBlockUpdated(pos, state, state, 3);
        } else if (te instanceof TileJarBrain brain) {
            net.minecraft.nbt.CompoundTag brainTag =
                    arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
            if (brainTag != null) {
                if (brainTag.contains("xp")) {
                    brain.xp = brainTag.getIntOr("xp", 0);
                } else if (brainTag.contains("XP")) {
                    brain.xp = brainTag.getIntOr("XP", 0);
                }
            }
            brain.markDirtyAndSync();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (placer != null && level.getBlockEntity(pos) instanceof TileJar jar) {
            orientJarFacing(jar, placer.getYRot());
        }
    }

    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) split in 26.x;
    // this interaction uses hand+hit -> useItemOn (same body, same order).
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof TileJarBrain brain) {
            if (level.isClientSide()) {
                level.playSound(player, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.2f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            brain.eatDelay = 40;
            int var6 = level.getRandom().nextInt(Math.min(brain.xp + 1, 64));
            if (var6 > 0) {
                brain.xp -= var6;
                int xp = var6;
                while (xp > 0) {
                    int split = ExperienceOrb.getExperienceValue(xp);
                    xp -= split;
                    level.addFreshEntity(new ExperienceOrb(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, split));
                }
                level.sendBlockUpdated(pos, state, state, 3);
                brain.setChanged();
            }
            return InteractionResult.SUCCESS;
        }

        if (!(blockEntity instanceof TileJar jar)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        Direction side = hit.getDirection();

        if (!jar.isBlocked() && held.is(ModItems.JAR_BRACE.get())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            jar.setBlocked(true);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && jar.getAspectFilter() != null && side.get3DDataValue() == jar.getFacing()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            jar.setAspectFilter(null);
            level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            ItemStack labelDrop = new ItemStack(ModItems.LABEL_BLANK.get());
            level.addFreshEntity(new ItemEntity(level,
                    pos.getX() + 0.5 + side.getStepX() / 3.0,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + side.getStepZ() / 3.0, labelDrop));
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (level.isClientSide()) {
                level.playSound(player, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f);
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5f,
                        1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f, false);
                return InteractionResult.SUCCESS;
            }
            if (jar.getAspectFilter() == null) {
                jar.setAspect(null, 0);
            }
            AuraHelper.polluteAura(level, pos, jar.getAmount(), true);
            jar.setAspect(jar.getAspect(), 0);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelStack) {
        if (player.level().isClientSide()) {
            return true;
        }
        BlockEntity te = player.level().getBlockEntity(pos);
        if (!(te instanceof TileJar jar) || jar.getAspectFilter() != null) {
            return false;
        }
        Aspect labelAspect = getLabelAspect(labelStack);
        if (jar.getAmount() == 0 && labelAspect == null) {
            return false;
        }
        if (jar.getAmount() == 0 && labelAspect != null) {
            jar.setAspect(labelAspect, jar.getAmount());
        }
        orientJarFacing(jar, player.getYRot());
        jar.setAspectFilter(jar.getAspect());
        Level level = player.level();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f);
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof TileJarBrain brain) {
            ItemStack drop = new ItemStack(this);
            if (brain.xp > 0) {
                // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same keys).
                net.minecraft.nbt.CompoundTag xpTag =
                        arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(drop);
                xpTag.putInt("xp", brain.xp);
                xpTag.putInt("XP", brain.xp);
                arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(drop, xpTag);
            }
            return Collections.singletonList(drop);
        }
        if (be instanceof TileJar jar) {
            List<ItemStack> drops = new ArrayList<>();
            ItemStack drop = new ItemStack(this);
            if (jar.getAmount() > 0 && jar.getAspect() != null) {
                BlockJarItem.setAspectsStatic(drop, new AspectList().add(jar.getAspect(), jar.getAmount()));
            }
            if (jar.getAspectFilter() != null) {
                net.minecraft.nbt.CompoundTag filterTag =
                        arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(drop);
                filterTag.putString("AspectFilter", jar.getAspectFilter().getTag());
                arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(drop, filterTag);
            }
            drops.add(drop);
            if (jar.isBlocked()) {
                drops.add(new ItemStack(ModItems.JAR_BRACE.get()));
            }
            return drops;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(this);
        // NEOFORGE_PORT (26.3): no includeData guard - the 1.20.1 override always copied the
        // jar contents/label/xp (plain pick block and scanning saw the filled jar).
        {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileJarBrain brain && brain.xp > 0) {
                // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
                net.minecraft.world.item.component.CustomData.update(
                        net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                        tag -> tag.putInt("xp", brain.xp));
            } else if (be instanceof TileJar jar) {
                if (jar.getAmount() > 0 && jar.getAspect() != null) {
                    AspectList aspects = new AspectList().add(jar.getAspect(), jar.getAmount());
                    net.minecraft.world.item.component.CustomData.update(
                            net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                            tag -> aspects.writeToNBT(tag));
                }
                if (jar.getAspectFilter() != null) {
                    String filterTag = jar.getAspectFilter().getTag();
                    net.minecraft.world.item.component.CustomData.update(
                            net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                            tag -> tag.putString("AspectFilter", filterTag));
                }
            }
        }
        return stack;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)). Inventory drops are automatic
        // now (BlockEntity.preRemoveSideEffects default drops Container contents).
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);


    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileJarBrain brain) {
            float r = brain.xp / (float) TileJarBrain.MAX_XP;
            return net.minecraft.util.Mth.floor(r * 14.0f) + (brain.xp > 0 ? 1 : 0);
        }
        if (tile instanceof TileJar jar) {
            float r = jar.getAmount() / 250.0f;
            return net.minecraft.util.Mth.floor(r * 14.0f) + (jar.getAmount() > 0 ? 1 : 0);
        }
        return super.getAnalogOutputSignal(state, level, pos, direction);
    }

    // NEOFORGE_PORT: Forge vanilla hook moved to IBlockExtension in 26.x with
    // BlockGetter instead of LevelReader (same 5.0f brain-jar bonus, same logic).
    @Override
    public float getEnchantPowerBonus(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileJarBrain) {
            return 5.0f;
        }
        return super.getEnchantPowerBonus(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileJarBrain brain && brain.xp >= TileJarBrain.MAX_XP
                && FXDispatcher.INSTANCE != null) {
            FXDispatcher.INSTANCE.spark(pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    3.0f, 0.2f + rand.nextFloat() * 0.2f, 1.0f, 0.3f + rand.nextFloat() * 0.2f, 0.5f);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (jarType) {
            case BRAIN -> new TileJarBrain(pos, state);
            case VOID -> new TileJarVoid(pos, state);
            default -> new TileJar(pos, state);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() && jarType != JarType.BRAIN) {
            return null;
        }

        return switch (jarType) {
            case BRAIN -> {
                if (type == ModBlockEntities.JAR_BRAIN.get()) {
                    if (level.isClientSide()) {
                        yield (lvl, pos, st, te) -> TileJarBrain.clientTick(lvl, pos, st, (TileJarBrain) te);
                    } else {
                        yield (lvl, pos, st, te) -> TileJarBrain.serverTick(lvl, pos, st, (TileJarBrain) te);
                    }
                }
                yield null;
            }
            case VOID -> type == ModBlockEntities.JAR_VOID.get() ?
                    (lvl, pos, st, te) -> TileJarVoid.serverTick(lvl, pos, st, (TileJarVoid) te) : null;
            default -> type == ModBlockEntities.JAR.get() ?
                    (lvl, pos, st, te) -> TileJar.serverTick(lvl, pos, st, (TileJar) te) : null;
        };
    }

    public enum JarType {
        NORMAL(250),
        VOID(250),
        BRAIN(0);

        private final int capacity;

        JarType(int capacity) {
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}