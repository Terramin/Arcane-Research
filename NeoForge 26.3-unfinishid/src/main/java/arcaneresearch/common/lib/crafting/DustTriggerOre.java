package arcaneresearch.common.lib.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.IDustTrigger;
import arcaneresearch.common.lib.events.ServerEvents;

import javax.annotation.Nullable;

public class DustTriggerOre implements IDustTrigger {

    private final TagKey<Block> target;
    private final ItemStack result;
    private final String research;

    public DustTriggerOre(@Nullable String research, TagKey<Block> target, ItemStack result) {
        this.target = target;
        this.result = result;
        this.research = research;
    }

    @Override
    public Placement getValidFace(Level level, Player player, BlockPos pos, Direction face) {
        BlockState state = level.getBlockState(pos);
        boolean matches = state.is(target);
        return (matches && (research == null || ArcaneresearchCapabilities.knowsResearch(player, research)))
                ? new Placement(0, 0, 0, null)
                : null;
    }

    @Override
    public void execute(Level level, Player player, BlockPos pos, Placement placement, Direction side) {
        if (level.isClientSide()) {
            return;
        }

        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, result.copy(), null));

        BlockState state = level.getBlockState(pos);

        ServerEvents.addRunnableServer(level, () -> {
            ServerEvents.addSwapper(level, pos, state, result.copy(), false, 0, player,
                    true, true, -9999, false, false, 0, ServerEvents.DEFAULT_PREDICATE, 0.0f);
        }, 50);
    }
}
