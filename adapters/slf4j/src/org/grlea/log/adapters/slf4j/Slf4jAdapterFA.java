package org.grlea.log.adapters.slf4j;

// $Id: Slf4jAdapterFA.java,v 1.1 2005-08-09 10:13:40 grlea Exp $
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactoryAdapter;

/**
 * <p>
 * An implementation of {@link LoggerFactoryAdapter} which always returns {@link Slf4jAdapter}
 * instances.
 * </p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
Slf4jAdapterFA
implements LoggerFactoryAdapter
{
   private static final String SUPRESS_WARNINGS_PROPERTY =
      "org.grlea.log.adapters.slf4j.supressWarnings";

   public
   Slf4jAdapterFA()
   {}

   public Logger
   getLogger(String loggerName)
   {
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
            System.err.println("WARNING: Simple Log (Slf4jAdapterFA): " +
                               "Failed to read system property '" + SUPRESS_WARNINGS_PROPERTY + "'");
         }

         if (!supressWarnings)
         {
            System.err.println("WARNING: Simple Log (Slf4jAdapterFA): " +
                               "Failed to find class for logger loggerName '" + loggerName + "'. " +
                               "Using class '" + Logger.class.getName() + "' and instanceId '" +
                               loggerName +"'.");
         }

         newLogger = new SimpleLogger(Logger.class, loggerName);
      }

      return new Slf4jAdapter(newLogger);
   }

   public Logger
   getLogger(String domain, String subDomain)
   {
      SimpleLogger newLogger;
      try
      {
         Class loggingClass = Class.forName(domain);
         newLogger = new SimpleLogger(loggingClass, subDomain);
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
                               "Failed to find class for logger domain '" + domain + "'. " +
                               "Using class '" + Logger.class.getName() + "' and instanceId '" +
                               (domain + "." + subDomain) + "'.");
         }

         newLogger = new SimpleLogger(Logger.class, (domain + "." + subDomain));
      }

      return new Slf4jAdapter(newLogger);
   }
}