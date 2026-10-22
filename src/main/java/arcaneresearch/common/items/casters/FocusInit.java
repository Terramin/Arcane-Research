package arcaneresearch.common.items.casters;

import net.minecraft.resources.Identifier;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.FocusMediumRoot;
import arcaneresearch.common.items.casters.foci.*;

public class FocusInit {

    public static void registerFoci() {

        
        FocusEngine.registerElement(FocusMediumRoot.class,
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/foci/root.png"), 0x999999);

        FocusEngine.registerFocusNode("arcaneresearch.TOUCH", FocusMediumTouch::new, 0xAD8585);

        FocusEngine.registerFocusNode("arcaneresearch.BOLT", FocusMediumBolt::new, 0xAD9985);
        FocusEngine.registerFocusNode("arcaneresearch.PROJECTILE", FocusMediumProjectile::new, 0xADAD85);
        FocusEngine.registerFocusNode("arcaneresearch.CLOUD", FocusMediumCloud::new, 0x99AD85);
        FocusEngine.registerFocusNode("arcaneresearch.MINE", FocusMediumMine::new, 0x85AD85);
        FocusEngine.registerFocusNode("arcaneresearch.PLAN", FocusMediumPlan::new, 0x85AD98);
        FocusEngine.registerFocusNode("arcaneresearch.SPELLBAT", FocusMediumSpellBat::new, 0x85ADAC);

        FocusEngine.registerFocusNode("arcaneresearch.FIRE", FocusEffectFire::new, 0xFF5A01);
        FocusEngine.registerFocusNode("arcaneresearch.FROST", FocusEffectFrost::new, 0xE1FFFF);
        FocusEngine.registerFocusNode("arcaneresearch.AIR", FocusEffectAir::new, 0xFFFF7E);
        FocusEngine.registerFocusNode("arcaneresearch.EARTH", FocusEffectEarth::new, 0x56C000);

        FocusEngine.registerFocusNode("arcaneresearch.FLUX", FocusEffectFlux::new, 0x800080);
        FocusEngine.registerFocusNode("arcaneresearch.HEAL", FocusEffectHeal::new, 0xDE0005);
        FocusEngine.registerFocusNode("arcaneresearch.BREAK", FocusEffectBreak::new, 0x8A4B08);
        FocusEngine.registerFocusNode("arcaneresearch.EXCHANGE", FocusEffectExchange::new, 0x578357);
        FocusEngine.registerFocusNode("arcaneresearch.CURSE", FocusEffectCurse::new, 0x6A0005);
        FocusEngine.registerFocusNode("arcaneresearch.RIFT", FocusEffectRift::new, 0x2F1165);

        FocusEngine.registerFocusNode("arcaneresearch.SCATTER", FocusModScatter::new, 0x999999);
        FocusEngine.registerFocusNode("arcaneresearch.SPLITTRAJECTORY", FocusModSplitTrajectory::new, 0x999999);
        FocusEngine.registerFocusNode("arcaneresearch.SPLITTARGET", FocusModSplitTarget::new, 0x999999);

        FocusEngine.registerElement(FocusModSplitTrajectory.class,
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/foci/split_trajectory.png"), 0x999999);
        FocusEngine.registerElement(FocusModSplitTarget.class,
                Identifier.fromNamespaceAndPath("arcaneresearch", "textures/foci/split_target.png"), 0x999999);
    }

    public static String[] getAllFocusKeys() {
        return new String[]{

                "arcaneresearch.TOUCH",

                "arcaneresearch.BOLT",
                "arcaneresearch.PROJECTILE",
                "arcaneresearch.CLOUD",
                "arcaneresearch.MINE",
                "arcaneresearch.PLAN",
                "arcaneresearch.SPELLBAT",

                "arcaneresearch.FIRE",
                "arcaneresearch.FROST",
                "arcaneresearch.AIR",
                "arcaneresearch.EARTH",
                "arcaneresearch.FLUX",
                "arcaneresearch.HEAL",
                "arcaneresearch.BREAK",
                "arcaneresearch.EXCHANGE",
                "arcaneresearch.CURSE",
                "arcaneresearch.RIFT",

                "arcaneresearch.SCATTER",
                "arcaneresearch.SPLITTRAJECTORY",
                "arcaneresearch.SPLITTARGET"
        };
    }
}
