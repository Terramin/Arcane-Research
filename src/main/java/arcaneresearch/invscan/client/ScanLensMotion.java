package arcaneresearch.invscan.client;

import net.minecraft.world.inventory.Slot;

public final class ScanLensMotion {

    private static final int SCAN_TICKS = 20; // совпадает с InvScanClient.SCAN_TICKS
    private static final double FOLLOW_SPEED = 22.0D;
    private static final double RETURN_SPEED = 28.0D;

    private boolean initialized;
    private long lastFrame;
    private Slot lastTarget;
    private int lastTicks;
    private long tickStartedAt;
    private double centerX;
    private double centerY;
    private double angle;
    private double lastCursorX;
    private double lastCursorY;
    private boolean resting;

    public Pose update(Slot target, int ticks, long now,
                       double cursorX, double cursorY,
                       double slotX, double slotY,
                       double mouseX, double mouseY) {

        if (!this.initialized || now - this.lastFrame > 250_000_000L) {
            this.initialized = true;
            this.centerX = this.lastCursorX = cursorX;
            this.centerY = this.lastCursorY = cursorY;
            this.angle = 0.0D;
            this.lastTarget = null;
            this.lastTicks = 0;
            this.lastFrame = now;
        }

        double seconds = clamp((now - this.lastFrame) / 1.0E9D, 0.0D, 0.05D);
        this.lastFrame = now;

        boolean active = target != null && ticks > 0;
        double desiredX = cursorX;
        double desiredY = cursorY;
        double desiredAngle = 0.0D;

        if (active) {
            if (target != this.lastTarget || ticks != this.lastTicks) {
                this.tickStartedAt = now;
            }
            double fraction = clamp((now - this.tickStartedAt) / 5.0E7D, 0.0D, 1.0D);
            double progress = clamp((ticks - 1.0D + fraction) / (SCAN_TICKS - 1.0D), 0.0D, 1.0D);
            double eased = progress * progress * (3.0D - 2.0D * progress);
            double phase = eased * Math.PI * 2.0D;

            desiredX = slotX - 2.0D * Math.sin(phase);
            desiredY = slotY + 1.25D * (Math.cos(phase) - 1.0D);

            double horizontal = clamp((mouseX - slotX) / 8.0D, -1.0D, 1.0D);
            double vertical = clamp((mouseY - slotY) / 8.0D, -1.0D, 1.0D);
            desiredAngle = Math.toRadians(
                    8.0D * horizontal - 3.0D * vertical + 2.5D * Math.sin(phase));
        } else {
            this.centerX += cursorX - this.lastCursorX;
            this.centerY += cursorY - this.lastCursorY;
        }

        double speed = active ? FOLLOW_SPEED : RETURN_SPEED;
        double blend = -Math.expm1(-speed * seconds);
        this.centerX += (desiredX - this.centerX) * blend;
        this.centerY += (desiredY - this.centerY) * blend;
        this.angle += (desiredAngle - this.angle) * blend;

        this.resting = !active
                && Math.abs(this.centerX - cursorX) < 0.01D
                && Math.abs(this.centerY - cursorY) < 0.01D
                && Math.abs(this.angle) < 1.0E-4D;
        if (this.resting) {
            this.centerX = cursorX;
            this.centerY = cursorY;
            this.angle = 0.0D;
        }

        this.lastCursorX = cursorX;
        this.lastCursorY = cursorY;
        this.lastTarget = active ? target : null;
        this.lastTicks = active ? ticks : 0;

        return new Pose((float) this.centerX, (float) this.centerY, (float) this.angle);
    }

    public boolean isResting() {
        return this.resting;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    public record Pose(float centerX, float centerY, float angle) {}
}