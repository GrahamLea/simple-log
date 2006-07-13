package org.grlea.log;

// $Id: SimpleLogger.java,v 1.12 2006-07-13 12:40:06 grlea Exp $
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

import java.text.MessageFormat;
import java.util.Date;

/**
 * <p>Used to create log messages for a single class or instance of a class.</p>
 *
 * <p><code>SimpleLogger</code> is where it all happens, from a client's perspective.<br>
 * The easiest way to use Simple Log is to create one <code>SimpleLogger</code> for each class and
 * then log to it!</p>
 *
 * <p>1. Create a logger like this:<pre>
 *    private static final SimpleLogger log = new SimpleLogger(HelloWorld.class);
 * </pre></p>
 *
 * <p>2. And then use it like this:<pre>
 *       log.entry("main()");
 *       log.debugObject("argv", argv);
 *       if (argv.length == 0)
 *       {
 *          log.info("No arguments. Using defaults.");
 *          ...
 *       }
 *       ...
 *       log.exit("main()");
 * </pre></p>
 *
 * <p>
 * <code>SimpleLogger</code> provides for four types of log messages:
 * <ul>
 *    <li>
 *       "db" = Debug (see {@link #db SimpleLogger.db()})<br>
 *       Lets you log a simple log message, e.g. "Got to the point where you thought it wasn't
 *       getting to."
 *    </li><br><br>
 *    <li>
 *       "dbo" = Debug Object
 *       (see {@link #dbo(DebugLevel,String,Object) SimpleLogger.dbo()})
 *       <br>
 *       Debugs the name and value of an object. Specially handled variants exist for all primitives,
 *       Object[], byte[] and char[].
 *    </li><br><br>
 *    <li>
 *       "dbe" = Debug Exception (see {@link #dbe SimpleLogger.dbe()})<br>
 *       Special handling of exceptions that will print a stack trace (can be turned off).
 *    </li><br><br>
 *    <li>
 *       Tracing (see {@link #entry SimpleLogger.entry()} and {@link #exit SimpleLogger.exit()})<br>
 *       Logs entry to and exit from a method. Can be turned on/off independent of debug level.<br>
 *    </li>
 * </ul>
 *
 * as well as convenience methods, named after the various levels, as shortcuts to the above methods.
 * </p>
 *
 * <p>
 * Instance-based <code>SimpleLogger</code>s can be used to determine, from the log output,
 * exactly which object a log statement is coming from. This is useful when many objects of the same
 * class are all performing logging and it is crucial to analysis to know which object logged any
 * given message.<br><br>
 *
 * Here is an example of how an instance-based logger would be used:<pre>
 * public class
 * Test
 * {
 *    private final SimpleLogger log;
 *
 *    private final String id;
 *
 *    public
 *    Test(String id)
 *    {
 *       log = new SimpleLogger(Test.class, id);
 *       this.id = id;
 *    }
 * }</pre>
 * Note the following important features of this pattern and instance-based logs in general:
 * <ul>
 * <li>The <code>log</code> field is <i>not</i> static. There is one per instance.</li>
 * <li><code>&lt;ClassName&gt;.class</code> is used rather than <code>getClass()</code>. Otherwise,
 * if this class were subclassed, logging statements would appear with the subclass's name as the
 * source.</li>
 * <li>There are separate log formats for instance-based logs in the properties file.</li>
 * </ul>
 * </p>
 *
 * @version $Revision: 1.12 $
 * @author $Author: grlea $
 */
