package arcaneresearch.api.wands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Жезлы: контракты стоимости фокуса и поведения предметов")
class WandContractTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", "src/main/resources")).getParent().resolve("java");

    private static String source(String relative) {
        try {
            return Files.readString(JAVA.resolve(relative), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Cannot read " + relative, e);
        }
    }

    @Test
    @DisplayName("getVisCost возвращает пустой Null Object, а не null")
    void visCostIsNullObject() {
        String src = source("arcaneresearch/api/wands/FocusItem.java");
        assertTrue(src.contains("public AspectList getVisCost(ItemStack focusstack) {"));
        assertTrue(src.contains("return new AspectList();"));
    }

    @Test
    @DisplayName("applyUpgrade проверяет ранг до индексации массива")
    void applyUpgradeRejectsInvalidRank() {
        String src = source("arcaneresearch/api/wands/FocusItem.java");
        assertTrue(src.contains("if (type == null || rank < 1 || rank > 5)"));
        assertTrue(src.contains("if (rank > upgrades.length || upgrades[rank - 1] != -1)"));
        assertTrue(src.contains("upgradeId >= 0 && upgradeId < FocusUpgradeType.types.length"));
    }

    @Test
    @DisplayName("стоимость списывается с регистра улучшений фокуса")
    void castAndChannelUseTheFocusStack() {
        String src = source("arcaneresearch/common/items/wands/ItemWand.java");
        assertTrue(src.contains("focus.getVisCost(focusStack)"));
        assertTrue(src.contains("focus.getVisCost(getFocusStack(stack))"));
    }

    @Test
    @DisplayName("consumeVis не меняет NBT в симуляции и на клиенте")
    void consumeVisHonoursSimulateAndClientSide() {
        String src = source("arcaneresearch/common/items/wands/ItemWand.java");
        assertTrue(src.contains("if (!simulate && !isClientSide(player))"));
        assertTrue(src.contains("getConsumptionModifier(stack, player, aspect, false)"));
    }

    @Test
    @DisplayName("подзарядка доступна и жезлу в оффханде")
    void offhandWandKeepsBaseRechargeRate() {
        String src = source("arcaneresearch/common/items/wands/ItemWand.java");
        assertTrue(src.contains("getBaseChargeRate(player, held, selected ? slot : 9)"));
    }

    @Test
    @DisplayName("публичная и внутренняя ёмкость считаются из одного источника")
    void capacityHasSingleSourceOfTruth() {
        String src = source("arcaneresearch/common/items/wands/ItemWand.java");
        assertTrue(src.contains("return getMaxVisInternal(stack) / 100;"));
        assertTrue(src.contains("return getMaxVisInternal(stack);"));
        assertTrue(src.contains("getMaxVisHundredths(ItemStack stack)"));
    }

    @Test
    @DisplayName("пакет не доверяет id игрока и не надевает фокус на скипетр")
    void packetIgnoresClientIdAndRejectsSceptres() {
        String src = source("arcaneresearch/common/lib/network/wand/PacketWandFocusChangeToServer.java");
        assertTrue(src.contains("ServerPlayer player = ctx.get().getSender();"));
        assertFalse(src.contains("level.getEntity(msg.playerId)"));
        assertTrue(src.contains("wand.isSceptre(stack)"));
    }

    @Test
    @DisplayName("размахивание передаёт стек фокуса в сам фокус")
    void swingHandlersForwardTheFocusStack() {
        for (String file : List.of(
                "arcaneresearch/common/items/wands/ItemWand.java",
                "arcaneresearch/common/items/casters/ItemCaster.java")) {
            assertTrue(source(file).contains("onEntitySwing(focusStack, entity)"), file);
        }
    }

    @Test
    @DisplayName("Pech-жезл расходуется только на сервере")
    void pechWandIsConsumedOnServerOnly() {
        String src = source("arcaneresearch/common/items/wands/PechWandItem.java");
        assertTrue(src.contains("if (!clientSide && !player.getAbilities().instabuild)"));
    }
}
