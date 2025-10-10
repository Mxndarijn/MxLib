package nl.mxndarijn.mxlib.builders;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Fluent builder/factory for spawning and configuring ArmorStands.
 * <p>
 * Usage:
 * ArmorStand stand = ArmorStandHelper.create()
 *     .setLocation(loc)
 *     .setName("Hello")
 *     .setCustomNameVisible(true)
 *     .setSmall(true)
 *     .setArms(true)
 *     .setMarker(false)
 *     .setEquipment(EquipmentSlot.HEAD, new ItemStack(Material.DIAMOND_HELMET))
 *     .build();
 * <p>
 * Or apply settings to an existing armor stand:
 * ArmorStandHelper.create()
 *     .setSmall(true)
 *     .applyTo(existing);
 */
public final class ArmorStandHelper {

    // Factory
    public static ArmorStandHelper create() { return new ArmorStandHelper(); }

    // Required/primary spawn data
    private @Nullable World world;
    private @Nullable Location location;

    // Basic flags
    private @Nullable Boolean visible;
    private @Nullable Boolean gravity;
    private @Nullable Boolean marker;
    private @Nullable Boolean small;
    private @Nullable Boolean basePlate;
    private @Nullable Boolean arms;
    private @Nullable Boolean glowing;
    private @Nullable Boolean invulnerable;
    private @Nullable Boolean persistent;
    private @Nullable Boolean silent;
    private @Nullable Boolean collidable;
    private @Nullable Boolean customNameVisible;
    private @Nullable Boolean canPickupItems;

    // Name
    private @Nullable Component customNameComponent;

    // Poses
    private @Nullable EulerAngle headPose;
    private @Nullable EulerAngle bodyPose;
    private @Nullable EulerAngle leftArmPose;
    private @Nullable EulerAngle rightArmPose;
    private @Nullable EulerAngle leftLegPose;
    private @Nullable EulerAngle rightLegPose;

    // Equipment
    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

    // Scoreboard tags
    private final Set<String> scoreboardTags = new HashSet<>();

    // Post-spawn consumer hook
    private final Collection<Consumer<ArmorStand>> afterSpawnConsumers = new ArrayList<>();

    private ArmorStandHelper() {}

    // ----- Location/World -----
    public ArmorStandHelper setWorld(@NotNull World world) { this.world = Objects.requireNonNull(world); return this; }
    public ArmorStandHelper setLocation(@NotNull Location location) { this.location = Objects.requireNonNull(location); this.world = location.getWorld(); return this; }
    public ArmorStandHelper SetLocation(@NotNull Location location) { return setLocation(location); } // alias

    // ----- Name -----
    public ArmorStandHelper setName(@Nullable String name) { this.customNameComponent = MiniMessage.miniMessage().deserialize("<!i>" + name); return this; }
    public ArmorStandHelper SetName(@Nullable String name) { return setName(name); } // alias
    public ArmorStandHelper setName(@Nullable Component name) { this.customNameComponent = name; return this; }

    public ArmorStandHelper setCustomNameVisible(boolean visible) { this.customNameVisible = visible; return this; }

    // ----- Flags -----
    public ArmorStandHelper setVisible(boolean value) { this.visible = value; return this; }
    public ArmorStandHelper setInvisible(boolean value) { this.visible = !value; return this; }
    public ArmorStandHelper setGravity(boolean value) { this.gravity = value; return this; }
    public ArmorStandHelper setMarker(boolean value) { this.marker = value; return this; }
    public ArmorStandHelper setSmall(boolean value) { this.small = value; return this; }
    public ArmorStandHelper setBasePlate(boolean value) { this.basePlate = value; return this; }
    public ArmorStandHelper setArms(boolean value) { this.arms = value; return this; }
    public ArmorStandHelper setGlowing(boolean value) { this.glowing = value; return this; }
    public ArmorStandHelper setInvulnerable(boolean value) { this.invulnerable = value; return this; }
    public ArmorStandHelper setPersistent(boolean value) { this.persistent = value; return this; }
    public ArmorStandHelper setSilent(boolean value) { this.silent = value; return this; }
    public ArmorStandHelper setCollidable(boolean value) { this.collidable = value; return this; }
    public ArmorStandHelper setCanPickupItems(boolean value) { this.canPickupItems = value; return this; }

