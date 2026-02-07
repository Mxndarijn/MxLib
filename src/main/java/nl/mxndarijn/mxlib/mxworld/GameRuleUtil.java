package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.configuration.ConfigurationSection;

public final class GameRuleUtil {

    public static void applyGameRules(World world, ConfigurationSection gamerulesSection) {
        if (gamerulesSection == null) return;

        for (String keyStr : gamerulesSection.getKeys(false)) {
            Object raw = gamerulesSection.get(keyStr);
            if (raw == null) continue;

            NamespacedKey key = toNamespacedKey(keyStr);
            GameRule<?> rule = Registry.GAME_RULE.get(key); // registry lookup
            if (rule == null) {
                Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, "Unknown GameRule: " + keyStr);
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
        }
    }

    private static NamespacedKey toNamespacedKey(String s) {
        // accepteer zowel "minecraft:advance_time" als "advance_time"
        if (s.contains(":")) return NamespacedKey.fromString(s);
        return new NamespacedKey(NamespacedKey.MINECRAFT, s);
    }

    private static <T> void set(World world, GameRule<T> rule, T value) {
        world.setGameRule(rule, value);
    }
}
