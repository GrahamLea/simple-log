package org.grlea.log.test.commons;

// $Id: TestOfCommonsLoggingAdapter.java,v 1.1 2005-08-08 14:13:45 grlea Exp $
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


import org.grlea.log.adapters.commons.CommonsLoggingAdapter;
import org.grlea.log.SimpleLog;
import org.grlea.log.DebugLevel;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.lang.reflect.Field;

/**
 * <p>Tests the public interface of {@link CommonsLoggingAdapter}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfCommonsLoggingAdapter
extends TestCase
{
   private ByteArrayOutputStream outputStream;

   public
   TestOfCommonsLoggingAdapter(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   static int instanceNumber = 1;

   protected void
   setUp()
   throws Exception
   {
      outputStream = new ByteArrayOutputStream(512);

      SimpleLog simpleLog = SimpleLog.defaultInstance();
      Properties simpleLogProperties = getLogProperties(simpleLog);
      simpleLogProperties.clear();
      simpleLog.reloadProperties();
      simpleLog.setWriter(new PrintWriter(outputStream, true));

      System.setProperty("org.apache.commons.logging.Log", CommonsLoggingAdapter.class.getName());
   }

   private Properties
   getLogProperties(SimpleLog simpleLog)
   throws NoSuchFieldException, IllegalAccessException
   {
      Field propertiesField = simpleLog.getClass().getDeclaredField("properties");
      propertiesField.setAccessible(true);
      return (Properties) propertiesField.get(simpleLog);
   }

   protected void
   tearDown()
   {
      outputStream = null;
   }

   public void
   testNormalLogging()
   throws Exception
   {
      new SimpleCommonsLoggingClass().doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleCommonsLoggingClass|Test of Fatal",
         "   |main|SimpleCommonsLoggingClass|Test of Error",
         "   |main|SimpleCommonsLoggingClass|Test of Warn",
         "   |main|SimpleCommonsLoggingClass|Test of Info",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testTracingWhenVerboseOn()
   throws Exception
   {
      SimpleLog.defaultInstance().setDefaultLevel(DebugLevel.L6_VERBOSE);

      new SimpleCommonsLoggingClass().doSomeTracing();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleCommonsLoggingClass|IN: doSomeTracing()",
         "   |main|SimpleCommonsLoggingClass|OUT: doSomeTracing()",
      };

      checkOutput(expectedOutputLineParts);
   }

   public void
   testTracingWhenVerboseOff()
   throws Exception
   {
      SimpleLog.defaultInstance().setDefaultLevel(DebugLevel.L5_DEBUG);
      new SimpleCommonsLoggingClass().doSomeTracing();

      checkOutput(new String[0]);
   }


   /**
    * Returns a test suite that will automatically run all test methods in this
    * class beginning with "test".
    */
   public static TestSuite
   suite()
   {
      return new TestSuite(TestOfCommonsLoggingAdapter.class);
   }


   protected void
   checkOutput(String[] expectedOutputLineParts)
   throws IOException
   {
      System.err.flush();

      byte[] output = outputStream.toByteArray();
      ByteArrayInputStream byteIn = new ByteArrayInputStream(output);
      InputStreamReader streamReader = new InputStreamReader(byteIn);
      BufferedReader in = new BufferedReader(streamReader);
      String outputLine;
      int lineNumber = 0;
      while ((outputLine = in.readLine()) != null)
      {
         if (lineNumber >= expectedOutputLineParts.length)
         {
            fail("More output lines than expected.\nExtra line: " + outputLine);
         }

         String expectedOutputLinePart = expectedOutputLineParts[lineNumber];
         boolean linePartFound = outputLine.indexOf(expectedOutputLinePart) != -1;
         assertEquals("'" + expectedOutputLinePart + "' found in '" + outputLine + "'",
                      true, linePartFound);

         lineNumber++;
      }

      assertEquals("output lines", expectedOutputLineParts.length, lineNumber);
   }
}