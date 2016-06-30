SendBird Android Sample UI
===========
This Quick Start guide will get you up and running with a `SendBird` sample project.  
When launch the sample project, you'll be able to join lobby channel, see channel list and start or join a messaging channel.

## Set up the Quick Start project

To get the Quick Start project running you will need `Android Studio` installed.

1. Clone the project
``` bash
$ git clone https://github.com/smilefam/SendBird-Android.git
```
2. Open the android project from `Android Studio`
3. Launch the project


## Before You Start
There are some terms you should understand before starting this tutorial.

1. **Open chat**: Open chat is a public chat. There is a channel which can be participated by anyone without an admission. The channel can be created on `SendBird` dashboard or via `server API`.

2. **1-on-1 messaging**: 1-on-1 messaging is a private channel between two users. Basically it is same as group messaging channel with only two members.

3. **Group messaging**: Group messaging is a private channel between multiple users. The user who wants to join the Group messaging channel has to be invited by other user who is already joined the Group messaging channel.


## See SendBird in Action
Build and run the Quick Start project to start an open chat or a 1:1 messaging. 

- The sample project is shipped with test ** `SendBird App ID` **.
- ** You must replace with your own `App ID` found in `SendBird Dashboard` for production use. **


----
### Main Page
- Once the sample project is running, you can check the following functions: 
  1. **Join Lobby channel**: You can join the open chat channel and send or receive messages to/from the channel.
  2. **See Channel List**: You can browse a list of channels and join into one of them from the list.
  3. **Start a Messaging Channel**: You can find members from the lobby channel and start 1:1 or 1:n private messaging.
  4. **See My Messaging Channel List**: You can see and manage a list of your private messaging channels.

![preview](https://raw.githubusercontent.com/smilefam/SendBird-Docs/master/images/android/sample_preview_da.png)

----
### Join Lobby channel
- You have joined an open chat channel named Lobby, you can send/receive messages or files.

![sample_1](https://raw.githubusercontent.com/smilefam/SendBird-Docs/master/images/android/sample_1_da.png)

----
### See Channel List
- You can browse all open chat channels created in `dashboard` or through `server API`.

![sample_2](https://raw.githubusercontent.com/smilefam/sendbird-android-doc/master/file/sample_2_da.png)

----
### Start a Messaging Channel
- You can see a list of member from Lobby channel and start a messaging channel with them. You should join into the lobby channel first to be listed here.

![sample_3](https://raw.githubusercontent.com/smilefam/SendBird-Docs/master/images/android/sample_3_da.png)

----
### See My Messaging Channel List
- This page shows the list of my 1-on-1 and group messaging channels.

![sample_4](https://raw.githubusercontent.com/smilefam/SendBird-Docs/master/images/android/sample_4_da.png)
