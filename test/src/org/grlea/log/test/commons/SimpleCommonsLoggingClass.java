package org.grlea.log.test.commons;

// $Id: SimpleCommonsLoggingClass.java,v 1.1 2005-08-08 14:13:44 grlea Exp $
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

/**
 * <p></p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
SimpleCommonsLoggingClass
{
   private final Log log;

   public
   SimpleCommonsLoggingClass()
   {
      log = LogFactory.getLog(getClass());
   }

   public Log
   getLog()
   {
      return log;
   }

   public void
   doSomeLogging()
   {
      log.fatal("Test of Fatal");
      log.error("Test of Error");
      log.warn("Test of Warn");
      log.info("Test of Info");
      log.debug("Test of Debug");
      log.trace("Test of Trace");
   }

   public void
   doSomeTracing()
   {
      log.trace("IN: doSomeTracing()");
      log.trace("OUT: doSomeTracing()");
   }

   public void
   logAnError()
   {
      log.info("Test of Exception", new Throwable("Test Message"));
   }
}