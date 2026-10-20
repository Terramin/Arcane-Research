package arcaneresearch.common.lib.aura;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.lib.wands.WandResearchHelper;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.tiles.aura.TileJarNode;

/**
 * Ритуал окулуса, порт TC4 WandManager.createOculus (верbatim).
 * Клик жезлом БЕЗ фокуса по алтарю с 4 глазами, пока над ним
 * тёмная нода и готов лабиринт: жезл ест 100 каждого праймала,
 * алтарь открывается, нода исчезает, НА ЕЁ МЕСТЕ встаёт портал.
 * Ничего лишнего, как в 1.7.10: ни шаблонов, ни бонус-нод, ни вспышек
 * (лишния для оригинала приставки вырезаны).
 * Лежит в arcaneresearch.arcaneresearch.common.lib.aura.
 */
public final class EldritchOculusRitual {
    private EldritchOculusRitual() {
    }

    public static boolean createOculus(ItemStack stack, Player player, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }
        try {
            BlockEntity be = level.getBlockEntity(pos);
            BlockEntity above = level.getBlockEntity(pos.above());
            // TC4: tile instanceof TileEldritchAltar && eyes == 4 && !isOpen.
            if (!(be instanceof EldritchAltarTile altar)
                    || altar.getEyes() != 4 || altar.isOpen()) {
                return false;
            }
            // TC4: node instanceof TileNode (не банка) с типом DARK.
            if (!(above instanceof INode node) || above instanceof TileJarNode
                    || node.getNodeType() != NodeType.DARK) {
                return false;
            }
            // TC4 performTrigger (событие 6): досмотр OCULUS; в этом порту
            // ключ называется LEGACYOCULUS — принимаем оба варианта.
            if (!WandResearchHelper.isResearchComplete(player, "LEGACYOCULUS")
                    && !WandResearchHelper.isResearchComplete(player, "OCULUS")) {
                return false;
            }
            if (!altar.checkForMaze()) {
                return false;
            }
            // TC4: itemstack.getItem() instanceof ItemWandCasting
            // (путь performTrigger — только жезл; фокус — гейт вызывающего
            // ItemWandCasting.onUse, тут проверяем так же).
            if (stack.isEmpty() || !(stack.getItem() instanceof IWand wand)) {
                return false;
            }
            if (wand.getFocus(stack) != null) {
                return false;
            }
            AspectList cost = new AspectList()
                    .add(Aspect.AIR, 100)
                    .add(Aspect.FIRE, 100)
                    .add(Aspect.EARTH, 100)
                    .add(Aspect.WATER, 100)
                    .add(Aspect.ORDER, 100)
                    .add(Aspect.ENTROPY, 100);
            if (!wand.consumeAllVis(stack, player, cost, false, true)) {
                return false;
            }
            // TC4: world.playSoundEffect(..., "thaumcraft:wand", 1.0F, 1.0F).
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    WandSounds.wand(), SoundSource.PLAYERS, 1.0F, 1.0F);
            // TC4: setOpen(true); удалить ноду; blockEldritchPortal на её месте;
            // синк тайла + notify блока. Больше ничего.
            altar.setOpen(true);
            level.removeBlock(pos.above(), false);
            level.setBlockAndUpdate(pos.above(), AuraBlocks.ELDRITCH_PORTAL.get().defaultBlockState());
            altar.syncTile(true);
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