public class
SimpleLogger
{
   /**
    * The characters to use to represent the values of a byte.
    */
   private static final char[] BYTE_CHARS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   /**
    * The log that this logger logs to.
    */
   private final SimpleLog log;

   /**
    * The class that this SimpleLogger instance belongs to.
    */
   private final Class sourceClass;

   /**
    * The fully-qualified name of the class that this SimpleLogger instance belongs to.
    */
   private final String className;

   /**
    * The short name (ie. no package) of the class that this SimpleLogger instance belongs to.
    */
   private final String classNameShort;

   /**
    * The instance identifier for this <code>SimpleLogger</code>, or <code>null</code> if this
    * instance is for a class, not an instance.
    */
   private Object instanceId;

   /**
    * A boolean indicating whether this SimpleLogger object contains an instanceId.
    */
   private final boolean isInstanceDebugger;

   /**
    * A boolean indicating whether trace/debug messages through this SimpleLogger object should
    * have the fully-qualified (long) name of the class printed.
    */
   private final boolean useLongName;

   /**
    * <p>
    * The name used to specify and look up the configuration of this <code>SimpleLogger</code>.
    * </p>
    *
    * <p>
    * The config name is the fully-qualified name of the source class of the logger, plus, if the
    * logger is an instance logger, a period followed by the string representation
    * ({@link Object#toString() toString()}) of the logger's instance ID. Additionally, any dollar
    * signs appearing in the name are changed to periods.
    * </p>
    */
   private String configName;

   /**
    * The current debug level for this <code>SimpleLogger</code>.
    */
   private DebugLevel debugLevel;

   /**
    * Whether this logger is currently reporting tracing or not.
    */
   private boolean tracing;

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class using the default
    * <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class) new SimpleLogger(SimpleLog.defaultInstance(), sourceClass)}
    * </p>
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    */
   public
   SimpleLogger(Class sourceClass)
   {
      this(SimpleLog.defaultInstance(), sourceClass);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class using the default
    * <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,Object)
    * new SimpleLogger(SimpleLog.defaultInstance(), sourceClass, instanceId)}
    * </p>
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param instanceId An object uniquely identifying the instance of <code><i>sourceClass</i></code>
    * that this <code>SimpleLogger</code> is specifically for. Can be <code>null</code> if this
    * <code>SimpleLogger</code> instance is for all instances of <code><i>sourceClass</i></code>
    * rather than for a particular instance (this is the norm).
    */
   public
   SimpleLogger(Class sourceClass, Object instanceId)
   {
      this(SimpleLog.defaultInstance(), sourceClass, instanceId);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class using the default
    * <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,boolean)
    * new SimpleLogger(SimpleLog.defaultInstance(), sourceClass, useLongName)}
    * </p>
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param useLongName A boolean indicating whether the fully-qualified name of the specified
    * class should be printed in each message (when set to <code>true</code>), or just the name of
    * the class within it's package (ie. the package will not be printed when set to
    * <code>false</code>).
    */
   public
   SimpleLogger(Class sourceClass, boolean useLongName)
   {
      this(SimpleLog.defaultInstance(), sourceClass, useLongName);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class using the default
    * <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,Object,boolean)
    * new SimpleLogger(SimpleLog.defaultInstance(), sourceClass, instanceId, useLongName)}
    * </p>
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param instanceId An object uniquely identifying the instance of <code><i>sourceClass</i></code>
    * that this <code>SimpleLogger</code> is specifically for. Can be <code>null</code> if this
    * <code>SimpleLogger</code> instance is for all instances of <code><i>sourceClass</i></code>
    * rather than for a particular instance (this is the norm).
    *
    * @param useLongName A boolean indicating whether the fully-qualified name of the specified
    * class should be printed in each message (when set to <code>true</code>), or just the name of
    * the class within it's package (ie. the package will not be printed when set to
    * <code>false</code>).
    */
   public
   SimpleLogger(Class sourceClass, Object instanceId, boolean useLongName)
   {
      this(SimpleLog.defaultInstance(), sourceClass, instanceId, useLongName);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class that will log to, and be
    * configured by, the provided <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,boolean)
    * new SimpleLogger(log, sourceClass, false)}
    * </p>
    *
    * @param log the <code>SimpleLog</code> instance that this <code>SimpleLogger</code> should log
    * to and be configured by.
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    */
   public
   SimpleLogger(SimpleLog log, Class sourceClass)
   {
      this(log, sourceClass, false);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class that will log to, and be
    * configured by, the provided <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,Object,boolean)
    * new SimpleLogger(SimpleLog.defaultInstance(), sourceClass, instanceId, false)}
    * </p>
    *
    * @param log the <code>SimpleLog</code> instance that this <code>SimpleLogger</code> should log
    * to and be configured by.
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param instanceId An object uniquely identifying the instance of <code><i>sourceClass</i></code>
    * that this <code>SimpleLogger</code> is specifically for. Can be <code>null</code> if this
    * <code>SimpleLogger</code> instance is for all instances of <code><i>sourceClass</i></code>
    * rather than for a particular instance (this is the norm).
    */
   public
   SimpleLogger(SimpleLog log, Class sourceClass, Object instanceId)
   {
      this(log, sourceClass, instanceId, false);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class that will log to, and be
    * configured by, the provided <code>SimpleLog</code>.</p>
    *
    * <p>This constructor is the equivalent of calling:<br>
    * {@link #SimpleLogger(SimpleLog,Class,Object,boolean)
    * new SimpleLogger(SimpleLog.defaultInstance(), sourceClass, null, useLongName)}
    * </p>
    *
    * @param log the <code>SimpleLog</code> instance that this <code>SimpleLogger</code> should log
    * to and be configured by.
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param useLongName A boolean indicating whether the fully-qualified name of the specified
    * class should be printed in each message (when set to <code>true</code>), or just the name of
    * the class within it's package (ie. the package will not be printed when set to
    * <code>false</code>).
    */
   public
   SimpleLogger(SimpleLog log, Class sourceClass, boolean useLongName)
   {
      this(log, sourceClass, null, useLongName);
   }

   /**
    * <p>Creates a new <code>SimpleLogger</code> for the specified Class that will log to, and be
    * configured by, the provided <code>SimpleLog</code>.</p>
    *
    * @param log the <code>SimpleLog</code> instance that this <code>SimpleLogger</code> should log
    * to and be configured by.
    *
    * @param sourceClass The class that this SimpleLogger object is for, the name of which will be
    * printed out as part of every message.
    *
    * @param instanceId An object uniquely identifying the instance of <code><i>sourceClass</i></code>
    * that this <code>SimpleLogger</code> is specifically for. Can be <code>null</code> if this
    * <code>SimpleLogger</code> instance is for all instances of <code><i>sourceClass</i></code>
    * rather than for a particular instance (this is the norm).
    *
    * @param useLongName A boolean indicating whether the fully-qualified name of the specified
    * class should be printed in each message (when set to <code>true</code>), or just the name of
    * the class within it's package (ie. the package will not be printed when set to
    * <code>false</code>).
    */
   public
   SimpleLogger(SimpleLog log, Class sourceClass, Object instanceId, boolean useLongName)
   {
      this.log = log;
      this.sourceClass = sourceClass;
      this.instanceId = instanceId;
      this.isInstanceDebugger = instanceId != null;
      this.useLongName = useLongName;
      this.className = sourceClass.getName();
      this.classNameShort = createClassNameShort(sourceClass);

      generateConfigName();

      log.register(this);
   }

   /**
    * Creates and sets this <code>SimpleLogger</code>'s {@link #configName}.
    */
   private void
   generateConfigName()
   {
      String loggerName = sourceClass.getName();

      if (isInstanceDebugger)
      {
         loggerName = loggerName + '.' + instanceId;
      }

      if (loggerName.indexOf('$') != -1)
      {
         loggerName = loggerName.replace('$', '.');
      }

      this.configName = loggerName;
   }

   /**
    * Determines and returns the short name (i.e. without the package) of the given class.
    *
    * @param sourceClass the class to retrieve the short name of
    *
    * @return the class's short name.
    */
   private String
   createClassNameShort(Class sourceClass)
   {
      if (className.indexOf('.') == -1)
      {
         return className;
      }

      if (sourceClass.getPackage() != null && sourceClass.getPackage().getName() != null)
      {
         return className.substring(sourceClass.getPackage().getName().length() + 1);
      }

      // We have to do this trickery to make sure inner classes have their whole name (I think?)
      String packageName = className;
      boolean packageNameIsAClass = true;
      int lastPeriod = packageName.indexOf('.');
      while (packageNameIsAClass && lastPeriod != -1)
      {
         packageName = packageName.substring(0, lastPeriod);
         try
         {
            Class.forName(packageName);
            packageNameIsAClass = true;
         }
         catch (Throwable e)
         {
            packageNameIsAClass = false;
         }
         lastPeriod = packageName.lastIndexOf('.');
      }

      if (packageName.length() == 0)
      {
         return className;
      }
      else
      {
         return className.substring(packageName.length() + 1);
      }
   }

   /**
    * Returns <code>true</code> if this <code>SimpleLogger</code> is a logger for one instance of
    * the source class rather than for the class as a whole.
    */
   public boolean
   isInstanceDebugger()
   {
      return isInstanceDebugger;
   }

   /**
    * Returns the {@link #instanceId} of this <code>SimpleLogger</code>, or <code>null</code> if it
    * does not have one (i.e. it is a class logger, not an instance logger).
    */
   public Object
   getInstanceID()
   {
      return instanceId;
   }

   /**
    * Sets the {@link #instanceId} of this <code>SimpleLogger</code> instance.
    *
    * @throws IllegalArgumentException if <code>instanceId</code> is <code>null</code>
    *
    * @throws IllegalStateException if this <code>SimpleLogger</code> is for a class, not an
    * instance. (See the javadoc for the class for more information.)
    */
   public void
   setInstanceID(Object instanceId)
   {
      if (!isInstanceDebugger)
      {
         throw new IllegalStateException("This is not an instance debugger.");
      }
      if (instanceId == null)
      {
         throw new IllegalArgumentException("instanceId cannot be null.");
      }
      this.instanceId = instanceId;

      generateConfigName();
   }

   /**
    * <p>
    * Returns this <code>SimpleLogger</code>'s config name, the name used to specify and look up
    * its configuration.
    * </p>
    *
    * <p>
    * Currently, the config name is the fully-qualified name of the source class of the logger,
    * plus, if the logger is an instance logger, a period ('.') followed by the string
    * representation ({@link Object#toString() toString()}) of the logger's instance ID.
    * Additionally, any dollar signs appearing in the name are changed to periods.
    * </p>
    */
   public String
   getConfigName()
   {
      return configName;
   }

   /**
    * Returns the current debug level of this <code>SimpleLogger</code>.
    */
   public DebugLevel
   getDebugLevel()
   {
      return debugLevel;
   }

   /**
    * A convenience method for testing whether a message logged at the specified level would be
    * logged by this <code>SimpleLogger</code>.
    *
    * @param level the level of the hypothetical message being logged
    *
    * @return <code>true</code> if a message at the specified level would be logged given this
    * <code>SimpleLogger</code>'s current log level, <code>false</code> if it would not.
    *
    * @see #getDebugLevel
    * @see DebugLevel#shouldLog
    */
   public boolean
   wouldLog(DebugLevel level)
   {
      return log.isOutputting() && getDebugLevel().shouldLog(level);
   }

   /**
    * Sets the current debug level of this <code>SimpleLogger</code>.
    *
    * @param debugLevel the new debug level
    *
    * @throws IllegalArgumentException if <code>debugLevel</code> is <code>null</code>.
    */
   public void
   setDebugLevel(DebugLevel debugLevel)
   {
      if (debugLevel == null)
      {
         throw new IllegalArgumentException("debugLevel cannot be null.");
      }
      this.debugLevel = debugLevel;
   }

   /**
    * Returns whether this <code>SimpleLogger</code> is currently reporting tracing or not.
    *
    * @return <code>true</code> if this logger is reporting tracing, false otherwise.
    */
   public boolean
   isTracing()
   {
      return tracing;
   }

   /**
    * Sets this <code>SimpleLogger</code>'s tracing flag.
    *
    * @param tracing <code>true</code> if this logger should report tracing, false if it should not.
    */
   public void
   setTracing(boolean tracing)
   {
      this.tracing = tracing;
   }

   /**
    * Returns the source class of this <code>SimpleLogger</code>.
    */
   public Class
   getSourceClass()
   {
      return sourceClass;
   }

   /**
    * Creates an array of data containing the standard arguments for a log message, plus empty
    * array slots for the specified number of arguments.
    *
    * @param level the level at which this data is going to be logged.
    *
    * @param extraArguments the number of empty slots to create at the end of the array.
    *
    * @return the newly created array of objects.
    */
   private Object[]
   createData(DebugLevel level, int extraArguments)
   {
      Object[] result = new Object[5 + extraArguments];
      result[0] = new Date();
      result[1] = Thread.currentThread().getName();
      result[2] = useLongName ? className : classNameShort;
      result[3] = instanceId;
      result[4] = level;
      return result;
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L1_FATAL Fatal} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   fatal(String message)
   {
      db(DebugLevel.L1_FATAL, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L2_ERROR Error} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   error(String message)
   {
      db(DebugLevel.L2_ERROR, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L3_WARN Warn} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   warn(String message)
   {
      db(DebugLevel.L3_WARN, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L4_INFO Info} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   info(String message)
   {
      db(DebugLevel.L4_INFO, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L5_DEBUG Debug} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   debug(String message)
   {
      db(DebugLevel.L5_DEBUG, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L6_VERBOSE Verbose} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   verbose(String message)
   {
      db(DebugLevel.L6_VERBOSE, message);
   }

   /**
    * Logs a debug message at the {@link DebugLevel#L7_LUDICROUS Ludicrous} level.
    *
    * @param message the message to log.
    *
    * @see #db
    */
   public void
   ludicrous(String message)
   {
      db(DebugLevel.L7_LUDICROUS, message);
   }

   /**
    * <p>Logs a simple debug message.</p>
    *
    * <p>The message will be printed if the given debug level is less than or equal to the current
    * debug level of this <code>SimpleLogger</code>.</p>
    *
    * @param message The debug message to print.
    *
    * @see #fatal
    * @see #error
    * @see #warn
    * @see #info
    * @see #debug
    * @see #verbose
    */
   public void
   db(DebugLevel level, String message)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      Object[] data = createData(level, 1);
      data[data.length - 1] = message;
      MessageFormat format =
         isInstanceDebugger ? log.getDebugInstanceFormat() : log.getDebugFormat();
      log.println(format.format(data));
   }

   /**
    * <p>Logs a message containing an object's name and value.</p>
    *
    * <p>The object name and value will be printed only if the given debug level is less than or
    * equal to the current debug level of this <code>SimpleLogger</code>.</p>
    *
    * <p>Note that, if the given object is an instance of <code>Object[]</code>, <code>byte[]</code>
    * or <code>char[]</code>, this method will route the call to the corresponding variation of
    * <code>dbo()</code> (each of which performs special formatting for the particualr type),
    * eliminating the need to perform pre-logging type checks and casts.</p>
    *
    * <p>Note that passing a {@link Throwable} to this method does not behave in the same way as
    * {@link #dbe}. I.e. passing an exception in as the object value will not result in a stack
    * trace being printed.</p>
    *
    * @param objectName The name of the object whose value is being given.
    *
    * @param value The value of the object.
    */
   public void
   dbo(DebugLevel level, String objectName, Object value)
   {
      if (wouldLog(level))
      {
         if (value instanceof Object[])
         {
            dbo(level, objectName, (Object[]) value);
         }
         else if (value instanceof byte[])
         {
            dbo(level, objectName, (byte[]) value);
         }
         else if (value instanceof char[])
         {
            dbo(level, objectName, (char[]) value);
         }
         else
         {
            dboNoCheck(level, objectName, value);
         }
      }
   }

   /**
    * The same as {@link #dbo(DebugLevel,String,Object)} but without the debug level check.
    * This method is used by all the primitive- and array-typed variants of <code>dbo()</code>,
    * which do their own checks before calling this method.
    */
   private void
   dboNoCheck(DebugLevel level, String objectName, Object val)
   {
      Object[] data = createData(level, 2);
      data[data.length - 2] = objectName;
      data[data.length - 1] = val;
      MessageFormat format = isInstanceDebugger ? log.getDebugObjectInstanceFormat() :
                                                           log.getDebugObjectFormat();
      log.println(format.format(data));
   }

   /**
    * <p>Logs a message containing an object array's name and value.</p>
    *
    * <p>The array name and value will be printed only if the given debug level is less than or
    * equal to the current debug level of this <code>SimpleLogger</code>.</p>
    *
    * @param objectName The name of the array whose value is being given.
    *
    * @param val The array.
    */
   public void
   dbo(DebugLevel level, String objectName, Object[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      StringBuffer stringValue = new StringBuffer("[");
      for (int i = 0; i < val.length; i++)
      {
         if (i == 0)
         {
            stringValue.append(val[i]);
         }
         else
         {
            stringValue.append(", ").append(val[i]);
         }
      }
      stringValue.append("]");

      dboNoCheck(level, objectName, stringValue);
   }

   /**
    * <p>Logs a message containing a <code>short</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, short val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing an <code>int</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, int val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>long</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, long val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>boolean</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, boolean val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>float</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, float val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>double</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, double val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>byte</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, byte val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, toString(val));
   }

   /**
    * <p>Logs a message containing a <code>byte</code> array's name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, byte[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, toString(val));
   }

   /**
    * <p>Logs a message containing a <code>char</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, char val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>char</code> array's name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objectName, char[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      dboNoCheck(level, objectName, String.valueOf(val));
   }

   /**
    * Convenience method for logging an object's name and value at the {@link DebugLevel#L4_INFO
    * Info} level.
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   infoObject(String objectName, Object val)
   {
      dbo(DebugLevel.L4_INFO, objectName, val);
   }

   /**
    * Convenience method for logging a <code>boolean</code>'s name and value at the {@link
    * DebugLevel#L4_INFO Info} level.
    *
    * @see #dbo(DebugLevel,String,boolean)
    */
   public void
   infoObject(String objectName, boolean val)
   {
      dbo(DebugLevel.L4_INFO, objectName, val);
   }

   /**
    * Convenience method for logging an <code>int</code>'s name and value at the
    * {@link DebugLevel#L4_INFO Info} level.
    *
    * @see #dbo(DebugLevel,String,int)
    */
   public void
   infoObject(String objectName, int val)
   {
      dbo(DebugLevel.L4_INFO, objectName, val);
   }

   /**
    * Convenience method for logging an object's name and value at the
    * {@link DebugLevel#L5_DEBUG Debug} level.
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   debugObject(String objectName, Object val)
   {
      dbo(DebugLevel.L5_DEBUG, objectName, val);
   }

   /**
    * Convenience method for logging a <code>boolean</code>'s name and value at the
    * {@link DebugLevel#L5_DEBUG Debug} level.
    *
    * @see #dbo(DebugLevel,String,boolean)
    */
   public void
   debugObject(String objectName, boolean val)
   {
      dbo(DebugLevel.L5_DEBUG, objectName, val);
   }

   /**
    * Convenience method for logging an <code>int</code>'s name and value at the
    * {@link DebugLevel#L5_DEBUG Debug} level.
    *
    * @see #dbo(DebugLevel,String,int)
    */
   public void
   debugObject(String objectName, int val)
   {
      dbo(DebugLevel.L5_DEBUG, objectName, val);
   }

   /**
    * Convenience method for logging an object's name and value at the
    * {@link DebugLevel#L6_VERBOSE Verbose} level.
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   verboseObject(String objectName, Object val)
   {
      dbo(DebugLevel.L6_VERBOSE, objectName, val);
   }

   /**
    * Convenience method for logging a <code>boolean</code>'s name and value at the
    * {@link DebugLevel#L6_VERBOSE Verbose} level.
    *
    * @see #dbo(DebugLevel,String,boolean)
    */
   public void
   verboseObject(String objectName, boolean val)
   {
      dbo(DebugLevel.L6_VERBOSE, objectName, val);
   }

   /**
    * Convenience method for logging an <code>int</code>'s name and value at the
    * {@link DebugLevel#L6_VERBOSE Verbose} level.
    *
    * @see #dbo(DebugLevel,String,int)
    */
   public void
   verboseObject(String objectName, int val)
   {
      dbo(DebugLevel.L6_VERBOSE, objectName, val);
   }

   /**
    * Convenience method for logging an object's name and value at the
    * {@link DebugLevel#L7_LUDICROUS Ludicrous} level.
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   ludicrousObject(String objectName, Object val)
   {
      dbo(DebugLevel.L7_LUDICROUS, objectName, val);
   }

   /**
    * Convenience method for logging a <code>boolean</code>'s name and value at the
    * {@link DebugLevel#L7_LUDICROUS Ludicrous} level.
    *
    * @see #dbo(DebugLevel,String,boolean)
    */
   public void
   ludicrousObject(String objectName, boolean val)
   {
      dbo(DebugLevel.L7_LUDICROUS, objectName, val);
   }

   /**
    * Convenience method for logging an <code>int</code>'s name and value at the
    * {@link DebugLevel#L7_LUDICROUS Ludicrous} level.
    *
    * @see #dbo(DebugLevel,String,int)
    */
   public void
   ludicrousObject(String objectName, int val)
   {
      dbo(DebugLevel.L7_LUDICROUS, objectName, val);
   }

   /**
    * <p>Logs a message containing an exception (or throwable).</p>
    *
    * <p>The exception will be printed only if the given debug level is less than or
    * equal to the current debug level of this <code>SimpleLogger</code>. This method will result in
    * the stack trace of the exception being printed if this option is turned on in the properties
    * (which it is by default).</p>
    *
    * @param t the throwable to log.
    */
   public void
   dbe(DebugLevel level, Throwable t)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
      {
         return;
      }

      Object[] data = createData(level, 1);
      data[data.length - 1] = t;
      MessageFormat format = isInstanceDebugger ? log.getDebugExceptionInstanceFormat() :
                                                           log.getDebugExceptionFormat();
      log.println(format.format(data));
   }

   /**
    * Convenience method for logging an exception (or throwable) at the
    * {@link DebugLevel#L1_FATAL Fatal} level.
    *
    * @see #dbe(DebugLevel,Throwable)
    */
   public void
   fatalException(Throwable t)
   {
      dbe(DebugLevel.L1_FATAL, t);
   }

   /**
    * Convenience method for logging an exception (or throwable) at the
    * {@link DebugLevel#L2_ERROR Error} level.
    *
    * @see #dbe(DebugLevel,Throwable)
    */
   public void
   errorException(Throwable t)
   {
      dbe(DebugLevel.L2_ERROR, t);
   }

   /**
    * Convenience method for logging an exception (or throwable) at the
    * {@link DebugLevel#L3_WARN Warn} level.
    *
    * @see #dbe(DebugLevel,Throwable)
    */
   public void
   warnException(Throwable t)
   {
      dbe(DebugLevel.L3_WARN, t);
   }

   /**
    * <p>Logs an entry message.</p>
    *
    * <p>The message will be printed only if the this <code>SimpleLogger</code>'s tracing flag is
    * set to <code>true</code>.</p>

    * @param methodName The method name to include in the entry message.
    */
   public void
   entry(String methodName)
   {
      if (!log.isOutputting() || !tracing)
      {
         return;
      }

      Object[] data = createData(DebugLevel.FAKE_TRACE, 1);
      data[data.length - 1] = methodName;
      MessageFormat format = isInstanceDebugger ? log.getEntryInstanceFormat() :
                                                           log.getEntryFormat();
      log.println(format.format(data));
   }

   /**
    * <p>Logs an exit message.</p>
    *
    * <p>The message will be printed only if the this <code>SimpleLogger</code>'s tracing flag is
    * set to <code>true</code>.</p>
    */
   public void
   exit(String methodName)
   {
      if (!log.isOutputting() || !tracing)
      {
         return;
      }

      Object[] data = createData(DebugLevel.FAKE_TRACE, 1);
      data[data.length - 1] = methodName;
      MessageFormat format = isInstanceDebugger ? log.getExitInstanceFormat() :
                                                           log.getExitFormat();
      log.println(format.format(data));
   }

   /**
    * Converts a <code>byte</code> to a hex string.
    *
    * @param b the byte
    *
    * @return the string
    */
   private static String
   toString(byte b)
   {
      return "0x" + byteString(b);
   }

   /**
    * Converts an array of bytes to a string of two-character hex values.
    *
    * @param bytes the byte array
    *
    * @return the string
    */
   private static String
   toString(byte[] bytes)
   {
      if (bytes == null)
      {
         return "null";
      }

      StringBuffer buf = new StringBuffer(bytes.length * 4);
      buf.append("0x[");
      boolean first = true;
      for (int i = 0; i < bytes.length; i++)
      {
         if (first && !(first = false))
         {
            buf.append(byteString(bytes[i]));
         }
         else
         {
            buf.append(", ").append(byteString(bytes[i]));
         }
      }
      buf.append(']');
      return buf.toString();
   }

   /**
    * Returns the two hex characters that represent the given byte.
    *
    * @param b the byte
    *
    * @return the two hex characters that represent the byte.
    */
   private static String
   byteString(byte b)
   {
      return new String(new char[] {BYTE_CHARS[(b >> 4) & 0x0F], BYTE_CHARS[b & 0x0F]});
   }
}