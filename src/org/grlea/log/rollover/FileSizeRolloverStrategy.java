package org.grlea.log.rollover;

// $Id: FileSizeRolloverStrategy.java,v 1.2 2005-11-11 11:36:40 grlea Exp $
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
import java.util.Date;
import java.util.Map;

/**
 * <p>A {@link RolloverStrategy} that requests rollover when the log file reaches a specified size.
 * Note that, because the strategy is only consulted at intervals, log files will always grow to
 * slightly more than the specified size before being rolled.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
FileSizeRolloverStrategy
implements RolloverStrategy
{
   /** The key of the property specifying the file size at which to roll over. */
   private static final String KEY_FILE_SIZE = "simplelog.rollover.fileSize.size";

   /** The default vaue for the file size. */
   private static final String FILE_SIZE_DEFAULT = "100M";

   private static final char[] factorUnits = {'B', 'K', 'M', 'G', 'T'};

   /** The file size at which to rollover. */
   private long rolloverSize;

   /** Whether the current file size was set programatically, rather than by a call to configure. */
   private boolean rolloverSizeSetProgramatically = false;

   /**
    * <p>Creates a new <code>FileSizeRolloverStrategy</code>.</p>
    *
    * <p>This constructor is only intended to be used by {@link RolloverManager}.</p>
    */
   FileSizeRolloverStrategy()
   {
      this.rolloverSize = -1;
   }

   /**
    * Creates a new <code>FileSizeRolloverStrategy</code> that will request log files be rolled
    * when they reach the specified size. The rollover file size set by using this method will not
    * be changed by calls to {@link #configure}.
    *
    * @param rolloverSize the size (in bytes) at which files shoud be rolled over.
    *
    * @throws IllegalArgumentException if rolloverSize is less than <code>1</code>.
    */
   public
   FileSizeRolloverStrategy(long rolloverSize)
   {
      setRolloverSize(rolloverSize);
   }

   public void
   configure(Map properties)
   throws IOException
   {
      if (rolloverSizeSetProgramatically)
         return;

      String fileSizeString = (String) properties.get(KEY_FILE_SIZE);
      if (fileSizeString == null)
         fileSizeString = FILE_SIZE_DEFAULT;
      else
         fileSizeString = fileSizeString.trim();

      if (fileSizeString.length() == 0)
         fileSizeString = FILE_SIZE_DEFAULT;

      long fileSize = decodeFileSize(fileSizeString);
      setRolloverSizeInernal(fileSize);
   }

   /**
    * Converts the given string from a value and magnitude expression (e.g. 120K) to a raw file size
    * in bytes.
    *
    * @param fileSizeString a string containing a number followed by a single letter, being one of
    * the valid single-letter abbreviations for base-2 magnitude, i.e. B, K, M, G or T. Case is
    * disregarded.
    *
    * @return The value contained in the given string as a raw number of bytes
    *
    * @throws IOException if the given string does not match the specified constraints
    */
   private long
   decodeFileSize(String fileSizeString)
   throws IOException
   {
//      assert fileSizeString != null;
//      assert fileSizeString.length() != 0;

      try
      {
         int lastCharacterIndex = fileSizeString.length() - 1;

         String sizeOnlyString = fileSizeString.substring(0, lastCharacterIndex);
         long fileSize = Long.parseLong(sizeOnlyString);

         char lastCharacter = fileSizeString.charAt(lastCharacterIndex);
         lastCharacter = Character.toUpperCase(lastCharacter);

         boolean factorIdentified = false;
         for (int factor = 0; !factorIdentified && (factor < factorUnits.length); factor++)
         {
            char factorAbbreviation = factorUnits[factor];
            if (lastCharacter == factorAbbreviation)
            {
               factorIdentified = true;
               fileSize = fileSize * (1L << (factor * 10));
            }
         }

         if (!factorIdentified)
         {
            throw new IOException(
               "The specified file size does not conform to the required pattern: " + fileSizeString);
         }

         return fileSize;
      }
      catch (NumberFormatException e)
      {
         // When you're writing a logging component, you can't debug stuff like this!
         throw new IOException("Specified file size does not conform to the required pattern: " +
                               fileSizeString);
      }
   }

   public boolean
   rolloverNow(Date fileCreated, long fileLength)
   {
      if (rolloverSize == -1)
         throw new IllegalStateException("FileSizeRolloverStrategy has not been configured.");

      return fileLength >= rolloverSize;
   }

   /**
    * Returns the size at which files are currenty being rolled over.
    */
   public long
   getRolloverSize()
   {
      return rolloverSize;
   }

   /**
    * Sets the size at which files should be rolled over. One the rollover file size has been set
    * using this method, it will not be changed by calls to {@link #configure}.
    *
    * @param rolloverSize the new size (in bytes) at which files should be rolled over.
    *
    * @throws IllegalArgumentException if rolloverSize is less than <code>1</code>.
    */
   public void
   setRolloverSize(long rolloverSize)
   {
      setRolloverSizeInernal(rolloverSize);
      this.rolloverSizeSetProgramatically = true;
   }

   /**
    * Sets the size at which files should be rolled over. This method does not set
    * {@link #rolloverSizeSetProgramatically}.
    *
    * @param rolloverSize the new size (in bytes) at which files should be rolled over.
    */
   private void
   setRolloverSizeInernal(long rolloverSize)
   {
      if (rolloverSize < 1)
      {
         throw new IllegalArgumentException("rolloverSize must be > 0");
      }
      this.rolloverSize = rolloverSize;
   }
}