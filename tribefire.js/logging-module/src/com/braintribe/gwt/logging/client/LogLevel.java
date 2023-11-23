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
package com.braintribe.gwt.logging.client;

/**
 * This enum is used to define different levels
 * of log levels. Any {@link LogEvent} has
 * one level and could by some filter mechanism
 * filtered using that level.
 * @author Dirk
 *
 */
public enum LogLevel implements Comparable<LogLevel>{
	FATAL(0), ERROR(3), WARN(4), INFO(6), DEBUG(7), PROFILING(8), PROFILINGDEBUG(9), TRACE(10);
	
	private int level;
	
	private LogLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
