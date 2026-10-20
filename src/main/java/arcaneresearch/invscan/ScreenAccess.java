package arcaneresearch.invscan;
import java.lang.reflect.Field;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Reflective access to AbstractContainerScreen internals (leftPos/topPos/hoveredSlot/imageWidth/imageHeight).
 * Uses SRG names that are mapped at runtime both in dev (official mappings)
 * and in production, so no access transformer is required.
 * If field access fails, leftPos/topPos fall back to being computed from
 * screen.width/height and imageWidth/imageHeight.
 */
public final class ScreenAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger("arcaneresearch.invscan");
    private static final Field HOVERED_SLOT;
    private static final Field LEFT_POS;
    private static final Field TOP_POS;
    private static final Field IMAGE_WIDTH;
    private static final Field IMAGE_HEIGHT;
    static {
        Field hoveredSlot = null;
        Field leftPos = null;
        Field topPos = null;
        Field imageWidth = null;
        Field imageHeight = null;
        try {
            hoveredSlot = ObfuscationReflectionHelper.findField(
                    AbstractContainerScreen.class, "f_97734_");
            leftPos = ObfuscationReflectionHelper.findField(
                    AbstractContainerScreen.class, "f_97735_");
            topPos = ObfuscationReflectionHelper.findField(
                    AbstractContainerScreen.class, "f_97736_");
            imageWidth = ObfuscationReflectionHelper.findField(
                    AbstractContainerScreen.class, "f_97726_");
            imageHeight = ObfuscationReflectionHelper.findField(
                    AbstractContainerScreen.class, "f_97727_");
        } catch (Throwable t) {
            LOGGER.error("[invscan] Reflection init failed, self-hover may not work", t);
        }
        HOVERED_SLOT = hoveredSlot;
        LEFT_POS = leftPos;
        TOP_POS = topPos;
        IMAGE_WIDTH = imageWidth;
        IMAGE_HEIGHT = imageHeight;
    }
    private ScreenAccess() {
    }
    public static Slot hoveredSlot(AbstractContainerScreen<?> screen) {
        if (HOVERED_SLOT == null) {
            return null;
        }
        try {
            return (Slot) HOVERED_SLOT.get(screen);
        } catch (Exception e) {
            return null;
        }
    }
    public static int imageWidth(AbstractContainerScreen<?> screen) {
        if (IMAGE_WIDTH == null) {
            return -1;
        }
        try {
            return IMAGE_WIDTH.getInt(screen);
        } catch (Exception e) {
            return -1;
        }
    }
    public static int imageHeight(AbstractContainerScreen<?> screen) {
        if (IMAGE_HEIGHT == null) {
            return -1;
        }
        try {
            return IMAGE_HEIGHT.getInt(screen);
        } catch (Exception e) {
            return -1;
        }
    }
    public static int leftPos(AbstractContainerScreen<?> screen) {
        if (LEFT_POS != null) {
            try {
                return LEFT_POS.getInt(screen);
            } catch (Exception ignored) {
            }
        }
        int image = imageWidth(screen);
        if (image > 0) {
            return (screen.width - image) / 2;
        }
        return 0;
    }
    public static int topPos(AbstractContainerScreen<?> screen) {
        if (TOP_POS != null) {
            try {
                return TOP_POS.getInt(screen);
            } catch (Exception ignored) {
            }
        }
        int image = imageHeight(screen);
        if (image > 0) {
            return (screen.height - image) / 2;
        }
        return 0;
    }
}
