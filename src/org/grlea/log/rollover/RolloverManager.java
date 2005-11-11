package org.grlea.log.rollover;

// $Id: RolloverManager.java,v 1.2 2005-11-11 11:36:41 grlea Exp $
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>The RolloverManager is a {@link Writer} implementation used by Simple Log to enable periodic
 * log-rolling functionality. It is a writer which writes to an "active" log file and, when told by
 * its {@link RolloverStrategy} that the file is due to be rolled over, moves the content of the
 * active log file into a new rollover log file. Sufficient mechanics are employed to ensure that
 * no log content is lost while the roll over is being conducted.</p>
 *
 * @author Graham Lea
 * @version $Revision: 1.2 $
 */
public class
RolloverManager
extends Writer
{
   /** The prefix for all special properties keys. */
   private static final String KEY_PREFIX = "simplelog.";

   /** The property key for the rollover strategy. */
   private static final String KEY_ROLLOVER_STRATEGY = KEY_PREFIX + "rollover";

   /** The rollover strategy alias for FileSizeRolloverStrategy. */
   private static final String ROLLOVER_STRATEGY_FILESIZE = "fileSize";

   /** The rollover strategy alias for TimeOfDayRolloverStrategy. */
   private static final String ROLLOVER_STRATEGY_TIMEOFDAY = "timeOfDay";

   /** The property key for the active log file name. */
   private static final String KEY_ACTIVE_LOG_FILE = KEY_PREFIX + "logFile";

   /** The property key for rolled log file names. */
   private static final String KEY_ROLLOVER_LOG_FILE = KEY_PREFIX + "rollover.filename";

   /** The property key for rolled log file names. */
   private static final String KEY_ROLLOVER_DIRECTORY = KEY_PREFIX + "rollover.directory";

   /** The property key for the log rollover check period. */
   private static final String KEY_ROLLOVER_PERIOD = KEY_PREFIX + "rollover.period";

   /** The default rollover period. */
   private static final String DEFAULT_ROLLOVER_PERIOD = "60";

   /**
    * A semaphore lock which, in concert which {@link #printerCount} is used to monitor the number
    * of printing threads and to notify a thread waiting to change the writer.
    */
   private final Object PRINTERS_SEMAPHORE = new Object();

   /** The number of threads currently executing a printing operation. */
   private int printerCount = 0;

   /**
    * Lock object used as a gate to prevent new printing threads from acquiring a printing semaphore
    * when another thread is waiting to change the writer. Basically, it is used to ensure priority
    * for the writer-changing thread.
    */
   private final Object WRITER_CHANGE_GATE = new Object();

   /** The current writer. */
   private Writer writer;

   /** The temporary writer, used while the rollover is occurring. */
   private StringWriter tempWriter = new StringWriter(1024);

   /** The rollover strategy in use. */
   private RolloverStrategy strategy;

   /** The name used to create the rollover strategy currently in use. */
   private String currentRolloverStrategyName;

   /** The active log file currently in use. */
   private File currentActiveLogFile;

   /** The directory of the current active log file. */
   private File currentActiveLogFileDirectory;

   /** The directory where rolled log files will be stored. */
   private File rolloverDirectory;

   /** The format used to construct rolled log file names. */
   private MessageFormat rolloverLogFileFormat;

   /** The read/write stream for the active log file. */
   private RandomAccessFile fileOut;

   /** Indicates whether the strategy currently in use was set programatically. */
   private boolean strategySetProgramatically = false;

   /** The time (in seconds) between checks for rollover. */
   private long rolloverPeriod;

   /** The timer used to prompt rollover to occur. */
   private Timer timer;

   /** The current unique file ID */
   private int uniqueFileId = 0;

   /** An object to which errors should be reported. */
   private ErrorReporter errorReporter;

   /**
    * Creates a new <code>RolloverManager</code>, configuring it with provided properties. The given
    * {@link ErrorReporter} object is retained and used to report errors for the life of the new
    * <code>RolloverManager</code> instance.
    *
    * @param properties properties to be used to configure the new <code>RolloverManager</code>
    *
    * @param errorReporter and object to which errors that occur while the
    * <code>RolloverManager</code> is running will be reported. May be <code>null</code>.
    *
    * @throws IOException if an error occurs while configuring the <code>RolloverManager</code> or
    * opening the files it will be logging to.
    *
    * @throws IllegalArgumentException if <code>properties</code> is <code>null</code>.
    */
   public
   RolloverManager(Properties properties, ErrorReporter errorReporter)
   throws IOException
   {
      if (properties == null)
         throw new IllegalArgumentException("properties cannot be null.");

      this.errorReporter = errorReporter;
      configure(properties);
   }

   /**
    * Configures this <code>RolloverManager</code> using the given properties.
    *
    * @param properties properties to be used to configure this <code>RolloverManager</code>
    *
    * @throws IOException if an error occurs while configuring the <code>RolloverManager</code> or
    * opening the files it will be logging to.
    */
   public void
   configure(Properties properties)
   throws IOException
   {
      configureStrategy(properties);
      configureWriter(properties);
   }

   /**
    * Configures the strategy of this <code>RolloverManager</code>.
    */
   private void
   configureStrategy(Properties properties)
   throws IOException
   {
      if (strategySetProgramatically)
      {
         return;
      }

      // Get the name.
      String rolloverStrategyString = properties.getProperty(KEY_ROLLOVER_STRATEGY);

      if (rolloverStrategyString == null)
      {
         throw new IOException("RolloverManger created, but rollover property not specified.");
      }

      rolloverStrategyString = rolloverStrategyString.trim();
      if (rolloverStrategyString.length() == 0)
      {
         throw new IOException("RolloverManger created, but rollover property not specified.");
      }

      boolean strategyChanged = !rolloverStrategyString.equals(currentRolloverStrategyName);
      RolloverStrategy newStrategy;
      if (strategyChanged)
      {
         try
         {
            if (ROLLOVER_STRATEGY_FILESIZE.equals(rolloverStrategyString))
            {
               newStrategy = new FileSizeRolloverStrategy();
            }
            else if (ROLLOVER_STRATEGY_TIMEOFDAY.equals(rolloverStrategyString))
            {
               newStrategy = new TimeOfDayRolloverStrategy();
            }
            else
            {
               try
               {
                  Class strategyClass = Class.forName(rolloverStrategyString);
                  Object strategyObject = strategyClass.newInstance();
                  if (!(strategyObject instanceof RolloverStrategy))
                  {
                     throw new IOException(strategyClass.getName() + " is not a RolloverStrategy");
                  }

                  newStrategy = (RolloverStrategy) strategyObject;
               }
               catch (ClassNotFoundException e)
               {
                  throw new IOException("Class '" + rolloverStrategyString + "' not found: " + e);
               }
               catch (InstantiationException e)
               {
                  throw new IOException("Failed to create an instance of " +
                                        rolloverStrategyString + ": " + e);
               }
               catch (IllegalAccessException e)
               {
                  throw new IOException("Failed to create an instance of " +
                                        rolloverStrategyString + ": " + e);
               }
            }
         }
         catch (IOException e)
         {
            throw new IOException("Error creating RolloverStrategy: " + e);
         }
      }
      else
      {
         newStrategy = strategy;
      }

      try
      {
         newStrategy.configure(Collections.unmodifiableMap(properties));
      }
      catch (IOException e)
      {
         throw new IOException("Error configuring RolloverStrategy: " + e);
      }

      this.strategy = newStrategy;
      currentRolloverStrategyName = rolloverStrategyString;
   }

   /**
    * Configures the writer of this <code>RolloverManager</code>.
    */
   private void
   configureWriter(Properties properties)
   throws IOException
   {
      // Active log file
      String newActiveLogFileName = properties.getProperty(KEY_ACTIVE_LOG_FILE);
      if (newActiveLogFileName == null)
         throw new IOException("RolloverManager created but no active log file name specified.");

      // Create active log file's directory
      File newActiveLogFile = new File(newActiveLogFileName.trim()).getAbsoluteFile();

      if (newActiveLogFile.isDirectory())
         throw new IOException("The specified active log file name already exists as a directory.");

      File newActiveLogFileDirectory = newActiveLogFile.getParentFile();
      if (newActiveLogFileDirectory != null)
         newActiveLogFileDirectory.mkdirs();
      else
         throw new IOException("Caanot access the active log file's parent directory.");

      // Rollover log file name pattern
      String newRolloverLogFileNamePattern = properties.getProperty(KEY_ROLLOVER_LOG_FILE);
      if (newRolloverLogFileNamePattern == null)
         newRolloverLogFileNamePattern = "{1}-" + newActiveLogFileName;

      newRolloverLogFileNamePattern = newRolloverLogFileNamePattern.trim();

      // Ensure rollover log fille name pattern compiles
      MessageFormat newRolloverLogFileNameFormat;
      try
      {
         newRolloverLogFileNameFormat = new MessageFormat(newRolloverLogFileNamePattern);
      }
      catch (IllegalArgumentException e)
      {
         throw new IOException("Illegal pattern provided for rollover log file name: " +
                               e.getMessage());
      }

      // Rollover log file directory
      String newRolloverDirectoryName = properties.getProperty(KEY_ROLLOVER_DIRECTORY);
      File newRolloverDirectory;
      if (newRolloverDirectoryName != null)
         newRolloverDirectory = new File(newRolloverDirectoryName.trim()).getAbsoluteFile();
      else
         newRolloverDirectory = newActiveLogFileDirectory;

      if (newRolloverDirectory.exists() && !newRolloverDirectory.isDirectory())
      {
         throw new IOException(
            "The location specified for storing rolled log files is not a directory: " +
            newRolloverDirectory);
      }

      // Rollover period
      String rolloverPeriodString = (String) properties.get(KEY_ROLLOVER_PERIOD);
      if (rolloverPeriodString == null)
         rolloverPeriodString = DEFAULT_ROLLOVER_PERIOD;

      rolloverPeriodString = rolloverPeriodString.trim();
      if (rolloverPeriodString.length() == 0)
         rolloverPeriodString = DEFAULT_ROLLOVER_PERIOD;

      long newRolloverPeriod;
      try
      {
         newRolloverPeriod = Long.parseLong(rolloverPeriodString);
         if (newRolloverPeriod < 1)
            throw new NumberFormatException("Must be greater than 0");
      }
      catch (NumberFormatException e)
      {
         throw new IOException("Invalid rollover period specified: " + rolloverPeriodString + " (" +
                               e.getMessage() + ")");
      }

      // Determine the current unique ID if one is required
      Format[] formats = newRolloverLogFileNameFormat.getFormatsByArgumentIndex();
      boolean uniqueIdUsedInPattern = formats.length > 1;
      if (uniqueIdUsedInPattern)
      {
         String[] filenames = newRolloverDirectory.list();
         int maximumFileId = 0;
         for (int i = 0; filenames != null && i < filenames.length; i++)
         {
            String filename = filenames[i];
            try
            {
               Object[] parameters = newRolloverLogFileNameFormat.parse(filename);
               if (parameters.length > 1 && parameters[1] != null)
               {
                  String uniqueFileIdString = (String) parameters[1];
                  int parsedFileId = Integer.parseInt(uniqueFileIdString, 16);
                  if (parsedFileId > maximumFileId)
                     maximumFileId = parsedFileId;
               }
            }
            catch (ParseException e)
            {
               // This will happen for any filenames that don't match the pattern
            }
         }
         uniqueFileId = maximumFileId + 1;
      }

      rolloverDirectory = newRolloverDirectory;
      rolloverLogFileFormat = newRolloverLogFileNameFormat;

      // Create/Open standard log file
      if (!newActiveLogFile.equals(currentActiveLogFile))
      {
         openWriter(newActiveLogFile, newActiveLogFileDirectory);
         currentActiveLogFile = newActiveLogFile;
         currentActiveLogFileDirectory = newActiveLogFileDirectory;
      }

      // Setup the timer
      if (timer == null || newRolloverPeriod != rolloverPeriod)
      {
         if (timer != null)
            timer.cancel();

         timer = new Timer(true);
         timer.schedule(new RolloverTask(), 0, newRolloverPeriod * 1000L);
         rolloverPeriod = newRolloverPeriod;
      }
   }

   /**
    * Opens the specified file as an active log file for this <code>RolloverManager</code>. This
    * will also result in the creation of a "creation date" file if necessary.
    *
    * @param newActiveLogFile the file that will be opened as the active log file
    *
    * @param activeLogFileDirectory the directory in which the active log file resides
    */
   private void
   openWriter(File newActiveLogFile, File activeLogFileDirectory)
   throws IOException
   {
      synchronized (WRITER_CHANGE_GATE)
      {
         synchronized (PRINTERS_SEMAPHORE)
         {
            if (printerCount != 0)
            {
               try
               {
                  PRINTERS_SEMAPHORE.wait();
               }
               catch (InterruptedException e)
               {
                  throw new IllegalStateException("Interrupted while attempting to configure writer");
               }
            }

            if (writer != tempWriter)
            {
               if (writer != null)
               {
                  try
                  {
                     writer.close();
                  }
                  catch (IOException e)
                  {
                     throw new IOException("Failed to close open file: " + e);
                  }
               }

               RandomAccessFile newFileOut = new RandomAccessFile(newActiveLogFile, "rwd");
               FileChannel channel = newFileOut.getChannel();
               long initialChannelSize = channel.size();
               newFileOut.seek(initialChannelSize);
               Writer newFileWriter = Channels.newWriter(newFileOut.getChannel(), "UTF-8");
               // Record the time the file was created if it is new
               storeFileCreationTimeIfNecessary(activeLogFileDirectory, newActiveLogFile);
               fileOut = newFileOut;
               writer = newFileWriter;
            }
         }
      }
   }

   /**
    * Writes the creation time (which is the current time) of the active log file to a new file if
    * necessary (i.e. if the creation file doesn't already exist).
    *
    * @param newActiveLogFile the file that will be opened as the active log file
    *
    * @param activeLogFileDirectory the directory in which the active log file resides
    */
   private void
   storeFileCreationTimeIfNecessary(File activeLogFileDirectory, File newActiveLogFile)
   throws IOException
   {
      long creationTime = System.currentTimeMillis();

      try
      {
         File creationTimeFile = getCreationTimeFile(activeLogFileDirectory, newActiveLogFile);

         // The active log file being new is determined by whether the creation time file exists
         boolean recordCreationTime = !creationTimeFile.exists();
         if (recordCreationTime)
         {
            FileWriter creationTimeFileOut = new FileWriter(creationTimeFile);
            creationTimeFileOut.write(String.valueOf(creationTime));
            creationTimeFileOut.close();
         }
      }
      catch (IOException e)
      {
         throw new IOException("Failed to write file creation time: " + e);
      }
   }

   /**
    * Reads and returns the time (in milliseconds since the 1970 epoch) that the active log file was
    * created, according to its associated creation date file.
    *
    * @return the time the active log file was created (in millis since the epoch) or -1 if the
    * creation date file is not available.
    *
    * @throws IOException if any error occurs opening or reading and interpreting the contents of
    * the file
    */
   private long
   readCreationTime()
   throws IOException
   {
      try
      {
         File creationTimeFile =
            getCreationTimeFile(currentActiveLogFileDirectory, currentActiveLogFile);

         if (!creationTimeFile.exists())
            storeFileCreationTimeIfNecessary(currentActiveLogFileDirectory, currentActiveLogFile);

         FileReader fileIn = new FileReader(creationTimeFile);
         BufferedReader in = new BufferedReader(fileIn);
         String firstLine = in.readLine();
         in.close();
         return Long.parseLong(firstLine);
      }
      catch (IOException e)
      {
         throw new IOException("Error reading creation time file: " + e);
      }
      catch (NumberFormatException e)
      {
         throw new IOException("Error reading creation time: " + e);
      }
   }

   /**
    * Returns the name of the creation date file associated with the specified active log file.
    *
    * @param newActiveLogFile the file that will be opened as the active log file
    *
    * @param activeLogFileDirectory the directory in which the active log file resides
    *
    * @return the file used to store the creation date of the active log file. It doesn't
    * necessarily exist when this method returns.
    */
   private File
   getCreationTimeFile(File activeLogFileDirectory, File newActiveLogFile)
   {
      return new File(activeLogFileDirectory, newActiveLogFile.getName() + "-CREATED");
   }


   /**
    * Closes the file resources associated with this <code>RolloverManager</code> and releases any
    * other resources used by it. Any errors occurring during this operation are captured and
    * reported to the error reporter.
    */
   public void
   close()
   {
      synchronized (WRITER_CHANGE_GATE)
      {
         synchronized (PRINTERS_SEMAPHORE)
         {
            if (printerCount != 0)
            {
               try
               {
                  PRINTERS_SEMAPHORE.wait();
               }
               catch (InterruptedException e)
               {
                  throw new IllegalStateException("Interrupted while attempting to configure writer");
               }
            }

            writer = tempWriter;
         }

         timer.cancel();

         try
         {
            writer.close();
         }
         catch (IOException e)
         {
            reportError("Error closing RolloverManager's writer", e, true);
         }

         try
         {
            fileOut.close();
         }
         catch (IOException e)
         {
            reportError("Error closing RolloverManager's file", e, true);
         }
      }
   }

   /**
    * Reports the given error to the error reporter, if there is one.
    *
    * @param description a description of the error
    *
    * @param e the exception that indicates the error
    *
    * @param printExceptionType a flag indicating whether the type of the exception should be
    * printed.
    */
   private void
   reportError(String description, IOException e, boolean printExceptionType)
   {
      if (errorReporter != null)
         errorReporter.error(description, e, printExceptionType);
   }

   /**
    * Does nothing, as the <code>RolloverManager</code> auto-flushes after every write()
    */
   public void
   flush()
   {
   }

   /**
    * Writes the output to the current writer and flushes it.
    *
    * @throws IOException if an error occurs writing to the current writer.
    */
   public void
   write(char cbuf[], int off, int len)
   throws IOException
   {
      // Writer priority:
      // Waiting for this lock prevents new printers from starting once a Writer Change starts
      synchronized (WRITER_CHANGE_GATE)
      {}

      // Increment the printers semaphore
      synchronized (PRINTERS_SEMAPHORE)
      {
         printerCount++;
      }

      try
      {
         writer.write(cbuf, off, len);
         writer.flush();
      }
      finally
      {
         // Decrement the printers semaphore and notify if we are last out
         synchronized (PRINTERS_SEMAPHORE)
         {
            printerCount--;
            if (printerCount == 0)
            {
               PRINTERS_SEMAPHORE.notifyAll();
            }
         }
      }
   }

   /**
    * Prompts the <code>RolloverManager</code> to check whether the log file needs to be rolled and
    * to perform that rolling if necessary.
    *
    * @throws IOException if an error occurs while rolling the log file.
    */
   public void
   rolloverIfNecessary()
   throws IOException
   {
      long creationTime = readCreationTime();
      FileChannel activeFileChannel = fileOut.getChannel();
      long fileLength = activeFileChannel.size();
      Date creationDate = new Date(creationTime);

      boolean rolloverNow = strategy.rolloverNow(creationDate, fileLength);

      if (rolloverNow)
      {
         // Switch to the temporary writer
         synchronized (WRITER_CHANGE_GATE)
         {
            synchronized (PRINTERS_SEMAPHORE)
            {
               if (printerCount != 0)
               {
                  try
                  {
                     PRINTERS_SEMAPHORE.wait();
                  }
                  catch (InterruptedException e)
                  {
                     throw new IllegalStateException("Interrupted while attempting to configure writer");
                  }
               }

               writer = tempWriter;
            }
         }

         // Create the rollover file
         int fileIdNumber = uniqueFileId++;
         StringBuffer fileIdString = new StringBuffer();
         fileIdString.append(Integer.toHexString(fileIdNumber).toUpperCase());
         char[] zeroes = new char[8 - fileIdString.length()];
         Arrays.fill(zeroes, '0');
         fileIdString.insert(0, zeroes);
         String rolloverFileName =
            rolloverLogFileFormat.format(new Object[] {new Date(), fileIdString.toString()});
         if (!rolloverDirectory.exists())
            rolloverDirectory.mkdirs();
         File rolloverFile = new File(rolloverDirectory, rolloverFileName);
         FileOutputStream rolloverOut = new FileOutputStream(rolloverFile, false);

         // Copy all the contents of the real file over to the rollover file
         FileChannel rolloverChannel = rolloverOut.getChannel();
         activeFileChannel.transferTo(0, activeFileChannel.size(), rolloverChannel);

         // Close the rollover file
         rolloverOut.close();

         // Truncate the real file and create a new writer
         activeFileChannel.truncate(0);
         fileOut.seek(0);
         Writer newFileWriter = Channels.newWriter(activeFileChannel, "UTF-8");

         //Write the active file creation time
         File creationTimeFile =
            getCreationTimeFile(currentActiveLogFileDirectory, currentActiveLogFile);
         creationTimeFile.delete();
         storeFileCreationTimeIfNecessary(currentActiveLogFileDirectory, currentActiveLogFile);

         // Switch back to the active file writer
         synchronized (WRITER_CHANGE_GATE)
         {
            synchronized (PRINTERS_SEMAPHORE)
            {
               if (printerCount != 0)
               {
                  try
                  {
                     PRINTERS_SEMAPHORE.wait();
                  }
                  catch (InterruptedException e)
                  {
                     throw new IllegalStateException("Interrupted while attempting to configure writer");
                  }
               }

               // Switch back to a real file writer.
               writer = newFileWriter;
               // Write the temporary contents to the file writer and clear it
               StringBuffer tempBuffer = tempWriter.getBuffer();
               newFileWriter.write(tempBuffer.toString());
               tempBuffer.delete(0, tempBuffer.length());
            }
         }
      }
   }

   /**
    * Returns the {@link RolloverStrategy} object currently in use by this
    * <code>RolloverManager</code>.
    */
   public RolloverStrategy
   getStrategy()
   {
      return strategy;
   }

   /**
    * Sets the {@link RolloverStrategy} to be used by this <code>RolloverManager</code>. Once the
    * strategy has been set using this method, it will never be changed by calls to
    * {@link #configure}, which includes auto-reloading of the properties.
    *
    * @param strategy the new strategy implementation to be used
    *
    * @throws IllegalArgumentException if <code>strategy</code> is <code>null</code>.
    */
   public void
   setStrategy(RolloverStrategy strategy)
   {
      if (strategy == null)
      {
         throw new IllegalArgumentException("strategy cannot be null.");
      }

      this.strategy = strategy;
      strategySetProgramatically = true;
   }

   /**
    * A {@link TimerTask} used to initiate {@link RolloverManager#rolloverIfNecessary}.
    */
   private final class
   RolloverTask
   extends TimerTask
   {
      public void
      run()
      {
         try
         {
            rolloverIfNecessary();
         }
         catch (IOException e)
         {
            reportError("SimpleLog ERROR: Failed to check or attempt rollover", e, true);
         }
      }
   }

   /**
    * An interface an object wishing to be notified of errors occurring in a {@link RolloverManager}
    * while it is running.
    */
   public static interface
   ErrorReporter
   {
      public void
      error(String description, Throwable t, boolean printExceptionType);
   }
}