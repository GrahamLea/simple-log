package org.grlea.log.test;

// $Id: TestOfInnerClassLogging.java,v 1.1 2005-08-08 14:13:44 grlea Exp $
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

import junit.framework.TestSuite;

/**
 * <p>Tests that logging for inner classes works and is configurable.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfInnerClassLogging
extends AbstractLoggingTest
{

   public
   TestOfInnerClassLogging(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   public void
   testNormalLogging()
   throws Exception
   {
      new InnerClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Fatal",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Error",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Warn",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Info",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testLudicrousLogging()
   throws Exception
   {
      properties.setProperty(InnerClass.class.getName(), "Ludicrous");
      log.reloadProperties();
      new InnerClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Fatal",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Error",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Warn",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Info",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Debug",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Verbose",
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Ludicrous",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testTracing()
   throws Exception
   {
      properties.setProperty(InnerClass.class.getName(), "Fatal");
      properties.setProperty(InnerClass.class.getName() + "#trace", "true");
      log.reloadProperties();
      new InnerClass(log).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|TestOfInnerClassLogging$InnerClass|Test of Fatal",
         ">>>|main|TestOfInnerClassLogging$InnerClass|doSomeLogging",
         "<<<|main|TestOfInnerClassLogging$InnerClass|doSomeLogging",
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
      return new TestSuite(TestOfInnerClassLogging.class);
   }


   private static final class
   InnerClass
   {
      private final SimpleLogger log;

      public
      InnerClass(SimpleLog logTarget)
      {
         log = new SimpleLogger(logTarget, InnerClass.class);
      }

      private void
      doSomeLogging()
      {
         log.fatal("Test of Fatal");
         log.error("Test of Error");
         log.warn("Test of Warn");
         log.info("Test of Info");
         log.debug("Test of Debug");
         log.verbose("Test of Verbose");
         log.ludicrous("Test of Ludicrous");

         log.entry("doSomeLogging()");
         log.exit("doSomeLogging()");
      }
   }
}