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

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Immutable version of {@link ExtSimpleDateFormat} that cannot be modified after initialization, i.e. all <code>set/apply</code> methods throw an
 * {@link UnsupportedOperationException}.
 *
 * @author michael.lafite
 */

public class ImmutableExtSimpleDateFormat extends ExtSimpleDateFormat {

	private static final long serialVersionUID = 4343404207569300340L;

	private static final String ERROR_MESSAGE = "Instances of " + ImmutableExtSimpleDateFormat.class.getName()
			+ " cannot be changed after initialization. Use " + ExtSimpleDateFormat.class.getName() + " instead.";

	public ImmutableExtSimpleDateFormat(final String pattern) {
		super(pattern);
	}

	public ImmutableExtSimpleDateFormat(final String pattern, final Locale locale) {
		super(pattern, locale);
	}

	@Override
	public void set2DigitYearStart(final Date startDate) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void applyPattern(final String pattern) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void applyLocalizedPattern(final String pattern) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void setDateFormatSymbols(final DateFormatSymbols newFormatSymbols) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void setCalendar(final Calendar newCalendar) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void setNumberFormat(final NumberFormat newNumberFormat) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void setTimeZone(final TimeZone zone) {
		throw new UnsupportedOperationException(ERROR_MESSAGE);
	}

	@Override
	public void setLenient(final boolean lenient) {
		if (isInitialized()) {
			throw new UnsupportedOperationException(ERROR_MESSAGE);
		} else {
			super.setLenient(lenient);
		}
	}
}
