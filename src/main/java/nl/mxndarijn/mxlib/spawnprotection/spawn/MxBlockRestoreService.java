package nl.mxndarijn.mxlib.spawnprotection.spawn;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Manages scheduled restores of gate, door, and trapdoor block states in the spawn world.
 *
 * <p>When a player opens a gate or door, the original {@link BlockData} is captured and
 * a restore is scheduled for 2 minutes later (2400 ticks). Pending restores are persisted
 * to {@code spawn-gate-restores.yml} so they survive server restarts.</p>
 *
 * <p>This service is a singleton; obtain the instance via {@link #getInstance(JavaPlugin)}.</p>
 */
public final class MxBlockRestoreService {

    private static MxBlockRestoreService instance;

    private final JavaPlugin plugin;

    /**
     * Pending gate/door restores that have not yet been applied.
     * Key: {@code "world,x,y,z"} — uniquely identifies a block location.
     * Value: serialized {@link BlockData} string representing the original state to restore.
     */
    private final HashMap<String, String> pendingRestores = new HashMap<>();

    /** File used to persist pending restores across server restarts. */
    private final File restoreFile;

    /**
     * Private constructor. Loads and schedules any restores that survived a server restart.
     */
    private MxBlockRestoreService(JavaPlugin plugin) {
        this.plugin = plugin;
        restoreFile = new File(plugin.getDataFolder(), "spawn-gate-restores.yml");
        loadAndScheduleRestores();
    }

    /**
     * Returns the singleton instance of {@code MxBlockRestoreService}, creating it if necessary.
     *
     * @param plugin the {@link JavaPlugin} instance used for scheduling and data folder access
     * @return the singleton instance; never {@code null}
     */
    public static MxBlockRestoreService getInstance(JavaPlugin plugin) {
        if (instance == null)
            instance = new MxBlockRestoreService(plugin);
        return instance;
    }

    /**
     * Builds a unique string key for a block location in the format {@code "world,x,y,z"}.
     *
     * @param b the block to build a key for
     * @return the location key string; never {@code null}
     */
    public String blockKey(Block b) {
        return b.getWorld().getName() + "," + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    /**
     * Returns whether a pending restore is currently registered for the given block.
     *
     * @param block the block to check
     * @return {@code true} if a restore is pending for this block
     */
    public boolean hasPendingRestore(Block block) {
        return pendingRestores.containsKey(blockKey(block));
    }

    /**
     * Cancels any pending restore for the given block, making the current block state permanent.
     * Also updates the persist file.
     *
     * @param block the block whose pending restore should be cancelled
     */
    public void cancelRestore(Block block) {
        String key = blockKey(block);
        if (pendingRestores.remove(key) != null) {
            saveRestoreFile();
        }
    }

    /**
     * Records a pending restore for the given block and schedules it to be applied
     * after 2 minutes (2400 ticks). Only the first original state is recorded;
     * subsequent interactions before the restore fires do not overwrite it.
     *
     * @param block        the block whose state should be restored
     * @param originalData the {@link BlockData} to restore the block to
     */
    public void scheduleRestore(Block block, BlockData originalData) {
        String key = blockKey(block);
        String dataStr = originalData.getAsString();
        if (pendingRestores.containsKey(key)) return;
        pendingRestores.put(key, dataStr);
        saveRestoreFile();
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreBlock(block, dataStr), 1200L);
    }

    /**
     * Applies a pending restore by setting the block's data back to the original state.
     * If the pending restore has been superseded or cancelled, this method does nothing.
     * Removes the entry from the pending map and updates the persist file on success.
     *
     * @param block           the block to restore
     * @param originalDataStr the serialized {@link BlockData} string to apply
     */
    private void restoreBlock(Block block, String originalDataStr) {
        String key = blockKey(block);
        if (!originalDataStr.equals(pendingRestores.get(key))) return;
        pendingRestores.remove(key);
        saveRestoreFile();
        try {
            BlockData data = Bukkit.createBlockData(originalDataStr);
            block.setBlockData(data, false);
        } catch (Exception ex) {
            plugin.getLogger().warning("[MxBlockRestoreService] Could not restore block data: " + ex.getMessage());
        }
    }

    /**
     * Persists the current pending restores map to {@link #restoreFile}
     * so that pending restores survive server restarts.
     * Commas in block keys are replaced with underscores for YAML compatibility.
     */
    private void saveRestoreFile() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (var entry : pendingRestores.entrySet()) {
            cfg.set(entry.getKey().replace(",", "_"), entry.getValue());
        }
        try {
            cfg.save(restoreFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("[MxBlockRestoreService] Could not save gate-restore file: " + ex.getMessage());
        }
    }

    /**
     * Reads {@link #restoreFile} on startup and immediately schedules any restores
     * that were pending before the server restarted. Overdue restores are applied
     * after a short 20-tick delay to allow the world to finish loading.
     * Clears the file after scheduling to avoid duplicate restores.
     */
    private void loadAndScheduleRestores() {
        if (!restoreFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(restoreFile);
        for (String rawKey : cfg.getKeys(false)) {
            String blockDataStr = cfg.getString(rawKey);
            if (blockDataStr == null) continue;
            String[] parts = rawKey.split("_");
            if (parts.length < 4) continue;
            int z = Integer.parseInt(parts[parts.length - 1]);
            int y = Integer.parseInt(parts[parts.length - 2]);
            int x = Integer.parseInt(parts[parts.length - 3]);
            String worldName = String.join("_", Arrays.copyOf(parts, parts.length - 3));
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            Block block = world.getBlockAt(x, y, z);
            String key = blockKey(block);
            pendingRestores.put(key, blockDataStr);
            final String finalBlockDataStr = blockDataStr;
            Bukkit.getScheduler().runTaskLater(plugin, () -> restoreBlock(block, finalBlockDataStr), 20L);
        }
        saveRestoreFile();
    }
}

