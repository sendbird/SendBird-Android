package com.sendbird.localcaching.sample.model;

public class InitEvent {
    private final boolean isInitialized;

    public InitEvent(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
