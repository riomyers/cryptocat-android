Cryptocat for Android
=================

This is a project I've started for the 2013 Cryptocat Hackathon: a Java implementation of the Cryptocat protocol, and an Android app.

**WARNING**: This is still a very early work in progress. It's under heavy development. It's not acceptably secure yet (see below) so **do NOT under ANY circumstances use it** for sensitive conversations.

**NOTE**: It's compatible with the latest Cryptocat master, not the released version. There have been some protocol changes that are still pending for release. See cryptocat/cryptocat#410 for more details.

Some screenshots: http://imgur.com/a/NlB09

What's done
---

* Joining servers and rooms.
* Sending and receiving multiParty messages 
* Multiple servers and rooms
* User list

What's not done (yet)
---

* Fingerprint showing
* OTR (1-to-1) chat
* File transfer
* Smilies!
* Custom server
* Lots of navigation: 
   * connect/disconnect servers
   * Join/leave rooms
   * Proper Back button
* Proper notifications
* Tablet-friendly layout
* Make it not look ugly
* Many other things

Known bugs (they will get fixed!)
---
* No fix for the broken Android SecureRandom
* SSL certificate checking is not working (I think)
* There's a bug in aSmack that fails to notify the app when someone leaves
* It doesn't work (?) if you join the same room with two nicknames
* Reconnection doesn't work
* Sometimes you get randomly disconnected and the app fails to see it
* No way to close the service (It should close when you disconnect from all servers)
