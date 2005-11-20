package org.grlea.log.adapters.slf4j;

// $Id: Slf4jAdapter.java,v 1.2 2005-11-20 00:35:33 grlea Exp $
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

import org.grlea.log.SimpleLogger;
import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryAdapter;
import org.slf4j.impl.MessageFormatter;

/**
 * <p>
 * An adapter for using <a href="https:/simple-log.dev.java.net/">Simple Log</a> within the
 * <a href="http://www.slf4j.org/">SLF4J</a> package.
 * </p>
 *
 * <p><b>Instructions for Use</b></p>
 *
 * <p>
 * To use this <code>Slf4jAdapter</code> with SLF4J, you should only need to include the JARs for
 * Simple Log (simple-log.jar) and for the SLF4J Adapter (simple-log-slf4j.jar) in your classpath
 * and use the SLF4J {@link LoggerFactory} to obtain log instances. <b>Be careful</b> not to confuse
 * 'slf4j-simple.jar' as being part of the arrangement. It is a SLF4J deliverable that simply prints
 * output to <code>System.out</code>.
 * </p>
 *
 * <p><b>Implementation Details</b></p>
 *
 * <p>
 * The <code>Slf4jAdapter</code> always uses the
 * {@link SimpleLog#defaultInstance() default SimpleLog}. It is anticipated this will do the job for
 * the preverbial 99% of cases. If this is insufficient for your needs, please write to me and we
 * can try and find a more flexible solution.
 * </p>
 *
 * <p>
 * The SLF4J levels map directly to the Simple Log levels:
 *    <table border="1" style="border: 1px solid black;">
 *       <tr>  <th>SLF4J</th>                               <th>Simple Log</th> </tr>
 *       <tr>  <td>{@link Logger#error(String) Error}</td>  <td>{@link DebugLevel#L2_ERROR Error}</td> </tr>
 *       <tr>  <td>{@link Logger#warn(String) Warn}</td>    <td>{@link DebugLevel#L3_WARN Warn}</td> </tr>
 *       <tr>  <td>{@link Logger#info(String) Info}</td>    <td>{@link DebugLevel#L4_INFO Info}</td> </tr>
 *       <tr>  <td>{@link Logger#debug(String) Debug}</td>  <td>{@link DebugLevel#L5_DEBUG Debug}</td> </tr>
 *    </table>
 * </p>
 *
 * <p><b>"Tracing"</b></p>
 *
 * <p>
 * Note that the SLF4J has no notion of tracing whatsoever, so the tracing properties in Simple Log
 * will have no bearing on the output.
 * </p>
 *
 * <p><b>Logger Names</b></p>
 *
 * <p>
 * The SLF4J API provides logger names that are Strings, while Simple Log prefers Class
 * objects for naming its loggers. This adapter tries to rectify the discord by attempting to
 * interpret logger names (or domain names, in
 * {@link LoggerFactoryAdapter#getLogger(String, String) the two argument getLogger() method})
 * as fully qualified class names.
 * Where this is not possible (because the logger name is not a class name), a
 * <code>SimpleLogger</code> will be created using a source class of
 * <code>org.slf4j.Logger</code> and with the provided logger name as the instance ID of the logger.
 * (See {@link SimpleLogger#SimpleLogger(Class, Object)}.
 * When the two-argument domain + sub-domain method is used and the domain is not a fully-qualified
 * class name, the domain and sub-domain are concatenated with a period ('.') in between, e.g.
 * <code>"domain.subdomain"</code>, and this is used as the instance ID.
 * This means that, when writing properties to configure loggers that don't use class names, the
 * properties should take the form:<pre>
 *    org.slf4j.Logger.<i>&lt;logger-name&gt;</i></pre>
 * or<pre>
 *    org.slf4j.Logger.<i>&lt;domain&gt;.&lt;subdomain&gt;</i></pre>
 * </p>
 *
 * <p>
 * When logger names cannot be interpreted as class names, each logger name that cannot be
 * interpreted will be printed with a warning to {@link System#err}. You can supress these warnings
 * by setting the system property <code>org.grlea.log.adapters.slf4j.supressWarnings</code> to
 * <code>true</code>.
 * </p>
 *
 * <p><b>Object Rendering</b></p>
 *
 * <p>
 * Messages submitted to the <code>Slf4jAdapter</code> using
 * {@link #debug(String, Object) the format + argument(s) methods} are interpreted using the
 * {@link MessageFormatter} that is provided in SLF4J.
 * </p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
Slf4jAdapter
implements Logger
{
   /** The logger this <code>Slf4jAdapter</code> will output to. */
   private final SimpleLogger log;

   /**
    * Creates a new <code>Slf4jAdapter</code> that will output to the provided {@link SimpleLogger}.
    *
    * @param log the logger this <code>Slf4jAdapter</code> will output to.
    */
   Slf4jAdapter(SimpleLogger log)
   {
      this.log = log;
   }

   public boolean
   isErrorEnabled()
   {
      return log.wouldLog(DebugLevel.L2_ERROR);
   }

   public boolean
   isWarnEnabled()
   {
      return log.wouldLog(DebugLevel.L3_WARN);
   }

   public boolean
   isInfoEnabled()
   {
      return log.wouldLog(DebugLevel.L4_INFO);
   }

   public boolean
   isDebugEnabled()
   {
      return log.wouldLog(DebugLevel.L5_DEBUG);
   }

   public void
   error(String message)
   {
      log.db(DebugLevel.L2_ERROR, message);
   }

   public void
   error(String messageFormat, Object argument)
   {
      if (log.wouldLog(DebugLevel.L2_ERROR))
      {
         log.db(DebugLevel.L2_ERROR, MessageFormatter.format(messageFormat, argument));
      }
   }

   public void
   error(String messageFormat, Object argument1, Object argument2)
   {
      if (log.wouldLog(DebugLevel.L2_ERROR))
      {
         log.db(DebugLevel.L2_ERROR, MessageFormatter.format(messageFormat, argument1, argument2));
      }
   }

   public void
   error(String message, Throwable exception)
   {
      log.db(DebugLevel.L2_ERROR, message);
      log.dbe(DebugLevel.L2_ERROR, exception);
   }

   public void
   warn(String message)
   {
      log.db(DebugLevel.L3_WARN, message);
   }

   public void
   warn(String messageFormat, Object argument)
   {
      if (log.wouldLog(DebugLevel.L3_WARN))
      {
         log.db(DebugLevel.L3_WARN, MessageFormatter.format(messageFormat, argument));
      }
   }

   public void
   warn(String messageFormat, Object argument1, Object argument2)
   {
      if (log.wouldLog(DebugLevel.L3_WARN))
      {
         log.db(DebugLevel.L3_WARN, MessageFormatter.format(messageFormat, argument1, argument2));
      }
   }

   public void
   warn(String message, Throwable exception)
   {
      log.db(DebugLevel.L3_WARN, message);
      log.dbe(DebugLevel.L3_WARN, exception);
   }

   public void
   info(String message)
   {
      log.db(DebugLevel.L4_INFO, message);
   }

   public void
   info(String messageFormat, Object argument)
   {
      if (log.wouldLog(DebugLevel.L4_INFO))
      {
         log.db(DebugLevel.L4_INFO, MessageFormatter.format(messageFormat, argument));
      }
   }

   public void
   info(String messageFormat, Object argument1, Object argument2)
   {
      if (log.wouldLog(DebugLevel.L4_INFO))
      {
         log.db(DebugLevel.L4_INFO, MessageFormatter.format(messageFormat, argument1, argument2));
      }
   }

   public void
   info(String message, Throwable exception)
   {
      log.db(DebugLevel.L4_INFO, message);
      log.dbe(DebugLevel.L4_INFO, exception);
   }

   public void
   debug(String message)
   {
      log.db(DebugLevel.L5_DEBUG, message);
   }

   public void
   debug(String messageFormat, Object argument)
   {
      if (log.wouldLog(DebugLevel.L5_DEBUG))
      {
         log.db(DebugLevel.L5_DEBUG, MessageFormatter.format(messageFormat, argument));
      }
   }

   public void
   debug(String messageFormat, Object argument1, Object argument2)
   {
      if (log.wouldLog(DebugLevel.L5_DEBUG))
      {
         log.db(DebugLevel.L5_DEBUG, MessageFormatter.format(messageFormat, argument1, argument2));
      }
   }

   public void
   debug(String message, Throwable exception)
   {
      log.db(DebugLevel.L5_DEBUG, message);
      log.dbe(DebugLevel.L5_DEBUG, exception);
   }
}