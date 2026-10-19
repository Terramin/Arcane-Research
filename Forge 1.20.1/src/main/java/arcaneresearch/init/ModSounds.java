package arcaneresearch.init;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Arcaneresearch.MODID);
    public static final RegistryObject<SoundEvent> HEARTBEAT = registerSound("heartbeat");
    public static final RegistryObject<SoundEvent> RUNIC_SHIELD_EFFECT = registerSound("runicshieldeffect");
    public static final RegistryObject<SoundEvent> RUNIC_SHIELD_CHARGE = registerSound("runicshieldcharge");
    public static final RegistryObject<SoundEvent> SPILL = registerSound("spill");
    public static final RegistryObject<SoundEvent> DUST = registerSound("dust");
    public static final RegistryObject<SoundEvent> BUBBLE = registerSound("bubble");
    public static final RegistryObject<SoundEvent> CREAK = registerSound("creak");
    public static final RegistryObject<SoundEvent> SQUEEK = registerSound("squeek");
    public static final RegistryObject<SoundEvent> JAR = registerSound("jar");
    public static final RegistryObject<SoundEvent> PUMP = registerSound("pump");
    public static final RegistryObject<SoundEvent> CRYSTAL = registerSound("crystal");
    public static final RegistryObject<SoundEvent> GORE = registerSound("gore");
    public static final RegistryObject<SoundEvent> INFUSER = registerSound("infuser");
    public static final RegistryObject<SoundEvent> INFUSER_START = registerSound("infuserstart");
    public static final RegistryObject<SoundEvent> URN_BREAK = registerSound("urnbreak");
    public static final RegistryObject<SoundEvent> EVIL_PORTAL = registerSound("evilportal");
    public static final RegistryObject<SoundEvent> GRIND = registerSound("grind");
    public static final RegistryObject<SoundEvent> FLY = registerSound("fly");
    public static final RegistryObject<SoundEvent> KEY = registerSound("key");
    public static final RegistryObject<SoundEvent> TICKS = registerSound("ticks");
    public static final RegistryObject<SoundEvent> CLACK = registerSound("clack");
    public static final RegistryObject<SoundEvent> POOF = registerSound("poof");
    public static final RegistryObject<SoundEvent> BRAIN = registerSound("brain");
    public static final RegistryObject<SoundEvent> RUMBLE = registerSound("rumble");
    public static final RegistryObject<SoundEvent> JACOBS = registerSound("jacobs");
    public static final RegistryObject<SoundEvent> WIND = registerSound("wind");
    public static final RegistryObject<SoundEvent> WHISPERS = registerSound("whispers");
    public static final RegistryObject<SoundEvent> MONOLITH = registerSound("monolith");
    public static final RegistryObject<SoundEvent> PAGE = registerSound("page");
    public static final RegistryObject<SoundEvent> PAGE_TURN = registerSound("pageturn");
    public static final RegistryObject<SoundEvent> LEARN = registerSound("learn");
    public static final RegistryObject<SoundEvent> WRITE = registerSound("write");
    public static final RegistryObject<SoundEvent> ERASE = registerSound("erase");
    public static final RegistryObject<SoundEvent> WAND = registerSound("wand");
    public static final RegistryObject<SoundEvent> WAND_FAIL = registerSound("wandfail");
    public static final RegistryObject<SoundEvent> ICE = registerSound("ice");
    public static final RegistryObject<SoundEvent> HH_OFF = registerSound("hhoff");
    public static final RegistryObject<SoundEvent> HH_ON = registerSound("hhon");
    public static final RegistryObject<SoundEvent> SHOCK = registerSound("shock");
    public static final RegistryObject<SoundEvent> ZAP = registerSound("zap");
    public static final RegistryObject<SoundEvent> CRAFT_FAIL = registerSound("craftfail");
    public static final RegistryObject<SoundEvent> SCAN = registerSound("scan");
    public static final RegistryObject<SoundEvent> CRAFT_START = registerSound("craftstart");
    public static final RegistryObject<SoundEvent> TOOL = registerSound("tool");
    public static final RegistryObject<SoundEvent> UPGRADE = registerSound("upgrade");
    public static final RegistryObject<SoundEvent> COINS = registerSound("coins");
    public static final RegistryObject<SoundEvent> SWARM = registerSound("swarm");
    public static final RegistryObject<SoundEvent> SWARM_ATTACK = registerSound("swarmattack");
    public static final RegistryObject<SoundEvent> WISP_DEAD = registerSound("wispdead");
    public static final RegistryObject<SoundEvent> WISP_LIVE = registerSound("wisplive");
    public static final RegistryObject<SoundEvent> TENTACLE = registerSound("tentacle");
    public static final RegistryObject<SoundEvent> PECH_IDLE = registerSound("pech_idle");
    public static final RegistryObject<SoundEvent> PECH_TRADE = registerSound("pech_trade");
    public static final RegistryObject<SoundEvent> PECH_DICE = registerSound("pech_dice");
    public static final RegistryObject<SoundEvent> PECH_HIT = registerSound("pech_hit");
    public static final RegistryObject<SoundEvent> PECH_DEATH = registerSound("pech_death");
    public static final RegistryObject<SoundEvent> PECH_CHARGE = registerSound("pech_charge");
    public static final RegistryObject<SoundEvent> EG_IDLE = registerSound("egidle");
    public static final RegistryObject<SoundEvent> EG_ATTACK = registerSound("egattack");
    public static final RegistryObject<SoundEvent> EG_DEATH = registerSound("egdeath");
    public static final RegistryObject<SoundEvent> EG_SCREECH = registerSound("egscreech");
    public static final RegistryObject<SoundEvent> CRAB_CLAW = registerSound("crabclaw");
    public static final RegistryObject<SoundEvent> CRAB_DEATH = registerSound("crabdeath");
    public static final RegistryObject<SoundEvent> CRAB_TALK = registerSound("crabtalk");
    public static final RegistryObject<SoundEvent> CHANT = registerSound("chant");
    public static final SoundType GORE_TYPE = new ForgeSoundType(0.5f, 1.0f,
            GORE::get, GORE::get, GORE::get, GORE::get, GORE::get);
    public static final SoundType CRYSTAL_TYPE = new ForgeSoundType(0.5f, 1.0f,
            CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get);
    public static final SoundType JAR_TYPE = new ForgeSoundType(0.5f, 1.0f,
            JAR::get, JAR::get, JAR::get, JAR::get, JAR::get);
    public static final SoundType URN_TYPE = new ForgeSoundType(0.5f, 1.5f,
            URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get);
    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                arcaneresearch.helper.IDsHelper.mod(name)));
    }
}
