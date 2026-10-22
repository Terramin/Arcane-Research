package thaumictinker.gear;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import arcaneresearch.init.ModSounds;

 


 
public class XpTalismanItem extends Item implements ICurioItem {

    public static final int RANGE = 3;
    public static final int MAX_XP = 1500;
    public static final int BOTTLE_COST = 10;

    public XpTalismanItem(Properties properties) {
        super(properties);
    }

    public static int getXp(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("thaumictinker:xp", CompoundTag.TAG_INT)) {
            return 0;
        }
        return stack.getTag().getInt("thaumictinker:xp");
    }

    public static void setXp(ItemStack stack, int xp) {
        stack.getOrCreateTag().putInt("thaumictinker:xp", Math.max(0, Math.min(MAX_XP, xp)));
    }

    public static boolean isAbsorbing(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("thaumictinker:active");
    }

    public static void setAbsorbing(ItemStack stack, boolean absorbing) {
        if (absorbing) {
            stack.getOrCreateTag().putBoolean("thaumictinker:active", true);
        } else if (stack.hasTag()) {
            stack.getTag().remove("thaumictinker:active");
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (getXp(stack) >= MAX_XP) {
                return InteractionResultHolder.pass(stack);
            }
            if (!level.isClientSide()) {
                setAbsorbing(stack, !isAbsorbing(stack));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 0.3F, 0.1F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (getXp(stack) < BOTTLE_COST) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(Items.GLASS_BOTTLE)) {
                continue;
            }
            candidate.shrink(1);
            ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            if (!inventory.add(bottle) && !bottle.isEmpty()) {
                player.spawnAtLocation(bottle);
            }
            setXp(stack, getXp(stack) - BOTTLE_COST);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.1F, 0.1F + level.getRandom().nextFloat() / 2.0F);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("XP: " + getXp(stack)));
        if (getXp(stack) >= MAX_XP) {
            tooltip.add(Component.translatable("ttinkerer.ttmisc.full"));
        } else {
            tooltip.add(Component.translatable(isAbsorbing(stack)
                    ? "ttinkerer.ttmisc.absorbing" : "ttinkerer.ttmisc.notAbsorbing"));
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("necklace");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level() instanceof ServerLevel level && isAbsorbing(stack)) {
            absorb(level, stack, entity);
        }
    }

    public static int absorb(ServerLevel level, ItemStack stack, LivingEntity wearer) {
        if (wearer.tickCount % 5 != 0) {
            return 0;
        }
        int currentXp = getXp(stack);
        int remaining = MAX_XP - currentXp;
        if (remaining <= 0) {
            setAbsorbing(stack, false);
            return 0;
        }
        int added = 0;
        AABB box = new AABB(wearer.getX() - RANGE, wearer.getY() - RANGE, wearer.getZ() - RANGE,
                wearer.getX() + RANGE, wearer.getY() + RANGE, wearer.getZ() + RANGE);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, box);
        for (ExperienceOrb orb : orbs) {
            if (orb.isRemoved()) {
                continue;
            }
            int value = orb.getValue();
            if (added + value <= remaining) {
                added += value;
                orb.discard();
                level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), ModSounds.ZAP.get(),
                        SoundSource.PLAYERS, value / 10.0F, 1.0F);
            }
            remaining -= added;
            if (remaining <= 0) {
                break;
            }
        }
        setXp(stack, Math.min(MAX_XP, currentXp + added));
        return added;
    }
}
