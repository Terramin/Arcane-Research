package arcaneresearch.common.golems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.parts.*;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
public class GolemProperties implements IGolemProperties {
    private long data = 0L;
    private Set<EnumGolemTrait> traitCache = null;
    public GolemProperties() {
    }
    public static IGolemProperties fromLong(long d) {
        GolemProperties out = new GolemProperties();
        out.data = d;
        return out;
    }
    private static void addToListFromComps(ArrayList<ItemStack> comps, Object[] objs, GolemMaterial mat) {
        for (Object o : objs) {
            if (o instanceof ItemStack stack) {
                addToList(comps, stack, 1);
            } else if (o instanceof String s) {
                if (s.equalsIgnoreCase("base")) {
                    addToList(comps, mat.componentBase, 1);
                } else if (s.equalsIgnoreCase("mech")) {
                    addToList(comps, mat.componentMechanism, 1);
                }
            }
        }
    }
    private static void addToList(ArrayList<ItemStack> comps, ItemStack newItem, int mult) {
        for (ItemStack stack : comps) {
            if (ItemStack.isSameItemSameTags(stack, newItem)) {
                stack.grow(newItem.getCount() * mult);
                return;
            }
        }
        ItemStack stack2 = newItem.copy();
        stack2.setCount(stack2.getCount() * mult);
        comps.add(stack2);
    }
    private static int getByteInLong(long value, int byteIndex) {
        return (int) ((value >> (byteIndex * 8)) & 0xFF);
    }
    private static long setByteInLong(long value, byte newByte, int byteIndex) {
        long mask = ~(0xFFL << (byteIndex * 8));
        return (value & mask) | ((long) (newByte & 0xFF) << (byteIndex * 8));
    }
    public static void registerDefaultParts() {
        GolemMaterial.register(new GolemMaterial("WOOD",
                new String[]{"MATSTUDWOOD"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_wood.png"),
                5059370, 6, 2, 1,
                new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.LIGHT}));
        GolemMaterial.register(new GolemMaterial("IRON",
                new String[]{"MATSTUDIRON"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_iron.png"),
                0xFFFFFF, 20, 8, 3,
                new ItemStack(ModItems.PLATE_IRON.get()),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.HEAVY, EnumGolemTrait.FIREPROOF, EnumGolemTrait.BLASTPROOF}));
        GolemMaterial.register(new GolemMaterial("CLAY",
                new String[]{"MATSTUDCLAY"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_clay.png"),
                13071447, 10, 4, 2,
                new ItemStack(Blocks.TERRACOTTA),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.FIREPROOF}));
        GolemMaterial.register(new GolemMaterial("BRASS",
                new String[]{"MATSTUDBRASS"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_brass.png"),
                15638812, 16, 6, 3,
                new ItemStack(ModItems.PLATE_BRASS.get()),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.LIGHT}));
        GolemMaterial.register(new GolemMaterial("THAUMIUM",
                new String[]{"MATSTUDTHAUMIUM"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_thaumium.png"),
                5257074, 24, 10, 4,
                new ItemStack(ModItems.PLATE_THAUMIUM.get()),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.HEAVY, EnumGolemTrait.FIREPROOF, EnumGolemTrait.BLASTPROOF}));
        GolemMaterial.register(new GolemMaterial("VOID",
                new String[]{"MATSTUDVOID"},
                new ResourceLocation("arcaneresearch", "textures/entity/golems/mat_void.png"),
                1445161, 20, 6, 4,
                new ItemStack(ModItems.PLATE_VOID.get()),
                new ItemStack(ModItems.MECHANISM_SIMPLE.get()),
                new EnumGolemTrait[]{EnumGolemTrait.REPAIR}));
        GolemHead.register(new GolemHead("BASIC",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/head_basic.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_head_basic.obj"),
                        null, PartModel.EnumAttachPoint.HEAD),
                new Object[]{new ItemStack(ModItems.BRAIN_CLOCKWORK.get())},
                new EnumGolemTrait[]{}));
        GolemHead.register(new GolemHead("SMART",
                new String[]{"MINDBIOTHAUMIC"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/head_smart.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_head_smart.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_head_other.png"),
                        PartModel.EnumAttachPoint.HEAD),
                new Object[]{new ItemStack(ModItems.BRAIN_CURIOUS.get())},
                new EnumGolemTrait[]{EnumGolemTrait.SMART, EnumGolemTrait.FRAGILE}));
        GolemHead.register(new GolemHead("SMART_ARMORED",
                new String[]{"MINDBIOTHAUMIC", "GOLEMCOMBATADV"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/head_smartarmor.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_head_smart_armor.obj"),
                        null, PartModel.EnumAttachPoint.HEAD),
                new Object[]{new ItemStack(ModItems.BRAIN_CURIOUS.get()),
                        new ItemStack(ModItems.PLATE_BRASS.get()), "base",
                        new ItemStack(Blocks.WHITE_WOOL)},
                new EnumGolemTrait[]{EnumGolemTrait.SMART}));
        GolemHead.register(new GolemHead("SCOUT",
                new String[]{"GOLEMVISION"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/head_scout.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_head_scout.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_head_other.png"),
                        PartModel.EnumAttachPoint.HEAD),
                new Object[]{new ItemStack(ModItems.BRAIN_CLOCKWORK.get()),
                        new ItemStack(ModItems.GOLEM_MODULE_VISION.get())},
                new EnumGolemTrait[]{EnumGolemTrait.SCOUT, EnumGolemTrait.FRAGILE}));
        GolemHead.register(new GolemHead("SMART_SCOUT",
                new String[]{"GOLEMVISION", "MINDBIOTHAUMIC"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/head_smartscout.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_head_scout_smart.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_head_other.png"),
                        PartModel.EnumAttachPoint.HEAD),
                new Object[]{new ItemStack(ModItems.BRAIN_CURIOUS.get()),
                        new ItemStack(ModItems.GOLEM_MODULE_VISION.get())},
                new EnumGolemTrait[]{EnumGolemTrait.SCOUT, EnumGolemTrait.SMART, EnumGolemTrait.FRAGILE}));
        GolemArm.register(new GolemArm("BASIC",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/arms_basic.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_arms_basic.obj"),
                        null, PartModel.EnumAttachPoint.ARMS),
                new Object[]{},
                new EnumGolemTrait[]{}));
        GolemArm.register(new GolemArm("FINE",
                new String[]{"MATSTUDBRASS"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/arms_fine.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_arms_fine.obj"),
                        null, PartModel.EnumAttachPoint.ARMS),
                new Object[]{new ItemStack(ModItems.MECHANISM_SIMPLE.get()), "base"},
                new EnumGolemTrait[]{EnumGolemTrait.DEFT, EnumGolemTrait.FRAGILE}));
        GolemArm.register(new GolemArm("CLAWS",
                new String[]{"GOLEMCOMBATADV"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/arms_claws.png"),
                arcaneresearch.proxies.Proxies.get().createGolemPartModel("CLAWS",
                        new ResourceLocation("arcaneresearch", "models/obj/golem_arms_claws.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_arms_claws.png"),
                        PartModel.EnumAttachPoint.ARMS),
                new Object[]{new ItemStack(ModItems.GOLEM_MODULE_AGGRESSION.get()),
                        new ItemStack(Items.SHEARS, 2), "base"},
                new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.CLUMSY, EnumGolemTrait.BRUTAL}));
        GolemArm.register(new GolemArm("BREAKERS",
                new String[]{"GOLEMBREAKER"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/arms_breakers.png"),
                arcaneresearch.proxies.Proxies.get().createGolemPartModel("BREAKERS",
                        new ResourceLocation("arcaneresearch", "models/obj/golem_arms_breakers.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_arms_breakers.png"),
                        PartModel.EnumAttachPoint.ARMS),
                new Object[]{new ItemStack(Items.DIAMOND, 2), "base", new ItemStack(Blocks.PISTON, 2)},
                new EnumGolemTrait[]{EnumGolemTrait.BREAKER, EnumGolemTrait.CLUMSY, EnumGolemTrait.BRUTAL}));
        GolemArm.register(new GolemArm("DARTS",
                new String[]{"GOLEMCOMBATADV"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/arms_darts.png"),
                arcaneresearch.proxies.Proxies.get().createGolemPartModel("DARTS",
                        new ResourceLocation("arcaneresearch", "models/obj/golem_arms_darter.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_arms_darter.png"),
                        PartModel.EnumAttachPoint.ARMS),
                new Object[]{new ItemStack(ModItems.GOLEM_MODULE_AGGRESSION.get()),
                        new ItemStack(Blocks.DISPENSER, 2), new ItemStack(Items.ARROW, 32), "mech"},
                new arcaneresearch.common.golems.parts.GolemArmDart(),
                new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.CLUMSY,
                        EnumGolemTrait.RANGED, EnumGolemTrait.FRAGILE}));
        GolemLeg.register(new GolemLeg("WALKER",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/legs_walker.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_legs_walker.obj"),
                        null, PartModel.EnumAttachPoint.LEGS),
                new Object[]{"base", "mech"},
                new EnumGolemTrait[]{}));
        GolemLeg.register(new GolemLeg("ROLLER",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/legs_roller.png"),
                arcaneresearch.proxies.Proxies.get().createGolemPartModel("WHEEL",
                        new ResourceLocation("arcaneresearch", "models/obj/golem_legs_wheel.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_legs_wheel.png"),
                        PartModel.EnumAttachPoint.BODY),
                new Object[]{new ItemStack(Items.BOWL, 2), new ItemStack(Items.LEATHER), "mech"},
                new arcaneresearch.common.golems.parts.GolemLegWheels(),
                new EnumGolemTrait[]{EnumGolemTrait.WHEELED}));
        GolemLeg.register(new GolemLeg("CLIMBER",
                new String[]{"GOLEMCLIMBER"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/legs_climber.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_legs_climber.obj"),
                        new ResourceLocation("arcaneresearch", "textures/block/base_metal.png"),
                        PartModel.EnumAttachPoint.LEGS),
                new Object[]{new ItemStack(Items.FLINT, 4), "base", "mech", "mech"},
                new EnumGolemTrait[]{EnumGolemTrait.CLIMBER}));
        GolemLeg.register(new GolemLeg("FLYER",
                new String[]{"GOLEMFLYER"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/legs_flyer.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_legs_floater.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_legs_floater.png"),
                        PartModel.EnumAttachPoint.BODY),
                new Object[]{new ItemStack(ModBlocks.LEVITATOR.get()),
                        new ItemStack(ModItems.PLATE_BRASS.get(), 4),
                        new ItemStack(Items.SLIME_BALL), "mech"},
                new arcaneresearch.common.golems.parts.GolemLegLevitator(),
                new EnumGolemTrait[]{EnumGolemTrait.FLYER, EnumGolemTrait.FRAGILE}));
        GolemAddon.register(new GolemAddon("NONE",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/block/empty.png"),
                null,
                new Object[]{},
                new EnumGolemTrait[]{}));
        GolemAddon.register(new GolemAddon("ARMORED",
                new String[]{"GOLEMCOMBATADV"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/addon_armored.png"),
                new PartModel(new ResourceLocation("arcaneresearch", "models/obj/golem_armor.obj"),
                        null, PartModel.EnumAttachPoint.BODY),
                new Object[]{"base", "base", "base", "base"},
                new EnumGolemTrait[]{EnumGolemTrait.ARMORED, EnumGolemTrait.HEAVY}));
        GolemAddon.register(new GolemAddon("FIGHTER",
                new String[]{"SEALGUARD"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/addon_fighter.png"),
                null,
                new Object[]{new ItemStack(ModItems.GOLEM_MODULE_AGGRESSION.get()), "mech"},
                new EnumGolemTrait[]{EnumGolemTrait.FIGHTER}));
        GolemAddon.register(new GolemAddon("HAULER",
                new String[]{"MINDCLOCKWORK"},
                new ResourceLocation("arcaneresearch", "textures/misc/golem/addon_hauler.png"),
                arcaneresearch.proxies.Proxies.get().createGolemPartModel("HAULER",
                        new ResourceLocation("arcaneresearch", "models/obj/golem_hauler.obj"),
                        new ResourceLocation("arcaneresearch", "textures/entity/golems/golem_hauler.png"),
                        PartModel.EnumAttachPoint.BODY),
                new Object[]{new ItemStack(Items.LEATHER), new ItemStack(Blocks.CHEST)},
                new EnumGolemTrait[]{EnumGolemTrait.HAULER}));
    }
    @Override
    public Set<EnumGolemTrait> getTraits() {
        if (traitCache == null) {
            traitCache = new HashSet<>();
            for (EnumGolemTrait trait : getMaterial().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getHead().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getArms().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getLegs().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getAddon().traits) {
                addTraitSmart(trait);
            }
        }
        return traitCache;
    }
    private void addTraitSmart(EnumGolemTrait trait) {
        if (trait.opposite != null && traitCache.contains(trait.opposite)) {
            traitCache.remove(trait.opposite);
        } else {
            traitCache.add(trait);
        }
    }
    @Override
    public boolean hasTrait(EnumGolemTrait trait) {
        return getTraits().contains(trait);
    }
    @Override
    public GolemMaterial getMaterial() {
        return GolemMaterial.getById(getByteInLong(data, 0));
    }
    @Override
    public void setMaterial(GolemMaterial mat) {
        data = setByteInLong(data, mat.id, 0);
        traitCache = null;
    }
    @Override
    public GolemHead getHead() {
        return GolemHead.getById(getByteInLong(data, 1));
    }
    @Override
    public void setHead(GolemHead head) {
        data = setByteInLong(data, head.id, 1);
        traitCache = null;
    }
    @Override
    public GolemArm getArms() {
        return GolemArm.getById(getByteInLong(data, 2));
    }
    @Override
    public void setArms(GolemArm arms) {
        data = setByteInLong(data, arms.id, 2);
        traitCache = null;
    }
    @Override
    public GolemLeg getLegs() {
        return GolemLeg.getById(getByteInLong(data, 3));
    }
    @Override
    public void setLegs(GolemLeg legs) {
        data = setByteInLong(data, legs.id, 3);
        traitCache = null;
    }
    @Override
    public GolemAddon getAddon() {
        return GolemAddon.getById(getByteInLong(data, 4));
    }
    @Override
    public void setAddon(GolemAddon addon) {
        data = setByteInLong(data, addon.id, 4);
        traitCache = null;
    }
    @Override
    public int getRank() {
        return getByteInLong(data, 5);
    }
    @Override
    public void setRank(int rank) {
        data = setByteInLong(data, (byte) rank, 5);
    }
    @Override
    public long toLong() {
        return data;
    }
    @Override
    public ItemStack[] generateComponents() {
        ArrayList<ItemStack> comps = new ArrayList<>();
        ItemStack base = getMaterial().componentBase;
        ItemStack mech = getMaterial().componentMechanism;
        addToList(comps, base, 2);
        addToList(comps, mech, 1);
        addToListFromComps(comps, getArms().components, getMaterial());
        addToListFromComps(comps, getLegs().components, getMaterial());
        addToListFromComps(comps, getHead().components, getMaterial());
        addToListFromComps(comps, getAddon().components, getMaterial());
        return comps.toArray(new ItemStack[0]);
    }
}
