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
package com.braintribe.model.platformreflection.os;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Process extends GenericEntity {

	EntityType<Process> T = EntityTypes.T(Process.class);

	String getName();
	void setName(String name);

	int getParentProcessId();
	void setParentProcessId(int parentProcessId);

	int getProcessId();
	void setProcessId(int processId);

	String getPath();
	void setPath(String path);

	int getPriority();
	void setPriority(int priority);

	String getState();
	void setState(String state);

	int getThreadCount();
	void setThreadCount(int threadCount);

	long getKernelTimeInMs();
	void setKernelTimeInMs(long kernelTimeInMs);

	String getKernelTimeDisplay();
	void setKernelTimeDisplay(String kernelTimeDisplay);

	long getUserTimeInMs();
	void setUserTimeInMs(long userTimeInMs);

	String getUserTimeDisplay();
	void setUserTimeDisplay(String userTimeDisplay);

	Date getStartTime();
	void setStartTime(Date startTime);

	long getUptime();
	void setUptime(long uptime);

	String getUptimeDisplay();
	void setUptimeDisplay(String uptimeDisplay);

	long getVirtualSize();
	void setVirtualSize(long virtualSize);

	Double getVirtualSizeInGb();
	void setVirtualSizeInGb(Double virtualSizeInGb);

	boolean getIsCurrentProcess();
	void setIsCurrentProcess(boolean isCurrentProcess);

	String getCommandLine();
	void setCommandLine(String commandLine);

	String getUser();
	void setUser(String user);

	String getUserId();
	void setUserId(String userId);

	String getGroup();
	void setGroup(String group);

	String getGroupId();
	void setGroupId(String groupId);

	long getResidentSetSize();
	void setResidentSetSize(long residentSetSize);
	
	Double getResidentSetSizeInGb();
	void setResidentSetSizeInGb(Double residentSetSizeInGb);

	long getBytesRead();
	void setBytesRead(long bytesRead);

	Double getBytesReadInGb();
	void setBytesReadInGb(Double bytesReadInGb);

	long getBytesWritten();
	void setBytesWritten(long bytesWritten);
	
	Double getBytesWrittenInGb();
	void setBytesWrittenInGb(Double bytesWrittenInGb);

	long getOpenFiles();
	void setOpenFiles(long openFiles);
	
	String getCurrentWorkingDirectory();
	void setCurrentWorkingDirectory(String currentWorkingDirectory);
}
