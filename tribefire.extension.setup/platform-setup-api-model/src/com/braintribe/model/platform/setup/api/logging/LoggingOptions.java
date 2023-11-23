// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.platform.setup.api.logging;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface LoggingOptions extends GenericEntity {
	EntityType<LoggingOptions> T = EntityTypes.T(LoggingOptions.class);

	String consoleLogLevel = "consoleLogLevel";
	String logFilesLogLevel = "logFilesLogLevel";
	String logFilesDir = "logFilesDir";
	String logFilesMaxCount = "logFilesMaxCount";
	String logFilesMaxSizeInBytes = "logFilesMaxSizeInBytes";
	String logFilesCronRotate = "logFilesCronRotate";

	@Mandatory
	@Initializer("enum(com.braintribe.model.platform.setup.api.logging.LogLevel,INFO)")
	@Description("Log level for console output.")
	LogLevel getConsoleLogLevel();
	void setConsoleLogLevel(LogLevel consoleLogLevel);

	@Mandatory
	@Initializer("enum(com.braintribe.model.platform.setup.api.logging.LogLevel,FINE)")
	@Description("Log level for file output. This is used for the respective default log files of each container.")
	LogLevel getLogFilesLogLevel();
	void setLogFilesLogLevel(LogLevel logFilesLogLevel);

	@Description("Directory to log to.")
	String getLogFilesDir();
	void setLogFilesDir(String logFilesDir);

	@Min("0")
	@Initializer("20")
	@Description("Maximum number of archived log files before the oldest one gets deleted.")
	int getLogFilesMaxCount();
	void setLogFilesMaxCount(int logFilesMaxCount);

	@Min("0L")
	@Initializer("15000000L")
	@Description("Maximum size of a log file in bytes before it gets archived.")
	long getLogFilesMaxSize();
	void setLogFilesMaxSize(long logFilesMaxSizeInBytes);

	@Description("Cron string to set a time interval in which the current log file gets archived.")
	String getLogFilesCronRotate();
	void setLogFilesCronRotate(String logFilesCronRotate);

}
