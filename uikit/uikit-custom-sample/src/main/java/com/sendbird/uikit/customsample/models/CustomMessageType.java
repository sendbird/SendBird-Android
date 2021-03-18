package com.sendbird.uikit.customsample.models;

import com.sendbird.uikit.customsample.consts.StringSet;

public enum CustomMessageType {
    NONE(""), HIGHLIGHT(StringSet.highlight);

    private final String value;
    CustomMessageType(String value) { this.value = value; }

    public String getValue() {
        return value;
    }

    public static CustomMessageType from(String value) {
        for (CustomMessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return NONE;
    }
}
