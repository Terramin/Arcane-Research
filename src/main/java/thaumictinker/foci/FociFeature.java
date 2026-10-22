package thaumictinker.foci;

import net.minecraft.world.item.Item;

import thaumictinker.net.TtNet;
import thaumictinker.registry.TtRegistry;

 



 
public final class FociFeature {

    public static final String SMELT = "FOCUS_SMELT";
    public static final String FLIGHT = "FOCUS_FLIGHT";
    public static final String DISLOCATION = "FOCUS_DISLOCATION";
    public static final String TELEKINESIS = "FOCUS_TELEKINESIS";
    public static final String DEFLECT = "FOCUS_DEFLECT";
    public static final String ENDER_CHEST = "FOCUS_ENDER_CHEST";
    public static final String HEAL = "FOCUS_HEAL";

    public static final Item FOCUS_FLIGHT = TtRegistry.itemWithTab("focus_flight", () -> new FlightFocus());
    public static final Item FOCUS_DISLOCATION = TtRegistry.itemWithTab("focus_dislocation", () -> new DislocationFocus());
    public static final Item FOCUS_TELEKINESIS = TtRegistry.itemWithTab("focus_telekinesis", () -> new TelekinesisFocus());
    public static final Item FOCUS_SMELT = TtRegistry.itemWithTab("focus_smelt", () -> new SmeltFocus());
    public static final Item FOCUS_HEAL = TtRegistry.itemWithTab("focus_heal", () -> new HealFocus());
    public static final Item FOCUS_ENDER_CHEST = TtRegistry.itemWithTab("focus_ender_chest", () -> new EnderChestFocus());
    public static final Item FOCUS_DEFLECT = TtRegistry.itemWithTab("focus_deflect", () -> new DeflectFocus());

    private FociFeature() {
    }

    public static void load() {
        TtNet.register();
    }
}
