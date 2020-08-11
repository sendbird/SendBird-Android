# Sendbird UIKit for Android samples
![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

## Introduction

Sendbird UIKit for Android is a development kit with an user interface that enables an easy and fast integration of standard chat features into new or existing client apps. Here are two UIKit samples for Android in the submodules. 

- **uikit-sample** is a chat app which contains UIKit’s most essential features such as push notifications and auto sign-in. When you sign in to the sample app, you will only see a list of channels rendered by the [ChannelListActivity](https://docs.sendbird.com/android/ui_kit_key_functions#3_list_channels) on the screen.  UIKit basic sample.
- **uikit-custom-sample** is a chat app which contains customizable sample code for the following:  
  * Message type provides two types of messages: text and file.
  * MessageListParams provides various options for retrieving a list of messages with `MessageListParams`
  * ChannelListQuery provides various options for retrieving a list of channels with `ChannelListQuery`
  * User list provides various options for retrieving a list of users
  * Styles, colors, fonts
  * Message auto translation support 

### Sendbird UIKIT for Android doc

Find out more about Sendbird UIKit for Android at [UIKit for Android doc](https://docs.sendbird.com/android/ui_kit_getting_started).
  
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



 ## UIKit basic sample
 UIKit basic sample demonstrates the following features.
 * Push notifications
 * Auto-login
 
 ## UIKit customized sample
 UIKit customized sample demonstrates the following features.
 * Custom message type
 * Usage of MessageListParams
 * Usage of ChannelListQuery
 * Custom user list
 * Custom styles, colors, fonts
 * Multilingual support
