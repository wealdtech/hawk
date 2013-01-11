hawk-server-jersey
==================

This is a server-side implementation of the [Hawk](/hueniverse/hawk) protocol for Jersey.  Please see [the main page](/wealdtech/hawk) for more details on the Java project.

How To Use Hawk in Your Jersey Server
=====================================

Start off by adding the following dependency to obtain the client Hawk JARs:

    <dependency>
      <groupId>com.wealdtech.hawk</groupId>
      <artifactId>hawk-server-jersey</artifactId>
      <version>0.4.0</version>
    </dependency>

Hawk needs two thing from your application to work correctly: a way of obtaining a principal given the Hawk credentials key ID, and a way of obtaining a full set of Hawk credentials given a key ID.  Specifically, the steps required are as follows:

* Create a service that extends HawkPrincipalProvider<T> for your own application.  This requires a get() method that takes the Hawk key ID as an argument and returns the principal which owns that key.  An example is available in the test suite as [ExampleUserService](src/test/java/test/com/wealdtech/hawk/service/ExampleUserService.java).
* Ensure that your principal class implements HawkCredentialsProvider<T> so that it can return the full Hawk credentials.  An example is available in the test suite as [ExampleUser](src/test/java/test/com/wealdtech/hawk/model/ExampleUser.java).

Once these are in place you need to create an authentication filter to capture incoming requests and authenticate them.  If you are expecting to authenticate purely through Hawk you can just subclass the existing [HawkAuthenticationFilter](src/main/java/com/wealdtech/hawk/jersey/HawkAuthenticationFilter.java).  Note that this needs to be in Jersey's resource path for it to be picked up when Jersey starts.  An example is available in the test suite as [HawkExampleUserAuthenticationFilter](src/test/java/test/com/wealdtech/hawk/jersey/HawkExampleUserAuthenticationFilter).

Although the various items can be instantiated maually they have also been designed to work with Guice.  To work with Guice you need to create a Guice module that contains the bindings to your principal provider and authenticator, and add them to your injector setup.  An example is available in the test suite as [HawkConfigurationModule](src/test/java/test/com/wealdtech/jersey/guice/HawkConfigurationModule.java).

If desired, you can add a Jersey provider to access your authenticated principal inside resources.  The principal is stored in the servlet request attribute "com.wealdtech.authenticatedprincipal".  An example is avaialable in the test suite as [ExampleUserProvider](src/test/java/test/com/wealdtech/jersey/providers/ExampleUserProvider.java].
