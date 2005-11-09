package org.grlea.log.test.rollover;

// $Id: TestOfTimeOfDayRolloverStrategy.java,v 1.1 2005-11-09 21:58:16 grlea Exp $
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


import org.grlea.log.rollover.TimeOfDayRolloverStrategy;

import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * <p>Tests the public interface of {@link TimeOfDayRolloverStrategy}.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfTimeOfDayRolloverStrategy
extends TestCase
{
   static final String KEY_ROLLOVER_TIME = "simplelog.rollover.timeOfDay.time";

   static final String KEY_TIMEZONE = "simplelog.rollover.timeOfDay.timezone";

   private TimeOfDayRolloverStrategy rollover;

   public
   TestOfTimeOfDayRolloverStrategy(String name)
   {
      // Standard TestCase constructor. You shouldn't edit this.
      super(name);
   }

   protected void
   setUp()
   {
      rollover = new TimeOfDayRolloverStrategy(TimeZone.getDefault(), 0, 0);
   }

   protected void
   tearDown()
   {
      rollover = null;
   }

   public void
   testNotDueYet()
   throws Exception
   {
      Calendar calendar = createCalendar(60);
      rollover.setRolloverTime(TimeZone.getDefault(), getHour(calendar), getMinute(calendar));

      Date fileCreationTime = createCalendar(0, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);
   }

   public void
   testDueAfterMidnight()
   throws Exception
   {
      rollover.setRolloverTime(TimeZone.getDefault(), 0, 1);

      Date fileCreationTime = new Date(createCalendar(0, 1).getTime().getTime());
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);
   }

   public void
   testDueNow()
   throws Exception
   {
      Calendar calendar = createCalendar(-60);
      rollover.setRolloverTime(TimeZone.getDefault(), getHour(calendar), getMinute(calendar));

      Date fileCreationTime = createCalendar(0, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);
   }

   public void
   testDueBeforeMidnight()
   throws Exception
   {
      rollover.setRolloverTime(TimeZone.getDefault(), 23, 59);

      Date fileCreationTime = createCalendar(0, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);
   }

   public void
   testDueManyDaysAgo()
   throws Exception
   {
      rollover.setRolloverTime(TimeZone.getDefault(), 0, 0);

      Date fileCreationTime = createCalendar(24 * -7, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);

      rollover.setRolloverTime(TimeZone.getDefault(), 23, 59);

      rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);
   }

   public void
   _testNotDueAndThenDue()
   throws Exception
   {
      // Set the rollover time 60 seconds in the future
      Calendar calendar = createCalendar(60);
      rollover.setRolloverTime(TimeZone.getDefault(), getHour(calendar), getMinute(calendar));

      // Test
      Date fileCreationTime = createCalendar(0, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);

      // Sleep for sixty-two seconds
      System.out.println("Sleeping for 62 seconds...");
      Thread.sleep(62 * 1000);

      // Test again
      rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);
   }

   public void
   _testAnotherTimezone()
   throws Exception
   {
      // Create a TimeZone with a different offset to the default
      TimeZone defaultTimezone = TimeZone.getDefault();
      int defaultZoneOffset = defaultTimezone.getOffset(System.currentTimeMillis());
      int testZoneOffset = (defaultZoneOffset == 0) ? 1 : 0;
      TimeZone testZone = new SimpleTimeZone(testZoneOffset, "TEST");

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

      // Print current date
//      Date currentTime = new Date();
//      System.out.println("Current time:");
//      printDate(currentTime, dateFormat, defaultTimezone, testZone);

      // timeDifference is the value to add to local a default zone time to find the test zone time
      int timeDifference = testZoneOffset - defaultZoneOffset;
      int oneHour = 1000 * 60 * 60;
      int timeDifferenceHours = timeDifference / oneHour;
      int timeDifferenceMinutes = timeDifference % oneHour;
//      System.out.println("timeDifferenceHours = " + timeDifferenceHours);
//      System.out.println("timeDifferenceMinutes = " + timeDifferenceMinutes);

      // Get a calendar in local time
      Calendar calendar = createCalendar(60);
//      System.out.println("One minute from now:");
//      printDate(calendar.getTime(), dateFormat, defaultTimezone, testZone);

      // Set the rollover time using the time difference
      int rolloverHour = getHour(calendar) + timeDifferenceHours;
      if (rolloverHour < 0)
         rolloverHour += 24;
      if (rolloverHour > 23)
         rolloverHour -= 24;

      int rolloverMinute = getMinute(calendar) + timeDifferenceMinutes;
      if (rolloverMinute < 0)
         rolloverMinute += 24;
      if (rolloverMinute > 59)
         rolloverMinute -= 60;

      rollover.setRolloverTime(testZone, rolloverHour, rolloverMinute);

      // File creation time is
      Date fileCreationTime = createCalendar(0, 0).getTime();

//      System.out.println("File creation:");
//      printDate(fileCreationTime, dateFormat, defaultTimezone, testZone);

      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);

      System.out.println("Sleeping for 62 seconds...");
      Thread.sleep(62 * 1000);

      rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);
   }

   public void
   testResultImmediatelyAfterRollover()
   {
      Calendar calendar = createCalendar(-5);
      rollover.setRolloverTime(TimeZone.getDefault(), getHour(calendar), getMinute(calendar));

      Date fileCreationTime = createCalendar(0, 0).getTime();
      boolean rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", true, rolloverNow);

      // Date of newly created file:
      fileCreationTime = new Date();
      rolloverNow = rollover.rolloverNow(fileCreationTime, 0);
      assertEquals("rolloverNow", false, rolloverNow);
   }

   public void
   testConfigure()
   throws Exception
   {
      Properties properties = new Properties();

      Field timezoneField = TimeOfDayRolloverStrategy.class.getDeclaredField("timeZone");
      Field hourField = TimeOfDayRolloverStrategy.class.getDeclaredField("hour");
      Field minuteField = TimeOfDayRolloverStrategy.class.getDeclaredField("minute");

      timezoneField.setAccessible(true);
      hourField.setAccessible(true);
      minuteField.setAccessible(true);

      // default:
      TimeZone defaultTimeZone = TimeZone.getDefault();
      String defaultTimeZoneId = defaultTimeZone.getID();
      configureTest(properties, timezoneField, hourField, minuteField,
                    defaultTimeZoneId, 0, 0);

      // time:
      properties.setProperty(KEY_ROLLOVER_TIME, "1:00");
      configureTest(properties, timezoneField, hourField, minuteField,
                    defaultTimeZoneId, 1, 0);

      // time:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:01");
      configureTest(properties, timezoneField, hourField, minuteField,
                    defaultTimeZoneId, 0, 1);

      // largest time:
      properties.setProperty(KEY_ROLLOVER_TIME, "23:59");
      configureTest(properties, timezoneField, hourField, minuteField,
                    defaultTimeZoneId, 23, 59);

      // hours too large:
      properties.setProperty(KEY_ROLLOVER_TIME, "24:59");
      try
      {
         configureTest(properties, timezoneField, hourField, minuteField,
                       defaultTimeZoneId, 24, 59);
         fail("IOException expected");
      }
      catch (IOException e)
      {}

      // minutes too large:
      properties.setProperty(KEY_ROLLOVER_TIME, "11:60");
      try
      {
         configureTest(properties, timezoneField, hourField, minuteField,
                       defaultTimeZoneId, 11, 60);
         fail("IOException expected");
      }
      catch (IOException e)
      {}

      // preceding 0 okay:
      properties.setProperty(KEY_ROLLOVER_TIME, "02:10");
      configureTest(properties, timezoneField, hourField, minuteField,
                    defaultTimeZoneId, 2, 10);

      // alpha:
      properties.setProperty(KEY_ROLLOVER_TIME, "a:12");
      try
      {
         configureTest(properties, timezoneField, hourField, minuteField,
                       defaultTimeZoneId, 0, 12);
         fail("IOException expected");
      }
      catch (IOException e)
      {}

      // test all valid values:
      for (int hour = 0; hour < 24; hour++)
      {
         for (int minute = 0; minute < 60; minute++)
         {
            properties.setProperty(KEY_ROLLOVER_TIME, hour + ":" + (minute < 10 ? "0" : "") + minute);
            configureTest(properties, timezoneField, hourField, minuteField,
                          defaultTimeZoneId, hour, minute);
         }
      }

      // timezone GMT:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "GMT");
      configureTest(properties, timezoneField, hourField, minuteField, "GMT", 0, 0);

      // timezone GMT and time:
      properties.setProperty(KEY_ROLLOVER_TIME, "10:45");
      properties.setProperty(KEY_TIMEZONE, "GMT");
      configureTest(properties, timezoneField, hourField, minuteField, "GMT", 10, 45);

      // timezone Australia/Adelaide:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "Australia/Adelaide");
      configureTest(properties, timezoneField, hourField, minuteField, "Australia/Adelaide", 0, 0);

      // timezone GMT offset:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "GMT+5");
      configureTest(properties, timezoneField, hourField, minuteField, "GMT+05:00", 0, 0);

      // timezone GMT offset:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "GMT-10");
      configureTest(properties, timezoneField, hourField, minuteField, "GMT-10:00", 0, 0);

      // timezone GMT offset:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "GMT+0:30");
      configureTest(properties, timezoneField, hourField, minuteField, "GMT+00:30", 0, 0);

      // invalid timezone:
      properties.setProperty(KEY_ROLLOVER_TIME, "0:00");
      properties.setProperty(KEY_TIMEZONE, "Wonka/BubblePop Room");
      try
      {
         configureTest(properties, timezoneField, hourField, minuteField, "Wonka/BubblePop Room", 0, 0);
         fail("IOException expected");
      }
      catch (IOException e)
      {}
   }

   private void configureTest(Properties properties, Field timezoneField, Field hourField,
                              Field minuteField, String expectedTimezoneId, int expectedHour,
                              int expectedMinute)
   throws IOException, IllegalAccessException
   {
      rollover.configure(properties);
      TimeZone timezone = (TimeZone) timezoneField.get(rollover);
      int hour = hourField.getInt(rollover);
      int minute = minuteField.getInt(rollover);
      assertEquals("timezone", expectedTimezoneId, timezone.getID());
      assertEquals("hour", expectedHour, hour);
      assertEquals("minute", expectedMinute, minute);
   }

   private void
   printDate(Date time, SimpleDateFormat dateFormat, TimeZone defaultTimezone, TimeZone testZone)
   {
      dateFormat.setTimeZone(defaultTimezone);
      System.out.println("Default Zone: " + dateFormat.format(time));
      dateFormat.setTimeZone(testZone);
      System.out.println("Test Zone:    " + dateFormat.format(time));
   }

   private Calendar
   createCalendar(int hour, int minute)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      calendar.set(Calendar.MINUTE, minute);
      calendar.set(Calendar.SECOND, 0);
      return calendar;
   }

   private Calendar
   createCalendar(int differenceSeconds)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date(System.currentTimeMillis() + (differenceSeconds * 1000)));
      return calendar;
   }

   private int
   getHour(Calendar calendar)
   {
      return calendar.get(Calendar.HOUR_OF_DAY);
   }

   private int
   getMinute(Calendar calendar)
   {
      return calendar.get(Calendar.MINUTE);
   }
}