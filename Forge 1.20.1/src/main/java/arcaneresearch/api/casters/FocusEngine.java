package arcaneresearch.api.casters;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.api.aspects.Aspect;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
public class FocusEngine {
    private static final Map<String, Supplier<? extends FocusNode>> FOCUS_REGISTRY = new HashMap<>();
    public static final Map<String, Class<? extends FocusNode>> elements = new HashMap<>();
    private static final Map<String, Integer> ELEMENT_COLORS = new HashMap<>();
    private static final Map<String, ResourceLocation> ELEMENT_ICONS = new HashMap<>();
    private static final Map<Aspect, Integer> ASPECT_COLORS = new HashMap<>();
    static {
        initAspectColors();
    }
    public static void registerFocusNode(String key, Supplier<? extends FocusNode> factory) {
        FOCUS_REGISTRY.put(key, factory);
        FocusNode sample = factory.get();
        if (sample != null) {
            elements.put(key, sample.getClass());
        }
    }
    public static void registerFocusNode(String key, Supplier<? extends FocusNode> factory, int color) {
        FOCUS_REGISTRY.put(key, factory);
        ELEMENT_COLORS.put(key, color);
        FocusNode sample = factory.get();
        if (sample != null) {
            elements.put(key, sample.getClass());
        }
    }
    public static void registerFocusNode(String key, Class<? extends FocusNode> nodeClass) {
        elements.put(key, nodeClass);
        FOCUS_REGISTRY.put(key, () -> {
            try {
                return nodeClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        });
    }
    @Nullable
    public static FocusNode createFocusNode(String key) {
        Supplier<? extends FocusNode> factory = FOCUS_REGISTRY.get(key);
        if (factory != null) {
            return factory.get();
        }
        return null;
    }
    public static boolean isRegistered(String key) {
        return FOCUS_REGISTRY.containsKey(key);
    }
    public static Iterable<String> getRegisteredKeys() {
        return FOCUS_REGISTRY.keySet();
    }
    public static int getElementColor(String key) {
        if (ELEMENT_COLORS.containsKey(key)) {
            return ELEMENT_COLORS.get(key);
        }
        FocusNode node = createFocusNode(key);
        if (node != null) {
            Aspect aspect = node.getAspect();
            if (aspect != null && ASPECT_COLORS.containsKey(aspect)) {
                return ASPECT_COLORS.get(aspect);
            }
        }
        return 0xFFFFFF;
    }
    public static void setElementColor(String key, int color) {
        ELEMENT_COLORS.put(key, color);
    }
    public static int getAspectColor(Aspect aspect) {
        return ASPECT_COLORS.getOrDefault(aspect, 0xFFFFFF);
    }
    private static void initAspectColors() {
        ASPECT_COLORS.put(Aspect.AIR, 0xFFFF7E);
        ASPECT_COLORS.put(Aspect.EARTH, 0x56C000);
        ASPECT_COLORS.put(Aspect.FIRE, 0xFF5A01);
        ASPECT_COLORS.put(Aspect.WATER, 0x3CD4FC);
        ASPECT_COLORS.put(Aspect.ORDER, 0xD5D4EC);
        ASPECT_COLORS.put(Aspect.ENTROPY, 0x404040);
        ASPECT_COLORS.put(Aspect.LIFE, 0xDE0005);
        ASPECT_COLORS.put(Aspect.DEATH, 0x6A0005);
        ASPECT_COLORS.put(Aspect.COLD, 0x00BFFF);
        ASPECT_COLORS.put(Aspect.FLUX, 0x800080);
        ASPECT_COLORS.put(Aspect.MOTION, 0xCDCCF4);
        ASPECT_COLORS.put(Aspect.ENERGY, 0xFFFF00);
        ASPECT_COLORS.put(Aspect.AVERSION, 0xC05050);
        ASPECT_COLORS.put(Aspect.ALCHEMY, 0x23AC64);
        ASPECT_COLORS.put(Aspect.TRAP, 0x9A8080);
        ASPECT_COLORS.put(Aspect.CRAFT, 0x6991C7);
        ASPECT_COLORS.put(Aspect.EXCHANGE, 0x87CEEB);
        ASPECT_COLORS.put(Aspect.ELDRITCH, 0xAA00AA);
    }
    public static int combineColors(int... colors) {
        if (colors.length == 0) return 0xFFFFFF;
        if (colors.length == 1) return colors[0];
        int r = 0, g = 0, b = 0;
        for (int color : colors) {
            Color c = new Color(color);
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        r /= colors.length;
        g /= colors.length;
        b /= colors.length;
        return new Color(r, g, b).getRGB();
    }
    public static void clearRegistry() {
        FOCUS_REGISTRY.clear();
        ELEMENT_COLORS.clear();
        ELEMENT_ICONS.clear();
        elements.clear();
    }
    public static void registerElement(Class<? extends IFocusElement> element, ResourceLocation icon, int color) {
        try {
            IFocusElement fe = element.getDeclaredConstructor().newInstance();
            String key = fe.getKey();
            if (fe instanceof FocusNode node) {
                registerFocusNode(key, node.getClass());
            }
            ELEMENT_ICONS.put(key, icon);
            ELEMENT_COLORS.put(key, color);
        } catch (Exception ignored) {
        }
    }
    public static ResourceLocation getElementIcon(String key) {
        ResourceLocation custom = ELEMENT_ICONS.get(key);
        if (custom != null) {
            return custom;
        }
        String[] parts = key.split("\\.");
        if (parts.length >= 2) {
            String modid = parts[0].toLowerCase();
            String name = parts[1].toLowerCase();
            return new ResourceLocation(modid, "textures/foci/" + name + ".png");
        }
        return new ResourceLocation("arcaneresearch", "textures/foci/unknown.png");
    }
    @Nullable
    public static IFocusElement getElement(String key) {
        return createFocusNode(key);
    }
    public static boolean doesPackageContainElement(FocusPackage pack, String elementKey) {
        if (pack == null || elementKey == null || pack.nodes == null) {
            return false;
        }
        for (IFocusElement element : pack.nodes) {
            if (element != null && elementKey.equals(element.getKey())) {
                return true;
            }
        }
        return false;
    }
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage, boolean nocopy) {
        FocusPackage focusPackageCopy;
        if (nocopy)
            focusPackageCopy = focusPackage;
        else
            focusPackageCopy = focusPackage.copy(caster);
        focusPackageCopy.initialize(caster);
        focusPackageCopy.setUniqueID(java.util.UUID.randomUUID());
        for (FocusEffect effect : focusPackageCopy.getFocusEffects()) {
            effect.onCast(caster);
        }
        runFocusPackage(focusPackageCopy, null, null);
    }
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage) {
        castFocusPackage(caster, focusPackage, false);
    }
    public static boolean castFocusPackageSimple(LivingEntity caster, FocusPackage pack) {
        if (pack == null || caster == null) return false;
        castFocusPackage(caster, pack);
        return true;
    }
    public static void runFocusPackage(FocusPackage focusPackage, Trajectory[] trajectories, HitResult[] targets) {
        Trajectory[] prevTrajectories = trajectories;
        HitResult[] prevTargets = targets;
        synchronized (focusPackage.nodes) {
            if (focusPackage.nodes.isEmpty()) return;
            if (!(focusPackage.nodes.get(0) instanceof FocusMediumRoot)) {
                focusPackage.nodes.add(0, new FocusMediumRoot(trajectories, targets));
            }
            for (int idx = 0; idx < focusPackage.nodes.size(); idx++) {
                focusPackage.setExecutionIndex(idx);
                IFocusElement node = focusPackage.nodes.get(idx);
                if (idx > 0 && node instanceof FocusNode fn && fn.getParent() == null) {
                    IFocusElement nodePrev = focusPackage.nodes.get(idx - 1);
                    if (nodePrev instanceof FocusNode prevNode) {
                        fn.setParent(prevNode);
                    }
                }
                if (node instanceof FocusNode fn && fn.getPackage() == null) {
                    fn.setPackage(focusPackage);
                }
                if (node instanceof FocusNode fn) {
                    focusPackage.multiplyPower(fn.getPowerMultiplier());
                }
                if (node instanceof FocusPackage sub) {
                    runFocusPackage(sub, prevTrajectories, prevTargets);
                    break;
                } else if (node instanceof FocusMedium medium) {
                    if (prevTrajectories != null)
                        for (Trajectory trajectory : prevTrajectories) {
                            medium.execute(trajectory);
                        }
                    if (medium.hasIntermediary()) break;
                } else if (node instanceof FocusMod mod) {
                    if (node instanceof FocusModSplit split) {
                        for (FocusPackage sp : split.getSplitPackages()) {
                            split.setPackage(sp);
                            sp.multiplyPower(focusPackage.getPower());
                            split.execute();
                            runFocusPackage(sp, split.supplyTrajectories(), split.supplyTargets());
                        }
                        break;
                    } else {
                        mod.execute();
                    }
                } else if (node instanceof FocusEffect effect) {
                    if (prevTargets != null) {
                        int num = 0;
                        for (HitResult target : prevTargets) {
                            if (target instanceof net.minecraft.world.phys.EntityHitResult ehr
                                    && ehr.getEntity() != null) {
                                String k = ehr.getEntity().getId() + focusPackage.getUniqueID().toString();
                                if (damageResistList.contains(k) && ehr.getEntity().invulnerableTime > 0) {
                                    ehr.getEntity().invulnerableTime = 0;
                                } else {
                                    if (damageResistList.size() > 10) damageResistList.remove(0);
                                    damageResistList.add(k);
                                }
                            }
                            Trajectory tra = prevTrajectories != null
                                    ? ((prevTrajectories.length == prevTargets.length) ? prevTrajectories[num] : prevTrajectories[0])
                                    : null;
                            effect.execute(target, tra, focusPackage.getPower(), num);
                            num++;
                        }
                    }
                }
                if (node instanceof FocusNode fn) {
                    prevTrajectories = fn.supplyTrajectories();
                    prevTargets = fn.supplyTargets();
                }
            }
        }
    }
    private static java.util.ArrayList<String> damageResistList = new java.util.ArrayList<>();
}
