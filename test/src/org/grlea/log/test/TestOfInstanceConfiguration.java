package org.grlea.log.test;

// $Id: TestOfInstanceConfiguration.java,v 1.1 2005-08-08 14:13:44 grlea Exp $
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


import junit.framework.TestSuite;

/**
 * <p>Tests that logging for instance loggers works and is configurable.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfInstanceConfiguration
extends AbstractLoggingTest
{
   public
   TestOfInstanceConfiguration(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   public void
   testNormalLogging()
   throws Exception
   {
      String instanceId = "TestInstance";
      new SimpleLoggingClass(log, instanceId).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Fatal",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Error",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Warn",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Info",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testNonInstanceConfig()
   throws Exception
   {
      properties.setProperty(SimpleLoggingClass.class.getName(), "Ludicrous");

      String instanceId = "TestInstance";
      new SimpleLoggingClass(log, instanceId).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Fatal",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Error",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Warn",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Info",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Debug",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Verbose",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Ludicrous",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testInstanceConfig()
   throws Exception
   {
      String instanceId = "TestInstance";
      properties.setProperty(SimpleLoggingClass.class.getName() + "." + instanceId, "Ludicrous");

      new SimpleLoggingClass(log, instanceId).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Fatal",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Error",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Warn",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Info",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Debug",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Verbose",
         "   |main|SimpleLoggingClass[" + instanceId + "]|Test of Ludicrous",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testTwoInstancesConfig()
   throws Exception
   {
      String instanceId1 = "TestInstance-1";
      String instanceId2 = "TestInstance-2";
      properties.setProperty(SimpleLoggingClass.class.getName() + "." + instanceId1, "Ludicrous");

      new SimpleLoggingClass(log, instanceId1).doSomeLogging();
      new SimpleLoggingClass(log, instanceId2).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Fatal",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Error",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Warn",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Info",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Debug",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Verbose",
         "   |main|SimpleLoggingClass[" + instanceId1 + "]|Test of Ludicrous",

         "   |main|SimpleLoggingClass[" + instanceId2 + "]|Test of Fatal",
         "   |main|SimpleLoggingClass[" + instanceId2 + "]|Test of Error",
         "   |main|SimpleLoggingClass[" + instanceId2 + "]|Test of Warn",
         "   |main|SimpleLoggingClass[" + instanceId2 + "]|Test of Info",
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
      return new TestSuite(TestOfInstanceConfiguration.class);
   }
}