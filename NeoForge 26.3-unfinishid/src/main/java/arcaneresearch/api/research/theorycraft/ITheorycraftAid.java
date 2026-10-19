package arcaneresearch.api.research.theorycraft;

public interface ITheorycraftAid {

    Object getAidObject();

    Class<? extends TheorycraftCard>[] getCards();
}
