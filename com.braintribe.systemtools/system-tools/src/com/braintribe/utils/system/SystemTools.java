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
package com.braintribe.utils.system;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Locale;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.utils.system.exec.CommandExecution;
import com.braintribe.utils.system.exec.RunCommandContext;
import com.braintribe.utils.system.exec.RunCommandRequest;
import com.braintribe.utils.system.exec.SysTools;

public class SystemTools implements SysTools {

	private static Logger logger = Logger.getLogger(SystemTools.class);

	protected static String OS = null;
	protected static String OS_NAME = null;
	protected static String OS_VERSION = null;
	protected static String OS_ARCH = null;
	
	protected CommandExecution commandExecution = null;

	public static long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}
	public static long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * Returns the number of available processors or -1, if the count could not be acquired.
	 * 
	 * @return The number of processors, or -1 in case of an error.
	 */
	public static int getAvailableProcessors() {
		try {
			int count = Runtime.getRuntime().availableProcessors();
			return count;
		} catch(Exception e) {
			logger.debug("Could not get processor count by using the Runtime", e);
			try {
				OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
				int  availableProcessors = osBean.getAvailableProcessors();
				return availableProcessors;
			} catch(Exception e2) {
				logger.error("Could not get processor count by using the OperatingSystemMXBean", e2);
			}
		}
		return -1;
	}

	public static String getOperatingSystem() {
		if (OS == null) {
			if (OS_NAME == null) { 
				OS_NAME = System.getProperty("os.name"); 
			}
			if (OS_VERSION == null) {
				OS_VERSION = System.getProperty("os.version");
			}
			if (OS_ARCH == null) {
				OS_ARCH = System.getProperty("os.arch");
			}
			OS = "" + OS_NAME + " (version: " + OS_VERSION + ", architecture: "+ OS_ARCH +")";
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOperatingSystem().toLowerCase().indexOf("windows") >= 0;
	}
	public static boolean isMac() {
		return getOperatingSystem().toLowerCase().indexOf("mac") >= 0;
	}
	public static boolean isLinux() {
		return getOperatingSystem().toLowerCase().indexOf("linux") >= 0;
	}
	public static boolean isAix() {
		return getOperatingSystem().toLowerCase().indexOf("aix") >= 0;
	}
	public static boolean isUnix() {
		return getOperatingSystem().toLowerCase().indexOf("nix") >= 0;
	}
	public static boolean isSolaris() {
		return getOperatingSystem().toLowerCase().indexOf("sunos") >= 0;
	}
	public static boolean isHpUx() {
		String os = getOperatingSystem().toLowerCase(); 
		return ((os.indexOf("hpux") >= 0) || (os.indexOf("hp-ux") >= 0));
	}

	protected String executeCommandSilently(String[] command, String filter, boolean caseSensitiveFilter) {
		if ((command == null) || (command.length == 0)) {
			return null;
		}
		try {
			RunCommandContext context = this.commandExecution.runCommand(new RunCommandRequest(command, 5000L, true));
			String output = context.getOutput();
			if (filter == null) {
				return output;
			}
			if (output == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			String lines[] = output.split("\\r?\\n");
			if (lines != null) {
				for (String line : lines) {
					String lineForComparison = line;
					String compareString = filter;
					if (!caseSensitiveFilter) {
						lineForComparison = line.toLowerCase();
						compareString = filter.toLowerCase();
					}
					if (lineForComparison.indexOf(compareString) != -1) {
						sb.append(line);
						sb.append("\n");
					}
				}
			}
			return sb.toString();

		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			for (String c : command) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(c);
			}
			logger.debug("Could not execute the command "+sb.toString(), e);
			return null;
		}
	}

	@Override
	public String getDetailedProcessorInformation() {

		//HP-UX
		//ioscan -kfC processor
		//machinfo
		//getconf

		if (isLinux()) {
			String result = executeCommandSilently(new String[] {"/bin/sh", "-c", "cat /proc/cpuinfo"}, null, true);
			return result;

		} else if (isAix()) {
			String result = executeCommandSilently(new String[] {"/usr/sbin/lsdev", "-C", "-c", "processor"}, null, true);
			return result;

		} else if (isSolaris()) {
			String result = executeCommandSilently(new String[] {"/usr/sbin/psrinfo", "-v"}, null, true);
			String result2 = executeCommandSilently(new String[] {"/bin/sh", "-c", "dmesg"}, "cpu", true);
			StringBuilder sb = new StringBuilder();
			if (result != null) {
				sb.append(result);
			}
			if (result2 != null) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(result2);
			}
			return sb.toString();

		} else if (isMac()) {
			String result = executeCommandSilently(new String[] {"/usr/sbin/sysctl", "-a"}, "machdep.cpu", true);
			return result;

		} else if (isHpUx()) {
			String result = executeCommandSilently(new String[] {"/usr/sbin/ioscan", "-kfC", "processor"}, null, true);
			String result2 = executeCommandSilently(new String[] {"/usr/contrib/bin/machinfo"}, null, true);
			StringBuilder sb = new StringBuilder();
			if (result != null) {
				sb.append(result);
			}
			if (result2 != null) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(result2);
			}
			return sb.toString();
		} else if (isWindows()) {
			//not working (blocking): wmic cpu get name,CurrentClockSpeed,MaxClockSpeed,NumberOfCores,NumberOfLogicalProcessors
			String result = executeCommandSilently(new String[] {"systeminfo"}, null, true);
			return result;
		} else {
			logger.debug("Detailed Processor Information: Unsupported OS "+getOperatingSystem());
		}

		return null;
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPEZY" : "KMGTPEZY").charAt(exp-1) + (si ? "" : "i");
		//According to ISO-80000-1, either a dot or a comma is allowed as a decimal marker
		//So, we're sticking to the dot  
		return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String prettyPrintBytesBinary(long size) {
		return humanReadableByteCount(size, false);
	}
	public static String prettyPrintBytesDecimal(long size) {
		return humanReadableByteCount(size, true);
	}
	public static String prettyPrintBytes(long size) {
		return humanReadableByteCount(size, true) + " / "+humanReadableByteCount(size, false);
	}


	public static String getFileSystemInformation() {

		try {
			StringBuilder sb = new StringBuilder();

			FileSystem fileSystem = FileSystems.getDefault();
			if (fileSystem == null) {
				return "No default file system available.";
			}
			Iterable<FileStore> fileStoresIt = fileSystem.getFileStores();
			if (fileStoresIt == null || !fileStoresIt.iterator().hasNext()) {
				return "The default file system did not return any file store.";
			}
			for (FileStore fs : fileStoresIt) {
				sb.append("---------\n");
				sb.append(fs.toString());
				sb.append('\n');
				sb.append("Usable space:\t");
				try {
					long space = fs.getUsableSpace();
					sb.append(prettyPrintBytes(space));
					sb.append(" (");
					sb.append(space);
					sb.append(")");
				} catch(Exception ioe) {
					logger.debug("Could not get usable space from filestore "+fs, ioe);
					sb.append("Unknown");
				}
				sb.append('\n');
				sb.append("Free space:\t\t");
				try {
					long space = fs.getUnallocatedSpace();
					sb.append(prettyPrintBytes(space));
					sb.append(" (");
					sb.append(space);
					sb.append(")");
				} catch(Exception ioe) {
					logger.debug("Could not get free space from filestore "+fs, ioe);
					sb.append("Unknown");
				}
				sb.append('\n');
				sb.append("Total space:\t");
				try {
					long space = fs.getTotalSpace();
					sb.append(prettyPrintBytes(space));
					sb.append(" (");
					sb.append(space);
					sb.append(")");
				} catch(Exception ioe) {
					logger.debug("Could not get total space from filestore "+fs, ioe);
					sb.append("Unknown");
				}
				sb.append('\n');
				sb.append("Is read-only:\t");
				try {
					sb.append(fs.isReadOnly());
				} catch(Exception ioe) {
					logger.debug("Could not get read-only flag from filestore "+fs, ioe);
					sb.append("Unknown");
				}
				sb.append('\n');
			}

			return sb.toString();
		} catch(Exception e) {
			logger.error("Error while trying to get filesystem information.", e);
			return "Could not gather file system information.";
		}
	}

	public static String getPrettyPrintFreeSpaceOnDiskDevice(File anyFile) {
		File currentFile = anyFile;
		while (currentFile != null) {
			try {
				FileStore fs = Files.getFileStore(currentFile.toPath());
				StringBuilder sb = new StringBuilder();
				if (fs != null) {
					long usableSpace = fs.getUsableSpace();
					String usableSpaceString = prettyPrintBytes(usableSpace);
					sb.append("Device '");
					sb.append(fs.toString());
					sb.append("': ");
					sb.append(usableSpaceString);
				} else {
					sb.append("No file store");
				}
				return sb.toString();
			} catch(NoSuchFileException nsfe) {
				try {
					currentFile = currentFile.getParentFile();
				} catch(Exception e) {
					if (logger.isDebugEnabled()) logger.debug("Could not get parent file of "+currentFile.getAbsolutePath(), e);
				}
			} catch(Exception e) {
				logger.debug(() -> "Could not compute free file space.");
				logger.trace(() -> "Could not compute free file space.", e);
				return null;
			}
		}
		return null;
	}

	@Configurable
	@Required
	public void setCommandExecution(CommandExecution commandExecution) {
		this.commandExecution = commandExecution;
	}

}
