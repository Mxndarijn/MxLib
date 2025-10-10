package nl.mxndarijn.mxlib.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Smooth dancing controller for an ArmorStand.
 * - Provide your own ArmorStand (no spawning here).
 * - Choose one of 8 DanceType styles.
 * - Uses sinusoidal curves for smooth motion; tick interval = 2 for fluid animation.
 */
public class DancingStand {

    public enum DanceType {
        WAVE,          // flowing arm waves + slow spin
        ARM_SWING,     // big alternating arm swings + light foot taps
        ROBOT,         // staccato segmented moves (still smooth here)
        SALSA,         // hips + shoulders + small spin
        HEAD_BOP,      // head bops + subtle shoulder sway
        TWIST,         // torso twist + arm counter-movement
        T_POSE_SPIN,   // wide arms + continuous spin
        BREAKDANCE     // faster legs + alternating torso tilt
    }

    private final Plugin plugin;
    private ArmorStand stand;
    private DanceType danceType = DanceType.WAVE;
    private BukkitTask task;
    private double speed = 1.0;     // 1.0 = normal; >1 faster; <1 slower
    private boolean particles = false;

    // Precomputed dispatch for each dance type
    private final Map<DanceType, BiConsumer<Long, DancingStand>> styles = new EnumMap<>(DanceType.class);

    public DancingStand(Plugin plugin) {
        this.plugin = plugin;
        initStyles();
    }

