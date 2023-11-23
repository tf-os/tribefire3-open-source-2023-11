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
package com.braintribe.model.processing.platformreflection.os;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.platformreflection.os.Process;
import com.braintribe.utils.MathTools;
import com.braintribe.utils.StringTools;

import oshi.software.os.OSProcess;

public class ProcessesProvider {

	private static Logger logger = Logger.getLogger(ProcessesProvider.class);

	public static List<Process> getProcesses(oshi.SystemInfo si, boolean onlyJavaProcesses) {

		logger.debug(() -> "Getting processes");

		try {
			List<Process> processList = new ArrayList<Process>();

			String localProcessName = ManagementFactory.getRuntimeMXBean().getName();

			List<OSProcess> processes = si.getOperatingSystem().getProcesses();
			if (processes != null) {
				for (OSProcess op : processes) {

					String path = op.getPath();
					if (path == null) {
						continue;
					}
					path = path.toLowerCase();

					boolean addProcessToList = true;
					if (onlyJavaProcesses) {
						if (!(path.indexOf("java") != -1 || path.indexOf("tomcat") != -1)) {
							addProcessToList = false;
						}
					}

					if (addProcessToList) {

						Process process = Process.T.create();

						int processID = op.getProcessID();

						long kernelTime = op.getKernelTime();
						process.setKernelTimeInMs(kernelTime);
						process.setKernelTimeDisplay(StringTools.prettyPrintMilliseconds(kernelTime, true));
						long userTime = op.getUserTime();
						process.setUserTimeInMs(userTime);
						process.setUserTimeDisplay(StringTools.prettyPrintMilliseconds(userTime, true));
						process.setOpenFiles(op.getOpenFiles());
						process.setCurrentWorkingDirectory(op.getCurrentWorkingDirectory());

						process.setCommandLine(replaceNulls(op.getCommandLine()));
						process.setUser(op.getUser());
						process.setUserId(op.getUserID());
						process.setGroup(op.getGroup());
						process.setGroupId(op.getGroupID());
						long residentSetSize = op.getResidentSetSize();
						process.setResidentSetSize(residentSetSize);
						process.setResidentSetSizeInGb(MathTools.getNumberInG(residentSetSize, true, 2));
						long bytesRead = op.getBytesRead();
						process.setBytesRead(bytesRead);
						process.setBytesReadInGb(MathTools.getNumberInG(bytesRead, true, 2));
						long bytesWritten = op.getBytesWritten();
						process.setBytesWritten(bytesWritten);
						process.setBytesWrittenInGb(MathTools.getNumberInG(bytesWritten, true, 2));

						process.setName(op.getName());
						process.setParentProcessId(op.getParentProcessID());
						process.setPath(op.getPath());
						process.setPriority(op.getPriority());
						process.setProcessId(processID);
						Date startTime = new Date(op.getStartTime());
						process.setStartTime(startTime);
						process.setState(op.getState().name());
						process.setThreadCount(op.getThreadCount());
						long upTime = op.getUpTime();
						process.setUptime(upTime);
						process.setUptimeDisplay(StringTools.prettyPrintMilliseconds(upTime, true));
						long virtualSize = op.getVirtualSize();
						process.setVirtualSize(virtualSize);
						process.setVirtualSizeInGb(MathTools.getNumberInG(virtualSize, true, 2));

						if (localProcessName.indexOf("" + processID) != -1) {
							process.setIsCurrentProcess(true);
						} else {
							process.setIsCurrentProcess(false);
						}

						processList.add(process);
					}
				}
			}

			return processList;
		} finally {
			logger.debug(() -> "Done with getting processes");
		}
	}

	private static String replaceNulls(String text) {
		if (text == null) {
			return null;
		}
		String cleanText = text.replace('\0', ' ');
		return cleanText.trim();
	}
}
