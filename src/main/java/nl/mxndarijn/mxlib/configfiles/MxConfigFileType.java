package nl.mxndarijn.mxlib.configfiles;

/**
 * Interface representing a configuration file type.
 * Used for defining file names, paths, and autosave behavior.
 */
public interface MxConfigFileType {
    /**
     * Returns the file name (e.g., "config.yml").
     * @return the file name
     */
    String fileName();

    /**
     * Returns the relative path to the file (e.g., "config.yml" or "scoreboards/map.yml").
     * @return the path
     */
    String path();

    /**
     * Returns whether this file should be included in {@code saveAll()} operations.
     * @return true if it should autosave
     */
    boolean autoSave();
}