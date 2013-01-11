hawk
====

This is the Java implementation of the [Hawk](/hueniverse/hawk) protocol.  It attempts to provide packages for generic client and server functionality, along with specific implementations for popular Java products.

The current version of the Hawk protocol supported is 0.4.0.

How To Use Hawk in Your Java Project
====================================

Hawk uses Jersey for its reference implementation.  If you are using the Jersey client for carrying out HTTP requests then you should read about [using Hawk with Jersey client](hawk-client-jersey/README.md).  If you are using the Jersey server for authenticating HTTP requests then you should read about [using Hawk with Jersey server](hawk-server-jersey/README.md).  If you do not use Jersey for your HTTP client or server you should read about [building your own Hawk client or server](hawk-core/README.md)

Comments and Suggestions
========================
The Hawk libraries will attempt to keep up-to-date with the[/hueniverse/hawk](the main release of Hawk).  If you have any comments or suggestions on the protocol them please raise them there.  If you have any comments or suggestions on this Java implementation then please raise them here.

Copyright and License
======================
This project is copyright Weald Technology Trading Limited and is licensed under the Apache license version 2.0.
