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

import java.util.Date;

/**
 * Instances of this class will be sent as events by any logger
 * via the {@link LogManager}.
 * The event automatically gets the date of its creation.
 * @author Dirk
 *
 */
public class LogEvent {
	private static Long ID_COUNTER = 0l;
	private LogLevel level;
	private String message;
	private String category;
	private Date date;
	private Long id;
	
	public LogEvent(LogLevel level, String category, String message) {
		super();
		this.id = ID_COUNTER++;
		this.date = new Date();
		this.level = level;
		this.message = message;
		this.category = category;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getCategory() {
		return category;
	}
	
	public Date getDate() {
		return date;
	}
	
	public LogLevel getLevel() {
		return level;
	}
	
	public String getMessage() {
		return message;
	}
	
}
