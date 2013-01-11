hawk
====

This is the Java implementation of the Hawk protocol.  Hawk is more described at hueniverse/hawk.  It attempts to provide packages for generic client and server functionality, along with specific implementations for popular Java products.

The current version of the Hawk protocol supported is 0.4.0.

How To Use Hawk in Your Java Project
====================================

If you are using Jersey server then you need to add the following dependency to obtain the server-side Hawk JARs:

    <dependency>
      <groupId>com.wealdtech.hawk</groupId>
      <artifactId>hawk-server-jersey</artifactId>
      <version>0.4.0</version>
    </dependency>

If you are using Jersey client then you need to add the following dependency to obtain the client-side Hawk JARs:

    <dependency>
      <groupId>com.wealdtech.hawk</groupId>
      <artifactId>hawk-client-jersey</artifactId>
      <version>0.4.0</version>
    </dependency>

If you are not using either of these then for now you will need to build your own layer on the core Hawk implementation.  You need to add the following dependency to obtain the core Hawk JARs:

    <dependency>
      <groupId>com.wealdtech.hawk</groupId>
      <artifactId>hawk-core</artifactId>
      <version>0.4.0</version>
    </dependency>

[Using Hawk with Jersey server](hawk-server-jersey/README.md)
[Using Hawk with Jersey client](hawk-client-jersey/README.md)
[Building your own Hawk client or server](hawk-core/README.md)
