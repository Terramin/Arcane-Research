package arcaneresearch.init;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModSounds.java (PORTED 90%).
// REQUIRED changes:
//   DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ...) -> create(Registries.SOUND_EVENT, ...)
//   RegistryObject<SoundEvent> -> DeferredHolder<SoundEvent, SoundEvent> (same .get() API).
// All 70 sound entries ported in ORIGINAL ORDER with IDENTICAL registry names.
// DONE: GORE_TYPE / CRYSTAL_TYPE / JAR_TYPE / URN_TYPE restored below as
// LazySoundType (same volume/pitch/suppliers as Forge ForgeSoundType).
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Arcaneresearch.MODID);

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = registerSound("heartbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_EFFECT = registerSound("runicshieldeffect");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_CHARGE = registerSound("runicshieldcharge");

    public static final DeferredHolder<SoundEvent, SoundEvent> SPILL = registerSound("spill");
    public static final DeferredHolder<SoundEvent, SoundEvent> DUST = registerSound("dust");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = registerSound("bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAK = registerSound("creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUEEK = registerSound("squeek");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAR = registerSound("jar");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP = registerSound("pump");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL = registerSound("crystal");
    public static final DeferredHolder<SoundEvent, SoundEvent> GORE = registerSound("gore");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER = registerSound("infuser");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER_START = registerSound("infuserstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> URN_BREAK = registerSound("urnbreak");
    public static final DeferredHolder<SoundEvent, SoundEvent> EVIL_PORTAL = registerSound("evilportal");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIND = registerSound("grind");

    public static final DeferredHolder<SoundEvent, SoundEvent> FLY = registerSound("fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> KEY = registerSound("key");
    public static final DeferredHolder<SoundEvent, SoundEvent> TICKS = registerSound("ticks");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLACK = registerSound("clack");
    public static final DeferredHolder<SoundEvent, SoundEvent> POOF = registerSound("poof");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRAIN = registerSound("brain");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLE = registerSound("rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACOBS = registerSound("jacobs");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = registerSound("wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPERS = registerSound("whispers");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONOLITH = registerSound("monolith");

    public static final DeferredHolder<SoundEvent, SoundEvent> PAGE = registerSound("page");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAGE_TURN = registerSound("pageturn");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = registerSound("learn");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRITE = registerSound("write");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERASE = registerSound("erase");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND = registerSound("wand");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND_FAIL = registerSound("wandfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE = registerSound("ice");
    public static final DeferredHolder<SoundEvent, SoundEvent> HH_OFF = registerSound("hhoff");
    public static final DeferredHolder<SoundEvent, SoundEvent> HH_ON = registerSound("hhon");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = registerSound("shock");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = registerSound("zap");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_FAIL = registerSound("craftfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAN = registerSound("scan");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_START = registerSound("craftstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = registerSound("tool");
    public static final DeferredHolder<SoundEvent, SoundEvent> UPGRADE = registerSound("upgrade");
    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = registerSound("coins");

    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM = registerSound("swarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM_ATTACK = registerSound("swarmattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEAD = registerSound("wispdead");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_LIVE = registerSound("wisplive");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLE = registerSound("tentacle");

    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_IDLE = registerSound("pech_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_TRADE = registerSound("pech_trade");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DICE = registerSound("pech_dice");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_HIT = registerSound("pech_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DEATH = registerSound("pech_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_CHARGE = registerSound("pech_charge");

    public static final DeferredHolder<SoundEvent, SoundEvent> EG_IDLE = registerSound("egidle");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_ATTACK = registerSound("egattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_DEATH = registerSound("egdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_SCREECH = registerSound("egscreech");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_CLAW = registerSound("crabclaw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_DEATH = registerSound("crabdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_TALK = registerSound("crabtalk");

    public static final DeferredHolder<SoundEvent, SoundEvent> CHANT = registerSound("chant");

    // TODO_PORT (deferred SoundTypes — Forge 1:1 source kept for follow-up):
    //   public static final SoundType GORE_TYPE = new ForgeSoundType(0.5f, 1.0f,
    //           GORE::get, GORE::get, GORE::get, GORE::get, GORE::get);
    //   public static final SoundType CRYSTAL_TYPE = new ForgeSoundType(0.5f, 1.0f,
    //           CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get);
    //   public static final SoundType JAR_TYPE = new ForgeSoundType(0.5f, 1.0f,
    //           JAR::get, JAR::get, JAR::get, JAR::get, JAR::get);
    //   public static final SoundType URN_TYPE = new ForgeSoundType(0.5f, 1.5f,
    //           URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get);

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, name)));
    }

    // NEOFORGE_PORT: 1:1 from Forge ModSounds (was ForgeSoundType, same volume/pitch/suppliers).
    // LazySoundType resolves the DeferredHolders at play time (post-registration).
    public static final SoundType GORE_TYPE = new LazySoundType(0.5f, 1.0f,
            GORE::get, GORE::get, GORE::get, GORE::get, GORE::get);
    public static final SoundType CRYSTAL_TYPE = new LazySoundType(0.5f, 1.0f,
            CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get, CRYSTAL::get);
    public static final SoundType JAR_TYPE = new LazySoundType(0.5f, 1.0f,
            JAR::get, JAR::get, JAR::get, JAR::get, JAR::get);
    public static final SoundType URN_TYPE = new LazySoundType(0.5f, 1.5f,
            URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get, URN_BREAK::get);
}
