package arcaneresearch.common.lib.events;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.init.ModItems;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EldritchEyeDrops {
    private EldritchEyeDrops() {
    }
    @SubscribeEvent
    public static void onGuardianDeath(LivingDropsEvent event) {
        try {
            if (!(event.getEntity() instanceof EntityEldritchGuardian)) {
                return;
            }
            if (event.getEntity().level().isClientSide) {
                return;
            }
            boolean byPlayer = event.getSource().getEntity() instanceof Player;
            float chance = byPlayer ? 0.25F : 0.05F;
            if (event.getEntity().level().random.nextFloat() >= chance) {
                return;
            }
            ItemStack eye;
            try {
                eye = new ItemStack(ModItems.ELDRITCH_EYE.get());
            } catch (Exception e) {
                return;
            }
            if (eye.isEmpty()) {
                return;
            }
            ItemEntity drop = new ItemEntity(event.getEntity().level(),
                    event.getEntity().getX(), event.getEntity().getY() + 0.5D,
                    event.getEntity().getZ(), eye);
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        } catch (Exception ignored) {
        }
    }
}
