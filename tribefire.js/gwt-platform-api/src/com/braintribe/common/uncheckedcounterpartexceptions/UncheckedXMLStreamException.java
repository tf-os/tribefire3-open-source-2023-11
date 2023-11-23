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
package com.braintribe.common.uncheckedcounterpartexceptions;

import javax.xml.stream.XMLStreamException;

import com.braintribe.common.lcd.uncheckedcounterpartexceptions.UncheckedCounterpartException;

/**
 * Unchecked counterpart of {@link XMLStreamException}.
 *
 * @author michael.lafite
 */
public class UncheckedXMLStreamException extends UncheckedCounterpartException {

	private static final long serialVersionUID = -4178100302071435348L;

	public UncheckedXMLStreamException(final String message, final XMLStreamException cause) {
		super(message, cause);
	}
}
