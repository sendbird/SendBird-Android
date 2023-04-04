# Sendbird Android SDK v3 samples
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Deprecation Note (v3)
:warning: Please note that Sendbird’s SDK v3 will be deprecated by **July 2023**. You may still use the older SDKs at your choice, but no new updates or bug fixes will be made to SDK v3.

**We recommend clients to plan their migration to SDK v4 as early as possible as there are breaking changes.** We also provide prioritized support for migration and any issues related to v4. SDK v4 provides far richer and robust features in Websocket, Local caching, Polls, Scheduled Messages, Pinned Message, and many more. So try it out now! ([Chat SDK v4 samples](https://github.com/sendbird/sendbird-chat-sample-android/))

<br />

## Introduction

[Sendbird](https://sendbird.com) provides the chat API and SDK for your app, enabling real-time communication among the users. Here are various samples built using Sendbird Chat SDK.

- [**Chat sample**](#chat-sample) has core chat features. Group channel and open channel are the two main channel types in which you can create various subtypes where users can send and receive messages. This sample is written in Java with [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk).

- [**Chat (Local Caching) sample**](#chat-local-caching-sample) has core chat features with local caching enabled. It only supports group channel in which you can create various subtypes where users can send and receive messages. This sample is written in Java with [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk).

- [**SyncManager sample**](#syncmanager-sample) is equipped with a local cache along with core chat features. For faster data loading and caching, the sample synchronizes with the Sendbird server and saves a list of group channels and the messages within the local cache into your client app. This sample is written in Java with [Sendbird SyncManager](https://github.com/sendbird/sendbird-syncmanager-android) and [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk). Find more about SyncManager on [Sendbird SyncManager document](https://sendbird.com/docs/syncmanager/v1/android/getting-started/about-syncmanager).

<br />

## 🔒 Security tip
When a new Sendbird application is created in the dashboard the default security settings are set permissive to simplify running samples and implementing your first code.

Before launching make sure to review the security tab under ⚙️ Settings -> Security, and set Access token permission to Read Only or Disabled so that unauthenticated users can not login as someone else. And review the Access Control lists. Most apps will want to disable "Allow retrieving user list" as that could expose usage numbers and other information.

## Installation

To use our Android samples, you should first install [Chat SDK for Android](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk) 3.0.96 or higher on your system.

### Requirements

|Sample|Android|Java|Gradle| 
|---|---|---|---|
|Chat|4.1 (API level 16) or higher|7 or higher|3.4.0 or higher |
|Chat (Local Caching)|4.1 (API level 16) or higher|7 or higher|3.4.0 or higher |
|SyncManager|4.1 (API level 16) or higher|7 or higher|-|

### Chat sample

You can **clone** the project from the [Chat sample repository](https://github.com/sendbird/SendBird-Android). 

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git  

// Move to the Chat sample
cd SendBird-Android/basic
```

### Chat (Local Caching) sample

You can **clone** the project from the [Chat sample repository](https://github.com/sendbird/SendBird-Android). 

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git  

// Move to the Chat sample
cd SendBird-Android/local-caching
```

### SyncManager sample

You can **clone** the project directly from the [SyncManager sample repository](https://github.com/sendbird/SendBird-Android/tree/master/syncmanager).

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git

// Move to the SyncManager sample
cd SendBird-Android/syncmanager
```
