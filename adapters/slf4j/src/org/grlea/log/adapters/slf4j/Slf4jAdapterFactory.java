package org.grlea.log.adapters.slf4j;

// $Id: Slf4jAdapterFactory.java,v 1.2 2006-07-13 12:44:58 grlea Exp $
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

import org.grlea.log.SimpleLogger;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

/**
 * <p>
 * An implementation of {@link ILoggerFactory} which always returns {@link Slf4jAdapter}
 * instances.
 * </p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
Slf4jAdapterFactory
implements ILoggerFactory
{
   private static final String SUPRESS_WARNINGS_PROPERTY =
      "org.grlea.log.adapters.slf4j.supressWarnings";

   /**
    * Creates a new <code>Slf4jAdapterFactory</code>.
    */
   public
   Slf4jAdapterFactory()
   {}

   public final Logger
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

      return new Slf4jAdapter(newLogger, loggerName);
   }
}