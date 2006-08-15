package org.grlea.log.test;

// $Id: TestOfDefaultInstanceChange.java,v 1.1 2006-08-15 21:47:23 grlea Exp $
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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.lang.reflect.Field;

/**
 * <p>Test changing the default instance in {@link SimpleLog}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfDefaultInstanceChange
extends TestCase
{
   public
   TestOfDefaultInstanceChange(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   /**
    * Tests that the default SimpleLog instance can be changed, and that newly created loggers put
    * their output through the newly set instance.
    */
   public void
   testSettingOfDefaultInstance()
   throws Exception
   {
      Field defaultInstanceField = SimpleLog.class.getDeclaredField("defaultInstance");
      defaultInstanceField.setAccessible(true);
      SimpleLog originalDefaultInstance = (SimpleLog) defaultInstanceField.get(null);

      PrintStream originalErr = System.err;
      ByteArrayOutputStream bytesOnErr = new ByteArrayOutputStream(1024);
      System.setErr(new PrintStream(bytesOnErr));

      ByteArrayOutputStream loggerOutput = new ByteArrayOutputStream(1024);

      Properties properties = new Properties();
      SimpleLog log = new SimpleLog(properties);
      log.setWriter(new PrintWriter(loggerOutput, true));

      SimpleLog.setDefaultInstance(log);
      SimpleLogger logger = new SimpleLogger(getClass());
      logger.fatal("Testing Fatal");

      Field logField = SimpleLogger.class.getDeclaredField("log");
      logField.setAccessible(true);
      Object loggersLog = logField.get(logger);
      assertEquals("logger.log", log, loggersLog);

      String[] expectedOutputLineParts = {"   |main|TestOfDefaultInstanceChange|Testing Fatal"};
      checkOutput(expectedOutputLineParts, loggerOutput);
//      checkOutput(new String[0], bytesOnErr);
      System.setErr(originalErr);
      if (originalDefaultInstance != null)
         SimpleLog.setDefaultInstance(originalDefaultInstance);
   }

   /**
    * Tests that, when the default SimpleLog instance is changed and there was already an existing
    * default instance that had been used to create loggers, an error message is written to
    * System.err and the setting of the default instance still works.
    */
   public void
   testSettingOfDefaultInstanceAfterInitialisation()
   throws Exception
   {
      // Initialise the default instance and create a logger:
      new SimpleLogger(getClass());

      Field defaultInstanceField = SimpleLog.class.getDeclaredField("defaultInstance");
      defaultInstanceField.setAccessible(true);
      SimpleLog originalDefaultInstance = (SimpleLog) defaultInstanceField.get(null);

      PrintStream originalErr = System.err;
      ByteArrayOutputStream bytesOnErr = new ByteArrayOutputStream(1024);
      System.setErr(new PrintStream(bytesOnErr, true));

      ByteArrayOutputStream loggerOutput = new ByteArrayOutputStream(1024);

      Properties properties = new Properties();
      SimpleLog log = new SimpleLog(properties);
      log.setWriter(new PrintWriter(loggerOutput, true));
      SimpleLog.setDefaultInstance(log);

      SimpleLogger logger = new SimpleLogger(getClass());
      logger.fatal("Testing Fatal");

      Field logField = SimpleLogger.class.getDeclaredField("log");
      logField.setAccessible(true);
      Object loggersLog = logField.get(logger);
      assertEquals("logger.log", log, loggersLog);

      String[] expectedOutputLineParts = {"   |main|TestOfDefaultInstanceChange|Testing Fatal"};
      checkOutput(expectedOutputLineParts, loggerOutput);

      String[] expectedErrorLineParts =
         {"", "The default SimpleLog instance is being replaced, but has already been used to create SimpleLoggers.", ""};
      checkOutput(expectedErrorLineParts, bytesOnErr);

      System.setErr(originalErr);
      SimpleLog.setDefaultInstance(originalDefaultInstance);
   }

   protected void
   checkOutput(String[] expectedOutputLineParts, ByteArrayOutputStream outputStream)
   throws IOException
   {
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

   /**
    * Returns a test suite that will automatically run all test methods in this
    * class beginning with "test".
    */
   public static TestSuite
   suite()
   {
      return new TestSuite(TestOfDefaultInstanceChange.class);
   }
}