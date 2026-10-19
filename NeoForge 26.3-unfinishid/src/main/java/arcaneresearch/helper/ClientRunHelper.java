package arcaneresearch.helper;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Единая точка ветвления по стороне (порт из Forge 1.20.1:
 * {@code DistExecutor} в NeoForge нет — проверка {@code FMLEnvironment}).
 *
 * <p>Безопасно грузится на сервере: класс не ссылается на клиентские классы,
 * клиентский код приезжает только внутри лямбды. На долгой дистанции вызовы
 * из сетевых хендлеров должны переехать на сторону, зашитую в регистратор
 * пейлоада ({@code playToClient}/{@code playToServer}), — тогда хелпер не нужен.</p>
 */
public final class ClientRunHelper {

    private ClientRunHelper() {
    }

    /** Выполнить на клиенте, на сервере — ничего. */
    public static void runOnClient(Runnable action) {
        if (action == null) {
            return;
        }
        try {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                action.run();
            }
        } catch (Exception ignored) {
        }
    }
}
