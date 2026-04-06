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

    /**
     * Returns the {@code MxHeadsType} matching the given type string, case-insensitively.
     *
     * @param name the type string to look up
     * @return an {@link Optional} containing the matching type, or empty if not found
     */
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
