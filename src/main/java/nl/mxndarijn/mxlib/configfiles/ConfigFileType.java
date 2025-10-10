package nl.mxndarijn.mxlib.configfiles;

public interface ConfigFileType {
    String fileName();   // bv. "config.yml"
    String path();       // bv. "config.yml" of "scoreboards/scoreboard_map.yml"
    boolean autoSave();  // of dit bestand mee moet in saveAll()
}