    /** Start dancing on the provided stand with the given type. */
    public void start(ArmorStand stand, DanceType type) {
        stop();
        this.stand = stand;
        this.danceType = type;
        if (stand == null || !stand.isValid()) return;

        // Basic stand configuration safety (does not force your design)
        stand.setArms(true);
        stand.setGravity(false);

        task = new BukkitRunnable() {
            long t = 0; // ticks elapsed (at interval)

            @Override public void run() {
                if (DancingStand.this.stand == null || !DancingStand.this.stand.isValid()) {
                    cancel(); return;
                }
                // Dispatch style
                styles.getOrDefault(danceType, styles.get(DanceType.WAVE)).accept(t, DancingStand.this);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // update every 2 ticks for smooth animation
    }

    /** Stop dancing and release resources (does not remove the stand). */
    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        stand = null;
    }

    /** Optional runtime tweaks */
    public DancingStand setSpeed(double speed) { this.speed = Math.max(0.1, speed); return this; }
    public DancingStand setParticles(boolean particles) { this.particles = particles; return this; }

    // ====== Internal style implementations ======

    private void initStyles() {
        styles.put(DanceType.WAVE, (t, d) -> {
            double w = 0.08 * d.speed;                    // angular speed
            double s1 = Math.sin(t * w);
            double s2 = Math.sin(t * w + Math.PI / 2);

            // flowing arm "waves"
            d.setArmLeft(  deg(-10 + 25 * s1), 0, deg(-30 + 20 * s2));
            d.setArmRight( deg( 10 + 25 * s1), 0, deg( 30 - 20 * s2));

            // light leg bounce
            d.setLegLeft(  deg(8 * s2), 0, deg(-6 * s1));
            d.setLegRight( deg(-8 * s2), 0, deg(6 * s1));

            // head nod and slight yaw spin
            d.setHead( deg(5 * Math.sin(t * w * 0.5)), deg(6 * Math.sin(t * w * 0.33)), 0);
            d.spinYaw(1.5f * (float)d.speed);

            d.sparkle(0.02);
        });

        styles.put(DanceType.ARM_SWING, (t, d) -> {
            double w = 0.10 * d.speed;
            double s = Math.sin(t * w);
            double c = Math.cos(t * w);

            d.setArmLeft(  deg(35 * s), 0, deg(-25 * c));
            d.setArmRight( deg(-35 * s), 0, deg(25 * c));

            d.setLegLeft(  deg(10 * s), 0, deg(-6 * c));
            d.setLegRight( deg(-10 * s), 0, deg(6 * c));

            d.setHead( deg(3 * c), deg(4 * s), 0);
            d.spinYaw(0.8f * (float)d.speed);

            d.sparkle(0.01);
        });

        styles.put(DanceType.ROBOT, (t, d) -> {
            double w = 0.12 * d.speed;
            // square-ish easing by clamping sin (still smooth)
            double s = clamp(Math.sin(t * w) * 1.3, -1, 1);
            double c = clamp(Math.cos(t * w) * 1.3, -1, 1);

            d.setArmLeft(  deg(20 * stepish(s)), 0, deg(-35 * stepish(c)));
            d.setArmRight( deg(-20 * stepish(s)), 0, deg(35 * stepish(c)));

            d.setLegLeft(  deg(12 * stepish(c)), 0, deg(-8 * stepish(s)));
            d.setLegRight( deg(-12 * stepish(c)), 0, deg(8 * stepish(s)));

            d.setHead( deg(2 * stepish(s)), deg(10 * stepish(c)), 0);
            d.spinYaw(0.0f); // robot: no spin

            d.sparkle(0.0);
        });

        styles.put(DanceType.SALSA, (t, d) -> {
            double w = 0.14 * d.speed;
            double s = Math.sin(t * w);
            double c = Math.cos(t * w);

            // shoulders/hips alternating
            d.setArmLeft(  deg(18 + 20 * s), 0, deg(-10 + 18 * c));
            d.setArmRight( deg(18 - 20 * s), 0, deg(10 - 18 * c));

            d.setLegLeft(  deg(8 + 10 * c), 0, deg(-6 + 10 * s));
            d.setLegRight( deg(8 - 10 * c), 0, deg(6 - 10 * s));

            d.setHead( deg(4 * s), deg(6 * c), 0);
            d.spinYaw(1.2f * (float)d.speed);

            d.sparkle(0.03);
        });

        styles.put(DanceType.HEAD_BOP, (t, d) -> {
            double w = 0.18 * d.speed;
            double s = Math.sin(t * w);

            d.setArmLeft(  deg(5), 0, deg(-10 * s));
            d.setArmRight( deg(5), 0, deg(10 * s));

            d.setLegLeft(  deg(6 * s), 0, 0);
            d.setLegRight( deg(-6 * s), 0, 0);

            d.setHead( deg(12 * s), deg(4 * Math.sin(t * w * 0.5)), 0);
            d.spinYaw(0.4f * (float)d.speed);

            d.sparkle(0.01);
        });

        styles.put(DanceType.TWIST, (t, d) -> {
            double w = 0.12 * d.speed;
            double s = Math.sin(t * w);
            double c = Math.cos(t * w);

            // torso twist simulated with arms opposite Z roll
            d.setArmLeft(  deg(8 * c), 0, deg(-30 * s));
            d.setArmRight( deg(8 * c), 0, deg(30 * s));

            d.setLegLeft(  deg(6 * s), 0, deg(-8 * c));
            d.setLegRight( deg(-6 * s), 0, deg(8 * c));

            d.setHead( deg(3 * c), deg(8 * s), 0);
            d.spinYaw(0.9f * (float)d.speed);

            d.sparkle(0.02);
        });

        styles.put(DanceType.T_POSE_SPIN, (t, d) -> {
            double w = 0.10 * d.speed;
            double s = Math.sin(t * w) * 0.3;

            // wide T-pose with gentle bob
            d.setArmLeft(  deg(0 + 10 * s), 0, deg(-85));
            d.setArmRight( deg(0 + 10 * s), 0, deg(85));

            d.setLegLeft(  deg(4 * s), 0, 0);
            d.setLegRight( deg(-4 * s), 0, 0);

            d.setHead( deg(2 * s), 0, 0);
            d.spinYaw(3.2f * (float)d.speed); // continuous spin

            d.sparkle(0.015);
        });

        styles.put(DanceType.BREAKDANCE, (t, d) -> {
            double w = 0.22 * d.speed;
            double s = Math.sin(t * w);
            double c = Math.cos(t * w);

            // faster legs + alternating torso tilt via arms
            d.setArmLeft(  deg(20 * c), 0, deg(-40 * s));
            d.setArmRight( deg(-20 * c), 0, deg(40 * s));

            d.setLegLeft(  deg(16 * s), 0, deg(-10 * c));
            d.setLegRight( deg(-16 * s), 0, deg(10 * c));

            d.setHead( deg(6 * c), deg(10 * s), 0);
            d.spinYaw(1.6f * (float)d.speed);

            d.sparkle(0.04);
        });
    }

    // ====== Low-level setters ======

    private void setArmLeft(double x, double y, double z)  { stand.setLeftArmPose(new EulerAngle(x, y, z)); }
    private void setArmRight(double x, double y, double z) { stand.setRightArmPose(new EulerAngle(x, y, z)); }
    private void setLegLeft(double x, double y, double z)  { stand.setLeftLegPose(new EulerAngle(x, y, z)); }
    private void setLegRight(double x, double y, double z) { stand.setRightLegPose(new EulerAngle(x, y, z)); }
    private void setHead(double x, double y, double z)     { stand.setHeadPose(new EulerAngle(x, y, z)); }

    private void spinYaw(float deltaYawDegrees) {
        Location loc = stand.getLocation();
        loc.setYaw(loc.getYaw() + deltaYawDegrees);
        stand.teleport(loc);
    }

    private void sparkle(double chance) {
        if (!particles || chance <= 0) return;
        if (Math.random() < chance) {
            Location l = stand.getLocation().clone().add(0.0, 1.6, 0.0);
            l.getWorld().spawnParticle(Particle.NOTE, l, 1, 0, 0, 0, 0);
        }
    }

    // ====== Math helpers ======
    private static double deg(double d) { return Math.toRadians(d); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private static double stepish(double v) {
        // soft step: pushes towards -1 or 1 without snapping
        return Math.tanh(2.0 * v);
    }
}
