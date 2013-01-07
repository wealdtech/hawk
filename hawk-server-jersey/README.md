Using Hawk
==========

How to Implement Hawk In Your Jersey Application
================================================

* Create a service that extends HawkPrincipalProvider<T> for your own application.  This involves a get() method that takes the Hawk key ID as an argument and returns the user which owns that key.  An example is available in the test suite as ExampleUserService.
* Ensure that your user class implements HawkCredentialsProvider<T> so that it can return the full Hawk credentials.  An example is available in the test suite as ExampleUser.
* Create a Guice module that contains the bindings to your principal provider and authenticator, and add them to your injector stup.  An example is available in the test suite as HawkConfigurationModule.
  * If you are not using Guice in your Jersey application you will need to instantiate these manually instead of creating the configuration module.
* Create an authentication filter to capture incoming requests and authenticate them.  If you are expecting to authenticate purely through Hawk you can just subclass the existing HawkAuthenticationFilter with no changes.  Note that this needs to be in Jersey's resource path for it to be picked up when Jersey starts.  An example is available in the test suite as HawkExampleUserAuthenticationFilter.
* (Optional) Add a Jersey provider to access your authenticated user inside resources.  The user is stored in the servlet request attribute "com.wealdtech.authenticatedprincipal".  An example is avaialable in the test suite as ExampleUserProvider.
