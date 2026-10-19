package arcaneresearch.init;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import java.util.function.Consumer;
public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Arcaneresearch.MODID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Arcaneresearch.MODID);
    public static final RegistryObject<FluidType> LIQUID_DEATH_TYPE = FLUID_TYPES.register("liquid_death",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1500)
                    .rarity(Rarity.RARE)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                private static final ResourceLocation STILL =
                        new ResourceLocation(Arcaneresearch.MODID, "block/animatedglow");
                @Override
                @OnlyIn(Dist.CLIENT)
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }
                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return STILL;
                        }
                        @Override
                        public int getTintColor() {
                            return -263978855;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> LIQUID_DEATH = FLUIDS.register("liquid_death",
            () -> new ForgeFlowingFluid.Source(ModFluids.LIQUID_DEATH_PROPERTIES));
    public static final RegistryObject<FlowingFluid> LIQUID_DEATH_FLOWING = FLUIDS.register("liquid_death_flowing",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.LIQUID_DEATH_PROPERTIES));
    public static final RegistryObject<Item> LIQUID_DEATH_BUCKET = ModItems.ITEMS.register("bucket_death",
            () -> new BucketItem(ModFluids.LIQUID_DEATH,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final ForgeFlowingFluid.Properties LIQUID_DEATH_PROPERTIES =
            new ForgeFlowingFluid.Properties(ModFluids.LIQUID_DEATH_TYPE,
                    ModFluids.LIQUID_DEATH, ModFluids.LIQUID_DEATH_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.LIQUID_DEATH.get())
                    .bucket(() -> (BucketItem) ModFluids.LIQUID_DEATH_BUCKET.get())
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(10);
    public static final RegistryObject<FluidType> PURIFYING_FLUID_TYPE = FLUID_TYPES.register("purifying_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1000)
                    .rarity(Rarity.RARE)
                    .lightLevel(5)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                private static final ResourceLocation STILL =
                        new ResourceLocation("minecraft", "block/water_still");
                private static final ResourceLocation FLOWING =
                        new ResourceLocation("minecraft", "block/water_flow");
                @Override
                @OnlyIn(Dist.CLIENT)
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }
                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOWING;
                        }
                        @Override
                        public int getTintColor() {
                            return 2013252778;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> PURIFYING_FLUID = FLUIDS.register("purifying_fluid",
            () -> new ForgeFlowingFluid.Source(ModFluids.PURIFYING_FLUID_PROPERTIES));
    public static final RegistryObject<FlowingFluid> PURIFYING_FLUID_FLOWING = FLUIDS.register("purifying_fluid_flowing",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.PURIFYING_FLUID_PROPERTIES));
    public static final RegistryObject<Item> PURIFYING_FLUID_BUCKET = ModItems.ITEMS.register("bucket_pure",
            () -> new BucketItem(ModFluids.PURIFYING_FLUID,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final ForgeFlowingFluid.Properties PURIFYING_FLUID_PROPERTIES =
            new ForgeFlowingFluid.Properties(ModFluids.PURIFYING_FLUID_TYPE,
                    ModFluids.PURIFYING_FLUID, ModFluids.PURIFYING_FLUID_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.PURIFYING_FLUID.get())
                    .bucket(() -> (BucketItem) ModFluids.PURIFYING_FLUID_BUCKET.get())
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(10);
    public static final RegistryObject<FluidType> FLUX_GAS_TYPE = FLUID_TYPES.register("flux_gas",
            () -> new FluidType(FluidType.Properties.create()
                    .density(-1000)
                    .viscosity(500)
                    .rarity(Rarity.UNCOMMON)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                private static final ResourceLocation STILL =
                        new ResourceLocation(Arcaneresearch.MODID, "block/fluxgas");
                @Override
                @OnlyIn(Dist.CLIENT)
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }
                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return STILL;
                        }
                        @Override
                        public int getTintColor() {
                            return -1;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> FLUX_GAS = FLUIDS.register("flux_gas",
            () -> new ForgeFlowingFluid.Source(ModFluids.FLUX_GAS_PROPERTIES));
    public static final RegistryObject<FlowingFluid> FLUX_GAS_FLOWING = FLUIDS.register("flux_gas_flowing",
            () -> new ForgeFlowingFluid.Flowing(ModFluids.FLUX_GAS_PROPERTIES));
    public static final ForgeFlowingFluid.Properties FLUX_GAS_PROPERTIES =
            new ForgeFlowingFluid.Properties(ModFluids.FLUX_GAS_TYPE,
                    ModFluids.FLUX_GAS, ModFluids.FLUX_GAS_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.FLUX_GAS.get())
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2)
                    .tickRate(20);
}
