package org.grlea.log;

// $Id: SimpleLogger.java,v 1.3 2005-01-18 13:34:06 grlea Exp $
// Copyright (c) 2004-2005 Graham Lea. All rights reserved.

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
 *       log.dbo(DebugLevel.L6_VERBOSE, "argv", argv);
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
 *       "db" = Debug (see {@link org.grlea.log.SimpleLogger#db SimpleLogger.db()})<br>
 *       Lets you log a simple log message, e.g. "Got to the point where you thought it wasn't
 *       getting to."
 *    </li><br><br>
 *    <li>
 *       "dbo" = Debug Object
 *       (see {@link org.grlea.log.SimpleLogger#dbo(DebugLevel,String,Object) SimpleLogger.dbo()})
 *       <br>
 *       Debugs the name and value of an object. Specially handled variants exist for all primitives,
 *       Object[], byte[] and char[].
 *    </li><br><br>
 *    <li>
 *       "dbe" = Debug Exception (see {@link org.grlea.log.SimpleLogger#dbe SimpleLogger.dbe()})<br>
 *       Special handling of exceptions that will print a stack trace (can be turned off).
 *    </li><br><br>
 *    <li>
 *       Tracing (see {@link org.grlea.log.SimpleLogger#entry SimpleLogger.entry()} and
 *       {@link org.grlea.log.SimpleLogger#exit SimpleLogger.exit()})<br>
 *       Logs entry to and exit from a method. Can be turned on/off independent of debug level.<br>
 *    </li>
 * </ul>
 * </p>
 *
 * @version $Revision: 1.3 $
 * @author $Author: grlea $
 */
public final class
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
//      this.classNameShort = className.substring(className.lastIndexOf('.') + 1);
      if (className.indexOf('.') == -1)
         this.classNameShort = className;
      else
         this.classNameShort = className.substring(sourceClass.getPackage().getName().length() + 1);

      log.register(this);
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
         throw new IllegalStateException("This is not an instance debugger.");
      if (instanceId == null)
         throw new IllegalArgumentException("instanceId cannot be null.");
      this.instanceId = instanceId;
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
      return getDebugLevel().shouldLog(level);
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
         throw new IllegalArgumentException("debugLevel cannot be null.");
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
    * @param extraArguments the number of empty slots to create at the end of the array.
    *
    * @return the newly created array of objects.
    */
   private Object[]
   createData(int extraArguments)
   {
      Object[] result = new Object[4 + extraArguments];
      result[0] = new Date();
      result[1] = Thread.currentThread().getName();
      result[2] = useLongName ? className : classNameShort;
      result[3] = instanceId;
      return result;
   }

   /**
    * Logs a debug message at the "Fatal" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L1_FATAL
    */
   public void
   fatal(String s)
   {
      db(DebugLevel.L1_FATAL, s);
   }

   /**
    * Logs a debug message at the "Error" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L2_ERROR
    */
   public void
   error(String s)
   {
      db(DebugLevel.L2_ERROR, s);
   }

   /**
    * Logs a debug message at the "Warn" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L3_WARN
    */
   public void
   warn(String s)
   {
      db(DebugLevel.L3_WARN, s);
   }

   /**
    * Logs a debug message at the "Info" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L4_INFO
    */
   public void
   info(String s)
   {
      db(DebugLevel.L4_INFO, s);
   }

   /**
    * Logs a debug message at the "Debug" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L5_DEBUG
    */
   public void
   debug(String s)
   {
      db(DebugLevel.L5_DEBUG, s);
   }

   /**
    * Logs a debug message at the "Verbose" level.
    *
    * @param s the message to log.
    *
    * @see #db
    * @see DebugLevel#L6_VERBOSE
    */
   public void
   verbose(String s)
   {
      db(DebugLevel.L6_VERBOSE, s);
   }

   /**
    * <p>Logs a simple debug message.</p>
    *
    * <p>The message will be printed if the given debug level is less than or equal to the current
    * debug level of this <code>SimpleLogger</code>.</p>
    *
    * @param s The debug message to print.
    *
    * @see #fatal
    * @see #error
    * @see #warn
    * @see #info
    * @see #debug
    * @see #verbose
    */
   public void
   db(DebugLevel level, String s)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      Object[] data = createData(1);
      data[data.length - 1] = s;
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
    * @param objName The name of the object whose value is being given.
    *
    * @param val The name value of the object.
    */
   public void
   dbo(DebugLevel level, String objName, Object val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, val);
   }

   /**
    * The same as {@link #dbo(DebugLevel,String,Object)} but without the debug level check.
    * This method is used by all the primitive- and array-typed variants of <code>dbo()</code>,
    * which do their own checks before calling this method.
    */
   private void
   dboNoCheck(String objName, Object val)
   {
      Object[] data = createData(2);
      data[data.length - 2] = objName;
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
    * @param objName The name of the array whose value is being given.
    *
    * @param val The array.
    */
   public void
   dbo(DebugLevel level, String objName, Object[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      StringBuffer stringValue = new StringBuffer("[");
      for (int i = 0; i < val.length; i++)
      {
         if (i == 0)
            stringValue.append(val[i]);
         else
            stringValue.append(", ").append(val[i]);
      }
      stringValue.append("]");

      dboNoCheck(objName, stringValue);
   }

   /**
    * <p>Logs a message containing a <code>short</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, short val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing an <code>int</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, int val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>long</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, long val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName,String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>boolean</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, boolean val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>float</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, float val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>double</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, double val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>byte</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, byte val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, toString(val));
   }

   /**
    * <p>Logs a message containing a <code>byte</code> array's name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, byte[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, toString(val));
   }

   /**
    * <p>Logs a message containing a <code>char</code>'s name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, char val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing a <code>char</code> array's name and value.</p>
    *
    * @see #dbo(DebugLevel,String,Object)
    */
   public void
   dbo(DebugLevel level, String objName, char[] val)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      dboNoCheck(objName, String.valueOf(val));
   }

   /**
    * <p>Logs a message containing an exception (or throwable).</p>
    *
    * <p>The exception will be printed only if the given debug level is less than or
    * equal to the current debug level of this <code>SimpleLogger</code>.</p>
    *
    * @param t the throwable to log.
    */
   public void
   dbe(DebugLevel level, Throwable t)
   {
      if (!log.isOutputting() || !debugLevel.shouldLog(level))
         return;

      Object[] data = createData(1);
      data[data.length - 1] = t;
      MessageFormat format = isInstanceDebugger ? log.getDebugExceptionInstanceFormat() :
                                                           log.getDebugExceptionFormat();
      log.println(format.format(data));
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
         return;

      Object[] data = createData(1);
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
         return;

      Object[] data = createData(1);
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
         return "null";

      StringBuffer buf = new StringBuffer(bytes.length * 4);
      buf.append("0x[");
      boolean first = true;
      for (int i = 0; i < bytes.length; i++)
         if (first && !(first = false))
            buf.append(byteString(bytes[i]));
         else
            buf.append(", ").append(byteString(bytes[i]));
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