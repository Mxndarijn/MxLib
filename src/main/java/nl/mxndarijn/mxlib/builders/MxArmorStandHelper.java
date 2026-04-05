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
 * ArmorStand stand = MxArmorStandHelper.create()
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
 * MxArmorStandHelper.create()
 *     .setSmall(true)
 *     .applyTo(existing);
 */
public final class MxArmorStandHelper {

    /**
     * Creates a new instance of the armor stand helper.
     * @return a new {@link MxArmorStandHelper} instance
     */
    public static MxArmorStandHelper create() { return new MxArmorStandHelper(); }

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

    /**
     * Default constructor for the armor stand helper.
     */
    private MxArmorStandHelper() {}

    // ----- Location/World -----
    /**
     * Sets the world where the armor stand will be spawned.
     * @param world the world to spawn in
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setWorld(@NotNull World world) { this.world = Objects.requireNonNull(world); return this; }

    /**
     * Sets the location where the armor stand will be spawned.
     * @param location the location to spawn at
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setLocation(@NotNull Location location) { this.location = Objects.requireNonNull(location); this.world = location.getWorld(); return this; }

    /**
     * Alias for {@link #setLocation(Location)}.
     * @param location the location to spawn at
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper SetLocation(@NotNull Location location) { return setLocation(location); } // alias

    // ----- Name -----
    /**
     * Sets the custom name of the armor stand using a MiniMessage string.
     * @param name the custom name to set
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setName(@Nullable String name) { this.customNameComponent = MiniMessage.miniMessage().deserialize("<!i>" + name); return this; }

    /**
     * Alias for {@link #setName(String)}.
     * @param name the custom name to set
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper SetName(@Nullable String name) { return setName(name); } // alias

    /**
     * Sets the custom name of the armor stand using a {@link Component}.
     * @param name the custom name to set
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setName(@Nullable Component name) { this.customNameComponent = name; return this; }

    /**
     * Sets whether the custom name should be visible.
     * @param visible true if the name should be visible
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setCustomNameVisible(boolean visible) { this.customNameVisible = visible; return this; }

    // ----- Flags -----
    /**
     * Sets whether the armor stand is visible.
     * @param value true if visible
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setVisible(boolean value) { this.visible = value; return this; }

    /**
     * Sets whether the armor stand is invisible.
     * @param value true if invisible
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setInvisible(boolean value) { this.visible = !value; return this; }

    /**
     * Sets whether the armor stand has gravity.
     * @param value true if it has gravity
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setGravity(boolean value) { this.gravity = value; return this; }

    /**
     * Sets whether the armor stand is a marker (no collision, small hitbox).
     * @param value true if it's a marker
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setMarker(boolean value) { this.marker = value; return this; }

    /**
     * Sets whether the armor stand is small.
     * @param value true if small
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setSmall(boolean value) { this.small = value; return this; }

    /**
     * Sets whether the armor stand has a base plate.
     * @param value true if it has a base plate
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setBasePlate(boolean value) { this.basePlate = value; return this; }

    /**
     * Sets whether the armor stand has arms.
     * @param value true if it has arms
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setArms(boolean value) { this.arms = value; return this; }

    /**
     * Sets whether the armor stand is glowing.
     * @param value true if glowing
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setGlowing(boolean value) { this.glowing = value; return this; }

    /**
     * Sets whether the armor stand is invulnerable.
     * @param value true if invulnerable
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setInvulnerable(boolean value) { this.invulnerable = value; return this; }

    /**
     * Sets whether the armor stand is persistent.
     * @param value true if persistent
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setPersistent(boolean value) { this.persistent = value; return this; }

    /**
     * Sets whether the armor stand is silent.
     * @param value true if silent
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setSilent(boolean value) { this.silent = value; return this; }

    /**
     * Sets whether the armor stand is collidable.
     * @param value true if collidable
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setCollidable(boolean value) { this.collidable = value; return this; }

    /**
     * Sets whether the armor stand can pickup items.
     * @param value true if it can pickup items
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setCanPickupItems(boolean value) { this.canPickupItems = value; return this; }

    // ----- Poses -----
    /**
     * Sets the head pose.
     * @param pose the head pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setHeadPose(@NotNull EulerAngle pose) { this.headPose = Objects.requireNonNull(pose); return this; }

    /**
     * Sets the body pose.
     * @param pose the body pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setBodyPose(@NotNull EulerAngle pose) { this.bodyPose = Objects.requireNonNull(pose); return this; }
    /**
     * Sets the left arm pose.
     * @param pose the pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setLeftArmPose(@NotNull EulerAngle pose) { this.leftArmPose = Objects.requireNonNull(pose); return this; }

    /**
     * Sets the right arm pose.
     * @param pose the pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setRightArmPose(@NotNull EulerAngle pose) { this.rightArmPose = Objects.requireNonNull(pose); return this; }

    /**
     * Sets the left leg pose.
     * @param pose the pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setLeftLegPose(@NotNull EulerAngle pose) { this.leftLegPose = Objects.requireNonNull(pose); return this; }

    /**
     * Sets the right leg pose.
     * @param pose the pose
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setRightLegPose(@NotNull EulerAngle pose) { this.rightLegPose = Objects.requireNonNull(pose); return this; }

    // ----- Equipment -----
    /**
     * Sets the equipment for a specific slot.
     * @param slot the slot
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setEquipment(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
        Objects.requireNonNull(slot, "slot");
        if (item == null) equipment.remove(slot); else equipment.put(slot, item);
        return this;
    }

    /**
     * Sets the helmet.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setHelmet(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.HEAD, item); }

    /**
     * Sets the chestplate.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setChestplate(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.CHEST, item); }

    /**
     * Sets the leggings.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setLeggings(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.LEGS, item); }

    /**
     * Sets the boots.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setBoots(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.FEET, item); }

    /**
     * Sets the item in the main hand.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setMainHand(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.HAND, item); }

    /**
     * Sets the item in the off hand.
     * @param item the item stack, or null to clear
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper setOffHand(@Nullable ItemStack item) { return setEquipment(EquipmentSlot.OFF_HAND, item); }

    // ----- Tags -----
    /**
     * Adds a scoreboard tag.
     * @param tag the tag to add
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper addScoreboardTag(@NotNull String tag) { this.scoreboardTags.add(Objects.requireNonNull(tag)); return this; }

    /**
     * Adds multiple scoreboard tags.
     * @param tags the tags to add
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper addScoreboardTags(@NotNull Collection<String> tags) { this.scoreboardTags.addAll(Objects.requireNonNull(tags)); return this; }

    // ----- Hooks -----
    /**
     * Adds a consumer to be executed after the armor stand is spawned.
     * @param consumer the consumer
     * @return this helper instance for chaining
     */
    public MxArmorStandHelper afterSpawn(@NotNull Consumer<ArmorStand> consumer) { this.afterSpawnConsumers.add(Objects.requireNonNull(consumer)); return this; }

    // ----- Build / Apply -----
    /**
     * Spawns a new ArmorStand using the provided builder configuration.
     * World and Location must be set on the builder.
     * @return the spawned armor stand
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
     * @param stand the armor stand to apply properties to
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
    /**
     * Spawns an armor stand at the given location and configures it using the provided consumer.
     * @param location the location to spawn at
     * @param config the configuration consumer
     * @return the spawned armor stand
     */
    public static ArmorStand spawn(@NotNull Location location, @Nullable Consumer<MxArmorStandHelper> config) {
        MxArmorStandHelper helper = MxArmorStandHelper.create().setLocation(location);
        if (config != null) config.accept(helper);
        return helper.build();
    }
}
