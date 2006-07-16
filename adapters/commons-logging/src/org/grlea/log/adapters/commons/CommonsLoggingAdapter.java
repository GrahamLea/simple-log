package org.grlea.log.adapters.commons;

// $Id: CommonsLoggingAdapter.java,v 1.3 2006-07-16 22:50:23 grlea Exp $
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

import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLog;
import org.grlea.log.SimpleLogger;

import org.apache.commons.logging.Log;

/**
 * <p>
 * An adapter for using <a href="https:/simple-log.dev.java.net/">Simple Log</a> within the
 * Jakarta <a href="http://jakarta.apache.org/commons/logging/">Commons Logging</a> package.
 * </p>
 *
 * <p><b>Instructions for Use</b></p>
 *
 * <p>
 * To use this <code>CommonsLoggingAdapter</code> with Commons Logging, simply
 * {@link System#setProperty set the system property} '<code>org.apache.commons.logging.Log</code>'
 * to '<code>org.grlea.log.adapters.commons.CommonsLoggingAdapter</code>'.
 * </p>
 *
 * <p><b>Implementation Details</b></p>
 *
 * <p>
 * The <code>CommonsLoggingAdapter</code> always uses the
 * {@link SimpleLog#defaultInstance() default SimpleLog}. It is anticipated this will do the job for
 * the preverbial 99% of cases. If this is insufficient for your needs, please write to me and we
 * can try and find a more flexible solution. 
 * </p>
 *
 * <p>
 * The Commons Logging levels are mapped to the Simple Log levels in this manner:
 *    <table border="1" style="border: 1px solid black;">
 *       <tr>  <th>Commons Logging</th>                  <th>Simple Log</th> </tr>
 *       <tr>  <td>{@link Log#fatal(Object) Fatal}</td>  <td>{@link DebugLevel#L1_FATAL Fatal}</td> </tr>
 *       <tr>  <td>{@link Log#error(Object) Error}</td>  <td>{@link DebugLevel#L2_ERROR Error}</td> </tr>
 *       <tr>  <td>{@link Log#warn(Object) Warn}</td>    <td>{@link DebugLevel#L3_WARN Warn}</td> </tr>
 *       <tr>  <td>{@link Log#info(Object) Info}</td>    <td>{@link DebugLevel#L4_INFO Info}</td> </tr>
 *       <tr>  <td>{@link Log#debug(Object) Debug}</td>  <td>{@link DebugLevel#L5_DEBUG Debug}</td> </tr>
 *       <tr>  <td><b>{@link Log#trace(Object) Trace}</b></td>
 *             <td><b>{@link DebugLevel#L6_VERBOSE Verbose}</b></td> </tr>
 *    </table>
 * </p>
 *
 * <p><b>"Tracing"</b></p>
 *
 * <p>
 * Note that the Commons Logging 'Trace' level is <b>not</b> matched to Simple
 * Log's tracing facility (i.e. {@link SimpleLogger}'s {@link SimpleLogger#entry entry()} and
 * {@link SimpleLogger#exit exit()} methdos), but to the {@link DebugLevel#L6_VERBOSE Verbose}
 * level. This is because the Commons Logging API allows any kind of object or exception to be
 * logged at the Trace level (rather than just method names), and provides no information
 * regarding whether the log is for a method entry or exit.
 * </p>
 *
 * <p><b>Logger Names</b></p>
 *
 * <p>
 * The Commons Logging API provides logger names that are Strings, while Simple Log prefers Class
 * objects for naming its loggers. This adapter tries to rectify the discord by attempting to
 * interpret logger names as fully qualified class names. Where this is not possible (because the
 * logger name is not a class name), a <code>SimpleLogger</code> will be created using a source
 * class of <code>org.apache.commons.logging.Log</code> and with the provided logger name as the
 * instance ID of the logger. (See {@link SimpleLogger#SimpleLogger(Class, Object)}. This means
 * that, when writing properties to configure loggers that don't use class names, the properties
 * should take the form:<pre>
 *    org.apache.commons.logging.Log.<i>&lt;logger-name&gt;</i></pre>
 * </p>
 *
 * <p>
 * When logger names cannot be interpreted as class names, each logger name that cannot be
 * interpreted will be printed with a warning to {@link System#err}. You can supress these warnings
 * by setting the system property <code>org.grlea.log.adapters.commons.supressWarnings</code> to
 * <code>true</code>.
 * </p>
 *
 * <p><b>Object Rendering</b></p>
 *
 * <p>
 * Because Simple Log has a notion of logging message types (i.e. debug message, debug object and
 * debug exception) but Commons Logging does not, the generic 'message' objects passed in to this
 * adapter are passed on to Simple Log in this manner:
 * <ul>
 *    <li>
 *       Any String passed in to either form of a logging method (e.g. {@link #fatal(Object)}
 *       or {@link #fatal(Object, Throwable)}) is passed onto the {@link SimpleLogger#db} method.
 *    </li>
 *    <li>
 *       Any other {@link Object} passed in as the 'message' argument is passed onto the
 *       {@link SimpleLogger#dbo} method as an object whose vaue is to be debugged, with the empty
 *       string as the object name.
 *    </li>
 *    <li>
 *       Throwables passed as the second argument in methods of the form
 *       {@link #fatal(Object, Throwable)} result in an additional call to the
 *       {@link SimpleLogger#dbe} method.
 *    </li>
 * </ul>
 * </p>
 *
 * @author Graham Lea
 * @version $Revision: 1.3 $
 */
