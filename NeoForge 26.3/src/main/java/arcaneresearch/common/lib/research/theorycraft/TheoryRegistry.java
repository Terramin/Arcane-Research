package arcaneresearch.common.lib.research.theorycraft;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.theorycraft.TheorycraftManager;

public class TheoryRegistry {

    public static void init() {
        Arcaneresearch.LOGGER.info("Registering theorycraft cards and aids");

        TheorycraftManager.registerCard(arcaneresearch.api.research.theorycraft.CardAnalyze.class);
        TheorycraftManager.registerCard(CardExperimentation.class);
        TheorycraftManager.registerCard(CardInspired.class);
        TheorycraftManager.registerCard(CardPonder.class);
        TheorycraftManager.registerCard(arcaneresearch.api.research.theorycraft.CardReject.class);
        TheorycraftManager.registerCard(arcaneresearch.api.research.theorycraft.CardRethink.class);

        TheorycraftManager.registerCard(CardAwareness.class);
        TheorycraftManager.registerCard(CardFocus.class);
        TheorycraftManager.registerCard(CardChannel.class);

        TheorycraftManager.registerCard(CardConcentrate.class);
        TheorycraftManager.registerCard(CardReactions.class);
        TheorycraftManager.registerCard(CardSynthesis.class);

        TheorycraftManager.registerCard(CardCalibrate.class);
        TheorycraftManager.registerCard(CardMeasure.class);
        TheorycraftManager.registerCard(CardTinker.class);

        TheorycraftManager.registerCard(CardMindOverMatter.class);
        TheorycraftManager.registerCard(CardScripting.class);
        TheorycraftManager.registerCard(CardSculpting.class);

        TheorycraftManager.registerCard(CardSpellbinding.class);
        TheorycraftManager.registerCard(CardSynergy.class);
        TheorycraftManager.registerCard(CardInfuse.class);

        TheorycraftManager.registerCard(CardRevelation.class);

        TheorycraftManager.registerCard(CardRealization.class);
        TheorycraftManager.registerCard(CardTruth.class);
        TheorycraftManager.registerCard(CardCurio.class);
        TheorycraftManager.registerCard(CardCelestial.class);

        TheorycraftManager.registerCard(CardBalance.class);
        TheorycraftManager.registerCard(CardNotation.class);
        TheorycraftManager.registerCard(CardStudy.class);
        TheorycraftManager.registerCard(CardDarkWhispers.class);
        TheorycraftManager.registerCard(CardEnchantment.class);
        TheorycraftManager.registerCard(CardGlyphs.class);
        TheorycraftManager.registerCard(CardBeacon.class);
        TheorycraftManager.registerCard(CardDragonEgg.class);
        TheorycraftManager.registerCard(CardPortal.class);

        TheorycraftManager.registerAid(new AidBookshelf());

        TheorycraftManager.registerAid(new AidEnchantmentTable());

        TheorycraftManager.registerAid(new AidBeacon());

        TheorycraftManager.registerAid(new AidDragonEgg());

        TheorycraftManager.registerAid(new AidBrainInAJar());

        TheorycraftManager.registerAid(new AidGlyphedStone());

        TheorycraftManager.registerAid(new AidPortal.AidPortalEnd());
        TheorycraftManager.registerAid(new AidPortal.AidPortalNether());
        TheorycraftManager.registerAid(new AidPortal.AidPortalCrimson());

        TheorycraftManager.registerAid(new AidBasicAlchemy());
        TheorycraftManager.registerAid(new AidBasicArtifice());
        TheorycraftManager.registerAid(new AidBasicAuromancy());
        TheorycraftManager.registerAid(new AidBasicGolemancy());
        TheorycraftManager.registerAid(new AidBasicInfusion());
        TheorycraftManager.registerAid(new AidBasicEldritch());

        Arcaneresearch.LOGGER.info("Registered {} theorycraft cards and {} aids",
                TheorycraftManager.cards.size(),
                TheorycraftManager.aids.size());
    }
}
