Cryptocat for Android
=================

This is a project I've started for the 2013 Cryptocat Hackathon: a Java implementation of the Cryptocat protocol, and an Android app.

**WARNING**: This is still a very early work in progress. It's under heavy development. It's not acceptably secure yet (see below) so **do NOT under ANY circumstances use it** for sensitive conversations.

**NOTE**: It's compatible with the latest Cryptocat master, not the released version. There have been some protocol changes that are still pending for release. See cryptocat/cryptocat#410 for more details.

Some screenshots: http://imgur.com/a/YQdpT

What's done
---

* Joining servers and rooms.
* Sending and receiving multiParty messages 
* Multiple servers and rooms
* User list

What's not done (yet)
---

* Fingerprint showing
* OTR (1-to-1) chat -- in progress
* File transfer
* Smilies!
* Custom server
* Lots of navigation: 
   * connect/disconnect servers
   * Join/leave rooms
   * Proper Back button
* Proper notifications
* Tablet-friendly layout (it's easy to do)
* Make it not look ugly -- in progress
* Many other things

Known bugs (they will get fixed!)
---
* No fix for the broken Android SecureRandom
* SSL certificate checking is not working (I think)
* No auto-reconnection.
* Sometimes you get randomly disconnected and the app fails to see it (maybe it's fixed)
* No way to close the service (It should close when you disconnect from all servers)

How to build
---

You will need: 
* Java JDK 1.7 or older
* Android SDK
* IntelliJ Idea (any edition will work)
* Git

1. Clone the repo

```
    git clone git@github.com/Dirbaio/Cryptocat-Android
```

2. Init and update submodules

```
    cd Cryptocat-Android
    git submodule init
    git submodule update
```

3. Open the project in IntelliJ IDEA
4. Go to File -> Project Structure
5. If you see "Module SDK: Android Platform [invalid]", click New -> Android SDK
6. Choose the location of the Java and/or Android SDKs when asked.
7. Go to Run -> Edit Configurations
8. Click the "+" icon, then choose "Android Application"
9. Select Module "Cryptocat"
10. Choose whether you want to launch on emulator or USB device
11. Click OK.
12. Now you can compile and run Cryptocat from the IDE! Congratulations! :3

Awesome libraries used
---
* [ActionBarSherlock](http://actionbarsherlock.com)
* [SlidingMenu](https://github.com/jfeinstein10/SlidingMenu)
* [aSmack](https://github.com/flowdalic/asmack)
* [Google Gson](https://code.google.com/p/google-gson/)
* [SpongyCastle](http://rtyley.github.io/spongycastle/)
* [otr4j](https://code.google.com/p/otr4j/) (Modified to use SpongyCastle instead of BouncyCastle)
* Android Support v4

