# Sendbird Android samples
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Introduction

[Sendbird](https://sendbird.com) provides the chat API and SDK for your app, enabling real-time communication among the users. Here are various samples built using Sendbird Chat SDK.

- [**Chat sample**](#chat-sample) has core chat features. Group channel and open channel are the two main channel types in which you can create various subtypes where users can send and receive messages. This sample is written in Java with [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk).

- [**SyncManager sample**](#syncmanager-sample) is equipped with a local cache along with core chat features. For faster data loading and caching, the sample synchronizes with the Sendbird server and saves a list of group channels and the messages within the local cache into your client app. This sample is written in Java with [Sendbird SyncManager](https://github.com/sendbird/sendbird-syncmanager-android) and [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk). Find more about SyncManager on [Sendbird SyncManager document](https://sendbird.com/docs/syncmanager/v1/android/getting-started/about-syncmanager).

<br />

## Installation

To use our Android samples, you should first install [Chat SDK for Android](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk) 3.0.96 or higher on your system.

### Requirements

|Sample|Android|Java|Gradle| 
|---|---|---|---|
|Chat|4.0 (API level 14) or higher|7 or higher|3.4.0 or higher |
|SyncManager|4.0 (API level 14) or higher|7 or higher|-|

### Chat sample

You can **clone** the project from the [Chat sample repository](https://github.com/sendbird/SendBird-Android). 

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git  

// Move to the Chat sample
cd SendBird-Android/basic
```

### SyncManager sample

You can **clone** the project directly from the [SyncManager sample repository](https://github.com/sendbird/SendBird-Android/tree/master/syncmanager).

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git

// Move to the SyncManager sample
cd SendBird-Android/syncmanager
```
