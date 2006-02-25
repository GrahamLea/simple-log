package org.grlea.log.rollover;

// $Id: TimeOfDayRolloverStrategy.java,v 1.3 2006-02-25 15:24:50 grlea Exp $
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A {@link RolloverStrategy} that requests rollover at a specific time each day.
 * Note that, because the strategy is only consulted at intervals, log files will usually contain
 * records that are slightly past the rollover time (depending on the rollover interval).</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.3 $
 */
class
TimeOfDayRolloverStrategy
implements RolloverStrategy
{
   /** The key of the property specifying the time of day at which to roll over. */
   private static final String KEY_ROLLOVER_TIME = "simplelog.rollover.timeOfDay.time";

   /** The default value for the rollover time. */
   private static final String ROLLOVER_TIME_DEFAULT = "0:00";

   /** The key of the property specifying the timezone in which the rollover time is specified. */
   private static final String KEY_TIMEZONE = "simplelog.rollover.timeOfDay.timezone";

   /**
    * The time zone in which the time is being specified
    */
   private TimeZone timeZone;

   /**
    * The hour of the day at which to roll over (in 24 hour time, where 0 = midnight)
    */
   private int hour;

   /**
    * The minute of the hour to roll over
    */
   private int minute;

   /**
    * Whether the current rollover time was set programatically, rather than by calling configure.
    */
   private boolean timeSetProgramatically = false;

   /**
    * <p>Creates a new <code>SetTimeRolloverStrategy</code>.</p>
    *
    * <p>This constructor is only intended to be used by {@link RolloverManager}.</p>
    */
   TimeOfDayRolloverStrategy()
   {
      timeZone = null;
      hour = -1;
      minute = -1;
   }

   /**
    * Creates a new <code>SetTimeRolloverStrategy</code> that will request log files be rolled at
    * the specified time.
    *
    * @param timeZone the time zone in which the time is being specified
    * @param hour the hour of the day at which to roll over (in 24 hour time, where 0 = midnight)
    * @param minute the minute of the hour to roll over
    */
   public
   TimeOfDayRolloverStrategy(TimeZone timeZone, int hour, int minute)
   {
      setRolloverTime(timeZone, hour, minute);
   }

   public void
   configure(Map properties)
   throws IOException
   {
      if (timeSetProgramatically)
         return;
      
      String rolloverTime = (String) properties.get(KEY_ROLLOVER_TIME);
      if (rolloverTime == null)
         rolloverTime = ROLLOVER_TIME_DEFAULT;
      rolloverTime = rolloverTime.trim();
      if (rolloverTime.length() == 0)
         rolloverTime = ROLLOVER_TIME_DEFAULT;

      // Nice pattern this, huh? It handles 0:00 (or 00:00) through to 23:59, and nothing else.
      Pattern pattern = Pattern.compile("([01]?\\d|2[0123]):([0-5]\\d)");
      Matcher matcher = pattern.matcher(rolloverTime);
      if (!matcher.matches())
      {
         throw new IOException(
            "The specified rollover time does not conform to the required pattern: " + rolloverTime);
      }

      String hourString = matcher.group(1);
      String minuteString = matcher.group(2);

      int hour = Integer.parseInt(hourString);
      int minute = Integer.parseInt(minuteString);

      String timezoneString = (String) properties.get(KEY_TIMEZONE);
      TimeZone timeZone;
      if (timezoneString != null)
      {
         timeZone = TimeZone.getTimeZone(timezoneString);
         if (timeZone.getID().equals("GMT") && !timezoneString.equals("GMT"))
            throw new IOException("A TimeZone with the specified ID could not be created.");
      }
      else
      {
         timeZone = TimeZone.getDefault();
      }

      setRolloverTimeInternal(timeZone, hour, minute);
   }

   /**
    * Sets the time at which a rollover should be requested. Once the time has been set by this
    * method, it will not be changed by calls to {@link #configure}.
    *
    * @param timeZone the time zone in which the time is being specified
    * @param hour the hour of the day at which to roll over (in 24 hour time, where 0 = midnight)
    * @param minute the minute of the hour to roll over
    */
   public void
   setRolloverTime(TimeZone timeZone, int hour, int minute)
   {
      setRolloverTimeInternal(timeZone, hour, minute);
      this.timeSetProgramatically = true;
   }

   /**
    * Sets the time at which a rollover should be requested. This method does not set
    * {@link #timeSetProgramatically}.
    *
    * @param timeZone the time zone in which the time is being specified
    * @param hour the hour of the day at which to roll over (in 24 hour time, where 0 = midnight)
    * @param minute the minute of the hour to roll over
    */
   private void
   setRolloverTimeInternal(TimeZone timeZone, int hour, int minute)
   {
      if (timeZone == null)
      {
         throw new IllegalArgumentException("timeZone cannot be null.");
      }

      if (hour < 0 || hour > 23)
      {
         throw new IllegalArgumentException("hour must be between 0 and 23, inclusive.");
      }

      if (minute < 0 || minute > 59)
      {
         throw new IllegalArgumentException("minute must be between 0 and 59, inclusive.");
      }

      this.timeZone = timeZone;
      this.hour = hour;
      this.minute = minute;
   }

   public boolean
   rolloverNow(Date fileCreated, long fileLength)
   {
      if (timeZone == null)
      {
         throw new IllegalStateException("TimeOfDayRolloverStrategy has not been configured.");
      }

      // Calculate the next rollover time
      Calendar rolloverCalendar = Calendar.getInstance(timeZone);
      rolloverCalendar.setTime(fileCreated);
      rolloverCalendar.set(Calendar.HOUR_OF_DAY, hour);
      rolloverCalendar.set(Calendar.MINUTE, minute);
      rolloverCalendar.set(Calendar.SECOND, 0);
      rolloverCalendar.set(Calendar.MILLISECOND, 0);
      long nextRolloverTime = rolloverCalendar.getTime().getTime();

      while (nextRolloverTime <= fileCreated.getTime())
      {
         int day = rolloverCalendar.get(Calendar.DAY_OF_MONTH);
         rolloverCalendar.set(Calendar.DAY_OF_MONTH, day + 1);
         nextRolloverTime = rolloverCalendar.getTime().getTime();
      }

//      System.out.println("nextRolloverTime = " +
//                         new SimpleDateFormat("dd/MM HH:mm").format(new Date(nextRolloverTime)));

      // If now < rolloverTime, not ready to rollover yet
      long now = System.currentTimeMillis();
      if (now < nextRolloverTime)
      {
         return false;
      }
      else
      {
         return true;
      }
   }
}