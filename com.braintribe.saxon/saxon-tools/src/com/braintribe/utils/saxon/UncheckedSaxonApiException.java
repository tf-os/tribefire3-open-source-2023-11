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
package com.braintribe.utils.saxon;

import com.braintribe.common.lcd.uncheckedcounterpartexceptions.UncheckedCounterpartException;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Unchecked counterpart of {@link SaxonApiException}.
 *
 * @author michael.lafite
 */
public class UncheckedSaxonApiException extends UncheckedCounterpartException {

	private static final long serialVersionUID = 1316641584745480080L;

	public UncheckedSaxonApiException(final String message) {
		super(message);
	}

	public UncheckedSaxonApiException(final String message, final SaxonApiException cause) {
		super(message, cause);
	}
}
