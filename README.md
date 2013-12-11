Cryptocat for Android
=================

**WARNING**: Cryptocat for Android is still in development. It has not received any kind of security audit yet, so **do NOT consider it to be secure yet**.

What's done
---

* Joining multiple servers and rooms.
* Sending and receiving multiParty messages 
* User list
* OTR (1-to-1) chat

What's not done (yet)
---

* Fingerprint showing
* File transfer
* Smilies
* Custom servers and server list
* Lots of navigation: 
   * connect/disconnect servers
   * Join/leave rooms
* Notifications
* Tablet layout

Known bugs (they will get fixed!)
---
* Auto-reconnection sometimes fails (?)
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

