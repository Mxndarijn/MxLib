package nl.mxndarijn.mxlib.language;

import lombok.Getter;

/**
 * Default, built-in language keys. Other modules can contribute their own enums
 * by implementing {@link MxLanguageKey} and registering them via {@link MxLanguageManager}.
 */
@Getter
public enum MxStandardLanguageText implements MxLanguageKey {
    NO_PERMISSION("no-permission"),
    NO_PLAYER("no-player"),
    NOT_CORRECT_WORLD("not-correct-world"),
    ERROR_WHILE_EXECUTING_COMMAND("error-while-executing-command"),
    ERROR_WHILE_EXECUTING_ITEM("error-while-executing-item");

    private final String configValue;

    MxStandardLanguageText(String value) {
        this.configValue = value;
    }

    MxStandardLanguageText() {
        this.configValue = slug(name());
    }

    @Override
    public String key() {
        return configValue;
    }

    private static String slug(String enumName) {
        return enumName.toLowerCase().replaceAll("_", "-");
    }
}

