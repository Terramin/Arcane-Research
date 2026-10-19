package arcaneresearch.common.golems.ai;
public interface ITargets {
    boolean getTargetAnimal();
    void setTargetAnimal(boolean value);
    boolean getTargetMob();
    void setTargetMob(boolean value);
    boolean getTargetPlayer();
    void setTargetPlayer(boolean value);
    boolean getTargetFriendly();
    void setTargetFriendly(boolean value);
}
