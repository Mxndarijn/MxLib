package nl.mxndarijn.mxlib.inventory.heads;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum MxHeadsType {
    MANUALLY_ADDED("manually-added"),
    PLAYER("player");

    private final String type;

    MxHeadsType(String type) {
        this.type = type;
    }

    public static Optional<MxHeadsType> getTypeFromName(String name) {
        for (MxHeadsType t : values()) {
            if (t.getType().equalsIgnoreCase(name)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return type;
    }
}
