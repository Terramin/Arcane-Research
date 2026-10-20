package arcaneresearch.proxies;
import arcaneresearch.client.renderers.tile.EldritchCrabRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import arcaneresearch.init.ModEntities;

public class ProxyEntities {
    public void setupEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPECIAL_ITEM.get(),
                arcaneresearch.client.renderers.entity.SpecialItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FOLLOWING_ITEM.get(),
                arcaneresearch.client.renderers.entity.SpecialItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLING_TAINT.get(),
                arcaneresearch.client.renderers.entity.FallingTaintRenderer::new);
        event.registerEntityRenderer(ModEntities.FLUX_RIFT.get(),
                arcaneresearch.client.renderers.entity.FluxRiftRenderer::new);
        event.registerEntityRenderer(ModEntities.BRAINY_ZOMBIE.get(),
                arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                arcaneresearch.client.renderers.entity.BrainyZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.INHABITED_ZOMBIE.get(),
                arcaneresearch.client.renderers.entity.InhabitedZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_BAT.get(),
                arcaneresearch.client.renderers.entity.FireBatRenderer::new);
        event.registerEntityRenderer(ModEntities.SPELL_BAT.get(),
                arcaneresearch.client.renderers.entity.SpellBatRenderer::new);
        event.registerEntityRenderer(ModEntities.PECH.get(),
                arcaneresearch.client.renderers.entity.PechRenderer::new);
        event.registerEntityRenderer(ModEntities.WISP.get(),
                arcaneresearch.client.renderers.entity.WispRenderer::new);
        event.registerEntityRenderer(ModEntities.THAUMIC_SLIME.get(),
                arcaneresearch.client.renderers.entity.ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINT_CRAWLER.get(),
                arcaneresearch.client.renderers.entity.TaintCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINT_SEED.get(),
                arcaneresearch.client.renderers.entity.TaintSeedRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINT_SEED_PRIME.get(),
                arcaneresearch.client.renderers.entity.TaintSeedRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINTACLE.get(),
                arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINTACLE_SMALL.get(),
                arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINTACLE_GIANT.get(),
                arcaneresearch.client.renderers.entity.TaintacleRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINT_SWARM.get(),
                arcaneresearch.client.renderers.entity.TaintSwarmRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST.get(),
                arcaneresearch.client.renderers.entity.CultistRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_KNIGHT.get(),
                arcaneresearch.client.renderers.entity.CultistRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_CLERIC.get(),
                arcaneresearch.client.renderers.entity.CultistRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_LEADER.get(),
                arcaneresearch.client.renderers.entity.CultistLeaderRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_GUARDIAN.get(),
                arcaneresearch.client.renderers.entity.EldritchGuardianRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_WARDEN.get(),
                arcaneresearch.client.renderers.entity.EldritchWardenRenderer::new);
        event.registerEntityRenderer(ModEntities.MIND_SPIDER.get(),
                arcaneresearch.client.renderers.entity.MindSpiderRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_CRAB.get(),
                arcaneresearch.client.renderers.entity.EldritchCrabRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_GOLEM.get(),
                arcaneresearch.client.renderers.entity.EldritchGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_PORTAL_LESSER.get(),
                arcaneresearch.client.renderers.entity.CultistPortalRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_PORTAL_GREATER.get(),
                arcaneresearch.client.renderers.entity.CultistPortalGreaterRenderer::new);
        event.registerEntityRenderer(ModEntities.FOCUS_PROJECTILE.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.ALUMENTUM.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.CAUSALITY_COLLAPSER.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FOCUS_CLOUD.get(),
                arcaneresearch.client.renderers.entity.FocusCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.FOCUS_MINE.get(),
                arcaneresearch.client.renderers.entity.FocusMineRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_ORB.get(),
                ctx -> arcaneresearch.client.renderers.entity.ArcaneresearchProjectileRenderer.Factory.orb(ctx, 0x8844FF));
        event.registerEntityRenderer(ModEntities.GOLEM_DART.get(),
                ctx -> arcaneresearch.client.renderers.entity.ArcaneresearchProjectileRenderer.Factory.dart(ctx));
        event.registerEntityRenderer(ModEntities.HOMING_SHARD.get(),
                ctx -> arcaneresearch.client.renderers.entity.ArcaneresearchProjectileRenderer.Factory.magic(ctx, 0x66FFFF));
        event.registerEntityRenderer(ModEntities.BOTTLE_TAINT.get(),
                arcaneresearch.client.renderers.entity.BottleTaintRenderer::new);
        event.registerEntityRenderer(ModEntities.ELDRITCH_ORB.get(),
                arcaneresearch.client.renderers.entity.EldritchOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAPPLE.get(),
                arcaneresearch.client.renderers.entity.GrappleRenderer::new);
        event.registerEntityRenderer(ModEntities.RIFT_BLAST.get(),
                arcaneresearch.client.renderers.entity.RiftBlastRenderer::new);
        event.registerEntityRenderer(ModEntities.TURRET_CROSSBOW.get(),
                arcaneresearch.client.renderers.entity.TurretCrossbowRenderer::new);
        event.registerEntityRenderer(ModEntities.TURRET_CROSSBOW_ADVANCED.get(),
                arcaneresearch.client.renderers.entity.TurretCrossbowAdvancedRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCANE_BORE.get(),
                arcaneresearch.client.renderers.entity.ArcaneBoreRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCANERESEARCH_GOLEM.get(),
                arcaneresearch.client.renderers.entity.GolemRenderer::new);
    }
}