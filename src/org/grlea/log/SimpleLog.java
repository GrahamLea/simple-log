package org.grlea.log;

// $Id: SimpleLog.java,v 1.4 2005-01-18 10:42:52 grlea Exp $
// Copyright (c) 2004 Graham Lea. All rights reserved.

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Controls the configuration and formatting of a group of <code>SimpleLogger</code>s.</p>
 *
 * <p><code>SimpleLog</code> has a default instance, which is probably the only one that anyone will
 * ever use. If you use the default instance, you don't even really need to know anything about
 * <code>SimpleLog</code> - just use the {@link SimpleLogger#SimpleLogger(Class) basic SimpleLogger
 * constructor} and you'll never even know nor care.</p>
 *
 * @version $Revision: 1.4 $
 * @author $Author: grlea $
 */
public final class
SimpleLog
{
   // TODO (grahaml) Need a way to turn logging OFF for a package/class?

   // Constants
   //...............................................................................................

   /** The default name of the properties file used to configure a <code>SimpleLog</code>. */
   private static final String DEFAULT_PROPERTIES_FILE_NAME = "simplelog.properties";

   /** The prefix for all special properties keys. */
   private static final String KEY_PREFIX = "simplelog.";

   /** The prefix for all format-related properties keys. */
   private static final String KEY_FORMAT_PREFIX = KEY_PREFIX + "format.";

   /** The suffix for format property keys relating to instance formats. */
   private static final String KEY_FORMAT_INSTANCE_SUFFIX = ".instance";

   /** The property key for the debug format. */
   private static final String KEY_FORMAT_DB = KEY_FORMAT_PREFIX + "debug";

   /** The property key for the debug object format. */
   private static final String KEY_FORMAT_DBO = KEY_FORMAT_PREFIX + "debugObject";

   /** The property key for the debug exception format. */
   private static final String KEY_FORMAT_DBE = KEY_FORMAT_PREFIX + "debugException";

   /** The property key for the entry format. */
   private static final String KEY_FORMAT_ENTRY = KEY_FORMAT_PREFIX + "entry";

   /** The property key for the exit format. */
   private static final String KEY_FORMAT_EXIT = KEY_FORMAT_PREFIX + "exit";

   /** The property key for properties reloading. */
   private static final String KEY_RELOADING = KEY_PREFIX + "reloading";

   /** The default value for the reloading properties property. */
   private static final String KEY_LOG_FILE = KEY_PREFIX + "logFile";

   /** The default value for the reloading properties property. */
   private static final String RELOADING_DEFAULT = "false";

   /** The property key for the default debug level. */
   private static final String KEY_DEFAULT_LEVEL = KEY_PREFIX + "defaultLevel";

   /** The property key for the default tracing flag. */
   private static final String KEY_DEFAULT_TRACE = KEY_PREFIX + "defaultTrace";

   /** The property key for the date format. */
   private static final String KEY_DATE_FORMAT = KEY_PREFIX + "dateFormat";

   /** The default date format. */
   private static final String DATE_FORMAT_DEFAULT = "EEE yyyy/MM/dd HH:mm:ss.SSS";

   /** The property key for the print stack traces property. */
   private static final String KEY_PRINT_STACK_TRACES = KEY_PREFIX + "printStackTraces";

   /** The default value for the print stack traces property. */
   private static final String PRINT_STACK_TRACES_DEFAULT = "true";

   /**
    * The period (in milliseconds) between checks of the properties file (must be a FILE, not inside
    * a JAR).
    */
   private static final int RELOAD_FILE_CHECK_PERIOD = 20 * 1000;

   /**
    * The period (in milliseconds) between checks of the properties when they're not in a file.
    */
   private static final int RELOAD_URL_CHECK_PERIOD = 60 * 1000;

   /**
    * The suffix used in the properties file to identify a trace flag.
    */
   private static final String TRACE_SUFFIX = ".trace";

   /**
    * The string to use to print a new line between the debug exception line and the stack trace.
    */
   private static final String LINE_SEP = System.getProperty("line.separator");

   // Default Message Formats
   //..........................................................................................
   // The following arguments are always the same:
   // {0} - The current date and time (formatted as specified by DATE_FORMAT)
   // {1} - The name of the current Thread.
   // {2} - The name of the debugging class.
   // {3} - The instance ID, if in use.

   private static final String DEFAULT_FORMAT_STRING_DB = "{0}|   |{1}|{2}|{4}";
   private static final String DEFAULT_FORMAT_STRING_DBO = "{0}|---|{1}|{2}|{4}|{5}";
   private static final String DEFAULT_FORMAT_STRING_DBE = "{0}|***|{1}|{2}|{4}";
   private static final String DEFAULT_FORMAT_STRING_ENTRY = "{0}|>>>|{1}|{2}|{4}";
   private static final String DEFAULT_FORMAT_STRING_EXIT = "{0}|<<<|{1}|{2}|{4}";

   private static final String DEFAULT_FORMAT_STRING_DB_INSTANCE = "{0}|   |{1}|{2}[{3}]|{4}";
   private static final String DEFAULT_FORMAT_STRING_DBO_INSTANCE = "{0}|---|{1}|{2}[{3}]|{4}|{5}";
   private static final String DEFAULT_FORMAT_STRING_DBE_INSTANCE = "{0}|***|{1}|{2}[{3}]|{4}";
   private static final String DEFAULT_FORMAT_STRING_ENTRY_INSTANCE = "{0}|>>>|{1}|{2}[{3}]|{4}";
   private static final String DEFAULT_FORMAT_STRING_EXIT_INSTANCE = "{0}|<<<|{1}|{2}[{3}]|{4}";

   // Static variables
   //..........................................................................................

   /**
    * The default instance of <code>SimpleLog</code>.
    *
    * @see #defaultInstance()
    */
   private static SimpleLog defaultInstance = null;

   /**
    * An object to synchronize on when checking or changing the {@link #defaultInstance}.
    */
   private static final Object defaultInstanceLock = new Object();

   // Constants
   //...............................................................................................

   /**
    * The URL where this <code>SimpleLog</code> loaded (and may reload) its configuration from.
    */
   private final URL configurationSource;

   /** The properties governing this <code>SimpleLog</code>. */
   private final Properties properties;

   /** The destination of this <code>SimpleLog</code>'s output. */
   private PrintWriter out;

   /**
    * The path and name of the log file being written to, or <code>null</code> if output is going
    * somewhere else.
    */
   private String logFile;

   /** The default level of this <code>SimpleLog</code>. */
   private DebugLevel defaultLevel = DebugLevel.L4_INFO;

   /** The default trace flag of this <code>SimpleLog</code>. */
   private boolean defaultTracing = false;

   /** The {@link SimpleLogger}s attached to this <code>SimpleLog</code>. */
   private final List loggers = new ArrayList(32);

   /** An object to synchronize on when accessing or modifying the {@link #loggers} list. */
   private final Object LOGGERS_LOCK = new Object();

   /** The date format used in all formats in this <code>SimpleLog</code>. */
   private DateFormat dateFormat;

   /**
    * Message format for a simple debug message.
    * 4 = message (String)
    */
   private MessageFormat dbFormat;

   /**
    * Message format for a debug object message.
    * 4 = object name (String)
    * 5 = object value (Object)
    */
   private MessageFormat dboFormat;

   /**
    * Message format for a debug exception message.
    * 4 = exception (Throwable)
    */
   private MessageFormat dbeFormat;

   /**
    * Message format for a trace entry message.
    * 4 = method name (String)
    */
   private MessageFormat entryFormat;

   /**
    * Message format for a trace exit message.
    * 4 = method name (String)
    */
   private MessageFormat exitFormat;

   /**
    * Message format for a simple debug message (instance version).
    * 4 = message (String)
    */
   private MessageFormat dbFormat4Instance;

   /**
    * Message format for a debug object message (instance version).
    * 4 = object name (String)
    * 5 = object value (Object)
    */
   private MessageFormat dboFormat4Instance;

   /**
    * Message format for a debug exception message (instance version).
    * 4 = exception (Throwable)
    */
   private MessageFormat dbeFormat4Instance;

   /**
    * Message format for a trace entry message (instance version).
    * 4 = method name (String)
    */
   private MessageFormat entryFormat4Instance;

   /**
    * Message format for a trace exit message (instance version).
    * 4 = method name (String)
    */
   private MessageFormat exitFormat4Instance;

   /**
    * Creates a new <code>SimpleLog</code> configured by the given properties object. <br>
    * All <code>SimpleLog</code>s log to System.err by default.
    *
    * @param properties a properties object containing properties that will be used to configure
    * the <code>SimpleLog</code>.
    *
    * @throws IllegalArgumentException if <code>properties</code> is <code>null</code>.
    */
   public
   SimpleLog(Properties properties)
   {
      if (properties == null)
         throw new IllegalArgumentException("properties cannot be null.");

      this.configurationSource = null;
      this.properties = properties;
      this.out = new PrintWriter(System.err, true);

      readSettingsFromProperties();
   }

   /**
    * Creates a new <code>SimpleLog</code> configured by a properties file read from the given URL.
    * <br>
    * All <code>SimpleLog</code>s log to System.err by default.
    *
    * @param configurationSource the properties file to use to configure the <code>SimpleLog</code>
    * instance.
    *
    * @throws IOException if the properties file cannot be read from the given URL.
    */
   public
   SimpleLog(URL configurationSource)
   throws IOException
   {
      this.configurationSource = configurationSource;
      this.properties = new Properties();
      this.out = new PrintWriter(System.err, true);

      loadProperties();
      readSettingsFromProperties();

      // Note: Reloading is only checked on creation
      String reloadingString = properties.getProperty(KEY_RELOADING, RELOADING_DEFAULT);
      boolean reloading = Boolean.valueOf(reloadingString).booleanValue();
      if (reloading)
      {
         Timer timer = new Timer(true);
         TimerTask reloadTask;
         int reloadPeriod;

         if (configurationSource.getProtocol() != null &&
             "file".equals(configurationSource.getProtocol().toLowerCase()))
         {
            reloadTask = new FileConfigurationReloader();
            reloadPeriod = RELOAD_FILE_CHECK_PERIOD;
         }
         else
         {
            reloadTask = new UrlConfigurationReloader();
            reloadPeriod = RELOAD_URL_CHECK_PERIOD;
         }

         timer.schedule(reloadTask, reloadPeriod, reloadPeriod);
      }
   }

   /**
    * Loads the properties file from this <code>SimpleLog</code>'s configuration source and reads
    * all the special properties used to configure the <code>SimpleLog</code> (as opposed to those
    * that configure the attached <code>SimpleLogger</code>s).
    *
    * @throws IOException if the properties file cannot be read from the URL.
    */
   private void
   loadProperties()
   throws IOException
   {
      if (properties == null)
         return;

      // Load the properties into a new object, then replace the current ones if the read suceeds.
      InputStream inputStream = configurationSource.openStream();
      Properties newProperties = new Properties();
      newProperties.load(inputStream);

      properties.clear();
      properties.putAll(newProperties);
   }

   /**
    * Reads settings from this <code>SimpleLog</code>'s properties object and changes the object's
    * state accordingly.
    */
   private void
   readSettingsFromProperties()
   {
      // Read the log file
      String newLogFile = properties.getProperty(KEY_LOG_FILE);
      boolean logFileChanged = (logFile == null) != (newLogFile == null);
      logFileChanged |= logFile != null && newLogFile != null && !newLogFile.equals(newLogFile);
      if (logFileChanged)
      {
         try
         {
            if (newLogFile == null)
            {
               out = new PrintWriter(System.err, true);
            }
            else
            {
               File file = new File(newLogFile);
               file.getParentFile().mkdirs();
               FileWriter fileWriter = new FileWriter(file, true);
               out = new PrintWriter(fileWriter, true);
            }
            logFile = newLogFile;
         }
         catch (IOException e)
         {
            printError("Error opening log file for writing", e, true);
         }
      }

      // Read the Default level
      String defaultLevelStr = properties.getProperty(KEY_DEFAULT_LEVEL);
      if (defaultLevelStr != null)
      {
         try
         {
            int level = Integer.parseInt(defaultLevelStr);
            defaultLevel = DebugLevel.fromInt(level);
         }
         catch (NumberFormatException e)
         {
            printError("Error parsing debug level for " + KEY_DEFAULT_LEVEL, e, false);
         }
      }

      // Read the Default trace
      String defaultTraceStr = properties.getProperty(KEY_DEFAULT_TRACE);
      if (defaultTraceStr != null)
      {
         defaultTracing = Boolean.valueOf(defaultTraceStr).booleanValue();
      }

      // Read the Date format
      String dateFormatString = properties.getProperty(KEY_DATE_FORMAT, DATE_FORMAT_DEFAULT);
      try
      {
         dateFormat = new SimpleDateFormat(dateFormatString);
      }
      catch (IllegalArgumentException e)
      {
         printError("Error parsing date format", e, false);
      }

      // Read formats
      dbFormat = readFormat(KEY_FORMAT_DB, DEFAULT_FORMAT_STRING_DB);
      dboFormat = readFormat(KEY_FORMAT_DBO, DEFAULT_FORMAT_STRING_DBO);
      dbeFormat = readFormat(KEY_FORMAT_DBE, DEFAULT_FORMAT_STRING_DBE);
      entryFormat = readFormat(KEY_FORMAT_ENTRY, DEFAULT_FORMAT_STRING_ENTRY);
      exitFormat = readFormat(KEY_FORMAT_EXIT, DEFAULT_FORMAT_STRING_EXIT);

      dbFormat4Instance =
         readFormat(KEY_FORMAT_DB + KEY_FORMAT_INSTANCE_SUFFIX, DEFAULT_FORMAT_STRING_DB_INSTANCE);
      dboFormat4Instance =
         readFormat(KEY_FORMAT_DBO + KEY_FORMAT_INSTANCE_SUFFIX, DEFAULT_FORMAT_STRING_DBO_INSTANCE);
      dbeFormat4Instance =
         readFormat(KEY_FORMAT_DBE + KEY_FORMAT_INSTANCE_SUFFIX, DEFAULT_FORMAT_STRING_DBE_INSTANCE);
      entryFormat4Instance =
         readFormat(KEY_FORMAT_ENTRY + KEY_FORMAT_INSTANCE_SUFFIX,
                    DEFAULT_FORMAT_STRING_ENTRY_INSTANCE);
      exitFormat4Instance =
         readFormat(KEY_FORMAT_EXIT + KEY_FORMAT_INSTANCE_SUFFIX,
                    DEFAULT_FORMAT_STRING_EXIT_INSTANCE);

      updateDateFormats();

      // Read the Print stack traces property
      String printStackTracesStr =
         properties.getProperty(KEY_PRINT_STACK_TRACES, PRINT_STACK_TRACES_DEFAULT);
      boolean printStackTraces = Boolean.valueOf(printStackTracesStr).booleanValue();
      if (printStackTraces)
      {
         ExceptionFormat exceptionFormat = new ExceptionFormat();
         dbeFormat.setFormatByArgumentIndex(4, exceptionFormat);
         dbeFormat4Instance.setFormatByArgumentIndex(4, exceptionFormat);
      }

      // TODO (grahaml) Option of different outputs
   }

   /**
    * Reloads this <code>SimpleLog</code>'s configuration.
    */
   public void
   reloadProperties()
   {
      try
      {
         if (configurationSource != null)
            loadProperties();

         readSettingsFromProperties();
         reconfigureAllLoggers();
      }
      catch (Exception e)
      {
         printError("Falied to reload properties", e, true);
      }
   }

   /**
    * Reads a format from a specified key.
    *
    * @param key the key to read from the properties to obtain a new message format string.
    *
    * @param defaultPattern the default pattern to use if the property doesn't exist.
    *
    * @return a new MessageFormat, created from the resulting message format string.
    */
   private MessageFormat
   readFormat(String key, String defaultPattern)
   {
      String formatString = properties.getProperty(key, defaultPattern);
      MessageFormat format;
      try
      {
         format = new MessageFormat(formatString);
      }
      catch (Exception e)
      {
         printError("Error reading format string from " + key, e, false);
         format = new MessageFormat(defaultPattern);
      }
      return format;
   }

   /**
    * <p>Returns the default instance of <code>SimpleLog</code>.</p>
    *
    * <p>The default instance is either configured from a properties file found on the classpath
    * with the name 'simplelog.properties', or not configured at all (if the file cannot be found).
    * <br>If the instance is not configured at all, it will not produce any output unless a writer
    * is programmatically assigned to it (see {@link #setWriter}).</p>
    *
    * @return the default instance of <code>SimpleLog</code>.
    */
   public static SimpleLog
   defaultInstance()
   {
      synchronized (defaultInstanceLock)
      {
         if (defaultInstance == null)
         {
            // TODO (grahaml) Option for different file name?

            URL propertiesUrl =
               SimpleLog.class.getClassLoader().getResource(DEFAULT_PROPERTIES_FILE_NAME);

            if (propertiesUrl != null)
            {
               try
               {
                  defaultInstance = new SimpleLog(propertiesUrl);
               }
               catch (Exception e)
               {
                  printError("Error while attempting to load default properties", e, true);
               }
            }

            if (defaultInstance == null)
            {
               defaultInstance = new SimpleLog(new Properties());
               defaultInstance.setWriter(null);
            }
         }
      }
      return defaultInstance;
   }

   /**
    * Prints the given string to this <code>SimpleLog</code>'s output destination, followed by a
    * newline sequence.
    *
    * @param s the string to print
    *
    * @see PrintWriter#println(String)
    */
   void
   println(String s)
   {
      if (out != null)
         out.println(s);
   }

   /**
    * Configures the given logger by setting its debug level and trace flag according to the current
    * properties.
    *
    * @param logger the logger to configure.
    */
   void
   configure(SimpleLogger logger)
   {
      logger.setDebugLevel(getDebugLevel(logger.getSourceClass()));
      logger.setTracing(getTracingFlag(logger.getSourceClass()));
   }

   /**
    * Retrieves the debug level for the given class from the properties.
    *
    * @param sourceClass the class to find the debug level for.
    *
    * @return the debug level to be used for the class.
    */
   private DebugLevel
   getDebugLevel(Class sourceClass)
   {
      if (properties == null)
         return defaultLevel;

      String name = sourceClass.getName();

      int dotIndex = name.length();
      DebugLevel debugLevel = null;
      do
      {
         // On first iteration, this substring() call returns the whole string.
         // On subsequent iterations, it removes everything after and including the last period.
         name = name.substring(0, dotIndex);
         String value = properties.getProperty(name);
         if (value != null)
         {
            try
            {
               int level = Integer.parseInt(value);
               debugLevel = DebugLevel.fromInt(level);
            }
            catch (NumberFormatException e)
            {
               printError("Error parsing debug level for " + name, e, true);
            }
         }

         dotIndex = name.lastIndexOf('.');
      }
      while (debugLevel == null && dotIndex != -1);

      // If we found no level, use the default.
      if (debugLevel == null)
         debugLevel = defaultLevel;

      return debugLevel;
   }

   /**
    * Retrieves the tracing flag for the given class from the properties.
    *
    * @param sourceClass the class to find the tracing flag for.
    *
    * @return the tracing flag to be used for the class.
    */
   private boolean
   getTracingFlag(Class sourceClass)
   {
      if (properties == null)
         return defaultTracing;

      String name = sourceClass.getName();

      int dotIndex = name.length();
      boolean trace = defaultTracing;
      do
      {
         name = name.substring(0, dotIndex);
         String value = properties.getProperty(name + TRACE_SUFFIX);
         if (value != null)
         {
            trace = Boolean.valueOf(value).booleanValue();
            break;
         }

         dotIndex = name.lastIndexOf('.');
      }
      while (dotIndex != -1);

      return trace;
   }

   /**
    * Registers the give logger with this <code>SimpleLog</code>, so that the logger can be updaed
    * if the properties change.
    *
    * @param logger the logger to register
    */
   void
   register(SimpleLogger logger)
   {
      synchronized (LOGGERS_LOCK)
      {
         loggers.add(logger);
      }
      configure(logger);
   }

   /**
    * Re-configures all {@link SimpleLogger}s registered with this <code>SimpleLog</code>.
    */
   private void
   reconfigureAllLoggers()
   {
      synchronized (LOGGERS_LOCK)
      {
         for (Iterator iter = loggers.iterator(); iter.hasNext();)
         {
            configure((SimpleLogger) iter.next());
         }
      }
   }

   /**
    * Prints an error message from this <code>SimpleLog</code>.
    *
    * @param description a description the error
    * @param t the exception that occurred to cause the error
    * @param printExceptionType whether the whole toString of the exception should be printed (true)
    * of just the exception's 'message' (false).
    */
   private static void
   printError(String description, Throwable t, boolean printExceptionType)
   {
      System.err.println();

      System.err.print("   SimpleLog ERROR: ");
      System.err.print(description);
      System.err.print(": ");
      if (printExceptionType)
         System.err.print(t);
      else
         System.err.print(t.getMessage());

      System.err.println();
      System.err.println();
   }

   /**
    * Returns this <code>SimpleLog</code>'s default level.
    */
   public DebugLevel
   getDefaultLevel()
   {
      return defaultLevel;
   }

   /**
    * Sets this <code>SimpleLog</code>'s default level.
    *
    * @param defaultLevel the new default debug level
    *
    * @throws IllegalArgumentException if defaultLevel is null.
    */
   public void
   setDefaultLevel(DebugLevel defaultLevel)
   {
      if (defaultLevel == null)
         throw new IllegalArgumentException("defaultLevel cannot be null.");
      this.defaultLevel = defaultLevel;
      reconfigureAllLoggers();
   }

   /**
    * Returns this <code>SimpleLog</code>'s default tracing flag.
    */
   public boolean
   isDefaultTracing()
   {
      return defaultTracing;
   }

   /**
    * Sets this <code>SimpleLog</code>'s default tracing flag.
    *
    * @param defaultTracing the new default trace value (<code>true</code> for on, <code>false</code>
    * for off).
    */
   public void
   setDefaultTracing(boolean defaultTracing)
   {
      this.defaultTracing = defaultTracing;
      reconfigureAllLoggers();
   }

   /**
    * Returns this <code>SimpleLog</code>'s default date format.
    */
   public DateFormat
   getDateFormat()
   {
      return dateFormat;
   }

   /**
    * Sets this <code>SimpleLog</code>'s default date format. If the given value is
    * <code>null</code>, the default date format will be used.
    *
    * @param newDateFormat the new date format. May be <code>null</code>.
    */
   public void
   setDateFormat(DateFormat newDateFormat)
   {
      if (newDateFormat == null)
         dateFormat = new SimpleDateFormat(DATE_FORMAT_DEFAULT);
      else
         dateFormat = newDateFormat;

      updateDateFormats();
   }

   /**
    * Returns the print writer that is the destination for all of this <code>SimpleLog</code>'s
    * output.
    *
    * @return the print writer being used by this <code>SimpleLog</code>, or <code>null</code> if
    * it doesn't have one.
    */
   public PrintWriter
   getWriter()
   {
      return out;
   }

   /**
    * Sets the print writer to be used as the destination for all of this <code>SimpleLog</code>'s
    * output.
    *
    * @param out the print to be used by this <code>SimpleLog</code>, or <code>null</code> if
    * it should not create any output.
    */
   public void
   setWriter(PrintWriter out)
   {
      this.out = out;
   }

   /**
    * Returns whether this <code>SimpleLog</code> is currently printing output at all.
    */
   boolean
   isOutputting()
   {
      return out != null;
   }

   /**
    * Updates the date format in all of this <code>SimpleLog</code>'s message formats.
    */
   private void
   updateDateFormats()
   {
      dbFormat.setFormatByArgumentIndex(0, dateFormat);
      dboFormat.setFormatByArgumentIndex(0, dateFormat);
      dbeFormat.setFormatByArgumentIndex(0, dateFormat);
      entryFormat.setFormatByArgumentIndex(0, dateFormat);
      exitFormat.setFormatByArgumentIndex(0, dateFormat);

      dbFormat4Instance.setFormatByArgumentIndex(0, dateFormat);
      dboFormat4Instance.setFormatByArgumentIndex(0, dateFormat);
      dbeFormat4Instance.setFormatByArgumentIndex(0, dateFormat);
      entryFormat4Instance.setFormatByArgumentIndex(0, dateFormat);
      exitFormat4Instance.setFormatByArgumentIndex(0, dateFormat);
   }

   MessageFormat
   getDebugFormat()
   {
      return dbFormat;
   }

   MessageFormat
   getDebugInstanceFormat()
   {
      return dbFormat4Instance;
   }

   MessageFormat
   getDebugObjectFormat()
   {
      return dboFormat;
   }

   MessageFormat
   getDebugObjectInstanceFormat()
   {
      return dboFormat4Instance;
   }

   MessageFormat
   getDebugExceptionFormat()
   {
      return dbeFormat;
   }

   MessageFormat
   getDebugExceptionInstanceFormat()
   {
      return dbeFormat4Instance;
   }

   MessageFormat
   getEntryFormat()
   {
      return entryFormat;
   }

   MessageFormat
   getEntryInstanceFormat()
   {
      return entryFormat4Instance;
   }

   MessageFormat
   getExitFormat()
   {
      return exitFormat;
   }

   MessageFormat
   getExitInstanceFormat()
   {
      return exitFormat4Instance;
   }

   /**
    * Format implementation that prints the stack trace of an exception after the exception object's
    * toString.
    */
   private static final class
   ExceptionFormat
   extends Format
   {
      public StringBuffer
      format(Object obj, StringBuffer buf, FieldPosition pos)
      {
         if (!(obj instanceof Throwable))
            throw new IllegalArgumentException(getClass().getName() + " only formats Throwables.");

         Throwable t = (Throwable) obj;
         buf.append(t);

         // Append the stack trace to the buffer.
         StringWriter sw;
         t.printStackTrace(new PrintWriter(sw = new StringWriter()));
         buf.append(LINE_SEP).append(sw.toString());

         return buf;
      }

      public Object
      parseObject (String source, ParsePosition pos)
      {
         // Parsing not supported.
         throw new UnsupportedOperationException();
      }
   }

   /**
    * A {@link TimerTask} that checks to see if the last modified date of the configuration source
    * (which must be a "file" URL) has changed and, if it has, reloads the <code>SimpleLog</code>'s
    * configuration.
    */
   private final class
   FileConfigurationReloader
   extends TimerTask
   {
      private final File configurationFile;

      private long previousLastModified;

      public
      FileConfigurationReloader()
      {
         URI uri = null;
         try
         {
            uri = new URI(configurationSource.toExternalForm());
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Failed to create URI from URL");
         }

         this.configurationFile = new File(uri);
         this.previousLastModified = configurationFile.lastModified();
      }

      public void
      run()
      {
         long lastModified = configurationFile.lastModified();
         if (previousLastModified != lastModified)
         {
            reloadProperties();
            previousLastModified = lastModified;
         }
      }
   }

   /**
    * A {@link TimerTask} that checks to see if the last modified date of the configuration source
    * has changed and, if it has, reloads the <code>SimpleLog</code>'s configuration. For
    * configuration sources that cannot provide a last modified date, the properties in the
    * configuration source are loaded on every iteration and compared to the current properties.
    */
   private final class
   UrlConfigurationReloader
   extends TimerTask
   {
      private long previousLastModified;

      public
      UrlConfigurationReloader()
      {
         try
         {
            URLConnection connection = configurationSource.openConnection();
            previousLastModified = connection.getLastModified();
         }
         catch (IOException e)
         {
            previousLastModified = 0;
         }
      }

      public void
      run()
      {
         long lastModified;
         try
         {
            URLConnection connection = configurationSource.openConnection();
            lastModified = connection.getLastModified();
         }
         catch (IOException e)
         {
            lastModified = 0;
         }

         if (lastModified == 0)
         {
            // Can't get a date, so read the properties and compare
            Properties currentProperties = new Properties();
            try
            {
               InputStream inputStream = configurationSource.openStream();
               currentProperties.load(inputStream);
               if (!currentProperties.equals(properties))
                  reloadProperties();
            }
            catch (IOException e)
            {
               // Fail silently! (My pet hate)
            }
         }
         else if (previousLastModified != lastModified)
         {
            reloadProperties();
            previousLastModified = lastModified;
         }
      }
   }
}
