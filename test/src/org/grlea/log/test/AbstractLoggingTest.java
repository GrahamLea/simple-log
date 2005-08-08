package org.grlea.log.test;

// $Id: AbstractLoggingTest.java,v 1.1 2005-08-08 14:13:43 grlea Exp $
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

import junit.framework.TestCase;

import java.lang.Object;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Properties;

/**
 * <p></p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public abstract class
AbstractLoggingTest
extends TestCase
{
   private ByteArrayOutputStream outputStream;
   protected SimpleLog log;
   protected Properties properties;

   public
   AbstractLoggingTest(String name)
   {
      super(name);
   }

   protected void
   setUp()
   {
      outputStream = new ByteArrayOutputStream(512);
      System.setErr(new PrintStream(outputStream));

      properties = new Properties();
      log = new SimpleLog(properties);
   }

   protected void
   tearDown()
   {
      log = null;
      properties = null;
      outputStream = null;
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
}