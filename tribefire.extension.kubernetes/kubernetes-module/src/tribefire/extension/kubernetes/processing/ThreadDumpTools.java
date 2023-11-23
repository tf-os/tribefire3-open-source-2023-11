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
package tribefire.extension.kubernetes.processing;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.system.SystemTools;
import com.braintribe.utils.system.exec.CommandExecutionImpl;
import com.braintribe.utils.system.exec.ProcessTerminatorImpl;
import com.braintribe.utils.system.exec.RunCommandContext;
import com.braintribe.utils.system.exec.RunCommandRequest;

public class ThreadDumpTools {

	private final static Logger logger = Logger.getLogger(ThreadDumpTools.class);

	public static String getThreadDump() throws Exception {

		// First, we try to get a native thread dump. If that does not work out (e.g., no JDK is installed), we use a
		// minimal Java-internal dump

		String dump = getThreadDumpNative();
		if (dump == null || dump.trim().length() == 0) {
			dump = getThreadDumpJava();
		}
		return dump;
	}

	protected static String getThreadDumpNative() {
		logger.debug(() -> "Trying to create a native thread dump.");
		try {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			String localProcessName = runtimeMXBean.getName();
			int idx = localProcessName.indexOf('@');
			int pid = -1;
			if (idx != -1) {
				try {
					pid = Integer.parseInt(localProcessName.substring(0, idx));
				} catch (Exception e) {
					logger.debug(() -> "Could not get the local PID from the process name " + localProcessName);
					return null;
				}
			}
			if (pid == -1) {
				logger.debug("The process name " + localProcessName + " does not provide the PID.");
				return null;
			}
			String javaHome = System.getProperty("java.home");
			File javaHomeDir = new File(javaHome);

			String threadDump = null;
			File jcmdExecutable = findJCmd(javaHomeDir);
			if (jcmdExecutable != null) {
				threadDump = getThreadDumpNative(jcmdExecutable, pid, "" + pid, "Thread.print");
			}
			if (threadDump == null) {
				File jstackExecutable = findJStack(javaHomeDir);
				threadDump = getThreadDumpNative(jstackExecutable, pid, "-l", "" + pid);
			}
			return threadDump;

		} catch (Exception e) {
			logger.debug(() -> "Could not get the native thread dump.", e);
		}
		return null;
	}

	protected static String getThreadDumpNative(File executable, int pid, String... arguments) {
		if (executable == null) {
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Using executable " + executable.getAbsolutePath() + " to get a thread dump from process " + pid);
		}

		try {
			ProcessTerminatorImpl processTerminator = new ProcessTerminatorImpl();

			CommandExecutionImpl commandExecution = new CommandExecutionImpl();
			commandExecution.setProcessTerminator(processTerminator);

			List<String> execParts = new ArrayList<>(arguments.length + 1);
			execParts.add(executable.getAbsolutePath());
			CollectionTools.addElementsToCollection(execParts, arguments);

			RunCommandRequest request = new RunCommandRequest(execParts.toArray(new String[0]), 5000L);
			RunCommandContext context = commandExecution.runCommand(request);
			int errorCode = context.getErrorCode();
			if (errorCode == 0) {

				logger.debug(() -> "Creating a native thread dump succeeded.");

				String output = context.getOutput();
				if (StringTools.isBlank(output)) {
					return null;
				}
				return output;
			} else {
				logger.debug("Executing " + executable.getAbsolutePath() + " with PID " + pid + " resulted in: " + context.toString());
			}
		} catch (Exception e) {
			logger.debug(() -> "Could not get the native thread dump using " + executable.getAbsolutePath(), e);
		}
		return null;
	}

	protected static File findJStack(File javaHomeDir) {
		return findJavaBinExecutable(javaHomeDir, "jstack");
	}

	protected static File findJCmd(File javaHomeDir) {
		return findJavaBinExecutable(javaHomeDir, "jcmd");
	}

	protected static File findJavaBinExecutable(File javaHomeDir, String execName) {
		if (!javaHomeDir.exists()) {
			return null;
		}
		String executable = SystemTools.isWindows() ? execName + ".exe" : execName;

		List<File> toInspectList = new ArrayList<File>();
		toInspectList.add(javaHomeDir.getParentFile());
		while (!toInspectList.isEmpty()) {
			File dir = toInspectList.remove(0);
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						toInspectList.add(f);
					} else {
						String name = f.getName().toLowerCase();
						if (name.equals(executable)) {
							logger.debug(() -> "Found " + execName + " at: " + f.getAbsolutePath());
							return f;
						}
					}
				}
			}
		}

		logger.debug(() -> "Could not find " + execName);

		return null;
	}

	protected static String getThreadDumpJava() {

		logger.debug(() -> "Trying to create a thread dump within the JVM.");

		try {
			final StringBuilder dump = new StringBuilder();
			final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);

			Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
			Map<Long, Map.Entry<Thread, StackTraceElement[]>> threadMap = new HashMap<>();
			for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
				threadMap.put(entry.getKey().getId(), entry);
			}

			dump.append(DateTools.encode(new Date(), DateTools.ISO8601_DATE_FORMAT));
			dump.append('\n');
			dump.append("Full thread dump");

			RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
			String vmName = rmx.getVmName();
			if (vmName != null) {
				dump.append(' ');
				dump.append(vmName);
				String vmVersion = rmx.getVmVersion();
				if (vmVersion != null) {
					dump.append(" (");
					dump.append(vmVersion);
					dump.append(')');
				}
			}
			dump.append("\n\n");

			for (ThreadInfo threadInfo : threadInfos) {

				String threadName = threadInfo.getThreadName();
				Map.Entry<Thread, StackTraceElement[]> entry = threadMap.get(threadInfo.getThreadId());
				if (entry != null) {

					Thread thread = entry.getKey();

					dump.append(String.format("\"%s\" %sprio=%d tid=%d nid=1 %s\n   java.lang.Thread.State: %s", threadName,
							(thread.isDaemon() ? "daemon " : ""), thread.getPriority(), thread.getId(),
							Thread.State.WAITING.equals(thread.getState()) ? "in Object.wait()" : thread.getState().name().toLowerCase(),
							(thread.getState().equals(Thread.State.WAITING) ? "WAITING (on object monitor)" : thread.getState())));

					final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
					for (final StackTraceElement stackTraceElement : stackTraceElements) {
						dump.append("\n\tat ");
						dump.append(stackTraceElement);
					}

					dump.append("\n\n");

				}
			}

			logger.debug(() -> "Successfully created a thread dump within the JVM.");

			return dump.toString();
		} catch (Exception e) {
			logger.error("Could not create a thread dump.", e);
			return null;
		}
	}
}
