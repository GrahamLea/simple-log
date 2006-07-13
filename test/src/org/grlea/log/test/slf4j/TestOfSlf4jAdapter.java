package org.grlea.log.test.slf4j;

// $Id: TestOfSlf4jAdapter.java,v 1.2 2006-07-13 12:44:56 grlea Exp $
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


import org.grlea.log.DebugLevel;
import org.grlea.log.SimpleLog;
import org.grlea.log.adapters.commons.CommonsLoggingAdapter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * <p>Tests the public interface of {@link CommonsLoggingAdapter}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
TestOfSlf4jAdapter
extends TestCase
{
   private ByteArrayOutputStream outputStream;

   public
   TestOfSlf4jAdapter(String name)
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
      new SimpleSlf4jLoggingClass().doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleSlf4jLoggingClass|Test of Error",
         "   |main|SimpleSlf4jLoggingClass|Test of Warn",
         "   |main|SimpleSlf4jLoggingClass|Test of Info",
      };

      checkOutput(expectedOutputLineParts, true);
   }

   public void
   testErrorLogging()
   throws Exception
   {
      Properties logProperties = getLogProperties(SimpleLog.defaultInstance());
      logProperties.setProperty(SimpleSlf4jLoggingClass.class.getName(), "Error");
      reconfigureLoggers();
      new SimpleSlf4jLoggingClass().doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleSlf4jLoggingClass|Test of Error",
      };

      checkOutput(expectedOutputLineParts, true);
   }

   private void
   reconfigureLoggers()
   throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
   {
      Method reconfigureMethod = SimpleLog.class.getDeclaredMethod("reconfigureAllLoggers", new Class[0]);
      reconfigureMethod.setAccessible(true);
      reconfigureMethod.invoke(SimpleLog.defaultInstance(), new Object[0]);
   }

   public void
   testLogginAnError()
   throws Exception
   {
      SimpleLog.defaultInstance().setDefaultLevel(DebugLevel.L4_INFO);
      new SimpleSlf4jLoggingClass().logAnError();

      String[] expectedOutputLineParts =
      {
         "   |main|SimpleSlf4jLoggingClass|Test of Exception",
         "***|main|SimpleSlf4jLoggingClass|java.lang.Throwable: Test Message",
      };

      checkOutput(expectedOutputLineParts, false);
   }

   public void
   testLoggerNameThatIsNotAClassName()
   throws Exception
   {
      String loggerName = "MadeUpLoggerName";
      SimpleLog.defaultInstance().setDefaultLevel(DebugLevel.L4_INFO);
      new SimpleSlf4jLoggingClass(loggerName).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|Logger[MadeUpLoggerName]|Test of Error",
         "   |main|Logger[MadeUpLoggerName]|Test of Warn",
         "   |main|Logger[MadeUpLoggerName]|Test of Info",
      };

      checkOutput(expectedOutputLineParts, true);
   }

   public void
   testConfiguringLoggerNameThatIsNotAClassName()
   throws Exception
   {
      String loggerName = "MadeUpLoggerName";
      getLogProperties(SimpleLog.defaultInstance())
         .setProperty("org.slf4j.Logger.MadeUpLoggerName", "Error");
      reconfigureLoggers();
      new SimpleSlf4jLoggingClass(loggerName).doSomeLogging();

      String[] expectedOutputLineParts =
      {
         "   |main|Logger[MadeUpLoggerName]|Test of Error",
      };

      checkOutput(expectedOutputLineParts, true);
   }

   /**
    * Returns a test suite that will automatically run all test methods in this
    * class beginning with "test".
    */
   public static TestSuite
   suite()
   {
      return new TestSuite(TestOfSlf4jAdapter.class);
   }


   protected void
   checkOutput(String[] expectedOutputLineParts, boolean failWhenExtraLinesDetected)
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
            if (failWhenExtraLinesDetected)
               fail("More output lines than expected.\nExtra line: " + outputLine);
            else
               break;
         }

         String expectedOutputLinePart = expectedOutputLineParts[lineNumber];
         boolean linePartFound = outputLine.indexOf(expectedOutputLinePart) != -1;
         assertEquals("'" + expectedOutputLinePart + "' not found in '" + outputLine + "'",
                      true, linePartFound);

         lineNumber++;
      }

      assertEquals("output lines", expectedOutputLineParts.length, lineNumber);
   }
}