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

/**
 * Utility class used to terminate external processes after a timeout.
 * 
 * @author roman.kurmanowytsch
 */
public interface ProcessTerminator {
	
	/**
	 * Adds an external process to the list.
	 * 
	 * @param cmd The command that has been executed (for logging)
	 * @param commandProcess The Process itself.
	 * @param timeout The maximum time span in milliseconds
	 */
	void addProcess(String cmd, Process commandProcess, long timeout);

}
