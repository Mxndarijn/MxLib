package nl.mxndarijn.mxlib.permission;

import lombok.Getter;

@Getter
public enum MxStandardPermission implements MxPermissionType {
    ;

    private final String node;

    MxStandardPermission(String node) {
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
