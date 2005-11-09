package org.grlea.log.test.rollover;

// $Id: TestOfFileSizeRolloverStrategy.java,v 1.1 2005-11-09 21:58:15 grlea Exp $
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


import org.grlea.log.rollover.FileSizeRolloverStrategy;

import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * <p>Tests the public interface of {@link FileSizeRolloverStrategy}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfFileSizeRolloverStrategy
extends TestCase
{
   private static final long TEST_ROLLOVER_SIZE = 8 * 1024L;

   static final String FILE_SIZE_KEY = "simplelog.rollover.fileSize.size";

   private FileSizeRolloverStrategy rollover;

   public
   TestOfFileSizeRolloverStrategy(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   protected void
   setUp()
   {
      rollover = new FileSizeRolloverStrategy(TEST_ROLLOVER_SIZE);
   }

   protected void
   tearDown()
   {
      rollover = null;
   }

   public void
   testProgression()
   throws Exception
   {
      long step = TEST_ROLLOVER_SIZE / 13;
      long size = -step;
      Date creationDate = new Date();

      do
      {
         size += step;
         boolean rolloverNow = rollover.rolloverNow(creationDate, size);
         boolean expectedResult = size > TEST_ROLLOVER_SIZE;

         assertEquals("rolloverNow", expectedResult, rolloverNow);
      }
      while (size <= TEST_ROLLOVER_SIZE);
   }

   public void
   testBoundaries()
   {
      assertEquals("rollover one byte early", false,
                   rollover.rolloverNow(new Date(), TEST_ROLLOVER_SIZE - 1));
      assertEquals("rollover on exact length", true,
                   rollover.rolloverNow(new Date(), TEST_ROLLOVER_SIZE));
      assertEquals("rollover one byte after", true,
                   rollover.rolloverNow(new Date(), TEST_ROLLOVER_SIZE + 1));
   }

   public void
   testLongValues()
   {
      long rolloverSize = ((long) Integer.MAX_VALUE) << 4;
      rollover.setRolloverSize(rolloverSize);
      assertEquals("rollover one byte early", false,
                   rollover.rolloverNow(new Date(), rolloverSize - 1));
      assertEquals("rollover on exact length", true,
                   rollover.rolloverNow(new Date(), rolloverSize));
      assertEquals("rollover one byte after", true,
                   rollover.rolloverNow(new Date(), rolloverSize + 1));
   }

   public void
   testSettingIllegalValue()
   {
      rollover.setRolloverSize(1);

      try
      {
         rollover.setRolloverSize(0);
         fail("Exception should have been thrown");
      }
      catch (Exception e)
      {
      }

      try
      {
         rollover.setRolloverSize(-1);
         fail("Exception should have been thrown");
      }
      catch (Exception e)
      {
      }
   }

   public void
   testConfigure()
   throws Exception
   {
      Properties properties = new Properties();

      Field rolloverField = FileSizeRolloverStrategy.class.getDeclaredField("rolloverSize");
      rolloverField.setAccessible(true);

      // default (100M):
      rollover.configure(properties);
      long rolloverValue = rolloverField.getLong(rollover);
      assertEquals((long)(100 * 1024 * 1024), rolloverValue);

      // bytes:
      configureTest(properties, rolloverField, "24B", 24);

      // lower-case:
      configureTest(properties, rolloverField, "24b", 24);

      // kilobytes:
      configureTest(properties, rolloverField, "12K", 12 * 1024);

      // megabytes:
      configureTest(properties, rolloverField, "16M", 16 * 1024 * 1024);

      // gigabytes:
      configureTest(properties, rolloverField, "2G", 2L * 1024 * 1024 * 1024);

      // terabytes:
      configureTest(properties, rolloverField, "7T", 7L * 1024 * 1024 * 1024 * 1024);

      // megabytes, as bytes:
      configureTest(properties, rolloverField, (9 * 1024 * 1024) + "B", 9 * 1024 * 1024);

      // No magnitude:
      try
      {
         configureTest(properties, rolloverField, "24", 24);
         fail("IOException should have been thrown");
      }
      catch (IOException e)
      {}
   }

   private void
   configureTest(Properties properties, Field rolloverField, String stringValue, long expectedValue)
   throws IOException, IllegalAccessException
   {
      properties.setProperty(FILE_SIZE_KEY, stringValue);
      rollover.configure(properties);
      long rolloverValue = rolloverField.getLong(rollover);
      assertEquals(expectedValue, rolloverValue);
   }
}