package nl.mxndarijn.mxlib.mxworld;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.UUID;

public class MxWorld {
    @Getter
    private final String name;
    private final String uuid;
    @Getter
    private final File dir;
    @Setter
    @Getter
    private UUID worldUID;
    @Setter
    @Getter
    private boolean loaded;

    public MxWorld(String name, String uuid, File dir) {
        this.name = name;
        this.uuid = uuid;
        this.dir = dir;
        this.loaded = false;
        this.worldUID = null;
    }

    public String getUUID() {
        return uuid;
    }

}
