package org.grlea.log.test.slf4j;

// $Id: SimpleSlf4jLoggingClass.java,v 1.1 2005-08-09 10:14:38 grlea Exp $
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

import org.grlea.log.SimpleLog;
import org.grlea.log.SimpleLogger;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p></p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
SimpleSlf4jLoggingClass
{
   private final Logger log;

   public
   SimpleSlf4jLoggingClass()
   {
      log = LoggerFactory.getLogger(getClass());
   }

   public
   SimpleSlf4jLoggingClass(String loggerName)
   {
      log = LoggerFactory.getLogger(loggerName);
   }

   public Logger
   getLogger()
   {
      return log;
   }

   public void
   doSomeLogging()
   {
      log.error("Test of Error");
      log.warn("Test of Warn");
      log.info("Test of Info");
      log.debug("Test of Debug");
   }

   public void
   logAnError()
   {
      log.info("Test of Exception", new Throwable("Test Message"));
   }
}