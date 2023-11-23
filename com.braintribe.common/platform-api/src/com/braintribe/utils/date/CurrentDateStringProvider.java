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
package com.braintribe.utils.date;

import java.util.Date;
import java.util.function.Supplier;

import com.braintribe.utils.DateTools;

/**
 * Provides the current date in the specified {@link #setDateFormatPattern(String) pattern} plus optional {@link #setPrefix(String) prefix} and
 * {@link #setSuffix(String) suffix}.
 *
 * @author michael.lafite
 */
public class CurrentDateStringProvider implements Supplier<String> {

	private String dateFormatPattern = DateTools.DEFAULT_DATEFORMAT_PATTERN;
	private String prefix = "";
	private String suffix = "";

	public CurrentDateStringProvider() {
		// nothing to do
	}

	public String getDateFormatPattern() {
		return this.dateFormatPattern;
	}

	public void setDateFormatPattern(final String dateFormatPattern) {
		this.dateFormatPattern = dateFormatPattern;
		// make sure pattern is valid
		get();
	}

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return this.suffix;
	}

	public void setSuffix(final String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String get() {
		final String datePart = new ExtSimpleDateFormat(getDateFormatPattern()).format(new Date());
		final String result = getPrefix() + datePart + getSuffix();
		return result;
	}

}
