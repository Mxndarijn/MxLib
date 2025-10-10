package nl.mxndarijn.mxlib.permission;

import lombok.Getter;

@Getter
public enum StandardPermission implements PermissionType {
    ;

    private final String node;

    StandardPermission(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return node;
    }

    @Override
    public String node() {
        return node;
    }
}
