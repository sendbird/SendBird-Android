# Sendbird Android Samples

## Introduction

[Sendbird](https://sendbird.com) provides the chat API and SDK for your app, enabling real-time communication among the users. Here are various samples built using Sendbird Chat SDK.

- **Chat sample** has core chat features. Group channel and open channel are the two main channel types in which you can create various subtypes where users can send and receive messages. This sample is written in Java with [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk).

- **SyncManager sample** is equipped with a local cache along with core chat features. For faster data loading and caching, the sample synchronizes with the Sendbird server and saves a list of group channels and the messages within the local cache into your client app. This sample is written in Java with [Sendbird SyncManager](https://github.com/sendbird/sendbird-syncmanager-android) and [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk). Find more about SyncManager on [Sendbird SyncManager document](https://docs.sendbird.com/ios/sync_manager_getting_started).

- **UIKit sample** is a user interface development kit that allows easy and fast integration of core chat features for new or pre-existing client apps. UI components can be fully customized with ease to expedite the roll-out of your client app’s in-app chat service. This sample is written in Java with [Sendbird UIKit](https://github.com/sendbird/SendBird-Android/tree/master/uikit) and [Sendbird Chat SDK](https://github.com/sendbird/SendBird-SDK-Android/tree/master/com/sendbird/sdk/sendbird-android-sdk). Find more about Sendbird UIKit on [Sendbird UIKit document](https://docs.sendbird.com/android/ui_kit_getting_started)

## Installation

### Requirements

The minimum requirements for Chat SDK for Android are:
* Android 4.0 (API level 14) or higher
* Java 7 or higher
* Gradle 3.4.0 or higher

### Chat sample

A. You can **clone** the project directly from the [Chat sample repository](https://github.com/sendbird/SendBird-Android). 

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git  

// Clone only Chat sample
git submodule update --init ./basic
```

B. You can **pull** it by using the **submodule** command after **cloning** the git repository.

```
// Clone all submodule's repositories
git submodule update --init --recursive    
```


### SyncManager sample

This sample is linked with the git submodule which you can download in two ways. 

A. You can **clone** the project directly from the [SyncManager sample repository](https://github.com/sendbird/SendBird-Android/tree/master/syncmanager).

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git

// Clone only SyncManager sample
git submodule update --init ./syncmanager
```

B. You can **pull** it by using the **submodule** command after **cloning** the git repository.

```
// Clone all submodule's repositories
git submodule update --init --recursive    
```

### UIKit sample

This sample is linked with the git submodule which you can download in two ways. 

A. You can **clone** the project directly from the [UIKit sample repository](https://github.com/sendbird/SendBird-Android/tree/master/uikit). Or you can pull it using submodule command after this git repository.

```
// Clone this repository
git clone git@github.com:sendbird/SendBird-Android.git

// Clone only UIKit sample
git submodule update --init ./uikit
```

B. You can **pull** it by using the **submodule** command after **cloning** the git repository.

```
// Clone all submodule's repositories
git submodule update --init --recursive    
```
