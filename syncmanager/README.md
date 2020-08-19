# SendBird SyncManager Sample for Android

The repository for a sample project that use `SendBird SyncManager` for __LocalCache__. Manager offers an event-based data management so that each view would see a single spot by subscribing data event. And it stores the data into database which implements local caching for faster loading.

## SendBird SyncManager

[SyncManager SDK](https://github.com/sendbird/sendbird-syncmanager-android) is a support add-on for [SendBird SDK](https://github.com/sendbird/SendBird-SDK-Android). Major benefits of `SyncManager` are,

 - Local cache integrated: store channel/message data in local storage for fast view loading.
 - Event-driven data handling: subscribe channel/message event like `insert`, `update`, `remove` at a single spot in order to apply data event to view.

## Requirements

- SendBird SyncManager works on Android 4.0+ (API level 14), Java 7+ and [SendBird Android SDK](https://github.com/sendbird/SendBird-SDK-Android) 3.0.96+.


## Install using Gradle

```
repositories {
    maven { url "https://raw.githubusercontent.com/sendbird/sendbird-syncmanager-android/master/" }
}
dependencies {
    // SyncManager
    implementation 'com.sendbird.sdk:sendbird-syncmanager:1.1.18'

    // SendBird
    implementation 'com.sendbird.sdk:sendbird-android-sdk:3.0.141'
}
``` 

## How it works
- For more information, please refer to [SyncManager Document](https://docs.sendbird.com/android/sync_manager_getting_started).
