package arcaneresearch.common.lib.research.theorycraft;

import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;

public class AidBookshelf implements ITheorycraftAid {

    @Override
    public Object getAidObject() {
        return Blocks.BOOKSHELF;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends TheorycraftCard>[] getCards() {
        return new Class[]{
                CardBalance.class,
                CardNotation.class,
                CardNotation.class,
                CardStudy.class,
                CardStudy.class,
                CardStudy.class
        };
    }
}
