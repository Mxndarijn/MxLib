package nl.mxndarijn.mxlib.configfiles;

import lombok.Getter;

@Getter
public enum StandardConfigFile implements ConfigFileType {
    MAIN_CONFIG("config.yml", "config.yml", false),
    HEAD_DATA("head-data.yml", "head-data.yml", true),
    DEFAULT_LANGUAGE("nl-NL.yml", "languages/nl-NL.yml", false);

    private final String fileName;
    private final String path;
    private final boolean autoSave;

    StandardConfigFile(String fileName, String path, boolean autoSave) {
        this.fileName = fileName;
        this.path = path;
        this.autoSave = autoSave;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean autoSave() {
        return autoSave;
    }
}
