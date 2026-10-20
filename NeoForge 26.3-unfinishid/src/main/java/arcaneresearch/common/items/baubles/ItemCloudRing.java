package arcaneresearch.common.items.baubles;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.CommonHooks;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModSounds;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashMap;

public class ItemCloudRing extends Item implements ICurioItem {

    private static final HashMap<String, Boolean> jumpList = new HashMap<>();

    public ItemCloudRing(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    public static void tickClient(Player player, boolean spacePressed) {
        String name = player.getName().getString();
        if (spacePressed && !jumpList.containsKey(name)) {
            jumpList.put(name, true);
        }
        if (spacePressed && !player.onGround() && !player.isInWater()
                && jumpList.containsKey(name) && jumpList.get(name)) {
            Level level = player.level();
            FXDispatcher.INSTANCE.drawBamf(player.getX(), player.getY() + 0.5D, player.getZ(),
                    1.0F, 1.0F, 1.0F, false, false, Direction.UP);
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    ModSounds.POOF.get(), SoundSource.PLAYERS, 0.1F,
                    1.0F + (float) level.getRandom().nextGaussian() * 0.05F, false);
            jumpList.put(name, false);
            Vec3 motion = player.getDeltaMovement();
            double boost = 0.75D;
            // NEOFORGE_PORT: MobEffects.JUMP_BOOST renamed JUMP_BOOST in 26.x (both are Holders now).
            if (player.hasEffect(MobEffects.JUMP_BOOST)) {
                boost += (player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
            }
            player.setDeltaMovement(motion.x, boost, motion.z);
            if (player.isSprinting()) {
                float yaw = player.getYRot() * 0.017453292F;
                Vec3 moved = player.getDeltaMovement();
                player.setDeltaMovement(moved.x - Mth.sin(yaw) * 0.2F, moved.y,
                        moved.z + Mth.cos(yaw) * 0.2F);
            }
            player.fallDistance = 0.0F;
            arcaneresearch.common.lib.network.PacketHandler.sendToServer(
                    new arcaneresearch.common.lib.network.playerdata.PacketPlayerFlagToServer(
                            arcaneresearch.common.lib.network.playerdata.PacketPlayerFlagToServer.FLAG_RESET_FALL_DISTANCE));
            // NEOFORGE_PORT: ForgeHooks.onLivingJump -> CommonHooks.onLivingJump.
            CommonHooks.onLivingJump(player);
        }
        if (player.onGround()) {
            jumpList.remove(name);
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("ring");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}