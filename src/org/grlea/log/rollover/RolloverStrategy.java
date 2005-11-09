package org.grlea.log.rollover;

// $Id: RolloverStrategy.java,v 1.1 2005-11-09 21:47:55 grlea Exp $
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
 * <p>An interface for objects that wish to be able to decide when log files should be rolled over.
 * </p>
 *
 * @author Graham Lea
 * @version $Revision: 1.1 $
 */
public interface
RolloverStrategy
{
   /**
    * Reads from the given map any properties that affect the behaviour of this strategy.
    *
    * @param properties an unmodifiable map of properties, i.e. String keys with String values
    *
    * @throws IOException if an error occurs while configuring the strategy. 
    */
   public void
   configure(Map properties)
   throws IOException;

   /**
    * Decides whether or not the log file should be rolled over immediately.
    *
    * @param fileCreated the date and time at which the current log file was created
    * @param fileLength the current length of the log file
    *
    * @return <code>true</code> if the log file should be rolled over now, <code>false</code> if it
    * should not.
    */
   public boolean
   rolloverNow(Date fileCreated, long fileLength);
}