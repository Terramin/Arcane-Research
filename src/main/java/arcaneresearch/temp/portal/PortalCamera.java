package arcaneresearch.temp.portal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Виртуальная камера вида через портал: методы Camera#setPosition / #setRotation — protected,
 * подкласс открывает их для установки позиции/поворота вне стандартного setup().
 */
@OnlyIn(Dist.CLIENT)
public class PortalCamera extends net.minecraft.client.Camera {
    /**
     * Установка виртуальной камеры: позиция и углы взгляда.
     *
     * Сначала выполняется обычный setup(level, entity) — он единственный способ заполнить
     * приватные entity/level/initialized (они нужны ванильной и форджевской внутренней
     * логике вложенного прохода: FogRenderer/ForgeHooksClient.onFogRender требуют
     * camera.getEntity().level()), затем позиция и поворот перезаписываются виртуальными.
     */
    public void setView(double x, double y, double z, float yaw, float pitch,
                        BlockGetter level, Entity entity, float partialTick) {
        setup(level, entity, false, false, partialTick);
        setRotation(yaw, pitch);
        setPosition(x, y, z);
    }
}
