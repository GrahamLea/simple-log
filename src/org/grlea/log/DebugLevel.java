package org.grlea.log;

// $Id: DebugLevel.java,v 1.1 2004-12-13 12:19:30 grlea Exp $
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

/**
 * <p>A Java enum defining all possible debug levels. There are seven levels, from "Fatal" (most
 * important) to "Ludicrous" (most verbose).</p>
 * <p>Note that tracing is not related to the debug levels, as tracing is controlled
 * independently.</p>
 *
 * @version $Revision: 1.1 $
 * @author $Author: grlea $
 */
public final class
DebugLevel
{
   /**
    * The "Fatal" level. This should be used when an unexpected error occurs that is serious enough
    * to cause or warrant the halting of the current process or even the whole application.
    */
   public static final DebugLevel L1_FATAL = new DebugLevel(1);

   /**
    * The "Error" level. This should be used when an unexpected error occurs that cannot be
    * recovered from.
    */
   public static final DebugLevel L2_ERROR = new DebugLevel(2);

   /**
    * The "Warning" level. This should be used when an unexpected error occurs that CAN be
    * recovered from.
    */
   public static final DebugLevel L3_WARN = new DebugLevel(3);

   /**
    * The "Info" level. This should be used for reporting information to the end user.
    */
   public static final DebugLevel L4_INFO = new DebugLevel(4);

   /**
    * The "Debug" level. This should be used for the most basic debugging statements.
    */
   public static final DebugLevel L5_DEBUG = new DebugLevel(5);

   /**
    * The "Verbose" level. This should be used for debugging statements that produce a large amount
    * of output or that don't need to be seen when performing high-level debugging.
    */
   public static final DebugLevel L6_VERBOSE = new DebugLevel(6);

   /**
    * The "Ludicrous" level. This should be used for debugging statements that produce a
    * ridiculously welath of output, e.g. printing a few lines for every pixel in an image.
    */
   public static final DebugLevel L7_LUDICROUS = new DebugLevel(7);

   /**
    * The level of this particular <code>DebugLevel</code> instance.
    */
   private final int level;

   /**
    * Creates a new <code>DebugLevel</code> with the given level. This is private to preven external
    * instantiation.
    *
    * @param level the leel for the news <code>DebugLevel</code>.
    */
   private
   DebugLevel(int level)
   {
      this.level = level;
   }

   /**
    * Returns <code>true</code> if a {@link SimpleLogger} that has <b>this</b> level should log a
    * message that has the <b>given</b> level.
    *
    * @param logLevel the level of the message to be considered
    *
    * @return <code>true</code> if the message should be logged, <code>false</code> if it should be
    * ignored.
    */
   boolean
   shouldLog(DebugLevel logLevel)
   {
      return logLevel.level <= this.level;
   }

   /**
    * Returns the <code>DebugLevel</code> object associated with the given level. If the given level
    * is above or below the defined levels, the closest level will be returned.
    *
    * @param level the level value whose corresponding <code>DebugLevel</code> is to be returned.
    *
    * @return The matching or closest matching <code>DebugLevel</code>.
    */
   public static DebugLevel
   fromInt(int level)
   {
      switch (level)
      {
         case 1: return L1_FATAL;
         case 2: return L2_ERROR;
         case 3: return L3_WARN;
         case 4: return L4_INFO;
         case 5: return L5_DEBUG;
         case 6: return L6_VERBOSE;
         case 7: return L7_LUDICROUS;

         default:
            if (level < 1)
               return L1_FATAL;
            else
               return L7_LUDICROUS;
      }
   }

   /**
    * Returns this <code>DebugLevel</code>'s level.
    */
   public int
   hashCode()
   {
      return level;
   }

   /**
    * Returns <code>true</code> if the given object is a <code>DebugLevel</code> with the same level
    * value as this <code>DebugLevel</code>. 
    */
   public boolean
   equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null || !(o.getClass().equals(this.getClass())))
         return false;

      return this.level == ((DebugLevel) o).level;
   }
}