    // ----- Poses -----
    public ArmorStandHelper setHeadPose(@NotNull EulerAngle pose) { this.headPose = Objects.requireNonNull(pose); return this; }
    public ArmorStandHelper setBodyPose(@NotNull EulerAngle pose) { this.bodyPose = Objects.requireNonNull(pose); return this; }
    public ArmorStandHelper setLeftArmPose(@NotNull EulerAngle pose) { this.leftArmPose = Objects.requireNonNull(pose); return this; }
    public ArmorStandHelper setRightArmPose(@NotNull EulerAngle pose) { this.rightArmPose = Objects.requireNonNull(pose); return this; }
    public ArmorStandHelper setLeftLegPose(@NotNull EulerAngle pose) { this.leftLegPose = Objects.requireNonNull(pose); return this; }
    public ArmorStandHelper setRightLegPose(@NotNull EulerAngle pose) { this.rightLegPose = Objects.requireNonNull(pose); return this; }

    // ----- Equipment -----
    public ArmorStandHelper setEquipment(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
        Objects.requireNonNull(slot, "slot");
        if (item == null) equipment.remove(slot); else equipment.put(slot, item);
        return this;
    }

    public ArmorStandHelper setHelmet(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.HEAD, item); }
    public ArmorStandHelper setChestplate(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.CHEST, item); }
    public ArmorStandHelper setLeggings(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.LEGS, item); }
    public ArmorStandHelper setBoots(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.FEET, item); }
    public ArmorStandHelper setMainHand(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.HAND, item); }
    public ArmorStandHelper setOffHand(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.OFF_HAND, item); }

    // ----- Tags -----
    public ArmorStandHelper addScoreboardTag(@NotNull String tag) { this.scoreboardTags.add(Objects.requireNonNull(tag)); return this; }
    public ArmorStandHelper addScoreboardTags(@NotNull Collection<String> tags) { this.scoreboardTags.addAll(Objects.requireNonNull(tags)); return this; }

    // ----- Hooks -----
    public ArmorStandHelper afterSpawn(@NotNull Consumer<ArmorStand> consumer) { this.afterSpawnConsumers.add(Objects.requireNonNull(consumer)); return this; }

    // ----- Build / Apply -----
    /**
     * Spawns a new ArmorStand using the provided builder configuration.
     * World and Location must be set on the builder (or pass them via parameters).
     */
    public @NotNull ArmorStand build() {
        if (this.location == null) throw new IllegalStateException("Location must be set before build()");
        if (this.world == null) this.world = this.location.getWorld();
        if (this.world == null) throw new IllegalStateException("World must be resolvable from location before build()");

        // Use spawn with consumer to apply atomically
        return this.world.spawn(this.location, ArmorStand.class, this::applyTo);
    }

    /**
     * Applies the configured properties to an existing ArmorStand.
     */
    public void applyTo(@NotNull ArmorStand stand) {
        Objects.requireNonNull(stand, "stand");

        // Basic flags
        if (visible != null) stand.setVisible(visible);
        if (gravity != null) stand.setGravity(gravity);
        if (marker != null) stand.setMarker(marker);
        if (small != null) stand.setSmall(small);
        if (basePlate != null) stand.setBasePlate(basePlate);
        if (arms != null) stand.setArms(arms);
        if (glowing != null) stand.setGlowing(glowing);
        if (invulnerable != null) stand.setInvulnerable(invulnerable);
        if (persistent != null) stand.setPersistent(persistent);
        if (silent != null) stand.setSilent(silent);
        if (collidable != null) stand.setCollidable(collidable);
        if (canPickupItems != null) stand.setCanPickupItems(canPickupItems);

        // Name
        if (customNameComponent != null) stand.customName(customNameComponent);
        if (customNameVisible != null) stand.setCustomNameVisible(customNameVisible);

        // Poses
        if (headPose != null) stand.setHeadPose(headPose);
        if (bodyPose != null) stand.setBodyPose(bodyPose);
        if (leftArmPose != null) stand.setLeftArmPose(leftArmPose);
        if (rightArmPose != null) stand.setRightArmPose(rightArmPose);
        if (leftLegPose != null) stand.setLeftLegPose(leftLegPose);
        if (rightLegPose != null) stand.setRightLegPose(rightLegPose);

        // Equipment
        if (!equipment.isEmpty()) {
            equipment.forEach((slot, item) -> stand.getEquipment().setItem(slot, item, true));
        }

        // Scoreboard tags
        if (!scoreboardTags.isEmpty()) {
            scoreboardTags.forEach(stand::addScoreboardTag);
        }

        // After-spawn hooks
        if (!afterSpawnConsumers.isEmpty()) {
            afterSpawnConsumers.forEach(c -> c.accept(stand));
        }
    }

    // ----- Convenience static methods -----
    public static ArmorStand spawn(@NotNull Location location, @Nullable Consumer<ArmorStandHelper> config) {
        ArmorStandHelper helper = ArmorStandHelper.create().setLocation(location);
        if (config != null) config.accept(helper);
        return helper.build();
    }
}
