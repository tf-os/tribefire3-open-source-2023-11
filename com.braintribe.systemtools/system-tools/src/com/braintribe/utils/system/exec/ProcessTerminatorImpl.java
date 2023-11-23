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
package com.braintribe.utils.system.exec;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.braintribe.logging.Logger;

/**
 * Utility class used to
 * terminate external processes after a timeout.
 * 
 * @author roman.kurmanowytsch
 */
public class ProcessTerminatorImpl implements Runnable, ProcessTerminator {

	private static Logger logger = Logger.getLogger(ProcessTerminatorImpl.class);

	protected ConcurrentLinkedDeque<ProcessInformation> processes = new ConcurrentLinkedDeque<ProcessInformation>();

	/**
	 * This method checks whether the process is still running.
	 * If so, the process will be destroyed.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			// iterare through the list of still running
			// processes
			long now = System.currentTimeMillis();
			for (Iterator<ProcessInformation> it = this.processes.iterator(); it.hasNext();) {
				ProcessInformation pi = it.next();
				// has it reached its timeout?
				if (pi.exitTime < now) {
					// remove it from the list and check whether it is still running
					it.remove();
					try {
						// this will throw an exception if it is not terminated
						pi.process.exitValue();
						// at this point, the process has terminated already
						// no further action needed
					} catch (IllegalThreadStateException notYetTerminatedException) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
						logger.debug("Forcing termination of external command after timeout: " + pi.toString()
						+ " (Now: " + sdf.format(now) + ")");
						pi.process.destroy();
					}
				}
			}
		} catch(Exception e) {
			logger.debug("Error during checking running processes.", e);
		}
	}

	/**
	 * Adds an external process to the list.
	 * 
	 * @param cmd The command that has been executed (for logging)
	 * @param commandProcess The Process itself.
	 * @param timeout The maximum time span in milliseconds
	 */
	@Override
	public void addProcess(String cmd, Process commandProcess, long timeout) {
		ProcessInformation pi = new ProcessInformation(cmd, commandProcess, timeout);
		this.processes.add(pi);
	}


	class ProcessInformation {
		public String cmd = null;
		public Process process = null;
		public long exitTime = -1;

		public ProcessInformation(String cmd, Process process, long timeout) {
			this.cmd = cmd;
			this.process = process;
			this.exitTime = System.currentTimeMillis() + timeout;
		}

		@Override
		public String toString() {
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
			return this.cmd + " (Timeout: " + sdf.format(this.exitTime) + ")";
		}
	}
}
