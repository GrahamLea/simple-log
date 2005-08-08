package org.grlea.log.test;

// $Id: TestOfDefaultConfiguration.java,v 1.1 2005-08-08 14:13:44 grlea Exp $
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
import org.grlea.log.DebugLevel;

import junit.framework.TestSuite;

/**
 * <p>Tests that logging for instance loggers works and is configurable.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfDefaultConfiguration
extends AbstractLoggingTest
{
   public
   TestOfDefaultConfiguration(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   public void
   testFatal()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Fatal");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L1_FATAL, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testError()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Error");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L2_ERROR, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testWarn()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Warn");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L3_WARN, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
         "   |main|SimpleLoggingClass|Test of Warn",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testInfo()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Info");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L4_INFO, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
         "   |main|SimpleLoggingClass|Test of Warn",
         "   |main|SimpleLoggingClass|Test of Info",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testDebug()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Debug");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L5_DEBUG, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
         "   |main|SimpleLoggingClass|Test of Warn",
         "   |main|SimpleLoggingClass|Test of Info",
         "   |main|SimpleLoggingClass|Test of Debug",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testVerbose()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Verbose");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L6_VERBOSE, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
         "   |main|SimpleLoggingClass|Test of Warn",
         "   |main|SimpleLoggingClass|Test of Info",
         "   |main|SimpleLoggingClass|Test of Debug",
         "   |main|SimpleLoggingClass|Test of Verbose",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testLudicrous()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Ludicrous");
      log.reloadProperties();
      assertEquals("log.defaultLevel", DebugLevel.L7_LUDICROUS, log.getDefaultLevel());

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         "   |main|SimpleLoggingClass|Test of Error",
         "   |main|SimpleLoggingClass|Test of Warn",
         "   |main|SimpleLoggingClass|Test of Info",
         "   |main|SimpleLoggingClass|Test of Debug",
         "   |main|SimpleLoggingClass|Test of Verbose",
         "   |main|SimpleLoggingClass|Test of Ludicrous",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testTracing()
   throws Exception
   {
      properties.setProperty("simplelog.defaultLevel", "Fatal");
      properties.setProperty("simplelog.defaultTrace", "true");
      log.reloadProperties();
      assertEquals("log.defaultTrace", true, log.isDefaultTracing());      

      new SimpleLoggingClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass|Test of Fatal",
         ">>>|main|SimpleLoggingClass|doSomeLogging()",
         "<<<|main|SimpleLoggingClass|doSomeLogging()",
      };

      checkOutput(expectedOutputLineParts);
   }

   /**
    * Returns a test suite that will automatically run all test methods in this
    * class beginning with "test".
    */
   public static TestSuite
   suite()
   {
      return new TestSuite(TestOfDefaultConfiguration.class);
   }
}