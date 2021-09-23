# Sendbird SyncManager for Android sample
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Introduction

SyncManager for Android is a Chat SDK add-on that optimizes the user caching experience by interlinking the synchronization of the local data storage with the chat data in Sendbird server through an event-driven structure. Provided here is a SyncManager sample for Android to experience first-hand the benefits of Sendbird’s SyncManager.

### Benefits

Sendbird SyncManager provides the local caching system and data synchronization with the Sendbird server, which are run on an event-driven structure. According to the real-time events of the messages and channels, SyncManager takes care of the background tasks for the cache updates from the Sendbird server to the local device. By leveraging this systemized structure with connection-based synchronization, SyncManager allows you to easily integrate the Chat SDK to utilize all of its features, while also reducing data usage and offering a reliable and effortless storage mechanism. 

### More about Sendbird SyncManager for Android

Find out more about Sendbird SyncManager for Android at [SyncManager for Android doc](https://sendbird.com/docs/syncmanager/v1/android/getting-started/about-syncmanager). If you need any help in resolving any issues or have questions, visit [our community](https://community.sendbird.com).

<br />

## Before getting started

This section provides the prerequisites for testing Sendbird SyncManager for Android sample app.

### Requirements

The minimum requirements for SyncManager for Android are:

- Android 4.0+ (API level 14)
- Java 7+
- [Chat SDK for Android](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk) 3.0 96+

### Try the sample app using your data 

If you would like to try the sample app specifically fit to your usage, you can do so by replacing the default sample app ID with yours, which you can obtain by [creating your Sendbird application from the dashboard](https://sendbird.com/docs/chat/v3/android/getting-started/install-chat-sdk#2-step-1-create-a-sendbird-application-from-your-dashboard). Furthermore, you could also add data of your choice on the dashboard to test. This will allow you to experience the sample app with data from your Sendbird application. 

<br />

## Getting started

This section explains the steps you need to take before testing the Android sample app.

### Install Chat SDK & SyncManager for Android

Installing the SyncManager SDK is simple if you're familiar with using external libraries or SDKs. First, add the following code to your **root** `build.gradle` file:

```gradle
allprojects {
    repositories {
        ...
        maven { url "https://repo.sendbird.com/public/maven" }
    }
}
```

>**Note**: Make sure the above code block isn't added to your module `bundle.gradle` file.

Then, add the dependency to the project's top-level `build.gradle` file.

```gradle
dependencies {
    // SyncManager SDK for Android (Latest, embeds Sendbird Chat SDK 3.0.170)
    implementation 'com.sendbird.sdk:sendbird-syncmanager:1.1.31'

    // Chat SDK for Android (If you want to use higher version than the version embedded in the SyncManager)
    implementation 'com.sendbird.sdk:sendbird-android-sdk:3.0.172'
}
```

> **Note**: SyncManager SDK versions `1.1.30` or lower can be downloaded from JCenter until February 1, 2022. SDK versions higher than `1.1.30` will be available on Sendbird's remote repository.
