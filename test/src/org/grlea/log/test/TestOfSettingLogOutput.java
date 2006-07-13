package org.grlea.log.test;

// $Id: TestOfSettingLogOutput.java,v 1.3 2006-07-13 12:44:57 grlea Exp $
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


import org.grlea.log.SimpleLog;
import org.grlea.log.SimpleLogger;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * <p>Test setting the output of a {@link SimpleLog}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.3 $
 */
public class
TestOfSettingLogOutput
extends TestCase
{
   private ByteArrayOutputStream outputStream;
   private SimpleLog log;
   private Properties properties;
   private PrintWriter writer;

   public
   TestOfSettingLogOutput(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   protected void
   setUp()
   {
      properties = new Properties();
      log = new SimpleLog(properties);

      outputStream = new ByteArrayOutputStream(512);
      writer = new PrintWriter(outputStream, true);
      log.setWriter(writer);
   }

   protected void
   tearDown()
   {
      log = null;
      properties = null;
      outputStream = null;
      writer = null;
   }

   public void
   testSettingLogOutput()
   throws Exception
   {
      new SimpleLogger(log, getClass()).fatal("Testing Fatal");

      String[] expectedOutputLineParts = {"   |main|TestOfSettingLogOutput|Testing Fatal"};
      checkOutput(expectedOutputLineParts);
   }


   public void
   testReloadingDoesntOverrideLogOutput()
   throws Exception
   {
      assertEquals("SimpleLog.writer", writer, log.getWriter());

      new SimpleLogger(log, getClass()).fatal("Testing Fatal");

      properties.setProperty("simplelog.logFile", "TestOfSettingOutput.log");
      log.reloadProperties();

      String[] expectedOutputLineParts = {"   |main|TestOfSettingLogOutput|Testing Fatal"};
      checkOutput(expectedOutputLineParts);

      assertEquals("SimpleLog.writer", writer, log.getWriter());
   }

   protected void
   checkOutput(String[] expectedOutputLineParts)
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
      return new TestSuite(TestOfSettingLogOutput.class);
   }
}