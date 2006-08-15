package org.grlea.log.test;

// $Id: TestOfConsoleOutput.java,v 1.1 2006-08-15 10:29:45 grlea Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Properties;

/**
 * <p>Test setting the output of a {@link SimpleLog}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfConsoleOutput
extends TestCase
{
   public
   TestOfConsoleOutput(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   public void
   testDefaultOutputToSystemErr()
   throws Exception
   {
      PrintStream originalOut = System.out;
      PrintStream originalErr = System.err;
      ByteArrayOutputStream bytesOnErr = new ByteArrayOutputStream(1024);
      ByteArrayOutputStream bytesOnOut = new ByteArrayOutputStream(1024);
      System.setOut(new PrintStream(bytesOnOut));
      System.setErr(new PrintStream(bytesOnErr));

      Properties properties = new Properties();
      SimpleLog log = new SimpleLog(properties);
      new SimpleLogger(log, getClass()).fatal("Testing Fatal");

      String[] expectedOutputLineParts = {"   |main|TestOfConsoleOutput|Testing Fatal"};
      checkOutput(new String[0], bytesOnOut);
      checkOutput(expectedOutputLineParts, bytesOnErr);
      System.setErr(originalOut);
      System.setErr(originalErr);
   }

   public void
   testOutputToSystemOut()
   throws Exception
   {
      PrintStream originalOut = System.out;
      PrintStream originalErr = System.err;
      ByteArrayOutputStream bytesOnErr = new ByteArrayOutputStream(1024);
      ByteArrayOutputStream bytesOnOut = new ByteArrayOutputStream(1024);
      System.setOut(new PrintStream(bytesOnOut));
      System.setErr(new PrintStream(bytesOnErr));

      Properties properties = new Properties();
      properties.setProperty("simplelog.console", "System.out");
      SimpleLog log = new SimpleLog(properties);
      new SimpleLogger(log, getClass()).fatal("Testing Fatal");

      String[] expectedOutputLineParts = {"   |main|TestOfConsoleOutput|Testing Fatal"};
      checkOutput(expectedOutputLineParts, bytesOnOut);
      checkOutput(new String[0], bytesOnErr);
      System.setErr(originalOut);
      System.setErr(originalErr);
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
      return new TestSuite(TestOfConsoleOutput.class);
   }
}