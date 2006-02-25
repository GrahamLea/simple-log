package org.grlea.log.test;

// $Id: TestOfPropertiesLocationOptions.java,v 1.1 2006-02-25 23:34:56 grlea Exp $
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

import org.grlea.log.SimpleLogger;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * <p></p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public class
TestOfPropertiesLocationOptions
extends TestCase
{
   private static final File workingDirectory = new File("propertiesLocationTests");

   private static final File absoluteFileDir = new File(System.getProperty("java.io.tmpdir"));
   private static final File absoluteFile = new File(absoluteFileDir, "absoluteSimpleLog.properties");

   private static final String RELATIVE_FILE_DIR_NAME = "config";
   private static final File relativeFileDir = new File(workingDirectory, RELATIVE_FILE_DIR_NAME);
   private static final String RELATIVE_PROPERTIES = "relativeSimpleLog.properties";
   private static final File relativeFile = new File(relativeFileDir, RELATIVE_PROPERTIES);

   private static final File classpathDir = new File(workingDirectory, "classpath");
   private static final File defaultFile = new File(classpathDir, "simplelog.properties");

   private static final String IN_CLASSPATH_PATH = "inClasspathTest";
   private static final File inClasspathFileDir = new File(classpathDir, IN_CLASSPATH_PATH);
   private static final String IN_CLASSPATH_PROPERTIES = "inClasspathSimpleLog.properties";
   private static final File inClasspathFile = new File(inClasspathFileDir,
                                                        IN_CLASSPATH_PROPERTIES);

   private static final File outputDir = new File(workingDirectory, "log");
   private static final File outputFile = new File(outputDir,
                                                   TestOfPropertiesLocationOptions.class.getName() +
                                                   ".log");

   public
   TestOfPropertiesLocationOptions()
   {}

   protected void
   setUp()
   throws Exception
   {
      if (!absoluteFileDir.exists())
         absoluteFileDir.mkdirs();

      if (!relativeFileDir.exists())
         relativeFileDir.mkdirs();

      if (!classpathDir.exists())
         classpathDir.mkdirs();

      if (!inClasspathFileDir.exists())
         inClasspathFileDir.mkdirs();

      if (!outputDir.exists())
         outputDir.mkdirs();
   }

   protected void
   tearDown()
   throws Exception
   {
      if (absoluteFileDir.exists())
         absoluteFileDir.delete();

      if (relativeFileDir.exists())
         relativeFileDir.delete();

      if (classpathDir.exists())
         classpathDir.delete();

      if (outputDir.exists())
         outputDir.delete();
   }

   public void
   testZeroOutput()
   throws Exception
   {
      writeProperties(0, absoluteFile);
      writeProperties(0, relativeFile);
      writeProperties(0, defaultFile);
      writeProperties(0, inClasspathFile);

      runLogGenerator(null);

      long logFileSize = outputFile.length();
      assertEquals("logFileSize > 0", false, logFileSize > 0);
   }

   public void
   testDefaultLocation()
   throws Exception
   {
      writeProperties(0, absoluteFile);
      writeProperties(0, relativeFile);
      writeProperties(7, defaultFile);
      writeProperties(0, inClasspathFile);

      runLogGenerator(null);

      long logFileSize = outputFile.length();
      assertEquals("logFileSize > 0", true, logFileSize > 0);
   }

   public void
   testAbsolutePropetiesLocation()
   throws Exception
   {
      writeProperties(7, absoluteFile);
      writeProperties(0, relativeFile);
      writeProperties(0, defaultFile);
      writeProperties(0, inClasspathFile);

      runLogGenerator("file:" + absoluteFile.getAbsolutePath());

      long logFileSize = outputFile.length();
      assertEquals("logFileSize > 0", true, logFileSize > 0);
   }

   public void
   testRelativePropetiesLocation()
   throws Exception
   {
      writeProperties(0, absoluteFile);
      writeProperties(7, relativeFile);
      writeProperties(0, defaultFile);
      writeProperties(0, inClasspathFile);

      runLogGenerator("file:" + RELATIVE_FILE_DIR_NAME + File.separator + RELATIVE_PROPERTIES);

      long logFileSize = outputFile.length();
      assertEquals("logFileSize > 0", true, logFileSize > 0);
   }

   public void
   testInClasspathPropetiesLocation()
   throws Exception
   {
      writeProperties(0, absoluteFile);
      writeProperties(0, relativeFile);
      writeProperties(0, defaultFile);
      writeProperties(7, inClasspathFile);

      runLogGenerator("classpath:" + IN_CLASSPATH_PATH + File.separator + IN_CLASSPATH_PROPERTIES);

      long logFileSize = outputFile.length();
      assertEquals("logFileSize > 0", true, logFileSize > 0);
   }

   private void
   runLogGenerator(String logfileLocationPropertyValue)
   throws Exception
   {
      String javaHomeString = System.getProperty("java.home");
      File javaHome = new File(javaHomeString);
      File javaBin = new File(javaHome, "bin");
      File javaExe = new File(javaBin, "java");
      if (!javaExe.exists())
         javaExe = new File(javaBin, "java.exe");
      if (!javaExe.exists())
         fail("Failed to find java executable in " + javaBin.getAbsolutePath());

      String classpath = System.getProperty("java.class.path");
      classpath = classpathDir.getAbsolutePath() + File.pathSeparator + classpath;

      String[] commandLine =
         (logfileLocationPropertyValue == null) ?
            new String[] {javaExe.getAbsolutePath(), "-cp", classpath, LogGenerator.class.getName()} :
            new String[] {javaExe.getAbsolutePath(), "-cp", classpath,
                          "-Dsimplelog.configuration=" + logfileLocationPropertyValue,
                          LogGenerator.class.getName()};

//      for (int i = 0; i < commandLine.length; i++)
//         System.out.println("\t" + commandLine[i]);

      Process process = Runtime.getRuntime().exec(commandLine, new String[0], workingDirectory);
      process.waitFor();
   }

   private void
   writeProperties(int defaultLevel, File file)
   throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("simplelog.defaultLevel", String.valueOf(defaultLevel));
      properties.setProperty("simplelog.logFile", outputFile.getAbsolutePath());
      properties.setProperty("simplelog.logFile.append", "false");
      FileOutputStream out = new FileOutputStream(file);
      try
      {
         properties.store(out, "");
      }
      finally
      {
         out.close();
      }
   }

   public static final class
   LogGenerator
   {
      private static final SimpleLogger log = new SimpleLogger(LogGenerator.class);

      public static void
      main(String[] args)
      {
//         JOptionPane.showMessageDialog(null, "LogGenerator running!");
         log.ludicrous("Ludicrous");
         System.exit(0);
      }
   }
}