public class
CommonsLoggingAdapter
implements Log
{
   private static final String SUPRESS_WARNINGS_PROPERTY = "org.grlea.log.adapters.commons.supressWarnings";

   private static SimpleLog log = null;

   private final SimpleLogger logger;

   public
   CommonsLoggingAdapter(String loggerName)
   {
      initLog();

      SimpleLogger newLogger;
      try
      {
         Class loggingClass = Class.forName(loggerName);
         newLogger = new SimpleLogger(loggingClass);
      }
      catch (Exception e)
      {
         boolean supressWarnings = false;
         try
         {
            String supressWarningsString = System.getProperty(SUPRESS_WARNINGS_PROPERTY);
            supressWarnings =
               supressWarningsString != null && supressWarningsString.toLowerCase().equals("true");
         }
         catch (Exception e1)
         {
            System.err.println("WARNING: Simple Log (CommonsLoggingAdapter): " +
                               "Failed to read system property '" + SUPRESS_WARNINGS_PROPERTY + "'");
         }

         if (!supressWarnings)
         {
            System.err.println("WARNING: Simple Log (CommonsLoggingAdapter): " +
                               "Failed to find class for logger name '" + loggerName + "'. " +
                               "Using class '" + Log.class.getName() + "' and instanceId '" +
                               loggerName +"'.");
         }

         newLogger = new SimpleLogger(Log.class, loggerName);
      }

      this.logger = newLogger;
   }

   private static void
   initLog()
   {
      if (log == null)
      {
         setLog(SimpleLog.defaultInstance());
      }
   }

   public static void
   setLog(SimpleLog simpleLog)
   {
      log = simpleLog;
   }

   public boolean
   isFatalEnabled()
   {
      return logger.wouldLog(DebugLevel.L1_FATAL);
   }

   public boolean
   isErrorEnabled()
   {
      return logger.wouldLog(DebugLevel.L2_ERROR);
   }

   public boolean
   isWarnEnabled()
   {
      return logger.wouldLog(DebugLevel.L3_WARN);
   }

   public boolean
   isInfoEnabled()
   {
      return logger.wouldLog(DebugLevel.L4_INFO);
   }

   public boolean
   isDebugEnabled()
   {
      return logger.wouldLog(DebugLevel.L5_DEBUG);
   }

   public boolean
   isTraceEnabled()
   {
      return logger.isTracing();
   }

   public void
   fatal(Object message)
   {
      if (logger.wouldLog(DebugLevel.L1_FATAL))
      {
         log(DebugLevel.L1_FATAL, message);
      }
   }

   public void
   fatal(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L1_FATAL))
      {
         log(DebugLevel.L1_FATAL, message);
         logger.dbe(DebugLevel.L1_FATAL, t);
      }
   }

   public void
   error(Object message)
   {
      if (logger.wouldLog(DebugLevel.L2_ERROR))
      {
         log(DebugLevel.L2_ERROR, message);
      }
   }

   public void
   error(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L2_ERROR))
      {
         log(DebugLevel.L2_ERROR, message);
         logger.dbe(DebugLevel.L2_ERROR, t);
      }
   }

   public void
   warn(Object message)
   {
      if (logger.wouldLog(DebugLevel.L3_WARN))
      {
         log(DebugLevel.L3_WARN, message);
      }
   }

   public void
   warn(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L3_WARN))
      {
         log(DebugLevel.L3_WARN, message);
         logger.dbe(DebugLevel.L3_WARN, t);
      }
   }

   public void
   info(Object message)
   {
      if (logger.wouldLog(DebugLevel.L4_INFO))
      {
         log(DebugLevel.L4_INFO, message);
      }
   }

   public void
   info(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L4_INFO))
      {
         log(DebugLevel.L4_INFO, message);
         logger.dbe(DebugLevel.L4_INFO, t);
      }
   }

   public void
   debug(Object message)
   {
      if (logger.wouldLog(DebugLevel.L5_DEBUG))
      {
         log(DebugLevel.L5_DEBUG, message);
      }
   }

   public void
   debug(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L5_DEBUG))
      {
         log(DebugLevel.L5_DEBUG, message);
         logger.dbe(DebugLevel.L5_DEBUG, t);
      }
   }

   public void
   trace(Object message)
   {
      if (logger.wouldLog(DebugLevel.L6_VERBOSE))
      {
         log(DebugLevel.L6_VERBOSE, message);
      }
   }

   public void
   trace(Object message, Throwable t)
   {
      if (logger.wouldLog(DebugLevel.L6_VERBOSE))
      {
         log(DebugLevel.L6_VERBOSE, message);
         logger.dbe(DebugLevel.L6_VERBOSE, t);
      }
   }

   private void
   log(DebugLevel level, Object message)
   {
      if (message instanceof String)
         logger.db(level, (String) message);
      else
         logger.dbo(level, "", message);
   }
}