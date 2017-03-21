SendBird Android Sample UI
===========
The Sendbird Android Sample UI is a fully functional messaging app. In **Open Channels**, users can freely enter and chat with anyone within a channel. **Group Channels** can be created by inviting others into a private 1:1 chat or a group chat among multiple friends.


## Table of Contents

  1. [Installation](#installation)
  1. [Integrating the sample into your own app](#integrating-the-sample-into-your-own-app)
  1. [Previous versions](#previous-versions)
  1. [Contributing](#contributing)
  
## Installation

You can open the sample project from **Android Studio**.

Build and run the Sample UI project to play around with Open Channels and Group Channels.

> The sample project is shipped with a **Testing App ID**. This means that you are sharing the app, including its users, channels, and messages, with everyone who downloads this sample or samples in other platforms. To use the sample in your own app, see the **2. Integrating the sample into your own app** section.

### Notes

* This sample currently uses `v25.3.0` of the Android Support Libraries.
* If you encounter a `Failed to resolve: com.google.firebase:firebase-messaging:9.6.1` error message while building, please install or upgrade to the latest Google Repository from the SDK Manager.
* The current minimum SDK version is `14`. This is due to the Google Play Services and Firebase dropping support for `<14` versions beginning from Google Play Services `v10.2.0`.

    However, the SendBird SDK is compatible with all Android versions from Gingerbread(SDK version 10), and if you wish to run the sample on an older device, you can simply downgrade the Firebase version.


## Integrating the sample into your own app

If you wish to use parts of the sample for messaging in your own app, you must create a new SendBird application from the **[SendBird Dashboard](https://dashboard.sendbird.com)**. If you do not yet have an account, you can log in with Google, GitHub, or create a new account.

After you create a SendBird application in the Dashboard, replace `APP_ID` in `BaseApplication` with your own App ID. You will then be able to manage the users and channels, as well as general settings of your messaging app, through your Dashboard.

> All users within the same SendBird application are able to communicate with each other, across all platforms. This means users using iOS, Android, web clients, etc. can all chat with one another. However, users in different SendBird applications cannot talk to each other.

**For more information, please refer to our [documentation](https://docs.sendbird.com/android).**


## Previous versions

To view the version 2 sample, checkout the `v2` branch instead of `master.`


## Contributing

The SendBird Android Sample UI is fully open-source. All contributions and suggestions are welcome!
