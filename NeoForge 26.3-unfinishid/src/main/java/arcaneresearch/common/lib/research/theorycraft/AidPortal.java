package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;

public class AidPortal implements ITheorycraftAid {

    private final Object portal;

    public AidPortal(Object o) {
        this.portal = o;
    }

    @Override
    public Object getAidObject() {
        return portal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{CardPortal.class};
    }

    public static class AidPortalEnd extends AidPortal {
        public AidPortalEnd() {
            super(Blocks.END_PORTAL);
        }
    }

    public static class AidPortalNether extends AidPortal {
        public AidPortalNether() {
            super(Blocks.NETHER_PORTAL);
        }
    }

    public static class AidPortalCrimson extends AidPortal {
        public AidPortalCrimson() {
            super(EntityCultistPortalLesser.class);
        }
    }
}
