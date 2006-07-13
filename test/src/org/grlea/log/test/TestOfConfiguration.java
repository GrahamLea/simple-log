package org.grlea.log.test;

// $Id: TestOfConfiguration.java,v 1.3 2006-07-13 12:44:56 grlea Exp $
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


import junit.framework.TestSuite;

/**
 * <p>Tests that logging for instance loggers works and is configurable.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.3 $
 */
public class
TestOfConfiguration
extends AbstractLoggingTest
{
   public
   TestOfConfiguration(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   public void
   testFatal()
   throws Exception
   {
      properties.setProperty(SimpleLoggingClass.class.getName(), "Fatal");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Error");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Warn");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Info");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Debug");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Verbose");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Ludicrous");
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
      properties.setProperty(SimpleLoggingClass.class.getName(), "Fatal");
      properties.setProperty(SimpleLoggingClass.class.getName() + "#trace", "true");
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
      return new TestSuite(TestOfConfiguration.class);
   }
}