package org.grlea.log;

// $Id: SimpleLog.java,v 1.22 2006-08-15 10:29:45 grlea Exp $
// Copyright (c) 2004-2006 Graham Lea. All rights reserved.

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

import org.grlea.log.rollover.RolloverManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.Map;

/**
 * <p>Controls the configuration and formatting of a group of <code>SimpleLogger</code>s.</p>
 *
 * <p><code>SimpleLog</code> has a default instance, which is probably the only one that anyone will
 * ever use. If you use the default instance, you don't even really need to know anything about
 * <code>SimpleLog</code> - just use the {@link SimpleLogger#SimpleLogger(Class) basic SimpleLogger
 * constructor} and you'll never even know nor care.</p>
 *
 * @version $Revision: 1.22 $
 * @author $Author: grlea $
 */
public final class
SimpleLog
{
   // TODO (grahaml) Need a way to turn logging OFF for a package/class?

   // Constants
   //...............................................................................................

   /** The name of the system property defining the location of the properties. */
   private static final String CONFIG_PROPERTY = "simplelog.configuration";

   /** The name of the system property for debugging Simple Log itself. */
   private static final String DEV_DEBUG_PROPERTY = "simplelog.dev.debug";

   /** The name of the system property printing Simple Log stack traces. */
   private static final String DEV_STACKTRACES_PROPERTY = "simplelog.dev.printStackTraces";

   /** The prefix used when the configuration is coming from a file. */
   private static final String FILE_CONFIG_PREFIX = "file:";

   /** The prefix used when the configuration is coming from the classpath. */
   private static final String CLASSPATH_CONFIG_PREFIX = "classpath:";

   /** The default name of the properties file used to configure a <code>SimpleLog</code>. */
   private static final String DEFAULT_PROPERTIES_FILE_NAME = "simplelog.properties";

   /** The prefix for all special properties keys. */
   private static final String KEY_PREFIX = "simplelog.";

   /** The prefix for all special properties keys. */
   private static final String KEY_CONSOLE = KEY_PREFIX + "console";

   /** The property key for a list of files to import. */
   private static final String KEY_IMPORT = KEY_PREFIX + "import";

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
   private static final String RELOADING_DEFAULT = "false";

   /** The property key for the log file name. */
   private static final String KEY_LOG_FILE = KEY_PREFIX + "logFile";

   /** The property key for whether the log file name should be interpreted. */
   private static final String KEY_INTERPRET_NAME = KEY_LOG_FILE + ".interpretName";

   /** The default value for the parse log file name property. */
   private static final boolean INTREPRET_NAME_DEFAULT = true;

   /** The property key for whether the log file should be appended. */
   private static final String KEY_APPEND = KEY_LOG_FILE + ".append";

   /** The default value for the append property. */
   private static final boolean APPEND_DEFAULT = true;

   /** The property key for whether output to a log file should also be printed to the console. */
   private static final String KEY_PIPE_TO_CONSOLE = KEY_LOG_FILE + ".andConsole";

   /** The default value for the andConsole property. */
   private static final boolean PIPE_TO_CONSOLE_DEFAULT = false;

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

   /** The property key for the rollover strategy. */
   private static final String KEY_ROLLOVER_STRATEGY = "simplelog.rollover";

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
   private static final String TRACE_SUFFIX = "#trace";

   /**
    * The string to use to print a new line between the debug exception line and the stack trace.
    */
   private static final String LINE_SEP = System.getProperty("line.separator");

   /** The class name of the RolloverManager. */
   private static final String ROLLOVER_WRITER_CLASS = "org.grlea.log.rollover.RolloverManager";

   // Default Message Formats
   //..........................................................................................
   // The following arguments are always the same:
   // {0} - The current date and time (formatted as specified by DATE_FORMAT)
   // {1} - The name of the current Thread.
   // {2} - The name of the debugging class.
   // {3} - The instance ID, if in use.
   // {4} - The DebugLevel name

   private static final String DEFAULT_FORMAT_STRING_DB = "{0}|   |{1}|{2}|{5}";
   private static final String DEFAULT_FORMAT_STRING_DBO = "{0}|---|{1}|{2}|{5}|{6}";
   private static final String DEFAULT_FORMAT_STRING_DBE = "{0}|***|{1}|{2}|{5}";
   private static final String DEFAULT_FORMAT_STRING_ENTRY = "{0}|>>>|{1}|{2}|{5}";
   private static final String DEFAULT_FORMAT_STRING_EXIT = "{0}|<<<|{1}|{2}|{5}";

   private static final String DEFAULT_FORMAT_STRING_DB_INSTANCE = "{0}|   |{1}|{2}[{3}]|{5}";
   private static final String DEFAULT_FORMAT_STRING_DBO_INSTANCE = "{0}|---|{1}|{2}[{3}]|{5}|{6}";
   private static final String DEFAULT_FORMAT_STRING_DBE_INSTANCE = "{0}|***|{1}|{2}[{3}]|{5}";
   private static final String DEFAULT_FORMAT_STRING_ENTRY_INSTANCE = "{0}|>>>|{1}|{2}[{3}]|{5}";
   private static final String DEFAULT_FORMAT_STRING_EXIT_INSTANCE = "{0}|<<<|{1}|{2}[{3}]|{5}";

   private static final int DEFAULT_FORMAT_EXCEPTION_INDEX = 5;
   private static final int DEFAULT_FORMAT_EXCEPTION_INDEX_INSTANCE = 5;

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

   /**
    * Specifies whether Simple Log will output debug information about itself.
    */
   private static boolean devDebug = false;

   static
   {
      try
      {
         String devDebugString = System.getProperty(DEV_DEBUG_PROPERTY);
         if (devDebugString != null && "true".equalsIgnoreCase(devDebugString.trim()))
         {
            devDebug = true;
            printDebugIfEnabled("Simple Log DEV Debugging enabled (-Dsimplelog.dev.debug)");
         }
      }
      catch (Exception e)
      {
         printError("Exception while reading system property '" + DEV_DEBUG_PROPERTY + "'", e, true);
      }
   }

   // Constants
   //...............................................................................................

   /**
    * The URL from where this <code>SimpleLog</code> loaded (and may reload) its configuration.
    */
   private final URL configurationSource;

   /** The properties governing this <code>SimpleLog</code>. */
   private final Properties properties;

   /** The destination of this <code>SimpleLog</code>'s output. */
   private PrintWriter out;

   /** This <code>SimpleLog</code>'s console. */
   private PrintStream console;

   /** The writer that the print writer is printing to. */
   private Writer currentWriter;

   /** Indicates whether the output writer has been set programatically. */
   private boolean outputSetProgramatically = false;

   /**
    * The path and name of the log file being written to, or <code>null</code> if output is going
    * somewhere else.
    */
   private String logFile;

   /** Whether the PrintWriter is printing straight to the console. */
   private boolean printWriterGoesToConsole = true;

   /** Whether output is currently going to the console as well as a log file. */
   private boolean pipingOutputToConsole = false;

   /** The default level of this <code>SimpleLog</code>. */
   private DebugLevel defaultLevel = DebugLevel.L4_INFO;

   /** The default trace flag of this <code>SimpleLog</code>. */
   private boolean defaultTracing = false;

   /**
    * The non-instance {@link SimpleLogger}s attached to this <code>SimpleLog</code>.
    */
   private final List loggers = new ArrayList(32);

   /**
    * A list of {@link WeakReference}s to instance {@link SimpleLogger}s attached to this
    * <code>SimpleLog</code>.
    */
   private final List instanceLoggerReferences = new ArrayList(16);

   /** Queue of references to SimpleLoggers now unreachable. */
   private ReferenceQueue instanceLoggersReferenceQueue = null;

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
      {
         throw new IllegalArgumentException("properties cannot be null.");
      }

      this.configurationSource = null;
      this.properties = properties;
      setConsole(properties);
      this.out = new PrintWriter(console, true);

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
      setConsole(properties);
      this.out = new PrintWriter(console, true);

      loadProperties();
      readSettingsFromProperties();

      // Note: Reloading is only checked on creation
      String reloadingString = properties.getProperty(KEY_RELOADING, RELOADING_DEFAULT);
      boolean reloading = Boolean.valueOf(reloadingString).booleanValue();
      if (reloading)
      {
         printDebugIfEnabled("Configuration reloading enabled");

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
      else
      {
         printDebugIfEnabled("Configuration reloading is disabled");
      }
   }

   private void setConsole(Properties properties)
   {
      String console = properties.getProperty(KEY_CONSOLE);
      if (console != null && console.trim().toLowerCase().equals("system.out"))
         this.console = System.out;
      else
         this.console = System.err;
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
      {
         return;
      }

      printDebugIfEnabled("Loading properties");

      // Load the properties into a new object, then replace the current ones if the read suceeds.
      InputStream inputStream = configurationSource.openStream();
      Properties newProperties = new Properties();
      try
      {
         newProperties.load(inputStream);
      }
      finally
      {
         inputStream.close();
      }

      // Import any properties files as specified
      String importList = newProperties.getProperty(KEY_IMPORT);
      if (importList != null)
      {
         importList = importList.trim();
         if (importList.length() > 0)
         {
            String[] filesToImport = importList.split(",");
            if (filesToImport != null && filesToImport.length != 0)
            {
               String configurationContext = configurationSource.toExternalForm();
               int lastSlash = configurationContext.lastIndexOf('/');
               lastSlash += 1;
               configurationContext = configurationContext.substring(0, lastSlash);
               for (int i = 0; i < filesToImport.length; i++)
               {
                  String filenameToImport = filesToImport[i];
                  URL urlToImport = new URL(configurationContext + filenameToImport);
                  InputStream importStream = null;
                  try
                  {
                     printDebugIfEnabled("Importing file", urlToImport);
                     importStream = urlToImport.openStream();
                     newProperties.load(importStream);
                  }
                  catch (IOException e)
                  {
                     printError("Error importing properties file: " + filenameToImport +
                                "(" + urlToImport + ")", e, true);
                  }
                  finally
                  {
                     if (importStream != null)
                        importStream.close();
                  }
               }
            }
         }
      }

      // List all loaded properties if debug is on
      if (devDebug)
      {
         Set properties = newProperties.entrySet();
         printDebugIfEnabled("_____ Properties List START _____");
         for (Iterator iterator = properties.iterator(); iterator.hasNext();)
         {
            Map.Entry entry = (Map.Entry) iterator.next();
            printDebugIfEnabled((String) entry.getKey(), entry.getValue());
         }
         printDebugIfEnabled("______ Properties List END ______");
      }

      // Replace the current properties with the loaded ones
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
      if (!outputSetProgramatically)
      {
         setConsole(properties);

         try
         {
            String rolloverStrategyString = properties.getProperty(KEY_ROLLOVER_STRATEGY);
            boolean useRollover =
               rolloverStrategyString != null && rolloverStrategyString.trim().length() != 0;

            Writer newWriter;

            if (useRollover)
            {
               newWriter = configureRolloverWriter();
            }
            else
            {
               newWriter = configureFileWriter();
            }

            if (newWriter != currentWriter)
            {
               out = new PrintWriter(newWriter, true);

               if (currentWriter != null)
               {
                  try
                  {
                     currentWriter.close();
                  }
                  catch (IOException e)
                  {
                     printError("Error while closing log file", e, true);
                  }
               }

               currentWriter = newWriter;
            }
         }
         catch (IOException e)
         {
            printError("Error opening log file for writing", e, true);
         }
      }

      // Read the "andConsole" property
      String pipeOutputToConsoleString = properties.getProperty(KEY_PIPE_TO_CONSOLE);
      // The strategy here is to only turn andConsole on if the property definitely says true
      pipingOutputToConsole = PIPE_TO_CONSOLE_DEFAULT;
      if (pipeOutputToConsoleString != null)
      {
         pipingOutputToConsole = pipeOutputToConsoleString.trim().equalsIgnoreCase("true");
      }


      // Read the Default level
      String defaultLevelStr = properties.getProperty(KEY_DEFAULT_LEVEL);
      if (defaultLevelStr != null)
      {
         defaultLevelStr = defaultLevelStr.trim();

         try
         {
            // Try to read it as an int first...
            int level = Integer.parseInt(defaultLevelStr);
            defaultLevel = DebugLevel.fromInt(level);
         }
         catch (NumberFormatException e1)
         {
            // ... then try it as a name.
            try
            {
               defaultLevel = DebugLevel.fromName(defaultLevelStr);
            }
            catch (IllegalArgumentException e2)
            {
               printError("Error parsing debug level for '" + KEY_DEFAULT_LEVEL + "'", e1, true);
               printError("Error parsing debug level for '" + KEY_DEFAULT_LEVEL + "'", e2, false);
            }
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
         dbeFormat.setFormatByArgumentIndex(DEFAULT_FORMAT_EXCEPTION_INDEX, exceptionFormat);
         dbeFormat4Instance.setFormatByArgumentIndex(
            DEFAULT_FORMAT_EXCEPTION_INDEX_INSTANCE, exceptionFormat);
      }

      // Copy any properties that have '$' in their name
      Enumeration propertyNames = properties.propertyNames();
      Properties newProperties = new Properties();
      while (propertyNames.hasMoreElements())
      {
         String key = (String) propertyNames.nextElement();
         if (key.indexOf('$') != -1)
         {
            newProperties.put(key.replace('$', '.'), properties.getProperty(key));
         }
      }
      properties.putAll(newProperties);
   }

   /**
    * Configures this <code>SimpleLog</code> to use a plain FileWriter, according to the properties
    * in the current properties object.
    *
    * @return the writer to be used to write log output.
    */
   private Writer
   configureFileWriter()
   throws IOException
   {
      Writer writer;
      // Read the log file name
      String newLogFile = properties.getProperty(KEY_LOG_FILE);

      boolean interpretName = INTREPRET_NAME_DEFAULT;
      String interpretNameStr = properties.getProperty(KEY_INTERPRET_NAME);
      // The strategy here is to only turn interpret off if the property definitely says false
      if (interpretNameStr != null)
      {
         interpretName = !(interpretNameStr.trim().equalsIgnoreCase("false"));
      }

      // Substitue the date into the name if necessary
      boolean newLogFileNotNull = newLogFile != null;
      boolean nameContainsBraces = newLogFileNotNull && newLogFile.indexOf('{') != -1;
      if (newLogFileNotNull && nameContainsBraces && interpretName)
      {
         try
         {
            MessageFormat logFileNameFormat = new MessageFormat(newLogFile);
            newLogFile = logFileNameFormat.format(new Object[] {new Date()});
         }
         catch (Exception e)
         {
            printError("Error generating log file name", e, true);
            newLogFile = null;
         }
      }

      // The log file has changed if it used to be null and now isn't, or vice versa...
      boolean logFileChanged = (logFile == null) != (newLogFile == null);

      // or it's changed if it wasn't null and still isn't null but the name has changed.
      logFileChanged |= logFile != null && newLogFileNotNull && !newLogFile.equals(logFile);

      // or it's changed if rollover was on before (but it's now off if we're in here)
      boolean rolloverWasInUse =
         currentWriter != null &&
         currentWriter.getClass().getName().equals(ROLLOVER_WRITER_CLASS);

      logFileChanged |= rolloverWasInUse;

      if (logFileChanged)
      {
         if (newLogFile == null)
         {
            writer = new OutputStreamWriter(console);
            printWriterGoesToConsole = true;
         }
         else
         {
            File file = new File(newLogFile).getAbsoluteFile();

            if (file.isDirectory())
            {
               throw new IOException(
                  "The specified log file name already exists as a directory.");
            }

            File parentFile = file.getParentFile();
            if (parentFile != null)
            {
               parentFile.mkdirs();
            }

            boolean append = APPEND_DEFAULT;
            String appendStr = properties.getProperty(KEY_APPEND);
            // The strategy here is to only turn append off if the property definitely says
            // false
            if (appendStr != null)
            {
               append = !(appendStr.trim().equalsIgnoreCase("false"));
            }

            writer = new FileWriter(file, append);

            printWriterGoesToConsole = false;
         }

         logFile = newLogFile;
      }
      else
      {
         writer = currentWriter;
      }

      return writer;
   }

   /**
    * Configures this <code>SimpleLog</code> to use a {@link RolloverManager}, according to the
    * properties in the current properties object.
    *
    * @return the writer to be used to write log output.
    */
   private Writer
   configureRolloverWriter()
   throws IOException
   {
      // Test we've got the RolloverManager class, so we can throw something really meaningful.
      try
      {
         Class.forName(ROLLOVER_WRITER_CLASS);
      }
      catch (ClassNotFoundException e)
      {
         throw new IOException("The RolloverManager class is not available: " + e);
      }
      catch (Throwable t)
      {
         // Ignore anything else that might be thrown (e.g. SecurityException).
      }

      Writer writer;
      if (currentWriter != null && currentWriter.getClass().getName().equals(ROLLOVER_WRITER_CLASS))
      {
         ((RolloverManager) currentWriter).configure(properties);
         writer = currentWriter;
      }
      else
      {
         writer = RolloverManager.createRolloverManager(properties, ErrorReporter.create());
      }

      printWriterGoesToConsole = false;

      return writer;
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
         {
            loadProperties();
         }

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
            printDebugIfEnabled("Creating default SimpleLog instance");

            URL propertiesUrl = getPropertiesUrl();

            if (propertiesUrl != null)
            {
               printDebugIfEnabled("Attempting to configure using properties at", propertiesUrl);
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
               printDebugIfEnabled("");
               printDebugIfEnabled("FAILED to load any SimpleLog configuration.");
               printDebugIfEnabled("");
               printDebugIfEnabled("NO LOG OUTPUT WILL BE GENERATED.");
               defaultInstance = new SimpleLog(new Properties());
               defaultInstance.setWriter(null);
            }
         }
      }
      return defaultInstance;
   }

   private static URL
   getPropertiesUrl()
   {
      // Read the system property
      String propertiesDefinition = null;
      try
      {
         propertiesDefinition = System.getProperty(CONFIG_PROPERTY);
      }
      catch (SecurityException e)
      {
         printError("SecurityException while trying to read system property", e, true);
      }
      printDebugIfEnabled("System property '" + CONFIG_PROPERTY + "'", propertiesDefinition);

      URL propertiesUrl = null;
      if (propertiesDefinition != null)
      {
         // File
         if (propertiesDefinition.startsWith(FILE_CONFIG_PREFIX))
         {
            String propertiesLocation =
               propertiesDefinition.substring(FILE_CONFIG_PREFIX.length());

            File propertiesFile = new File(propertiesLocation);

            if (propertiesFile.exists())
            {
               try
               {
                  propertiesUrl = propertiesFile.toURL();
               }
               catch (MalformedURLException e)
               {
                  printError("Error creating URL from filename '" + propertiesLocation + "'", e,
                             false);
               }
            }
            else
            {
               printError("Properties file not found at '" + propertiesLocation + "'");
            }
         }
         // Classpath
         else if (propertiesDefinition.startsWith(CLASSPATH_CONFIG_PREFIX))
         {
            String propertiesLocation =
               propertiesDefinition.substring(CLASSPATH_CONFIG_PREFIX.length());

            propertiesUrl = SimpleLog.class.getClassLoader().getResource(propertiesLocation);
            if (propertiesUrl == null)
               printError("Properties not found in classpath at '" + propertiesLocation + "'");
         }
         // Junk
         else
         {
            printError(CONFIG_PROPERTY + " property must begin with '" + FILE_CONFIG_PREFIX +
                       "' or '" + CLASSPATH_CONFIG_PREFIX + "' ('" + propertiesDefinition + "')");
         }
      }

      // Default: simplelog.properties in the root of the classpath
      if (propertiesUrl == null)
      {
         printDebugIfEnabled(
            "Attempting to load default properties (simplelog.properties) from classpath");
         propertiesUrl = SimpleLog.class.getClassLoader().getResource(DEFAULT_PROPERTIES_FILE_NAME);
      }

      // Encode any spaces in the URL
      if (propertiesUrl != null && propertiesUrl.toExternalForm().indexOf(' ') != -1)
      {
         try
         {
            propertiesUrl = new URL(propertiesUrl.toString().replaceAll(" ", "%20"));
         }
         catch (MalformedURLException e)
         {
            printError("Failed to encode spaces in properties URL", e, true);
         }
      }

      return propertiesUrl;
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
      {
         out.println(s);
         if (!printWriterGoesToConsole && pipingOutputToConsole)
         {
            console.println(s);
         }
      }
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
      logger.setDebugLevel(getDebugLevel(logger));
      logger.setTracing(getTracingFlag(logger));
   }

   /**
    * Retrieves the debug level for the given class from the properties.
    *
    * @param logger the logger for which to retrieve the debug level
    *
    * @return the debug level to be used for the class.
    */
   private DebugLevel
   getDebugLevel(SimpleLogger logger)
   {
      if (properties == null)
      {
         return defaultLevel;
      }

      String loggerConfigName = logger.getConfigName();

      int dotIndex = loggerConfigName.length();
      DebugLevel debugLevel = null;
      do
      {
         // On first iteration, this substring() call returns the whole string.
         // On subsequent iterations, it removes everything after and including the last period.
         loggerConfigName = loggerConfigName.substring(0, dotIndex);
         String value = properties.getProperty(loggerConfigName);
         if (value != null)
         {
            value = value.trim();

            try
            {
               // Try to read it as an int first...
               int level = Integer.parseInt(value);
               debugLevel = DebugLevel.fromInt(level);
            }
            catch (NumberFormatException e1)
            {
               // ... then try it as a loggerConfigName.
               try
               {
                  debugLevel = DebugLevel.fromName(value);
               }
               catch (IllegalArgumentException e2)
               {
                  printError("Error parsing debug level for '" + loggerConfigName + "'", e1, true);
                  printError("Error parsing debug level for '" + loggerConfigName + "'", e2, false);
               }
            }
         }

         dotIndex = loggerConfigName.lastIndexOf('.');
      }
      while (debugLevel == null && dotIndex != -1);

      // If we found no level, use the default.
      if (debugLevel == null)
      {
         debugLevel = defaultLevel;
      }

      return debugLevel;
   }

   /**
    * Retrieves the tracing flag for the given class from the properties.
    *
    * @param logger the logger for which to retrieve the debug level
    *
    * @return the tracing flag to be used for the class.
    */
   private boolean
   getTracingFlag(SimpleLogger logger)
   {
      if (properties == null)
      {
         return defaultTracing;
      }

      String loggerConfigName = logger.getConfigName();

      int dotIndex = loggerConfigName.length();
      boolean trace = defaultTracing;
      do
      {
         loggerConfigName = loggerConfigName.substring(0, dotIndex);
         String value = properties.getProperty(loggerConfigName + TRACE_SUFFIX);
         if (value != null)
         {
            trace = Boolean.valueOf(value).booleanValue();
            break;
         }

         dotIndex = loggerConfigName.lastIndexOf('.');
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
         if (!logger.isInstanceDebugger())
         {
            loggers.add(logger);
         }
         else
         {
            // Instance loggers get weak referenced, because they're much more likely to be GC'd.
            if (instanceLoggersReferenceQueue == null)
            {
               createInstanceLoggersReferenceQueue();
            }
            instanceLoggerReferences.add(new WeakReference(logger, instanceLoggersReferenceQueue));
         }
      }
      configure(logger);
   }

   /**
    * Creates the {@link ReferenceQueue} for instance loggers and starts a daemon thread to clear
    * queue.
    */
   private void
   createInstanceLoggersReferenceQueue()
   {
      instanceLoggersReferenceQueue = new ReferenceQueue();
      Thread thread = new Thread(new Runnable()
      {
         public void
         run()
         {
            while (true)
            {
               try
               {
                  Thread.yield();
                  Reference reference = instanceLoggersReferenceQueue.remove();
                  synchronized (LOGGERS_LOCK)
                  {
                     instanceLoggerReferences.remove(reference);
                  }
               }
               catch (Throwable t)
               {
                  // Ignore - who cares?
               }
            }
         }
      }, "SimpleLog Instance Logger Cleaner");
      thread.setDaemon(true);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
   }

   /**
    * Re-configures all {@link SimpleLogger}s registered with this <code>SimpleLog</code>.
    */
   private void
   reconfigureAllLoggers()
   {
      synchronized (LOGGERS_LOCK)
      {
         printDebugIfEnabled("Re-configuring all loggers");

         for (Iterator iter = loggers.iterator(); iter.hasNext();)
         {
            configure((SimpleLogger) iter.next());
         }

         for (Iterator iter = instanceLoggerReferences.iterator(); iter.hasNext();)
         {
            Reference loggerReference = (Reference) iter.next();
            SimpleLogger logger = (SimpleLogger) loggerReference.get();
            if (logger != null)
               configure(logger);
         }
      }
   }

   /**
    * Prints the given debug message to System.err if Simple Log dev debugging is enabled.
    *
    * @param message a message to print
    */
   private static void
   printDebugIfEnabled(String message)
   {
      if (devDebug)
         System.err.println("SimpleLog [dev.debug]: " + message);
   }

   /**
    * Prints the given debug message and object value to System.err if Simple Log dev debugging is
    * enabled.
    *
    * @param message a message to print
    *
    * @param value an object to print
    */
   private static void
   printDebugIfEnabled(String message, Object value)
   {
      if (devDebug)
         System.err.println("SimpleLog [dev.debug]: " + message + ": " + value);
   }

   /**
    * Prints an error message from this <code>SimpleLog</code>.
    *
    * @param description a description the error
    */
   private static void
   printError(String description)
   {
      printError(description, null, false);
   }

   /**
    * Prints an error message from this <code>SimpleLog</code>.
    *
    * @param description a description the error
    * @param error the exception that occurred to cause the error (may be <code>null</code>)
    * @param printExceptionType whether the whole toString of the exception should be printed (true)
    * of just the exception's 'message' (false).
    */
   private static void
   printError(String description, Throwable error, boolean printExceptionType)
   {
      boolean printStackTraces = false;
      try
      {
         String printStackTracesStr = System.getProperty(DEV_STACKTRACES_PROPERTY);
         printStackTraces = printStackTracesStr != null &&
                            printStackTracesStr.trim().equalsIgnoreCase("true");
      }
      catch (SecurityException e)
      {
         // Ignore SecurityExceptions from trying to read system properties
      }

      synchronized (System.err)
      {
         System.err.println();

         System.err.print("   SimpleLog ERROR: ");
         System.err.print(description);

         if (error != null)
         {
            System.err.print(": ");
            if (printExceptionType)
            {
               System.err.println(error);
            }
            else
            {
               System.err.println(error.getMessage());
            }

            if (printStackTraces)
            {
               try
               {
                  System.err.println();
                  error.printStackTrace(System.err);
               }
               catch (SecurityException e)
               {
                  // Ignore SecurityExceptions from trying to print stack traces
               }
            }

            System.err.println();
         }
         else
         {
            System.err.println();
            System.err.println();
         }
      }
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
      {
         throw new IllegalArgumentException("defaultLevel cannot be null.");
      }
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
      {
         dateFormat = new SimpleDateFormat(DATE_FORMAT_DEFAULT);
      }
      else
      {
         dateFormat = newDateFormat;
      }

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
      this.outputSetProgramatically = true;
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
    * Returns whether this <code>SimpleLog</code>, when printing output to a file, will also print
    * the output to the console. This attribute has no effect when output is not going to a file.
    *
    * @return <code>true</code> if output will be piped to the console, <code>false</code> if it
    * won't.
    */
   public boolean
   isPipingOutputToConsole()
   {
      return pipingOutputToConsole;
   }

   /**
    * Sets whether this <code>SimpleLog</code>, when printing output to a file, should also print
    * the output to the console. This attribute has no effect when output is not going to a file.
    *
    * @param pipeOutputToConsole <code>true</code> if output should be piped to the console as well
    * as the output file, <code>false</code> if it should not.
    */
   public void
   setPipingOutputToConsole(boolean pipeOutputToConsole)
   {
      this.pipingOutputToConsole = pipeOutputToConsole;
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
         {
            throw new IllegalArgumentException(getClass().getName() + " only formats Throwables.");
         }

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
         try
         {
            URI uri = new URI(configurationSource.toExternalForm());
            this.configurationFile = new File(uri);
            this.previousLastModified = configurationFile.lastModified();
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Failed to create URI from URL due to " + e);
         }
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
               {
                  reloadProperties();
               }
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

   private static class
   ErrorReporter
   implements RolloverManager.ErrorReporter
   {
      public void
      error(String description, Throwable t, boolean printExceptionType)
      {
         printError(description, t, printExceptionType);
      }

      private static RolloverManager.ErrorReporter
      create()
      {
         return new ErrorReporter();
      }
   }
}
