package arcaneresearch.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities; // NEOFORGE_PORT: was ForgeCapabilities
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;

import java.util.ArrayList;
import java.util.List;

public class ScanningManager {

    private static final List<IScanThing> things = new ArrayList<>();

    public static void addScannableThing(IScanThing obj) {
        things.add(obj);
    }

    public static boolean removeScannableThing(IScanThing obj) {
        return things.remove(obj);
    }

    public static void scanTheThing(Player player, Object object) {
        boolean found = false;
        boolean suppress = false;

        for (IScanThing thing : things) {
            if (thing.checkThing(player, object)) {
                String researchKey = thing.getResearchKey(player, object);

                if (researchKey == null || researchKey.isEmpty() || progressResearch(player, researchKey)) {
                    if (researchKey == null || researchKey.isEmpty()) {
                        suppress = true;
                    }
                    found = true;
                    thing.onSuccess(player, object);
                }
            }
        }

        if (!suppress) {
            if (!found) {
                player.sendOverlayMessage(Component.literal("\u00a75\u00a7o")
                        .append(Component.translatable("tc.unknownobject")));
            } else {
                player.sendOverlayMessage(Component.literal("\u00a7a\u00a7o")
                        .append(Component.translatable("tc.knownobject")));
            }
        }

        if (object instanceof BlockPos pos) {
            Level level = player.level();
            level.getBlockEntity(pos);

            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                // NEOFORGE_PORT: Forge blockEntity.getCapability(ITEM_HANDLER, side):LazyOptional{ifPresent}
                //   -> NeoForge level.getCapability(Item.BLOCK, pos, side):@Nullable ResourceHandler
                //   (transfer rework). Handler logic below unchanged 1:1.
                var handler = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
                if (handler != null) {
                    int scanned = 0;
                    for (int slot = 0; slot < handler.size(); slot++) {
                        // NEOFORGE_PORT: getStackInSlot(slot) -> ItemUtil.getStack(handler, slot).
                        ItemStack stack = net.neoforged.neoforge.transfer.item.ItemUtil.getStack(handler, slot);
                        if (!stack.isEmpty()) {
                            scanTheThing(player, stack);
                            scanned++;
                        }
                        if (scanned >= 100) {
                            player.sendOverlayMessage(Component.literal("\u00a75\u00a7o")
                                    .append(Component.translatable("tc.invtoolarge")));
                            break;
                        }
                    }
                }
            }
        }
    }

    public static boolean isThingStillScannable(Player player, Object object) {
        for (IScanThing thing : things) {
            if (thing.checkThing(player, object)) {
                try {
                    String key = thing.getResearchKey(player, object);
                    if (!ArcaneresearchCapabilities.isResearchKnown(player, key)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    public static ItemStack getItemFromParams(Player player, Object obj) {
        ItemStack result = ItemStack.EMPTY;

        if (obj instanceof ItemStack stack) {
            result = stack;
        } else if (obj instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
            result = itemEntity.getItem();
        } else if (obj instanceof BlockPos pos) {
            Level level = player.level();
            BlockState state = level.getBlockState(pos);

            BlockHitResult hit = rayTrace(player);
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                    && hit.getBlockPos().equals(pos)) {
                // NEOFORGE_PORT: getCloneItemStack protected in 26.x; scanning needs item identity.
                result = new ItemStack(state.getBlock().asItem());
            }
            if (result.isEmpty()) {
                // NEOFORGE_PORT: getCloneItemStack protected in 26.x; scanning needs item identity.
                result = new ItemStack(state.getBlock().asItem());
            }

            if (result.isEmpty()) {
                var fluidState = level.getFluidState(pos);
                if (fluidState.is(Fluids.WATER) || fluidState.is(Fluids.FLOWING_WATER)) {
                    result = new ItemStack(Items.WATER_BUCKET);
                } else if (fluidState.is(Fluids.LAVA) || fluidState.is(Fluids.FLOWING_LAVA)) {
                    result = new ItemStack(Items.LAVA_BUCKET);
                }
            }
        }

        return result;
    }

    private static BlockHitResult rayTrace(Player player) {
        Vec3 eyePos = player.getEyePosition(0);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);

        return player.level().clip(new ClipContext(
            eyePos, endPos,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.ANY,
            player
        ));
    }

    private static boolean progressResearch(Player player, String researchKey) {
        if (arcaneresearch.api.ArcaneresearchApi.internalMethods != null) {
            return arcaneresearch.api.ArcaneresearchApi.internalMethods.progressResearch(player, researchKey);
        }
        // NEOFORGE_PORT: Forge getKnowledge(player):LazyOptional{.map().orElse()}
        //   -> NeoForge getKnowledge(player):@Nullable direct null-check. Logic identical.
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null && !knowledge.isResearchKnown(researchKey)) {
            knowledge.addResearch(researchKey);
            return true;
        }
        return false;
    }

    public static boolean knowsResearch(Player player, String researchKey) {
        return ArcaneresearchCapabilities.isResearchKnown(player, researchKey);
    }

    public static int getScannableCount() {
        return things.size();
    }
}
