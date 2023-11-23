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

import com.braintribe.gwt.ioc.client.Configurable;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Builds a single from a {@link LogEvent} by
 * putting any information of the LogEvent to it.
 * @author Dirk
 *
 */
public class StandardStringFormatter implements Formatter<String> {
	private DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * 
	 * @param dateFormat is used to format the date of a {@link LogEvent} when formatting
	 * that event.
	 */
	@Configurable
	public void setDateFormat(DateTimeFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public String format(LogEvent e) {
		String date = dateFormat.format(e.getDate());
		return date +" [" + e.getLevel() + "] " + e.getCategory() + ": " + e.getMessage();
	}
}
