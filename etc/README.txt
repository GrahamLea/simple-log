
                               Simple  Log

                               Version @VERSION@

                      http://simple-log.dev.java.net

LICENSE

Simple Log is Copyright (c) 2004-2005 Graham Lea. All rights reserved.
Simple Log is freely distributed under the Apache License 2.0.
See LICENSE.txt for details.


INSTALLATION

To install Simple Log, simply unzip the the release ZIP to wherever you keep your Java libraries,
making sure the 'Use Folder Names' option is on.


REQUIREMENTS

Simple Log requires a Java Runtime Environment (JRE) of version 1.4 or above.
If you need to use it in a lower version, get in contact with me and I'll see what I can do.

Download Java now   @   http://java.com/


USAGE

To use Simple Log in your application, you will need to:

1. Have the simple-log.jar in your classpath.

2. Have a 'simplelog.properties' file in a directory or JAR that is on your classpath.
   Simple Log will not fail if this file is not present, but it will not produce any output.
   A template properties file is included with the release.


COMMONS LOGGING

If you plan use Simple Log's CommonsLoggingAdapter with Jakarta Commons Logging, you will also need
to:

1. Include the simple-log-commons-logging.jar in your classpath.

2. Set the system property:   org.apache.commons.logging.Log
                        to:   org.grlea.log.adapters.commons.CommonsLoggingAdapter

You should also read the documentation of CommonsLoggingAdapter very carefully, as it contains
important information about how the commons-logging API maps to a Simple Log configuration.


SLF4J

If you plan use Simple Log's Slf4jAdapter with SLF4J, you will also need to:

1. Include the simple-log-slf4j.jar in your classpath.

You should also read the documentation of Slf4jAdapter very carefully, as it contains important
information about how the SLF4J API maps to a Simple Log configuration.


STATUS

Version 1.0 was basically a major refactor of my own similar logger that I've been using for years.

Version 1.7 continues the tradition of implementing a good suggestion from a user and making a
release! I've also started implementing some of the outstanding issues,so this release now comes
with adapters for commons-logging and SLF4J (which explains why the release doubled in size).

See ChangeLog.txt for a list of new features and completed issues.

Users should not that the configuration files for version 1.6 are incompatible with previous
versions' configuration files in two small ways:
1. The log formats have changed, which will change the result of any custom formats being used.
   Old formats can be upgraded by changing any {4} and {5} arguments to {5} and {6}, respectively.
   Users using the default formats should not be affected.
2. The tracing configuration now uses the suffix "#trace" instead of ".trace".
   Most old configurations can be upgraded by performing a search/replace for .trace/#trace


FUTURE FEATURES

Some features that might be added in the future *if people ask for them* are:

* Programmatic access to the log formats
* Ability to turn logging off for a class/package (rather than just down to "Fatal")
* Option to use a different filename/location for the default SimpleLog instance's properties
* Log file rolling

If you think you need one of these features, or some other feature, please feel free to contact me
and I should be able to hack it up within a couple of days (assuming I haven't gone camping).
But remember, this is "Simple Log", so if you ask me to do something like "make it more extendable",
expect to be vilified. ;)


CONTACT

 - Reporting Bugs -

If you find a bug, please follow these steps:

1. Make sure it is a bug!
   Re-read the documentation and check that you're not doing something that's causing the problem.

2. Try to figure out something about the bug yourself.
   Does it only occur with a certain type of message, object, value (e.g. null)?
   Does it only occur when the properties file is in a certain place?
   Can you write a really small test class that demonstrates the problem?

3. Send an email, with the subject "Simple Log BUG", to:

      issues@simple-log.dev.java.net

   Please describe the problem VERBOSELY.
   Make sure to include your Java versions and Simple Log version.
   Also include in the email everything you discovered about the bug in step 2.


 - Contacting the Creator -

If you need to contact me regarding Simple Log, I can be reached at owner@simple-log.dev.java.net
