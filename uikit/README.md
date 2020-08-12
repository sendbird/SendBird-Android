# Sendbird UIKit for Android samples
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Introduction

Sendbird UIKit for Android is a development kit with an user interface that enables an easy and fast integration of standard chat features into new or existing client apps. Here are two UIKit samples for Android in the submodules. 

- **uikit-sample** is a chat app which contains UIKit’s most essential features such as push notifications and auto sign-in. When you sign in to the sample app, you will only see a list of channels rendered by the [ChannelListActivity](https://docs.sendbird.com/android/ui_kit_key_functions#3_list_channels) on the screen. 
- **uikit-custom-sample** is a chat app which contains customizable sample code for the following:  
  * Message type provides two types of messages: text and file.
  * MessageListParams provides various options for retrieving a list of messages with `MessageListParams`
  * ChannelListQuery provides various options for retrieving a list of channels with `ChannelListQuery`
  * User list provides various options for retrieving a list of users
  * Styles, colors, fonts
  * Message auto translation support 

### Sendbird UIKIT for Android doc

Find out more about Sendbird UIKit for Android at [UIKit for Android doc](https://docs.sendbird.com/android/ui_kit_getting_started).

<br />

## Before getting started

This section shows you the prerequisites you need for testing Sendbird UIKit for Android sample apps.

### Requirements

The minimum requirements for UIKit for Android are:

- Android + (API level as 16 or higher) 
- Java 8
- Support androidx only 
- Gradle 3.4.0 or higher 


### Try the sample app applied with your data 

If you would like to try the sample app specifically fit to your usage, you can do so by replacing the default sample app ID with yours, which you can obtain by [creating your Sendbird application from the dashboard](https://docs.sendbird.com/android/quick_start#3_install_and_configure_the_chat_sdk_4_step_1_create_a_sendbird_application_from_your_dashboard). This will allow you to experience the sample app with data from your Sendbird application. 

<br />

## Getting started

This section explains the steps you need to take before testing the sample apps.

### Create a project

Go to your `Android Studio` and create a project for UIKit for Android in the **Project window** as follows:

1. In the **Welcome to Android Studio** window, click **Start a new Android Studio project**.
2. In the **Select a Project Template** window, select **Empty Activity**, and click **Next**.
3. Enter your project name in the **Name** field in the **Configure your project** window.
4. Select your language as either **Java** or **Kotlin** from the **Language** drop-down menu.
5. Enable `Use androidx.*artifacts`.
6. Select minimum API level as 16 or higher.

### Install UIKit for Android

UIKit for Android is installed via `Gradle`. Begin by opening the project's top-level `build.gradle` file and adding code blocks as below:

> Note: Add the code blocks in your root `build.gradle` file, not your module `build.gradle` file.

```gradle
buildscript {
 repositories {
  google()
  jcenter()
 }
 dependencies {
  classpath 'com.android.tools.build:gradle:3.5.0'
 }
}

allprojects {
 repositories {
  google()
  jcenter()
  maven { url "https://jitpack.io" }
 }
}
```
 
Then, open the `build.gradle` file at the application level. For `Java` and `Kotlin`, add code blocks and dependencies as below:

> Note: Data binding should be enabled in your `build.gradle` file.

```gradle
apply plugin: 'com.android.application'

android {
 ...
 
 dataBinding {
  enabled = true
 }
 
 compileOptions {
  sourceCompatibility JavaVersion.VERSION_1_8
  targetCompatibility JavaVersion.VERSION_1_8
 }
 
 ...
}

dependencies {
 implementation 'com.sendbird.sdk:uikit:1.1.2'
 ...

}
```

After saving your `build.gradle` file, click the **Sync** button to apply all the changes. 

<br />

## UIKit features and ways to customize 

Here is an overview of a list of key components that can be customized on UIKit. All components can be called while fragments and activities are running on the Android platform. 

| Component | Desctription |
| :---: | :--- |
|ChannelList | A component that shows all channels a user has joined.|
|Channel | A component that shows the current channel a user has joined. From this component, users can send or receive messages.|
|CreateChannel | A component that shows all the users in your client app so you can create a channel. Users can be selected from this component to begin chatting.|
|InviteChannel | A component that shows all the users of your client app from the current channel so you can invite other users to join. |
|ChannelSettings | A component that changes the channel information.|
|MemberList | A component that shows the list of members who have joined the current channel.|
