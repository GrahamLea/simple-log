package org.grlea.log.rollover;

// $Id: TestOfRolloverManager.java,v 1.2 2006-07-13 12:44:53 grlea Exp $
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


import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * <p>Tests the public interface of {@link RolloverManager}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
TestOfRolloverManager
extends TestCase
{
   private static final String KEY_ROLLOVER = "simplelog.rollover";

   public
   TestOfRolloverManager(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   protected void
   setUp()
   {
   }

   protected void
   tearDown()
   {
   }

   public void
   testFileSizeStrategy()
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, "fileSize");
      RolloverManager rollover = new RolloverManager(properties, null);
      RolloverStrategy strategy = rollover.getStrategy();
      assertEquals(FileSizeRolloverStrategy.class, strategy.getClass());
   }

   public void
   testTimeOfDayStrategy()
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, "timeOfDay");
      RolloverManager rollover = new RolloverManager(properties, null);
      RolloverStrategy strategy = rollover.getStrategy();
      assertEquals(TimeOfDayRolloverStrategy.class, strategy.getClass());
   }

   public void
   testFileSizeStrategyConfigured()
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, "fileSize");
      properties.setProperty(TestOfFileSizeRolloverStrategy.FILE_SIZE_KEY, "120K");
      RolloverManager rollover = new RolloverManager(properties, null);
      FileSizeRolloverStrategy strategy = (FileSizeRolloverStrategy) rollover.getStrategy();
      assertEquals(120 * 1024, strategy.getRolloverSize());
   }

   public void
   testTimeOfDayStrategyConfigured()
   throws Exception
   {
      Field timezoneField = TimeOfDayRolloverStrategy.class.getDeclaredField("timeZone");
      Field hourField = TimeOfDayRolloverStrategy.class.getDeclaredField("hour");
      Field minuteField = TimeOfDayRolloverStrategy.class.getDeclaredField("minute");

      timezoneField.setAccessible(true);
      hourField.setAccessible(true);
      minuteField.setAccessible(true);

      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, "timeOfDay");
      properties.setProperty(TestOfTimeOfDayRolloverStrategy.KEY_ROLLOVER_TIME, "8:30");
      properties.setProperty(TestOfTimeOfDayRolloverStrategy.KEY_TIMEZONE, "Australia/Perth");
      RolloverManager rollover = new RolloverManager(properties, null);
      TimeOfDayRolloverStrategy strategy = (TimeOfDayRolloverStrategy) rollover.getStrategy();
      assertEquals(8, hourField.getInt(strategy));
      assertEquals(30, minuteField.getInt(strategy));
      assertEquals("Australia/Perth", ((TimeZone) timezoneField.get(strategy)).getID());
   }


   public void
   testNamedStrategy()
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, TestRolloverStrategy.class.getName());
      RolloverManager rollover = new RolloverManager(properties, null);
      RolloverStrategy strategy = rollover.getStrategy();
      assertEquals(TestRolloverStrategy.class, strategy.getClass());
   }

   public void
   testConfigureExceptionCommunicated()
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, ExceptionThrowingTestRolloverStrategy.class.getName());
      try
      {
         new RolloverManager(properties, null);
         fail("IOException expected");
      }
      catch (IOException e)
      {}
   }

   public void
   testNullStrategyNotAllowed()
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      try
      {
         new RolloverManager(properties, null);
         fail("IOException expected");
      }
      catch (IOException e)
      {}
   }

   public void
   testEmptyStrategyNotAllowed()
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.logFile", "foo.log");

      properties.setProperty(KEY_ROLLOVER, " ");
      try
      {
         new RolloverManager(properties, null);
         fail("IOException expected");
      }
      catch (IOException e)
      {}
   }

   public void
   testMissingActiveFileNotAllowed()
   {
      Properties properties = new Properties();
      properties.setProperty(KEY_ROLLOVER, "fileSize");
      try
      {
         new RolloverManager(properties, null);
         fail("IOException expected");
      }
      catch (IOException e)
      {}
   }

   public static class
   TestRolloverStrategy
   implements RolloverStrategy
   {
      private boolean throwExceptionFromConfigure;

      public
      TestRolloverStrategy()
      {
         this(false);
      }

      public
      TestRolloverStrategy(boolean throwExceptionFromConfigure)
      {
         this.throwExceptionFromConfigure = throwExceptionFromConfigure;
      }

      public void
      configure(Map properties)
      throws IOException
      {
         if (throwExceptionFromConfigure)
         {
            throw new IOException("Test Exception");
         }
      }

      public boolean
      rolloverNow(Date fileCreated, long fileLength)
      {
         return false;
      }
   }

   public static class
   ExceptionThrowingTestRolloverStrategy
   extends TestRolloverStrategy
   {
      public
      ExceptionThrowingTestRolloverStrategy()
      {
         super(true);
      }
   }
}