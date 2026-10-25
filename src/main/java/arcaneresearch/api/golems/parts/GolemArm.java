package arcaneresearch.api.golems.parts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
public class GolemArm {
    protected static GolemArm[] arms = new GolemArm[1];
    private static byte lastID = 0;
    public byte id;
    public String key;
    public String[] research;
    public ResourceLocation icon;
    public Object[] components;
    public EnumGolemTrait[] traits;
    public IArmFunction function;
    public PartModel model;
    public GolemArm(String key, String[] research, ResourceLocation icon, PartModel model,
                    Object[] components, EnumGolemTrait[] traits) {
        this.key = key;
        this.research = research;
        this.icon = icon;
        this.components = components;
        this.traits = traits;
        this.model = model;
        this.function = null;
    }
    public GolemArm(String key, String[] research, ResourceLocation icon, PartModel model,
                    Object[] components, IArmFunction function, EnumGolemTrait[] traits) {
        this(key, research, icon, model, components, traits);
        this.function = function;
    }
    public static void register(GolemArm arm) {
        arm.id = lastID;
        lastID++;
        if (arm.id >= arms.length) {
            GolemArm[] temp = new GolemArm[arm.id + 1];
            System.arraycopy(arms, 0, temp, 0, arms.length);
            arms = temp;
        }
        arms[arm.id] = arm;
    }
    public Component getLocalizedName() {
        return Component.translatable("golem.arm." + key.toLowerCase());
    }
    public Component getLocalizedDescription() {
        return Component.translatable("golem.arm.text." + key.toLowerCase());
    }
    public static GolemArm[] getArms() {
        return arms;
    }
    public static GolemArm getById(int id) {
        if (id >= 0 && id < arms.length) {
            return arms[id];
        }
        return arms[0];
    }
    public interface IArmFunction extends IGenericFunction {
        void onMeleeAttack(IGolemAPI golem, Entity target);
        void onRangedAttack(IGolemAPI golem, LivingEntity target, float distanceFactor);
        net.minecraft.world.entity.ai.goal.Goal getRangedAttackAI(RangedAttackMob mob);
    }
}
