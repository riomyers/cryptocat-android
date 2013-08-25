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

It's an IntelliJ IDEA project. Not sure how much luck you will have using it with other IDEs. (I tried to make it as a Maven project but failed miserably. Help on that would be highly welcome)

It requires a few libs:
* [ActionBarSherlock](http://actionbarsherlock.com)
* [SlidingMenu](https://github.com/jfeinstein10/SlidingMenu)
* [aSmack](https://github.com/flowdalic/asmack)
* [Google Gson](https://code.google.com/p/google-gson/)
* Android Support v4

If you include these libs as modules and then add them as dependancies on the Cryptocat module everything should build fine.

