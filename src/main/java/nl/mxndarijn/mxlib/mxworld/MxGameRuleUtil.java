package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Utility class for applying game rules to a {@link org.bukkit.World} from a YAML configuration section.
 */
public final class MxGameRuleUtil {

    /**
     * Applies all game rules defined in the given {@link ConfigurationSection} to the specified world.
     * Supports both {@code Boolean} and {@code Integer} game rule types.
     * Unknown or invalid game rule keys are logged and skipped.
     *
     * @param world             the world to apply game rules to
     * @param gamerulesSection  the configuration section containing game rule key-value pairs
     */
    public static void applyGameRules(World world, ConfigurationSection gamerulesSection) {
        if (gamerulesSection == null) return;

        for (String keyStr : gamerulesSection.getKeys(false)) {
            try {
                Object raw = gamerulesSection.get(keyStr);
                if (raw == null) continue;

                NamespacedKey key = toNamespacedKey(keyStr);
                GameRule<?> rule = Registry.GAME_RULE.get(key); // registry lookup
                if (rule == null) {
                    MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, "Unknown GameRule: " + keyStr);
                    continue;
                }

                Class<?> type = rule.getType(); // Boolean of Integer
                if (type == Boolean.class) {
                    boolean val = (raw instanceof Boolean b) ? b : Boolean.parseBoolean(raw.toString());
                    set(world, (GameRule<Boolean>) rule, val);
                } else if (type == Integer.class) {
                    int val = (raw instanceof Number n) ? n.intValue() : Integer.parseInt(raw.toString());
                    set(world, (GameRule<Integer>) rule, val);
                }
            } catch (IllegalArgumentException e) {
                MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXATLAS, "Invalid GameRule: " + keyStr);
                e.printStackTrace();
            }
        }
    }

    private static NamespacedKey toNamespacedKey(String s) {
        // accepts both "minecraft:gamerule" and plain "gamerule" format
        if (s.contains(":")) return NamespacedKey.fromString(s);
        return new NamespacedKey(NamespacedKey.MINECRAFT, s);
    }

    private static <T> void set(World world, GameRule<T> rule, T value) {
        world.setGameRule(rule, value);
    }
}
