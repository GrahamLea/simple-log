
                               Simple  Log

                               Version @VERSION@

                      http://simple-log.dev.java.net

LICENSE

Simple Log is Copyright (c) 2004-2006 Graham Lea. All rights reserved.
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

To best understand how to use Simple Log, you should read the Quick Start in the User Guide
(doc/quickStart.html). If you're too lazy to even do that, here's the general idea...

To use Simple Log in your application you need jars and properties:

1. JARS

   You need to have the simple-log.jar in your classpath.
   If you want rolling logs, you'll also need simple-log-rollover.jar.

2. PROPERTIES

   You need to have a 'simplelog.properties' file in a directory or JAR that is on your classpath.
   Simple Log will not fail if this file is not present, but it will not produce any output.

   As of Simple Log 2, it is possible for the simplelog.properties file to import other files.
   Any files you want to import must also be in the classpath.

   A template properties file is included with the release.
   The template 'simplelog.properties' file imports two other files:

      simplelog-config.properties
      simplelog-rollover.properties

   The standard file contains the default debug level and trace flag, and the documentation for
   writing properties to configure the levels and flags for your own classes and packages.

   The -config file contains properties for almost all the optional features, including writing log
   output to a file, enabling reloading properties and changing the format of log messages.

   The -rollover file contains the properties needed to enable log rolling.

   If you want to use any of these options, you will need to include the corresponding files.
   Otherwise, you should remove their names from the import property to prevent Simple Log from
   printing error messages.


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

Version 2.0 was the biggest and most important update to Simple Log since its creation.
Most notably, the inclusing of log rolling now makes Simple Log the perfect logging toolkit for
lightweight enterprise applications. Along with that, a couple of small enhancements have been
added to increase your flexibility in using Simple Log. Version 2 also sees the inclusion of a
brand-spanking new User Guide. I think you'll like it.

See ChangeLog.txt for a list of new features and completed issues.


FUTURE FEATURES

Some features that might be added in the future *if people ask for them* are:

* Programmatic access to the log formats
* Option to log threadId rather than threadName (in Java 1.5+)
* Ability to output to System.out instead of System.err
* Ability to attach listeners to a SimpleLog instance

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
