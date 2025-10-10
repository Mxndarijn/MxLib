package nl.mxndarijn.mxlib.logger;


import lombok.Getter;

@Getter
public enum StandardPrefix implements PrefixType {
    MXATLAS("<dark_aqua>MxAtlas"),
    MXCOMMAND("<dark_green>MxCommand"),
    MXITEM("<dark_aqua>MxItem"),
    MXHEAD_MANAGER("<dark_blue>MxHead-Manager"),
    MXINVENTORY("<dark_green>MxInventory"),
    MXCHATINPUT_MANAGER("<dark_purple>MxChatInput-Manager"),
    CHANGEWORLD_MANAGER("<aqua>ChangeWorld-Manager"),
    LANGUAGE_MANAGER("<gold>Language-Manager"),
    CONFIG_FILES("<yellow>Config-Files"),
    PERMISSION_SERVICE("<yellow>Permission-Manager"),
    LOGGER("<dark_purple>Logger");

    private final String prefix;
    private final String name;

    StandardPrefix(String prefix) {
        this.prefix = "<dark_gray>" + "[" + prefix + "<dark_gray>" + "] ";
        this.name = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }

}
